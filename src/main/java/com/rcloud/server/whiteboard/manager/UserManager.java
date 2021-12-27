package com.rcloud.server.whiteboard.manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.rcloud.server.whiteboard.constant.Constants;
import com.rcloud.server.whiteboard.constant.ErrorCode;
import com.rcloud.server.whiteboard.constant.SmsServiceType;
import com.rcloud.server.whiteboard.domain.*;
import com.rcloud.server.whiteboard.exception.ServiceException;
import com.rcloud.server.whiteboard.model.ServerApiParams;
import com.rcloud.server.whiteboard.rongcloud.RongCloudClient;
import com.rcloud.server.whiteboard.service.*;
import com.rcloud.server.whiteboard.sms.SmsService;
import com.rcloud.server.whiteboard.sms.SmsServiceFactory;
import com.rcloud.server.whiteboard.spi.verifycode.VerifyCodeAuthentication;
import com.rcloud.server.whiteboard.spi.verifycode.VerifyCodeAuthenticationFactory;
import com.rcloud.server.whiteboard.util.*;
import io.micrometer.core.instrument.util.IOUtils;
import io.rong.models.Result;
import io.rong.models.response.TokenResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Author: xiuwei.nie
 * @Author: Jianlu.Yu
 * @Date: 2020/7/7
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Service
@Slf4j
public class UserManager extends BaseManager {

    @Resource
    private RongCloudClient rongCloudClient;

    @Resource
    private VerificationCodesService verificationCodesService;

    @Resource
    private VerificationViolationsService verificationViolationsService;

    @Resource
    private UsersService usersService;

    @Value("classpath:region.json")
    private org.springframework.core.io.Resource regionResource;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private HttpClient httpClient;

    private Integer SUCCESS_CODE = 200;


    /**
     * 向手机发送验证码
     */
    public void sendCode(String region, String phone, SmsServiceType smsServiceType, ServerApiParams serverApiParams) throws ServiceException {
        log.info("send code. region:[{}] phone:[{}] smsServiceType:[{}]", region, phone, smsServiceType.getCode());

        region = MiscUtils.removeRegionPrefix(region);
        ValidateUtils.checkRegion(region);
        ValidateUtils.checkCompletePhone(phone);

        String ip = serverApiParams.getRequestUriInfo().getIp();

        // 获取最近一次发送验证码记录
        VerificationCodes verificationCodes = verificationCodesService.getByRegionAndPhone(region, phone);
        if (verificationCodes != null) {
            checkLimitTime(verificationCodes);
        }

        //云片服务获取验证码，检查IP请求频率限制
        checkRequestFrequency(ip);

        //保存或更新verificationCodes、 发送验证码
        upsertAndSendToSms(region, phone, smsServiceType);

        refreshRequestFrequency(ip);
    }

    /**
     * 刷新ip请求频率
     *
     * @param ip
     */
    private void refreshRequestFrequency(String ip) {
        //更新verification_violations ip地址访问时间次和数
        VerificationViolations verificationViolations = verificationViolationsService.getByPrimaryKey(ip);
        if (verificationViolations == null) {
            verificationViolations = new VerificationViolations();
            verificationViolations.setIp(ip);
            verificationViolations.setCount(1);
            verificationViolations.setTime(new Date());
            verificationViolationsService.saveSelective(verificationViolations);
        } else {
            DateTime dateTime = new DateTime(new Date());
            dateTime = dateTime.minusHours(whiteboardConfig.getSmsLimitedTime());
            Date limitDate = dateTime.toDate();
            if (limitDate.after(verificationViolations.getTime())) {
                //如果上次记录时间已经是1小时前，重置计数和开始时间
                verificationViolations.setCount(1);
                verificationViolations.setTime(new Date());
            } else {
                verificationViolations.setCount(verificationViolations.getCount() + 1);
            }
            verificationViolationsService.updateByPrimaryKeySelective(verificationViolations);
        }

    }

    /**
     * 发送短信并更新数据库
     */
    private VerificationCodes upsertAndSendToSms(String region, String phone, SmsServiceType smsServiceType) throws ServiceException {
        if (Constants.ENV_DEV.equals(whiteboardConfig.getConfigEnv())) {
            //开发环境直接插入数据库，不调用短信接口
            return verificationCodesService.saveOrUpdate(region, phone, "");
        } else {
            SmsService smsService = SmsServiceFactory.getSmsService(smsServiceType);
            String sessionId = smsService.sendVerificationCode(region, phone);
            return verificationCodesService.saveOrUpdate(region, phone, sessionId);
        }
    }

    /**
     * 检查发送时间限制
     * 开发环境间隔5秒后  可再次请求发送验证码
     * 生产环境间隔1分钟后 可再次请求发送验证码
     *
     * @param verificationCodes
     * @throws ServiceException
     */
    private void checkLimitTime(VerificationCodes verificationCodes)
            throws ServiceException {

        DateTime dateTime = new DateTime(new Date());
        if (Constants.ENV_DEV.equals(whiteboardConfig.getConfigEnv())) {
            dateTime = dateTime.minusSeconds(50);
        } else {
            dateTime = dateTime.minusMinutes(1);
        }
        Date limitDate = dateTime.toDate();

        if (limitDate.before(verificationCodes.getUpdatedAt())) {
            throw new ServiceException(ErrorCode.LIMIT_ERROR);
        }
    }

    /**
     * IP请求频率限制检查
     *
     * @param ip
     * @throws ServiceException
     */
    private void checkRequestFrequency(String ip) throws ServiceException {
        Integer smsLimitedTime = whiteboardConfig.getSmsLimitedTime();
        Integer smsLimitedCount = whiteboardConfig.getSmsLimitedCount();

        if (smsLimitedTime == null) {
            smsLimitedTime = 1;
        }

        if (smsLimitedCount == null) {
            smsLimitedCount = 20;
        }

        VerificationViolations verificationViolations = verificationViolationsService.getByPrimaryKey(ip);
        if (verificationViolations == null) {
            return;
        }

        DateTime dateTime = new DateTime(new Date());
        Date sendDate = dateTime.minusHours(smsLimitedTime).toDate();

        boolean beyondLimit = verificationViolations.getCount() >= smsLimitedCount;

        //如果上次请求发送验证码的时间在1小时内，并且次数达到阈值，返回异常"Too many times sent"
        if (sendDate.before(verificationViolations.getTime()) && beyondLimit) {
            throw new ServiceException(ErrorCode.YUN_PIAN_SMS_ERROR);
        }
    }




    /**
     * 校验验证码
     *
     * @param region
     * @param phone
     * @param code
     * @param smsServiceType
     * @return
     * @throws ServiceException
     */
    public String verifyCode(String region, String phone, String code, SmsServiceType smsServiceType) throws ServiceException {

        VerificationCodes verificationCodes = verificationCodesService.getByRegionAndPhone(region, phone);
        VerifyCodeAuthentication verifyCodeAuthentication = VerifyCodeAuthenticationFactory.getVerifyCodeAuthentication(smsServiceType);
        verifyCodeAuthentication.validate(verificationCodes, code, whiteboardConfig.getConfigEnv());
        verificationCodesService.updateIsUse(verificationCodes);
        return verificationCodes.getToken();
    }



    /**
     * 注册插入user 表、dataversion表
     * 同一事务
     *
     * @param nickname
     * @param region
     * @param phone
     * @param salt
     * @param hashStr
     * @return
     */
    private Users register0(String nickname, String region, String phone, int salt, String hashStr) {
        return transactionTemplate.execute(new TransactionCallback<Users>() {
            @Override
            public Users doInTransaction(TransactionStatus transactionStatus) {
                //插入user表
                Users u = new Users();
                u.setNickname(nickname);
                u.setRegion(region);
                u.setPhone(phone);
                u.setPasswordHash(hashStr);
                u.setPasswordSalt(String.valueOf(salt));
                u.setCreatedAt(new Date());
                u.setUpdatedAt(u.getCreatedAt());
                u.setPortraitUri(whiteboardConfig.getRongcloudDefaultPortraitUrl());

                usersService.saveSelective(u);

                return u;
            }
        });

    }


    /**
     * 获取融云token
     * 1、根据currentUserId查询User
     * 2、调用融云用户注册接口，获取token
     * 3、根据userId更新本地数据users表中rongCloudToken
     * 4、把userid，token返回给前端
     */
    public Pair<Integer, String> getToken(Integer currentUserId) throws ServiceException {

        Users user = usersService.getByPrimaryKey(currentUserId);

        //调用融云用户注册接口，获取token
        //如果用户头像地址为空，采用默认头像地址
        String portraitUri = StringUtils.isEmpty(user.getPortraitUri()) ? whiteboardConfig.getRongcloudDefaultPortraitUrl() : user.getPortraitUri();
        TokenResult tokenResult = rongCloudClient.register(N3d.encode(user.getId()), user.getNickname(), portraitUri);
        String token = tokenResult.getToken();

        //根据userId更新本地数据users表中rongCloudToken
        Users param = new Users();
        param.setId(user.getId());
        param.setUserId(N3d.encode(user.getId()));
        param.setRongCloudToken(token);
        param.setUpdatedAt(new Date());
        usersService.updateByPrimaryKeySelective(param);

        return Pair.of(user.getId(), token);
    }

    /**
     * 短信校验即注册
     *
     * @param region
     * @param phone
     * @param code
     * @param smsServiceType
     * @return
     * @throws ServiceException
     */
    public Triple<Integer, String,String> verifyCodeRegister(String region, String phone, String code, SmsServiceType smsServiceType) throws ServiceException {
        //校验验证码
        this.verifyCode(region, phone, code, smsServiceType);

        //如果是新用户，注册用户
        Users param = new Users();
        param.setRegion(region);
        param.setPhone(phone);
        Users u = usersService.getOne(param);

        if (u == null) {
            int salt = 0;
            String hashStr = "";
            String nickname = "融云" + phone.substring(phone.length() - 4, phone.length());
            u = register0(nickname, region, phone, salt, hashStr);
        }
        //

        log.info("login id:{} nickname:{} region:{} phone={} ", u.getId(), u.getNickname(), u.getRegion(), u.getPhone());
        //缓存nickname
        CacheUtil.set(CacheUtil.NICK_NAME_CACHE_PREFIX + u.getId(), u.getNickname());



        String token = u.getRongCloudToken();
        if (StringUtils.isEmpty(token)) {
            //如果user表中的融云token为空，调用融云sdk 获取token
            //如果用户头像地址为空，采用默认头像地址
            String portraitUri = StringUtils.isEmpty(u.getPortraitUri()) ? whiteboardConfig.getRongcloudDefaultPortraitUrl() : u.getPortraitUri();
            TokenResult tokenResult = rongCloudClient.register(N3d.encode(u.getId()), u.getNickname(), portraitUri);
            if (!Constants.CODE_OK.equals(tokenResult.getCode())) {
                throw new ServiceException(ErrorCode.SERVER_ERROR, "'RongCloud Server API Error Code: " + tokenResult.getCode());
            }

            token = tokenResult.getToken();

            //获取后根据userId更新表中token
            Users users = new Users();
            users.setId(u.getId());
            users.setUserId(N3d.encode(u.getId()));
            users.setRongCloudToken(token);
            users.setUpdatedAt(new Date());
            usersService.updateByPrimaryKeySelective(users);
        }

        //返回userId、token
        return Triple.of(u.getId(), token,u.getNickname());

    }

}

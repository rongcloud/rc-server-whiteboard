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
     * ????????????????????????
     */
    public void sendCode(String region, String phone, SmsServiceType smsServiceType, ServerApiParams serverApiParams) throws ServiceException {
        log.info("send code. region:[{}] phone:[{}] smsServiceType:[{}]", region, phone, smsServiceType.getCode());

        region = MiscUtils.removeRegionPrefix(region);
        ValidateUtils.checkRegion(region);
        ValidateUtils.checkCompletePhone(phone);

        String ip = serverApiParams.getRequestUriInfo().getIp();

        // ???????????????????????????????????????
        VerificationCodes verificationCodes = verificationCodesService.getByRegionAndPhone(region, phone);
        if (verificationCodes != null) {
            checkLimitTime(verificationCodes);
        }

        //????????????????????????????????????IP??????????????????
        checkRequestFrequency(ip);

        //???????????????verificationCodes??? ???????????????
        upsertAndSendToSms(region, phone, smsServiceType);

        refreshRequestFrequency(ip);
    }

    /**
     * ??????ip????????????
     *
     * @param ip
     */
    private void refreshRequestFrequency(String ip) {
        //??????verification_violations ip???????????????????????????
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
                //?????????????????????????????????1???????????????????????????????????????
                verificationViolations.setCount(1);
                verificationViolations.setTime(new Date());
            } else {
                verificationViolations.setCount(verificationViolations.getCount() + 1);
            }
            verificationViolationsService.updateByPrimaryKeySelective(verificationViolations);
        }

    }

    /**
     * ??????????????????????????????
     */
    private VerificationCodes upsertAndSendToSms(String region, String phone, SmsServiceType smsServiceType) throws ServiceException {
        if (Constants.ENV_DEV.equals(whiteboardConfig.getConfigEnv())) {
            //?????????????????????????????????????????????????????????
            return verificationCodesService.saveOrUpdate(region, phone, "");
        } else {
            SmsService smsService = SmsServiceFactory.getSmsService(smsServiceType);
            String sessionId = smsService.sendVerificationCode(region, phone);
            return verificationCodesService.saveOrUpdate(region, phone, sessionId);
        }
    }

    /**
     * ????????????????????????
     * ??????????????????5??????  ??????????????????????????????
     * ??????????????????1????????? ??????????????????????????????
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
     * IP????????????????????????
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

        //?????????????????????????????????????????????1???????????????????????????????????????????????????"Too many times sent"
        if (sendDate.before(verificationViolations.getTime()) && beyondLimit) {
            throw new ServiceException(ErrorCode.YUN_PIAN_SMS_ERROR);
        }
    }




    /**
     * ???????????????
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
     * ????????????user ??????dataversion???
     * ????????????
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
                //??????user???
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
     * ????????????token
     * 1?????????currentUserId??????User
     * 2??????????????????????????????????????????token
     * 3?????????userId??????????????????users??????rongCloudToken
     * 4??????userid???token???????????????
     */
    public Pair<Integer, String> getToken(Integer currentUserId) throws ServiceException {

        Users user = usersService.getByPrimaryKey(currentUserId);

        //???????????????????????????????????????token
        //?????????????????????????????????????????????????????????
        String portraitUri = StringUtils.isEmpty(user.getPortraitUri()) ? whiteboardConfig.getRongcloudDefaultPortraitUrl() : user.getPortraitUri();
        TokenResult tokenResult = rongCloudClient.register(N3d.encode(user.getId()), user.getNickname(), portraitUri);
        String token = tokenResult.getToken();

        //??????userId??????????????????users??????rongCloudToken
        Users param = new Users();
        param.setId(user.getId());
        param.setUserId(N3d.encode(user.getId()));
        param.setRongCloudToken(token);
        param.setUpdatedAt(new Date());
        usersService.updateByPrimaryKeySelective(param);

        return Pair.of(user.getId(), token);
    }

    /**
     * ?????????????????????
     *
     * @param region
     * @param phone
     * @param code
     * @param smsServiceType
     * @return
     * @throws ServiceException
     */
    public Triple<Integer, String,String> verifyCodeRegister(String region, String phone, String code, SmsServiceType smsServiceType) throws ServiceException {
        //???????????????
        this.verifyCode(region, phone, code, smsServiceType);

        //?????????????????????????????????
        Users param = new Users();
        param.setRegion(region);
        param.setPhone(phone);
        Users u = usersService.getOne(param);

        if (u == null) {
            int salt = 0;
            String hashStr = "";
            String nickname = "??????" + phone.substring(phone.length() - 4, phone.length());
            u = register0(nickname, region, phone, salt, hashStr);
        }
        //

        log.info("login id:{} nickname:{} region:{} phone={} ", u.getId(), u.getNickname(), u.getRegion(), u.getPhone());
        //??????nickname
        CacheUtil.set(CacheUtil.NICK_NAME_CACHE_PREFIX + u.getId(), u.getNickname());



        String token = u.getRongCloudToken();
        if (StringUtils.isEmpty(token)) {
            //??????user???????????????token?????????????????????sdk ??????token
            //?????????????????????????????????????????????????????????
            String portraitUri = StringUtils.isEmpty(u.getPortraitUri()) ? whiteboardConfig.getRongcloudDefaultPortraitUrl() : u.getPortraitUri();
            TokenResult tokenResult = rongCloudClient.register(N3d.encode(u.getId()), u.getNickname(), portraitUri);
            if (!Constants.CODE_OK.equals(tokenResult.getCode())) {
                throw new ServiceException(ErrorCode.SERVER_ERROR, "'RongCloud Server API Error Code: " + tokenResult.getCode());
            }

            token = tokenResult.getToken();

            //???????????????userId????????????token
            Users users = new Users();
            users.setId(u.getId());
            users.setUserId(N3d.encode(u.getId()));
            users.setRongCloudToken(token);
            users.setUpdatedAt(new Date());
            usersService.updateByPrimaryKeySelective(users);
        }

        //??????userId???token
        return Triple.of(u.getId(), token,u.getNickname());

    }

}

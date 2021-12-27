package com.rcloud.server.whiteboard.sms.impl;

import com.rcloud.server.whiteboard.configuration.WhiteboardConfig;
import com.rcloud.server.whiteboard.constant.SmsServiceType;
import com.rcloud.server.whiteboard.exception.ServiceException;
import com.rcloud.server.whiteboard.sms.SmsService;
import com.rcloud.server.whiteboard.sms.SmsTemplateService;
import com.rcloud.server.whiteboard.util.HttpClient;
import com.rcloud.server.whiteboard.util.RandomUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @Author: Jianlu.Yu
 * @Date: 2020/8/4
 * @Description: 融云sms获取短信验证码
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Service
@Slf4j
public class DefaultSmsService implements SmsService {

    private Integer SUCCESS_CODE = 200;


    @Resource
    protected WhiteboardConfig whiteboardConfig;
    @Autowired
    private HttpClient httpClient;
    @Autowired
    private SmsTemplateService smsTemplateService;

    @Override
    public SmsServiceType getIdentifier() {
        return SmsServiceType.RONGCLOUD;
    }

    /**
     * 发送验证码
     *
     * @param region 地区，如中国大陆地区 region=86
     * @param phone  手机号
     * @return 返回验证码对应的唯一标示
     * @throws ServiceException
     */
    @Override
    public String sendVerificationCode(String region, String phone) throws ServiceException {

        String lang = smsTemplateService.getLangByRegion(region);
        String templateId = smsTemplateService.getSmsTemplateIdByLang(lang);
        return sendToSms(region,phone,templateId);
    }

    /**
     * 调用融云接口 发送短信
     */
    private String sendToSms(String region, String phone,String smsTemplateId) {
        if(StringUtils.isBlank(smsTemplateId)){
            throw new RuntimeException("smsTemplateId is null");
        }

        String smsAppKey = whiteboardConfig.getSmsAppKey();
        String smsAppSecretKey = whiteboardConfig.getSmsAppSecret();
        if(StringUtils.isBlank(smsAppKey) || StringUtils.isBlank(smsAppSecretKey)){
            smsAppKey = whiteboardConfig.getRongcloudAppKey();
            smsAppSecretKey = whiteboardConfig.getRongcloudAppSecret();
        }

        long timeStamp = System.currentTimeMillis();
        int code  = RandomUtil.randomBetween(100000, 999999);
        int nonce  = RandomUtil.randomBetween(100000, 999999);
        String signature = sign(smsAppSecretKey,String.valueOf(nonce),String.valueOf(timeStamp));

        String smsUrl = "";
        if (region.equals("86")) {
            //国内短信
            smsUrl = "http://api.sms.ronghub.com/sendNotify.json";
        } else {
            //国际短信
            smsUrl = "http://api.sms.ronghub.com/sendIntl.json";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("App-Key",smsAppKey);
        headers.set("Nonce",String.valueOf(nonce));
        headers.set("Timestamp",String.valueOf(timeStamp));
        headers.set("Signature",signature);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("mobile", phone);
        map.add("templateId", smsTemplateId);
        map.add("region", region);
        map.add("p1", String.valueOf(code));


        ResponseEntity<String> response = httpClient.post(headers, smsUrl, map, MediaType.APPLICATION_FORM_URLENCODED);
        if (SUCCESS_CODE.equals(response.getStatusCodeValue())) {
            log.info("send sms success,responseBody={}",response.getBody());
        } else {
            log.info("send sms fail,responseBody={}",response.getBody());
        }
        return String.valueOf(code);
    }

    public static String sign(String secret,String nonce, String timestamp ){
        StringBuilder toSign = new StringBuilder(secret).append(nonce).append(
            timestamp);
        String sign = hexSHA1(toSign.toString());
        return sign;
    }

    public static String hexSHA1(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(value.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            return Hex.encodeHexString(digest);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

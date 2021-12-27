package com.rcloud.server.whiteboard.spi.verifycode;

import com.rcloud.server.whiteboard.constant.SmsServiceType;
import com.rcloud.server.whiteboard.exception.ServiceException;
import com.rcloud.server.whiteboard.spi.verifycode.impl.DefaultVerifyCodeAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: Jianlu.Yu
 * @Date: 2020/8/4
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Service
@Slf4j
public class VerifyCodeAuthenticationFactory {

    private static ConcurrentHashMap<SmsServiceType, VerifyCodeAuthentication> verifyCodeAuthenticationMap = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @Resource
    private DefaultVerifyCodeAuthentication defaultVerifyCodeAuthentication;
    @PostConstruct
    public void postConstruct() {

        Map<String, VerifyCodeAuthentication> serviceMap =  applicationContext.getBeansOfType(VerifyCodeAuthentication.class);
        if(serviceMap!=null){
            Set<Map.Entry<String,VerifyCodeAuthentication>> vcaSet = serviceMap.entrySet();
            Iterator<Map.Entry<String,VerifyCodeAuthentication>> iterator = vcaSet.iterator();

            while(iterator.hasNext()){
                Map.Entry<String,VerifyCodeAuthentication> verifyCodeAuthenticationEntry = iterator.next();
                VerifyCodeAuthentication verifyCodeAuthentication = verifyCodeAuthenticationEntry.getValue();
                log.info("postConstruct identifier:"+verifyCodeAuthentication.getIdentifier());
                verifyCodeAuthenticationMap.put(verifyCodeAuthentication.getIdentifier(), verifyCodeAuthentication);
            }
        }
    }

    public static VerifyCodeAuthentication getVerifyCodeAuthentication(SmsServiceType smsServiceType) throws ServiceException {
        return verifyCodeAuthenticationMap.get(smsServiceType);
    }
}

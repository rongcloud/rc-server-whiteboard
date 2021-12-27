package com.rcloud.server.whiteboard.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author;
 * @Date;
 * @Description;
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Data
@Component
public class WhiteboardConfig {

    @Value("${whiteboard-config.auth_cookie_name}")
    private String authCookieName;
    @Value("${whiteboard-config.auth_cookie_key}")
    private String authCookieKey;
    @Value("${whiteboard-config.auth_cookie_max_age}")
    private String authCookieMaxAge;

    @Value("${whiteboard-config.sms_service_type}")
    private String smsServiceType;
    @Value("${whiteboard-config.rongcloud_app_key}")
    private String rongcloudAppKey;
    @Value("${whiteboard-config.config_env}")
    private String configEnv;
    @Value("${whiteboard-config.rongcloud_app_secret}")
    private String rongcloudAppSecret;
    @Value("${whiteboard-config.rongcloud_api_url}")
    private String rongcloudApiUrl;
    @Value("${whiteboard-config.rongcloud_api_v1_url}")
    private String rongcloudApiV1Url;
    @Value("${whiteboard-config.rongcloud_default_portrait_url}")
    private String rongcloudDefaultPortraitUrl;         //默认头像地址


    @Value("${whiteboard-config.sms_limited_time:1}")//限制小时
    private Integer smsLimitedTime;
    @Value("${whiteboard-config.sms_limited_count:20}")//限制次数
    private Integer smsLimitedCount;

    @Value("${whiteboard-config.n3d_key}")
    private String n3dKey;
    @Value("${whiteboard-config.auth_cookie_domain}")
    private String authCookieDomain;
    @Value("${whiteboard-config.cors_hosts}")
    private String corsHosts;

    @Value("${whiteboard-config.exclude_url}")
    private String excludeUrl;

    @Value("${whiteboard-config.sms_app_key}")
    private String smsAppKey;
    @Value("${whiteboard-config.sms_app_secret}")
    private String smsAppSecret;
}

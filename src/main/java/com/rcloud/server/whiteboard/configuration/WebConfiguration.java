package com.rcloud.server.whiteboard.configuration;

import com.rcloud.server.whiteboard.interceptor.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @Author: xiuwei.nie
 * @Date: 2020/7/6
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final RequestInterceptor requestInterceptor;

    @Resource
    private WhiteboardConfig whiteboardConfig;

    @Autowired
    public WebConfiguration(RequestInterceptor requestInterceptor) {
        this.requestInterceptor = requestInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor)
                .addPathPatterns("/**");
    }

}

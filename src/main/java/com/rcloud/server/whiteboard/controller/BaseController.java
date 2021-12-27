package com.rcloud.server.whiteboard.controller;

import com.rcloud.server.whiteboard.configuration.WhiteboardConfig;
import com.rcloud.server.whiteboard.interceptor.ServerApiParamHolder;
import com.rcloud.server.whiteboard.model.ServerApiParams;

import javax.annotation.Resource;

/**
 * @Author: Jianlu.Yu
 * @Date: 2020/8/7
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
public abstract class BaseController {

    @Resource
    protected WhiteboardConfig whiteboardConfig;

    protected Integer getCurrentUserId() {
        ServerApiParams serverApiParams = ServerApiParamHolder.get();
        if (serverApiParams != null) {
            return serverApiParams.getCurrentUserId();
        } else {
            return null;
        }
    }

    protected WhiteboardConfig getWhiteboardConfig() {
        return whiteboardConfig;
    }

    protected ServerApiParams getServerApiParams() {
        return ServerApiParamHolder.get();
    }

}

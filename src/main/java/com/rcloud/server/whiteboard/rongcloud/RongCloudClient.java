package com.rcloud.server.whiteboard.rongcloud;

import com.rcloud.server.whiteboard.exception.ServiceException;
import com.rcloud.server.whiteboard.rongcloud.message.CustomerGroupApplyMessage;
import io.rong.models.Result;
import io.rong.models.message.GroupMessage;
import io.rong.models.message.PrivateMessage;
import io.rong.models.response.BlackListResult;
import io.rong.models.response.ResponseResult;
import io.rong.models.response.TokenResult;
import io.rong.models.response.UserResult;

import java.util.List;
import java.util.Map;

/**
 * @Author: Jianlu.Yu
 * @Date: 2020/8/6
 * @Description: 调用融云服务客户端
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
public interface RongCloudClient {


    /**
     * 注册并获取token 通过调用sdk的方式
     *
     * @param encodeId 用户id  n3d编码
     * @param name     昵称
     * @param portrait 头像地址
     * @return
     * @throws ServiceException
     */
    TokenResult register(String encodeId, String name, String portrait) throws ServiceException;


}

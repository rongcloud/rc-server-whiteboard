package com.rcloud.server.whiteboard.rongcloud;


import com.rcloud.server.whiteboard.constant.ErrorCode;
import com.rcloud.server.whiteboard.exception.ServiceException;
import io.rong.models.Result;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Jianlu.Yu
 * @Date: 2020/8/6
 * @Description: 融云服务调用模版
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Slf4j
public class RongCloudInvokeTemplate {

    public static <T extends Result> T getData(RongCloudCallBack<T> action) throws ServiceException {

        try {
            Result result = action.doInvoker();
            if (result.getCode().equals(200)) {
                return (T) result;
            } else {
                log.error("invoke rongcloud server exception,resultCode={},errorMessage={}", result.getCode(), result.getErrorMessage());
                return (T) result;
            }
        } catch (Exception e) {
            log.error("invoke rongcloud server error：" + e.getMessage() , e);
            throw new ServiceException(ErrorCode.INVOKE_RONG_CLOUD_SERVER_ERROR);
        }
    }
}

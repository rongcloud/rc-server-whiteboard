package com.rcloud.server.whiteboard.util;

import com.google.common.collect.ImmutableList;
import com.rcloud.server.whiteboard.constant.Constants;
import com.rcloud.server.whiteboard.constant.ErrorCode;
import com.rcloud.server.whiteboard.exception.ServiceException;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @Author: Jianlu.Yu
 * @Date: 2020/8/4
 * @Description: 校验util
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
public class ValidateUtils {

    public static void notEmpty(String str) throws ServiceException {
        if (str == null || StringUtils.isEmpty(str.trim())) {
            throw new ServiceException(ErrorCode.PARAM_ERROR);
        }
    }


    public static void checkCompletePhone(String completePhone) throws ServiceException {
        if (!RegexUtils.checkMobile(completePhone)) {
            throw new ServiceException(ErrorCode.INVALID_REGION_PHONE);
        }
    }

    public static void checkRegion(String region) throws ServiceException {
        if (!MiscUtils.isNumberStr(region)) {
            throw new ServiceException(ErrorCode.INVALID_REGION_PHONE);
        }
    }

}

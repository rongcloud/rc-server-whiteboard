package com.rcloud.server.whiteboard.controller;

import com.rcloud.server.whiteboard.constant.Constants;
import com.rcloud.server.whiteboard.constant.SmsServiceType;
import com.rcloud.server.whiteboard.controller.param.UserParam;
import com.rcloud.server.whiteboard.exception.ServiceException;
import com.rcloud.server.whiteboard.manager.UserManager;
import com.rcloud.server.whiteboard.model.ServerApiParams;
import com.rcloud.server.whiteboard.model.response.APIResult;
import com.rcloud.server.whiteboard.model.response.APIResultWrap;
import com.rcloud.server.whiteboard.util.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: xiuwei.nie
 * @Author: Jianlu.Yu
 * @Date: 2020/7/6
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Api(tags = "用户相关")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController extends BaseController {

    @Resource
    private UserManager userManager;

    @ApiOperation(value = "向手机发送验证码")
    @RequestMapping(value = "/send_code", method = RequestMethod.POST)
    public APIResult<Object> sendCode(@RequestBody UserParam userParam) throws ServiceException {

        String region = userParam.getRegion();
        String phone = userParam.getPhone();

        ValidateUtils.notEmpty(region);
        ValidateUtils.notEmpty(phone);

        region = MiscUtils.removeRegionPrefix(region);
        ValidateUtils.checkRegion(region);
        ValidateUtils.checkCompletePhone(phone);

        ServerApiParams serverApiParams = getServerApiParams();
        SmsServiceType smsServiceType;
        smsServiceType = SmsServiceType.RONGCLOUD;

        userManager.sendCode(region, phone, smsServiceType, serverApiParams);
        return APIResultWrap.ok();

    }
    /**
     * 短信验证即登录
     * @param userParam
     * @return
     * @throws ServiceException
     */
    @ApiOperation(value = "短信验证即注册")
    @RequestMapping(value = "/verify_code_register", method = RequestMethod.POST)
    public APIResult<Object> verifyCodeLogin(@RequestBody UserParam userParam, HttpServletResponse response) throws ServiceException {
        String region = userParam.getRegion();
        String phone = userParam.getPhone();
        String code = userParam.getCode();
        region = MiscUtils.removeRegionPrefix(region);

        ValidateUtils.checkRegion(region);
        ValidateUtils.checkCompletePhone(phone);

        Triple<Integer, String,String> pairResult = userManager.verifyCodeRegister(region,phone,code,SmsServiceType.YUNPIAN);

        //设置cookie  userId加密存入cookie
        //登录成功后的其他请求，当前登录用户useId获取从cookie中获取
        setCookie(response, pairResult.getLeft());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", pairResult.getLeft());
        resultMap.put("token", pairResult.getMiddle());
        resultMap.put("nickName", pairResult.getRight());

        //对result编码
        return APIResultWrap.ok(MiscUtils.encodeResults(resultMap));
    }

    @ApiOperation(value = "获取融云token")
    @RequestMapping(value = "/get_token", method = RequestMethod.GET)
    public APIResult<Object> getToken() throws ServiceException {

        Integer currentUserId = getCurrentUserId();
        Pair<Integer, String> pairResult = userManager.getToken(currentUserId);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", pairResult.getLeft());
        resultMap.put("token", pairResult.getRight());

        return APIResultWrap.ok(MiscUtils.encodeResults(resultMap));
    }
    /**
     * 设置AuthCookie
     *
     * @param response
     * @param userId
     */
    private void setCookie(HttpServletResponse response, int userId) {
        int salt = RandomUtil.randomBetween(1000, 9999);
        String text = salt + Constants.SEPARATOR_NO + userId + Constants.SEPARATOR_NO + System.currentTimeMillis();
        byte[] value = AES256.encrypt(text, whiteboardConfig.getAuthCookieKey());
        Cookie cookie = new Cookie(whiteboardConfig.getAuthCookieName(), new String(value));
        cookie.setHttpOnly(true);
        cookie.setDomain(whiteboardConfig.getAuthCookieDomain());
        cookie.setMaxAge(Integer.valueOf(whiteboardConfig.getAuthCookieMaxAge()));
        cookie.setPath("/");
        response.addCookie(cookie);
    }

}

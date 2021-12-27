package com.rcloud.server.whiteboard.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rcloud.server.whiteboard.constant.ErrorCode;
import com.rcloud.server.whiteboard.domain.Users;
import com.rcloud.server.whiteboard.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author: xiuwei.nie
 * @Author: Jianlu.Yu
 * @Date: 2020/7/7
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Slf4j
public class JacksonUtil {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectMapper getInstance() {
        return objectMapper;
    }

    /**
     * bean、array、List、Map --> json
     */
    public static String toJson(Object object) throws ServiceException {
        try {
            return getInstance().writeValueAsString(object);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new ServiceException("toJson exception:" + e.getMessage(), ErrorCode.SERVER_ERROR, e);
        }
    }

    /**
     * string --> bean、Map、List(array)
     *
     * @param jsonStr
     * @param clazz
     * @return obj
     * @throws Exception
     */
    public static <T> T fromJson(String jsonStr, Class<T> clazz) throws ServiceException {

        try {
            return getInstance().readValue(jsonStr, clazz);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new ServiceException("fromJson exception:" + e.getMessage(), ErrorCode.SERVER_ERROR, e);
        }
    }

    public static Map<String, Object> toMap(String jsonStr) {

        try {
            return getInstance().readValue(jsonStr, Map.class);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("convert json to map  exception:" + jsonStr);
        }
    }

    /**
     * string --> List<Bean>
     *
     * @param jsonStr
     * @param parametrized
     * @param parameterClasses
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String jsonStr, Class<?> parametrized, Class<?>... parameterClasses) throws ServiceException {

        try {
            JavaType javaType = getInstance().getTypeFactory().constructParametricType(parametrized, parameterClasses);
            return getInstance().readValue(jsonStr, javaType);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new ServiceException("fromJson exception:" + e.getMessage(), ErrorCode.SERVER_ERROR, e);
        }
    }



    public static void main(String[] args) {
        try {
            // bean
            Users u = new Users();
            u.setId(123);
            u.setNickname("张三");
            u.setCreatedAt(new Date());
            String jsonStr = toJson(u);
            System.out.println(jsonStr);

            System.out.println(fromJson(jsonStr.substring(20), Users.class));

            // list
            List<Users> list = new ArrayList<Users>();
            for (int i = 0; i < 2; i++) {
                u = new Users();
                u.setId(10 + i);
                u.setNickname("tom" + i);
                list.add(u);
            }
            String result = JacksonUtil.toJson(list);
            System.out.println(result);

            List<Users> list2 = fromJson(result, List.class, Users.class);
            System.out.println(list2);
        } catch (ServiceException e) {
            log.error(e.getMessage(), e);
        }
    }

}

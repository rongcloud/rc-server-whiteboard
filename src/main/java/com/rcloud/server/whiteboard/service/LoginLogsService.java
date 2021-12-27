package com.rcloud.server.whiteboard.service;

import com.rcloud.server.whiteboard.dao.LoginLogsMapper;
import com.rcloud.server.whiteboard.domain.LoginLogs;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;

import javax.annotation.Resource;

/**
 * @Author: xiuwei.nie
 * @Date: 2020/7/6
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Service
public class LoginLogsService extends AbstractBaseService<LoginLogs, Integer> {

    @Resource
    private LoginLogsMapper mapper;

    @Override
    protected Mapper<LoginLogs> getMapper() {
        return mapper;
    }
}

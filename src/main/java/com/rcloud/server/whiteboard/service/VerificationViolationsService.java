package com.rcloud.server.whiteboard.service;

import com.rcloud.server.whiteboard.dao.VerificationViolationsMapper;
import com.rcloud.server.whiteboard.domain.VerificationViolations;
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
public class VerificationViolationsService extends AbstractBaseService<VerificationViolations, String> {

    @Resource
    private VerificationViolationsMapper mapper;

    @Override
    protected Mapper<VerificationViolations> getMapper() {
        return mapper;
    }

}

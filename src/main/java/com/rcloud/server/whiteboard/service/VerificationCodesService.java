package com.rcloud.server.whiteboard.service;

import com.rcloud.server.whiteboard.dao.VerificationCodesMapper;
import com.rcloud.server.whiteboard.domain.VerificationCodes;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

/**
 * @Author: xiuwei.nie
 * @Date: 2020/7/6
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Service
public class VerificationCodesService extends AbstractBaseService<VerificationCodes, Integer> {

    @Resource
    private VerificationCodesMapper mapper;

    @Override
    protected Mapper<VerificationCodes> getMapper() {
        return mapper;
    }

    public VerificationCodes saveOrUpdate(String region, String phone, String sessionId) {
        Assert.hasLength(region, "region is empty");
        Assert.hasLength(phone, "phone is empty");
        Assert.notNull(sessionId, "sessionId is null");

        Example example = new Example(VerificationCodes.class);
        example.createCriteria().andEqualTo("region", region)
                .andEqualTo("phone", phone);

        VerificationCodes verificationCodes = this.getOneByExample(example);

        if (verificationCodes == null) {
            verificationCodes = new VerificationCodes();
            verificationCodes.setRegion(region);
            verificationCodes.setPhone(phone);
            verificationCodes.setSessionId(sessionId);
            //默认uuid1 str
            verificationCodes.setToken(UUID.randomUUID().toString());
            verificationCodes.setCreatedAt(new Date());
            verificationCodes.setUpdatedAt(verificationCodes.getCreatedAt());
            verificationCodes.setIsUse(0);
            this.saveSelective(verificationCodes);
        } else {
            VerificationCodes newVerificationCodes = new VerificationCodes();
            newVerificationCodes.setRegion(region);
            newVerificationCodes.setPhone(phone);
            newVerificationCodes.setSessionId(sessionId);
            newVerificationCodes.setUpdatedAt(new Date());
            newVerificationCodes.setId(verificationCodes.getId());
            newVerificationCodes.setIsUse(0);
            this.updateByPrimaryKeySelective(newVerificationCodes);
        }
        return verificationCodes;
    }

    public VerificationCodes getByRegionAndPhone(String region, String phone) {
        Assert.hasLength(region, "region is empty");
        Assert.hasLength(phone, "phone is empty");
        Example example = new Example(VerificationCodes.class);
        example.createCriteria().andEqualTo("region", region)
                .andEqualTo("phone", phone).andEqualTo("isUse",0);
        return this.getOneByExample(example);
    }

    public VerificationCodes getByToken(String verificationToken) {

        Assert.hasLength(verificationToken, "verificationToken is empty");
        VerificationCodes v = new VerificationCodes();
        v.setToken(verificationToken);
        return this.getOne(v);

    }

    public void updateIsUse(VerificationCodes verificationCodes){
        if(verificationCodes != null){
            verificationCodes.setIsUse(1);
            this.updateByPrimaryKeySelective(verificationCodes);
        }
    }
}

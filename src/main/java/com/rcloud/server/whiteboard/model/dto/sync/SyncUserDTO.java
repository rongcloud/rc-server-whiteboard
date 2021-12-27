package com.rcloud.server.whiteboard.model.dto.sync;

import lombok.Data;

/**
 * @Author: Jianlu.Yu
 * @Date: 2020/9/3
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Data
public class SyncUserDTO {

    private String id;
    private String nickname;
    private String portraitUri;
    private Long timestamp;



}

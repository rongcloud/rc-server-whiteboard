package com.rcloud.server.whiteboard.rongcloud;

import com.rcloud.server.whiteboard.configuration.WhiteboardConfig;
import com.rcloud.server.whiteboard.exception.ServiceException;
import com.rcloud.server.whiteboard.rongcloud.message.*;
import com.rcloud.server.whiteboard.util.N3d;
import io.rong.RongCloud;
import io.rong.messages.GroupNotificationMessage;
import io.rong.messages.TxtMessage;
import io.rong.methods.message._private.Private;
import io.rong.methods.message.system.MsgSystem;
import io.rong.methods.user.User;
import io.rong.methods.user.blacklist.Blacklist;
import io.rong.models.Result;
import io.rong.models.group.GroupMember;
import io.rong.models.group.GroupModel;
import io.rong.models.group.UserGroup;
import io.rong.models.message.*;
import io.rong.models.response.BlackListResult;
import io.rong.models.response.ResponseResult;
import io.rong.models.response.TokenResult;
import io.rong.models.response.UserResult;
import io.rong.models.user.UserModel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Jianlu.Yu
 * @Date: 2020/8/6
 * @Description: 调用融云服务客户端实现
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Service
public class DefaultRongCloudClient implements RongCloudClient {


    @Resource
    private WhiteboardConfig whiteboardConfig;

    private RongCloud rongCloud;

    private User User;
    private Blacklist BlackList;

    private Private Private;
    private MsgSystem system;



    @PostConstruct
    public void postConstruct() {
        String apiUrlStr = whiteboardConfig.getRongcloudApiV1Url();
        if (StringUtils.isEmpty(apiUrlStr)) {
            throw new RuntimeException("rongcloud client init exception");
        }
        String[] apiUrlArray = apiUrlStr.split(",");
        String mainUrl = apiUrlArray[0].trim();
        if (!mainUrl.startsWith("http://") && !mainUrl.startsWith("https://")) {
            mainUrl = "http://" + mainUrl;
        }
        List<String> backUpUrlList = new ArrayList<>();
        if (apiUrlArray.length > 1) {
            for (int i = 1; i < apiUrlArray.length; i++) {
                String backApiUrl = apiUrlArray[i].trim();
                if (!backApiUrl.startsWith("http://") && !backApiUrl.startsWith("https://")) {
                    backApiUrl = "http://" + backApiUrl;
                }
                backUpUrlList.add(backApiUrl);
            }
        }
        rongCloud = RongCloud.getInstance(whiteboardConfig.getRongcloudAppKey(), whiteboardConfig.getRongcloudAppSecret(), mainUrl, backUpUrlList);
        User = rongCloud.user;
        BlackList = rongCloud.user.blackList;
        Private = rongCloud.message.msgPrivate;
        system = rongCloud.message.system;
    }


    @Override
    public TokenResult register(String encodeId, String name, String portrait) throws ServiceException {

        return RongCloudInvokeTemplate.getData(new RongCloudCallBack<TokenResult>() {
            @Override
            public TokenResult doInvoker() throws Exception {
                UserModel user = new UserModel()
                        .setId(encodeId)
                        .setName(name)
                        .setPortrait(portrait);

                return User.register(user);
            }
        });
    }

}

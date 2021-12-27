package com.rcloud.server.whiteboard.controller;

import com.rcloud.server.whiteboard.constant.Constants;
import com.rcloud.server.whiteboard.constant.SmsServiceType;
import com.rcloud.server.whiteboard.controller.param.UserParam;
import com.rcloud.server.whiteboard.exception.ServiceException;
import com.rcloud.server.whiteboard.manager.IwbManager;
import com.rcloud.server.whiteboard.manager.UserManager;
import com.rcloud.server.whiteboard.model.ServerApiParams;
import com.rcloud.server.whiteboard.model.response.APIResult;
import com.rcloud.server.whiteboard.model.response.APIResultWrap;
import com.rcloud.server.whiteboard.util.AES256;
import com.rcloud.server.whiteboard.util.MiscUtils;
import com.rcloud.server.whiteboard.util.RandomUtil;
import com.rcloud.server.whiteboard.util.ValidateUtils;
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

@Api(tags = "白板相关")
@RestController
@Slf4j
public class IwbController extends BaseController {

    @Resource
    private IwbManager iwbManager;

    @ApiOperation(value = "获取录像记录")
    @RequestMapping(value = "/records", method = RequestMethod.GET)
    public String getRecords(
            @RequestHeader(name = "RC-Token") String token,
            @ApiParam( value = "房间ID") @RequestParam (value = "hubId", required = false) String hubId,
            @ApiParam( value = "用户Id") @RequestParam (value = "userId", required = false) String userId,
            @ApiParam( value = "开始时间") @RequestParam (value = "rsTime", required = false, defaultValue = "0") Long rsTime,
            @ApiParam( value = "结束时间") @RequestParam (value = "reTime", required = false, defaultValue = "0") Long reTime,
            @ApiParam( value = "分页查询开始页数") @RequestParam (value = "offset", required = false, defaultValue = "0") Integer offset,
            @ApiParam( value = "每页条数") @RequestParam (value = "limit", required = false, defaultValue = "20") Integer limit
            ) throws ServiceException {


       if (limit > 200) {
           limit = 200;
       }
       if (reTime == 0) {
           reTime = System.currentTimeMillis();
       }

       String result = iwbManager.getRecord(token, hubId, userId, rsTime, reTime, offset, limit);

       return result;

    }

    @ApiOperation(value = "销毁白板房间")
    @RequestMapping(value = "/free/{hubId}", method = RequestMethod.DELETE)
    public String destoryHub(
            @RequestHeader(name = "RC-Token") String token,
            @PathVariable (value = "hubId") String hubId
    ) throws ServiceException {
        String result = iwbManager.destroyIwb(token,hubId);
        return result;
    }

    @ApiOperation(value = "删除录像记录")
    @RequestMapping(value = "/records", method = RequestMethod.DELETE)
    public String deleteRecord(
            @RequestHeader(name = "RC-Token") String token,
            @ApiParam( value = "录像id") @RequestParam (value = "recordIds", required = false) String recordIds
    ) throws ServiceException {
        String result = iwbManager.deleteRecord(token,recordIds);
        return result;
    }

    @ApiOperation(value = "转化录像的播放地址")
    @RequestMapping(value = "/player", method = RequestMethod.POST)
    public String transPlayer(
            @RequestHeader(name = "RC-Token") String token,
            @RequestBody String body
    ) throws ServiceException {
        String result = iwbManager.transPlayer(token,body);
        return result;
    }

}

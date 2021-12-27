package com.rcloud.server.whiteboard.service;

import com.rcloud.server.whiteboard.constant.SmsServiceType;
import com.rcloud.server.whiteboard.exception.ServiceException;


public interface ApiService {


    /**
     * 获取录像记录
     * @param imToken
     * @param hubId
     * @param userId
     * @param rsTime
     * @param reTime
     * @param offset
     * @param limit
     * @return
     */
    String getRecord(String imToken, String hubId, String userId, long rsTime, long reTime, int offset, int limit);


    /**
     * 销毁白板房间
     * @param imToken
     * @param hubId
     * @param adminToken
     * @param applyId
     * @param applyName
     * @param reason
     * @return
     * @throws ServiceException
     */
    String destroyIwb(String imToken, String hubId, String adminToken, String applyId, String applyName, String reason) throws ServiceException;
}

package com.rcloud.server.whiteboard.manager;


import com.rcloud.server.whiteboard.exception.ServiceException;
import com.rcloud.server.whiteboard.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Service
@Slf4j
public class IwbManager extends BaseManager {

    @Value("classpath:region.json")
    private org.springframework.core.io.Resource regionResource;

    @Autowired
    private HttpClient httpClient;

    private Integer SUCCESS_CODE = 200;

    /**
     * 获取白板录像
     * @param token
     * @param hubId
     * @param userId
     * @param rsTime
     * @param reTime
     * @param offset
     * @param limit
     * @return
     */
    public String getRecord(String token, String hubId, String userId, long rsTime, long reTime, int offset, int limit) {
        String appkey = whiteboardConfig.getRongcloudAppKey();;
        String appSecret = whiteboardConfig.getRongcloudAppSecret();

        long timeStamp = System.currentTimeMillis();
        int nonce  = RandomUtil.randomBetween(100000, 999999);
        String signature = SignUtil.sign(appSecret,String.valueOf(nonce),String.valueOf(timeStamp));

        String apiUrlStr = whiteboardConfig.getRongcloudApiUrl();
        if (StringUtils.isEmpty(apiUrlStr)) {
            throw new RuntimeException("rongcloud client init exception");
        }
        String[] apiUrlArray = apiUrlStr.split(",");
        String mainUrl = apiUrlArray[0].trim();
        if (!mainUrl.startsWith("http://") && !mainUrl.startsWith("https://")) {
            mainUrl = "http://" + mainUrl;
        }
        mainUrl = mainUrl + "/v2/iwb/records";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("RC-App-Key",appkey);
        headers.set("RC-Nonce",String.valueOf(nonce));
        headers.set("RC-Timestamp",String.valueOf(timeStamp));
        headers.set("RC-Signature",signature);
        headers.set("RC-Token", token);

        StringBuilder stringBuilder = new StringBuilder(mainUrl);
        stringBuilder.append("?offset=").append(offset);
        stringBuilder.append("&limit=").append(limit).append("&rsTime=").append(rsTime).append("&reTime=").append(reTime);
        if (!StringUtils.isEmpty(hubId)) {
            stringBuilder.append("&hubId=").append(hubId);
        }
        if (!StringUtils.isEmpty(userId)) {
            stringBuilder.append("&userId=").append(userId);
        }

//        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
//        map.add("hubId", hubId);
//        map.add("userId", userId);
//        map.add("rsTime", rsTime);
//        map.add("reTime", reTime);
//        map.add("offset", offset);
//        map.add("limit", limit);

        ResponseEntity<String> response = httpClient.get(stringBuilder.toString(), headers);
        if (SUCCESS_CODE.equals(response.getStatusCodeValue())) {
            log.info("getRecord success,responseBody={},hubId:{}, userId:{}",response.getBody(),hubId, userId);
        } else {
            log.info("getRecord fail,responseBody={},hubId:{}, userId:{}",response.getBody(),hubId, userId);
        }
        return response.getBody();
    }

    /**
     * 销毁白板房间
     * @param imToken
     * @param hubId
     * @return
     * @throws ServiceException
     */
    public String destroyIwb(String imToken, String hubId) throws ServiceException {
        String appkey = whiteboardConfig.getRongcloudAppKey();;
        String appSecret = whiteboardConfig.getRongcloudAppSecret();

        long timeStamp = System.currentTimeMillis();
        int nonce  = RandomUtil.randomBetween(100000, 999999);
        String signature = SignUtil.sign(appSecret,String.valueOf(nonce),String.valueOf(timeStamp));

        String apiUrlStr = whiteboardConfig.getRongcloudApiUrl();
        if (StringUtils.isEmpty(apiUrlStr)) {
            throw new RuntimeException("rongcloud client init exception");
        }
        String[] apiUrlArray = apiUrlStr.split(",");
        String mainUrl = apiUrlArray[0].trim();
        if (!mainUrl.startsWith("http://") && !mainUrl.startsWith("https://")) {
            mainUrl = "http://" + mainUrl;
        }
        mainUrl = mainUrl + "/v2/iwb/" + hubId + "/free";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("RC-App-Key",appkey);
        headers.set("RC-Nonce",String.valueOf(nonce));
        headers.set("RC-Timestamp",String.valueOf(timeStamp));
        headers.set("RC-Signature",signature);
        headers.set("RC-Token", imToken);

        ResponseEntity<String> response = httpClient.delete(mainUrl, headers);
        if (SUCCESS_CODE.equals(response.getStatusCodeValue())) {
            log.info("destroyIwb success,responseBody={},body:{}",response.getBody(),hubId);
        } else {
            log.info("destroyIwb fail,responseBody={},body:{}",response.getBody(),hubId);
        }
        return response.getBody();
    }

    /**
     * 删除白板录像
     * @param imToken
     * @param ids
     * @return
     * @throws ServiceException
     */
    public String deleteRecord(String imToken, String ids) throws ServiceException {
        String appkey = whiteboardConfig.getRongcloudAppKey();;
        String appSecret = whiteboardConfig.getRongcloudAppSecret();

        long timeStamp = System.currentTimeMillis();
        int nonce  = RandomUtil.randomBetween(100000, 999999);
        String signature = SignUtil.sign(appSecret,String.valueOf(nonce),String.valueOf(timeStamp));

        String apiUrlStr = whiteboardConfig.getRongcloudApiUrl();
        if (StringUtils.isEmpty(apiUrlStr)) {
            throw new RuntimeException("rongcloud client init exception");
        }
        String[] apiUrlArray = apiUrlStr.split(",");
        String mainUrl = apiUrlArray[0].trim();
        if (!mainUrl.startsWith("http://") && !mainUrl.startsWith("https://")) {
            mainUrl = "http://" + mainUrl;
        }
        mainUrl = mainUrl + "/v2/iwb/records?recordIds=" + ids;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("RC-App-Key",appkey);
        headers.set("RC-Nonce",String.valueOf(nonce));
        headers.set("RC-Timestamp",String.valueOf(timeStamp));
        headers.set("RC-Signature",signature);
        headers.set("RC-Token", imToken);

        ResponseEntity<String> response = httpClient.delete(mainUrl, headers);
        if (SUCCESS_CODE.equals(response.getStatusCodeValue())) {
            log.info("delete record success,responseBody={},body:{}",response.getBody(),ids);
        } else {
            log.info("delete record fail,responseBody={},body:{}",response.getBody(),ids);
        }
        return response.getBody();
    }


    /**
     * 获得白板录像播放地址
     * @param imToken
     * @param body
     * @return
     * @throws ServiceException
     */
    public String transPlayer(String imToken, String body) throws ServiceException {
        String appkey = whiteboardConfig.getRongcloudAppKey();;
        String appSecret = whiteboardConfig.getRongcloudAppSecret();

        long timeStamp = System.currentTimeMillis();
        int nonce  = RandomUtil.randomBetween(100000, 999999);
        String signature = SignUtil.sign(appSecret,String.valueOf(nonce),String.valueOf(timeStamp));

        String apiUrlStr = whiteboardConfig.getRongcloudApiUrl();
        if (StringUtils.isEmpty(apiUrlStr)) {
            throw new RuntimeException("rongcloud client init exception");
        }
        String[] apiUrlArray = apiUrlStr.split(",");
        String mainUrl = apiUrlArray[0].trim();
        if (!mainUrl.startsWith("http://") && !mainUrl.startsWith("https://")) {
            mainUrl = "http://" + mainUrl;
        }
        mainUrl = mainUrl + "/v2/iwb/player";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("RC-App-Key",appkey);
        headers.set("RC-Nonce",String.valueOf(nonce));
        headers.set("RC-Timestamp",String.valueOf(timeStamp));
        headers.set("RC-Signature",signature);
        headers.set("RC-Token", imToken);

        ResponseEntity<String> response = httpClient.post(headers, mainUrl, body);
        if (SUCCESS_CODE.equals(response.getStatusCodeValue())) {
            log.info("transPlayer suc,responseBody={},body:{}",response.getBody(),body);
        } else {
            log.info("transPlayer failed,responseBody={},body:{}",response.getBody(),body);
        }
        return response.getBody();
    }

}

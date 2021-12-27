package com.rcloud.server.whiteboard.util;

import javax.annotation.Resource;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * @Author: xiuwei.nie
 * @Date: 2020/7/7
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Component
public class HttpClient {

    @Resource
    private RestTemplate restTemplate;

    public String post(String url, MultiValueMap<String, ?> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MultiValueMap<String, ?>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> entity = restTemplate.postForEntity(url, request, String.class);
        return entity.getBody();
    }

    public ResponseEntity<String> post(HttpHeaders httpHeaders,String url, MultiValueMap<String, ?> params,
        MediaType mediaType) {
        if(httpHeaders==null){
            httpHeaders = new HttpHeaders();
        }
        httpHeaders.setContentType(mediaType);
        HttpEntity<MultiValueMap<String, ?>> request = new HttpEntity<>(params, httpHeaders);
        ResponseEntity<String> entity = restTemplate.postForEntity(url, request, String.class);
        return entity;
    }

    public ResponseEntity<String> post(HttpHeaders httpHeaders,String url, String body) {
        HttpEntity<String> request = new HttpEntity<>(body, httpHeaders);
        ResponseEntity<String> entity = restTemplate.postForEntity(url, request, String.class);
        return entity;
    }


    public String get(String url) {
        return restTemplate.getForObject(url, String.class);
    }

    public ResponseEntity<String> get(String url, HttpHeaders httpHeaders) {
        HttpEntity<MultiValueMap<String, ?>> request = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<String> entity = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        return entity;
    }

    public ResponseEntity<String> delete(String url, HttpHeaders httpHeaders) {
        HttpEntity<String> request = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<String> entity = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        return entity;
    }

}

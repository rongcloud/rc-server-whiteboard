package com.rcloud.server.whiteboard.constant;

public enum ErrorCode {

    //common error
    COOKIE_ERROR(404, "Invalid cookie value!",404),
    SERVER_ERROR(500, "Server error.",500),
    PARAM_ERROR(400, "Parameter error or empty,Please check.",400),
    YUN_PIAN_SMS_ERROR(3004, "Too many times sent",3004),
    REQUEST_ERROR(400, "Request error.",400),

    //send_code
    LIMIT_ERROR(5000, "Throttle limit exceeded.",200),
    INVALID_REGION_PHONE(400, "Invalid region and phone number.",400),

    //invoke rongcloud server error
    INVOKE_RONG_CLOUD_SERVER_ERROR(2000,"RongCloud Server API Error: ",2000),


    //verify_code error
    UNKOWN_PHONE_NUMBER(404, "Unknown phone number.",404),
    VERIFY_CODE_EXPIRED(2000, "Verification code expired.",200),
    INVALID_VERIFY_CODE(1000, "Invalid verification code.",200);


    private int errorCode;
    private String errorMessage;
    private int httpStatusCode;

    ErrorCode(int errorCode, String errorMessage,int httpStatusCode) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatusCode = httpStatusCode;

    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }
}

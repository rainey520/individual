package com.ruoyi.framework.web.domain;

/**
 * 状态码枚举类
 * @Author: Rainey
 * @Date: 2019/11/26 8:34
 * @Version: 1.0
 **/
public enum HttpCode {

    /**
     * 请求成功
     */
    SUCCESS(0,"请求成功"),
    /**
     * 请求失败
     */
    FAILED(1,"请求失败"),
    /**
     * 系统异常
     */
    SYS_ERROR(500,"系统异常");

    private int code;
    private String message;

    private HttpCode(int code, String message) {
        this.code=code;
        this.message=message;
    }

    public String getMessage() {
        return this.message;
    }
    public int getCode() {
        return this.code;
    }
}

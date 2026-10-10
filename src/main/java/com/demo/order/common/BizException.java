package com.demo.order.common;

/** 业务异常（带业务码，供全局异常处理转换） */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) { this(400, message); }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() { return code; }

    public static BizException notFound(String message) { return new BizException(404, message); }
    public static BizException conflict(String message) { return new BizException(409, message); }
}

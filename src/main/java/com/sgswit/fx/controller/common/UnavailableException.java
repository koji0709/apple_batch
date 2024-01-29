package com.sgswit.fx.controller.common;

/**
 * 503异常
 */
public class UnavailableException extends RuntimeException{

    public UnavailableException() {
        super("操作频繁，请稍后重试");
    }
}

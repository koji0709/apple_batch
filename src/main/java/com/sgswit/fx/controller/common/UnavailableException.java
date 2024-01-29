package com.sgswit.fx.controller.common;

/**
 * 503异常
 */
public class UnavailableException extends RuntimeException{

    public UnavailableException(String message) {
        super(message);
    }
}

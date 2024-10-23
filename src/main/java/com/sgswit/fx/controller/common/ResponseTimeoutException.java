package com.sgswit.fx.controller.common;

import cn.hutool.core.util.StrUtil;

public class ResponseTimeoutException extends RuntimeException{
    public ResponseTimeoutException(String message) {
        super(message);
    }

    public ResponseTimeoutException(String message, String defaultMessage){
        super(!StrUtil.isEmpty(message) ? message : defaultMessage);
    }
}

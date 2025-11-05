package com.sgswit.fx.controller.exception;

import cn.hutool.core.util.StrUtil;

public class TwoFactorAuthenticationException extends RuntimeException{
    public TwoFactorAuthenticationException(String message) {
        super(message);
    }

    public TwoFactorAuthenticationException(String message, String defaultMessage){
        super(!StrUtil.isEmpty(message) ? message : defaultMessage);
    }
}

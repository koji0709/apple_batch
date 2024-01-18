package com.sgswit.fx.controller.common;

import cn.hutool.core.util.StrUtil;

public class ServiceException extends RuntimeException{
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message,String defaultMessage){
        super(!StrUtil.isEmpty(message) ? message : defaultMessage);
    }
}

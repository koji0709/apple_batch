package com.sgswit.fx.controller.common;

import cn.hutool.core.util.StrUtil;

public class PointDeduException extends RuntimeException{

    private String funCode;

    public PointDeduException(String funCode,String message) {
        super(message);
        this.funCode = funCode;
    }

    public PointDeduException(String funCode,String message, String defaultMessage){
        super(!StrUtil.isEmpty(message) ? message : defaultMessage);
        this.funCode = funCode;
    }

    public String getFunCode() {
        return funCode;
    }

    public void setFunCode(String funCode) {
        this.funCode = funCode;
    }
}

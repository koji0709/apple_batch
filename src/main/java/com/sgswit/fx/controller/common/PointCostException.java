package com.sgswit.fx.controller.common;

public class PointCostException extends RuntimeException{

    private String type;

    public PointCostException(String type,String message) {
        super(message);
        this.type = type;
    }
    public PointCostException(String message) {
        super(message);
    }

    public String getType() {
        return type;
    }
}

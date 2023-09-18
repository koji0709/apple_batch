package com.sgswit.fx.controller.iTunes.model;

/**
 * @author DeZh
 * @title: PhoneNumber
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/159:59
 */
public class PhoneNumber {
    private String areaCode;
    private String number;
    private String countryCode;

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaCode() {
        return this.areaCode;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return this.number;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return this.countryCode;
    }
}

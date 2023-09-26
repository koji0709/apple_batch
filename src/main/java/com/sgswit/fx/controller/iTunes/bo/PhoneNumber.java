package com.sgswit.fx.controller.iTunes.bo;

/**
 * @author DeZh
 * @title: PhoneNumber
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/159:59
 */
public class PhoneNumber {
    /**区号**/
    private String areaCode;
    /**电话**/
    private String number;
    /**国家拨号**/
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

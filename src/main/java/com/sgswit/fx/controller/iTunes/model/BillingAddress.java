package com.sgswit.fx.controller.iTunes.model;

/**
 * @author DeZh
 * @title: BillingAddress
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/159:57
 */
public class BillingAddress {
    private String line1;

    private String line2;

    private String line3;

    private String suburb;

    private String county;

    private String city;

    private String countryCode;

    private String postalCode;

    private String stateProvinceName;

    public void setLine1(String line1){
        this.line1 = line1;
    }
    public String getLine1(){
        return this.line1;
    }
    public void setLine2(String line2){
        this.line2 = line2;
    }
    public String getLine2(){
        return this.line2;
    }
    public void setLine3(String line3){
        this.line3 = line3;
    }
    public String getLine3(){
        return this.line3;
    }
    public void setSuburb(String suburb){
        this.suburb = suburb;
    }
    public String getSuburb(){
        return this.suburb;
    }
    public void setCounty(String county){
        this.county = county;
    }
    public String getCounty(){
        return this.county;
    }
    public void setCity(String city){
        this.city = city;
    }
    public String getCity(){
        return this.city;
    }
    public void setCountryCode(String countryCode){
        this.countryCode = countryCode;
    }
    public String getCountryCode(){
        return this.countryCode;
    }
    public void setPostalCode(String postalCode){
        this.postalCode = postalCode;
    }
    public String getPostalCode(){
        return this.postalCode;
    }
    public void setStateProvinceName(String stateProvinceName){
        this.stateProvinceName = stateProvinceName;
    }
    public String getStateProvinceName(){
        return this.stateProvinceName;
    }
}

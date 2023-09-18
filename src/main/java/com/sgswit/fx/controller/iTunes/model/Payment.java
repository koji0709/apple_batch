package com.sgswit.fx.controller.iTunes.model;

/**
 * @author DeZh
 * @title: Payment
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/159:57
 */
public class Payment {
    private OwnerName ownerName;

    private PhoneNumber phoneNumber;

    private BillingAddress billingAddress;

    private int id;

    public void setOwnerName(OwnerName ownerName){
        this.ownerName = ownerName;
    }
    public OwnerName getOwnerName(){
        return this.ownerName;
    }
    public void setPhoneNumber(PhoneNumber phoneNumber){
        this.phoneNumber = phoneNumber;
    }
    public PhoneNumber getPhoneNumber(){
        return this.phoneNumber;
    }
    public void setBillingAddress(BillingAddress billingAddress){
        this.billingAddress = billingAddress;
    }
    public BillingAddress getBillingAddress(){
        return this.billingAddress;
    }
    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return this.id;
    }
}

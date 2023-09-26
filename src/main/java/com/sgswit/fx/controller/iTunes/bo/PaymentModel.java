package com.sgswit.fx.controller.iTunes.bo;

/**
 * @author DeZh
 * @title: PaymentModel
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/2520:50
 */
public class PaymentModel {
    private OwnerName ownerName;
    private PhoneNumber phoneNumber;
    private BillingAddress billingAddress;
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
}

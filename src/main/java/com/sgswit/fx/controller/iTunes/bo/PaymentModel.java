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

    public OwnerName getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(OwnerName ownerName) {
        this.ownerName = ownerName;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public BillingAddress getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(BillingAddress billingAddress) {
        this.billingAddress = billingAddress;
    }
}

package com.sgswit.fx.controller.iTunes.bo;

/**
 * @author DeZh
 * @title: UserNationalMode
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/1510:01
 */
public class UserNationalModel {
    private String name;
    private String id;
    private PaymentModel payment;

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }

    public PaymentModel getPayment() {
        return payment;
    }

    public void setPayment(PaymentModel payment) {
        this.payment = payment;
    }
}

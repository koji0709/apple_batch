package com.sgswit.fx.controller.iTunes.model;

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
    private Payment payment;
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
    public void setPayment(Payment payment){
        this.payment = payment;
    }
    public Payment getPayment(){
        return this.payment;
    }

}

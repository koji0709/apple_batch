package com.sgswit.fx.model;


import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Balance {

    private final SimpleIntegerProperty seq;
    private final SimpleStringProperty account;
    private final SimpleStringProperty pwd;
    private final SimpleStringProperty state;
    private final SimpleStringProperty balance;
    private final SimpleStringProperty area;
    private final SimpleStringProperty name;
    private final SimpleStringProperty status;
    private final SimpleStringProperty note;
    private final SimpleStringProperty logtime;

    private  final SimpleStringProperty answer1;
    private  final SimpleStringProperty answer2;
    private  final SimpleStringProperty answer3;

    public Balance(){
        this.seq = new SimpleIntegerProperty();
        this.account = new SimpleStringProperty();
        this.pwd = new SimpleStringProperty();
        this.state = new SimpleStringProperty();
        this.balance = new SimpleStringProperty();
        this.area = new SimpleStringProperty();
        this.name = new SimpleStringProperty();
        this.status = new SimpleStringProperty();
        this.note = new SimpleStringProperty();
        this.logtime = new SimpleStringProperty();
        this.answer1 = new SimpleStringProperty();
        this.answer2 = new SimpleStringProperty();
        this.answer3 = new SimpleStringProperty();
    }

    public Integer getSeq() {
        return seq.get();
    }

    public void setSeq(Integer s) {
        seq.set(s);
    }

    public String getAccount() {
        return account.get();
    }

    public void setAccount(String a) {
        account.set(a);
    }

    public String getPwd() {
        return pwd.get();
    }

    public void setPwd(String p) {
        pwd.set(p);
    }

    public String getState() {
        return state.get();
    }

    public void setState(String s) {
        state.set(s);
    }

    public String getArea() {
        return area.get();
    }

    public void setArea(String a) {
        area.set(a);
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String s) {
        status.set(s);
    }

    public String getNote() {
        return note.get();
    }

    public void setNote(String n) {
        note.set(n);
    }

    public String getName(){return this.name.get();}

    public void setName(String name){this.name.set(name);}

    public String getLogtime(){ return this.logtime.get();}

    public void setLogtime(String logtime){this.logtime.set(logtime);}


    public String getAnswer1() {
        return answer1.get();
    }

    public void setAnswer1(String answer1) {
        this.answer1.set(answer1);
    }

    public String getAnswer2() {
        return answer2.get();
    }

    public void setAnswer2(String answer2) {
        this.answer2.set(answer2);
    }

    public String getAnswer3() {
        return answer3.get();
    }

    public void setAnswer3(String answer3) {
        this.answer3.set(answer3);
    }

    public String getBalance() {
        return balance.get();
    }

    public SimpleStringProperty balanceProperty() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance.set(balance);
    }
}

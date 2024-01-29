package com.sgswit.fx.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author DeZh
 * @title: UserInfo
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/12/111:04
 */
public final class UserInfo extends AuthData{

    private final SimpleIntegerProperty seq;
    private final SimpleStringProperty account;
    private final SimpleStringProperty pwd;
    private final SimpleStringProperty note;
    private final SimpleStringProperty name;
    private final SimpleStringProperty nationalId;
    private final SimpleStringProperty phone;

    public UserInfo() {
        this.seq = new SimpleIntegerProperty();
        this.account = new SimpleStringProperty();
        this.note = new SimpleStringProperty();
        this.pwd = new SimpleStringProperty();
        this.nationalId = new SimpleStringProperty();
        this.phone = new SimpleStringProperty();
        this.name = new SimpleStringProperty();
    }

    public int getSeq() {
        return seq.get();
    }

    public void setSeq(int seq) {
        this.seq.set(seq);
    }

    public String getAccount() {
        return account.get();
    }

    public void setAccount(String account) {
        this.account.set(account);
    }

    public String getNote() {
        return note.get();
    }

    public void setNote(String note) {
        this.note.set(note);
    }

    public String getName() {
        return name.get();
    }


    public void setName(String name) {
        this.name.set(name);
    }

    public String getNationalId() {
        return nationalId.get();
    }

    public void setNationalId(String nationalId) {
        this.nationalId.set(nationalId);
    }

    public String getPhone() {
        return phone.get();
    }


    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    public String getPwd() {
        return pwd.get();
    }

    public void setPwd(String pwd) {
        this.pwd.set(pwd);
    }
}

package com.sgswit.fx.controller.iTunes.vo;

import com.sgswit.fx.model.LoginInfo;
import javafx.beans.property.SimpleStringProperty;


public class GiftCardRedeem extends LoginInfo {
    private final SimpleStringProperty account;
    private final SimpleStringProperty pwd;
    private final SimpleStringProperty giftCardCode;
    private final SimpleStringProperty giftCardStatus;
    private final SimpleStringProperty execTime;
    private final SimpleStringProperty note;

    public GiftCardRedeem() {
        this.account = new SimpleStringProperty();
        this.pwd = new SimpleStringProperty();
        this.giftCardCode = new SimpleStringProperty();
        this.giftCardStatus = new SimpleStringProperty();
        this.note = new SimpleStringProperty();
        this.execTime = new SimpleStringProperty();
    }
    public String getAccount() {
        return account.get();
    }

    public SimpleStringProperty accountProperty() {
        return account;
    }

    public void setAccount(String account) {
        this.account.set(account);
    }

    public String getPwd() {
        return pwd.get();
    }

    public SimpleStringProperty pwdProperty() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd.set(pwd);
    }

    public String getGiftCardCode() {
        return giftCardCode.get();
    }

    public SimpleStringProperty giftCardCodeProperty() {
        return giftCardCode;
    }

    public void setGiftCardCode(String giftCardCode) {
        this.giftCardCode.set(giftCardCode);
    }

    public String getGiftCardStatus() {
        return giftCardStatus.get();
    }

    public SimpleStringProperty giftCardStatusProperty() {
        return giftCardStatus;
    }

    public void setGiftCardStatus(String giftCardStatus) {
        this.giftCardStatus.set(giftCardStatus);
    }
    public String getExecTime() {
        return execTime.get();
    }

    public SimpleStringProperty execTimeProperty() {
        return execTime;
    }

    public void setExecTime(String execTime) {
        this.execTime.set(execTime);
    }

    public String getNote() {
        return note.get();
    }

    public SimpleStringProperty noteProperty() {
        return note;
    }

    public void setNote(String note) {
        this.note.set(note);
    }

    @Override
    public String toString() {
        return "GiftCardRedeem{" +
                "account=" + account +
                ", pwd=" + pwd +
                ", giftCardCode=" + giftCardCode +
                ", giftCardStatus=" + giftCardStatus +
                ", execTime=" + execTime +
                ", note=" + note +
                '}';
    }
}

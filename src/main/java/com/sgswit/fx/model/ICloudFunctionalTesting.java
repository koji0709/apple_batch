package com.sgswit.fx.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ICloudFunctionalTesting extends LoginInfo{
    private final SimpleStringProperty account;
    private final SimpleStringProperty pwd;
    private final SimpleStringProperty area;
    private final SimpleStringProperty mailStatus;
    private final SimpleStringProperty icloudMail;
    private final SimpleStringProperty isIcloudAccount;
    private final SimpleStringProperty note;

    private final SimpleStringProperty createTime;

    public ICloudFunctionalTesting(){
        this.account = new SimpleStringProperty();
        this.pwd = new SimpleStringProperty();
        this.area = new SimpleStringProperty();
        this.mailStatus = new SimpleStringProperty();
        this.icloudMail = new SimpleStringProperty();
        this.isIcloudAccount = new SimpleStringProperty();
        this.note = new SimpleStringProperty();
        this.createTime = new SimpleStringProperty();
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

    public String getArea() {
        return area.get();
    }

    public SimpleStringProperty areaProperty() {
        return area;
    }

    public void setArea(String area) {
        this.area.set(area);
    }

    public String getMailStatus() {
        return mailStatus.get();
    }

    public SimpleStringProperty mailStatusProperty() {
        return mailStatus;
    }

    public void setMailStatus(String mailStatus) {
        this.mailStatus.set(mailStatus);
    }

    public String getIcloudMail() {
        return icloudMail.get();
    }

    public SimpleStringProperty icloudMailProperty() {
        return icloudMail;
    }

    public void setIcloudMail(String icloudMail) {
        this.icloudMail.set(icloudMail);
    }

    public String getIsIcloudAccount() {
        return isIcloudAccount.get();
    }

    public SimpleStringProperty isIcloudAccountProperty() {
        return isIcloudAccount;
    }

    public void setIsIcloudAccount(String isIcloudAccount) {
        this.isIcloudAccount.set(isIcloudAccount);
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

    public String getCreateTime() {
        return createTime.get();
    }

    public SimpleStringProperty createTimeProperty() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime.set(createTime);
    }

}

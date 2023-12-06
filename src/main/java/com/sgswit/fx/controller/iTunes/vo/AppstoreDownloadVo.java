package com.sgswit.fx.controller.iTunes.vo;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class AppstoreDownloadVo {
    private final SimpleIntegerProperty seq;
    private final SimpleStringProperty account;
    private final SimpleStringProperty pwd;
    private final SimpleStringProperty area;
    private final SimpleStringProperty itemNum;
    private final SimpleStringProperty successNum;
    private final SimpleStringProperty failNum;
    private final SimpleStringProperty note;

    public AppstoreDownloadVo(){
        this.seq = new SimpleIntegerProperty();
        this.account = new SimpleStringProperty();
        this.pwd = new SimpleStringProperty();
        this.area = new SimpleStringProperty();
        this.itemNum = new SimpleStringProperty();
        this.successNum = new SimpleStringProperty();
        this.failNum = new SimpleStringProperty();
        this.note = new SimpleStringProperty();
    }

    public int getSeq() {
        return seq.get();
    }

    public SimpleIntegerProperty seqProperty() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq.set(seq);
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

    public String getItemNum() {
        return itemNum.get();
    }

    public SimpleStringProperty itemNumProperty() {
        return itemNum;
    }

    public void setItemNum(String itemNum) {
        this.itemNum.set(itemNum);
    }

    public String getSuccessNum() {
        return successNum.get();
    }

    public SimpleStringProperty successNumProperty() {
        return successNum;
    }

    public void setSuccessNum(String successNum) {
        this.successNum.set(successNum);
    }

    public String getFailNum() {
        return failNum.get();
    }

    public SimpleStringProperty failNumProperty() {
        return failNum;
    }

    public void setFailNum(String failNum) {
        this.failNum.set(failNum);
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
        return "AppstoreDownloadVo{" +
                "seq=" + seq +
                ", account=" + account +
                ", pwd=" + pwd +
                ", area=" + area +
                ", itemNum=" + itemNum +
                ", successNum=" + successNum +
                ", failNum=" + failNum +
                ", note=" + note +
                '}';
    }
}

package com.sgswit.fx.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author DeZh
 * @title: GiftCard
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/3121:37
 */
public class GiftCard extends AuthData{
    private final SimpleStringProperty account;
    private final SimpleStringProperty pwd;
    private final SimpleIntegerProperty seq;
    private final SimpleStringProperty giftCardCode;
    private final SimpleStringProperty balance;
    private final SimpleStringProperty giftCardNumber;
    private final SimpleStringProperty note;
    private final SimpleStringProperty logTime;

    public GiftCard() {
        this.account = new SimpleStringProperty();
        this.pwd = new SimpleStringProperty();
        this.seq = new SimpleIntegerProperty();
        this.giftCardCode = new SimpleStringProperty();
        this.balance = new SimpleStringProperty();
        this.giftCardNumber = new SimpleStringProperty();
        this.note = new SimpleStringProperty();
        this.logTime = new SimpleStringProperty();
    }


    public int getSeq() {
        return seq.get();
    }

    public void setSeq(int seq) {
        this.seq.set(seq);
    }

    public String getGiftCardCode() {
        return giftCardCode.get();
    }

    public void setGiftCardCode(String giftCardCode) {
        this.giftCardCode.set(giftCardCode);
    }

    public String getBalance() {
        return balance.get();
    }

    public void setBalance(String balance) {
        this.balance.set(balance);
    }

    public String getGiftCardNumber() {
        return giftCardNumber.get();
    }

    public void setGiftCardNumber(String giftCardNumber) {
        this.giftCardNumber.set(giftCardNumber);
    }

    public String getNote() {
        return note.get();
    }

    public void setNote(String note) {
        this.note.set(note);
    }

    public String getLogTime() {
        return logTime.get();
    }

    public void setLogTime(String logTime) {
        this.logTime.set(logTime);
    }

    public String getAccount() {
        return account.get();
    }


    public void setAccount(String account) {
        this.account.set(account);
    }

    public String getPwd() {
        return pwd.get();
    }

    public void setPwd(String pwd) {
        this.pwd.set(pwd);
    }
}

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
public class CreditCard {
    private final SimpleIntegerProperty seq;
    private final SimpleStringProperty account;
    private final SimpleStringProperty pwd;
    private final SimpleStringProperty creditCardNumber;
    private final SimpleStringProperty creditCardExpirationMonth;
    private final SimpleStringProperty creditCardExpirationYear;
    private final SimpleStringProperty creditVerificationNumber;
    private final SimpleStringProperty creditInfo;
    private final SimpleStringProperty note;
    private final SimpleStringProperty monthAndYear;

    public CreditCard() {
        this.seq = new SimpleIntegerProperty();
        this.account = new SimpleStringProperty();
        this.pwd = new SimpleStringProperty();
        this.creditCardNumber = new SimpleStringProperty();
        this.creditCardExpirationMonth = new SimpleStringProperty();
        this.creditCardExpirationYear = new SimpleStringProperty();
        this.creditVerificationNumber = new SimpleStringProperty();
        this.creditInfo = new SimpleStringProperty();
        this.note = new SimpleStringProperty();
        this.monthAndYear = new SimpleStringProperty();
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

    public String getPwd() {
        return pwd.get();
    }

    public void setPwd(String pwd) {
        this.pwd.set(pwd);
    }

    public String getCreditCardNumber() {
        return creditCardNumber.get();
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber.set(creditCardNumber);
    }

    public String getCreditCardExpirationMonth() {
        return creditCardExpirationMonth.get();
    }

    public SimpleStringProperty creditCardExpirationMonthProperty() {
        return creditCardExpirationMonth;
    }

    public void setCreditCardExpirationMonth(String creditCardExpirationMonth) {
        this.creditCardExpirationMonth.set(creditCardExpirationMonth);
    }

    public String getCreditCardExpirationYear() {
        return creditCardExpirationYear.get();
    }

    public SimpleStringProperty creditCardExpirationYearProperty() {
        return creditCardExpirationYear;
    }

    public void setCreditCardExpirationYear(String creditCardExpirationYear) {
        this.creditCardExpirationYear.set(creditCardExpirationYear);
    }

    public String getCreditVerificationNumber() {
        return creditVerificationNumber.get();
    }

    public SimpleStringProperty creditVerificationNumberProperty() {
        return creditVerificationNumber;
    }

    public void setCreditVerificationNumber(String creditVerificationNumber) {
        this.creditVerificationNumber.set(creditVerificationNumber);
    }

    public String getCreditInfo() {
        return creditInfo.get();
    }

    public SimpleStringProperty creditInfoProperty() {
        return creditInfo;
    }

    public void setCreditInfo(String creditInfo) {
        this.creditInfo.set(creditInfo);
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

    public String getMonthAndYear() {
        return monthAndYear.get();
    }

    public void setMonthAndYear(String monthAndYear) {
        this.monthAndYear.set(monthAndYear);
    }
}

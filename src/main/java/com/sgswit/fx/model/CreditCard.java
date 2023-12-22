package com.sgswit.fx.model;

import com.sgswit.fx.annotation.CustomAnnotation;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableMap;

/**
 * @author DeZh
 * @title: GiftCard
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/3121:37
 */
public class CreditCard {
    private final SimpleIntegerProperty seq;
    @CustomAnnotation(copy = true,desc = "账号")
    private final SimpleStringProperty account;
    @CustomAnnotation(copy = true,desc = "密码")
    private final SimpleStringProperty pwd;
    private final SimpleStringProperty creditCardNumber;
    private final SimpleStringProperty creditCardExpirationMonth;
    private final SimpleStringProperty creditCardExpirationYear;
    private final SimpleStringProperty creditVerificationNumber;
    @CustomAnnotation(copy = true,desc = "卡号信息")
    private final SimpleStringProperty creditInfo;
    @CustomAnnotation(copy = true,desc = "执行信息")
    private final SimpleStringProperty note;
    private final SimpleMapProperty<String,Object> authData;
    private final SimpleStringProperty step;
    private final SimpleStringProperty smsCode;

    public CreditCard() {
        this.seq = new SimpleIntegerProperty();
        this.account = new SimpleStringProperty();
        this.pwd = new SimpleStringProperty();
        this.creditCardNumber = new SimpleStringProperty();
        this.creditCardExpirationMonth = new SimpleStringProperty();
        this.creditCardExpirationYear = new SimpleStringProperty();
        this.creditVerificationNumber = new SimpleStringProperty();
        this.creditInfo = new SimpleStringProperty();
        this.authData = new SimpleMapProperty<>();
        this.step = new SimpleStringProperty();
        this.smsCode = new SimpleStringProperty();
        this.note = new SimpleStringProperty();
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


    public void setNote(String note) {
        this.note.set(note);
    }

    public ObservableMap<String, Object> getAuthData() {
        return authData.get();
    }


    public void setAuthData(ObservableMap<String, Object> authData) {
        this.authData.set(authData);
    }

    public String getStep() {
        return step.get();
    }

    public void setStep(String step) {
        this.step.set(step);
    }

    public String getSmsCode() {
        return smsCode.get();
    }
    public void setSmsCode(String smsCode) {
        this.smsCode.set(smsCode);
    }
}

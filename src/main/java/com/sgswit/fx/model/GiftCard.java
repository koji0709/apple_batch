package com.sgswit.fx.model;

import javafx.beans.property.SimpleBooleanProperty;
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
    /**
     * 查询次数
     */
    private final SimpleIntegerProperty queryCount;
    /**
     * 是否有余额
     */
    private final SimpleBooleanProperty hasBalance;
    /**
     * 是否是定时任务查询
     */
    private final SimpleBooleanProperty scheduledFlag;
    /**
     * 是否正在查询标识
     */
    private final SimpleBooleanProperty running;


    public GiftCard() {
        this.account = new SimpleStringProperty();
        this.pwd = new SimpleStringProperty();
        this.seq = new SimpleIntegerProperty();
        this.giftCardCode = new SimpleStringProperty();
        this.balance = new SimpleStringProperty();
        this.giftCardNumber = new SimpleStringProperty();
        this.note = new SimpleStringProperty();
        this.logTime = new SimpleStringProperty();
        this.queryCount = new SimpleIntegerProperty(0);
        this.hasBalance = new SimpleBooleanProperty(false);
        this.scheduledFlag = new SimpleBooleanProperty(false);
        this.running = new SimpleBooleanProperty(false);
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


    public SimpleStringProperty accountProperty() {
        return account;
    }

    public SimpleStringProperty pwdProperty() {
        return pwd;
    }

    public SimpleIntegerProperty seqProperty() {
        return seq;
    }

    public SimpleStringProperty giftCardCodeProperty() {
        return giftCardCode;
    }

    public SimpleStringProperty balanceProperty() {
        return balance;
    }

    public SimpleStringProperty giftCardNumberProperty() {
        return giftCardNumber;
    }

    public SimpleStringProperty noteProperty() {
        return note;
    }

    public SimpleStringProperty logTimeProperty() {
        return logTime;
    }

    public boolean isHasBalance() {
        return hasBalance.get();
    }

    public SimpleBooleanProperty hasBalanceProperty() {
        return hasBalance;
    }

    public boolean isScheduledFlag() {
        return scheduledFlag.get();
    }

    public SimpleBooleanProperty scheduledFlagProperty() {
        return scheduledFlag;
    }

    public boolean isRunning() {
        return running.get();
    }

    public SimpleBooleanProperty runningProperty() {
        return running;
    }

    public int getQueryCount() {
        return queryCount.get();
    }

    public SimpleIntegerProperty queryCountProperty() {
        return queryCount;
    }
    public void setQueryCount(int queryCount) {
        this.queryCount.set(queryCount);
    }
}

package com.sgswit.fx.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * @author DeZh
 * @title: ConsumptionBill
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/12/111:04
 */
public final class ConsumptionBill {

    private final SimpleIntegerProperty seq;

    private final SimpleStringProperty account;

    private final SimpleStringProperty note;

    private final SimpleStringProperty area;

    private final SimpleStringProperty status;

    private final SimpleStringProperty lastPurchaseDate;

    private final SimpleStringProperty earliestPurchaseDate;

    private final SimpleStringProperty totalConsumption;

    private final SimpleStringProperty totalRefundAmount;

    private final SimpleStringProperty purchaseRecord;

    private final SimpleStringProperty paymentInformation;

    private final SimpleStringProperty shippingAddress;

    private final SimpleStringProperty pwd;

    private final SimpleStringProperty accountBalance;
    private final SimpleStringProperty whetherArrearage;
    private final SimpleStringProperty name;
    private final SimpleStringProperty familyDetails;
    public ConsumptionBill() {
        this.seq = new SimpleIntegerProperty();
        this.account = new SimpleStringProperty();
        this.note = new SimpleStringProperty();
        this.area = new SimpleStringProperty();
        this.status = new SimpleStringProperty();
        this.lastPurchaseDate = new SimpleStringProperty();
        this.earliestPurchaseDate = new SimpleStringProperty();
        this.totalConsumption = new SimpleStringProperty();
        this.totalRefundAmount = new SimpleStringProperty();
        this.purchaseRecord = new SimpleStringProperty();
        this.paymentInformation = new SimpleStringProperty();
        this.shippingAddress = new SimpleStringProperty();
        this.pwd = new SimpleStringProperty();
        this.accountBalance = new SimpleStringProperty();
        this.whetherArrearage = new SimpleStringProperty();
        this.name = new SimpleStringProperty();
        this.familyDetails = new SimpleStringProperty();
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

    public String getArea() {
        return area.get();
    }

    public void setArea(String area) {
        this.area.set(area);
    }

    public String getStatus() {
        return status.get();
    }


    public void setStatus(String status) {
        this.status.set(status);
    }

    public String getLastPurchaseDate() {
        return lastPurchaseDate.get();
    }


    public void setLastPurchaseDate(String lastPurchaseDate) {
        this.lastPurchaseDate.set(lastPurchaseDate);
    }

    public String getEarliestPurchaseDate() {
        return earliestPurchaseDate.get();
    }

    public void setEarliestPurchaseDate(String earliestPurchaseDate) {
        this.earliestPurchaseDate.set(earliestPurchaseDate);
    }

    public String getTotalConsumption() {
        return totalConsumption.get();
    }

    public void setTotalConsumption(String totalConsumption) {
        this.totalConsumption.set(totalConsumption);
    }

    public String getTotalRefundAmount() {
        return totalRefundAmount.get();
    }

    public void setTotalRefundAmount(String totalRefundAmount) {
        this.totalRefundAmount.set(totalRefundAmount);
    }

    public String getPurchaseRecord() {
        return purchaseRecord.get();
    }


    public void setPurchaseRecord(String purchaseRecord) {
        this.purchaseRecord.set(purchaseRecord);
    }

    public String getPaymentInformation() {
        return paymentInformation.get();
    }

    public void setPaymentInformation(String paymentInformation) {
        this.paymentInformation.set(paymentInformation);
    }

    public String getShippingAddress() {
        return shippingAddress.get();
    }


    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress.set(shippingAddress);
    }

    public String getPwd() {
        return pwd.get();
    }

    public void setPwd(String pwd) {
        this.pwd.set(pwd);
    }

    public String getAccountBalance() {
        return accountBalance.get();
    }

    public void setAccountBalance(String accountBalance) {
        this.accountBalance.set(accountBalance);
    }

    public String getWhetherArrearage() {
        return whetherArrearage.get();
    }

    public void setWhetherArrearage(String whetherArrearage) {
        this.whetherArrearage.set(whetherArrearage);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getFamilyDetails() {
        return familyDetails.get();
    }

    public void setFamilyDetails(String familyDetails) {
        this.familyDetails.set(familyDetails);
    }
}

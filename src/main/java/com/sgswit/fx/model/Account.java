package com.sgswit.fx.model;


import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Account extends LoginInfo {

    private final SimpleIntegerProperty seq;
    private final SimpleStringProperty account;
    private final SimpleStringProperty pwd;
    private final SimpleStringProperty state;
    private final SimpleStringProperty birthday;
    private final SimpleStringProperty area;
    private final SimpleStringProperty areaId;
    private final SimpleStringProperty areaCode;
    private final SimpleStringProperty phone;
    private final SimpleStringProperty name;
    private final SimpleStringProperty status;
    private final SimpleStringProperty note;
    private final SimpleStringProperty logtime;
    private final SimpleStringProperty answer1;
    private final SimpleStringProperty answer2;
    private final SimpleStringProperty answer3;
    private final SimpleStringProperty originalCountry;
    private final SimpleStringProperty targetCountry;
    private final SimpleStringProperty dsid;
    private final SimpleStringProperty support;
    private final SimpleStringProperty familyDetails;

    private  String country;

    // 页面临时参数
    private SimpleStringProperty email;
    private SimpleStringProperty popKey;
    private SimpleStringProperty pin;
    private SimpleStringProperty pinExpir;
    private SimpleStringProperty disableStatus;
    private SimpleStringProperty balance;
    private SimpleStringProperty createTime;
    private SimpleStringProperty inspection;
    private SimpleStringProperty purchasesLast90Count;

    private SimpleStringProperty paymentAccount;
    private SimpleStringProperty paymentPwd;

    private SimpleStringProperty memberAccount;
    private SimpleStringProperty memberPwd;
    private SimpleStringProperty cvv;
    private SimpleStringProperty rescueEmail;

    private SimpleStringProperty createdDate;
    private SimpleStringProperty updateDate;



    public Account(){
        this.seq = new SimpleIntegerProperty();
        this.account = new SimpleStringProperty();
        this.pwd = new SimpleStringProperty();
        this.state = new SimpleStringProperty();
        this.area = new SimpleStringProperty();
        this.areaCode = new SimpleStringProperty();
        this.phone = new SimpleStringProperty();
        this.name = new SimpleStringProperty();
        this.status = new SimpleStringProperty();
        this.note = new SimpleStringProperty();
        this.logtime = new SimpleStringProperty();
        this.answer1 = new SimpleStringProperty();
        this.answer2 = new SimpleStringProperty();
        this.answer3 = new SimpleStringProperty();
        this.birthday = new SimpleStringProperty();
        this.originalCountry = new SimpleStringProperty();
        this.targetCountry = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.popKey = new SimpleStringProperty();
        this.dsid = new SimpleStringProperty();
        this.support = new SimpleStringProperty();
        this.pin = new SimpleStringProperty();
        this.pinExpir = new SimpleStringProperty();
        this.disableStatus = new SimpleStringProperty();
        this.balance = new SimpleStringProperty();
        this.createTime = new SimpleStringProperty();
        this.inspection = new SimpleStringProperty();
        this.purchasesLast90Count = new SimpleStringProperty();
        this.familyDetails = new SimpleStringProperty();
        this.paymentAccount = new SimpleStringProperty();
        this.paymentPwd = new SimpleStringProperty();
        this.memberAccount = new SimpleStringProperty();
        this.memberPwd = new SimpleStringProperty();
        this.cvv = new SimpleStringProperty();
        this.rescueEmail = new SimpleStringProperty();
        this.createdDate = new SimpleStringProperty();
        this.updateDate = new SimpleStringProperty();
        this.areaId = new SimpleStringProperty();

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

    public String getAreaId() {
        return areaId.get();
    }

    public void setAreaId(String a) {
        areaId.set(a);
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

    public SimpleStringProperty noteProperty() {
        return note;
    }

    public void setNote(String note) {
        this.note.set(note);
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

    public String getOriginalCountry() {
        return originalCountry.get();
    }

    public void setOriginalCountry(String originalCountry) {
        this.originalCountry.set(originalCountry);
    }

    public String getTargetCountry() {
        return targetCountry.get();
    }

    public void setTargetCountry(String targetCountry) {
        this.targetCountry.set(targetCountry);
    }

    public String getBirthday() {
        return birthday.get();
    }

    public void setBirthday(String birthday) {
        this.birthday.set(birthday);
    }

    public String getPhone() {
        return phone.get();
    }

    public SimpleStringProperty phoneProperty() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    public String getEmail() {
        return email.get();
    }

    public SimpleStringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getPopKey() {
        return popKey.get();
    }

    public SimpleStringProperty popKeyProperty() {
        return popKey;
    }

    public void setPopKey(String popKey) {
        this.popKey.set(popKey);
    }

    public String getDsid() {
        return dsid.get();
    }


    public void setDsid(String dsid) {
        this.dsid.set(dsid);
    }

    public String getSupport() {
        return support.get();
    }


    public void setSupport(String support) {
        this.support.set(support);
    }

    public String getPin() {
        return pin.get();
    }

    public SimpleStringProperty pinProperty() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin.set(pin);
    }

    public String getPinExpir() {
        return pinExpir.get();
    }

    public SimpleStringProperty pinExpirProperty() {
        return pinExpir;
    }

    public void setPinExpir(String pinExpir) {
        this.pinExpir.set(pinExpir);
    }

    public String getAreaCode() {
        return areaCode.get();
    }

    public SimpleStringProperty areaCodeProperty() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode.set(areaCode);
    }

    public String getDisableStatus() {
        return disableStatus.get();
    }

    public SimpleStringProperty disableStatusProperty() {
        return disableStatus;
    }

    public void setDisableStatus(String disableStatus) {
        this.disableStatus.set(disableStatus);
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

    public String getCreateTime() {
        return createTime.get();
    }

    public SimpleStringProperty createTimeProperty() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime.set(createTime);
    }

    public String getInspection() {
        return inspection.get();
    }

    public SimpleStringProperty inspectionProperty() {
        return inspection;
    }

    public void setInspection(String inspection) {
        this.inspection.set(inspection);
    }

    public String getPurchasesLast90Count() {
        return purchasesLast90Count.get();
    }

    public SimpleStringProperty purchasesLast90CountProperty() {
        return purchasesLast90Count;
    }

    public void setPurchasesLast90Count(String purchasesLast90Count) {
        this.purchasesLast90Count.set(purchasesLast90Count);
    }

    public String getFamilyDetails() {
        return familyDetails.get();
    }

    public void setFamilyDetails(String familyDetails) {
        this.familyDetails.set(familyDetails);
    }

    public String getPaymentAccount() {
        return paymentAccount.get();
    }

    public SimpleStringProperty paymentAccountProperty() {
        return paymentAccount;
    }

    public void setPaymentAccount(String paymentAccount) {
        this.paymentAccount.set(paymentAccount);
    }

    public String getPaymentPwd() {
        return paymentPwd.get();
    }

    public SimpleStringProperty paymentPwdProperty() {
        return paymentPwd;
    }

    public void setPaymentPwd(String paymentPwd) {
        this.paymentPwd.set(paymentPwd);
    }

    public String getMemberAccount() {
        return memberAccount.get();
    }

    public SimpleStringProperty memberAccountProperty() {
        return memberAccount;
    }

    public void setMemberAccount(String memberAccount) {
        this.memberAccount.set(memberAccount);
    }

    public String getMemberPwd() {
        return memberPwd.get();
    }

    public SimpleStringProperty memberPwdProperty() {
        return memberPwd;
    }

    public void setMemberPwd(String memberPwd) {
        this.memberPwd.set(memberPwd);
    }

    public String getCvv() {
        return cvv.get();
    }

    public SimpleStringProperty cvvProperty() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv.set(cvv);
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRescueEmail() {
        return rescueEmail.get();
    }

    public SimpleStringProperty rescueEmailProperty() {
        return rescueEmail;
    }

    public void setRescueEmail(String rescueEmail) {
        this.rescueEmail.set(rescueEmail);
    }

    public String getCreatedDate() {
        return createdDate.get();
    }

    public SimpleStringProperty createdDateProperty() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate.set(createdDate);
    }

    public String getUpdateDate() {
        return updateDate.get();
    }

    public SimpleStringProperty updateDateProperty() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate.set(updateDate);
    }


    @Override
    public String toString() {
        return "Account{" +
                "seq=" + seq +
                ", account=" + account +
                ", pwd=" + pwd +
                ", state=" + state +
                ", birthday=" + birthday +
                ", area=" + area +
                ", areaCode=" + areaCode +
                ", phone=" + phone +
                ", name=" + name +
                ", status=" + status +
                ", note=" + note +
                ", logtime=" + logtime +
                ", answer1=" + answer1 +
                ", answer2=" + answer2 +
                ", answer3=" + answer3 +
                ", originalCountry=" + originalCountry +
                ", targetCountry=" + targetCountry +
                ", dsid=" + dsid +
                ", support=" + support +
                ", familyDetails=" + familyDetails +
                ", country='" + country + '\'' +
                ", email=" + email +
                ", popKey=" + popKey +
                ", pin=" + pin +
                ", pinExpir=" + pinExpir +
                ", disableStatus=" + disableStatus +
                ", balance=" + balance +
                ", createTime=" + createTime +
                ", inspection=" + inspection +
                ", purchasesLast90Count=" + purchasesLast90Count +
                ", paymentAccount=" + paymentAccount +
                ", paymentPwd=" + paymentPwd +
                ", memberAccount=" + memberAccount +
                ", memberPwd=" + memberPwd +
                ", cvv=" + cvv +
                ", rescueEmail=" + rescueEmail +
                ", createdDate=" + createdDate +
                ", updateDate=" + updateDate +
                '}';
    }
}

package com.sgswit.fx.model;


import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Problem {

    private final SimpleIntegerProperty seq;
    private final SimpleStringProperty account;
    private final SimpleStringProperty pwd;
    private final SimpleStringProperty state;
    private final SimpleStringProperty birthday;
    private final SimpleStringProperty aera;
    private final SimpleStringProperty name;
    private final SimpleStringProperty status;
    private final SimpleStringProperty note;
    private final SimpleStringProperty logtime;
    private  final SimpleStringProperty problem1;
    private  final SimpleStringProperty problem2;
    private  final SimpleStringProperty problem3;
    private  final SimpleStringProperty answer1;
    private  final SimpleStringProperty answer2;
    private  final SimpleStringProperty answer3;
    private  final SimpleStringProperty originalCountry;
    private  final SimpleStringProperty targetCountry;


    public Problem(){
        this.seq = new SimpleIntegerProperty();
        this.account = new SimpleStringProperty();
        this.pwd = new SimpleStringProperty();
        this.state = new SimpleStringProperty();
        this.aera = new SimpleStringProperty();
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
        this.problem1 = new SimpleStringProperty();
        this.problem2 = new SimpleStringProperty();
        this.problem3 = new SimpleStringProperty();
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

    public String getAera() {
        return aera.get();
    }

    public void setAera(String a) {
        aera.set(a);
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

    public String getProblem1() {
        return problem1.get();
    }

    public SimpleStringProperty problem1Property() {
        return problem1;
    }

    public void setProblem1(String problem1) {
        this.problem1.set(problem1);
    }

    public String getProblem2() {
        return problem2.get();
    }

    public SimpleStringProperty problem2Property() {
        return problem2;
    }

    public void setProblem2(String problem2) {
        this.problem2.set(problem2);
    }

    public String getProblem3() {
        return problem3.get();
    }

    public SimpleStringProperty problem3Property() {
        return problem3;
    }

    public void setProblem3(String problem3) {
        this.problem3.set(problem3);
    }

    public void appendNote(String n) {
        if (n != null && n != ""){
            String s = note.get();
            if (s != null && s != ""){
                note.set(s+n);
            }else{
                note.set(n);
            }
        }
    }

    @Override
    public String toString() {
        return "Account{" +
                "seq=" + seq +
                ", account=" + account +
                ", pwd=" + pwd +
                ", state=" + state +
                ", aera=" + aera +
                ", name=" + name +
                ", status=" + status +
                ", note=" + note +
                ", logtime=" + logtime +
                ", problem1=" + problem1 +
                ", problem2=" + problem2 +
                ", problem3=" + problem3 +
                ", answer1=" + answer1 +
                ", answer2=" + answer2 +
                ", answer3=" + answer3 +
                ", birthday=" + birthday +
                '}';
    }

}

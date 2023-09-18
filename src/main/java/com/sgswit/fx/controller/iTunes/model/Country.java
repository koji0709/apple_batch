package com.sgswit.fx.controller.iTunes.model;


import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author DELL
 */
public class Country {

    private final SimpleIntegerProperty seqNo;
    private final SimpleStringProperty countryName;

    public Country(){
        this.seqNo = new SimpleIntegerProperty();
        this.countryName = new SimpleStringProperty();
    }



    public Integer getSeqNo() {
        return seqNo.get();
    }

    public void setSeqNo(Integer s) {
        seqNo.set(s);
    }

    public String getCountryName() {
        return countryName.get();
    }

    public void setCountryName(String a) {
        countryName.set(a);
    }


}

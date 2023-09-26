package com.sgswit.fx.controller.iTunes.vo;


import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * @author DELL
 */
public class CountryVo {

    private final SimpleStringProperty id;
    private final SimpleIntegerProperty seqNo;
    private final SimpleStringProperty countryName;

    public CountryVo(){
        this.id = new SimpleStringProperty();
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

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }
}

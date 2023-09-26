package com.sgswit.fx.controller.iTunes.vo;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Map;

/**
 * @author DeZh
 * @title: BillingAddress
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/159:57
 */
public class BillingAddressVo {
    protected final SimpleStringProperty id;
    protected final SimpleStringProperty value;
    protected final SimpleStringProperty type;
    protected final SimpleBooleanProperty required;
    protected final SimpleStringProperty requiredDesc;
    protected final SimpleStringProperty title;
    protected final SimpleStringProperty placeholder;
    protected final ObservableList<Map<String,String>> values;

    public BillingAddressVo() {
        this.id = new SimpleStringProperty();
        this.value = new SimpleStringProperty();
        this.type = new SimpleStringProperty();
        this.required = new SimpleBooleanProperty();
        this.requiredDesc = new SimpleStringProperty();
        this.title = new SimpleStringProperty();
        this.placeholder = new SimpleStringProperty();
        this.values =  FXCollections.observableArrayList();;
    }

    public String getId() {
        return id.get();
    }

    public SimpleStringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getValue() {
        return value.get();
    }


    public void setValue(String value) {
        this.value.set(value);
    }

    public String getType() {
        return type.get();
    }


    public void setType(String type) {
        this.type.set(type);
    }

    public boolean isRequired() {
        return required.get();
    }


    public void setRequired(boolean required) {
        this.required.set(required);
    }

    public String getRequiredDesc() {
        return requiredDesc.get();
    }


    public void setRequiredDesc(String requiredDesc) {
        this.requiredDesc.set(requiredDesc);
    }

    public String getTitle() {
        return title.get();
    }


    public void setTitle(String title) {
        this.title.set(title);
    }

    public String getPlaceholder() {
        return placeholder.get();
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder.set(placeholder);
    }

    public ObservableList<Map<String, String>> getValues() {
        return values;
    }
}

package com.sgswit.fx.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableMap;

/**
 * @author DeZh
 * @title: AuthData
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/12/2218:21
 */
public class AuthData {
    private final SimpleMapProperty<String,Object> authData;
    private final SimpleStringProperty step;
    private final SimpleStringProperty authCode;
    public AuthData() {
        this.authData = new SimpleMapProperty<>();
        this.step = new SimpleStringProperty();
        this.authCode = new SimpleStringProperty();
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

    public String getAuthCode() {
        return authCode.get();
    }

    public void setAuthCode(String authCode) {
        this.authCode.set(authCode);
    }
}

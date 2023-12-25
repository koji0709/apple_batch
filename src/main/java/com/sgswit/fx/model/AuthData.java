package com.sgswit.fx.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author DeZh
 * @title: AuthData
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/12/2218:21
 */
public class AuthData {
    private Map<String,Object> authData = new HashMap<>();
    private String step;
    private String authCode;

    public Map<String, Object> getAuthData() {
        return authData;
    }

    public void setAuthData(Map<String, Object> authData) {
        this.authData = authData;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }
}

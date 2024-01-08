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
    private String authCode = "";
    private boolean hasFinished = true;
    /**数据成功状态：0-失败，1-成功，默认为空**/
    private String dataStatus = "";
    /**失败次数**/
    private int failCount = 0;

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

    public boolean isHasFinished() {
        return hasFinished;
    }

    public void setHasFinished(boolean hasFinished) {
        this.hasFinished = hasFinished;
    }

    public String getDataStatus() {
        return dataStatus;
    }

    public void setDataStatus(String dataStatus) {
        this.dataStatus = dataStatus;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }
}

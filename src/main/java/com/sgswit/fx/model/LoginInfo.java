package com.sgswit.fx.model;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.http.HttpResponse;
import com.sgswit.fx.utils.CookieUtils;

import java.util.HashMap;
import java.util.Map;

public class LoginInfo extends AuthData{
    private String domainId = "1";
    private String clientId = "af1139274f266b22b68c2a3e7ad932cb3c0bbe854e13a79af78dcc73136882c3";
    private String frameId = createFrameId();

    // ----------------- AppleID
    private String scnt;

    private String sstt;

    private String XAppleIDSessionId;

    private Map<String,String> cookieMap;

    // 双重认证 device-code or sms-code
    private String securityCode;


    // ----------------- iTunes
    private String itspod;

    private String storeFront;

    private String dsPersonId;

    private String passwordToken;

    private String guid;

    // -----------------
    private boolean isLogin;

    public void clearLoginInfo() {
        this.isLogin = false;
        this.scnt = null;
        this.cookieMap = null;
        this.XAppleIDSessionId = null;
    }

    public void updateLoginInfo(HttpResponse rsp){
        CookieUtils.setCookiesToMap(rsp,getCookieMap());

        if(StrUtil.isNotEmpty(rsp.header("scnt"))){
            setScnt(rsp.header("scnt"));
        }
        if(StrUtil.isNotEmpty(rsp.header("sstt"))){
            setSstt(rsp.header("sstt"));
        }

    }
    private String createFrameId(){
        Digester md5 = new Digester(DigestAlgorithm.MD5);
        StringBuilder sb = new StringBuilder();

        sb.append("auth-");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(2,10));
        sb.append("-");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(2,6));
        sb.append("-");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(2,6));
        sb.append("-");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(2,6));
        sb.append("-");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(2,10));

        return sb.toString();
    }
    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public  Map<String, String> getCookieMap() {
        if(cookieMap == null){
            cookieMap = new HashMap<>();
        }
        return cookieMap;
    }

    public String getCookie(){
        return MapUtil.join(getCookieMap(),";","=",true);
    }

    public void setCookieMap(Map<String, String> cookieMap) {
        this.cookieMap = cookieMap;
    }

    public String getScnt() {
        return scnt;
    }

    public void setScnt(String scnt) {
        this.scnt = scnt;
    }

    public String getXAppleIDSessionId() {
        return XAppleIDSessionId;
    }

    public void setXAppleIDSessionId(String XAppleIDSessionId) {
        this.XAppleIDSessionId = XAppleIDSessionId;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setIsLogin(boolean login) {
        isLogin = login;
    }

    public String getItspod() {
        return itspod;
    }

    public void setItspod(String itspod) {
        this.itspod = itspod;
    }

    public String getStoreFront() {
        return storeFront;
    }

    public void setStoreFront(String storeFront) {
        this.storeFront = storeFront;
    }

    public String getDsPersonId() {
        return dsPersonId;
    }

    public void setDsPersonId(String dsPersonId) {
        this.dsPersonId = dsPersonId;
    }

    public String getPasswordToken() {
        return passwordToken;
    }

    public void setPasswordToken(String passwordToken) {
        this.passwordToken = passwordToken;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getSstt() {
        return sstt;
    }

    public void setSstt(String sstt) {
        this.sstt = sstt;
    }
}

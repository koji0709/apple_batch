package com.sgswit.fx.controller.common;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.exception.ServiceException;
import com.sgswit.fx.model.LoginInfo;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.ICloudUtil;
import com.sgswit.fx.utils.ICloudWeblogin;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.ContextMenuEvent;

import java.net.URL;
import java.util.*;

public class ICloudView<T> extends CustomTableView<T> {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }

    /**
     * iTunes登录鉴权
     */
    public void iCloudLogin(T account) {
        LoginInfo loginInfo = (LoginInfo) account;
        // 第一次进入icloud.com
        if (loginInfo.isLogin()){
            return;
        }
        if (StrUtil.isEmpty(loginInfo.getSecurityCode())) {
            String accountNo = ((SimpleStringProperty) ReflectUtil.getFieldValue(account, "account")).getValue();
            String pwd = ((SimpleStringProperty) ReflectUtil.getFieldValue(account, "pwd")).getValue();

            String clientId = ICloudUtil.getClientId();
            String frameId = ICloudWeblogin.createFrameId();
            String redirectUri = "https://www.icloud.com";
            String domain = "icloud.com";

            Map<String, String> signInMap = new HashMap<>();
            signInMap.put("clientId", clientId);
            signInMap.put("frameId", frameId);
            signInMap.put("redirectUri", redirectUri);
            signInMap.put("domain", domain);
            signInMap.put("account", accountNo);
            signInMap.put("pwd", pwd);

            iCloudLoginHandler(account, signInMap);
        } else {
            // 双重认证登录
            Map<String, Object> authData = loginInfo.getAuthData();
            HttpResponse signInRsp = (HttpResponse) authData.get("signInRsp");
            HttpResponse authRsp = (HttpResponse) authData.get("authRsp");

            Map<String, String> signInMap = (Map<String, String>) authData.get("signInMap");
            signInMap.put("securityCode", loginInfo.getSecurityCode());
            signInMap.put("cookie", loginInfo.getCookie());
            if (authRsp.getStatus() != 200) {
                throw new ServiceException("登录失败");
            }

            HttpResponse securityCodeRsp = ICloudUtil.securityCode(signInMap, authRsp);
            String body = securityCodeRsp.body();
            if (securityCodeRsp.getStatus() != 200 && securityCodeRsp.getStatus() != 204) {
                String message = AppleIDUtil.getValidationErrors(securityCodeRsp, "登录失败");
                throw new ServiceException(message);
            }

            HttpResponse trustRsp = ICloudUtil.trust(signInMap,securityCodeRsp);
            HttpResponse accountLoginRsp = ICloudUtil.accountLogin(trustRsp, signInMap.get("domain"));
            loginInfo.getAuthData().put("accountLoginRsp",accountLoginRsp);
            ((LoginInfo) account).setIsLogin(true);
            setAndRefreshNote(account,"登录成功");
        }
    }

    public HttpResponse iCloudLoginHandler(T account, Map<String, String> signInMap) {
        String clientId = signInMap.get("clientId");
        String frameId = signInMap.get("frameId");
        String domain = signInMap.get("domain");

        //登录 通用 www.icloud.com
        setAndRefreshNote(account, "正在登录...");
        HttpResponse signInRsp = ICloudWeblogin.signin(signInMap);
        //非双重认证账号，登录后，http code = 412；
        //        此时返回的header中 有 X-Apple-Repair-Session-Token ，无 X-Apple-Session-Token，
        //        需先进行处理后，才能返回X-Apple-Session-Token
        // 双重认证账号，登录成功后， http code = 409；
        //        但此时返回的header中包含 X-Apple-Session-Token，可直接使用获取icloud账户信息
        int status = signInRsp.getStatus();
        if (status != 412 && status != 409) {
            String message = AppleIDUtil.getValidationErrors(signInRsp, "登录失败; status=" + status);
            throw new ServiceException(message,"登录失败; status=" + status);
        }
        // 412普通登录, 409双重登录
        if (status == 412) {
            HttpResponse repairRes = ICloudUtil.appleIDrepair(signInRsp);
            //获取session-id，后续操作需基于该id进行处理
            String sessionId = ICloudUtil.getSessionId(repairRes);
            HttpResponse optionsRes = ICloudUtil.appleIDrepairOptions(signInRsp, repairRes, clientId, sessionId);
            HttpResponse upgradeRes = ICloudUtil.appleIDUpgrade(signInRsp, optionsRes, clientId, sessionId);
            HttpResponse setuplaterRes = ICloudUtil.appleIDSetuplater(signInRsp, optionsRes, upgradeRes, clientId, sessionId);
            HttpResponse options2Res = ICloudUtil.appleIDrepairOptions2(signInRsp, optionsRes, setuplaterRes, clientId, sessionId);
            HttpResponse completeRes = ICloudUtil.appleIDrepairComplete(signInRsp, options2Res, clientId, sessionId);
            signInRsp = completeRes;
        } else if (status == 409) {
            HttpResponse authRsp = ICloudUtil.auth(signInRsp, frameId, clientId, domain);
            Map<String, Object> authData = new HashMap<>();
            authData.put("authRsp", authRsp);
            authData.put("signInRsp", signInRsp);
            authData.put("signInMap", signInMap);
            LoginInfo loginInfo = (LoginInfo) account;
            loginInfo.setAuthData(authData);
            setAndRefreshNote(account, "此账号已开启双重认证");
        }

        HttpResponse accountLoginRsp = null;
        String sessionToken = signInRsp.header("X-Apple-Session-Token");
        if (!StrUtil.isEmpty(sessionToken)) {
            accountLoginRsp = ICloudUtil.accountLogin(signInRsp, domain);
            repairWebICloud(accountLoginRsp, account, domain);
        }

        if (accountLoginRsp.getStatus() == 302) {
            //非 www.icloud.com账户（亦即美国账户），需要到具体国家的icloud域名上获取账户信息
            JSONObject jo = JSONUtil.parseObj(accountLoginRsp.body());
            domain = jo.getStr("domainToUse").toLowerCase();
            signInMap.put("redirectUri", "https://www." + domain);
            signInMap.put("domain", domain);
            return iCloudLoginHandler(account, signInMap);
        }

        Object note = ReflectUtil.getFieldValue(account, "getNote");
        // 如果是双重认证不该扣分数,抛出异常补给用户
        if ("此账号已开启双重认证".equals(note)){
            throw new ServiceException(note.toString());
        }

        LoginInfo loginInfo = (LoginInfo) account;
        loginInfo.getAuthData().put("accountLoginRsp", accountLoginRsp);
        if (status == 412) {
            loginInfo.setIsLogin(true);
            setAndRefreshNote(account, "登录成功");
        }
        return accountLoginRsp;
    }

    public void repairWebICloud(HttpResponse accountLoginRsp, T account, String domain) {
        JSONObject body = JSONUtil.parseObj(accountLoginRsp.body());
        Boolean isRepairNeeded = body.getBool("isRepairNeeded", false);
        if (isRepairNeeded) {
            setAndRefreshNote(account, "同意协议中..");
            HttpResponse repairDoneRsp = ICloudUtil.repairWebICloud(accountLoginRsp, domain);
            Boolean success = JSONUtil.parseObj(repairDoneRsp.body()).getBool("success");
            if (!success) {
                throw new ServiceException("iCloud网页登录修复失败");
            }
        }
    }

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        onContentMenuClick(contextMenuEvent, new ArrayList<>(menuItem));
    }

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent, List<String> menuItemList) {
        super.onContentMenuClick(contextMenuEvent, accountTableView, menuItemList, new ArrayList<>());
    }

}

package com.sgswit.fx.controller.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.MapUtils;
import javafx.collections.ObservableMap;
import javafx.scene.input.ContextMenuEvent;

import java.net.URL;
import java.util.*;

public class AppleIdView extends CustomTableView<Account> {


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }

    public HttpResponse signIn(Account account){
        // SignIn
        HttpResponse signInRsp = AppleIDUtil.signin(account);

        if(signInRsp.getStatus()==503){
            throwAndRefreshNote(account,"操作频繁，请稍后重试！");
        }

        String status = "正常";
        if (!StrUtil.isEmpty(signInRsp.body())){
            String failMessage = "";
            JSONArray errorArr = JSONUtil.parseObj(signInRsp.body())
                    .getByPath("serviceErrors",JSONArray.class);
            if (errorArr != null && errorArr.size()>0){
                JSONObject err = (JSONObject)(errorArr.get(0));
                if ("-20209".equals(err.getStr("code"))){
                    status = "锁定";
                }
                for (Object o : errorArr) {
                    JSONObject jsonObject = (JSONObject) o;
                    failMessage += jsonObject.getByPath("message") + ";";
                }
                setAndRefreshNote(account,failMessage);
            }
            account.setStatus(status);
        }

        if(signInRsp.getStatus()!=409){
            throwAndRefreshNote(account,"请检查用户名密码是否正确");
        }

        return signInRsp;
    }

    /**
     * appleid官网登录(不区分登录方式)
     */
    public void login(Account account){
        if (account.isLogin()){
            return;
        }

        HttpResponse securityCodeOrReparCompleteRsp = null;

        // 代表属于验证码再次执行
        if (!StrUtil.isEmpty(account.getSecurityCode())){
            Map<String, Object> authData = account.getAuthData();
            HttpResponse authRsp = (HttpResponse) authData.get("authRsp");
            if (authRsp == null){
                throwAndRefreshNote(account,"请先执行程序;");
            }
            securityCodeOrReparCompleteRsp = AppleIDUtil.securityCode(account,authRsp);
        }else{
            setAndRefreshNote(account,"正在验证账号密码...",false);
            HttpResponse signInRsp = signIn(account);
            if(signInRsp.getStatus()!=409){
                throwAndRefreshNote(account,"请检查用户名密码是否正确;");
            }
            // Auth
            HttpResponse authRsp = AppleIDUtil.auth(account,signInRsp);
            String authType = JSONUtil.parse(signInRsp.body()).getByPath("authType",String.class);

            // 双重认证
            if ("hsa2".equals(authType)) {
                account.getAuthData().put("authRsp",authRsp);
                throwAndRefreshNote(account,"此账号已开启双重认证;");
            } else { // sa 密保认证
                if (StrUtil.isEmpty(account.getAnswer1()) || StrUtil.isEmpty(account.getAnswer2()) || StrUtil.isEmpty(account.getAnswer3())){
                    throwAndRefreshNote(account,"密保认证必须输入密保问题;");
                }
                // 密保认证
                setAndRefreshNote(account,"正在验证密保问题...",false);
                HttpResponse questionRsp = AppleIDUtil.questions(account,authRsp);
                if (questionRsp.getStatus() != 412) {
                    throwAndRefreshNote(account,"密保问题验证失败;");
                }
                setAndRefreshNote(account,"密保问题验证通过",false);
                ThreadUtil.sleep(500);
                setAndRefreshNote(account,"正在阅读协议...",false);
                HttpResponse accountRepairRsp = AppleIDUtil.accountRepair(account,questionRsp);
                String XAppleIDSessionId = "";
                String scnt = accountRepairRsp.header("scnt");

                for (String item : accountRepairRsp.headerList("Set-Cookie")) {
                    if (item.startsWith("aidsp")) {
                        XAppleIDSessionId = item.substring(item.indexOf("aidsp=") + 6, item.indexOf("; Domain=appleid.apple.com"));
                    }
                }
                setAndRefreshNote(account,"正在同意协议...",false);
                HttpResponse repareOptionsRsp = AppleIDUtil.repareOptions(account, questionRsp, accountRepairRsp);
                HttpResponse securityUpgradeRsp = AppleIDUtil.securityUpgrade(account,repareOptionsRsp,XAppleIDSessionId,scnt);
                HttpResponse securityUpgradeSetuplaterRsp = AppleIDUtil.securityUpgradeSetuplater(account,securityUpgradeRsp,XAppleIDSessionId,scnt);
                HttpResponse repareOptionsSecondRsp = AppleIDUtil.repareOptionsSecond(account,securityUpgradeSetuplaterRsp,XAppleIDSessionId,scnt);
                securityCodeOrReparCompleteRsp =  AppleIDUtil.repareComplete(account,repareOptionsSecondRsp,questionRsp);
            }
        }

        HttpResponse tokenRsp = AppleIDUtil.token(account,securityCodeOrReparCompleteRsp);
        account.setScnt(tokenRsp.header("scnt"));
        account.setXAppleIDSessionId(tokenRsp.header("X-Apple-ID-Session-Id"));

        if (tokenRsp.getStatus() != 200){
            throwAndRefreshNote(account,"登录异常;");
        }
        setAndRefreshNote(account,"登录成功;",false);
        account.setIsLogin(true);
    }

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        onContentMenuClick(contextMenuEvent, new ArrayList<>(menuItem));
    }

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent,List<String> menuItemList) {
        super.onContentMenuClick(contextMenuEvent,accountTableView,menuItemList,new ArrayList<>());
    }

    public String getValidationErrors(String body){
        List errorMessageList = JSONUtil.parseObj(body).getByPath("validationErrors.message", List.class);
        if (CollUtil.isEmpty(errorMessageList)){
            return "";
        }
        return String.join(";",errorMessageList);
    }

}

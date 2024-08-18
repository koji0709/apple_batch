package com.sgswit.fx.controller.common;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.scene.input.ContextMenuEvent;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AppleIdView extends CustomTableView<Account> {


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }

    public HttpResponse signIn(Account account){
        // SignIn
        HttpResponse signInRsp = AppleIDUtil.signin(account);
        String status = "正常";
        String code="";
        if (!StrUtil.isEmpty(signInRsp.body())){
            String failMessage = "";
            JSONArray errorArr = JSONUtil.parseObj(signInRsp.body())
                    .getByPath("serviceErrors",JSONArray.class);
            if (errorArr != null && errorArr.size()>0){
                JSONObject err = (JSONObject)(errorArr.get(0));
                if ("-20209".equals(err.getStr("code"))){
                    status = "锁定";
                    code="-1";
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
            String message="Apple ID或密码不正确";
            if("-1".equals(code)){
                message="此账号已被锁定";
            }
            throw new ServiceException(message);
        }

        return signInRsp;
    }
//    /**
//     * appleid官网登录(不区分登录方式)
//     */
    public void login(Account account){
//        if (account.isLogin()){
//            return;
//        }

        HttpResponse securityCodeOrReparCompleteRsp = null;

        // 代表属于验证码再次执行
        if (!StrUtil.isEmpty(account.getSecurityCode())){
            Map<String, Object> authData = account.getAuthData();
            HttpResponse authRsp = (HttpResponse) authData.get("authRsp");
            if (authRsp == null){
                throw new ServiceException("请先执行程序;");
            }
            securityCodeOrReparCompleteRsp = AppleIDUtil.securityCode(account,authRsp);
            checkAndThrowUnavailableException(securityCodeOrReparCompleteRsp);
        }else{
            setAndRefreshNote(account,"正在验证账号密码...");
            HttpResponse signInRsp = signIn(account);
            if(signInRsp.getStatus()!=409){
                throw new ServiceException("Apple ID或密码不正确;");
            }
            // Auth
            HttpResponse authRsp = AppleIDUtil.auth(account,signInRsp);
            checkAndThrowUnavailableException(authRsp);

            String authType = JSONUtil.parse(signInRsp.body()).getByPath("authType",String.class);

            // 双重认证
            if ("hsa2".equals(authType)) {
                account.getAuthData().put("authRsp",authRsp);
                throw new ServiceException("此账号已开启双重认证;");
            } else { // sa 密保认证
                if (StrUtil.isEmpty(account.getAnswer1()) || StrUtil.isEmpty(account.getAnswer2()) || StrUtil.isEmpty(account.getAnswer3())){
                    throw new ServiceException("密保认证必须输入密保问题;");
                }
                // 密保认证
                setAndRefreshNote(account,"正在验证密保问题...");
                HttpResponse questionRsp = AppleIDUtil.questions(account,authRsp);
                if (questionRsp.getStatus() != 412) {
                    throw new ServiceException("密保问题验证失败;");
                }
                setAndRefreshNote(account,"密保问题验证通过");
                ThreadUtil.sleep(500);
                setAndRefreshNote(account,"正在阅读协议...");
                HttpResponse accountRepairRsp = AppleIDUtil.accountRepair(account,questionRsp);



                checkAndThrowUnavailableException(authRsp);

                String XAppleIDSessionId = "";
                String scnt = accountRepairRsp.header("scnt");

                for (String item : accountRepairRsp.headerList("Set-Cookie")) {
                    if (item.startsWith("aidsp")) {
                        XAppleIDSessionId = item.substring(item.indexOf("aidsp=") + 6, item.indexOf("; Domain=appleid.apple.com"));
                    }
                }
                setAndRefreshNote(account,"正在同意协议...");
                HttpResponse repareOptionsRsp = AppleIDUtil.repareOptions(account, questionRsp, accountRepairRsp);
                checkAndThrowUnavailableException(repareOptionsRsp);

                HttpResponse securityUpgradeRsp = AppleIDUtil.securityUpgrade(account,repareOptionsRsp,XAppleIDSessionId,scnt);
                checkAndThrowUnavailableException(securityUpgradeRsp);

                HttpResponse securityUpgradeSetuplaterRsp = AppleIDUtil.securityUpgradeSetuplater(account,securityUpgradeRsp,XAppleIDSessionId,scnt);
                checkAndThrowUnavailableException(securityUpgradeSetuplaterRsp);

                HttpResponse repareOptionsSecondRsp = AppleIDUtil.repareOptionsSecond(account,securityUpgradeSetuplaterRsp,XAppleIDSessionId,scnt);
                checkAndThrowUnavailableException(repareOptionsSecondRsp);

                securityCodeOrReparCompleteRsp =  AppleIDUtil.repareComplete(account,repareOptionsSecondRsp,questionRsp);
                checkAndThrowUnavailableException(securityCodeOrReparCompleteRsp);
            }
        }

        HttpResponse tokenRsp = AppleIDUtil.token(account,securityCodeOrReparCompleteRsp);
        checkAndThrowUnavailableException(tokenRsp);
        if (tokenRsp.getStatus() != 200){
            throw new PointDeduException(FunctionListEnum.ACCOUNT_INFO_MODIFY_INFOERR.getCode(), "登录异常;");
        }
        setAndRefreshNote(account,"登录成功;");
        account.setIsLogin(true);
    }

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        onContentMenuClick(contextMenuEvent, new ArrayList<>(menuItem));
    }

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent,List<String> menuItemList) {
        super.onContentMenuClick(contextMenuEvent,accountTableView,menuItemList,new ArrayList<>());
    }


}

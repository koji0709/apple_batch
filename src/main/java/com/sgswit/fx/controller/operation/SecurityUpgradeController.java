package com.sgswit.fx.controller.operation;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.operation.viewData.SecurityUpgradeView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.DataUtil;
import javafx.fxml.FXML;
import javafx.scene.input.ContextMenuEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 双重认证controller
 */
public class SecurityUpgradeController extends SecurityUpgradeView {

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction() {
        openImportAccountView(List.of("account----pwd-answer1-answer2-answer3-phone"));
    }

    /**
     * qewqeq@2980.com----dPFb6cSD415-宠物-工作-父母-17608177103
     */
    @Override
    public void accountHandler(Account account) {
        // 登陆
        login(account);

        String phone = account.getPhone();
        Object verifyCode = account.getAuthData().get("verifyCode");
        if (verifyCode == null){
            String body = "{\"acceptedWarnings\":[],\"phoneNumberVerification\":{\"phoneNumber\":{\"countryCode\":\"CN\",\"number\":\""+phone+"\",\"countryDialCode\":\"86\",\"nonFTEU\":true},\"mode\":\"sms\"}}";
            HttpResponse securityUpgradeVerifyPhoneRsp = AppleIDUtil.securityUpgradeVerifyPhone(account, body);

            if (securityUpgradeVerifyPhoneRsp.getStatus() == 503){
                throwAndRefreshNote(account,"操作频繁");
            }
            JSON jsonBody = JSONUtil.parse(securityUpgradeVerifyPhoneRsp.body());
            String areaCode = jsonBody.getByPath("phoneNumberVerification.phoneNumber.countryCode", String.class);
            account.setArea(DataUtil.getNameByCountryCode(areaCode));

            if (securityUpgradeVerifyPhoneRsp.getStatus() != 200){
                List meesageList = jsonBody.getByPath("phoneNumberVerification.serviceErrors.message", List.class);
                String message = String.join(",", meesageList);
                throwAndRefreshNote(account,message,"发送验证码失败");
            }

            account.getAuthData().put("securityUpgradeVerifyPhoneRsp",securityUpgradeVerifyPhoneRsp);
            setAndRefreshNote(account,"成功发送验证码，请输入验证码。");
        } else {
            Object securityUpgradeVerifyPhoneObject = account.getAuthData().get("securityUpgradeVerifyPhoneRsp");
            if (securityUpgradeVerifyPhoneObject == null){
                throwAndRefreshNote(account,"请先发送验证码");
            }
            HttpResponse securityUpgradeVerifyPhoneRsp = (HttpResponse) securityUpgradeVerifyPhoneObject;
            JSON jsonBody2 = JSONUtil.parse(securityUpgradeVerifyPhoneRsp.body());
            JSONObject phoneNumber = jsonBody2.getByPath("phoneNumberVerification.phoneNumber", JSONObject.class);

            String body2 = "{\"phoneNumberVerification\":{\"phoneNumber\":{\"id\":"+phoneNumber.getInt("id")+",\"number\":\""+phone+"\",\"countryCode\":\""+phoneNumber.getStr("countryCode")+"\",\"nonFTEU\":"+phoneNumber.getBool("nonFTEU")+"},\"securityCode\":{\"code\":\""+ verifyCode +"\"},\"mode\":\"sms\"}}";
            HttpResponse securityUpgradeRsp = AppleIDUtil.securityUpgrade(securityUpgradeVerifyPhoneRsp, body2);
            if (securityUpgradeRsp.getStatus() != 302){
                throwAndRefreshNote(account,"绑定双重认证失败");
            }
            setAndRefreshNote(account,"绑定双重认证成功");
        }
    }

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> menuItem =new ArrayList<>(){{
            add(Constant.RightContextMenu.DELETE.getCode());
            add(Constant.RightContextMenu.REEXECUTE.getCode());
            add(Constant.RightContextMenu.COPY.getCode());
            add(Constant.RightContextMenu.CODE.getCode());
        }};
        super.onContentMenuClick(contextMenuEvent,accountTableView,menuItem,new ArrayList<>());
    }

    @Override
    protected void reExecute(Account account) {
        accountHandler(account);
    }

    @Override
    protected void secondStepHandler(Account account, String code) {
        account.getAuthData().put("verifyCode",code);
        accountHandler(account);
    }
}

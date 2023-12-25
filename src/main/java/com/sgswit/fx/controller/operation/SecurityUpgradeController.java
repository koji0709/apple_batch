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

    @Override
    public void accountHandler(Account account) {
        if (!account.isLogin()){
            HttpResponse signInRsp = signIn(account);
            String authType = JSONUtil.parse(signInRsp.body()).getByPath("authType",String.class);
            if ("hsa2".equals(authType)){
                throwAndRefreshNote(account,"该账号已开通双重认证!");
            }
            // 登陆
            login(account);
        }

        String phone = account.getPhone();
        // todo 目前固定中国手机号码
        String body = "{\"acceptedWarnings\":[],\"phoneNumberVerification\":{\"phoneNumber\":{\"countryCode\":\"CN\",\"number\":\""+phone+"\",\"countryDialCode\":\"86\",\"nonFTEU\":true},\"mode\":\"sms\"}}";
        HttpResponse securityUpgradeVerifyPhoneRsp = AppleIDUtil.securityUpgradeVerifyPhone(account, body);
        if (securityUpgradeVerifyPhoneRsp.getStatus() == 503){
            setAndRefreshNote(account,"操作频繁");
            return;
        }
        if (securityUpgradeVerifyPhoneRsp.getStatus() != 200){
            setAndRefreshNote(account,"发送验证码失败");
            return;
        }

        String verifyCode = dialog("["+account.getAccount()+"] 手机验证码","请输入短信验证码：");
//        Console.log("请输入短信验证码：");
//        String verifyCode = Console.input();

        JSON jsonBody = JSONUtil.parse(securityUpgradeVerifyPhoneRsp.body());
        JSONObject phoneNumber = jsonBody.getByPath("phoneNumberVerification.phoneNumber", JSONObject.class);
        String body2 = "{\"phoneNumberVerification\":{\"phoneNumber\":{\"id\":"+phoneNumber.getInt("id")+",\"number\":\""+phone+"\",\"countryCode\":\""+phoneNumber.getStr("countryCode")+"\",\"nonFTEU\":"+phoneNumber.getBool("nonFTEU")+"},\"securityCode\":{\"code\":\""+verifyCode+"\"},\"mode\":\"sms\"}}";
        HttpResponse securityUpgradeRsp = AppleIDUtil.securityUpgrade(securityUpgradeVerifyPhoneRsp, body2);
        if (securityUpgradeRsp.getStatus() != 302){
            setAndRefreshNote(account,"绑定双重认证失败");
            return;
        }
        account.setArea(phoneNumber.getStr("countryCode"));
        setAndRefreshNote(account,"绑定双重认证成功");
    }

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> items=new ArrayList<>(){{
            add(Constant.RightContextMenu.DELETE.getCode());
            add(Constant.RightContextMenu.REEXECUTE.getCode());
            add(Constant.RightContextMenu.COPY.getCode());
            add(Constant.RightContextMenu.CODE.getCode());
        }};
        List<String> fields=new ArrayList<>();
        super.onContentMenuClick(contextMenuEvent,accountTableView,items,fields);
    }
}

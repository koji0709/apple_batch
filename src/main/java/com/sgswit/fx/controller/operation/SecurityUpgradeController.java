package com.sgswit.fx.controller.operation;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.operation.viewData.SecurityUpgradeView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * 双重认证controller
 */
public class SecurityUpgradeController extends SecurityUpgradeView {

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction() {
        openImportAccountView("account----pwd-answer1-answer2-answer3-phone");
    }

    @Override
    public void accountHandler(Account account) {
        // 登陆
        String scnt = loginAndGetScnt(account);
        if (StrUtil.isEmpty(scnt)){
            return;
        }

        String phone = account.getPhone();
        // todo 目前固定中国手机号码
        String body = "{\"acceptedWarnings\":[],\"phoneNumberVerification\":{\"phoneNumber\":{\"countryCode\":\"CN\",\"number\":\""+phone+"\",\"countryDialCode\":\"86\",\"nonFTEU\":true},\"mode\":\"sms\"}}";
        HttpResponse securityUpgradeVerifyPhoneRsp = AppleIDUtil.securityUpgradeVerifyPhone(scnt, account.getPwd(), body);
        if (securityUpgradeVerifyPhoneRsp.getStatus() != 200){
            setAndRefreshNote(account,"发送验证码失败");
            return;
        }

        String verifyCode = dialog("["+account.getAccount()+"] 手机验证码","请输入短信验证码：");
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


}

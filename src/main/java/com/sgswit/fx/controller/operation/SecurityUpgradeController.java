package com.sgswit.fx.controller.operation;

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
import java.util.ResourceBundle;

/**
 * 双重认证controller
 */
public class SecurityUpgradeController extends SecurityUpgradeView {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
    }

    @Override
    public void importAccountButtonAction() {
        super.importAccountButtonAction("account----pwd-answer1-answer2-answer3-phone");
    }

    /**
     * 开始执行按钮点击
     */
    public void executeButtonAction(){
        // 校验
        if (accountList.isEmpty()){
            alert("请先导入账号！");
            return;
        }

        for (Account account : accountList) {
            // 检测账号是否被处理过
            boolean processed = isProcessed(account);
            if (processed){
                continue;
            }

            String phone = account.getPhone();
            // todo 目前固定中国手机号码
            String body = "{\"acceptedWarnings\":[],\"phoneNumberVerification\":{\"phoneNumber\":{\"countryCode\":\"CN\",\"number\":\""+phone+"\",\"countryDialCode\":\"86\",\"nonFTEU\":true},\"mode\":\"sms\"}}";
            HttpResponse securityUpgradeVerifyPhoneRsp = AppleIDUtil.securityUpgradeVerifyPhone(getTokenScnt(account), account.getPwd(), body);
            if (securityUpgradeVerifyPhoneRsp.getStatus() != 200){
                account.setNote("发送验证码失败");
                continue;
            }

            String verifyCode = dialog("["+account.getAccount()+"] 手机验证码","请输入短信验证码：");
            JSON jsonBody = JSONUtil.parse(securityUpgradeVerifyPhoneRsp.body());
            JSONObject phoneNumber = jsonBody.getByPath("phoneNumberVerification.phoneNumber", JSONObject.class);
            String body2 = "{\"phoneNumberVerification\":{\"phoneNumber\":{\"id\":"+phoneNumber.getInt("id")+",\"number\":\""+phone+"\",\"countryCode\":\""+phoneNumber.getStr("countryCode")+"\",\"nonFTEU\":"+phoneNumber.getBool("nonFTEU")+"},\"securityCode\":{\"code\":\""+verifyCode+"\"},\"mode\":\"sms\"}}";
            HttpResponse securityUpgradeRsp = AppleIDUtil.securityUpgrade(securityUpgradeVerifyPhoneRsp, body2);
            if (securityUpgradeRsp.getStatus() != 302){
                account.setNote("绑定双重认证失败");
                continue;
            }
            account.setArea(phoneNumber.getStr("countryCode"));
            account.setNote("绑定双重认证成功");
        }
        this.refreshTableView();
    }



}

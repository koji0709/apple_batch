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

    /**
     * shabagga222@tutanota.com----Xx97595031..2-猪-狗-牛-17608177103
     */
    @Override
    public void importAccountButtonAction() {
        super.importAccountButtonAction("account----pwd-answer1-answer2-answer3-phone");
        super.bindActions();
    }

    @Override
    public TableCell<Account, Void> buildTableCell(){
        TableCell<Account,Void> cell = new TableCell<>() {
            private final Button btn1 = new Button("发送验证码且绑定");
            {
                btn1.setOnAction((ActionEvent event) -> {
                    Account account = getTableView().getItems().get(getIndex());
                    String phone = account.getPhone();
                    String body = "{\"acceptedWarnings\":[],\"phoneNumberVerification\":{\"phoneNumber\":{\"countryCode\":\"CN\",\"number\":\""+phone+"\",\"countryDialCode\":\"86\",\"nonFTEU\":true},\"mode\":\"sms\"}}";
                    HttpResponse securityUpgradeVerifyPhoneRsp = AppleIDUtil.securityUpgradeVerifyPhone(getTokenScnt(account), account.getPwd(), body);
                    if (securityUpgradeVerifyPhoneRsp.getStatus() != 200){
                        account.setNote("发送验证码失败");
                        refresh();
                        return;
                    }
                    String verifyCode = dialog("验证码","请输入短信验证码：");
                    JSON jsonBody = JSONUtil.parse(securityUpgradeVerifyPhoneRsp.body());
                    JSONObject phoneNumber = jsonBody.getByPath("phoneNumberVerification.phoneNumber", JSONObject.class);
                    String body2 = "{\"phoneNumberVerification\":{\"phoneNumber\":{\"id\":"+phoneNumber.getInt("id")+",\"number\":\""+phone+"\",\"countryCode\":\""+phoneNumber.getStr("countryCode")+"\",\"nonFTEU\":"+phoneNumber.getBool("nonFTEU")+"},\"securityCode\":{\"code\":\""+verifyCode+"\"},\"mode\":\"sms\"}}";
                    HttpResponse securityUpgradeRsp = AppleIDUtil.securityUpgrade(securityUpgradeVerifyPhoneRsp, body2);
                    if (securityUpgradeRsp.getStatus() != 302){
                        account.setNote("绑定双重认证失败");
                        refresh();
                        return;
                    }
                    account.setNote("绑定双重认证成功");
                    refresh();
                });
            }
            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn1);
                }
            }
        };
        return cell;
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
            account.setNote("开通双重认证只能单条处理");
        }

        accountTableView.refresh();
    }



}

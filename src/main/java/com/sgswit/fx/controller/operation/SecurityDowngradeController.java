package com.sgswit.fx.controller.operation;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.operation.viewData.SecurityDowngradeView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 关闭双重认证controller
 */
public class SecurityDowngradeController extends SecurityDowngradeView {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
    }

    /**
     * shabagga222@tutanota.com-猪-狗-牛-19960810
     */
    @Override
    public void importAccountButtonAction() {
        super.importAccountButtonAction("account-answer1-answer2-answer3-birthday");
        super.bindActions();
    }

    @Override
    public TableCell<Account, Void> buildTableCell(){
        TableCell<Account,Void> cell = new TableCell<>() {
            private final Button captBtn = new Button("输入验证码并执行");
            {
                captBtn.setOnAction((ActionEvent event) -> {
                    String newPassword = pwdTextField.getText();
                    if (StrUtil.isEmpty(newPassword)){
                        alert("必须填写新密码！");
                        return;
                    }

                    HttpResponse captchaRsp = AppleIDUtil.captcha();
                    JSON captchaRspJSON = JSONUtil.parse(captchaRsp.body());
                    String captBase64 = captchaRspJSON.getByPath("payload.content", String.class);

                    Integer captId     = captchaRspJSON.getByPath("id", Integer.class);
                    String  captToken  = captchaRspJSON.getByPath("token", String.class);
                    String  captAnswer = captchaDialog(captBase64);

                    Account account = getTableView().getItems().get(getIndex());
                    if (StrUtil.isEmpty(captAnswer)){
                        account.setNote("未输入验证码");
                        return;
                    }
                    String verifyAppleIdBody = "{\"id\":\"%s\",\"captcha\":{\"id\":%d,\"answer\":\"%s\",\"token\":\"%s\"}}";
                    verifyAppleIdBody = String.format(verifyAppleIdBody,account.getAccount(),captId,captAnswer,captToken);
                    HttpResponse verifyAppleIdRsp = AppleIDUtil.verifyAppleId(verifyAppleIdBody);
                    if (verifyAppleIdRsp.getStatus() == 302){
                        account.setNote("验证验证码成功");
                    }
                    HttpResponse securityDowngradeRsp = AppleIDUtil.securityDowngrade(verifyAppleIdRsp,account,newPassword);
                    if (securityDowngradeRsp.getStatus() == 302){
                        account.setNote("关闭双重验证成功");
                        account.setPwd(newPassword);
                    }else{
                        account.setNote("关闭双重验证失败");
                    }
                });
            }
            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(captBtn);
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

        String newPassword = pwdTextField.getText();
        if (StrUtil.isEmpty(newPassword)){
            alert("必须填写新密码！");
            return;
        }

        for (Account account : accountList) {
            account.setNote("请输入验证码并执行");
        }

        accountTableView.refresh();
    }
}

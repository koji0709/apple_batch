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

    @Override
    public void importAccountButtonAction() {
        super.importAccountButtonAction();
        this.bindButton();
    }

    /**
     * 绑定按钮
     * shabagga222@tutanota.com----Xx97595031.257-猪-狗-牛
     */
    public void bindButton(){
        Set<String> columnSet = accountTableView.getColumns().stream()
                .map(TableColumn::getText)
                .collect(Collectors.toSet());
        if (!columnSet.contains(ACTION_COLUMN_NAME)){
            TableColumn<Account, Void> colBtn = new TableColumn(ACTION_COLUMN_NAME);
            accountTableView.getColumns().add(colBtn);
            Callback<TableColumn<Account, Void>, TableCell<Account, Void>> cellFactory = params -> {
                final TableCell<Account, Void> cell = new TableCell<>() {
                    private final Button captBtn = new Button("输入验证码并执行");
                    {
                        captBtn.setOnAction((ActionEvent event) -> {
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
                            account.setBirthday("1996-08-10");
                            String verifyAppleIdBody = "{\"id\":\"%s\",\"captcha\":{\"id\":%d,\"answer\":\"%s\",\"token\":\"%s\"}}";
                            verifyAppleIdBody = String.format(verifyAppleIdBody,account.getAccount(),captId,captAnswer,captToken);
                            HttpResponse verifyAppleIdRsp = AppleIDUtil.verifyAppleId(verifyAppleIdBody);
                            HttpResponse securityDowngradeRsp = AppleIDUtil.securityDowngrade(verifyAppleIdRsp,account);
                            System.err.println(securityDowngradeRsp.getStatus());
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
            };
            colBtn.setCellFactory(cellFactory);
        }
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

        // todo

        accountTableView.refresh();
    }

    /**
     * 本地记录按钮点击
     */
    public void localHistoryButtonAction(){
        alert("本地记录按钮点击");
    }

    /**
     * 导出Excel按钮点击
     */
    public void exportExcelButtonAction(){
        alert("导出Excel按钮点击");
    }

    /**
     * 停止任务按钮点击
     */
    public void stopTaskButtonAction(){
        alert("停止任务按钮点击");
    }

}

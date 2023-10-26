package com.sgswit.fx.controller.operation;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import com.sgswit.fx.controller.operation.viewData.UpdateAppleIDView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;


/**
 * 官网修改资料controller
 */
public class UpdateAppleIdController extends UpdateAppleIDView {

    /**
     * 新邮箱(账号)或救援邮箱
     */
    @FXML
    private TableColumn email;

    @FXML
    private TableColumn popKey;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
        email.setCellValueFactory(new PropertyValueFactory<Account,Integer>("email"));
        popKey.setCellValueFactory(new PropertyValueFactory<Account,String>("popKey"));
        opTypeChoiceBox.setValue("更改AppleId");
    }

    @Override
    public void importAccountButtonAction() {
        super.importAccountButtonAction("account-pwd-answer1-answer2-answer3-email");
        super.bindActions();
    }

    @Override
    public TableCell<Account, Void> buildTableCell(){
        TableCell<Account,Void> cell = new TableCell<>() {
            private final Button captBtn = new Button("输入验证码并执行");
            {
                captBtn.setOnAction((ActionEvent event) -> {
                    String opType = opTypeChoiceBox.getValue().toString();
                    if ("更改AppleId".equals(opType)){
                        Account account = getTableView().getItems().get(getIndex());
//                        HttpResponse verifyRsp = AppleIDUtil
//                                .updateAppleIdSendVerifyCode(getTokenScnt(account), account.getPwd(), account.getEmail());
                        String verifyCode = dialog("验证码","请输入邮件验证码：");
//                        HttpResponse updateAppleIdRsp = AppleIDUtil.updateAppleId(verifyRsp, account.getEmail(), verifyCode);
//                        System.err.println(updateAppleIdRsp);
                        // todo
                    }
                    if ("新增救援邮件".equals(opType)){
                        // todo
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

        boolean updateAccountInfoCheckBoxSelected = updateAccountInfoCheckBox.isSelected();
        String opType = opTypeChoiceBox.getValue().toString();
        if ("更改AppleId".equals(opType)){
            for (Account account : accountList) {
                account.setNote("更新AppleId请输入验证码并执行");
            }
        }

        if ("新增救援邮件".equals(opType)){
            for (Account account : accountList) {
                account.setNote("新增救援邮件请输入验证码并执行");
            }
        }

        // 更改其他资料
        if (updateAccountInfoCheckBoxSelected){
            for (Account account : accountList) {
                LocalDate birthdayDatePickerValue = birthdayDatePicker.getValue();
                if (birthdayDatePickerValue != null){
                    HttpResponse updateBirthdayRsp = AppleIDUtil.updateBirthday(getTokenScnt(account), birthdayDatePickerValue.toString());
                    if (updateBirthdayRsp.getStatus() != 200){
                        account.setNote("修改生日失败;");
                    }else{
                        account.setBirthday(birthdayDatePickerValue.toString());
                    }
                }


                String newPwd = pwdTextField.getText();
                if (!StrUtil.isEmpty(newPwd)){
                    HttpResponse updatePasswordRsp = AppleIDUtil.updatePassword(getTokenScnt(account), account.getPwd(), newPwd);
                    if (updatePasswordRsp.getStatus() != 200){
                        account.setNote("修改密码失败;");
                    }else{
                        account.setPwd(newPwd);
                    }
                }

                String answer1TextFieldText = answer1TextField.getText();
                String answer2TextFieldText = answer2TextField.getText();
                String answer3TextFieldText = answer3TextField.getText();
                if (!StrUtil.isEmpty(answer1TextFieldText) && !StrUtil.isEmpty(answer2TextFieldText) && !StrUtil.isEmpty(answer3TextFieldText)){
                    String body = "{\"questions\":[{\"answer\":\"%s\",\"id\":\"%s\",\"question\":\"%s\"}" +
                            ",{\"answer\":\"%s\",\"id\":\"%s\",\"question\":\"%s\"}" +
                            ",{\"answer\":\"%s\",\"id\":\"%s\",\"question\":\"%s\"}]}";
                    body = String.format(body
                            ,answer1TextFieldText,130,"你少年时代最好的朋友叫什么名字？"
                            ,answer2TextFieldText,136,"你的理想工作是什么？"
                            ,answer3TextFieldText,142,"你的父母是在哪里认识的？");
                    HttpResponse updateQuestionsRsp = AppleIDUtil.updateQuestions(getTokenScnt(account), account.getPwd(), body);
                    if (updateQuestionsRsp.getStatus() != 200){
                        account.setNote("修改密保失败;");
                    }else{
                        account.setAnswer1(answer1TextFieldText);
                        account.setAnswer2(answer2TextFieldText);
                        account.setAnswer3(answer3TextFieldText);
                    }
                }

            }
        }
        accountTableView.refresh();
    }

}

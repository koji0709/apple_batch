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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class UpdateAppleIdController extends UpdateAppleIDView {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
        opTypeChoiceBox.setValue("更改AppleId");
    }

    /**
     * 导入账号按钮点击 kojitest001@outlook.com----Xx97595031-kojitest002@outlook.com
     */
    public void importAccountButtonAction() {
        openImportAccountView(List.of("account----pwd-email","account----pwd-answer1-answer2-answer3-email"));
    }

    @Override
    public void accountHandler(Account account) {
        HttpResponse loginRsp = login(account);
        if (loginRsp == null){
            return;
        }
        boolean updateAccountInfoCheckBoxSelected = updateAccountInfoCheckBox.isSelected();
        String opType = opTypeChoiceBox.getValue().toString();

        // 更改AppleId
        if ("更改AppleId".equals(opType)) {
            // 发送邮件
            HttpResponse verifyRsp = AppleIDUtil.updateAppleIdSendVerifyCode(account);

            // 更改AppleID
            String verifyCode = dialog("["+account.getEmail()+"] 邮箱验证码","请输入邮件验证码：");
            HttpResponse updateAppleIdRsp = AppleIDUtil.updateAppleId(verifyRsp, account.getEmail(), verifyCode);

            // 成功
            if (updateAppleIdRsp.getStatus() == 302){

            }
            System.err.println(updateAppleIdRsp);
            // todo 判断状态码
        }

        // 新增救援邮件
        if ("新增救援邮件".equals(opType)){
            // 发送邮件
            HttpResponse verifyRsp = AppleIDUtil.addRescueEmailSendVerifyCode(account);
            if (verifyRsp.getStatus() == 201){
                String verifyCode = dialog("["+account.getAccount()+"] 邮箱验证码","请输入邮件验证码：");
                HttpResponse addRescueEmailRsp = AppleIDUtil.addRescueEmail(verifyRsp, account.getEmail(), verifyCode);
                if (addRescueEmailRsp.getStatus() == 200){
                    setAndRefreshNote(account,"新增救援邮箱成功");
                }else{
                    // todo 看是否有具体错误信息
                    setAndRefreshNote(account,"新增救援邮箱失败");
                }
            }else{
                setAndRefreshNote(account,"救援邮箱发送邮件失败");
            }
        }

        // 更改其他资料
        if (updateAccountInfoCheckBoxSelected){
            LocalDate birthdayDatePickerValue = birthdayDatePicker.getValue();
            if (birthdayDatePickerValue != null){
                HttpResponse updateBirthdayRsp = AppleIDUtil.updateBirthday(account, birthdayDatePickerValue.toString());
                if (updateBirthdayRsp.getStatus() != 200){
                    setAndRefreshNote(account,"修改生日失败");
                }else{
                    account.setBirthday(birthdayDatePickerValue.toString());
                    setAndRefreshNote(account,"修改生日成功");
                }
            }


            String newPwd = pwdTextField.getText();
            if (!StrUtil.isEmpty(newPwd)){
                HttpResponse updatePasswordRsp = AppleIDUtil.updatePassword(account, account.getPwd(), newPwd);
                if (updatePasswordRsp.getStatus() != 200){
                    setAndRefreshNote(account,"修改密码失败");
                }else{
                    account.setPwd(newPwd);
                    setAndRefreshNote(account,"修改密码成功");
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
                HttpResponse updateQuestionsRsp = AppleIDUtil.updateQuestions(account, body);
                if (updateQuestionsRsp.getStatus() != 200){
                    setAndRefreshNote(account,"修改密保失败");
                }else{
                    account.setAnswer1(answer1TextFieldText);
                    account.setAnswer2(answer2TextFieldText);
                    account.setAnswer3(answer3TextFieldText);
                    setAndRefreshNote(account,"修改密保成功");
                }
            }
        }

    }
}

package com.sgswit.fx.controller.operation;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.operation.viewData.UpdateAppleIDView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.AuthData;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;


public class UpdateAppleIdController extends UpdateAppleIDView {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
        opTypeChoiceBox.setValue("更改AppleId");
    }

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction() {
        openImportAccountView(List.of("account----pwd-email","account----pwd-answer1-answer2-answer3-email"));
    }

    @Override
    public void accountHandler(Account account) {
        login(account);

        // true = 更改AppleId, false = 新增救援邮箱
        boolean isUpdateAppleId = "更改AppleId".equals(opTypeChoiceBox.getValue().toString());

        Map<String, Object> authData = account.getAuthData();
        Object verifyCode = authData.get("verifyCode");
        if (verifyCode == null){
            if (account.getAccount().equals(account.getEmail())){
                throwAndRefreshNote(account,"新账号或救援邮箱不能和当前账号使用同一个邮箱");
            }
            // 发送邮件
            HttpResponse verifyRsp = isUpdateAppleId ? AppleIDUtil.updateAppleIdSendVerifyCode(account)
                                        :  AppleIDUtil.addRescueEmailSendVerifyCode(account);
            if (isUpdateAppleId){
                if (verifyRsp.getStatus() != 200){
                    throwAndRefreshNote(account,getValidationErrors(verifyRsp.body()),"发送邮件失败");
                }
            }else{
                if (verifyRsp.getStatus() != 201){
                    throwAndRefreshNote(account,getValidationErrors(verifyRsp.body()),"发送邮件失败");
                }
            }
            account.getAuthData().put("verifyRsp",verifyRsp);
            setAndRefreshNote(account,"成功发送验证码，请输入验证码。");
        }else{
            // 更改AppleID
            Object verifyRsp = authData.get("verifyRsp");
            if (verifyRsp == null){
                setAndRefreshNote(account,"请先开始执行",false);
                return;
            }
            HttpResponse updateRsp = isUpdateAppleId ? AppleIDUtil.updateAppleId((HttpResponse) verifyRsp, account, verifyCode.toString())
                                            : AppleIDUtil.addRescueEmail((HttpResponse)verifyRsp,account, verifyCode.toString());

            if (updateRsp.getStatus() == 200){
                if (isUpdateAppleId){
                    account.setAccount(account.getEmail());
                    setAndRefreshNote(account,"修改账号成功");
                }else{
                    setAndRefreshNote(account,"新增救援邮箱成功");
                }
            }else{
                setAndRefreshNote(account,failMessage(updateRsp),"修改账号失败");
            }
        }

        // 更改其他资料
        boolean updateAccountInfoCheckBoxSelected = updateAccountInfoCheckBox.isSelected();
        if (updateAccountInfoCheckBoxSelected){
            LocalDate birthdayDatePickerValue = birthdayDatePicker.getValue();
            if (birthdayDatePickerValue != null){
                HttpResponse updateBirthdayRsp = AppleIDUtil.updateBirthday(account, birthdayDatePickerValue.toString());
                if (updateBirthdayRsp.getStatus() != 200){
                    appendAndRefreshNote(account,"修改生日失败");
                }else{
                    account.setBirthday(birthdayDatePickerValue.toString());
                    appendAndRefreshNote(account,"修改生日成功");
                }
            }


            String newPwd = pwdTextField.getText();
            if (!StrUtil.isEmpty(newPwd)){
                HttpResponse updatePasswordRsp = AppleIDUtil.updatePassword(account, account.getPwd(), newPwd);
                if (updatePasswordRsp.getStatus() != 200){
                    appendAndRefreshNote(account,"修改密码失败");
                }else{
                    account.setPwd(newPwd);
                    appendAndRefreshNote(account,"修改密码成功");
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
                    appendAndRefreshNote(account,"修改密保失败");
                }else{
                    account.setAnswer1(answer1TextFieldText);
                    account.setAnswer2(answer2TextFieldText);
                    account.setAnswer3(answer3TextFieldText);
                    appendAndRefreshNote(account,"修改密保成功");
                }
            }
            setAndRefreshNote(account,account.getNote());
        }

    }

    @Override
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> menuItem =new ArrayList<>(){{
            add(Constant.RightContextMenu.DELETE.getCode());
            add(Constant.RightContextMenu.REEXECUTE.getCode());
            add(Constant.RightContextMenu.COPY.getCode());
            add(Constant.RightContextMenu.WEB_TWO_FACTOR_CODE.getCode());
            add(Constant.RightContextMenu.CODE.getCode());
        }};
        super.onContentMenuClick(contextMenuEvent,accountTableView,menuItem,new ArrayList<>());
    }

    @Override
    protected void reExecute(Account account) {
        ThreadUtil.execute(() -> {
            try {
                setAndRefreshNote(account, "执行中", false);
                accountHandler(account);
            } catch (ServiceException e) {
                // 异常不做处理只是做一个停止程序作用
            } catch (Exception e) {
                setAndRefreshNote(account, "数据处理异常", true);
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void secondStepHandler(Account account, String code) {
        ThreadUtil.execute(() -> {
            try {
                account.getAuthData().put("verifyCode",code);
                accountHandler(account);
            } catch (ServiceException e) {
                // 异常不做处理只是做一个停止程序作用
            } catch (Exception e) {
                setAndRefreshNote(account, "数据处理异常", true);
                e.printStackTrace();
            }
        });
    }

}

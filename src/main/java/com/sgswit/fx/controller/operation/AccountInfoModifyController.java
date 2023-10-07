package com.sgswit.fx.controller.operation;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import com.github.javafaker.Faker;
import com.sgswit.fx.AppleIDTest;
import com.sgswit.fx.controller.operation.viewData.AccountInfoModifyView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.NbUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;


/**
 * 官网修改资料controller
 */
public class AccountInfoModifyController extends AccountInfoModifyView {

    private ObservableList<Account> accountList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
        initViewData();
        refreshList();
    }

    /**
     * 初始化视图数据
     */
    public void initViewData(){
        List<List<String>> questionList = NbUtil.getQuestionList();
        question1ChoiceBox.setItems(FXCollections.observableArrayList(questionList.get(0)));
        question2ChoiceBox.setItems(FXCollections.observableArrayList(questionList.get(1)));
        question3ChoiceBox.setItems(FXCollections.observableArrayList(questionList.get(2)));

        List<String> languageList = NbUtil.getLanguageList();
        updateShowLangChoiceBox.setItems(FXCollections.observableArrayList(languageList));

        nameGenerationTypeChoiceBox.setValue("固定姓名");
    }

    /***************************************** 业务方法 *************************************/

    /**
     * 刷新页面数据
     */
    public void refreshList(){
        // 清空数据
        accountList.clear();

        // 初始化表格数据
        List<Account> list = NbUtil.getAccountList();

        if (!CollectionUtil.isEmpty(list)){
            accountList.addAll(list);
        }
        if (!CollectionUtil.isEmpty(list)){
            for (int i = 0; i < accountList.size(); i++) {
                Account account = accountList.get(i);
                account.setSeq(i+1);
            }
        }
        tableViewDataList.setItems(accountList);
        accountNumLable.setText(accountList.size()+"");
    }

    /**
     * 清空账号列表
     */
    public void clearList(){
        accountList.clear();
    }

    /***************************************** 操作 *************************************/

    /**
     * 开始执行按钮点击
     */
    public void executeButtonAction(){
        boolean tryAgainCheckBoxSelected = tryAgainCheckBox.isSelected();
        boolean randomIPProxyCheckBoxSelected = randomIPProxyCheckBox.isSelected();

        boolean updatePwdCheckBoxSelected = updatePwdCheckBox.isSelected();
        boolean updateBirthdayCheckBoxSelected = updateBirthdayCheckBox.isSelected();
        boolean updateNameCheckBoxSelected = updateNameCheckBox.isSelected();
        boolean updatePasswordProtectionCheckBoxSelected = updatePasswordProtectionCheckBox.isSelected();
        boolean removeDeviceCheckBoxSelected = removeDeviceCheckBox.isSelected();
        boolean removeRescueEmailCheckBoxSelected = removeRescueEmailCheckBox.isSelected();

        // 校验
        if (accountList.isEmpty()){
            alert("请先导入账号！");
            return;
        }
        // 修改密码
        if (updatePwdCheckBoxSelected){
            String newPwd = pwdTextField.getText();
            if (StrUtil.isEmpty(newPwd)){
                alert("新密码不能为空！");
                return;
            }
        }
        // 修改生日
        if (updateBirthdayCheckBoxSelected){
            LocalDate birthdayDatePickerValue = birthdayDatePicker.getValue();
            if (birthdayDatePickerValue == null){
                alert("新生日不能为空！");
                return;
            }
        }
        // 修改姓名
        if (updateNameCheckBoxSelected){
            String firstName = firstNameTextField.getText();
            String lastName = lastNameTextField.getText();
            Object nameGenerationTypeChoiceBoxValue = nameGenerationTypeChoiceBox.getValue();
            if ((StrUtil.isEmpty(firstName) || StrUtil.isEmpty(lastName)) && "固定姓名".equals(nameGenerationTypeChoiceBoxValue)){
                alert("姓名不能为空！");
                return;
            }
        }
        // 修改密保
        if (updatePasswordProtectionCheckBoxSelected){
            Object question1ChoiceBoxValue = question1ChoiceBox.getValue();
            Object question2ChoiceBoxValue = question2ChoiceBox.getValue();
            Object question3ChoiceBoxValue = question3ChoiceBox.getValue();
            String answer1TextFieldText = answer1TextField.getText();
            String answer2TextFieldText = answer2TextField.getText();
            String answer3TextFieldText = answer3TextField.getText();
            if (question1ChoiceBoxValue == null || question2ChoiceBoxValue == null || question3ChoiceBoxValue == null
                    || StrUtil.isEmpty(answer1TextFieldText) || StrUtil.isEmpty(answer2TextFieldText) || StrUtil.isEmpty(answer3TextFieldText)){
                alert("请填写完整密保信息");
                return;
            }
        }

        for (Account account : accountList) {
            // 修改密码
            if (updatePwdCheckBoxSelected){
                String newPwd = pwdTextField.getText();
                HttpResponse updatePasswordRsp = AppleIDUtil.updatePassword(getTokenScnt(account), account.getPwd(), newPwd);
                if (updatePasswordRsp.getStatus() != 200){
                    account.appendNote("修改密码失败;");
                }else{
                    account.setPwd(newPwd);
                }
            }

            // 修改生日
            if (updateBirthdayCheckBoxSelected){
                LocalDate birthdayDatePickerValue = birthdayDatePicker.getValue();
                HttpResponse updateBirthdayRsp = AppleIDUtil.updateBirthday(getTokenScnt(account), birthdayDatePickerValue.toString());
                if (updateBirthdayRsp.getStatus() != 200){
                    account.appendNote("修改生日失败;");
                }else{
                    account.setBirthday(birthdayDatePickerValue.toString());
                }
            }

            // 修改姓名
            if (updateNameCheckBoxSelected){
                String firstName = firstNameTextField.getText();
                String lastName = lastNameTextField.getText();
                Object nameGenerationTypeChoiceBoxValue = nameGenerationTypeChoiceBox.getValue();
                if ("随机中文".equals(nameGenerationTypeChoiceBoxValue)){
                    Faker faker = new Faker(Locale.CHINA);
                    firstName = faker.name().firstName();
                    lastName  = faker.name().lastName();
                }
                if ("随机英文".equals(nameGenerationTypeChoiceBoxValue)){
                    Faker faker = new Faker();
                    firstName = faker.name().firstName();
                    lastName  = faker.name().lastName();
                }

                HttpResponse updateNameRsp = AppleIDUtil.updateName(getTokenScnt(account), account.getPwd(), firstName, lastName);
                if (updateNameRsp.getStatus() != 200){
                    account.appendNote("姓名修改失败;");
                }else{
                    account.setName(firstName + lastName);
                }
            }

            // 修改密保
            if (updatePasswordProtectionCheckBoxSelected){
                Object question1ChoiceBoxValue = question1ChoiceBox.getValue();
                Object question2ChoiceBoxValue = question2ChoiceBox.getValue();
                Object question3ChoiceBoxValue = question3ChoiceBox.getValue();
                String answer1TextFieldText = answer1TextField.getText();
                String answer2TextFieldText = answer2TextField.getText();
                String answer3TextFieldText = answer3TextField.getText();

                String body = "{\"questions\":[{\"answer\":\"%s\",\"id\":\"%s\",\"question\":\"%s\"}" +
                                            ",{\"answer\":\"%s\",\"id\":\"%s\",\"question\":\"%s\"}" +
                                            ",{\"answer\":\"%s\",\"id\":\"%s\",\"question\":\"%s\"}]}";
                LinkedHashMap<String, Integer> questionMap = NbUtil.getQuestionMap();

                body = String.format(body
                            ,answer1TextFieldText,questionMap.get(question1ChoiceBoxValue.toString()),question1ChoiceBoxValue
                            ,answer2TextFieldText,questionMap.get(question2ChoiceBoxValue.toString()),question2ChoiceBoxValue
                            ,answer3TextFieldText,questionMap.get(question3ChoiceBoxValue.toString()),question3ChoiceBoxValue);
                AppleIDUtil.updateQuestions(getTokenScnt(account),account.getPwd(),body);
            }

            // 移除设备
            if (removeDeviceCheckBoxSelected){
                getTokenScnt(account);
                AppleIDUtil.removeDevices();
            }
            // 移除救援邮箱
            if (removeRescueEmailCheckBoxSelected){
                HttpResponse deleteRescueEmailRsp = AppleIDUtil.deleteRescueEmail(getTokenScnt(account), account.getPwd());
                if (deleteRescueEmailRsp.getStatus() != 204){
                    account.appendNote("移除救援邮箱失败;");
                }
            }

        }
        tableViewDataList.refresh();
    }

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction(){
        refreshList();
    }

    /**
     * 清空列表按钮点击
     */
    public void clearAccountListButtonAction(){
        clearList();
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

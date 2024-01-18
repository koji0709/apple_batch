package com.sgswit.fx.controller.operation;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.javafaker.Faker;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.operation.viewData.AccountInfoModifyView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.DataUtil;
import com.sgswit.fx.utils.PointUtil;
import javafx.collections.FXCollections;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;


/**
 * 官网修改资料controller
 */
public class AccountInfoModifyController extends AccountInfoModifyView {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.ACCOUNT_INFO_MODIFY.getCode())));
        super.initialize(url,resourceBundle);
        initViewData();
        menuItem.add(Constant.RightContextMenu.WEB_TWO_FACTOR_CODE.getCode());
    }

    /**
     * 初始化视图数据
     */
    public void initViewData(){
        List<List<String>> questionList = DataUtil.getQuestionList();
        question1ChoiceBox.setItems(FXCollections.observableArrayList(questionList.get(0)));
        question2ChoiceBox.setItems(FXCollections.observableArrayList(questionList.get(1)));
        question3ChoiceBox.setItems(FXCollections.observableArrayList(questionList.get(2)));

        List<String> languageList = DataUtil.getLanguageList();
        updateShowLangChoiceBox.setItems(FXCollections.observableArrayList(languageList));

        nameGenerationTypeChoiceBox.setValue("固定姓名");
    }

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction() {
        openImportAccountView(List.of("account----pwd","account----pwd-answer1-answer2-answer3"));
    }

    @Override
    public boolean executeButtonActionBefore() {
        boolean updatePwdCheckBoxSelected = updatePwdCheckBox.isSelected();
        boolean updateBirthdayCheckBoxSelected = updateBirthdayCheckBox.isSelected();
        boolean updateNameCheckBoxSelected = updateNameCheckBox.isSelected();
        boolean updatePasswordProtectionCheckBoxSelected = updatePasswordProtectionCheckBox.isSelected();
        boolean updateShowLangCheckBoxSelected = updateShowLangCheckBox.isSelected();

        // 修改密码
        if (updatePwdCheckBoxSelected){
            String newPwd = pwdTextField.getText();
            if (StrUtil.isEmpty(newPwd)){
                alert("新密码不能为空！");
                return false;
            }
        }
        // 修改生日
        if (updateBirthdayCheckBoxSelected){
            LocalDate birthdayDatePickerValue = birthdayDatePicker.getValue();
            if (birthdayDatePickerValue == null){
                alert("新生日不能为空！");
                return false;
            }
        }
        // 修改姓名
        if (updateNameCheckBoxSelected){
            String firstName = firstNameTextField.getText();
            String lastName = lastNameTextField.getText();
            Object nameGenerationTypeChoiceBoxValue = nameGenerationTypeChoiceBox.getValue();
            if ((StrUtil.isEmpty(firstName) || StrUtil.isEmpty(lastName)) && "固定姓名".equals(nameGenerationTypeChoiceBoxValue)){
                alert("姓名不能为空！");
                return false;
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
                return false;
            }
        }
        // 修改显示语言
        if (updateShowLangCheckBoxSelected){
            Object value = updateShowLangChoiceBox.getValue();
            if (value == null){
                alert("请选择显示语言");
                return false;
            }
        }
        return true;
    }

    @Override
    public void accountHandler(Account account) {
        boolean updatePwdCheckBoxSelected = updatePwdCheckBox.isSelected();
        boolean updateBirthdayCheckBoxSelected = updateBirthdayCheckBox.isSelected();
        boolean updateNameCheckBoxSelected = updateNameCheckBox.isSelected();
        boolean updatePasswordProtectionCheckBoxSelected = updatePasswordProtectionCheckBox.isSelected();
        boolean removeDeviceCheckBoxSelected = removeDeviceCheckBox.isSelected();
        boolean removeRescueEmailCheckBoxSelected = removeRescueEmailCheckBox.isSelected();
        boolean updateShowLangCheckBoxSelected = updateShowLangCheckBox.isSelected();

        // 登录账号
        login(account);
        // 清空之前的信息
        setAndRefreshNote(account,"",false);

        HttpResponse accountRsp = AppleIDUtil.account(account);
        JSON accountJSON = JSONUtil.parse(accountRsp.body());
        account.setArea(accountJSON.getByPath("account.person.primaryAddress.countryName",String.class));
        account.setBirthday(accountJSON.getByPath("account.person.birthday",String.class));
        account.setName(accountJSON.getByPath("name.fullName",String.class));

        // 修改密码
        if (updatePwdCheckBoxSelected){
            String newPwd = pwdTextField.getText();
            HttpResponse updatePasswordRsp = AppleIDUtil.updatePassword(account, account.getPwd(), newPwd);
            if (updatePasswordRsp.getStatus() != 200){
                appendAndRefreshNote(account,getValidationErrors(updatePasswordRsp.body()),"修改密码失败");
            }else{
                account.setPwd(newPwd);
                appendAndRefreshNote(account,"修改密码成功");
            }
        }

        // 修改生日
        if (updateBirthdayCheckBoxSelected){
            LocalDate birthdayDatePickerValue = birthdayDatePicker.getValue();
            HttpResponse updateBirthdayRsp = AppleIDUtil.updateBirthday(account, birthdayDatePickerValue.toString());
            if (updateBirthdayRsp.getStatus() != 200){
                appendAndRefreshNote(account,getValidationErrors(updateBirthdayRsp.body()),"修改生日失败");
            }else{
                account.setBirthday(birthdayDatePickerValue.toString());
                appendAndRefreshNote(account,"修改生日成功");
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

            HttpResponse updateNameRsp = AppleIDUtil.updateName(account, account.getPwd(), firstName, lastName);
            if (updateNameRsp.getStatus() != 200){
                appendAndRefreshNote(account,getValidationErrors(updateNameRsp.body()),"修改姓名失败");
            }else{
                account.setName(firstName + lastName);
                appendAndRefreshNote(account,"姓名修改成功");
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
            LinkedHashMap<String, Integer> questionMap = DataUtil.getQuestionMap();

            body = String.format(body
                    ,answer1TextFieldText,questionMap.get(question1ChoiceBoxValue.toString()),question1ChoiceBoxValue
                    ,answer2TextFieldText,questionMap.get(question2ChoiceBoxValue.toString()),question2ChoiceBoxValue
                    ,answer3TextFieldText,questionMap.get(question3ChoiceBoxValue.toString()),question3ChoiceBoxValue);
            HttpResponse updateQuestionsRsp = AppleIDUtil.updateQuestions(account, body);
            if (updateQuestionsRsp.getStatus() != 200){
                appendAndRefreshNote(account,getValidationErrors(updateQuestionsRsp.body()),"修改密保失败");
            }else{
                account.setAnswer1(answer1TextFieldText);
                account.setAnswer2(answer2TextFieldText);
                account.setAnswer3(answer3TextFieldText);
                appendAndRefreshNote(account,"修改密保成功");
            }
        }

        // 移除设备
        if (removeDeviceCheckBoxSelected){
            HttpResponse deviceListRsp = AppleIDUtil.getDeviceList(account);
            String body = deviceListRsp.body();
            JSONObject bodyJSON = JSONUtil.parseObj(body);
            List<String> deviceIdList = bodyJSON.getByPath("devices.id", List.class);

            if (CollUtil.isEmpty(deviceIdList)){
                appendAndRefreshNote(account,"该账号下暂无设备");
            }else{
                AppleIDUtil.removeDevices(deviceListRsp);
                appendAndRefreshNote(account,"移除设备成功");
            }
        }

        // 移除救援邮箱
        if (removeRescueEmailCheckBoxSelected){
            HttpResponse deleteRescueEmailRsp = AppleIDUtil.deleteRescueEmail(account);
            if (deleteRescueEmailRsp.getStatus() != 204){
                appendAndRefreshNote(account,getValidationErrors(deleteRescueEmailRsp.body()),"移除救援邮箱失败");
            }else{
                appendAndRefreshNote(account,"移除救援邮箱成功");
            }
        }

        // 修改显示语言
        if (updateShowLangCheckBoxSelected){
            Object showLang = updateShowLangChoiceBox.getValue();
            LinkedHashMap<String, String> languageMap = DataUtil.getLanguageMap();
            String langCode = languageMap.get(showLang);
            HttpResponse changeShowLanguageRsp = AppleIDUtil.changeShowLanguage(account,langCode);
            if (changeShowLanguageRsp.getStatus() != 200){
                appendAndRefreshNote(account,getValidationErrors(changeShowLanguageRsp.body()),"修改显示语言失败");
            }else{
                appendAndRefreshNote(account,"修改显示语言成功");
            }
        }

        setAndRefreshNote(account, account.getNote());
    }

    @Override
    protected void reExecute(Account account) {
        accountHandlerExpand(account);
    }



}

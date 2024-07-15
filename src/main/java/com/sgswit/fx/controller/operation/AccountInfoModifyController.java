package com.sgswit.fx.controller.operation;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.javafaker.Faker;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.operation.viewData.AccountInfoModifyView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.*;
import com.sgswit.fx.utils.proxy.ProxyUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;


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
        updateShowLangComboBox.setItems(FXCollections.observableArrayList(languageList));

        nameGenerationTypeChoiceBox.setValue("固定姓名");
        //读取保存的数据
        //新密码
        String newPassword= PropertiesUtil.getOtherConfig("accountInfoModify.newPassword","");
        pwdTextField.setText(newPassword);
        //新生日
        String birthday= PropertiesUtil.getOtherConfig("accountInfoModify.birthday","");
        birthdayTextField.setText(birthday);
        //姓氏
        String lastName= PropertiesUtil.getOtherConfig("accountInfoModify.lastName","");
        lastNameTextField.setText(lastName);
        String firstName= PropertiesUtil.getOtherConfig("accountInfoModify.firstName","");
        firstNameTextField.setText(lastName);
        getQuestionsAndAnswerFromLocation();
    }
    @Override
    public void closeStageActionBefore(){
        setQuestionsAndAnswerToLocation();
        //密码
        PropertiesUtil.setOtherConfig("accountInfoModify.newPassword",pwdTextField.getText());
        //生日
        PropertiesUtil.setOtherConfig("accountInfoModify.birthday",birthdayTextField.getText());
        //姓名
        PropertiesUtil.setOtherConfig("accountInfoModify.lastName",lastNameTextField.getText());
        PropertiesUtil.setOtherConfig("accountInfoModify.firstName",firstNameTextField.getText());
    }
    private void setQuestionsAndAnswerToLocation(){
        List<String> questions= new ArrayList<>();
        String question1ChoiceBoxValue = CustomStringUtils.isEmpty(question1ChoiceBox.getValue())?"":question1ChoiceBox.getValue().toString();
        String question2ChoiceBoxValue = CustomStringUtils.isEmpty(question2ChoiceBox.getValue())?"":question2ChoiceBox.getValue().toString();
        String question3ChoiceBoxValue = CustomStringUtils.isEmpty(question3ChoiceBox.getValue())?"":question3ChoiceBox.getValue().toString();
        String answer1TextFieldText = answer1TextField.getText();
        String answer2TextFieldText = answer2TextField.getText();
        String answer3TextFieldText = answer3TextField.getText();
        questions.add(question1ChoiceBoxValue+"|"+answer1TextFieldText);
        questions.add(question2ChoiceBoxValue+"|"+answer2TextFieldText);
        questions.add(question3ChoiceBoxValue+"|"+answer3TextFieldText);
        String questionsAndAnswer = questions.stream().collect(Collectors.joining("----", "", ""));
        PropertiesUtil.setOtherConfig("accountInfoModify.questionsAndAnswer",questionsAndAnswer);
    }
    private void getQuestionsAndAnswerFromLocation(){
        String questionsAndAnswer=PropertiesUtil.getOtherConfig("accountInfoModify.questionsAndAnswer","");
        if (StrUtil.isEmpty(questionsAndAnswer)){
            return;
        }
        String[] arr=questionsAndAnswer.split("----");

        String[] q1=arr[0].split("\\|",-1);
        answer1TextField.setText(q1[1]);
        question1ChoiceBox.getSelectionModel().select(q1[0]);

        String[] q2=arr[1].split("\\|",-1);
        answer2TextField.setText(q2[1]);
        question2ChoiceBox.getSelectionModel().select(q2[0]);

        String[] q3=arr[2].split("\\|",-1);
        answer3TextField.setText(q3[1]);
        question3ChoiceBox.getSelectionModel().select(q3[0]);
    }
    /**
     * 设置每一行执行的间隔频率
     */
    @Override
    public Long getIntervalFrequency() {
        return 1000L;
    }

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction(ActionEvent actionEvent) {
        openImportAccountView(List.of("account----pwd","account----pwd-answer1-answer2-answer3"),actionEvent);
    }

    @Override
    public boolean executeButtonActionBefore() {
        boolean updatePwdCheckBoxSelected = updatePwdCheckBox.isSelected();
        boolean updateBirthdayCheckBoxSelected = updateBirthdayCheckBox.isSelected();
        boolean updateNameCheckBoxSelected = updateNameCheckBox.isSelected();
        boolean updatePasswordProtectionCheckBoxSelected = updatePasswordProtectionCheckBox.isSelected();
        boolean removeDeviceCheckBoxSelected = removeDeviceCheckBox.isSelected();
        boolean removeRescueEmailCheckBoxSelected = removeRescueEmailCheckBox.isSelected();
        boolean updateShowLangCheckBoxSelected = updateShowLangCheckBox.isSelected();


        if (!(updatePwdCheckBoxSelected || updateBirthdayCheckBoxSelected  || updateNameCheckBoxSelected || updatePasswordProtectionCheckBoxSelected
                || updatePasswordProtectionCheckBoxSelected || removeDeviceCheckBoxSelected || removeRescueEmailCheckBoxSelected || updateShowLangCheckBoxSelected)){
            alert("至少选择一项修改信息！");
            return false;
        }

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
            String birthdayTextFieldText = birthdayTextField.getText();
            if (StringUtils.isEmpty(birthdayTextFieldText)){
                alert("新生日不能为空！");
                return false;
            }
            try{
                DateUtil.parse(birthdayTextFieldText);
            }catch (Exception e){
                alert("生日格式不正确！");
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
            Object value = updateShowLangComboBox.getValue();
            if (value == null){
                alert("请选择显示语言");
                return false;
            }
        }
        return true;
    }

    /**
     * qewqeq@2980.com----Ac223388-宠物-工作-父母
     * shabagga222@tutanota.com----Ac223388-宠物-工作-父母
     */
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
        setAndRefreshNote(account,"正在读取用户信息...");
        HttpResponse accountRsp = AppleIDUtil.account(account);
        checkAndThrowUnavailableException(accountRsp);
        JSON accountJson = JSONUtil.parse(accountRsp.body());
        account.setArea(accountJson.getByPath("account.person.primaryAddress.countryName",String.class));
        account.setBirthday(accountJson.getByPath("account.person.birthday",String.class));
        account.setName(accountJson.getByPath("name.fullName",String.class));
        account.setRescueEmail(accountJson.getByPath("account.security.rescueEmail",String.class));
        String createdDate = accountJson.getByPath("account.person.reachableAtOptions.primaryEmailAddress.createdDate", String.class);
        String updateDate = accountJson.getByPath("account.person.reachableAtOptions.primaryEmailAddress.updateDate", String.class);
        account.setCreatedDate(DateUtil.format(new Date(Long.valueOf(createdDate.substring(0,10))*1000),"yyyy年MM月dd日"));
        account.setUpdateDate(DateUtil.format(new Date(Long.valueOf(updateDate.substring(0,10))*1000),"yyyy年MM月dd日"));
        account.setNote("查询成功");
        super.accountTableView.refresh();
        Map<String,String> messageMap= new LinkedHashMap<>();
        // 修改密码
        if (updatePwdCheckBoxSelected){
            setMessageAndRefreshTable("updatePwd","正在修改密码...",messageMap,account);
            String newPwd = pwdTextField.getText();
            if(StringUtils.isEmpty(newPwd)){
                setMessageAndRefreshTable("updatePwd","修改密码失败【未设置新密码】",messageMap,account);
            }else{
                HttpResponse updatePasswordRsp = AppleIDUtil.updatePassword(account, account.getPwd(), newPwd);
                if (updatePasswordRsp.getStatus() != 200){
                    String message = AppleIDUtil.getValidationErrors("修改密码",updatePasswordRsp, "修改密码失败");
                    setMessageAndRefreshTable("updatePwd",message,messageMap,account);
                }else{
                    account.setPwd(newPwd);
                    setMessageAndRefreshTable("updatePwd","修改密码成功",messageMap,account);
                }
            }
        }

        // 修改生日
        if (updateBirthdayCheckBoxSelected){
            setMessageAndRefreshTable("updateBirthday","正在修改生日...",messageMap,account);
            DateTime birthday = DateUtil.parse(birthdayTextField.getText());
            if(null==birthday){
                setMessageAndRefreshTable("updateBirthday","修改生日失败【未设置生日】",messageMap,account);
            }else{
                String birthdayFormat = DateUtil.format(birthday, "yyyy-MM-dd");
                HttpResponse updateBirthdayRsp = AppleIDUtil.updateBirthday(account, birthdayFormat);
                if (updateBirthdayRsp.getStatus() != 200){
                    String message = AppleIDUtil.getValidationErrors("修改生日",updateBirthdayRsp, "修改生日失败");
                    setMessageAndRefreshTable("updateBirthday",message,messageMap,account);
                }else{
                    account.setBirthday(birthdayFormat);
                    setMessageAndRefreshTable("updateBirthday","修改生日成功",messageMap,account);
                }
            }
        }

        // 修改姓名
        if (updateNameCheckBoxSelected){
            setMessageAndRefreshTable("updateName","正在修改姓名...",messageMap,account);
            String firstName = firstNameTextField.getText();
            String lastName = lastNameTextField.getText();
            Object nameGenerationTypeChoiceBoxValue = nameGenerationTypeChoiceBox.getValue();

            if ("随机中文".equals(nameGenerationTypeChoiceBoxValue)){
                Faker faker = new Faker(Locale.CHINA);
                firstName = faker.name().firstName();
                lastName  = faker.name().lastName();
            }else if ("随机英文".equals(nameGenerationTypeChoiceBoxValue)){
                Faker faker = new Faker();
                firstName = faker.name().firstName();
                lastName  = faker.name().lastName();
            }

            HttpResponse updateNameRsp = AppleIDUtil.updateName(account, account.getPwd(), firstName, lastName);
            if (updateNameRsp.getStatus() != 200){
                String message = AppleIDUtil.getValidationErrors("修改姓名",updateNameRsp, "修改姓名失败");
                setMessageAndRefreshTable("updateName", message,messageMap,account);
            }else{
                account.setName(firstName + lastName);
                setMessageAndRefreshTable("updateName","姓名修改成功",messageMap,account);
            }

        }

        // 修改密保
        if (updatePasswordProtectionCheckBoxSelected){
            setMessageAndRefreshTable("updatePasswordProtection","正在修改密保...",messageMap,account);
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
            if(null==question1ChoiceBoxValue || null==question2ChoiceBoxValue || null==question3ChoiceBoxValue){
                setMessageAndRefreshTable("updatePasswordProtection","密保修改失败【未设置密保问题】",messageMap,account);
            }else{
                body = String.format(body
                        ,answer1TextFieldText,questionMap.get(question1ChoiceBoxValue.toString()),question1ChoiceBoxValue
                        ,answer2TextFieldText,questionMap.get(question2ChoiceBoxValue.toString()),question2ChoiceBoxValue
                        ,answer3TextFieldText,questionMap.get(question3ChoiceBoxValue.toString()),question3ChoiceBoxValue);
                HttpResponse updateQuestionsRsp = AppleIDUtil.updateQuestions(account, body);
                if (updateQuestionsRsp.getStatus() != 200){
                    String message = AppleIDUtil.getValidationErrors("修改密保",updateQuestionsRsp, "修改密保失败");
                    setMessageAndRefreshTable("updatePasswordProtection", message,messageMap,account);
                }else{
                    account.setAnswer1(answer1TextFieldText);
                    account.setAnswer2(answer2TextFieldText);
                    account.setAnswer3(answer3TextFieldText);
                    setMessageAndRefreshTable("updatePasswordProtection","修改密保成功",messageMap,account);
                }
            }
        }

        // 移除设备
        if (removeDeviceCheckBoxSelected){
            setMessageAndRefreshTable("removeDevice","正在移除设备...",messageMap,account);
            HttpResponse deviceListRsp = AppleIDUtil.getDeviceList(account);
            checkAndThrowUnavailableException(deviceListRsp);

            String body = deviceListRsp.body();
            JSONObject bodyJson = JSONUtil.parseObj(body);
            List<String> deviceIdList = bodyJson.getByPath("devices.id", List.class);

            if (CollUtil.isEmpty(deviceIdList)){
                setMessageAndRefreshTable("removeDevice","该账号下暂无设备",messageMap,account);
            }else{
                AppleIDUtil.removeDevices(account,deviceIdList);
                setMessageAndRefreshTable("removeDevice","移除设备成功",messageMap,account);
            }
        }

        // 移除救援邮箱
        if (removeRescueEmailCheckBoxSelected){
            setMessageAndRefreshTable("removeRescueEmail","正在移除救援邮箱...",messageMap,account);
            if (StrUtil.isEmpty(account.getRescueEmail())){
                setMessageAndRefreshTable("removeRescueEmail","该账号下无救援邮箱",messageMap,account);
            }else{
                HttpResponse deleteRescueEmailRsp = AppleIDUtil.deleteRescueEmail(account);
                if (deleteRescueEmailRsp.getStatus() != 204){
                    String message = AppleIDUtil.getValidationErrors("移除救援邮箱",deleteRescueEmailRsp, "移除救援邮箱失败");
                    setMessageAndRefreshTable("removeRescueEmail",message,messageMap,account);
                }else{
                    setMessageAndRefreshTable("removeRescueEmail","移除救援邮箱成功",messageMap,account);
                }
            }
        }

        // 修改显示语言
        if (updateShowLangCheckBoxSelected){
            setMessageAndRefreshTable("updateShowLang","正在修改显示语言...",messageMap,account);
            Object showLang = updateShowLangComboBox.getValue();
            if(null==showLang){
                setMessageAndRefreshTable("updateShowLang","修改显示语言失败【未设置显示语言】",messageMap,account);
            }else{
                LinkedHashMap<String, String> languageMap = DataUtil.getLanguageMap();
                String langCode = languageMap.get(showLang);
                HttpResponse changeShowLanguageRsp = AppleIDUtil.changeShowLanguage(account,langCode);
                if (changeShowLanguageRsp.getStatus() != 200){
                    String message = AppleIDUtil.getValidationErrors("修改显示语言",changeShowLanguageRsp,"修改显示语言失败");
                    setMessageAndRefreshTable("updateShowLang", message ,messageMap,account);
                }else{
                    setMessageAndRefreshTable("updateShowLang","修改显示语言成功",messageMap,account);
                }
            }
        }

        if (!account.getNote().contains("成功")){
            throw new ServiceException(account.getNote());
        }
    }

    private void setMessageAndRefreshTable(String key,String message, Map<String,String> messageMap,Account account){
        String note="";
        messageMap.put(key, message);
        for(Map.Entry entry : messageMap.entrySet()){
            note=StringUtils.isEmpty(note)?entry.getValue().toString():(note+";"+entry.getValue());
        }
        account.setNote(note);
        super.accountTableView.refresh();
    }
}

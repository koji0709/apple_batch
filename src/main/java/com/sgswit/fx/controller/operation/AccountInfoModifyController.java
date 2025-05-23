package com.sgswit.fx.controller.operation;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.javafaker.Faker;
import com.sgswit.fx.controller.exception.ServiceException;
import com.sgswit.fx.controller.operation.viewData.AccountInfoModifyView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.*;
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
        //menuItem.add(Constant.RightContextMenu.WEB_TWO_FACTOR_CODE.getCode());
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
        String question1ChoiceBoxValue = StrUtils.isEmpty(question1ChoiceBox.getValue())?"":question1ChoiceBox.getValue().toString();
        String question2ChoiceBoxValue = StrUtils.isEmpty(question2ChoiceBox.getValue())?"":question2ChoiceBox.getValue().toString();
        String question3ChoiceBoxValue = StrUtils.isEmpty(question3ChoiceBox.getValue())?"":question3ChoiceBox.getValue().toString();
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

    public List<Account> parseAccount(String accountStr) {
        if (StrUtil.isEmpty(accountStr)){
            return Collections.emptyList();
        }
        String[] accList = accountStr.split("\n");
        if (accList.length == 0){
            return Collections.emptyList();
        }
        List<String> fieldList = Arrays.asList("account","pwd","answer1","answer2","answer3");

        List<Account> accountList = new ArrayList<>();
        for (int i = 0; i < accList.length; i++) {
            String acc = accList[i];
            if(StringUtils.isEmpty(acc)){
                continue;
            }
            acc = acc.trim();
            List<String> fieldValueList = splitfieldValue(acc);
            Account account = new Account();
            for (int i1 = 0; i1 < fieldList.size(); i1++) {
                if (i1 < fieldValueList.size()){
                    String field = fieldList.get(i1);
                    ReflectUtil.invoke(
                            account
                            , "set" + field.substring(0, 1).toUpperCase() + field.substring(1)
                            , fieldValueList.get(i1));
                }
            }
            accountList.add(account);
        }
        return accountList;
    }

    public static List<String> splitfieldValue(String acc) {
        // 判断字符串是否包含空格或制表符
        if (acc.contains(" ") || acc.contains("\t")) {
            // 使用空格或制表符作为分隔符分割字符串
            return Arrays.asList(acc.split("[ \t]+"));
        } else {
            // 使用 ---- 或 - 作为分隔符分割字符串
            return Arrays.asList(acc.split("[-]+"));
        }
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
                DateTime birthday = DateUtil.parse(birthdayTextFieldText);
                long ageInYears = DateUtil.betweenYear(birthday, new Date(),false);
                if (ageInYears < 18){
                    throw new ServiceException("年龄至少18岁");
                }
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
     * 修改操作
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
        if (StrUtil.isEmpty(accountRsp.body()) || !JSONUtil.isTypeJSON(accountRsp.body())){
            throw new ServiceException("读取用户信息失败");
        }
        JSON accountJson = JSONUtil.parse(accountRsp.body());
        account.setArea(accountJson.getByPath("account.person.primaryAddress.countryName",String.class));
        account.setBirthday(accountJson.getByPath("account.person.birthday",String.class));
        account.setName(accountJson.getByPath("name.fullName",String.class));
        account.setRescueEmail(accountJson.getByPath("account.security.rescueEmail",String.class));
        String createdDate = accountJson.getByPath("account.person.reachableAtOptions.primaryEmailAddress.createdDate", String.class);
        String updateDate = accountJson.getByPath("account.person.reachableAtOptions.primaryEmailAddress.updateDate", String.class);
        account.setCreatedDate(DateUtil.format(new Date(Long.valueOf(createdDate.split("\\.")[0])*1000),"yyyy年MM月dd日"));
        account.setUpdateDate(DateUtil.format(new Date(Long.valueOf(updateDate.split("\\.")[0])*1000),"yyyy年MM月dd日"));
        account.setNote("查询成功");
        super.accountTableView.refresh();
        Map<String,String> messageMap= new LinkedHashMap<>();

        boolean errorExist = false;

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
                    errorExist = true;
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
                errorExist = true;
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
                    errorExist = true;
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
                    errorExist = true;
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
                    errorExist = true;
                    String message = AppleIDUtil.getValidationErrors("修改显示语言",changeShowLanguageRsp,"修改显示语言失败");
                    setMessageAndRefreshTable("updateShowLang", message ,messageMap,account);
                }else{
                    setMessageAndRefreshTable("updateShowLang","修改显示语言成功",messageMap,account);
                }
            }
        }
        // 修改密码
        if (updatePwdCheckBoxSelected){
            setMessageAndRefreshTable("updatePwd","正在修改密码...",messageMap,account);
            String newPwd = pwdTextField.getText();
            if(StringUtils.isEmpty(newPwd)){
                setMessageAndRefreshTable("updatePwd","修改密码失败【未设置新密码】",messageMap,account);
            }else{
                HttpResponse updatePasswordRsp = AppleIDUtil.updatePassword(account, account.getPwd(), newPwd);

                if (updatePasswordRsp.getStatus() != 200){
                    List<String> errorCodeList = new ArrayList<>();
                    String body = updatePasswordRsp.body();
                    if (StrUtil.isNotEmpty(body) && JSONUtil.isTypeJSON(body)){
                        JSONObject jsonObject= JSONUtil.parseObj(body);
                        errorCodeList = jsonObject.getByPath("validationErrors.code", List.class);
                    }
                    if (CollUtil.isNotEmpty(errorCodeList) && errorCodeList.contains("matchCurrent")){
                        account.setPwd(newPwd);
                        setMessageAndRefreshTable("updatePwd","修改密码成功",messageMap,account);
                    }else{
                        errorExist = true;
                        String message = AppleIDUtil.getValidationErrors("修改密码",updatePasswordRsp, "修改密码失败");
                        setMessageAndRefreshTable("updatePwd",message,messageMap,account);
                    }
                }else{
                    account.setPwd(newPwd);
                    setMessageAndRefreshTable("updatePwd","修改密码成功",messageMap,account);
                }
            }
        }

        // 如果有错误存在, 则把该数据登陆状态设置为false
        if (errorExist){
            account.setIsLogin(false);
        }
        if (!account.getNote().contains("成功")){
            throw new ServiceException(account.getNote());
        }
    }

    public Map<String,String> getRepareInfo(){
        Map<String,String> map = new HashMap<>();
        boolean updateBirthdayCheckBoxSelected = updateBirthdayCheckBox.isSelected();
        // 修改生日
        if (updateBirthdayCheckBoxSelected){
            DateTime birthday = DateUtil.parse(birthdayTextField.getText());
            String birthdayFormat = DateUtil.format(birthday, "yyyy-MM-dd");
            map.put("birthday",birthdayFormat);
        }

        boolean updatePasswordProtectionCheckBoxSelected = updatePasswordProtectionCheckBox.isSelected();
        if (updatePasswordProtectionCheckBoxSelected){
            Object question1ChoiceBoxValue = question1ChoiceBox.getValue();
            Object question2ChoiceBoxValue = question2ChoiceBox.getValue();
            Object question3ChoiceBoxValue = question3ChoiceBox.getValue();
            String answer1TextFieldText = answer1TextField.getText();
            String answer2TextFieldText = answer2TextField.getText();
            String answer3TextFieldText = answer3TextField.getText();

            LinkedHashMap<String, Integer> questionMap = DataUtil.getQuestionMap();
            String body = "{\"security\":{\"questions\":[{\"answer\":\"%s\",\"id\":\"%s\",\"question\":\"%s\"}" +
                    ",{\"answer\":\"%s\",\"id\":\"%s\",\"question\":\"%s\"}" +
                    ",{\"answer\":\"%s\",\"id\":\"%s\",\"question\":\"%s\"}]}}";

            body = String.format(body
                    ,answer1TextFieldText,questionMap.get(question1ChoiceBoxValue.toString()),question1ChoiceBoxValue
                    ,answer2TextFieldText,questionMap.get(question2ChoiceBoxValue.toString()),question2ChoiceBoxValue
                    ,answer3TextFieldText,questionMap.get(question3ChoiceBoxValue.toString()),question3ChoiceBoxValue);
            map.put("ppBody",body);
            map.put("answer1",answer1TextFieldText);
            map.put("answer2",answer2TextFieldText);
            map.put("answer3",answer3TextFieldText);
        }

        return map;
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

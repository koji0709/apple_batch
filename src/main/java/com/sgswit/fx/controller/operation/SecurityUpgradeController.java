package com.sgswit.fx.controller.operation;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.exception.ServiceException;
import com.sgswit.fx.controller.operation.viewData.SecurityUpgradeView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.cache.DataUtil;
import com.sgswit.fx.utils.PointUtil;
import com.sgswit.fx.utils.web.AppleIDUtil;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 双重认证controller
 */
public class SecurityUpgradeController extends SecurityUpgradeView {

    Map<String, JSONObject> globalMobilePhoneMap = new HashMap<>();
    private String regex = "(\\d{6})";
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        apiCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                apiYcTextField.setDisable(false);
                apiCsTextField.setDisable(false);
                apiDmTextField.setDisable(false);
            } else {
                apiYcTextField.setDisable(true);
                apiCsTextField.setDisable(true);
                apiDmTextField.setDisable(true);
            }
        });
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.SECURITY_UPGRADE.getCode())));
        super.initialize(url, resourceBundle);

        initViewData();
    }

    public void onPrompt(MouseEvent mouseEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setPrefHeight(400);
        alert.getDialogPane().setPrefWidth(800);
        alert.getDialogPane().setStyle(" -fx-font-size: 16px;");
        alert.setTitle("提示");
        alert.setContentText("Api读取验证码配置说明：\n" +
                "Api读取验证码支持字段或者正则提取验证码，多个关键字按|分割\n" +
                "例如: api请求返回： {\"code\":1,\"msg\":\"ok\",\"data\":{\"code\":\"Your Apple Account Code is: 583173. Don't share it with anyone.\",\"code_time\":\"2025-11-03 14:44:01\",\"expired_date\":\"2025-11-20\"}}\n" +
                "验证码是 583173,字段是 code。\n" +
                "\n" +
                "正则方式 提取六位整数：则 APi配置填: \\\\d{6}\n" +
                "正则方式 限定上下文：匹配is: 后的 6 位数字：则 APi配置填: is: (\\\\d{6})\n" +
                "正则方式 更严谨：结合前后文边界（避免部分匹配）则 APi配置填: is: (\\\\d{6})\\\\.\n");

        alert.showAndWait();
    }

    public void initViewData() {
        // 默认中国
        dialCodeComboBox.setValue("+86(中国大陆)");
        String mobilePhoneJson = ResourceUtil.readUtf8Str("data/support_all_country.json");
        JSONArray mobilePhoneArray = JSONUtil.parseArray(mobilePhoneJson);
        for (Object o : mobilePhoneArray) {
            JSONObject json = (JSONObject) o;
            // +86（中国大陆）
            String format = "+%s(%s)";
            String code = json.getStr("dial_code");
            String zh = json.getStr("name_zh");
            dialCodeComboBox.getItems().add(String.format(format, code, zh));
            globalMobilePhoneMap.put(code, json);
        }
    }

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction(ActionEvent actionEvent) {
        openImportAccountView(List.of("account----pwd-answer1-answer2-answer3-phone-api","account----pwd-answer1-answer2-answer3-phone"), actionEvent);
    }

    /**
     * qewqeq@2980.com----dPFb6cSD422-宠物-工作-父母-17761292080
     */
    @Override
    public void accountHandler(Account account) {
        String phone = account.getPhone();
        Object verifyCode = account.getAuthData().get("verifyCode");
//        if (StrUtil.isEmptyIfStr(verifyCode)){
            // 登录
            HttpResponse upgradeResp = AppleIDUtil.securityUpgradeLogin(account);
            String format = dialCodeComboBox.getValue().toString();
            String countryDialCode   = format.substring(1,format.indexOf("("));
            JSONObject json = globalMobilePhoneMap.get(countryDialCode);
            String countryCode = json.getStr("code");
            countryCode= DataUtil.getInfoByCountryCode(countryCode).getCode2();
            String[] eligibilityWarnings=json.getByPath("eligibilityWarnings",String[].class);
            String body  = "{\"phoneNumberVerification\":{\"phoneNumber\":{\"number\":\""+phone+"\",\"countryCode\":\""+countryCode+"\"},\"mode\":\"sms\"}}";;
            if(null!=eligibilityWarnings && eligibilityWarnings.length>0){JSON bodyJson=JSONUtil.parse(body);
                bodyJson.putByPath("acceptedWarnings",eligibilityWarnings);
                body=JSONUtil.toJsonStr(bodyJson);
            }
            HttpResponse securityUpgradeVerifyPhoneRsp = AppleIDUtil.securityUpgradeVerifyPhone(account, body);
            String failMessage = AppleIDUtil.hasFailMessage(securityUpgradeVerifyPhoneRsp);
            if (!StrUtil.isEmpty(failMessage)){
                throw new ServiceException(failMessage);
            }

            JSON jsonBody = JSONUtil.parse(securityUpgradeVerifyPhoneRsp.body());
            String areaCode = jsonBody.getByPath("phoneNumberVerification.phoneNumber.countryCode", String.class);
            account.setArea(DataUtil.getNameByCountryCode(areaCode));

            if (securityUpgradeVerifyPhoneRsp.getStatus() != 200){
                List meesageList = jsonBody.getByPath("phoneNumberVerification.serviceErrors.message", List.class);
                String message = String.join(",", meesageList);
                if(securityUpgradeVerifyPhoneRsp.getStatus()==423){
                    account.getAuthData().put("securityUpgradeVerifyPhoneRsp",securityUpgradeVerifyPhoneRsp);
                    throw new ServiceException(message);
                }else{
                    throw new ServiceException(message,"发送验证码失败");
                }
            }
            account.getAuthData().put("securityUpgradeVerifyPhoneRsp",securityUpgradeVerifyPhoneRsp);


            //查看是否需要api接码
            boolean selected = apiCheckBox.isSelected();
            if (selected) {
                setAndRefreshNote(account, "成功发送验证码，正在解析验证码...");
                // 循环查询验证码
                Integer cs = Integer.valueOf(apiCsTextField.getText());
                //获取接码
                String api = account.getApi();
                if (StrUtil.isEmpty(api)) {
                    throw new ServiceException("解析失败，未绑定接码的API地址。");
                }
                String smsCode = "";
                for (int i = 1; i <= cs; i++) {
                    try {
                        // 暂停5秒（5000毫秒）
                        Integer yc = Integer.valueOf(apiYcTextField.getText());
                        Thread.sleep(yc * 1000);
                        setAndRefreshNote(account,"第 " + (i) + " 次获取验证码");
                        HttpResponse httpResponse = HttpRequest.get(api).execute();
                        if(httpResponse.getStatus()==200){
                            String responseBody = httpResponse.body();
                            if (StrUtil.isNotEmpty(responseBody)) {
                                Pattern compile = Pattern.compile(StrUtil.isEmpty(apiDmTextField.getText())?regex:apiDmTextField.getText(),Pattern.CASE_INSENSITIVE);
                                Matcher matcher = compile.matcher(responseBody);
                                // 查找并提取匹配的数字
                                if (matcher.find()) {
                                    smsCode = matcher.group();
                                    break;
                                }
                            }
                        }

                    } catch (InterruptedException e) {
                        // 恢复中断状态（可选，根据业务需求）
                        Thread.currentThread().interrupt();
                        // 若中断，可提前退出循
                    }
                }
                if(!StrUtil.isEmpty(smsCode)){
                    account.getAuthData().put("verifyCode", smsCode);
                    setAndRefreshNote(account, "已成功接收验证码，正在绑定。");
                    accountHandler(account);
                }else{
                    setAndRefreshNote(account, "开通双重认证失败：验证码解析失败。");
                }
            } else {
                setAndRefreshNote(account, "成功发送验证码，请输入验证码。");
            }
//        } else {
//            Object securityUpgradeVerifyPhoneObject = account.getAuthData().get("securityUpgradeVerifyPhoneRsp");
//            if (securityUpgradeVerifyPhoneObject == null){
//                account.getAuthData().put("verifyCode","");
//                throw new ServiceException("请先发送验证码");
//            }
//            account.setNote("正在绑定...");
//            HttpResponse securityUpgradeVerifyPhoneRsp = (HttpResponse) securityUpgradeVerifyPhoneObject;
//            JSON jsonBody = JSONUtil.parse(securityUpgradeVerifyPhoneRsp.body());
//            JSONObject phoneNumber = jsonBody.getByPath("phoneNumberVerification.phoneNumber", JSONObject.class);
//            String[] eligibilityWarnings=jsonBody.getByPath("eligibilityWarnings",String[].class);
//            String body="{\"phoneNumberVerification\":{\"phoneNumber\":{\"id\":"+phoneNumber.getInt("id")+",\"number\":\""+phone+"\",\"countryCode\":\""+phoneNumber.getStr("countryCode")+"\",\"nonFTEU\":"+phoneNumber.getBool("nonFTEU")+"},\"securityCode\":{\"code\":\""+ verifyCode +"\"},\"mode\":\"sms\"}}";
//
//            if(null!=eligibilityWarnings && eligibilityWarnings.length>0){
//                JSON bodyJson=JSONUtil.parse(body);
//                bodyJson.putByPath("acceptedWarnings",eligibilityWarnings);
//                body=JSONUtil.toJsonStr(bodyJson);
//            }
//            HttpResponse securityUpgradeRsp = AppleIDUtil.securityUpgrade(account,body);
//            if (securityUpgradeRsp.getStatus() != 200){
//                String failMessage = AppleIDUtil.getValidationErrors("绑定双重认证", securityUpgradeRsp, "绑定双重认证失败");
//                account.getAuthData().put("verifyCode","");
//                throw new ServiceException(failMessage);
//            }
            setAndRefreshNote(account,"开启双重认证成功");
//        }
    }

    @Override
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> menuItem = new ArrayList<>() {{
            add(Constant.RightContextMenu.DELETE.getCode());
            add(Constant.RightContextMenu.REEXECUTE.getCode());
            add(Constant.RightContextMenu.COPY.getCode());
            add(Constant.RightContextMenu.CODE.getCode());
        }};
        super.onContentMenuClick(contextMenuEvent, accountTableView, menuItem, new ArrayList<>());
    }

    @Override
    protected void secondStepHandler(Account account, String code) {
        account.getAuthData().put("verifyCode", code);
        accountHandlerExpand(account);
    }
}

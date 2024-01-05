package com.sgswit.fx.controller.operation;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.operation.viewData.SecurityUpgradeView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.DataUtil;
import javafx.scene.input.ContextMenuEvent;

import java.net.URL;
import java.util.*;

/**
 * 双重认证controller
 */
public class SecurityUpgradeController extends SecurityUpgradeView {

    Map<String,JSONObject> globalMobilePhoneMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        initViewData();
    }

    public void initViewData(){
        // 默认中国
        dialCodeComboBox.setValue("+86(中国大陆)");
        String mobilePhoneJson = ResourceUtil.readUtf8Str("data/global-mobile-phone-regular.json");
        JSONObject jsonObj = JSONUtil.parseObj(mobilePhoneJson);
        JSONArray mobilephoneArray = jsonObj.getJSONArray("data");
        for (Object o : mobilephoneArray) {
            JSONObject json = (JSONObject) o;
            // +86（中国大陆）
            String format = "+%s(%s)";
            String code = json.getStr("code");
            String zh = json.getStr("zh");
            dialCodeComboBox.getItems().add(String.format(format, code, zh));
            globalMobilePhoneMap.put(code,json);
        }
    }

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction() {
        openImportAccountView(List.of("account----pwd-answer1-answer2-answer3-phone"));
    }

    /**
     * qewqeq@2980.com----dPFb6cSD422-宠物-工作-父母-17761292080
     */
    @Override
    public void accountHandler(Account account) {
        // 登陆
        login(account);

        String phone = account.getPhone();
        Object verifyCode = account.getAuthData().get("verifyCode");
        if (verifyCode == null){
            String format = dialCodeComboBox.getValue().toString();
            String countryDialCode   = format.substring(1,format.indexOf("("));
            JSONObject json = globalMobilePhoneMap.get(countryDialCode);
            String countryCode = json.getStr("locale");

            String body = "{\"acceptedWarnings\":[],\"phoneNumberVerification\":{\"phoneNumber\":{\"countryCode\":\""+countryCode+"\",\"number\":\""+phone+"\",\"countryDialCode\":\""+countryDialCode+"\",\"nonFTEU\":true},\"mode\":\"sms\"}}";

            HttpResponse securityUpgradeVerifyPhoneRsp = AppleIDUtil.securityUpgradeVerifyPhone(account, body);
            if (securityUpgradeVerifyPhoneRsp.getStatus() == 503){
                throwAndRefreshNote(account,"操作频繁");
            }
            String failMessage = failMessage(securityUpgradeVerifyPhoneRsp);
            if (!StrUtil.isEmpty(failMessage)){
                throwAndRefreshNote(account,failMessage);
            }

            JSON jsonBody = JSONUtil.parse(securityUpgradeVerifyPhoneRsp.body());
            String areaCode = jsonBody.getByPath("phoneNumberVerification.phoneNumber.countryCode", String.class);
            account.setArea(DataUtil.getNameByCountryCode(areaCode));

            if (securityUpgradeVerifyPhoneRsp.getStatus() != 200){
                List meesageList = jsonBody.getByPath("phoneNumberVerification.serviceErrors.message", List.class);
                String message = String.join(",", meesageList);
                throwAndRefreshNote(account,message,"发送验证码失败");
            }

            account.getAuthData().put("securityUpgradeVerifyPhoneRsp",securityUpgradeVerifyPhoneRsp);
            setAndRefreshNote(account,"成功发送验证码，请输入验证码。");
        } else {
            Object securityUpgradeVerifyPhoneObject = account.getAuthData().get("securityUpgradeVerifyPhoneRsp");
            if (securityUpgradeVerifyPhoneObject == null){
                setAndRefreshNote(account,"请先发送验证码",false);
                return;
            }
            HttpResponse securityUpgradeVerifyPhoneRsp = (HttpResponse) securityUpgradeVerifyPhoneObject;
            JSON jsonBody2 = JSONUtil.parse(securityUpgradeVerifyPhoneRsp.body());
            JSONObject phoneNumber = jsonBody2.getByPath("phoneNumberVerification.phoneNumber", JSONObject.class);

            String body2 = "{\"phoneNumberVerification\":{\"phoneNumber\":{\"id\":"+phoneNumber.getInt("id")+",\"number\":\""+phone+"\",\"countryCode\":\""+phoneNumber.getStr("countryCode")+"\",\"nonFTEU\":"+phoneNumber.getBool("nonFTEU")+"},\"securityCode\":{\"code\":\""+ verifyCode +"\"},\"mode\":\"sms\"}}";
            HttpResponse securityUpgradeRsp = AppleIDUtil.securityUpgrade(securityUpgradeVerifyPhoneRsp,account,body2);
            if (securityUpgradeRsp.getStatus() != 302){
                throwAndRefreshNote(account,"绑定双重认证失败");
            }
            setAndRefreshNote(account,"绑定双重认证成功");
        }

    }

    @Override
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> menuItem =new ArrayList<>(){{
            add(Constant.RightContextMenu.DELETE.getCode());
            add(Constant.RightContextMenu.REEXECUTE.getCode());
            add(Constant.RightContextMenu.COPY.getCode());
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

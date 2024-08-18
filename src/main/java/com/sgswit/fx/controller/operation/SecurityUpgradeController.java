package com.sgswit.fx.controller.operation;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.operation.viewData.SecurityUpgradeView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.DataUtil;
import com.sgswit.fx.utils.PointUtil;
import javafx.event.ActionEvent;
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
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.SECURITY_UPGRADE.getCode())));
        super.initialize(url, resourceBundle);
        initViewData();
    }

    public void initViewData(){
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
            globalMobilePhoneMap.put(code,json);
        }
    }

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction(ActionEvent actionEvent) {
        openImportAccountView(List.of("account----pwd-answer1-answer2-answer3-phone"),actionEvent);
    }

    /**
     * qewqeq@2980.com----dPFb6cSD422-宠物-工作-父母-17761292080
     */
    @Override
    public void accountHandler(Account account) {
        String phone = account.getPhone();
        Object verifyCode = account.getAuthData().get("verifyCode");
        if (StrUtil.isEmptyIfStr(verifyCode)){
            // 登录
            AppleIDUtil.securityUpgradeLogin(account);
            String format = dialCodeComboBox.getValue().toString();
            String countryDialCode   = format.substring(1,format.indexOf("("));
            JSONObject json = globalMobilePhoneMap.get(countryDialCode);
            String countryCode = json.getStr("code");
            countryCode=DataUtil.getInfoByCountryCode(countryCode).getCode2();
            String body = "{\"phoneNumberVerification\":{\"phoneNumber\":{\"number\":\""+phone+"\",\"countryCode\":\""+countryCode+"\"},\"mode\":\"sms\"}}";
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
            setAndRefreshNote(account,"成功发送验证码，请输入验证码。");
        } else {
            Object securityUpgradeVerifyPhoneObject = account.getAuthData().get("securityUpgradeVerifyPhoneRsp");
            if (securityUpgradeVerifyPhoneObject == null){
                account.getAuthData().put("verifyCode","");
                throw new ServiceException("请先发送验证码");
            }
            account.setNote("正在绑定...");
            HttpResponse securityUpgradeVerifyPhoneRsp = (HttpResponse) securityUpgradeVerifyPhoneObject;
            JSON jsonBody = JSONUtil.parse(securityUpgradeVerifyPhoneRsp.body());
            JSONObject phoneNumber = jsonBody.getByPath("phoneNumberVerification.phoneNumber", JSONObject.class);

            String body2 = "{\"phoneNumberVerification\":{\"phoneNumber\":{\"id\":"+phoneNumber.getInt("id")+",\"number\":\""+phone+"\",\"countryCode\":\""+phoneNumber.getStr("countryCode")+"\",\"nonFTEU\":"+phoneNumber.getBool("nonFTEU")+"},\"securityCode\":{\"code\":\""+ verifyCode +"\"},\"mode\":\"sms\"}}";
            HttpResponse securityUpgradeRsp = AppleIDUtil.securityUpgrade(account,body2);
            if (securityUpgradeRsp.getStatus() != 200){
                String failMessage = AppleIDUtil.getValidationErrors("绑定双重认证", securityUpgradeRsp, "绑定双重认证失败");
                throw new ServiceException(failMessage);
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
    protected void secondStepHandler(Account account, String code) {
        account.getAuthData().put("verifyCode",code);
        accountHandlerExpand(account);
    }
}

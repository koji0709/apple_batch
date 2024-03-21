package com.sgswit.fx.controller.common;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.model.LoginInfo;
import com.sgswit.fx.utils.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.ContextMenuEvent;
import org.apache.commons.lang3.StringUtils;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ItunesView<T extends LoginInfo> extends CustomTableView<T> {

    // 登录成功的账号缓存(缓存5分钟,能刷新)
    protected static TimedCache<String, LoginInfo> loginSuccessMap = CacheUtil.newTimedCache(300000);

    static {
        loginSuccessMap.schedulePrune(300000);
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        menuItem.add(Constant.RightContextMenu.CODE.getCode());
    }

    public void itunesLogin(T accountModel){
        String appleId = ((SimpleStringProperty) ReflectUtil.getFieldValue(accountModel, "account")).getValue();
        String pwd = ((SimpleStringProperty) ReflectUtil.getFieldValue(accountModel, "pwd")).getValue();

        LoginInfo loginInfo = loginSuccessMap.get(appleId+pwd);
        if (loginInfo != null) {
            accountModel.setIsLogin(loginInfo.isLogin());
            accountModel.setItspod(loginInfo.getItspod());
            accountModel.setStoreFront(loginInfo.getStoreFront());
            accountModel.setDsPersonId(loginInfo.getDsPersonId());
            accountModel.setPasswordToken(loginInfo.getPasswordToken());
            accountModel.setGuid(loginInfo.getGuid());
            accountModel.setAuthData(loginInfo.getAuthData());
            accountModel.setCookieMap(loginInfo.getCookieMap());
            setAndRefreshNote(accountModel,"成功获取登录信息。");
        }else{
            accountModel.setIsLogin(false);
        }

        if (accountModel.isLogin()){
            return;
        }

        String guid = DataUtil.getGuidByAppleId(appleId);
        accountModel.setGuid(guid);

        String url = "";
        HttpResponse loginRsp;
        if (StrUtil.isEmpty(accountModel.getAuthCode())){
            url = "https://buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/authenticate?guid="+accountModel.getGuid();
            loginRsp = itunesLogin(accountModel,url,0);
        }else{
            Object authRsp = accountModel.getAuthData().get("authRsp");
            if (authRsp == null){
                throw new ServiceException("请先登录鉴权");
            }
            url = "https://p"+ accountModel.getItspod() +"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/authenticate?guid="+accountModel.getGuid();
            loginRsp = itunesLogin(accountModel,url,1);
        }

        int status = loginRsp.getStatus();
        if (status == 503){
            throw new UnavailableException();
        }

        if (loginRsp == null || StrUtil.isEmpty(loginRsp.body())){
            throw new ServiceException("AppleID或密码错误。");
        }

        if (!loginRsp.body().startsWith("<?xml")){
            LoggerManger.info("接口响应异常; " + loginRsp.body());
            throw new UnavailableException();
        }

        JSONObject json = PListUtil.parse(loginRsp.body());
        String failureType     = json.getStr("failureType","");
        String customerMessage = json.getStr("customerMessage","");

        boolean verify = !(status != 200 || !StrUtil.isEmpty(failureType)  || !StrUtil.isEmpty(customerMessage));
        if (verify){
            setAndRefreshNote(accountModel,"登录成功。");
            accountModel.setAuthCode("");
            accountModel.getAuthData().put("authRsp",loginRsp);
            accountModel.setItspod(loginRsp.header(Constant.ITSPOD));
            accountModel.setStoreFront(loginRsp.header(Constant.HTTPHeaderStoreFront));
            accountModel.setDsPersonId(json.getStr("dsPersonId",""));
            accountModel.setPasswordToken(json.getStr("passwordToken",""));
            CookieUtils.setCookiesToMap(loginRsp,accountModel.getCookieMap());
            accountModel.setIsLogin(true);
            loginSuccessMap.put(appleId+pwd,accountModel);
            return;
        }

        String message = "登录失败。";
        if (!StrUtil.isEmpty(customerMessage)){
            for (Map.Entry<String, String> entry : Constant.errorMap.entrySet()) {
                if (StringUtils.containsIgnoreCase(customerMessage,entry.getKey())){
                    message = entry.getValue();
                    break;
                }
            }
        }
        throw new ServiceException(message);
    }

    private HttpResponse itunesLogin(T accountModel,String url,Integer attempt){
        String guid = accountModel.getGuid();
        String account = ((SimpleStringProperty) ReflectUtil.getFieldValue(accountModel, "account")).getValue();
        String pwd     = ((SimpleStringProperty) ReflectUtil.getFieldValue(accountModel, "pwd")).getValue();

        HttpResponse authRsp = ITunesUtil.authenticate(account, pwd, accountModel.getAuthCode(), guid, url);
        url = authRsp.header("location");
        int status = authRsp.getStatus();

        if (status == 302){
            return itunesLogin(accountModel,url,1);
        }

        if (status != 200){
            return authRsp;
        }

        JSONObject authBody        = PListUtil.parse(authRsp.body());
        String     failureType     = authBody.getStr("failureType","");
        String     customerMessage = authBody.getStr("customerMessage","");

        // 重试
        if(attempt == 0 && Constant.FailureTypeInvalidCredentials.equals(failureType)){
            return itunesLogin(accountModel,url,1);
        }

        // 双重认证
        if("".equals(failureType) && Constant.CustomerMessageBadLogin.equals(customerMessage)){
            accountModel.getAuthData().put("authRsp",authRsp);
            accountModel.setItspod(authRsp.header(Constant.ITSPOD));
            throw new ServiceException("Apple ID或密码错误。或需要输入双重验证码;");
        }

        if(!"".equals(failureType) && !"".equals(customerMessage)){
            return authRsp;
        }

        if(!"".equals(failureType)){
            return authRsp;
        }
        return authRsp;
    }

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        onContentMenuClick(contextMenuEvent, new ArrayList<>(menuItem));
    }

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent, List<String> menuItemList) {
        super.onContentMenuClick(contextMenuEvent,accountTableView,menuItemList,new ArrayList<>());
    }

}

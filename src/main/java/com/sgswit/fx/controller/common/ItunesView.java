package com.sgswit.fx.controller.common;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.model.LoginInfo;
import com.sgswit.fx.utils.CookieUtils;
import com.sgswit.fx.utils.ITunesUtil;
import com.sgswit.fx.utils.PListUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.ContextMenuEvent;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.StringUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ItunesView<T extends LoginInfo> extends CustomTableView<T> {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        menuItem.add(Constant.RightContextMenu.CODE.getCode());
    }

    public void itunesLogin(T accountModel){
        if (accountModel.isLogin()){
            return;
        }

        String url = "";
        HttpResponse loginRsp;
        if (StrUtil.isEmpty(accountModel.getAuthCode())){
            url = "https://buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/authenticate?guid="+accountModel.getGuid();
            loginRsp = itunesLogin(accountModel,url,0);
        }else{
            Object authRsp = accountModel.getAuthData().get("authRsp");
            if (authRsp == null){
                throwAndRefreshNote(accountModel,"请先登陆鉴权");
            }
            url = "https://p"+ accountModel.getItspod() +"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/authenticate?guid="+accountModel.getGuid();
            loginRsp = itunesLogin(accountModel,url,1);
        }

        int status = loginRsp.getStatus();
        if (status == 503){
            throwAndRefreshNote(accountModel,"操作频繁!");
        }

        if (loginRsp == null || StrUtil.isEmpty(loginRsp.body())){
            throwAndRefreshNote(accountModel,"AppleID或密码错误。");
        }

        JSONObject json = PListUtil.parse(loginRsp.body());
        String failureType     = json.getStr("failureType","");
        String customerMessage = json.getStr("customerMessage","");

        boolean verify = !(status != 200 || !StrUtil.isEmpty(failureType)  || !StrUtil.isEmpty(customerMessage));
        if (verify){
            setAndRefreshNote(accountModel,"登陆成功。");
            accountModel.getAuthData().put("authRsp",loginRsp);
            accountModel.setItspod(loginRsp.header(Constant.ITSPOD));
            accountModel.setStoreFront(loginRsp.header(Constant.HTTPHeaderStoreFront));
            accountModel.setDsPersonId(json.getStr("dsPersonId",""));
            accountModel.setPasswordToken(json.getStr("passwordToken",""));
            CookieUtils.setCookiesToMap(loginRsp,accountModel.getCookieMap());
            accountModel.setIsLogin(true);
            return;
        }

        String message = "登陆失败。";
        if (!StrUtil.isEmpty(customerMessage)){
            for (Map.Entry<String, String> entry : Constant.errorMap.entrySet()) {
                if (StringUtils.containsIgnoreCase(customerMessage,entry.getKey())){
                    message = entry.getValue();
                    break;
                }
            }
        }
        throwAndRefreshNote(accountModel,message);
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
        if(attempt == 0 && "".equals(failureType) && StrUtil.isEmpty(accountModel.getAuthCode()) && Constant.CustomerMessageBadLogin.equals(customerMessage)){
            accountModel.getAuthData().put("authRsp",authRsp);
            accountModel.setItspod(authRsp.header(Constant.ITSPOD));
            throwAndRefreshNote(accountModel,"此账号已开启双重认证;");
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

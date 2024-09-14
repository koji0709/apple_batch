package com.sgswit.fx.controller.common;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.model.LoginInfo;
import com.sgswit.fx.utils.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.ContextMenuEvent;

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
        String message ="";
        try{
            String appleId = ((SimpleStringProperty) ReflectUtil.getFieldValue(accountModel, "account")).getValue();
            String pwd = ((SimpleStringProperty) ReflectUtil.getFieldValue(accountModel, "pwd")).getValue();
            String id=super.createId(appleId,pwd);
            LoginInfo loginInfo = loginSuccessMap.get(id);
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
                url = "https://buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/authenticate?guid="+guid;
                loginRsp = itunesLogin(accountModel,url,0);
            }else{
                Object authRsp = accountModel.getAuthData().get("authRsp");
                if (authRsp == null){
                    throw new ServiceException("请先登录鉴权");
                }
                url = "https://p"+ accountModel.getItspod() +"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/authenticate?guid="+guid;
                loginRsp = itunesLogin(accountModel,url,1);
            }
            JSONObject json=null;
            try{
                json = PListUtil.parse(loginRsp.body());
            }catch (Exception e){
                throw new UnavailableException();
            }
            Map<String,Object> result= ITunesUtil.checkLoginRes(loginRsp.body());

            boolean verify = result.get("code").equals(Constant.SUCCESS)?true:false;
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
                String storeId=super.createId(appleId,pwd);
                loginSuccessMap.put(storeId,accountModel);
                return;
            }
            message = MapUtil.getStr(result,"msg");
        }catch (ServiceException e){
            LoggerManger.info("itunesLogin登陆失败",e);
            message=e.getMessage();
        }
        throw new ServiceException("登录失败："+message);
    }

    private HttpResponse itunesLogin(T accountModel,String url,Integer attempt){
        String guid = accountModel.getGuid();
        String account = ((SimpleStringProperty) ReflectUtil.getFieldValue(accountModel, "account")).getValue();
        String pwd     = ((SimpleStringProperty) ReflectUtil.getFieldValue(accountModel, "pwd")).getValue();

        String signatureUrl = "http://192.168.31.68:9000/signature";
//        HttpResponse actionSignatureRsp = ITunesUtil.actionSignature(account, pwd, accountModel.getAuthCode(), guid, signatureUrl);
//        String signature = actionSignatureRsp.body();
        String signature = "";
//        System.err.println("【签名✍️✍️✍️✍️✍️】" + signature);
        HttpResponse authRsp = ITunesUtil.authenticate(account, pwd, accountModel.getAuthCode(), guid,signature, url);
        url = authRsp.header("location");
        String status = String.valueOf(authRsp.getStatus());
        if (status.equals(Constant.REDIRECT_CODE)){
            return itunesLogin(accountModel,url,1);
        }else if (!status.equals(Constant.SUCCESS)){
            return authRsp;
        }

        JSONObject authBody        = PListUtil.parse(authRsp.body());
        String     failureType     = authBody.getStr("failureType","");
        // 重试
        if(attempt == 0 && Constant.FailureTypeInvalidCredentials.equals(failureType)){
            return itunesLogin(accountModel,url,1);
        }
        // 双重认证
        Map<String,Object> result=ITunesUtil.checkLoginRes(authRsp.body());
        if(Constant.TWO_FACTOR_AUTHENTICATION.equals(result.get("code"))){
            accountModel.getAuthData().put("authRsp",authRsp);
            accountModel.setItspod(authRsp.header(Constant.ITSPOD));
            throw new ServiceException(MapUtil.getStr(result,"msg"));
        }else if(!Constant.SUCCESS.equals(result.get("code"))){
            throw new ServiceException(MapUtil.getStr(result,"msg"));
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

package com.sgswit.fx.controller.common;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.utils.ITunesUtil;
import com.sgswit.fx.utils.PListUtil;
import com.sgswit.fx.utils.StringUtils;
import javafx.beans.property.SimpleStringProperty;
import java.net.URL;
import java.util.ResourceBundle;

public class ItunesView<T> extends TableView<T> {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }

    /**
     * iTunes登陆鉴权
     */
    public HttpResponse itunesLogin(T accountModel,String guid,Boolean show2FADialog){
        String url = "https://buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/authenticate?guid="+guid;
        return itunesLogin(accountModel,"",guid,url,show2FADialog,0);
    }
    private HttpResponse itunesLogin(T accountModel, String authCode, String guid, String url, Boolean show2FADialog, Integer attempt){
        String account = ((SimpleStringProperty) ReflectUtil.getFieldValue(accountModel, "account")).getValue();
        String pwd     = ((SimpleStringProperty) ReflectUtil.getFieldValue(accountModel, "pwd")).getValue();
        HttpResponse authRsp = ITunesUtil.authenticate(account, pwd, authCode, guid, url);

        url = authRsp.header("location");

        int status = authRsp.getStatus();
        if (status == 302){
            return itunesLogin(accountModel, authCode,guid,url,show2FADialog,1);
        }
        
        if (status != 200){
            return authRsp;
        }

        JSONObject authBody        = PListUtil.parse(authRsp.body());
        String     failureType     = authBody.getStr("failureType","");
        String     customerMessage = authBody.getStr("customerMessage","");

        // 重试
        if(attempt == 0 && Constant.FailureTypeInvalidCredentials.equals(failureType)){
            return itunesLogin(accountModel, authCode,guid,url,show2FADialog,1);
        }

        if(!"".equals(failureType) && !"".equals(customerMessage)){
            return authRsp;
        }

        if(!"".equals(failureType)){
            return authRsp;
        }

        // 双重认证
        if(attempt == 0 && "".equals(failureType) && "".equals(authCode) && Constant.CustomerMessageBadLogin.equals(customerMessage)){
            if (show2FADialog){
                authCode = dialog("验证码","请输入双重验证码：");
                if (StrUtil.isEmpty(authCode)){
                    return authRsp;
                }
            }
            String itspod = authRsp.header(Constant.ITSPOD);
            url = "https://p"+ itspod +"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/authenticate?guid="+guid;
            return itunesLogin(accountModel, authCode,guid,url,show2FADialog,1);
        }



        return authRsp;
    }

    /**
     * 校验iTunes登陆成功与否,Model注入信息
     */
    public boolean itunesLoginVerify(HttpResponse authRsp,T account){
        int status = authRsp.getStatus();
        if (status == 503){
            setAndRefreshNote(account,"操作频繁!");
            return false;
        }

        if (authRsp == null || StrUtil.isEmpty(authRsp.body())){
            setAndRefreshNote(account,"AppleID或密码错误，或需输入双重验证码。");
            return false;
        }

        try {
            JSONObject json = PListUtil.parse(authRsp.body());
            String failureType     = json.getStr("failureType","");
            String customerMessage = json.getStr("customerMessage","");

            boolean verify = !(status != 200 || !StrUtil.isEmpty(failureType)  || !StrUtil.isEmpty(customerMessage));
            if (verify){
                setAndRefreshNote(account,"登陆成功。");
                return true;
            }
            if (!StrUtil.isEmpty(customerMessage)){
                if(customerMessage.contains("your account is disabled")) {
                    setAndRefreshNote(account,"出于安全原因，你的账户已被锁定。");
                }
                if(customerMessage.contains("You cannot login because your account has been locked")){
                    setAndRefreshNote(account,"帐户存在欺诈行为，已被【双禁】。");
                }
                if(Constant.CustomerMessageBadLogin.equals(customerMessage)){
                    setAndRefreshNote(account,"Apple ID或密码错误。或需要输入验证码！");
                }
                if(customerMessage.contains(Constant.CustomerMessageNotYetUsediTunesStore)){
                    setAndRefreshNote(account,"此 Apple ID 尚未用于 App Store。");
                }
                return false;
            }
            setAndRefreshNote(account,"登陆失败。");
            return false;

        }catch (Exception e){
            setAndRefreshNote(account,"登陆失败。");
            return false;
        }
    }

}

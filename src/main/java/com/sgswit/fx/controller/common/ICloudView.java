package com.sgswit.fx.controller.common;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.utils.ICloudUtil;
import com.sgswit.fx.utils.ICloudWeblogin;
import com.sgswit.fx.utils.ITunesUtil;
import com.sgswit.fx.utils.PListUtil;
import javafx.beans.property.SimpleStringProperty;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ICloudView<T> extends CustomTableView<T> {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }

    /**
     * iTunes登陆鉴权
     */
    public HttpResponse iCloudLogin(T accountModel){
        String account = ((SimpleStringProperty) ReflectUtil.getFieldValue(accountModel, "account")).getValue();
        String pwd     = ((SimpleStringProperty) ReflectUtil.getFieldValue(accountModel, "pwd")).getValue();

        String redirectUri = "https://www.icloud.com";
        String clientId = ICloudUtil.getClientId();

        Map<String,String> signInMap = new HashMap<>();
        signInMap.put("clientId",clientId);
        signInMap.put("redirectUri",redirectUri);
        signInMap.put("account",account);
        signInMap.put("pwd",pwd);

        //登录 通用 www.icloud.com
        setAndRefreshNote(accountModel,"开始签名");
        HttpResponse singInRes = ICloudWeblogin.signin(signInMap);
        // 设置账号对应的国家
        String domain = "icloud.com";
        //非双重认证账号，登录后，http code = 412；
        //        此时返回的header中 有 X-Apple-Repair-Session-Token ，无 X-Apple-Session-Token，
        //        需先进行处理后，才能返回X-Apple-Session-Token
        // 双重认证账号，登录成功后， http code = 409；
        //        但此时返回的header中包含 X-Apple-Session-Token，可直接使用获取icloud账户信息
        int status = singInRes.getStatus();

        if (status != 412 && status != 409){
            String errorMessages = serviceErrorMessages(singInRes.body());
            if (!StrUtil.isEmpty(errorMessages)){
                setAndRefreshNote(accountModel, errorMessages);
                return null;
            }

            Console.log("status: {}",status);
            setAndRefreshNote(accountModel,"签名失败; status=" + status);
            return null;
        }

        // 412普通登陆, 409双重登陆
        String sessionToken = "";
        HttpResponse singInLocalRes = singInRes;

        if (status == 412){
            HttpResponse repairRes = ICloudUtil.appleIDrepair(singInRes);
            //获取session-id，后续操作需基于该id进行处理
            String sessionId = ICloudUtil.getSessionId(repairRes);
            HttpResponse optionsRes = ICloudUtil.appleIDrepairOptions(singInRes,repairRes,clientId,sessionId);
            HttpResponse upgradeRes = ICloudUtil.appleIDUpgrade(singInRes,optionsRes,clientId,sessionId);
            HttpResponse setuplaterRes = ICloudUtil.appleIDSetuplater(singInRes,optionsRes,upgradeRes,clientId,sessionId);
            HttpResponse options2Res = ICloudUtil.appleIDrepairOptions2(singInRes,optionsRes,setuplaterRes,clientId,sessionId);
            HttpResponse completeRes = ICloudUtil.appleIDrepairComplete(singInRes,options2Res,clientId,sessionId);

            //处理后，获取account 信息
            sessionToken = completeRes.header("X-Apple-Session-Token");
            singInLocalRes = completeRes;
        }

        HttpResponse authRsp = null;
        if (!StrUtil.isEmpty(sessionToken)){
            authRsp = ICloudUtil.accountLogin(singInLocalRes, domain);
        }
        return authRsp;
    }

    /**
     * 校验iTunes登陆成功与否,Model注入信息
     */
    public boolean iCloudLoginVerify(HttpResponse authRsp,T accountModel){
        Boolean verify = authRsp != null && authRsp.getStatus() == 200;
        if (!verify){
            String errorMessages = serviceErrorMessages(authRsp.body());
            if (!StrUtil.isEmpty(errorMessages)){
                setAndRefreshNote(accountModel, errorMessages);
            }else{
                setAndRefreshNote(accountModel,"登陆失败");
            }
        }
        return verify;
    }

    private String serviceErrorMessages(String body){
        if (StrUtil.isEmpty(body)){
            return null;
        }
        List messageList = JSONUtil.parseObj(body).getByPath("serviceErrors.message", List.class);
        if (messageList == null){
            return null;
        }
        return String.join(";", messageList);
    }

}

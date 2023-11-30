package com.sgswit.fx.utils;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dd.plist.NSObject;
import com.dd.plist.XMLPropertyListParser;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.model.Account;

import java.util.HashMap;
import java.util.List;

/**
 *
 */
public class AppstoreUtil {

    public static void main(String[] args) throws Exception {
        Account account = new Account();
        account.setAccount("epine@163.com");
        account.setPwd("Jtsfh1982");

//        String guid = PropertiesUtil.getOtherConfig("guid");
        String guid     = "80732B71.C55244E6.71026BC4.99BDDA0D.3D524745.C7E6ABF9.F5DA58AB";
        HttpResponse authRsp = authenticate(account,guid);
        System.err.println(authRsp);

        if (authRsp != null && authRsp.getStatus() == 200){
            NSObject rspNO = XMLPropertyListParser.parse(authRsp.body().getBytes("UTF-8"));
            JSONObject rspJSON = (JSONObject) JSONUtil.parse(rspNO.toJavaObject());
            System.err.println(rspJSON);
//            String firstName = rspJSON.getByPath("accountInfo.address.firstName",String.class);
//            String lastName  = rspJSON.getByPath("accountInfo.address.lastName",String.class);
//            String creditDisplay  = rspJSON.getByPath("creditDisplay",String.class);
//            Boolean isDisabledAccount  = rspJSON.getByPath("accountFlags.isDisabledAccount",Boolean.class);
//            Console.log("Account firstName: {}, lastName:{}, creditDisplay:{}, isDisabledAccount:{}",firstName,lastName,creditDisplay,isDisabledAccount);
        }
    }

    /**
     * 鉴权
     */
    public static HttpResponse authenticate(Account account,String guid){
        String itspod = "";
        HttpResponse authRsp = authenticate(account, guid,itspod);
        if (authRsp.getStatus() == 302){
            authRsp = authenticate(account, guid,itspod);
        }
        String authBody = authRsp.charset("UTF-8").body();
        NSObject NSO = null;
        try {
            NSO = XMLPropertyListParser.parse(authBody.getBytes("UTF-8"));
        } catch (Exception e) {
            return authRsp;
        };

        JSONObject json = (JSONObject) JSONUtil.parse(NSO.toJavaObject());

        String failureType     = json.getStr("failureType","");
        String customerMessage = json.getStr("customerMessage","");

        if(Constant.FailureTypeInvalidCredentials.equals(failureType)){
            authRsp = authenticate(account, guid, itspod);
        }

        if (Constant.CustomerMessageBadLogin.equals(customerMessage)){
            itspod = "";
            itspod = "p" + authRsp.header(Constant.ITSPOD) + "-";
            authRsp = authenticate(account,guid,itspod);
        }

        if("".equals(failureType) || "".equals(customerMessage)){
            return authRsp;
        }

        return authRsp;
    }

    private static HttpResponse authenticate(Account account,String guid,String itspod){
        String authUrl = "https://%sbuy.itunes.apple.com/WebObjects/MZFinance.woa/wa/authenticate?guid=" + guid;
        authUrl = String.format(authUrl,itspod);
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", ListUtil.toList(ContentType.FORM_URLENCODED.getValue()));
        headers.put("User-Agent", ListUtil.toList("Configurator/2.15 (Macintosh; OS X 11.0.0; 16G29) AppleWebKit/2603.3.8"));

        String authBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">" +
                "<plist version=\"1.0\">" +
                "    <dict>" +
                "        <key>appleId</key>" +
                "        <string>%s</string>" +
                "        <key>attempt</key>" +
                "        <string>4</string>" +
                "        <key>createSession</key>" +
                "        <string>true</string>" +
                "        <key>guid</key>" +
                "        <string>%s</string>" +
                "        <key>password</key>" +
                "        <string>%s</string>" +
                "        <key>rmp</key>" +
                "        <string>0</string>" +
                "        <key>why</key>" +
                "        <string>signIn</string>" +
                "    </dict>" +
                "</plist>";
        authBody = String.format(authBody,account.getAccount(),guid,account.getPwd());
        HttpResponse authRsp = HttpUtil.createPost(authUrl)
                .header(headers)
                .body(authBody, ContentType.FORM_URLENCODED.getValue())
                .execute();
        return authRsp;
    }


}

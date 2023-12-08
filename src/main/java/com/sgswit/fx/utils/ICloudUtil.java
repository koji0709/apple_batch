package com.sgswit.fx.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.model.Account;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DeZh
 * @title: ICloudUtil
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/1320:38
 */
public class ICloudUtil {
    public static void main(String[] args) throws Exception {
//        HttpResponse response= checkCloudAccount(IdUtil.fastUUID().toUpperCase(),"1948401156@qq.com","B0527s0207!" );
        HttpResponse response= checkCloudAccount(IdUtil.fastUUID().toUpperCase(),"djli0506@163.com","!!B0527s0207!!" );
        getFamilyDetails(getAuthByHttResponse(response),"djli0506@163.com");
    }
    public static HttpResponse checkCloudAccount(String clientId, String appleId, String password){
        //clientId从数据库中获取每个appleId生成一个
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("setup.icloud.com"));
        headers.put("Referer", ListUtil.toList("https://setup.icloud.com/setup/iosbuddy/loginDelegates"));
        headers.put("Content-Type", ListUtil.toList("application/x-apple-plist; Charset=UTF-8"));
        headers.put("Accept", ListUtil.toList("*/*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language", ListUtil.toList(" zh-CN,zh;q=0.9,en;q=0"));

        String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<plist version=\"1.0\">" +
                "<dict>" +
                "<key>apple-id</key>" +
                "<string>"+appleId+"</string>" +
                "<key>client-id</key>" +
                "<string>"+ clientId +"</string>" +
                "<key>delegates</key>" +
                "<dict><key>com.apple.gamecenter</key>" +
                "<dict/>" +
                "<key>com.apple.mobileme</key>" +
                "<dict/>" +
                "<key>com.apple.private.ids</key>" +
                "<dict>" +
                "<key>protocol-version</key>" +
                "<string>8</string>" +
                "</dict> </dict>" +
                "<key>password</key>" +
                "<string>"+password+"</string>" +
                "</dict>" +
                "</plist>";
        HttpResponse response = HttpUtil.createPost("https://setup.icloud.com/setup/iosbuddy/loginDelegates")
                .header(headers)
                .body(body)
                .execute();
        return response;
    }

    public static String getAuthByHttResponse(HttpResponse response){
        String rb = response.charset("UTF-8").body();
        JSONObject rspJSON = PListUtil.parse(rb);
        String dsid = rspJSON.getStr("dsid");
        String mmeAuthToken= rspJSON.getJSONObject("delegates").getJSONObject("com.apple.mobileme").getByPath("service-data.tokens.mmeAuthToken",String.class);
        String auth = Base64.encode(dsid+":"+mmeAuthToken);
        return auth;
    }
    public static Map<String,Object> getFamilyDetails(String auth,String appleId){
        Map<String,Object> res=new HashMap<>();
        res.put("code","200");
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("setup.icloud.com"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language", ListUtil.toList("zh-cn"));
        headers.put("User-Agent", ListUtil.toList("%E8%AE%BE%E7%BD%AE/198 CFNetwork/1128.0.1 Darwin/19.6.0"));
        headers.put("X-MMe-LoggedIn-AppleID",ListUtil.toList(appleId));
        headers.put("Accept",ListUtil.toList("*/*"));
        headers.put("X-MMe-Client-Info",ListUtil.toList("<iPhone9,1> <iPhone OS;13.6;17G68> <com.apple.AppleAccount/1.0 (com.apple.Preferences/198)>"));
        headers.put("Authorization",ListUtil.toList("Basic "+auth));

        HttpResponse response = HttpUtil.createPost("https://setup.icloud.com/setup/family/getFamilyDetails")
                .header(headers)
                .execute();

        if(200==response.getStatus()){
            String rb = response.charset("UTF-8").body();
            JSONObject rspJSON = PListUtil.parse(rb);
            String dsid = rspJSON.getStr("dsid");
            res.put("dsid",dsid);
            boolean isMemberOfFamily = rspJSON.getByPath("is-member-of-family",Boolean.class);
            if(!isMemberOfFamily){
                res.put("familyDesc","未加入家庭共享");
            }else{
                //判断账户是否为组织者
                JSONArray array= JSONUtil.parseArray(rspJSON.getStr("family-members"));
                List<String> members=new ArrayList<>(array.size());
                for(Object object:array){
                    JSONObject jsonObject= (JSONObject) object;
                    String memberDisplayLabel=jsonObject.getStr("member-display-label");
                    String memberAppleId=jsonObject.getStr("member-apple-id");
                    if(appleId.equalsIgnoreCase(memberAppleId)){
                        members.add(memberDisplayLabel+"(此账号)");
                    }else{
                        members.add(memberDisplayLabel+"("+memberAppleId+")");
                    }
                }
                res.put("familyDesc",String.join("|",members));
            }
        }
        return res;


    }
}

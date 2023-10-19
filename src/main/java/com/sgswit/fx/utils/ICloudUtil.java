package com.sgswit.fx.utils;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;

import java.util.HashMap;
import java.util.List;

/**
 * @author DeZh
 * @title: ICloudUtil
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/1320:38
 */
public class ICloudUtil {

    public static void main(String[] args) {
//        loginCloud(IdUtil.fastUUID().toUpperCase(),"whjyvmbwyym@hotmail.com","Gao100287." );
//        loginCloud(IdUtil.fastUUID().toUpperCase(),"djli0506@163.com","@B0527s0207" );
        getFamilyDetails("djli0506@163.com","@B0527s0207" );
    }
    public static void loginCloud(String clientId,String appleId,String password){
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
        HttpResponse res = HttpUtil.createPost("https://setup.icloud.com/setup/iosbuddy/loginDelegates")
                .header(headers)
                .body(body)
                .execute();

        System.out.println(res.body());
    }
    public static void getFamilyDetails(String appleId,String password){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("setup.icloud.com"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language", ListUtil.toList(" zh-cn"));
//        headers.put("User-Agent", ListUtil.toList("%E8%AE%BE%E7%BD%AE/1.0 CFNetwork/711.2.23 Darwin/14.0.0"));
        headers.put("X-MMe-LoggedIn-AppleID",ListUtil.toList(appleId));
//        headers.put("X-Mme-Device-Id",ListUtil.toList("1862cdf18ff0c9eafe779607316445a42b1e91d6"));
//        headers.put("X-Apple-MD-M",ListUtil.toList("hjt4iCJS8FShZJLeRUFB4FXhptUdLQcE8qLV7XX+f9AqH3cliCfnatAsN7B5y5TGuF8i2ns7JV5vzDGP"));
//        headers.put("X-Apple-GS-Token",ListUtil.toList("ODEzNTQ0ODY1ODpHZHFEaDhSay93VDhma3VVc3c4ekhpbWVab3BHeHh1MVM2clROOHpRQld3SlJHQnh3RDcyTlNmVFg0WVlhT09JT2V3bVFmVnNTTzlKczRYUXd3eGJqTnVEQlQ2aDZ2VnRiN09BRFRScE5wYlp6dEVLdm1WQWxjbUZyZVlEYVl3d1htdElpOWpGTE9pMHVvdUlidEU5blNDUy9HSlRNREoxcWQ5d01FaUpJRnVZNWZZVWF6WDFqbXR6UjVDT0h4ODN6WlR5NnJNPQ=="));
//        headers.put("X-GS-Token",ListUtil.toList("ODEzNTQ0ODY1ODpHZHFEaDhSay93VDhma3VVc3c4ekhpbWVab3BHeHh1MVM2clROOHpRQld3SlJHQnh3RDcyTlNmVFg0WVlhT09JT2V3bVFmVnNTTzlKczRYUXd3eGJqTnVEQlQ2aDZ2VnRiN09BRFRScE5wYlp6dEVLdm1WQWxjbUZyZVlEYVl3d1htdElpOWpGTE9pMHVvdUlidEU5blNDUy9HSlRNREoxcWQ5d01FaUpJRnVZNWZZVWF6WDFqbXR6UjVDT0h4ODN6WlR5NnJNPQ=="));
//        headers.put("X-Apple-ADSID",ListUtil.toList("001871-10-204fce36-9315-42c7-9291-a020931cb9d3"));
//        headers.put("X-MMe-FMFAllowed",ListUtil.toList("true"));
        headers.put("Accept",ListUtil.toList("*/*"));
        headers.put("X-MMe-Client-Info",ListUtil.toList("<iPhone5,3> <iPhone OS;8.2;12D508> <com.apple.AppleAccount/1.0 (com.apple.Preferences/1.0)>"));
//        headers.put("X-Apple-MD",ListUtil.toList("AAAABAAAABBgyHg8C/0k0kaea2mlp8Tb"));
//        headers.put("X-MMe-Country",ListUtil.toList("CN"));
        headers.put("Authorization",ListUtil.toList("Basic ODEzNTQ0ODY1ODpJQUFBQUFBQUJMd0lBQUFBQUdVdDdEd1JEbWR6TG1samJHOTFaQzVoZFhSb3ZRQkZra1NROWJRT3hlUmxxWHVUOW43Z0kycEZDeHBLR200Ylc5NVRQZXREd3ZDbHVhc2tXa3ZtczdBczZ6V1k5T0RBdFRUNUVlczdOT1JSVTRJZk5HZ3FLRkJycHJ6OWFUT1lLNkpGQ1daOFZrQlkyelp6TmtBN2xyU2dhQ1lCMlNnTjZidDlIazZrL0tvV3N0UkJzd1ZNRElod21nPT0="));









        HttpResponse res = HttpUtil.createPost("https://setup.icloud.com/setup/family/getFamilyDetails")
                .header(headers)
                .execute();

        System.out.println(res.body());
    }

    private static class CN {
    }
}

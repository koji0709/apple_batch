package com.sgswit.fx.utils;

import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;

import java.util.Map;

public class HttpUtil{

    public static HttpResponse get(String url){
        url = getServiceUrl() + url;
        HttpResponse rsp = cn.hutool.http.HttpUtil.createGet(url).execute();
        Console.log("[GET] {}  Rsp status:{}",url,rsp.getStatus());
        return rsp;
    }

    public static HttpResponse get(String url, Map<String,Object> paramMap){
        url = getServiceUrl() + url;
        HttpResponse rsp = cn.hutool.http.HttpUtil.createGet(url).form(paramMap).execute();
        Console.log("[GET] {}  Rsp status:{}",url,rsp.getStatus());
        return rsp;
    }

    public static HttpResponse post(String url,Map<String,Object> paramMap){
        url = getServiceUrl() + url;
        HttpResponse rsp = cn.hutool.http.HttpUtil.createPost(url)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(paramMap))
                .execute();
        Console.log("[POST] {}  Rsp status:{}",url,rsp.getStatus());
        return rsp;
    }

    public static HttpResponse post(String url,String body){
        url = getServiceUrl() + url;
        HttpResponse rsp = cn.hutool.http.HttpUtil.createPost(url)
                .header("Content-Type", "application/json")
                .body(body)
                .execute();
        Console.log("[POST] {}  Rsp status:{}",url,rsp.getStatus());
        return rsp;
    }

    public static String getServiceUrl(){
        Setting config = new Setting("config.properties");
        return config.getStr("service.url");
    }

    public static boolean verifyRsp(HttpResponse rsp){
        return rsp.getStatus() == 200 && "200".equals(JSONUtil.parse(rsp.body()).getByPath("code",String.class));
    }

    public static String message(HttpResponse rsp){
        if (rsp.getStatus() != 200){
            return "系统异常！";
        }
        return JSONUtil.parse(rsp.body()).getByPath("msg",String.class);
    }

    public static JSONObject data(HttpResponse rsp){
        return JSONUtil.parse(rsp.body()).getByPath("data", JSONObject.class);
    }

    public static JSONArray dataList(HttpResponse rsp){
        return JSONUtil.parse(rsp.body()).getByPath("data", JSONArray.class);
    }
}

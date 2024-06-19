package com.sgswit.fx.utils;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.Map;

public class HttpUtils {

    public static HttpResponse get(String url){
        url = getServiceUrl() + url;
        HttpResponse rsp = HttpUtil.createGet(url).execute();
        return rsp;
    }

    public static HttpResponse get(String url, Map<String,Object> paramMap){
        url = getServiceUrl() + url;
        HttpResponse rsp = HttpUtil.createGet(url).form(paramMap).execute();
        return rsp;
    }

    public static HttpResponse post(String url,Map<String,Object> paramMap){
        String body=JSONUtil.toJsonStr(paramMap);
//        body=SM4Util.encryptBase64(body);
        url = getServiceUrl() + url;
        HttpResponse rsp = HttpUtil.createPost(url)
                .header("Content-Type", "application/json")
                .body(body)
                .setReadTimeout(30000)
                .execute();
        return rsp;
    }

    public static HttpResponse post(String url,String body){
//        body=SM4Util.encryptBase64(body);
        url = getServiceUrl() + url;
        HttpResponse rsp = HttpUtil.createPost(url)
                .header("Content-Type", "application/json")
                .body(body)
                .setReadTimeout(30000)
                .execute();
        return rsp;
    }

    public static String getServiceUrl(){
        return PropertiesUtil.getConfig("service.url");
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

    public static <T> T data(HttpResponse rsp,Class<T> clz){
        return JSONUtil.parse(rsp.body()).getByPath("data", clz);
    }
}

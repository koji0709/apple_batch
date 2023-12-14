package com.sgswit.fx.utils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 根据URL字符串解析参数信息
 * @author DeZh
 * @title: UrlParasUtil
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/11/2315:22
 */
public class UrlParasUtil {

    public static String getQueryParamsByKey(String urlString,String targetParam){
        String targetValue="";
        try{
            URL url=new URL(urlString);
            String queryString=url.getQuery();
            String[] params=queryString.split("&");
            for(String param:params){
                String[] keyValue = param.split("=");
                String key = keyValue[0];
                String value = keyValue[1];
                if (key.equals(targetParam)) {
                    targetValue = value;
                    break;
                }
            }
        }catch (Exception e){

        }
        return targetValue;
    }
    public static Map<String,String> getQueryParams(String urlString){
        Map<String,String> resMap=new HashMap<>();
        try{
            URL url=new URL(urlString);
            String queryString=url.getQuery();
            String[] params=queryString.split("&");
            for(String param:params){
                String[] keyValue = param.split("=");
                String key = keyValue[0];
                String value = keyValue[1];
                resMap.put("key",key);
                resMap.put("value",value);
            }

        }catch (Exception e){

        }
        return resMap;
    }
}

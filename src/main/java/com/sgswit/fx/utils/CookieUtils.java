package com.sgswit.fx.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpResponse;
import com.sgswit.fx.model.Account;

import java.util.HashMap;
import java.util.Map;

/**
 * @author DELL
 */
public class CookieUtils {

    public static String getCookiesFromHeader(HttpResponse res){
        Map<String,String> cookiesMap=new HashMap<>();
        if(null != res.headers().get("Set-Cookie")) {
            for (String c : res.headers().get("Set-Cookie")) {
                int split = c.indexOf(";");
                String[] items = c.substring(0, split).split("=");
                if (items.length < 2) {
                    continue;
                }
                cookiesMap.put(items[0],items[1]);
            }
        }
        if(null != res.headers().get("set-cookie")) {
            for (String c : res.headers().get("set-cookie")) {
                int split = c.indexOf(";");
                String[] items = c.substring(0, split).split("=");
                if (items.length < 2) {
                    continue;
                }
                cookiesMap.put(items[0],items[1]);
            }
        }
        return MapUtil.join(cookiesMap,";","=",true);
    }

    public static Map<String,String> setCookiesToMap(HttpResponse res,Map<String,String> cookiesMap){
        if(null != res.headers().get("Set-Cookie")) {
            for (String c : res.headers().get("Set-Cookie")) {
                int split = c.indexOf(";");
                String[] items = c.substring(0, split).replaceAll("==","\\|").split("=");
                if (items.length < 2) {
                    continue;
                }
                cookiesMap.put(items[0],items[1].replaceAll("\\|","=="));
            }
        }

        if(null != res.headers().get("set-cookie")) {
            for (String c : res.headers().get("set-cookie")) {
                int split = c.indexOf(";");
                String[] items = c.substring(0, split).replaceAll("==","\\|").split("=");
                if (items.length < 2) {
                    continue;
                }
                cookiesMap.put(items[0],items[1].replaceAll("\\|","=="));
            }
        }
        return cookiesMap;
    }

    public static void main(String[] args) {
        Map<String,String> cookiesMap=new HashMap<>();
        String cs="selfserv_toru=JZ4hCEh9xoQC6XhjDBU5I5xN5USXK/gSlDN81HmXg7EJwyLOq44YNiXSnNnRDY7upTZQx0wBvyW0HauSEKY4djaDgxu9jg==;";
        int split = cs.indexOf(";");
        String[] items = cs.substring(0, split).replaceAll("==","\\|").split("\\=");
        System.out.println(items[1].replaceAll("\\|","=="));
    }

}

package com.sgswit.fx.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpResponse;

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
                String cookieStr = c.substring(0, c.indexOf(";"));
                String[] items = cookieStr.split("=");
                if (items.length < 2) {
                    continue;
                }
                String value = cookieStr.substring(cookieStr.indexOf("=")+1);
                cookiesMap.put(items[0],value);
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
                String cookieStr = c.substring(0, c.indexOf(";"));
                String[] items = cookieStr.split("=");
                if (items.length < 2) {
                    continue;
                }
                String value = cookieStr.substring(cookieStr.indexOf("=")+1);
                cookiesMap.put(items[0],value);
            }
        }

        if(null != res.headers().get("set-cookie")) {
            for (String c : res.headers().get("set-cookie")) {
                String cookieStr = c.substring(0, c.indexOf(";"));
                String[] items = cookieStr.split("=");
                if (items.length < 2) {
                    continue;
                }
                String value = cookieStr.substring(cookieStr.indexOf("=")+1);
                cookiesMap.put(items[0],value);
            }
        }
        return cookiesMap;
    }


}

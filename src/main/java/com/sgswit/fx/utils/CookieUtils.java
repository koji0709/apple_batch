package com.sgswit.fx.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author DELL
 */
public class CookieUtils {

    public static String getSomeCookieFromHeader(HttpResponse res,String cookieName){
        if (res == null || StrUtil.isBlank(cookieName)) {
            return StrUtil.EMPTY;
        }
        Map<String,String> cookiesMap=new HashMap<>();
        setCookiesToMap(res,cookiesMap);
        return MapUtil.getStr(cookiesMap,cookieName,"");
    }

    public static String getCookiesFromHeader(HttpResponse res){
        if (res == null) {
            return StrUtil.EMPTY;
        }
        Map<String,String> cookiesMap=new HashMap<>();
        setCookiesToMap(res,cookiesMap);
        return MapUtil.join(cookiesMap,";","=",true);
    }

    public static Map<String,String> setCookiesToMap(HttpResponse res,Map<String,String> cookiesMap){
        if (res == null) {
            return cookiesMap != null ? cookiesMap : new HashMap<>();
        }

        if (cookiesMap == null) {
            cookiesMap = new HashMap<>();
        }
        extractCookiesFromHeaderList(res.headers().get("Set-Cookie"),cookiesMap);
        extractCookiesFromHeaderList(res.headers().get("set-cookie"),cookiesMap);
        return cookiesMap;
    }
    private static void extractCookiesFromHeaderList(List<String> headerValues, Map<String, String> cookiesMap) {
        if (headerValues == null || headerValues.isEmpty()) {
            return;
        }
        for (String cookieHeader : headerValues) {
            if (StrUtil.isBlank(cookieHeader)) {
                continue;
            }

            String cookieStr = cookieHeader.substring(0, cookieHeader.indexOf(";"));
            String[] items = cookieStr.split("=");
            if (items.length < 2) {
                continue;
            }
            String value = cookieStr.substring(cookieStr.indexOf("=")+1);
            cookiesMap.put(items[0],value);
        }
    }
}

package com.sgswit.fx.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpResponse;
import com.sgswit.fx.model.Account;

public class CookieUtils {

    public static void setCookies(HttpResponse res){
        if(null != res.headers().get("Set-Cookie")) {
            for (String c : res.headers().get("Set-Cookie")) {

                String cookieStr = c.substring(0, c.indexOf(";"));

                String[] items = cookieStr.split("=");
                if (items.length < 2) {
                    continue;
                }
                String value = cookieStr.substring(cookieStr.indexOf("=")+1);

                Account.getCookieMap().put(items[0],value);
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

                Account.getCookieMap().put(items[0],value);
            }
        }
    }

    public static String getCookies(){
        return  MapUtil.join(Account.getCookieMap(),";","=",true);
    }

}

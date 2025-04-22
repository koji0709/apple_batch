package com.sgswit.fx.utils;

import java.util.HashMap;
import java.util.Map;

public class LoginUtil {

    private static Map<String,String> cache = new HashMap<>();

    public static void set(String key,String value){
        cache.put(key,value);
    }

    public static void setUserName(String userName){
        LoginUtil.set("userName",userName);
    }

    public static void setPwd(String pwd){
        LoginUtil.set("pwd",pwd);
    }

    public static String getUserName(){
        return cache.get("userName");
    }

    public static String getPwd(){
        return cache.get("pwd");
    }

}

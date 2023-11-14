package com.sgswit.fx.setting;

import cn.hutool.setting.Setting;

/**
 * 登陆缓存工具类
 */
public class LoginSetting {

    private static final String PATH = System.getProperty("user.dir") + "/setting/login.setting";

    public static void clear(){
        Setting loginSetting = getLoginSetting();
        loginSetting.set("login.auto","false");
        loginSetting.set("login.rememberMe","false");
        loginSetting.set("login.userName","");
        loginSetting.set("login.pwd","");
        loginSetting.set("login.info","{}");
    }

    public static void store(Setting setting){
        setting.store(PATH);
    }

    public static Setting getLoginSetting(){
        Setting loginSetting = new Setting(PATH);
        return loginSetting;
    }

}

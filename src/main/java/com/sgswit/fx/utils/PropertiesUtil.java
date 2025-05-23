package com.sgswit.fx.utils;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @author DeZh
 * @title: PropertiesUtil
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/11/1420:48
 */
public class PropertiesUtil {
    private static String CUSTOMER_CONFIG = null;

    static {
        if(PropertiesUtil.getConfig("softwareInfo.platform","1").equals("2")){
            CUSTOMER_CONFIG= AppleBatchUtil.PATH + "config.ini";
        }else{
            CUSTOMER_CONFIG= "config.ini";
        }
        File file = new File(CUSTOMER_CONFIG);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean getConfigBool(String key,Boolean defaultValue) {
        boolean f;
        try {
            String res = getConfig(key);
            if(null==res || "".equals(res)){
                f= defaultValue;
            }else{
                f=Boolean.valueOf(res);
            }
        } catch (Exception e) {
            f=defaultValue;
        }
        return f;
    }
    public static String getConfig(String key,String defaultValue) {
        String res = null;
        try {
            Properties prop=new Properties();
            prop.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream("config.properties"), "UTF-8"));
            res = prop.getProperty(key,defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
    public static String getConfig(String key) {
        String res = null;
        try {
            res = getConfig(key, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }


    public static void setOtherConfig(String key, String value) {
        try {
            File file= new File(CUSTOMER_CONFIG);
            PropertiesConfiguration propsConfig = new PropertiesConfiguration(file);
            propsConfig.setAutoSave(true);
            propsConfig.setProperty(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getOtherConfig(String key) {
        String res = null;
        try {
            PropertiesConfiguration propsConfig = new PropertiesConfiguration(CUSTOMER_CONFIG);
            res = propsConfig.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static String getOtherConfig(String key,String defaultValue) {
        String res = null;
        try {
            res = StringUtils.isEmpty(getOtherConfig(key))?defaultValue:getOtherConfig(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
    /**
     * 获取Map指定key的值，并转换为Integer
     *
     * @param key 键
     * @return 值
     * @since 4.0.6
     */
    public static Integer getOtherInt(String key) {
        Integer res = null;
        try {
            PropertiesConfiguration propsConfig = new PropertiesConfiguration(CUSTOMER_CONFIG);
            if(StringUtils.isEmpty(propsConfig.getString(key))){
                res=0;
            }else{
                res=Integer.valueOf(propsConfig.getString(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static boolean getOtherBool(String key,boolean f) {
        boolean res = false;
        try {
            PropertiesConfiguration propsConfig = new PropertiesConfiguration(CUSTOMER_CONFIG);
            if(null==propsConfig.getString(key) || !Boolean.valueOf(propsConfig.getString(key))){
                res=f;
            }else{
                res=Boolean.valueOf(propsConfig.getString(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
}

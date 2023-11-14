package com.sgswit.fx.utils;

import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;

/**
 * @author DeZh
 * @title: PropertiesUtil
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/11/1420:48
 */
public class PropertiesUtil {
    private static final String CUSTOMER_CONFIG = "config.ini";
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

    public static void main(String[] args) {
        try {
            setOtherConfig("name2", "testError");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

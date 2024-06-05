package com.sgswit.fx.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author DeZh
 * @title: CustomStringUtil
 * @projectName appleBatch
 * @description: TODO
 * @date 2024/1/2211:35
 */
public class CustomStringUtils extends StringUtils {
    /**
    　*将多个连续空格替换为指定字符串 移除换行\r、回车\n、制表\t符的字符串
      * @param
     * @param str
     * @param str2
    　* @return java.lang.String
    　* @throws
    　* @author DeZh
    　* @date 2024/1/22 12:00
    */
    public static String replaceMultipleSpaces(String str,String str2){
        return str.replaceAll("[\\ \\t|\\r]+", str2);
    }
    /**
    　* 删除空白行
      * @param
     * @param content
    　* @return java.lang.String
    　* @throws
    　* @author DeZh
    　* @date 2024/6/5 18:22
    */
    public static String removeBlankLines(String content){
        String regex = "^\\s*\n";
        content= content.replaceAll(regex, "");
        return content;
    }
}

package com.sgswit.fx.utils;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DeZh
 * @title: CustomStringUtil
 * @projectName appleBatch
 * @description: TODO
 * @date 2024/1/2211:35
 */
public class StrUtils extends StringUtils {
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
    public static boolean isEmpty(Object o){
        if(null==o || "".equals(o)){
            return true;
        }
        return false;
    }
    /**
     * 礼品卡校验
     */
    public static boolean giftCardCodeVerify(String giftCardCode) {
        if(StrUtil.isEmpty(giftCardCode)){
            return false;
        }
        //判断礼品卡的格式是否正确
        String regex = "X[a-zA-Z0-9]{15}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(giftCardCode.toUpperCase());
        return matcher.matches();
    }
    public static String getScriptById(String html, String id) {
        // 解析 HTML
        Document doc = Jsoup.parse(html);
        Elements scripts = doc.select("script[id='"+id+"']");
        return scripts.html();
    }
    public static String getScriptByClass(String html, String className) {
        // 解析 HTML
        Document doc = Jsoup.parse(html);
        // 递归解析和处理 script 标签
       return processScripts(doc,className);
    }
    private static String processScripts(Element element,String className) {
        // 提取所有 script 标签
        Elements scripts = element.select("script");
        for (Element script : scripts) {
            // 检查 script 标签是否具有特定 class
            if (script.hasClass(className)) {
                // 打印具有特定 class 的 script 标签内容
                return script.html();
            }
            // 递归处理嵌套的 script 标签
            if (script.html().contains("<script")) {
                // 再次解析内部的 HTML
                Document innerDoc = Jsoup.parse(script.html());
               return processScripts(innerDoc,className);
            }
        }
        return "";
    }
    public static int getWeightedRandomIndex(int[] weights) {
        // 计算总权重
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }

        // 生成一个 [0, 总权重) 范围内的随机数
        Random random = new Random();
        int randomValue = random.nextInt(totalWeight);

        // 根据随机数和权重数组，选择一个索引
        int cumulativeWeight = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulativeWeight += weights[i];
            if (randomValue < cumulativeWeight) {
                return i;
            }
        }
        // 这种情况下不会到达这里，防止编译错误返回一个默认值
        return -1;
    }
    /**
     * 通用数据隐藏方法
     * @param data 原始数据（邮箱或手机号）
     * @return 部分隐藏后的数据
     */
    public static String maskData(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        // 判断是否为邮箱
        if (data.contains("@")) {
            return maskEmail(data);
        }
        // 判断是否为手机号（简单判断11位数字）
        else if (data.matches("\\d{11}")) {
            return maskPhone(data);
        }

        return data; // 其他格式不处理
    }

    /**
     * 隐藏邮箱
     */
    private static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) return email;

        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (username.length() <= 2) {
            return username + domain;
        } else {
            // 保留前2个字符和最后1个字符，中间用5个*代替
            return username.substring(0, 2) + "*****" +
                    (username.length() > 3 ? username.substring(username.length() - 1) : "") +
                    domain;
        }
    }
    /**
     * 隐藏手机号
     */
    /**
     * 智能隐藏手机号（动态保留前后位数）
     * @param phone 原始手机号
     * @return 部分隐藏后的手机号
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }
        int length = phone.length();
        // 根据长度动态确定保留位数
        int keepPrefix = Math.min(3, length / 2);  // 前保留位数
        int keepSuffix = Math.min(4, length - keepPrefix); // 后保留位数
        // 确保至少保留1位（防止keepSuffix为0）
        if (keepSuffix == 0) {
            keepPrefix = length - 1;
            keepSuffix = 1;
        }
        // 构建隐藏字符串
        String prefix = phone.substring(0, keepPrefix);
        String suffix = phone.substring(length - keepSuffix);

        return prefix + "****" + suffix;
    }
}

/**
 * Copyright (c) 2008, 邦泰联合（北京）科技有限公司
 * All rights reserved.
 * <p>
 * 文件名称：StringUtil.java
 * 摘   要：
 * 版   本：
 * 作   者：朱延超
 * 创建日期：2011-7-26
 */
package com.sgswit.fx.utils;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static Pattern p = Pattern.compile("\\s*|\t|\r|\n");

    /**
     * 半角转全角
     *
     * @param input
     *            String.
     * @return 全角字符串.
     */
    public static String ToSBC(String input) {
        char c[] = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == ' ') {
                c[i] = '\u3000';
            } else if (c[i] < '\177') {
                c[i] = (char) (c[i] + 65248);

            }
        }
        return new String(c);
    }

    /**
     * 全角转半角
     *
     * @param input
     *            String.
     * @return 半角字符串
     */
    public static String ToDBC(String input) {

        char c[] = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
//			if (c[i] == '\u3000') {
            if (c[i] == '\u3000' || c[i] == 160) {// 增加对&nbsp;的处理  modified by liuxinxing 20100203
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);

            }
        }
        String returnString = new String(c);

        return returnString;
    }

    public static String isNull(String str) {
        if (str == null || "".equals(str.trim()) || "undefined".equals(str)) {
            return null;
        }
        return str.trim();
    }

    public static boolean isEmpty(String str) {
        if (str == null || "".equals(str.trim()) || "undefined".equals(str)) {
            return true;
        }
        return false;
    }

    public static boolean isEmpty(Object object) {
        if (null == object || "".equals(object) || "undefined".equals(object)) {
            return true;
        }
        return false;
    }

    public static String nullConvertSpace(String str) {
        if (str == null || "null".equalsIgnoreCase(str.trim())) {
            str = "";
        }
        return str;
    }

    public static String spaceConvertNull(String str) {
        if (str == null || "".equals(str.trim()) || "null".equalsIgnoreCase(str.trim())) {
            str = null;
        }
        return str;
    }

    public static BigDecimal convertBigDecimal(String str) {
        if (str == null || "".equals(str.trim())) {
            str = null;
        }
        if (str != null) {
            BigDecimal bg = new BigDecimal(str);
            return bg;
        } else {
            return null;
        }

    }

    public static Integer convertInteger(String str) {
        if (str == null || "".equals(str.trim())) {
            str = null;
        }
        if (str != null) {
            try {
                return Integer.parseInt(str);
            } catch (Exception e) {
                return 0;
            }
        } else {
            return 0;
        }

    }

    public static long convertLong(String str) {
        if (str == null || "".equals(str.trim())) {
            str = null;
        }
        if (str != null) {
            try {
                return Long.parseLong(str);
            } catch (Exception e) {
                return 0;
            }
        } else {
            return 0;
        }

    }

    public static int pageStringConvertInt(String pageStr) {
        return (null == pageStr || "".equals(pageStr)) ? 1 : Integer.parseInt(pageStr);
    }

    public static String replaceBlank(String str) {


        Matcher m = p.matcher(str);
        String after = m.replaceAll("");
        return after;

    }

    /**
     * 重命名，UUIU
     *
     * @param oleFileName
     * @return
     */
    public static String reloadFile(String oleFileName, String newName) {
        oleFileName = getFileName(oleFileName);
        if (isEmpty(oleFileName)) {
            return oleFileName;
        }
        //得到后缀
        if (oleFileName.indexOf(".") == -1) {
            //对于没有后缀的文件，直接返回重命名
            return newName;
        }
        String[] arr = oleFileName.split("\\.");
        // 根据uuid重命名图片
        String fileName = newName + "." + arr[arr.length - 1];

        return fileName;
    }

    /**
     * 把带路径的文件地址解析为真实文件名 /25h/upload/hc/1448089199416_06cc07bf-7606-4a81-9844-87d847f8740f.mp4 解析为 1448089199416_06cc07bf-7606-4a81-9844-87d847f8740f.mp4
     *
     * @param url
     */
    public static String getFileName(final String url) {
        if (isEmpty(url)) {
            return url;
        }
        String newUrl = url;
        newUrl = newUrl.split("[?]")[0];
        String[] bb = newUrl.split("/");
        String fileName = bb[bb.length - 1];
        return fileName;
    }

}

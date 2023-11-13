package com.sgswit.fx.utils;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 */
public class MD5Util {
    // 加密方法：接收一个字符串明文，返回使用 MD5 加密后的哈希值
    public static String encrypt(String plaintext){
        // 使用 MD5 算法创建 MessageDigest 对象
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            // 更新 MessageDigest 对象中的字节数据
            md.update(plaintext.getBytes());
            // 对更新后的数据计算哈希值，存储在 byte 数组中
            byte[] digest = md.digest();
            // 将 byte 数组转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            // 返回十六进制字符串
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;

    }
}

package com.sgswit.fx.utils;


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * @author DeZh
 * @title: AESUtil
 * @projectName appleBatchService
 * @description: TODO
 * @date 2024/8/268:37
 */
public class AesUtil {

    private static final String ALGORITHM = "AES";
    private static final String key = "zAB8#1tIx$knW9z0";

    /**
    　* 使用字符串密钥进行AES加密
      * @param
     * @param data
    　* @return java.lang.String
    　* @throws
    　* @author DeZh
    　* @date 2024/8/26 8:37
    */
    public static String encrypt(String data) throws Exception {
        // 将字符串密钥转换为SecretKeySpec
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);

        // 创建AES Cipher实例
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        // 初始化Cipher为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // 执行加密操作
        byte[] encryptedData = cipher.doFinal(data.getBytes());

        // 返回Base64编码的加密结果
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
    　* 使用字符串密钥进行AES解密
      * @param
     * @param encryptedData
    　* @return java.lang.String
    　* @throws
    　* @author DeZh
    　* @date 2024/8/26 8:38
    */
    public static String decrypt(String encryptedData) throws Exception {
        // 将字符串密钥转换为SecretKeySpec
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);

        // 创建AES Cipher实例
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        // 初始化Cipher为解密模式
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // 执行解密操作
        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));

        // 返回解密后的字符串
        return new String(decryptedData);
    }
    public static void main(String[] args) {
        try {
            // 明文数据
            String data = "Hello, World!";

            // 加密数据
            String encryptedData = AesUtil.encrypt(data);
            System.out.println("加密后的数据: " + encryptedData);

            // 解密数据
            String decryptedData = AesUtil.decrypt(encryptedData);
            System.out.println("解密后的数据: " + decryptedData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

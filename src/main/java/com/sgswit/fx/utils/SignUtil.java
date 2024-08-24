package com.sgswit.fx.utils;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.bouncycastle.util.encoders.Hex;

/**
 * @author DeZh
 * @title: SM4
 * @projectName appleBatch
 * @description: TODO
 * @date 2024/2/511:31
 */
public class SignUtil {
    private static final String key ="1012BADD542E2C182DFD84795E65A019";
    /**
    　* 加密后返回base64
      * @param
     * @param content
    　* @return java.lang.String
    */
    public static String encryptBase64(String content){
        try{
            SymmetricCrypto sm4 = SmUtil.sm4(Hex.decode(key));
            return sm4.encryptBase64(content);
        }catch (Exception e){
            return "";
        }
    }
    /**
     　* 解密base64
     * @param
     * @param encryptBase64
    　* @return java.lang.String
     */
    public static String decryptBase64(String encryptBase64){
        try {
            SymmetricCrypto sm4 = SmUtil.sm4(Hex.decode(key));
            return sm4.decryptStr(encryptBase64);
        }catch (Exception e){
            return "";
        }
    }
}

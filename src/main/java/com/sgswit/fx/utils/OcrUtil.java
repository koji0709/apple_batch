package com.sgswit.fx.utils;

import com.mmg.ddddocr4j.utils.DDDDOcrUtil;

/**
 * 打码工具
 */
public class OcrUtil {
    public static String recognize(String base64Str){
        try {
            String code = DDDDOcrUtil.getCode(base64Str);
            LoggerManger.info("【验证码】code："+code);
            return code;
        }catch (Exception e){
            LoggerManger.info("【验证码】异常："+e.getMessage());
            e.printStackTrace();
        }
        return "";
    }
}

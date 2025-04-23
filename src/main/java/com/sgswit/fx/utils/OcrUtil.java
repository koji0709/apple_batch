package com.sgswit.fx.utils;

import com.mmg.ddddocr4j.OCREngine;
import com.mmg.ddddocr4j.utils.DDDDOcrUtil;

import top.gcszhn.d4ocr.utils.IOUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 打码工具
 */
public class OcrUtil {

    public static String recognize(String base64Str) {
        try {
            String code = DDDDOcrUtil.getCode(base64Str);
            LoggerManger.info("【验证码】code：" + code);
            return code;
        } catch (Exception e) {
            LoggerManger.info("【验证码】异常：" + e.getMessage());
            e.printStackTrace();
        }

        return "";
    }

    public static void main(String[] args) throws IOException {
        OCREngine engine = OCREngine.instance();
        BufferedImage image = IOUtils.read("/Users/koji/Downloads/a.jpeg");
        String preidct = engine.recognize(image);
        System.err.println(preidct);
    }
    
}

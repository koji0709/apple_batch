package com.sgswit.fx.utils;

import com.dd.plist.Base64;
import com.mmg.ddddocr4j.OCREngine;
import com.mmg.ddddocr4j.utils.DDDDOcrUtil;

import top.gcszhn.d4ocr.utils.IOUtils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 打码工具
 */
public class OcrUtil {

    public static String recognize(String base64Str) {
        String code = "";
        try {
            code = DDDDOcrUtil.getCode(base64Str);
            LoggerManger.info("【验证码】code：" + code);
            return code;
        } catch (Exception e) {
            LoggerManger.info("【验证码】异常：" + e.getMessage());
            e.printStackTrace();
        }

        if ("".equals(code)){
            LoggerManger.info("验证码解析异常, 切换解析工具");
            try {
                code = recognize2(base64Str);
                LoggerManger.info("【验证码】code：" + code);
                return code;
            } catch (Exception e) {
                LoggerManger.info("【验证码】异常：" + e.getMessage());
                e.printStackTrace();
            }
        }

        return code;
    }

    public static String recognize2(String base64Str){
        OCREngine engine = OCREngine.instance();
        String predict = null;
        try {
            predict = engine.recognize(toBufferedImage(base64Str));
        } catch (IOException e) {
            return "";
        }
        return predict;
    }

        public static BufferedImage toBufferedImage(String base64Str) throws IOException {
        byte[] decodedBytes = Base64.decode(base64Str);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedBytes));
        return image;
    }

    public static void main(String[] args) throws IOException {
        OCREngine engine = OCREngine.instance();
        BufferedImage image = IOUtils.read("/Users/koji/Downloads/a.jpeg");
        String preidct = engine.recognize(image);
        System.err.println(preidct);
    }
    
}

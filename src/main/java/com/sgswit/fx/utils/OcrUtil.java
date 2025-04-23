package com.sgswit.fx.utils;

import com.dd.plist.Base64;
import com.mmg.ddddocr4j.utils.DDDDOcrUtil;

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

        return code;
    }

    public static BufferedImage toBufferedImage(String base64Str) throws IOException {
        byte[] decodedBytes = Base64.decode(base64Str);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedBytes));
        return image;
    }
    
}

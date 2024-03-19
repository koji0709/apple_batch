package com.sgswit.fx.utils;

import com.dd.plist.Base64;
import top.gcszhn.d4ocr.OCREngine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 打码工具
 */
public class OcrUtil {
    public static String recognize(String base64Str){
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

}

package com.sgswit.fx.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.XMLPropertyListParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;

public class PListUtil {
    public static JSONObject parse(String body){
        NSObject rspNO = null;
        try {
            rspNO = XMLPropertyListParser.parse(body.getBytes("UTF-8"));
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (PropertyListFormatException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JSONObject rspJSON = (JSONObject) JSONUtil.parse(rspNO.toJavaObject());
        return rspJSON;
    }
}

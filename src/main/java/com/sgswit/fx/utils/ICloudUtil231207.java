package com.sgswit.fx.utils;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DeZh
 * @title: ICloudUtil
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/1320:38
 */
public class ICloudUtil231207 {

    public static void main(String[] args) throws Exception {
//        checkCloudAccount(IdUtil.fastUUID().toUpperCase(),"whjyvmbwyym@hotmail.com","Gao100287." );
//        checkCloudAccount(IdUtil.fastUUID().toUpperCase(),"djli0506@163.com","!!B0527s0207" );
//        checkCloudAccount(IdUtil.fastUUID().toUpperCase(),"qinqian@163.com","!!B0527s0207" );
//        loginCloud(IdUtil.fastUUID().toUpperCase(),"qewqeq@2980.com","dPFb6cSD" );
//        loginCloud(IdUtil.fastUUID().toUpperCase(),"shabagga222@tutanota.com","Xx97595031..2" );
//        getFamilyDetails("qianqian@163.com","!!B0527s0207" );
//        checkAccountInit("djli0506@163.com");
        checkAccountInit("17319139610");
    }
    public static HttpResponse checkCloudAccount(String clientId, String appleId, String password){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("setup.icloud.com"));
        headers.put("Referer", ListUtil.toList("https://setup.icloud.com/setup/iosbuddy/loginDelegates"));
        headers.put("Content-Type", ListUtil.toList("application/x-apple-plist; Charset=UTF-8"));
        headers.put("Accept", ListUtil.toList("*/*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language", ListUtil.toList(" zh-CN,zh;q=0.9,en;q=0"));

        String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<plist version=\"1.0\">" +
                "<dict>" +
                "<key>apple-id</key>" +
                "<string>"+appleId+"</string>" +
                "<key>client-id</key>" +
                "<string>"+ clientId +"</string>" +
                "<key>delegates</key>" +
                "<dict><key>com.apple.gamecenter</key>" +
                "<dict/>" +
                "<key>com.apple.mobileme</key>" +
                "<dict/>" +
                "<key>com.apple.private.ids</key>" +
                "<dict>" +
                "<key>protocol-version</key>" +
                "<string>8</string>" +
                "</dict> </dict>" +
                "<key>password</key>" +
                "<string>"+password+"</string>" +
                "</dict>" +
                "</plist>";
        HttpResponse response = HttpUtil.createPost("https://setup.icloud.com/setup/iosbuddy/loginDelegates")
                .header(headers)
                .body(body)
                .execute();
        return response;
    }
    public static void getFamilyDetails(String appleId, String password){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("setup.icloud.com"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language", ListUtil.toList("zh-cn"));
        headers.put("User-Agent", ListUtil.toList("%E8%AE%BE%E7%BD%AE/198 CFNetwork/1128.0.1 Darwin/19.6.0"));
        headers.put("X-MMe-LoggedIn-AppleID",ListUtil.toList(appleId));
        headers.put("Accept",ListUtil.toList("*/*"));
        headers.put("X-MMe-Client-Info",ListUtil.toList("<iPhone9,1> <iPhone OS;13.6;17G68> <com.apple.AppleAccount/1.0 (com.apple.Preferences/198)>"));
        headers.put("Authorization",ListUtil.toList("Basic MjUwMzY4MjIwMDQ6SUFBQUFBQUFCTHdJQUFBQUFGOHIxN01SRG1kekxtbGpiRzkxWkM1aGRYUm92UUM3WWl4aDhsL2NyMzV6Y0VObXgwZ1hEaWdNMWhEMmtDd0piQVp3bU0zUXJ6K0NBdTdjK3U3eEJ5WjVOU3ZvbWdNS1kyMHY5RzcvTlhMbmNPUDlWZlVacUNuazl3RVhnUDluS3dxYlAvV0lFZis5TkxmU1c1WDNvZjF1eHNidlowdVhzZ09od1MwMXVzRTdKOVNuSDMyMUpMeTBVZz09"));
        HttpResponse res = HttpUtil.createPost("https://setup.icloud.com/setup/family/getFamilyDetails")
                .header(headers)
                .execute();

        System.out.println(res.body());
    }

    public static String checkAccountInit(String appleId) throws Exception {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("gsa.apple.com"));
        headers.put("User-Agent", ListUtil.toList("%E8%AE%BE%E7%BD%AE/198 CFNetwork/1128.0.1 Darwin/19.6.0" ));
        headers.put("X-MMe-Country", ListUtil.toList("CN" ));
        headers.put("X-MMe-Client-Info", ListUtil.toList("<iPhone9,1> <iPhone OS;13.6;17G68> <com.apple.AuthKit/1 (com.apple.Preferences/198)>" ));
        headers.put("Accept-Language", ListUtil.toList("zh-cn" ));
        headers.put("Accept", ListUtil.toList("application/x-buddyml" ));
        headers.put("Content-Type", ListUtil.toList("application/x-plist" ));
        headers.put("X-Apple-I-MD-M", ListUtil.toList("klvRjMAEBGIwke/kP/YdF8AljjUZW7WPqjAwit7nVp3yHNMQqI+gyhlXXLQS6TSY72HiF2Avgg3H/2Tf" ));
        headers.put("X-Apple-I-MD", ListUtil.toList("AAAABQAAABDmJI5l56oUbPWpTfvhzXW4AAAAAw=="));
        HttpResponse initRes = HttpUtil.createGet("https://gsa.apple.com/iforgot/password/verify/appleid")
                .header(getHeader())
                .execute();
        if(initRes.getStatus()!=200){
            return "系统错误";
        }else{
            return checkAccount(appleId,initRes);
        }
    }
    public static String checkAccount(String appleId, HttpResponse response) throws Exception {
        String message="";

        String text=response.body();
        ////正则表达式提取链接
        String LINK_REGEX = "<linkBarItem\\s+(?:[^>]*?\\s+)?url=\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(LINK_REGEX);
        Matcher matcher = pattern.matcher(text);
        String checkUrl="https://gsa.apple.com";
        while (matcher.find()) {
            checkUrl+=matcher.group(1);
        }
        String body="<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">" +
                "<plist version=\"1.0\">" +
                "<dict>" +
                "<key>id</key>" +
                "<string>"+appleId+"</string>" +
                "</dict>" +
                "</plist>";

        HttpResponse res = HttpUtil.createPost(checkUrl)
                .header(getHeader())
                .body(body)
                .execute();









//        if(res.getStatus()==200){
//            String MESSAGE_REGEX = "<alert\\s+(?:[^>]*?\\s+)?message=\"([^\"]*)\"";
//            Pattern p = Pattern.compile(MESSAGE_REGEX);
//            Matcher m = p.matcher(res.body());
//            while (m.find()) {
//                message=matcher.group(1);
//            }
//            Console.error(message);
//        }else if(res.getStatus()==302){
//            System.out.println("----开始-------");
//            System.out.println(res.header("Location"));
//            System.out.println("----结束-------");
//        }




        System.out.println(res.getStatus());
        System.out.println(res.body());

        return message;
    }
    private static Map<String, List<String>> getHeader(){
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("gsa.apple.com"));
        headers.put("X-Apple-iOS-SLA-Version", ListUtil.toList("1636" ));
        headers.put("X-Apple-I-Locale", ListUtil.toList("zh_CN" ));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br" ));
        headers.put("User-Agent", ListUtil.toList("%E8%AE%BE%E7%BD%AE/198 CFNetwork/1128.0.1 Darwin/19.6.0" ));
        headers.put("X-MMe-Country", ListUtil.toList("CN" ));
        headers.put("X-MMe-Client-Info", ListUtil.toList("<iPhone9,1> <iPhone OS;13.6;17G68> <com.apple.AuthKit/1 (com.apple.Preferences/198)>" ));
        headers.put("Accept-Language", ListUtil.toList("zh-cn" ));
        headers.put("Accept", ListUtil.toList("application/x-buddyml" ));
        headers.put("Content-Type", ListUtil.toList("application/x-plist" ));
        headers.put("X-Apple-I-MD-M", ListUtil.toList("klvRjMAEBGIwke/kP/YdF8AljjUZW7WPqjAwit7nVp3yHNMQqI+gyhlXXLQS6TSY72HiF2Avgg3H/2Tf" ));
        headers.put("X-Apple-I-MD", ListUtil.toList("AAAABQAAABDmJI5l56oUbPWpTfvhzXW4AAAAAw=="));
        return headers;
    }
}

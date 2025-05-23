package com.sgswit.fx.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.constant.StoreFontsUtils;
import com.sgswit.fx.utils.proxy.ProxyUtil;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.util.DigestFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseBillUtil {

    public static void main(String[] args ) throws Exception {

        
        String url="https://play.itunes.apple.com/WebObjects/MZPlay.woa/wa/signSapSetup";
        String body="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                "<plist version=\"1.0\">\n" +
                "<dict>\n" +
                "\t<key>sign-sap-setup-buffer</key>\n" +
                "\t<data>\n" +
                "AnZ4Hj9lnZTXlUd6PodSFuwOX9XiqIVb4X97k3lXJWJvAAAB0AMAAAACAAABAKvN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76vN76sAAAAeeJfJJ89t2u89s2vxAMJBUaK5ImgZUOHkTpJ3k3JLAAAAnwFS6jUy6SO/LE9fBSmftJUoR2387QAAAIYJAcmjalt4Rbs5o/KlRllNpuxm4+SyWBolXso7uGv5Mc9HwpM6tXHlRl7LiSOYRPxQxl4EcwMzL1zOaRvDbPQB6GNfyzfbMkPa/S3BBIU3RIjKXIEowSIFIyMT0/hZmk0BrtYD714aMjNXnbEnhXZpJTCzyBzSZiRHU3PllJUcSfGKtVbJHgAA%" +
                "\t</data>\n" +
                "</dict>\n" +
                "</plist>";

        HttpResponse execute = HttpUtil.createPost(url).body(body).execute();
        System.err.println(execute);


//        HttpResponse step4Res = ProxyUtil.createRequest(Method.POST,url)
//                .body(body)
//                .execute();

        //Map<String,Object> res= iTunesAuth("3406858043@qq.com","B0527s0207");
//        ITunesUtil.getPaymentInfos(res);
    }
    ///网页版版
    public static Map<String,Object> webLoginAndAuth(String account,String pwd){
        Map<String,Object>  result=new HashMap<>();
        result.put("code",Constant.SUCCESS);
        String error="";
        HttpResponse pre1Response = shopPre1();
        if(pre1Response.getStatus() != 302){
            result.put("code","1");
            result.put("msg",error);
            return result;
        }
        String requestUrl = pre1Response.header("Location");
        Map<String,String> stringStringMap = HttpUtil.decodeParamMap(requestUrl, StandardCharsets.UTF_8);
        String clientId=stringStringMap.get("appIdKey");
        HttpResponse pre2Response = shopPre2(pre1Response);
        Map<String,Object> jx=jXDocument(pre1Response);
        String a=jx.get("a").toString();
        BigInteger n=new BigInteger(jx.get("n").toString());
        BigInteger ra=new BigInteger(jx.get("ra").toString());
        BigInteger g=new BigInteger(jx.get("g").toString());
        String frameId=jx.get("frameId").toString();
        String locationBase=jx.get("locationBase").toString();

        HttpResponse step0Res = federate(account,frameId,clientId, locationBase);

        HttpResponse step1Res = signinInit(account,a,frameId,clientId,locationBase,step0Res);
        if(step1Res.getStatus()!=200){
            result.put("code","1");
            result.put("msg","错误码："+step1Res.getStatus());
            return result;
        }
        HttpResponse step2Res = signinCompete(account,pwd,a,g,n,ra,step1Res,pre2Response,frameId,clientId,locationBase);

        if(null!=JSONUtil.parse(step2Res.body()).getByPath("serviceErrors")){
            JSON json = JSONUtil.parse(step2Res.body());
            error=json.getByPath("serviceErrors.message").toString();
            result.put("code","1");
            result.put("msg",error);
            return result;
        }else{
            JSON json = JSONUtil.parse(step2Res.body());
            String authType = (String)json.getByPath("authType");
            if ("hsa2".equals(authType)) {
                error="该账户为双重认证模式";
                result.put("code","1");
                result.put("msg",error);
                return result;
            }
        }
        HttpResponse step212Res =accountRepair(step2Res);
        String XAppleIDSessionId = "";
        String scnt = step212Res.header("scnt");
        List<String> cookies = step212Res.headerList("Set-Cookie");
        for (String item : cookies) {
            if (item.startsWith("aidsp")) {
                XAppleIDSessionId = item.substring(item.indexOf("aidsp=") + 6, item.indexOf("; Domain=appleid.apple.com"));
            }
        }
        HttpResponse step213Res =repareOptions(step2Res, step212Res);
        HttpResponse step214Res = securityUpgrade(step213Res, XAppleIDSessionId, scnt);

        HttpResponse step215Res = securityUpgradeSetuplater(step214Res, XAppleIDSessionId, scnt);
        HttpResponse step216Res = repareOptionsSecond(step215Res, XAppleIDSessionId, scnt);
        HttpResponse step22Res = repareComplete(step216Res, step2Res,frameId);
        Map<String,Object> loginResult= webLogin(pre1Response,step22Res);
        if(!loginResult.get("code").equals(Constant.SUCCESS)){
            result.put("code",loginResult.get("code"));
            result.put("msg",loginResult.get("msg"));
            return result;
        }
        result.put("loginResult",loginResult);
        return result;
    }

    public static Map<String,Object> jXDocument(HttpResponse pre1){
        Map<String,Object> res=new HashMap<>();
        String frameId  = createFrameId();
        String location = pre1.header("Location");
        String locationBase = "https://idmsa.apple.com/";
        // get x-apple-hc
        HttpResponse pre4 = signFrame(frameId,location,locationBase);

        //step1  signin
        String nHex = "AC6BDB41324A9A9BF166DE5E1389582FAF72B6651987EE07FC3192943DB56050A37329CBB4A099ED8193E0757767A13DD52312AB4B03310DCD7F48A9DA04FD50E8083969EDB767B0CF6095179A163AB3661A05FBD5FAAAE82918A9962F0B93B855F97993EC975EEAA80D740ADBF4FF747359D041D5C33EA71D281E446B14773BCA97B43A23FB801676BD207A436C6481F1D2B9078717461A5B9D32E688F87748544523B524B0D57D5EA77A2775D2ECFA032CFBDBF52FB3786160279004E57AE6AF874E7303CE53299CCC041C7BC308D82A5698F3A8D0C38271AE35F8E9DBFBB694B5C803D89F7AE435DE236D525F54759B65E372FCD68EF20FA7111F9E4AFF73";
        BigInteger n = new BigInteger(nHex,16);
        BigInteger g = new BigInteger("2");

        byte[] rb = RandomUtil.randomBytes(32);
        BigInteger ra = new BigInteger(1,rb);
        String a = calA(ra,n);
        res.put("g",g);
        res.put("n",n);
        res.put("ra",ra);
        res.put("a",a);
        res.put("frameId",frameId);
        res.put("location",location);
        res.put("locationBase",locationBase);
        return res;
    }
    public static HttpResponse shopPre1(){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Referer", ListUtil.toList("https://www.apple.com/"));
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        String url="https://reportaproblem.apple.com/";
        HttpResponse res = ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers));
        return res;
    }

    public static HttpResponse shopPre2(HttpResponse pre1){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Referer", ListUtil.toList("https://www.apple.com/"));
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        HttpRequest httpRequest=HttpUtil.createGet(pre1.header("Location"))
                .header(headers);
        HttpResponse res = ProxyUtil.execute(httpRequest);
        return res;
    }


    private static HttpResponse signFrame(String frameId,String clientId, String locationBase){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Host",ListUtil.toList("idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList(locationBase));
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        String url = "https://idmsa.apple.com/appleauth/auth/authorize/signin?frame_id="+frameId+"&language=en_US&skVersion=7&iframeId="+frameId
                +"&client_id="+clientId+"&redirect_uri="+locationBase+"&response_type=code&response_mode=web_message" +
                "&state="+frameId+"&authVersion=latest";

        HttpResponse res = ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers));
        return res;
    }

    public static HttpResponse federate(String account,String frameId,String clientId, String locationBase){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("X-Apple-Domain-Id", ListUtil.toList("35"));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(frameId));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));

        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));

        headers.put("X-Apple-0Auth-Client-Id",ListUtil.toList(clientId));
        headers.put("X-Apple-OAuth-State",ListUtil.toList(frameId));
        headers.put("X-Apple-OAuth-Redirect-URI",ListUtil.toList(locationBase));
        headers.put("X-Apple-OAuth-Response-Type",ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-Response-Mode",ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-Client-Type",ListUtil.toList("firstPartyAuth"));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        String body = "{\"accountName\":\""+account+"\",\"rememberMe\":false}";
        HttpRequest httpRequest=HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/federate?isRememberMeEnabled=true")
                .header(headers)
                .body(body);
        HttpResponse res = ProxyUtil.execute(httpRequest);
        return res;
    }

    public static HttpResponse signinInit(String account,String a ,String frameId,String clientId, String locationBase,HttpResponse res1){
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("X-Apple-Frame-Id", ListUtil.toList(frameId));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));
        headers.put("X-Apple-Trusted-Domain", ListUtil.toList("https://idmsa.apple.com"));

        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));

        headers.put("X-Apple-0Auth-Client-Id",ListUtil.toList(clientId));
        headers.put("X-Apple-OAuth-State",ListUtil.toList(frameId));
        headers.put("X-Apple-OAuth-Redirect-URI",ListUtil.toList(locationBase));
        headers.put("X-Apple-OAuth-Response-Type",ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-Response-Mode",ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-Client-Type",ListUtil.toList("firstPartyAuth"));

        headers.put("scnt",ListUtil.toList(res1.header("scnt")));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        String body = "{\"a\":\""+a+"\",\"accountName\":\""+account+"\",\"protocols\":[\"s2k\",\"s2k_fo\"]}";
        HttpRequest httpRequest=HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/signin/init")
                .header(headers)
                .body(body);
        HttpResponse res = ProxyUtil.execute(httpRequest);
        return res;
    }

    public static HttpResponse signinCompete(String account,String pwd,String a,BigInteger g,BigInteger n,BigInteger ra,HttpResponse res1,HttpResponse pre2,String frameId,String clientId, String locationBase){

        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("X-Apple-Locale", ListUtil.toList("CN-ZH"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("X-Apple-Domain-Id", ListUtil.toList("35"));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(frameId));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));

        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));

        headers.put("X-Apple-0Auth-Client-Id",ListUtil.toList(clientId));
        headers.put("X-Apple-OAuth-State",ListUtil.toList(frameId));
        headers.put("X-Apple-OAuth-Redirect-URI",ListUtil.toList(locationBase));
        headers.put("X-Apple-OAuth-Response-Type",ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-Response-Mode",ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-Client-Type",ListUtil.toList("firstPartyAuth"));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(res1.header("X-Apple-ID-Session-Id")));
        headers.put("scnt",ListUtil.toList(res1.header("scnt")));

        JSON json = JSONUtil.parse(res1.body());

        int iter = (Integer) json.getByPath("iteration");
        String salt = (String)json.getByPath("salt");
        String b = (String) json.getByPath("b");
        String c = (String)json.getByPath("c");

        Map map = calM(account, pwd, a, iter, salt, b, g, n, ra);
        Map<String,Object> paras=new HashMap<>(){{
            put("accountName",account);
            put("rememberMe",false);
            put("m1",map.get("m1"));
            put("c",c);
            put("m2",map.get("m2"));
        }};
        StringBuilder cookieBuilder = new StringBuilder();
        List<String> res1Cookies = res1.headerList("Set-Cookie");
        for(String item : res1Cookies){
            cookieBuilder.append(";").append(item);
        }

        List<String> pre3Cookies = pre2.headerList("Set-Cookie");
        for(String item : pre3Cookies){
            cookieBuilder.append(";").append(item);
        }

        HttpRequest httpRequest=HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/signin/complete?isRememberMeEnabled=true")
                .header(headers)
                .body(JSONUtil.toJsonStr(paras))
                .cookie(cookieBuilder.toString());
        HttpResponse res = ProxyUtil.execute(httpRequest);
        return res;
    }

    public static HttpResponse accountRepair(HttpResponse res1) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("Sec-Fetch-Dest", ListUtil.toList("iframe"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("navigate"));
        headers.put("navigate", ListUtil.toList("same-site"));

        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Upgrade-Insecure-Requests", ListUtil.toList("1"));
        headers.put("User-Agent", ListUtil.toList("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/118.0"));
        String location = res1.header("Location");
        HttpRequest httpRequest=HttpUtil.createGet("https://appleid.apple.com/widget/account/repair?trustedWidgetDomain=https%3A%2F%2Fidmsa.apple.com&widgetKey=20379f32034f8867d352666ff2904d2152d5ff6843ee2db5ab5df863c14b1aef&rv=1&language=zh_CN_CHN#!repair")
                .header(headers);
        HttpResponse res2 = ProxyUtil.execute(httpRequest);

        return res2;
    }
    public static HttpResponse repareOptions(HttpResponse step211Res, HttpResponse step212Res) {
        HashMap<String, List<String>> headers =  new HashMap<>();
        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-Skip-Repair-Attributes", ListUtil.toList("[]"));
        headers.put("X-Apple-Session-Token", ListUtil.toList(step211Res.header("X-Apple-Repair-Session-Token")));
        headers.put("X-Apple-ID-Session-Id", ListUtil.toList(step211Res.header("X-Apple-ID-Session-Id")));
        headers.put("Sec-Fetch-Site", ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("cors"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("empty"));
        headers.put("scnt", ListUtil.toList(step212Res.header("scnt")));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList("{\"U\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36\",\"L\":\"zh-CN\",\"Z\":\"GMT+08:00\",\"V\":\"1.1\",\"F\":\"Fla44j1e3NlY5BNlY5BSmHACVZXnN92hpu__Iq1JlQxQeLaD.SAuXjodUW1BNork0ugN.xL4FeHRJdlU9_y4AwcGY5BNlYJNNlY5QB4bVNjMk.2IL\"}"));
        headers.put("X-Apple-Widget-Key",ListUtil.toList("20379f32034f8867d352666ff2904d2152d5ff6843ee2db5ab5df863c14b1aef"));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("Content-Type",ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        String scUrl = "https://appleid.apple.com/account/manage/repair/options";
        HttpRequest httpRequest=HttpUtil.createGet(scUrl)
                .header(headers);
        HttpResponse res2 = ProxyUtil.execute(httpRequest);
        return res2;
    }
    public static HttpResponse securityUpgrade(HttpResponse res1, String XAppleIDSessionId, String scnt) {
        HashMap<String, List<String>> headers =  new HashMap<>();
        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-Skip-Repair-Attributes", ListUtil.toList("[]"));
        headers.put("X-Apple-Session-Token", ListUtil.toList(res1.header("X-Apple-Session-Token")));
        headers.put("X-Apple-ID-Session-Id", ListUtil.toList(XAppleIDSessionId));
        headers.put("Sec-Fetch-Site", ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("cors"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("empty"));
        headers.put("scnt", ListUtil.toList(scnt));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList("{\"U\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36\",\"L\":\"zh-CN\",\"Z\":\"GMT+08:00\",\"V\":\"1.1\",\"F\":\"Fla44j1e3NlY5BNlY5BSmHACVZXnN92hpu__Iq1JlQxQeLaD.SAuXjodUW1BNork0ugN.xL4FeHRJdlU9_y4AwcGY5BNlYJNNlY5QB4bVNjMk.2IL\"}"));
        headers.put("X-Apple-Widget-Key",ListUtil.toList("20379f32034f8867d352666ff2904d2152d5ff6843ee2db5ab5df863c14b1aef"));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("Content-Type",ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        String scUrl = "https://appleid.apple.com/account/security/upgrade";
        HttpResponse res2 = ProxyUtil.execute(HttpUtil.createGet(scUrl)
                .header(headers));
        return res2;
    }
    public static HttpResponse securityUpgradeSetuplater(HttpResponse res1, String XAppleIDSessionId, String scnt) {
        HashMap<String, List<String>> headers =  new HashMap<>();
        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-Skip-Repair-Attributes", ListUtil.toList("[]"));
        headers.put("X-Apple-Session-Token", ListUtil.toList(res1.header("X-Apple-Session-Token")));
        headers.put("X-Apple-ID-Session-Id", ListUtil.toList(XAppleIDSessionId));
        headers.put("Sec-Fetch-Site", ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("cors"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("empty"));
        headers.put("scnt", ListUtil.toList(scnt));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList("{\"U\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36\",\"L\":\"zh-CN\",\"Z\":\"GMT+08:00\",\"V\":\"1.1\",\"F\":\"Fla44j1e3NlY5BNlY5BSmHACVZXnN92hpu__Iq1JlQxQeLaD.SAuXjodUW1BNork0ugN.xL4FeHRJdlU9_y4AwcGY5BNlYJNNlY5QB4bVNjMk.2IL\"}"));
        headers.put("X-Apple-Widget-Key",ListUtil.toList("20379f32034f8867d352666ff2904d2152d5ff6843ee2db5ab5df863c14b1aef"));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("Content-Type",ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));

        String scUrl = "https://appleid.apple.com/account/security/upgrade/setuplater";
        HttpResponse res2 = ProxyUtil.execute(HttpUtil.createGet(scUrl)
                .header(headers));
        return res2;
    }
    public static HttpResponse repareOptionsSecond(HttpResponse res1, String XAppleIDSessionId, String scnt) {
        HashMap<String, List<String>> headers =  new HashMap<>();

        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-Skip-Repair-Attributes", ListUtil.toList("[]"));
        headers.put("X-Apple-Session-Token", ListUtil.toList(res1.header("X-Apple-Session-Token")));
        headers.put("X-Apple-ID-Session-Id", ListUtil.toList(XAppleIDSessionId));
        headers.put("Sec-Fetch-Site", ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("cors"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("empty"));
        headers.put("scnt", ListUtil.toList(scnt));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList("{\"U\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36\",\"L\":\"zh-CN\",\"Z\":\"GMT+08:00\",\"V\":\"1.1\",\"F\":\"Fla44j1e3NlY5BNlY5BSmHACVZXnN92hpu__Iq1JlQxQeLaD.SAuXjodUW1BNork0ugN.xL4FeHRJdlU9_y4AwcGY5BNlYJNNlY5QB4bVNjMk.2IL\"}"));
        headers.put("X-Apple-Widget-Key",ListUtil.toList("20379f32034f8867d352666ff2904d2152d5ff6843ee2db5ab5df863c14b1aef"));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("Content-Type",ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("X-Apple-Skip-Repair-Attributes", ListUtil.toList("[\"hsa2_enrollment\"]"));

        String scUrl = "https://appleid.apple.com/account/manage/repair/options";
        HttpResponse res2 = ProxyUtil.execute(HttpUtil.createGet(scUrl)
                .header(headers));
        return res2;
    }
    public static HttpResponse repareComplete(HttpResponse res1, HttpResponse step211Res,String frameId) {
        String XAppleIDSessionId=step211Res.header("X-Apple-ID-Session-Id");
        String scnt=step211Res.header("scnt");
        HashMap<String, List<String>> headers =  new HashMap<>();

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));
        headers.put("X-Apple-Repair-Session-Token", ListUtil.toList(res1.header("X-Apple-Session-Token")));
        headers.put("scnt", ListUtil.toList(scnt));
        headers.put("X-Apple-Widget-Key",ListUtil.toList("20379f32034f8867d352666ff2904d2152d5ff6843ee2db5ab5df863c14b1aef"));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList("{\"U\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36\",\"L\":\"zh-CN\",\"Z\":\"GMT+08:00\",\"V\":\"1.1\",\"F\":\"Fla44j1e3NlY5BNlY5BSmHACVZXnN92hpu__Iq1JlQxQeLaD.SAuXjodUW1BNork0ugN.xL4FeHRJdlU9_y4AwcGY5BNlYJNNlY5QB4bVNjMk.2IL\"}"));
        headers.put("X-Apple-ID-Session-Id", ListUtil.toList(XAppleIDSessionId));

        headers.put("X-Apple-Auth-Attributes", ListUtil.toList(step211Res.header("X-Apple-Auth-Attributes")));

        headers.put("X-Apple-Frame-Id", ListUtil.toList( headers.put("X-Apple-Frame-Id", ListUtil.toList(frameId))));
        headers.put("X-Apple-OAuth-State", ListUtil.toList( headers.put("X-Apple-Frame-Id", ListUtil.toList(frameId))));
        headers.put("X-Apple-Trusted-Domain", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("X-Apple-OAuth-Client-Id", ListUtil.toList("20379f32034f8867d352666ff2904d2152d5ff6843ee2db5ab5df863c14b1aef"));

        headers.put("X-Apple-OAuth-Redirect-URI", ListUtil.toList("https://idmsa.apple.com"));

        headers.put("X-Apple-OAuth-Response-Type",ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-Response-Mode",ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-Client-Type",ListUtil.toList("firstPartyAuth"));
        headers.put("Sec-Fetch-Site", ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("cors"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("empty"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        String scUrl = "https://idmsa.apple.com/appleauth/auth/repair/complete";
        HttpResponse res2 = ProxyUtil.execute(HttpUtil.createPost(scUrl)
                .header(headers));
        return res2;
    }
    /**
    　* 登录方法
      * @param
    　* @return
    　* @throws
    　* @author DeZh
    　* @date 2023/11/27 22:19
    */
    public static Map<String,Object> webLogin(HttpResponse pre1Response,HttpResponse step22Res) {
        Map<String,Object> result=new HashMap<>();
        result.put("code",Constant.SUCCESS);
        HashMap<String, List<String>> headers =  new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Host", ListUtil.toList("reportaproblem.apple.com"));
        headers.put("Referer", ListUtil.toList("https://reportaproblem.apple.com/"));
        headers.put("Sec-Fetch-Site", ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("cors"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("empty"));
        headers.put("Te",ListUtil.toList("trailers"));

        List<String> pre1Cookies = pre1Response.headerList("Set-Cookie");
        StringBuilder cookieBuilder = new StringBuilder();
        for(String item : pre1Cookies){
            cookieBuilder.append(";").append(item);
        }

        List<String> step2ResCookies = step22Res.headerList("Set-Cookie");
        for(String item : step2ResCookies){
            cookieBuilder.append(";").append(item);
        }
        String loginCookies = cookieBuilder.substring(1);
        String loginUrl="https://reportaproblem.apple.com/api/login";
        HttpResponse loginResponse = ProxyUtil.execute(HttpUtil.createGet(loginUrl)
                .header(headers)
                .cookie(loginCookies));
        if(400==loginResponse.getStatus()){
            result.put("code","400");
            String messageBodyLocKey=JSONUtil.parse(loginResponse.body()).getByPath("error.messageBodyLocKey",String.class);
            if("RAP2.Error.ACCOUNT_DISABLED.Body".equals(messageBodyLocKey)){
                result.put("msg","帐户存在欺诈行为，已被【双禁】。");
            }
            return result;
        }
        String countryCodeISO3A=JSONUtil.parse(loginResponse.body()).getByPath("ampAccount.countryCodeISO3A",String.class);
        result.put("countryName",DataUtil.getNameByCountryCode(countryCodeISO3A));
        String token=JSONUtil.parse(loginResponse.body()).getByPath("token").toString();
        String dsid=JSONUtil.parse(loginResponse.body()).getByPath("dsid").toString();
        //查询方法
        Map<String,String> cookiesMap=new HashMap<>();
        CookieUtils.setCookiesToMap(loginResponse,cookiesMap);
        CookieUtils.setCookiesToMap(step22Res,cookiesMap);
        String searchCookies = MapUtil.join(cookiesMap,";","=",true);
        result.put("token",token);
        result.put("dsid",dsid);
        result.put("searchCookies",searchCookies);
        return result;
    }
    /**
    　* 查询方法
      * @param
     * @param dsid
     * @param nextBatchId
    　* @return cn.hutool.http.HttpResponse
    　* @throws
    　* @author DeZh
    　* @date 2023/11/27 22:16
    */
    public static HttpResponse search(List<String> jsonStrList,String dsid,String nextBatchId,String token,String searchCookies) {
        HashMap<String, List<String>> headers =  new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*;"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("x-apple-xsrf-token",ListUtil.toList(token));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("x-apple-rap2-api",ListUtil.toList("3.0.0"));
        headers.put("Origin", ListUtil.toList("https://reportaproblem.apple.com"));
        headers.put("Referer", ListUtil.toList("https://reportaproblem.apple.com/"));
        headers.put("Sec-Fetch-Site", ListUtil.toList("same-origin"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("cors"));
        headers.put("Sec-Fetch-Dest", ListUtil.toList("empty"));
        headers.put("Te",ListUtil.toList("trailers"));
        String searchUrl = "https://reportaproblem.apple.com/api/purchase/search";

        String body="{\"batchId\":\"%s\",\"dsid\":\"%s\",\"purchaseAmount\":\"\"}";

        body = String.format(body,nextBatchId,dsid);
        HttpRequest httpRequest=HttpUtil.createPost(searchUrl)
                .header(headers)
                .cookie(searchCookies)
                .body(body);
        HttpResponse searchResponse = ProxyUtil.execute(httpRequest);
        if(searchResponse.getStatus()==200){
            JSON json=JSONUtil.parse(searchResponse.body());
            jsonStrList.add(searchResponse.body());
            if(!StringUtils.isEmpty(json.getByPath("nextBatchId",String.class))){
                nextBatchId=json.getByPath("nextBatchId").toString();
                search(jsonStrList,dsid,nextBatchId,token,searchCookies);
            }
        }
        return searchResponse;
    }
    private static Map<String,String> calM(String accountName, String password, String a, Integer iter, String salt, String b, BigInteger g, BigInteger n, BigInteger ra) {
        // calculatek // k = h(n|g) 直接串联,并且按照位数对齐，不足的前面补0凑
        byte[] nb = n.toByteArray();
        byte[] gb = g.toByteArray();

        if(nb.length > 256){
            nb = ArrayUtil.remove(nb,0);
        }

        //SRPPassword 计算srp P 字段，
        byte[] p = SRPPassword(password, salt, iter);
        // calculateX // x = SHA(s | SHA(U | ":" | p))
        BigInteger X = calculateX(salt, p);
        BigInteger bigB = new BigInteger(1, Base64.decode(b));
        BigInteger bigA = new BigInteger(HexUtil.encodeHexStr(Base64.decode(a)),16);

        byte[] ab = bigA.toByteArray();
        byte[] bb = bigB.toByteArray();
        if(ab.length>256){
            ab = ArrayUtil.remove(ab,0);
        }
        if(bb.length>256){
            bb = ArrayUtil.remove(bb,0);
        }

        // calculateU // U = SHA(a | b)
        BigInteger u= calculateU(ab,bb);

        BigInteger k = calculatek(nb, gb);

        //calculateS
        BigInteger S = calculateS(k,X, ra,bigB,u, n, g);

        //calculateK
        byte[] K = calculateK(S);

        //calculateM1
        byte[] m1 = calculateM1(accountName, salt,ab,bb,K, nb, gb);
        //calculateM2
        byte[] m2 = calculateM2(bigA,m1,K);
        Map<String,String> map = new HashMap<>();
        map.put("m1",Base64.encode(m1));
        map.put("m2",Base64.encode(m2));
        return map;
    }

    private static byte[] SRPPassword(String password,String salt,int iter){

        try {
            String algorithm = "PBKDF2WithHmacSHA256";
            int keyLength = 256;

            Digester digester = new Digester(DigestAlgorithm.SHA256);
            byte[] p = digester.digest(password.getBytes());
            byte[] sb = Base64.decode(salt);
            PBEParametersGenerator generator = new PKCS5S2ParametersGenerator(DigestFactory.createSHA256());
            generator.init(p, sb, iter);
            KeyParameter params = (KeyParameter)generator.generateDerivedParameters(keyLength);
            byte[] key = params.getKey();
            return key;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] calculateM2(BigInteger bigA, byte[] m1, byte[] k){
        Digester digester = new Digester(DigestAlgorithm.SHA256);
        byte[] m2 = digester.digest(ArrayUtil.addAll(bigA.toByteArray(),m1,k));
        return m2;
    }

    private static byte[] calculateM1(String accountName , String salt,byte[] ab,byte[] bb,byte[] k,byte[] nb,byte[] gb){

        byte[] pp = new byte[255];
        for(int i = 0 ; i < 255; i ++){
            pp[i] = 0;
        }

        Digester digester1 = new Digester(DigestAlgorithm.SHA256);
        byte[] digestn = digester1.digest(nb);

        Digester digester2 = new Digester(DigestAlgorithm.SHA256);
        byte[] digestg = digester2.digest(ArrayUtil.addAll(pp,gb));

        Digester digester3 = new Digester(DigestAlgorithm.SHA256);
        byte[] digenti = digester3.digest(accountName);

        byte[] hxor = new byte[digestn.length];
        for(int i = 0; i < digestn.length; i++){
            hxor[i] = (byte)(digestn[i] ^ digestg[i]);
        }

        Digester digester4 = new Digester(DigestAlgorithm.SHA256);

        byte[] m1 = digester4.digest(ArrayUtil.addAll(hxor,digenti, Base64.decode(salt),ab,bb,k));

        return m1;
    }

    private static byte[] calculateK(BigInteger S){
        Digester digester = new Digester(DigestAlgorithm.SHA256);

        byte[] s = S.toByteArray();
        if(s.length > 256){
            s = ArrayUtil.remove(s,0);
        }
        byte[] d = digester.digest(s);
        return d;
    }

    /* Client Side S = (B - k*(g^x)) ^ (a + ux) */
    private static BigInteger calculateS(BigInteger k , BigInteger X , BigInteger a,BigInteger b,BigInteger u,BigInteger n, BigInteger g){

        BigInteger result1 = g.modPow(X,n);

        BigInteger result2 = k.multiply(result1);

        BigInteger result3 = b.subtract(result2);

        BigInteger result4 = u.multiply(X);

        BigInteger result5 = a.add(result4);

        BigInteger result6 = result3.modPow(result5,n);

        BigInteger result7 = result6.mod(n);

        return result7;
    }

    private static BigInteger calculatek(byte[] nb,byte[] gb){

        byte[] pp = new byte[255];
        for(int i = 0 ; i < 255; i ++){
            pp[i] = 0;
        }

        byte[] h = ArrayUtil.addAll(nb,pp,gb);
        Digester digester = new Digester(DigestAlgorithm.SHA256);
        byte[] d = digester.digest(h);

        BigInteger k = new BigInteger(1,d);
        return k;
    }

    private static BigInteger calculateU(byte[] ab,byte[] bb){
        Digester digester1 = new Digester(DigestAlgorithm.SHA256);

        byte[] a = ArrayUtil.addAll(ab,bb);
        byte[] d = digester1.digest(a);

        BigInteger u = new BigInteger(1,d);
        return u;
    }

    private static BigInteger calculateX(String salt, byte[] password){

        Digester digester1 = new Digester(DigestAlgorithm.SHA256);
        byte[] d1 = digester1.digest(ArrayUtil.addAll(":".getBytes(StandardCharsets.UTF_8),password));

        Digester digester2 = new Digester(DigestAlgorithm.SHA256);
        byte[] d2 = digester2.digest(ArrayUtil.addAll(Base64.decode(salt),d1));

        BigInteger x = new BigInteger(1,d2);

        return  x;
    }

    private static String calA(BigInteger a,BigInteger n) {

        BigInteger g = new BigInteger("2");
        BigInteger ai = g.modPow(a,n);

        byte[] aib = ai.toByteArray();
        if(aib.length > 256){
            aib = ArrayUtil.remove(aib,0);
        }
        String a2k = Base64.encode(aib);
        return  a2k;
    }


    private static String getBit(byte by){
        StringBuffer sb = new StringBuffer();
        sb.append((by>>7)&0x1)
                .append((by>>6)&0x1)
                .append((by>>5)&0x1)
                .append((by>>4)&0x1)
                .append((by>>3)&0x1)
                .append((by>>2)&0x1)
                .append((by>>1)&0x1)
                .append((by>>0)&0x1);
        return sb.toString();
    }

    private static String createFrameId(){

        Digester md5 = new Digester(DigestAlgorithm.MD5);
        StringBuilder sb = new StringBuilder();

        sb.append("auth-");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(2,10));
        sb.append("-");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(2,6));
        sb.append("-");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(2,6));
        sb.append("-");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(2,6));
        sb.append("-");
        sb.append(md5.digestHex(RandomUtil.randomBytes(32)).substring(2,10));

        return sb.toString();
    }
    ///iTunes版
    public static Map<String,Object> iTunesAuth(String account,String pwd){
        String guid=DataUtil.getGuidByAppleId(account);
        String authUrl = "https://buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/authenticate?guid="+guid;
        Map<String,Object> paras=new HashMap<>();
        paras.put("account",account);
        paras.put("pwd",pwd);
        paras.put("authUrl",authUrl);
        paras.put("code",Constant.SUCCESS);
        String authCode = "";
        return iTunesLogin(authCode,guid,0,paras);
    }
    public static Map<String,Object> iTunesAuth(String authCode,Map<String,Object> paras){
        String guid=MapUtil.getStr(paras,"guid");
        String authUrl = "https://p"+MapUtil.getStr(paras,"itspod")+"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/authenticate?guid="+guid;
        paras.put("authUrl",authUrl);
        return iTunesLogin(authCode,guid,0,paras);
    }
    private static Map<String,Object> iTunesLogin(String authCode,String guid, Integer attempt,Map<String,Object> paras){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", ListUtil.toList("application/x-apple-plist; Charset=UTF-8"));
//        headers.put("User-Agent", ListUtil.toList(Constant.MACAPPSTORE20_USER_AGENT));
        headers.put("User-Agent", ListUtil.toList(Constant.CONFIGURATOR_USER_AGENT));
        headers.put("X-Apple-Store-Front", ListUtil.toList("143465-19,17"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));

        String authBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                "<plist version=\"1.0\">"+
                    "<dict>"+
                        "<key>appleId</key>"+
                        "<string>"+paras.get("account")+"</string>"+
                        "<key>attempt</key>"+
                        "<string>4</string>"+
                        "<key>createSession</key>"+
                        "<string>true</string>"+
                        "<key>guid</key>"+
                        "<string>"+guid+"</string>"+
                        "<key>password</key>"+
                        "<string>"+paras.get("pwd")+authCode+"</string>"+
                        "<key>rmp</key>"+
                        "<string>0</string>"+
                        "<key>why</key>"+
                        "<string>signIn</string>"+
                    "</dict>"+
                "</plist>";
        try {
            String authUrl=MapUtil.getStr(paras,"authUrl");
            HttpRequest httpRequest=HttpUtil.createPost(authUrl)
                    .header(headers)
                    .cookie(MapUtil.getStr(paras,"cookies"))
                    .body(authBody);
            HttpResponse res = ProxyUtil.execute(httpRequest);
            paras.put("storeFront",res.header(Constant.HTTPHeaderStoreFront));
            paras.put("itspod",res.header(Constant.ITSPOD));
            if(!StringUtils.isEmpty(res.header("location"))){
                paras.put("authUrl",res.header("location"));
            }
            paras.put("cookies",CookieUtils.getCookiesFromHeader(res));
            paras.put("storeFront",res.header(Constant.HTTPHeaderStoreFront));
            paras.put("guid",guid);
            String countryCode= "";
            if(StrUtil.isBlankIfStr(MapUtil.getStr(paras,"storeFront"))){

            }else{
                countryCode= StoreFontsUtils.getCountryCodeFromStoreFront(MapUtil.getStr(paras,"storeFront"));
            }
            String countryName="-";
            if(StringUtils.isEmpty(countryCode)){

            }else{
                countryName =DataUtil.getNameByCountryCode(countryCode);
            }
            paras.put("countryName",countryName);
            if(res.getStatus()==302 && attempt ==0){
                return iTunesLogin(authCode,guid,1,paras);
            }
            String rb = res.charset("UTF-8").body();
            JSONObject rspJSON = PListUtil.parse(rb);
            Map<String,Object> result=ITunesUtil.checkLoginRes(rb);
            String code= (String) result.get("code");
            if(!Constant.SUCCESS.equals(code)){
                if(Constant.CustomerMessageNotYetUsediTunesStoreCode.equals(code)){
                    paras.put("hasInspectionFlag",false);
                }
                paras.put("msg",result.get("msg"));
                paras.put("code",code);
                return paras;
            }
            paras.put("hasInspectionFlag",true);
            paras.put("msg","登录成功");
            paras.put("code",Constant.SUCCESS);
            String firstName = rspJSON.getByPath("accountInfo.address.firstName",String.class);
            String lastName  = rspJSON.getByPath("accountInfo.address.lastName",String.class);
            Boolean isDisabledAccount  = rspJSON.getByPath("accountFlags.isDisabledAccount",Boolean.class);
            paras.put("isDisabledAccount",isDisabledAccount);
            paras.put("name",lastName +  " " + firstName);
            paras.put("creditDisplay",StringUtils.isEmpty(rspJSON.getStr("creditDisplay"))?"0":rspJSON.getStr("creditDisplay"));
            paras.put("dsPersonId",rspJSON.getStr("dsPersonId"));
            paras.put("passwordToken",rspJSON.getStr("passwordToken"));
        } catch (IORuntimeException e) {
            paras.put("msg","连接异常，请检查网络");
            paras.put("code","-1");
        } catch (Exception e) {
            throw e;
        }

        return paras;
    }
    public static Map<String,Object> accountSummary(Map<String, Object> paras) {
        String accountUrl = "https://p"+ paras.get("itspod") +"-buy.itunes.apple.com/WebObjects/MZFinance.woa/wa/accountSummary?guid="+paras.get("guid");
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.MACAPPSTORE20_USER_AGENT));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        headers.put("X-Dsid",ListUtil.toList(paras.get("dsPersonId").toString()));
        headers.put("X-Apple-Store-Front",ListUtil.toList(paras.get("storeFront").toString()));
        headers.put("X-Token",ListUtil.toList(paras.get("passwordToken").toString()));
        headers.put("Accept-Encoding",ListUtil.toList("gzip"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.9,en;q=0"));

        String cookies = MapUtil.getStr(paras,"cookies","");
        try {
            HttpResponse res = ProxyUtil.execute(HttpUtil.createGet(accountUrl)
                    .header(headers)
                    .cookie(cookies));
            //解析HTML
            Document document=Jsoup.parse(res.body());
            Element element=document.getElementById("account-info-section");
            paras.put("balance","0");
            Element appleIdAccountEle = document.getElementById("apple-id-account");
            if (appleIdAccountEle != null){
                Elements info = appleIdAccountEle.getElementsByClass("info");
                if (!CollUtil.isEmpty(info)){
                    String text = info.get(0).text();
                    if (text.matches(".*[0-9].*")){
                        paras.put("balance", text);
                    }
                }
            }

            Element addressElement=element.getElementsByClass("address").get(0);
            String address=addressElement.html().replace("<br>",",");
            paras.put("address",address);
            String countryCode=StoreFontsUtils.getCountryCodeFromStoreFront(MapUtil.getStr(paras,"storeFront"));
            String countryName =DataUtil.getNameByCountryCode(countryCode);
            //账号国家
            paras.put("countryName",countryName);
            //寄送地址
            String paymentMethod=addressElement.parent().parent().previousElementSibling().getElementsByClass("info").text();
            paras.put("paymentMethod",paymentMethod);
        } catch (IORuntimeException e) {
            paras.put("msg","连接异常，请检查网络");
            paras.put("code","-1");
        } catch (Exception e) {
            throw e;
        }
        return paras;
    }

    /**
     　* 统计购买记录
     * @param
    　* @return java.util.List<java.util.Map<java.lang.String,java.lang.String>>
    　* @throws
    　* @author DeZh
    　* @date 2023/10/19 10:09
     */
    public  static int accountPurchasesLast90Count(Map<String,Object> paras){
        String host = "p"+ paras.get("itspod") +"-buy.itunes.apple.com";
        String url = "https://p"+ paras.get("itspod") +"-buy.itunes.apple.com/commerce/account/purchases";
        HashMap<String, List<String>> headers = new HashMap<>();
//
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));

        headers.put("Host", ListUtil.toList(host));
        headers.put("Referer", ListUtil.toList("https://finance-app.itunes.apple.com/"));
        headers.put("X-Dsid",ListUtil.toList(paras.get("dsPersonId").toString()));
        headers.put("X-Apple-Tz",ListUtil.toList("28800"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate"));
        headers.put("X-Token",ListUtil.toList(ListUtil.toList(paras.get("passwordToken").toString())));
        headers.put("X-Apple-Store-Front",ListUtil.toList(paras.get("storeFront").toString()));
        String cookies = MapUtil.getStr(paras,"cookies","");

        HttpResponse response = ProxyUtil.execute(HttpUtil.createRequest(Method.GET,url)
                .header(headers)
                .cookie(cookies));
        String purchasesJsonStr =JSONUtil.parse(response.body()).getByPath("data.attributes.purchases",String.class);
        return JSONUtil.parseArray(purchasesJsonStr).size();
    }
}

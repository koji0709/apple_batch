package com.sgswit.fx.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.common.UnavailableException;
import com.sgswit.fx.utils.proxy.ProxyUtil;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.util.DigestFactory;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class GiftCardUtil {

    public static HttpResponse shopPre1(String countryCode){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Referer", ListUtil.toList("https://www.apple.com/"));
        headers.put("User-Agent",ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));
        String url="https://secure.store.apple.com/shop/giftcard/balance";
        if(!countryCode.equalsIgnoreCase("us")){
            url="https://secure.store.apple.com/"+countryCode.toLowerCase()+"/shop/giftcard/balance";
        }
        HttpResponse res = ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers));
        return res;
    }

    public static HttpResponse shopPre2(HttpResponse pre1){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Referer", ListUtil.toList("https://www.apple.com/"));
        headers.put("User-Agent",ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));
        HttpResponse res = ProxyUtil.execute(HttpUtil.createGet(pre1.header("Location"))
                        .header(headers)
                        .cookie(getCookies(pre1)));
        return res;
    }

    public static HttpResponse shopPre3(HttpResponse pre1,HttpResponse pre2){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Referer", ListUtil.toList("https://www.apple.com/"));
        headers.put("User-Agent",ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));
        HttpResponse res = ProxyUtil.execute(HttpUtil.createGet(pre2.header("Location"))
                        .header(headers)
                        .cookie(getCookies(pre1)));
        return res;
    }
    public static Map<String,Object> jXDocument(HttpResponse pre2, HttpResponse pre3,Map<String,Object> paras){
        JXDocument underTest = JXDocument.create(pre3.body());

        List<JXNode>  nodes = underTest.selN("//script");
        String as_sfa = nodes.get(0).value().toString();
        String as_sfa_cookie   = as_sfa.substring(as_sfa.indexOf("as_sfa"),as_sfa.indexOf("\";"));
        paras.put("as_sfa_cookie",as_sfa_cookie);


        String metaXml = nodes.get(nodes.size()-1).value().toString();
        String metaJson = metaXml.substring(metaXml.indexOf("{\"meta\":"),metaXml.indexOf("</script>"));
        JSON meta = JSONUtil.parse(metaJson);
        String x_aos_model_page =meta.getByPath("meta.h.x-aos-model-page",String.class);
        paras.put("x_aos_model_page",x_aos_model_page);
        String x_aos_stk = meta.getByPath("meta.h.x-aos-stk",String.class);
        paras.put("x_aos_stk",x_aos_stk);
        String modelVersion =  meta.getByPath("meta.h.modelVersion",String.class);
        paras.put("modelVersion",modelVersion);
        String syntax = meta.getByPath("meta.h.syntax",String.class);
        paras.put("syntax",syntax);
        String serviceKey =meta.getByPath("signIn.customerLoginIDMS.d.serviceKey",String.class);
        paras.put("serviceKey",serviceKey);
        String serviceURL =meta.getByPath("signIn.customerLoginIDMS.d.serviceURL",String.class);
        paras.put("serviceURL",serviceURL);
        String callbackSignInUrl = meta.getByPath("signIn.customerLoginIDMS.d.callbackSignInUrl",String.class);
        paras.put("callbackSignInUrl",callbackSignInUrl);
        String clientId = serviceKey;
        paras.put("clientId",clientId);
        String frameId  = createFrameId();
        paras.put("frameId",frameId);
        String location = pre2.header("Location");
        paras.put("location",location);
        String locationBase = location.substring(0,location.indexOf("shop"));
        paras.put("locationBase",locationBase);
        String locationSSi  =  location.substring(location.indexOf("?"));
        paras.put("locationSSi",locationSSi);
        // get x-apple-hc
        HttpResponse pre4 = signFrame(paras);
        paras.put("code",pre4.getStatus());
        if(503==pre4.getStatus()){
            return paras;
        }
        paras.put("callbackSignInUrl",callbackSignInUrl);
        int xAppleHcBits = Integer.parseInt(pre4.header("X-Apple-HC-Bits"));
        paras.put("xAppleHcBits",xAppleHcBits);
        String xAppleHcChallenge = pre4.header("X-Apple-HC-Challenge");
        paras.put("xAppleHcChallenge",xAppleHcChallenge);
        //step1  signin
        String nHex = "AC6BDB41324A9A9BF166DE5E1389582FAF72B6651987EE07FC3192943DB56050A37329CBB4A099ED8193E0757767A13DD52312AB4B03310DCD7F48A9DA04FD50E8083969EDB767B0CF6095179A163AB3661A05FBD5FAAAE82918A9962F0B93B855F97993EC975EEAA80D740ADBF4FF747359D041D5C33EA71D281E446B14773BCA97B43A23FB801676BD207A436C6481F1D2B9078717461A5B9D32E688F87748544523B524B0D57D5EA77A2775D2ECFA032CFBDBF52FB3786160279004E57AE6AF874E7303CE53299CCC041C7BC308D82A5698F3A8D0C38271AE35F8E9DBFBB694B5C803D89F7AE435DE236D525F54759B65E372FCD68EF20FA7111F9E4AFF73";
        BigInteger n = new BigInteger(nHex,16);
        BigInteger g = new BigInteger("2");

        byte[] rb = RandomUtil.randomBytes(32);
        BigInteger ra = new BigInteger(1,rb);
        String a = calA(ra,n);
        paras.put("g",g);
        paras.put("n",n);
        paras.put("ra",ra);
        paras.put("a",a);
        return paras;
    }

    private static HttpResponse signFrame(Map<String,Object> paras){
        HashMap<String, List<String>> headers = new HashMap<>();
        String locationBase= MapUtil.getStr(paras,"locationBase");
        String frameId= MapUtil.getStr(paras,"frameId");
        String clientId= MapUtil.getStr(paras,"clientId");
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Host",ListUtil.toList("idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList(locationBase));
        headers.put("User-Agent",ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));
        String url = "https://idmsa.apple.com/appleauth/auth/authorize/signin?frame_id="+frameId+"&language=en_US&skVersion=7&iframeId="+frameId
                +"&client_id="+clientId+"&redirect_uri="+locationBase+"&response_type=code&response_mode=web_message" +
                "&state="+frameId+"&authVersion=latest";

        HttpResponse res = ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers));
        return res;
    }

    public static HttpResponse federate(String account,Map<String,Object> paras){
        String frameId= MapUtil.getStr(paras,"frameId");
        String clientId= MapUtil.getStr(paras,"clientId");
        String locationBase= MapUtil.getStr(paras,"locationBase");
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

        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList("{\"U\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/115.0\",\"L\":\"zh-CN\",\"Z\":\"GMT+08:00\",\"V\":\"1.1\",\"F\":\"Fla44j1e3NlY5BNlY5BSmHACVZXnN92fq9c0K8v0ururJhBR.uMp4UdHz13NlVjV2pNk0ug9WJZuJsejWvEkeUkd5BNlY5CGWY5BOgkLT0XxU..BTM\"}"));
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

        headers.put("User-Agent",ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));

        String body = "{\"accountName\":\""+account+"\",\"rememberMe\":false}";

        HttpResponse res = ProxyUtil.execute(HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/federate?isRememberMeEnabled=true")
                        .header(headers)
                        .body(body));
        return res;
    }

    public static HttpResponse signinInit(String account,String a ,HttpResponse res1,Map<String,Object> paras){
        String frameId= MapUtil.getStr(paras,"frameId");
        String clientId= MapUtil.getStr(paras,"clientId");
        String locationBase= MapUtil.getStr(paras,"locationBase");
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

        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList("{\"U\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/115.0\",\"L\":\"zh-CN\",\"Z\":\"GMT+08:00\",\"V\":\"1.1\",\"F\":\"Fla44j1e3NlY5BNlY5BSmHACVZXnN92fq9c0K8v0ururJhBR.uMp4UdHz13NlVjV2pNk0ug9WJZuJsejWvEkeUkd5BNlY5CGWY5BOgkLT0XxU..BTM\"}"));
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

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(res1.header("X-Apple-ID-Session-Id")));
        headers.put("scnt",ListUtil.toList(res1.header("scnt")));

        headers.put("User-Agent",ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));

        String body = "{\"a\":\""+a+"\",\"accountName\":\""+account+"\",\"protocols\":[\"s2k\",\"s2k_fo\"]}";
        HttpResponse res = ProxyUtil.execute(HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/signin/init")
                        .header(headers)
                        .body(body));
        return res;
    }

    public static HttpResponse signinCompete(String account,String pwd,Map<String,Object> paras,HttpResponse res1,HttpResponse pre1,HttpResponse pre3){
        String frameId= MapUtil.getStr(paras,"frameId");
        String clientId= MapUtil.getStr(paras,"clientId");
        String locationBase= MapUtil.getStr(paras,"locationBase");
        String a= MapUtil.getStr(paras,"a");
        String as_sfa_cookie= MapUtil.getStr(paras,"as_sfa_cookie");
        int xAppleHcBits= MapUtil.getInt(paras,"xAppleHcBits");
        String xAppleHcChallenge= MapUtil.getStr(paras,"xAppleHcChallenge");
        BigInteger g= MapUtil.get(paras,"g",BigInteger.class);
        BigInteger n= MapUtil.get(paras,"n",BigInteger.class);
        BigInteger ra= MapUtil.get(paras,"ra",BigInteger.class);


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

        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList("{\"U\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/115.0\",\"L\":\"zh-CN\",\"Z\":\"GMT+08:00\",\"V\":\"1.1\",\"F\":\"Fla44j1e3NlY5BNlY5BSmHACVZXnN92fq9c0K8v0ururJhBR.uMp4UdHz13NlVjV2pNk0ug9WJZuJsejWvEkeUkd5BNlY5CGWY5BOgkLT0XxU..BTM\"}"));
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

        headers.put("User-Agent",ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(res1.header("X-Apple-ID-Session-Id")));
        headers.put("scnt",ListUtil.toList(res1.header("scnt")));

        String hc = calCounter(xAppleHcBits,xAppleHcChallenge);

        headers.put("X-APPLE-HC",ListUtil.toList(hc));
        JSON json ;
        try{
            json =  JSONUtil.parse(res1.body());
        }catch (Exception e){
            throw new UnavailableException();
        }

        int iter = (Integer) json.getByPath("iteration");
        String salt = (String)json.getByPath("salt");
        String b = (String) json.getByPath("b");
        String c = (String)json.getByPath("c");

        Map map = calM(account, pwd, a, iter, salt, b, g, n, ra);
        Map<String,Object> bodyParas=new HashMap<>(){{
            put("accountName",account);
            put("rememberMe",false);
            put("m1",map.get("m1"));
            put("c",c);
            put("m2",map.get("m2"));
        }};
        Map<String,String> cookiesMap=new HashMap<>();
        CookieUtils.setCookiesToMap(res1,cookiesMap);
        CookieUtils.setCookiesToMap(pre3,cookiesMap);
        CookieUtils.setCookiesToMap(pre1,cookiesMap);
        String cookies= MapUtil.join(cookiesMap,";","=",true);
        cookies=cookies+";"+as_sfa_cookie;
        HttpResponse res = ProxyUtil.execute(HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/signin/complete?isRememberMeEnabled=true")
                        .header(headers)
                        .body(JSONUtil.toJsonStr(bodyParas))
                        .cookie(cookies));
        return res;
    }


    public static HttpResponse shopSignin(HttpResponse step2Res,HttpResponse pre1,Map<String,Object> paras){
        String syntax= MapUtil.getStr(paras,"syntax");
        String modelVersion= MapUtil.getStr(paras,"modelVersion");
        String x_aos_stk= MapUtil.getStr(paras,"x_aos_stk");
        String locationBase= MapUtil.getStr(paras,"locationBase");
        String location= MapUtil.getStr(paras,"location");
        String x_aos_model_page= MapUtil.getStr(paras,"x_aos_model_page");
        String as_sfa_cookie= MapUtil.getStr(paras,"as_sfa_cookie");
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("*/*"));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));

        headers.put("Origin", ListUtil.toList(locationBase));
        headers.put("Referer", ListUtil.toList(location));

        headers.put("x-aos-model-page", ListUtil.toList(x_aos_model_page));
        headers.put("x-aos-stk",ListUtil.toList(x_aos_stk));
        headers.put("modelVersion",ListUtil.toList(modelVersion));
        headers.put("syntax",ListUtil.toList(syntax));

        headers.put("x-requested-with",ListUtil.toList("Fetch"));


        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));


        headers.put("te",ListUtil.toList("trailers"));

        headers.put("User-Agent",ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));


        Map<String,String> cookiesMap=new HashMap<>();
        CookieUtils.setCookiesToMap(pre1,cookiesMap);
        CookieUtils.setCookiesToMap(step2Res,cookiesMap);
        CookieUtils.setCookiesToMap(pre1,cookiesMap);
        String cookies= MapUtil.join(cookiesMap,";","=",true);
        cookies=cookies+";"+as_sfa_cookie;

        Map<String,Object> paramMap = new HashMap<>();

        paramMap.put("deviceID","");
        paramMap.put("grantCode","");


        HttpResponse res3 = ProxyUtil.execute(HttpUtil.createPost(location.substring(0,location.indexOf("shop")) +
                        "shop/signIn/idms/authx" +
                        location.substring(location.indexOf("?")))
                        .header(headers)
                        .form(paramMap)
                        .cookie(cookies));
        return res3;
    }

    public static HttpResponse checkBalance( Map<String, Object> paras,String giftCardPin){
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("accept-language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Content-Type", ListUtil.toList("application/x-www-form-urlencoded"));

        headers.put("referer",ListUtil.toList(paras.get("locationBase")+ "shop/giftcard/balance"));
        headers.put("origin",ListUtil.toList(MapUtil.getStr(paras,"locationBase")));

        headers.put("User-Agent",ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));

        headers.put("x-aos-model-page", ListUtil.toList("giftCardBalancePage"));
        headers.put("x-aos-stk",ListUtil.toList(MapUtil.getStr(paras,"x_aos_stk")));
        headers.put("modelVersion",ListUtil.toList(MapUtil.getStr(paras,"modelVersion")));
        headers.put("syntax",ListUtil.toList(MapUtil.getStr(paras,"syntax")));

        headers.put("x-requested-with",ListUtil.toList("Fetch"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));

        Map<String,Object> data = new HashMap<>();
        data.put("giftCardBalanceCheck.giftCardPin",giftCardPin);
        String location=MapUtil.getStr(paras,"location");
        HttpResponse res4 = ProxyUtil.execute(HttpUtil.createPost(location.substring(0,location.indexOf("shop")) + "shop/giftcard/balancex?_a=checkBalance&_m=giftCardBalanceCheck")
                .header(headers)
                .cookie(MapUtil.getStr(paras,"cookies"))
                .form(data));
        return res4;
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
        BigInteger bigB = new BigInteger(1,Base64.decode(b));
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

    public static char[] byteToChar(byte[] bytes) {
        Charset charset = Charset.forName("UTF-8");
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        CharBuffer charBuffer = charset.decode(byteBuffer);
        return charBuffer.array();
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

    private static String calCounter(int xAppleHcBits,String xAppleHcChallenge) {
        String version = "1";
        String date = DateUtil.format(new DateTime(TimeZone.getTimeZone("GMT")),"yyyyMMddHHmmss");

        String hc = version + ":" + xAppleHcBits + ":" + date + ":" + xAppleHcChallenge + "::";

        int bytes = (int) Math.ceil(xAppleHcBits / 8.0);

        int counter = 0;
        boolean isZero = false;
        while(!isZero){
            Digester digester = new Digester(DigestAlgorithm.SHA1);
            byte[] d = digester.digest(hc+counter);
            byte[] prefix = ArrayUtil.sub(d, 0, bytes);

            String bitStr = "";
            for (int i = 0; i < bytes; i++) {
                bitStr += getBit(prefix[i]);
            }

            String zeroStr = "";
            for(int k = 0; k < xAppleHcBits; k++){
                zeroStr += "0";
            }

            if(bitStr.substring(0, xAppleHcBits).equals(zeroStr)){
                isZero = true;
                break;
            }
            counter++;
        }
        return hc + counter;
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
    private static String getCookies(HttpResponse response){
        StringBuilder cookieBuilder = new StringBuilder();
        List<String> resCookies = response.headerList("Set-Cookie");
        for(String item : resCookies){
            cookieBuilder.append(";").append(item);
        }
        return cookieBuilder.toString();
    }
}

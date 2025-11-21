package com.sgswit.fx.utils.web;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.utils.CookieUtils;
import com.sgswit.fx.utils.proxy.ProxyUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author DELL
 */
public class ShoppingLoginUtil {

    public static HttpResponse signin(Map<String,Object> signInMap){

        String frameId  = WebParasUtil.createFrameId();
        String clientId = MapUtil.getStr(signInMap,"serviceKey");

        Map<String,Object> result = WebParasUtil.calAWithout();
        String a= MapUtil.getStr(result,"a");
        BigInteger n= (BigInteger) result.get("n");
        BigInteger ra= (BigInteger) result.get("ra");
        BigInteger g= (BigInteger) result.get("g");

        signInMap.put("frameId",frameId);
        signInMap.put("clientId",clientId);

        HttpResponse step0Res = auth(signInMap);
        signInMap.put("a",a);
        HttpResponse step1Res = signinInit(step0Res,signInMap);
        if(step1Res.getStatus()==503){
            return step1Res;
        }
        signInMap.put("g",g);
        signInMap.put("n",n);
        signInMap.put("ra",ra);
        HttpResponse step2Res = signinCompete(step1Res,step0Res,signInMap);
        return  step2Res;
    }

    private static HttpResponse auth(Map<String,Object> signInMap){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type",ListUtil.toList("application/x-www-form-urlencoded"));

        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));
        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        String redirectUri ="";
        if(!StringUtils.isEmpty(MapUtil.getStr(signInMap,"callbackSignInUrl"))){
            redirectUri=MapUtil.getStr(signInMap,"callbackSignInUrl");
            redirectUri = redirectUri.substring(0,redirectUri.indexOf("shop"));
        }else{
            redirectUri="https://www.apple.com/";
        }
        String frameId=MapUtil.getStr(signInMap,"frameId");
        String clientId=MapUtil.getStr(signInMap,"clientId");
        String url = "https://idmsa.apple.com/appleauth/auth/authorize/signin?frame_id="+frameId+"&skVersion=7" +
                "&iframeId="+frameId+"&client_id="+clientId+"&redirect_uri="+ redirectUri +"&response_type=code" +
                "&response_mode=web_message&state="+frameId+"&authVersion=latest";
        Map<String,String> cookiesMap=new HashMap<>();
        if(null==signInMap.get("cookiesMap")){
            cookiesMap=new HashMap<>();
        }else{
            cookiesMap= (Map<String, String>) signInMap.get("cookiesMap");
        }
        HttpResponse res = ProxyUtil.execute(HttpUtil.createGet(url)
                        .cookie(cookiesMap.size()==0?"geo=CN;" :MapUtil.join(cookiesMap,";","=",true))
                        .header(headers));

        CookieUtils.setCookiesToMap(res,cookiesMap);

        signInMap.put("cookiesMap" , CookieUtils.setCookiesToMap(res,cookiesMap));
        return res;
    }

    private static HttpResponse signinInit(HttpResponse res1,Map<String,Object> paras){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("scnt",ListUtil.toList(res1.header("scnt")));
        headers.put("X-Apple-Auth-Attributes", ListUtil.toList(res1.header("X-Apple-Auth-Attributes")));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(MapUtil.getStr(paras,"clientId")));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("X-Apple-OAuth-Redirect-URI",ListUtil.toList("https://www.apple.com/"));
        headers.put("X-Apple-OAuth-Client-Id",ListUtil.toList(MapUtil.getStr(paras,"clientId")));
        headers.put("X-Apple-OAuth-Client-Type",ListUtil.toList("firstPartyAuth"));
        headers.put("X-Apple-OAuth-Response-Type",ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-Response-Mode",ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-State",ListUtil.toList(MapUtil.getStr(paras,"frameId")));
        headers.put("X-Apple-Domain-Id",ListUtil.toList("1"));
        headers.put("X-Apple-Frame-Id",ListUtil.toList(MapUtil.getStr(paras,"frameId")));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));
        String body = "{\"a\":\""+MapUtil.getStr(paras,"a")+"\",\"accountName\":\""+ MapUtil.getStr(paras,"account") +"\",\"protocols\":[\"s2k\",\"s2k_fo\"]}";
        HttpRequest httpRequest=HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/signin/init")
                .header(headers)
                .cookie(MapUtil.join((Map<String,String>) paras.get("cookiesMap"),";","=",true))
                .body(body);
        HttpResponse res = ProxyUtil.execute(httpRequest);
        return res;
    }

    private static HttpResponse signinCompete(HttpResponse res1,HttpResponse res0,Map<String,Object> paras){
        String a=MapUtil.getStr(paras,"a");
        BigInteger g=new BigInteger(MapUtil.getStr(paras,"g"));
        BigInteger n=new BigInteger(MapUtil.getStr(paras,"n"));
        BigInteger ra=new BigInteger(MapUtil.getStr(paras,"ra"));
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("scnt",ListUtil.toList(res1.header("scnt")));
        headers.put("X-Apple-Auth-Attributes", ListUtil.toList(res0.header("X-Apple-Auth-Attributes")));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(MapUtil.getStr(paras,"clientId")));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("X-Apple-OAuth-Redirect-URI",ListUtil.toList("https://www.apple.com/"));
        headers.put("X-Apple-OAuth-Client-Id",ListUtil.toList(MapUtil.getStr(paras,"clientId")));
        headers.put("X-Apple-OAuth-Client-Type",ListUtil.toList("firstPartyAuth"));
        headers.put("X-Apple-OAuth-Response-Type",ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-Response-Mode",ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-State",ListUtil.toList(MapUtil.getStr(paras,"frameId")));
        headers.put("X-Apple-Domain-Id",ListUtil.toList("1"));
        headers.put("X-Apple-Frame-Id",ListUtil.toList(MapUtil.getStr(paras,"frameId")));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        int xAppleHcBits = Integer.parseInt(res0.header("X-Apple-HC-Bits"));
        String xAppleHcChallenge = res0.header("X-Apple-HC-Challenge");

        String hc = WebParasUtil.generateXAppleHC(xAppleHcBits,xAppleHcChallenge);

        headers.put("X-APPLE-HC",ListUtil.toList(hc));

        JSON json = JSONUtil.parse(res1.body());

        int iter = (Integer) json.getByPath("iteration");
        String salt = (String)json.getByPath("salt");
        String b = (String) json.getByPath("b");
        String c = (String)json.getByPath("c");

        Map map = WebParasUtil.calM(MapUtil.getStr(paras,"account"), MapUtil.getStr(paras,"pwd"), a, iter, salt, b, g, n, ra);
        Map<String,String> cookiesMap;
        if(null==paras.get("cookiesMap")){
            cookiesMap=new HashMap<>();
        }else{
            cookiesMap= (Map<String, String>) paras.get("cookiesMap");
        }
        cookiesMap.put("geo","CN");
        String body = "{\"accountName\":\""+MapUtil.getStr(paras,"account")+"\",\"rememberMe\":false,\"m1\":\""+ map.get("m1") +"\",\"c\":\""+ c +"\",\"m2\":\"" + map.get("m2") +"\"}";
        HttpRequest httpRequest=HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/signin/complete?isRememberMeEnabled=true")
                .header(headers)
                .cookie(MapUtil.join(cookiesMap,";","=",true))
                .body(body);
        HttpResponse res = ProxyUtil.execute(httpRequest);


        paras.put("cookiesMap" , CookieUtils.setCookiesToMap(res,cookiesMap));
        paras.put("countryCode",res.header("X-Apple-ID-Account-Country"));

        return res;
    }

}

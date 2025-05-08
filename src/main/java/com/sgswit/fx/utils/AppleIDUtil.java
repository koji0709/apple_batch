package com.sgswit.fx.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.exception.ServiceException;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.LoginInfo;
import com.sgswit.fx.model.Question;
import com.sgswit.fx.utils.proxy.ProxyUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 */
public class AppleIDUtil {

    public static HttpResponse signin(Account account) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("X-Apple-Domain-Id", ListUtil.toList(account.getDomainId()));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(account.getFrameId()));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getFrameId()));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        String body = "{\"accountName\":\"%s\",\"password\":\"%s\",\"rememberMe\":false,\"trustTokens\":[]}";
        String scBogy = String.format(body, account.getAccount(), account.getPwd());
        String url = "https://idmsa.apple.com/appleauth/auth/signin?isRememberMeEnabled=false";
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createPost(url)
                .header(headers)
                .body(scBogy));
        account.updateLoginInfo(rsp);
        return rsp;
    }

    public static HttpResponse auth(LoginInfo loginInfo, HttpResponse signInRsp) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/html, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("X-Apple-Domain-Id", ListUtil.toList(loginInfo.getDomainId()));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(loginInfo.getFrameId()));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(loginInfo.getClientId()));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(signInRsp.header("X-Apple-ID-Session-Id")));
        headers.put("scnt",ListUtil.toList(signInRsp.header("scnt")));

        String url = "https://idmsa.apple.com/appleauth/auth";
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createPost(url)
                .header(headers)
                .cookie(loginInfo.getCookie()));

        loginInfo.updateLoginInfo(rsp);
        return rsp;
    }

    public static HttpResponse securityCode(LoginInfo loginInfo,HttpResponse authRsp) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("X-Apple-Domain-Id", ListUtil.toList(loginInfo.getDomainId()));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(loginInfo.getFrameId()));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(loginInfo.getClientId()));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(authRsp.header("X-Apple-ID-Session-Id")));
        headers.put("scnt",ListUtil.toList(authRsp.header("scnt")));

        String scDeviceBody = "{\"securityCode\":{\"code\":\"%s\"}}";
        String scPhoneBody = "{\"phoneNumber\":{\"id\":1},\"securityCode\":{\"code\":\"%s\"},\"mode\":\"sms\"}";

        String url = "";
        String body = "";

        String type = loginInfo.getSecurityCode().split("-")[0];
        String code = loginInfo.getSecurityCode().split("-")[1];

        if ("device".equals(type)) {
            url = "https://idmsa.apple.com/appleauth/auth/verify/trusteddevice/securitycode";
            body = String.format(scDeviceBody, code);
        } else if ("sms".equals(type)) {
            url = "https://idmsa.apple.com/appleauth/auth/verify/phone/securitycode";
            body = String.format(scPhoneBody, code);
        }

        HttpResponse rsp = null;
        if (!"".equals(body)) {
            rsp = ProxyUtil .execute(HttpUtil.createPost(url)
                    .header(headers)
                    .body(body)
                    .cookie(loginInfo.getCookie()));

            loginInfo.updateLoginInfo(rsp);
        }
        return rsp;
    }

    public static HttpResponse questions(Account account,HttpResponse authRsp) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/html, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("X-Apple-Domain-Id", ListUtil.toList(account.getDomainId()));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(account.getFrameId()));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getClientId()));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(authRsp.header("X-Apple-ID-Session-Id")));
        headers.put("scnt",ListUtil.toList(authRsp.header("scnt")));

        String content = authRsp.body();
        String questions = content.substring(content.indexOf("{\"direct\":{\"scriptSk7Url\""),content.indexOf("\"additional\":{\"canRoute2sv\":true}}")+35);
        questions = JSONUtil.parse(questions).getByPath("direct.twoSV.securityQuestions.questions").toString();

        List<Question> qs = JSONUtil.toList(questions, Question.class);
        for (int i = 0; i < qs.size(); i++) {
            Question q = qs.get(i);
            if (q.getNumber() == 1) {
                q.setAnswer(account.getAnswer1());
            } else if (q.getNumber() == 2) {
                q.setAnswer(account.getAnswer2());
            } else if (q.getNumber() == 3) {
                q.setAnswer(account.getAnswer3());
            }
        }

        String url = "https://idmsa.apple.com/appleauth/auth/verify/questions";
        String body = "{\"questions\":" + JSONUtil.parse(qs) + "}";
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createPost(url)
                .header(headers)
                .body(body)
                .cookie(account.getCookie()));

        account.updateLoginInfo(rsp);
        return rsp;
    }


    public static HttpResponse signin1(Account account) {
        String clientId=account.getClientId();
        String frameId=account.getFrameId();

        String nHex = "AC6BDB41324A9A9BF166DE5E1389582FAF72B6651987EE07FC3192943DB56050A37329CBB4A099ED8193E0757767A13DD52312AB4B03310DCD7F48A9DA04FD50E8083969EDB767B0CF6095179A163AB3661A05FBD5FAAAE82918A9962F0B93B855F97993EC975EEAA80D740ADBF4FF747359D041D5C33EA71D281E446B14773BCA97B43A23FB801676BD207A436C6481F1D2B9078717461A5B9D32E688F87748544523B524B0D57D5EA77A2775D2ECFA032CFBDBF52FB3786160279004E57AE6AF874E7303CE53299CCC041C7BC308D82A5698F3A8D0C38271AE35F8E9DBFBB694B5C803D89F7AE435DE236D525F54759B65E372FCD68EF20FA7111F9E4AFF73";
        BigInteger n = new BigInteger(nHex,16);
        BigInteger ra = new BigInteger(1,RandomUtil.randomBytes(32));
        String a = GiftCardUtil.calA(ra,n);

        account.setNote("正在验证账户...");

        String redirect_uri="https://appleid.apple.com";
        String url = "https://idmsa.apple.com/appleauth/auth/authorize/signin?frame_id="+frameId+"&skVersion=7&iframeId="+frameId
                +"&client_id="+clientId+"&redirect_uri="+redirect_uri+"&response_type=code&response_mode=web_message" +
                "&state="+frameId+"&authVersion=latest";

        HttpResponse signinRsp = ProxyUtil.execute(HttpUtil.createGet(url)
                .header("Upgrade-Insecure-Requests","1")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                .header("Content-Type","application/x-www-form-urlencoded")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://appleid.apple.com/")
                .header("Host","idmsa.apple.com")
        );
        account.updateLoginInfo(signinRsp);

        String body = "{\"a\":\""+a+"\",\"accountName\":\""+ account.getAccount() +"\",\"protocols\":[\"s2k\",\"s2k_fo\"]}";
        url="https://idmsa.apple.com/appleauth/auth/signin/init";
        HttpResponse signInInitRsp = ProxyUtil.execute(HttpUtil.createPost(url)
                .header("scnt",account.getScnt())
                .header("X-Apple-Auth-Attributes",signinRsp.header("X-Apple-Auth-Attributes"))
                .header("X-Apple-Widget-Key",clientId)
                .header("X-Requested-With","XMLHttpRequest")
                .header("X-Apple-OAuth-Redirect-URI"," https://appleid.apple.com")
                .header("X-Apple-OAuth-Client-Id",clientId)
                .header("X-Apple-OAuth-Client-Type","firstPartyAuth")
                .header("X-Apple-OAuth-Response-Type","code")
                .header("X-Apple-OAuth-Response-Mode","web_message")
                .header("X-Apple-OAuth-State",frameId)
                .header("X-Apple-Domain-Id","1")
                .header("X-Apple-Frame-Id",frameId)
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://appleid.apple.com/")
                .header("Host","idmsa.apple.com")
                .cookie(account.getCookie())
                .body(body)
        );
        account.updateLoginInfo(signInInitRsp);

        JSON intBodyJson=JSONUtil.parse(signInInitRsp.body());
        url="https://idmsa.apple.com/appleauth/auth/signin/complete?isRememberMeEnabled=true";
        int iter =intBodyJson.getByPath("iteration",Integer.class);
        String salt = intBodyJson.getByPath("salt",String.class);
        String b = intBodyJson.getByPath("b",String.class);
        String c = intBodyJson.getByPath("c",String.class);
        BigInteger g = new BigInteger("2");
        Map map = GiftCardUtil.calM(account.getAccount(), account.getPwd(), a, iter, salt, b, g, n, ra);
        Map<String,Object> bodyParas=new HashMap<>(){{
            put("accountName",account.getAccount());
            put("rememberMe",false);
            put("m1",map.get("m1"));
            put("c",c);
            put("m2",map.get("m2"));
        }};
        HttpResponse completeRsp = ProxyUtil.execute(HttpUtil.createPost(url)
                .header("scnt",account.getScnt())
                .header("X-Apple-Auth-Attributes",signinRsp.header("X-Apple-Auth-Attributes"))
                .header("X-Apple-Widget-Key",clientId)
                .header("X-Requested-With","XMLHttpRequest")
                .header("X-Apple-OAuth-Redirect-URI"," https://appleid.apple.com")
                .header("X-Apple-OAuth-Client-Id",clientId)
                .header("X-Apple-OAuth-Client-Type","firstPartyAuth")
                .header("X-Apple-OAuth-Response-Type","code")
                .header("X-Apple-OAuth-Response-Mode","web_message")
                .header("X-Apple-OAuth-State",frameId)
                .header("X-Apple-Domain-Id","1")
                .header("X-Apple-Frame-Id",frameId)
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://appleid.apple.com/")
                .header("Host","idmsa.apple.com")
                .cookie(account.getCookie())
                .body(JSONUtil.toJsonStr(bodyParas))
        );

        return completeRsp;
    }

    public static HttpResponse auth1(Account account, HttpResponse completeRsp) {
        String url = "https://idmsa.apple.com/appleauth/auth";
        HttpResponse authRsp = ProxyUtil.execute(HttpUtil.createGet(url)
                .header("scnt",account.getScnt())
                .header("X-Apple-Auth-Attributes",completeRsp.header("X-Apple-Auth-Attributes"))
                .header("X-Apple-Widget-Key",account.getClientId())
                .header("X-Requested-With","XMLHttpRequest")
                .header("X-Apple-ID-Session-Id",completeRsp.header("X-Apple-ID-Session-Id"))
                .header("X-Apple-OAuth-Redirect-URI"," https://appleid.apple.com")
                .header("X-Apple-OAuth-Client-Id",account.getClientId())
                .header("X-Apple-OAuth-Client-Type","firstPartyAuth")
                .header("X-Apple-OAuth-Response-Type","code")
                .header("X-Apple-OAuth-Response-Mode","web_message")
                .header("X-Apple-OAuth-State",account.getFrameId())
                .header("X-Apple-Domain-Id","1")
                .header("X-Apple-Frame-Id",account.getFrameId())
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","text/html")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://idmsa.apple.com/appleauth/auth/signin?widgetKey=af1139274f266b22b68c2a3e7ad932cb3c0bbe854e13a79af78dcc73136882c3&language=zh_CN")
                .header("Host","idmsa.apple.com")
                .cookie(account.getCookie())
        );
        account.updateLoginInfo(authRsp);
        return authRsp;
    }

    public static HttpResponse securityCode1(Account account,HttpResponse authRsp) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com/"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("Sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("Sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("Sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("Sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("Sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("Sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        headers.put("X-Apple-App-Id", ListUtil.toList(account.getClientId()));
        headers.put("X-Apple-Auth-Attributes",ListUtil.toList(authRsp.header("X-Apple-Auth-Attributes")));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getClientId()));

        headers.put("X-Apple-Domain-Id", ListUtil.toList("35"));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(account.getFrameId()));

        headers.put("X-Apple-OAuth-Client-Id",ListUtil.toList(account.getClientId()));
        headers.put("X-Apple-OAuth-Client-Type", ListUtil.toList("firstPartyAuth"));
        headers.put("X-Apple-OAuth-Redirect-URI", List.of("https://appleid.apple.com"));
        headers.put("X-Apple-OAuth-Require-Grant-Code", ListUtil.toList("true"));
        headers.put("X-Apple-OAuth-Response-Mode",ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-Response-Type",ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-State",ListUtil.toList(account.getClientId()));

        headers.put("X-Apple-Offer-Security-Upgrade", ListUtil.toList("1"));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));

        headers.put("Scnt",List.of(authRsp.header("scnt")));
        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(authRsp.header("X-Apple-ID-Session-Id")));


        String scDeviceBody = "{\"securityCode\":{\"code\":\"%s\"}}";
        String scPhoneBody = "{\"phoneNumber\":{\"id\":1},\"securityCode\":{\"code\":\"%s\"},\"mode\":\"sms\"}";

        String url = "";
        String body = "";

        String type = account.getSecurityCode().split("-")[0];
        String code = account.getSecurityCode().split("-")[1];

        if ("device".equals(type)) {
            url = "https://idmsa.apple.com/appleauth/auth/verify/trusteddevice/securitycode";
            body = String.format(scDeviceBody, code);
        } else if ("sms".equals(type)) {
            url = "https://idmsa.apple.com/appleauth/auth/verify/phone/securitycode";
            body = String.format(scPhoneBody, code);
        }

        HttpResponse rsp = null;
        if (!"".equals(body)) {
            rsp = ProxyUtil .execute(HttpUtil.createPost(url)
                            .header(headers)
                            .body(body)
                            .cookie(account.getCookie()));

            account.updateLoginInfo(rsp);
        }
        return rsp;
    }

    public static HttpResponse questions1(Account account,HttpResponse authRsp) {
        String boot_args=StrUtils.getScriptByClass(authRsp.body(),"boot_args");
        String questions = JSONUtil.parse(boot_args).getByPath("direct.twoSV.securityQuestions.questions",String.class);
        List<Question> qs = JSONUtil.toList(questions, Question.class);
        for (int i = 0; i < qs.size(); i++) {
            Question q = qs.get(i);
            if (q.getNumber() == 1) {
                q.setAnswer(account.getAnswer1());
            } else if (q.getNumber() == 2) {
                q.setAnswer(account.getAnswer2());
            } else if (q.getNumber() == 3) {
                q.setAnswer(account.getAnswer3());
            }
        }
        String url = "https://idmsa.apple.com/appleauth/auth/verify/questions";
        String body = "{\"questions\":" + JSONUtil.parse(qs) + "}";
        HttpResponse questionRsp = ProxyUtil.execute(HttpUtil.createPost(url)
                .header("X-Apple-App-Id",account.getClientId())
                .header("scnt",account.getScnt())
                .header("X-Apple-Auth-Attributes",authRsp.header("X-Apple-Auth-Attributes"))
                .header("X-Apple-Widget-Key",account.getClientId())
                .header("X-Requested-With","XMLHttpRequest")
                .header("X-Apple-ID-Session-Id",authRsp.header("X-Apple-ID-Session-Id"))
                .header("X-Apple-OAuth-Redirect-URI"," https://appleid.apple.com")
                .header("X-Apple-OAuth-Client-Id",account.getClientId())
                .header("X-Apple-OAuth-Client-Type","firstPartyAuth")
                .header("X-Apple-OAuth-Response-Type","code")
                .header("X-Apple-OAuth-Response-Mode","web_message")
                .header("X-Apple-OAuth-State",account.getFrameId())
                .header("X-Apple-Domain-Id","1")
                .header("X-Apple-Frame-Id",account.getFrameId())
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://idmsa.apple.com/appleauth/auth/signin?widgetKey="+account.getClientId()+"&language=zh_CN")
                .header("Host","idmsa.apple.com")
                .body(body)
                .cookie(account.getCookie()));
        account.updateLoginInfo(questionRsp);
       return questionRsp;
    }

    public static HttpResponse accountRepair(Account account,HttpResponse questionRsp) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));

        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("Sec-Fetch-Dest",ListUtil.toList("iframe"));
        headers.put("Sec-Fetch-Mode",ListUtil.toList("navigate"));
        headers.put("navigate",ListUtil.toList("same-site"));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        // https://appleid.apple.com/widget/account/repair?widgetKey=xx&rv=1&language=zh_CN_CHN#!repair
        String location = questionRsp.header("Location");
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createGet(location)
                        .header(headers)
                        .cookie(account.getCookie()));

        account.updateLoginInfo(rsp);
        return rsp;
    }

    public static HttpResponse repareOptions(Account account,HttpResponse questionRsp,HttpResponse accountRepairRsp) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-Domain-Id", ListUtil.toList(account.getDomainId()));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(account.getFrameId()));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getClientId()));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));


        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(questionRsp.header("X-Apple-ID-Session-Id")));
        headers.put("scnt",ListUtil.toList(accountRepairRsp.header("scnt")));

        headers.put("X-Apple-Skip-Repair-Attributes",ListUtil.toList("[]"));
        headers.put("X-Apple-Session-Token",ListUtil.toList(questionRsp.header("X-Apple-Repair-Session-Token")));

        String url = "https://appleid.apple.com/account/manage/repair/options";
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers)
                        .cookie(account.getCookie()));

        account.updateLoginInfo(rsp);
        return rsp;
    }

    public static HttpResponse repareOptions2(Account account,HttpResponse signInRes,String XAppleIDSessionId) {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getClientId()));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("X-Apple-OAuth-Context",ListUtil.toList(signInRes.header("X-Apple-OAuth-Context")));
        headers.put("X-Apple-Skip-Repair-Attributes",ListUtil.toList("[]"));
        headers.put("X-Apple-Offer-Security-Upgrade",ListUtil.toList("1"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        headers.put("Referer",ListUtil.toList("https://appleid.apple.com/"));

        headers.put("scnt",ListUtil.toList(account.getScnt()));
        headers.put("X-Apple-Session-Token",ListUtil.toList(signInRes.header("X-Apple-Repair-Session-Token")));
        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(XAppleIDSessionId));


        String url = "https://appleid.apple.com/account/manage/repair/options";
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createGet(url)
                .header(headers)
                .cookie(account.getCookie()));
        account.updateLoginInfo(rsp);
        return rsp;
    }

    public static HttpResponse repareBirthday(Account account,HttpResponse signInRes,String XAppleIDSessionId,String birthday) {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getClientId()));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("X-Apple-OAuth-Context",ListUtil.toList(signInRes.header("X-Apple-OAuth-Context")));
        headers.put("X-Apple-Skip-Repair-Attributes",ListUtil.toList("[]"));
        headers.put("X-Apple-Offer-Security-Upgrade",ListUtil.toList("1"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        headers.put("Referer",ListUtil.toList("https://appleid.apple.com/"));

        headers.put("scnt",ListUtil.toList(account.getScnt()));
        headers.put("X-Apple-Session-Token",ListUtil.toList(signInRes.header("X-Apple-Repair-Session-Token")));
        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(XAppleIDSessionId));


        String url = "https://appleid.apple.com/account/manage/repair";
        String body = "{\n" +
                "  \"security\": {\n" +
                "    \"birthday\": \""+birthday+"\"\n" +
                "  }\n" +
                "}";
        HttpRequest request = HttpUtil.createRequest(Method.PUT, url)
                .header(headers)
                .body(body)
                .cookie(account.getCookie());
        HttpResponse rsp = ProxyUtil.execute(request);
        account.updateLoginInfo(rsp);
        return rsp;
    }

    public static HttpResponse repareQuestions(Account account,HttpResponse signInRes,String XAppleIDSessionId,String body) {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getClientId()));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("X-Apple-OAuth-Context",ListUtil.toList(signInRes.header("X-Apple-OAuth-Context")));
        headers.put("X-Apple-Skip-Repair-Attributes",ListUtil.toList("[]"));
        headers.put("X-Apple-Offer-Security-Upgrade",ListUtil.toList("1"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        headers.put("Referer",ListUtil.toList("https://appleid.apple.com/"));

        headers.put("scnt",ListUtil.toList(account.getScnt()));
        headers.put("X-Apple-Session-Token",ListUtil.toList(signInRes.header("X-Apple-Repair-Session-Token")));
        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(XAppleIDSessionId));

        String url = "https://appleid.apple.com/account/manage/repair/questions";
        HttpRequest request = HttpUtil.createRequest(Method.PUT, url)
                .header(headers)
                .body(body)
                .cookie(account.getCookie());
        HttpResponse rsp = ProxyUtil.execute(request);
        account.updateLoginInfo(rsp);
        return rsp;
    }

    public static HttpResponse privacyAccept(Account account,HttpResponse signInRes,String XAppleIDSessionId){
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Host",ListUtil.toList("appleid.apple.com"));

        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getClientId()));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("X-Apple-OAuth-Context",ListUtil.toList(signInRes.header("X-Apple-OAuth-Context")));
        headers.put("X-Apple-Skip-Repair-Attributes",ListUtil.toList("[hsa2_enrollment]"));
        headers.put("X-Apple-Offer-Security-Upgrade",ListUtil.toList("1"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        headers.put("Referer",ListUtil.toList("https://appleid.apple.com/"));

        headers.put("scnt",ListUtil.toList(account.getScnt()));
        headers.put("X-Apple-Session-Token",ListUtil.toList(signInRes.header("X-Apple-Repair-Session-Token")));
        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(XAppleIDSessionId));

        String url = "https://appleid.apple.com/account/manage/privacy/accept";
        HttpRequest request = HttpUtil.createRequest(Method.PUT, url)
                .header(headers)
                .cookie(account.getCookie());
        HttpResponse rsp = ProxyUtil.execute(request);
        account.updateLoginInfo(rsp);
        return rsp;
    }

    public static HttpResponse repareComplete1(Account account,HttpResponse signInRes,String XAppleIDSessionId) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        headers.put("X-Apple-Domain-Id", ListUtil.toList(account.getDomainId()));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(account.getFrameId()));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getClientId()));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(XAppleIDSessionId));
        headers.put("scnt",ListUtil.toList(signInRes.header("scnt")));

        String url = "https://idmsa.apple.com/appleauth/auth/repair/complete";
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createPost(url)
                .header(headers)
                .cookie(account.getCookie()));
        account.updateLoginInfo(rsp);
        return rsp;
    }

    public static HttpResponse securityUpgrade(Account account,HttpResponse repareOptionsRsp,String XAppleIDSessionId,String scnt) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-Domain-Id", List.of(account.getDomainId()));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(account.getFrameId()));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getClientId()));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(XAppleIDSessionId));
        headers.put("scnt",ListUtil.toList(scnt));
        headers.put("X-Apple-Session-Token",ListUtil.toList(repareOptionsRsp.header("X-Apple-Session-Token")));
        headers.put("X-Apple-Skip-Repair-Attributes",ListUtil.toList("[]"));

        String url = "https://appleid.apple.com/account/security/upgrade";
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers)
                        .cookie(account.getCookie()));

        account.updateLoginInfo(rsp);
        return rsp;
    }

    public static HttpResponse securityUpgradeSetuplater(Account account,HttpResponse securityUpgradeRsp,String XAppleIDSessionId,String scnt) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-Domain-Id", ListUtil.toList(account.getDomainId()));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(account.getFrameId()));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getClientId()));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(XAppleIDSessionId));
        headers.put("scnt",ListUtil.toList(scnt));
        headers.put("X-Apple-Session-Token",ListUtil.toList(securityUpgradeRsp.header("X-Apple-Session-Token")));
        headers.put("X-Apple-Skip-Repair-Attributes",ListUtil.toList("[]"));

        String url = "https://appleid.apple.com/account/security/upgrade/setuplater";
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers)
                        .cookie(account.getCookie()));

        account.updateLoginInfo(rsp);
        return rsp;
    }

    public static HttpResponse repareOptionsSecond(Account account,HttpResponse securityUpgradeSetuplaterRsp,String XAppleIDSessionId,String scnt) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-Domain-Id", ListUtil.toList(account.getDomainId()));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(account.getFrameId()));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getClientId()));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(XAppleIDSessionId));
        headers.put("scnt",ListUtil.toList(scnt));
        headers.put("X-Apple-Session-Token",ListUtil.toList(securityUpgradeSetuplaterRsp.header("X-Apple-Session-Token")));
        headers.put("X-Apple-Skip-Repair-Attributes",ListUtil.toList("[\"hsa2_enrollment\"]"));

        String url = "https://appleid.apple.com/account/manage/repair/options";
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers)
                        .cookie(account.getCookie()));

        account.updateLoginInfo(rsp);
        return rsp;
    }

    public static HttpResponse repareComplete(Account account,HttpResponse repareOptionsSecondRsp,HttpResponse questionRsp) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("X-Apple-Domain-Id", ListUtil.toList(account.getDomainId()));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(account.getFrameId()));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getClientId()));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(questionRsp.header("X-Apple-ID-Session-Id")));
        headers.put("scnt",ListUtil.toList(questionRsp.header("scnt")));
        headers.put("X-Apple-Repair-Session-Token",ListUtil.toList(repareOptionsSecondRsp.header("X-Apple-Session-Token")));

        String url = "https://idmsa.apple.com/appleauth/auth/repair/complete";
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createPost(url)
                        .header(headers)
                        .cookie(account.getCookie()));

        account.updateLoginInfo(rsp);


        return rsp;
    }

    public static HttpResponse token(Account account,HttpResponse securityCodeOrReparCompleteRsp) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
//
        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));
//
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Apple-I-Request-Context",ListUtil.toList("ca"));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("scnt",ListUtil.toList(securityCodeOrReparCompleteRsp.header("scnt")));

        String url = "https://appleid.apple.com/account/manage/gs/ws/token";
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers)
                        .cookie(account.getCookie()));

        account.updateLoginInfo(rsp);
        account.setXAppleIDSessionId(rsp.header("X-Apple-ID-Session-Id"));
        return rsp;
    }

    /**
     * 获取账户信息
     */
    public static HttpResponse account(Account account) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        headers.put("User-Agent",ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(account.getXAppleIDSessionId()));
        headers.put("scnt",ListUtil.toList(account.getScnt()));

        String url = "https://appleid.apple.com/account/manage";
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers)
                        .cookie(account.getCookie()));
        return rsp;
    }

    /**
     * 修改用户生日信息
     * @param birthday 生日 yyyy-MM-dd
     */
    public static HttpResponse updateBirthday(Account account,String birthday) {
        String url = "https://appleid.apple.com/account/manage/security/birthday";
        String[] birthdayArr = birthday.split("-");
        String format = "{\"dayOfMonth\":\"%s\",\"monthOfYear\":\"%s\",\"year\":\"%s\"}";
        String body = String.format(format, birthdayArr[2], birthdayArr[1], birthdayArr[0]);

        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createRequest(Method.PUT, url)
                        .header("X-Apple-Widget-Key","cbf64fd6843ee630b463f358ea0b707b")
                        .header("X-Requested-With","XMLHttpRequest")
                        .header("X-Apple-Domain-Id","1")
                        .header("X-Apple-Api-Key","cbf64fd6843ee630b463f358ea0b707b")
                        .header("Accept-Encoding","gzip, deflate, br")
                        .header("Accept-Language","zh-CN,zh;q=0.9")
                        .header("Accept","application/json")
                        .header("Content-Type","application/json")
                        .header("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36")
                        .header("Referer","https://appleid.apple.com/account/manage")
                        .header("Host","appleid.apple.com")
                        .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                        .header("scnt",account.getScnt())
                        .body(body)
                        .cookie(account.getCookie()),false);
        return rsp;
    }

    /**
     * 移除救援邮箱
     */
    public static HttpResponse deleteRescueEmail(Account account) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("scnt", ListUtil.toList(account.getScnt()));

        headers.put("Origin",ListUtil.toList("https://appleid.apple.com"));
        headers.put("Referer",ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Apple-I-Request-Context",ListUtil.toList("ca"));
        headers.put("X-Apple-Api-Key",ListUtil.toList("cbf64fd6843ee630b463f358ea0b707b"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        String url = "https://appleid.apple.com/account/manage/security/email/rescue";
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createRequest(Method.DELETE, url)
                        .header(headers)
                        .cookie(account.getCookie()),false);

        int status = rsp.getStatus();


        if(status == 302){
            return rsp;
        }

        // 需要验证密码
        if (status == 451){
            verifyPassword(rsp,account);
            return deleteRescueEmail(account);
        }

        return rsp;
    }

    /**
     * 新增救援邮箱前置
     */
    public static HttpResponse addRescueEmailSendVerifyCode(Account account) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("scnt", ListUtil.toList(account.getScnt()));

        headers.put("Origin",ListUtil.toList("https://appleid.apple.com"));
        headers.put("Referer",ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Apple-I-Request-Context",ListUtil.toList("ca"));
        headers.put("X-Apple-Api-Key",ListUtil.toList("cbf64fd6843ee630b463f358ea0b707b"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        String url = "https://appleid.apple.com/account/manage/security/email/rescue/verification";

        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createRequest(Method.POST, url)
                        .header(headers)
                        .body("{\"address\":\""+account.getEmail()+"\"}")
                        .cookie(account.getCookie()),false);

        int status = rsp.getStatus();


        // 需要验证密码
        if (status == 451){
            verifyPassword(rsp,account);
            return addRescueEmailSendVerifyCode(account);
        }
        return rsp;
    }

    /**
     * 新增救援邮箱
     */
    public static HttpResponse addRescueEmail(HttpResponse verifyRsp,Account account,String answer) {
        String url = "https://appleid.apple.com/account/manage/security/email/rescue/verification";
        String body = "{\"address\":\""+account.getEmail()+"\",\"verificationInfo\":{\"id\":\""+JSONUtil.parse(verifyRsp.body()).getByPath("verificationId")+"\",\"answer\":\""+answer+"\"}}";
        HttpRequest request=HttpUtil.createRequest(Method.PUT, url)
                .header("Accept","application/json, text/plain, */*")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Cache-Control","no-cache")

                .header("Content-Type","application/json")
                .header("Host","appleid.apple.com")
                .header("Origin"," https://appleid.apple.com")
                .header("Pragma","no-cache")
                .header("Referer","https://appleid.apple.com/")
                .header("Sec-Fetch-Dest","empty")
                .header("Sec-Fetch-Mode","cors")
                .header("Sec-Fetch-Site","same-origin")
                .header("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                .header("X-Apple-Api-Key","cbf64fd6843ee630b463f358ea0b707b")
                .header("X-Apple-I-FD-Client-Info","{\"U\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36\",\"L\":\"zh-CN\",\"Z\":\"GMT+08:00\",\"V\":\"1.1\",\"F\":\"Fla44j1e3NlY5BNlY5BSmHACVZXnN92hpu__Iq1JlQxQeLaD.SAuXjodUW1BNork0ugN.xL4FeHRJdlU9_y4AwcGY5BNlYJNNlY5QB4bVNjMk.2IL\"}")
                .header("X-Apple-I-Request-Context","ca")
                .header("X-Apple-I-TimeZone","Asia/Shanghai")
                .header("scnt",verifyRsp.header("scnt"))
                .header("sec-ch-ua"," \"Chromium\";v=\"118\", \"Google Chrome\";v=\"118\", \"Not=A?Brand\";v=\"99\"")
                .header("sec-ch-ua-mobile","?0")
                .header("sec-ch-ua-platform","macOS")
                .body(body);
        HttpResponse rsp = ProxyUtil.execute(request,false);
        return rsp;
    }

    /**
     * 修改名称
     */
    public static HttpResponse updateName(Account account,String password,String firstName,String lastName) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("scnt", ListUtil.toList(account.getScnt()));

        headers.put("Origin",ListUtil.toList("https://appleid.apple.com"));
        headers.put("Referer",ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Apple-I-Request-Context",ListUtil.toList("ca"));
        headers.put("X-Apple-Api-Key",ListUtil.toList("cbf64fd6843ee630b463f358ea0b707b"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        String url = "https://appleid.apple.com/account/manage/name";
        String body = String.format("{\"firstName\":\"%s\",\"middleName\":\"\",\"lastName\":\"%s\"}",firstName,lastName);

        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createRequest(Method.PUT, url)
                .header(headers)
                .cookie(account.getCookie())
                .body(body));

        int status = rsp.getStatus();


        // 需要验证密码
        if (status == 451){
            verifyPassword(rsp,account);
            return updateName(account,password,firstName,lastName);
        }
        return rsp;
    }

    /**
     * 修改密码
     */
    public static HttpResponse updatePassword(Account account,String password,String newPassword){
        String url = "https://appleid.apple.com/account/manage/security/password";
        String body = String.format("{\"currentPassword\":\"%s\",\"newPassword\":\"%s\"}",password,newPassword);
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createRequest(Method.PUT, url)
                .header("X-Apple-Widget-Key","cbf64fd6843ee630b463f358ea0b707b")
                .header("X-Requested-With","XMLHttpRequest")
                .header("X-Apple-Domain-Id","1")
                .header("X-Apple-Api-Key","cbf64fd6843ee630b463f358ea0b707b")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json")
                .header("Content-Type","application/json")
                .header("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36")
                .header("Referer","https://appleid.apple.com/account/manage")
                .header("Host","appleid.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("scnt",account.getScnt())
                .cookie(account.getCookie())
                .body(body));
        return rsp;
    }

    /**
     * 修改密保
     */
    public static HttpResponse updateQuestions(Account account,String body){
        String url = "https://appleid.apple.com/account/manage/security/questions";
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createRequest(Method.PUT, url)

                .header("X-Apple-Widget-Key","cbf64fd6843ee630b463f358ea0b707b")
                .header("X-Requested-With","XMLHttpRequest")
                .header("X-Apple-Domain-Id","1")
                .header("X-Apple-Api-Key","cbf64fd6843ee630b463f358ea0b707b")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json")
                .header("Content-Type","application/json")
                .header("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36")
                .header("Referer","https://appleid.apple.com/account/manage")
                .header("Host","appleid.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("scnt",account.getScnt())
                .cookie(account.getCookie())
                .body(body));

        int status = rsp.getStatus();
        // 需要验证密码
        if (status == 451){
            verifyPassword(rsp,account);
            return updateQuestions(account,body);
        }
        return rsp;
    }

    /**
     * 验证密码
     */
    public static HttpResponse verifyPassword(HttpResponse rsp,Account account){
        String verifyPasswordUrl = "https://appleid.apple.com" + rsp.header("Location");
        HttpResponse rsp1 = ProxyUtil.execute(HttpUtil.createRequest(Method.POST, verifyPasswordUrl)
                        .body("{\"password\":\""+account.getPwd()+"\"}")
                        .header(rsp.headers())
                        .cookie(account.getCookie()));
        return rsp1;
    }

    /**
     * 获取设备列表
     */
    public static HttpResponse getDeviceList(Account account){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("scnt", ListUtil.toList(account.getScnt()));

        headers.put("Origin",ListUtil.toList("https://appleid.apple.com"));
        headers.put("Referer",ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Apple-I-Request-Context",ListUtil.toList("ca"));
        headers.put("X-Apple-Api-Key",ListUtil.toList("cbf64fd6843ee630b463f358ea0b707b"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        String url = "https://appleid.apple.com/account/manage/security/devices";
        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers)
                        .cookie(account.getCookie()));

        return rsp;
    }

    /**
     * 移除所有设备
     */
    public static void removeDevices(Account account,List<String> deviceIdList){
            for (String deviceId : deviceIdList) {
                String url = "https://appleid.apple.com/account/manage/security/devices/" + deviceId;
                HttpResponse rsp = ProxyUtil.execute(HttpUtil.createRequest(Method.DELETE,url)
                    .header("X-Apple-Widget-Key","cbf64fd6843ee630b463f358ea0b707b")
                    .header("X-Requested-With","XMLHttpRequest")
                    .header("X-Apple-Domain-Id","1")
                    .header("X-Apple-Api-Key","cbf64fd6843ee630b463f358ea0b707b")
                    .header("Accept-Encoding","gzip, deflate, br")
                    .header("Accept-Language","zh-CN,zh;q=0.9")
                    .header("Accept","application/json")
                    .header("Content-Type","application/json")
                    .header("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36")
                    .header("Referer","https://appleid.apple.com/account/manage")
                    .header("Host","appleid.apple.com")
                    .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                    .header("scnt",account.getScnt())
                    .cookie(account.getCookie()));
            }
    }

    /**
     * 修改显示语言
     */
    public static HttpResponse changeShowLanguage(Account account,String lang){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("scnt", ListUtil.toList(account.getScnt()));

        headers.put("Origin",ListUtil.toList("https://appleid.apple.com"));
        headers.put("Referer",ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Apple-I-Request-Context",ListUtil.toList("ca"));
        headers.put("X-Apple-Api-Key",ListUtil.toList("cbf64fd6843ee630b463f358ea0b707b"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        String url = "https://appleid.apple.com/account/manage/preferences";
        String format = "{\"preferredLanguage\":\"%s\",\"marketingPreferences\":{\"appleUpdates\":true,\"iTunesUpdates\":true,\"appleNews\":false,\"appleMusic\":false},\"privacyPreferences\":{\"allowDeviceDiagnosticsAndUsage\":false,\"allowShareThirdPartyDevelopers\":false,\"allowICloudDataAnalytics\":false}}";
        String body = String.format(format, lang);

        HttpResponse rsp = ProxyUtil.execute(HttpUtil.createRequest(Method.PUT, url)
                        .body(body)
                        .header(headers)
                        .cookie(account.getCookie()));
        return rsp;
    }

    /**
     * 修改appleId时发送邮件
     */
    public static HttpResponse updateAppleIdSendVerifyCode(Account account){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("scnt", ListUtil.toList(account.getScnt()));

        headers.put("Origin",ListUtil.toList("https://appleid.apple.com"));
        headers.put("Referer",ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Apple-I-Request-Context",ListUtil.toList("ca"));
        headers.put("X-Apple-Api-Key",ListUtil.toList("cbf64fd6843ee630b463f358ea0b707b"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        String url = "https://appleid.apple.com/account/manage/appleid/verification";
        String body = "{\"name\":\""+account.getEmail()+"\"}";
        HttpResponse verifyRsp = ProxyUtil.execute(HttpUtil.createPost(url)
                        .header(headers)
                        .cookie(account.getCookie())
                        .body(body));

        int status = verifyRsp.getStatus();

        // 需要验证密码
        if (status == 451){
            verifyPassword(verifyRsp,account);
            return updateAppleIdSendVerifyCode(account);
        }
        return verifyRsp;
    }

    /**
     * 修改appleId
     */
    public static HttpResponse updateAppleId(HttpResponse verifyRsp,Account account,String verifyCode){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("scnt", ListUtil.toList(account.getScnt()));

        headers.put("Origin",ListUtil.toList("https://appleid.apple.com"));
        headers.put("Referer",ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Apple-I-Request-Context",ListUtil.toList("ca"));
        headers.put("X-Apple-Api-Key",ListUtil.toList("cbf64fd6843ee630b463f358ea0b707b"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        String verifyId = JSONUtil.parse(verifyRsp.body()).getByPath("verificationId",String.class);
        String url = "https://appleid.apple.com/account/manage/appleid/verification";
        String body = "{\"name\":\""+account.getEmail()+"\",\"verificationInfo\":{\"id\":\""+verifyId+"\",\"answer\":\""+verifyCode+"\"}}";
        HttpResponse updateAppleIdRsp = ProxyUtil.execute(HttpUtil.createRequest(Method.PUT,url)
                        .header(headers)
                        .cookie(account.getCookie())
                        .body(body),false);
        return updateAppleIdRsp;
    }

    /**
     * 双重开通发送短信
     */
    public static HttpResponse securityUpgradeVerifyPhone(Account account,String body){
        //step8 phone
        HttpResponse questionsResp = (HttpResponse) account.getAuthData().get("questionsResp");
        HttpResponse optionsResp = (HttpResponse) account.getAuthData().get("optionsResp");
        account.setNote("正在发送验证码...");
        String url ="";
        if(body.contains("acceptedWarnings")){
            String acceptedWarnings= JSONUtil.parse(body).getByPath("acceptedWarnings",String[].class)[0];
            url = "https://appleid.apple.com/account/security/upgrade/verify/phone?warnings="+acceptedWarnings;
        }else{
            url = "https://appleid.apple.com/account/security/upgrade/verify/phone";
        }
        HttpResponse phoneResp = ProxyUtil.execute(HttpUtil.createRequest(Method.PUT,url)

                .header("scnt",account.getScnt())
                .header("X-Requested-With","XMLHttpRequest")
                .header("X-Apple-Skip-Repair-Attributes","[]")
                .header("X-Apple-OAuth-Context",questionsResp.header("X-Apple-OAuth-Context"))
                .header("X-Apple-Session-Token",optionsResp.header("X-Apple-Session-Token"))
                .header("X-Apple-ID-Session-Id",account.getXAppleIDSessionId())
                .header("X-Apple-Widget-Key",account.getClientId())
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://appleid.apple.com/widget/account/repair?widgetKey="+account.getClientId()+"&language=zh_CN_CHN")
                .header("Host","appleid.apple.com")
                .body(body)
                .cookie(account.getCookie()));
        // 需要验证密码
        if (phoneResp.getStatus() == 451){
            verifyPassword(phoneResp,account);
            return securityUpgradeVerifyPhone(account,body);
        }
        account.updateLoginInfo(phoneResp);
        return phoneResp;
    }

    /**
     * 开通双重认证
     * @param body {"phoneNumberVerification":{"phoneNumber":{"id":20101,"number":"17608177103","countryCode":"CN","nonFTEU":true},"securityCode":{"code":"563973"},"mode":"sms"}}
     */
    public static HttpResponse securityUpgrade(Account account,String body){
        HttpResponse questionsResp = (HttpResponse) account.getAuthData().get("questionsResp");
        HttpResponse optionsResp = (HttpResponse) account.getAuthData().get("optionsResp");
        String url ="";
        if(body.contains("acceptedWarnings")){
            String acceptedWarnings= JSONUtil.parse(body).getByPath("acceptedWarnings",String[].class)[0];
            url = "https://appleid.apple.com/account/security/upgrade?warnings="+acceptedWarnings;
        }else{
            url = "https://appleid.apple.com/account/security/upgrade";
        }

        HttpResponse securityUpgradeRsp = ProxyUtil.execute(HttpUtil.createRequest(Method.POST,url)
                .header("X-Requested-With","XMLHttpRequest")
                .header("X-Apple-Widget-Key",account.getClientId())
                .header("X-Apple-Skip-Repair-Attributes","[]")
                .header("X-Apple-OAuth-Context",questionsResp.header("X-Apple-OAuth-Context"))
                .header("X-Apple-Session-Token",optionsResp.header("X-Apple-Session-Token"))
                .header("X-Apple-ID-Session-Id",account.getXAppleIDSessionId())
                .header("X-Apple-I-Request-Context","ca")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://appleid.apple.com/widget/account/repair?widgetKey="+account.getClientId()+"&rv=1&language=zh_CN_CHN")
                .header("Host","appleid.apple.com")
                .header("scnt",account.getScnt())
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .body(body)
                .cookie(account.getCookie()),false);
        return securityUpgradeRsp;
    }

    /**
     * 密保关闭双重认证
     */
    public static HttpResponse securityDowngrade(HttpResponse verifyAppleIdRsp,Account account,String newPwd,String sstt) {
        //https://iforgot.apple.com/password/verify/phone?sstt=ApTHVltxmj9n81xmZ2Yq%2FpaR9qJmd2fhKGNI4KkIZbd4YOP46XX9LnzLcQDgQ9S9X8O5ZpTrQPW0NDFGe2duTVlhMatZp6KUnnNkxihdnAHhoMkBkPR1LmxBXyEx9K4Y9veQVQERw4t35yemhEHLzDi8V6HMZS7c5FLlxVgtdp2NRidZm0FUursk70ApQgPLK%2B8ad2UdvotfSMnVCzKWnF2PWg9xSkR4xP7%2BwDRkE7Ayi8NHVIdqrxjyHS6E1X4mzmWi%2FiECjvlXcR9Y8gnPTnE%2BmYjcl4ZmQHzWh2pkUhdoyVBGfUKOyxc5pWAOlFRbi2x8dvoY%2BSgciRRpxIApnxQPusn%2BhTfFBd%2BIUQFfAJ3ly6%2FgUmL%2FFI45CCLGZW3b7%2Bf79iPXKhDqaEof%2FTCeFuEQydMDpk6bC0ZCUhPYgZ0bZHtYf2hJDzlUqT2aPZXiJ0wk7m7nSz4crhKVhwY8OfBd0STFxQ%3D%3D
        String host = "https://iforgot.apple.com";
        String verifyPhone1Location = verifyAppleIdRsp.header("Location");
        account.setNote("正在检测账户...");
        HttpResponse verifyPhone1Rsp = ProxyUtil.execute(HttpUtil.createGet(host + verifyPhone1Location)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json; charset=utf-8")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",sstt)
                .cookie(account.getCookie())
        );
        account.updateLoginInfo(verifyPhone1Rsp);

        JSONObject bodyJSON = JSONUtil.parseObj(verifyPhone1Rsp.body());
        Boolean containsTrustedPhones = bodyJSON.containsKey("trustedPhones");
        LoggerManger.info("【关闭双重认证】检测账号认证状态: containsTrustedPhones = " + containsTrustedPhones);
        if (containsTrustedPhones == null || !containsTrustedPhones){
            throw new ServiceException("该账号没有双重认证");
        }

        Boolean recoverable = JSONUtil.parse(verifyPhone1Rsp.body()).getByPath("recoverable",Boolean.class);
        LoggerManger.info("【关闭双重认证】检测账号认证状态: recoverable = " + recoverable);
        if (recoverable == null || !recoverable){
            throw new ServiceException("此账号开通双重认证已超过两周, 不能关闭双重");
        }

        account.setNote("正在验证手机号");
        ThreadUtil.sleep(500);
        HttpResponse verifyPhone2Rsp = ProxyUtil.execute(HttpUtil.createGet(host + "/password/verify/phone")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","*")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",verifyPhone1Rsp.header("sstt"))
                .cookie(account.getCookie())
        );
        account.updateLoginInfo(verifyPhone2Rsp);
        ThreadUtil.sleep(300);
        String boot_args= StrUtils.getScriptById(verifyPhone2Rsp.body(),"boot_args");
        sstt=JSONUtil.parse(boot_args).getByPath("data.sstt", String.class);
        try {
            sstt = URLEncoder.encode(sstt, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpResponse unenrollmentRsp = ProxyUtil.execute(HttpUtil.createPost(host + "/password/verify/phone/unenrollment")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("sstt",sstt)
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .cookie(account.getCookie())
        );
        account.updateLoginInfo(unenrollmentRsp);
        String verifyBirthday1Location = unenrollmentRsp.header("Location");
        if(verifyBirthday1Location.contains("/session/timeout")){
            throw new ServiceException("双重关闭失败");
        }
        account.setNote("手机号验证通过...");

        ThreadUtil.sleep(300);
        account.setNote("正在验证生日");
        ThreadUtil.sleep(300);

        //https://iforgot.apple.com/unenrollment/verify/birthday?sstt=aRsmTXWvxfqFqOEhEPBow9iXHopDrYMw3EQ2S1JxrX8cKT0y1Ag2DKQsDfg8PECYQsT9TFijbhoW4cT9WsiAFA2%2B99gP%2FJbdYXXryEEF6BQ2ZnrB8WRRTwkX5jahY2PgA8tCrgS855jGQP7veAlIrAydKNkb173GYZyv0224s0g28wWRoSj%2Fs7e%2By0OSgwLan4Q5UHf%2FrF3Wut2dyW5nEf8gj1ZFzIbfmvbgjvcE9jc3mZ3MMjLfP8Rmz3NYWzTYVi4Gv%2BYSjw%2BLwYiJTvqFLS%2F0jsgXXb98iNfCKqmhVBcbxXlK1EGu7BoeBgXcu34rsqbUHBX0laqCEyc3vInw%2Fh%2B%2FgRGoF3fulYsNN8Qj8tCMa7N73VqvMFUS4%2Fa35QpzuiePKhA8oxIZnn7BkkyDbfWjxJiF%2FXujCZdmId7TPg8RBt8kD%2FK2ffac3uMgP8LHndq2BMPHPcyUQZxNt6FjtjNHzi8%2FXp9ZcCZFVzwKMm2SqN0hJnhH2qi2rqXZ6JFPTGAgUg%3D%3D
        HttpResponse verifyBirthday1Rsp = ProxyUtil.execute(HttpUtil.createGet(host + verifyBirthday1Location)
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json; charset=utf-8")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",sstt)
                .cookie(account.getCookie())
        );
        account.updateLoginInfo(verifyBirthday1Rsp);

        DateTime birthday=null;
        try{
            birthday = DateUtil.parse(account.getBirthday());
        }catch (Exception e){
            throw new ServiceException("生日验证失败");
        }
        ThreadUtil.sleep(500);
        HttpResponse verifyBirthday2Rsp = ProxyUtil.execute(HttpUtil.createPost(host + "/unenrollment/verify/birthday")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",verifyBirthday1Rsp.header("sstt"))
                .cookie(account.getCookie())
                .body("{\"monthOfYear\":\""+(birthday.month()+1)+"\",\"dayOfMonth\":\""+birthday.dayOfMonth()+"\",\"year\":\""+birthday.year()+"\"}"));
        checkAndThrowUnavailableException(verifyBirthday2Rsp,"生日验证");
        String verifyQuestions1Location = verifyBirthday2Rsp.header("Location");
        if(verifyQuestions1Location.contains("/session/timeout")){
            throw new ServiceException("生日验证失败");
        }
        account.setNote("生日验证通过...");
        ThreadUtil.sleep(300);
        account.updateLoginInfo(verifyBirthday2Rsp);

        account.setNote("正在验证密保");
        ThreadUtil.sleep(500);

        //https://iforgot.apple.com/unenrollment/verify/questions?sstt=MGCVqOzR2RL%2FzXaJ84CjoQFn3%2F6BiLsOZpGBnlubgiFiIu9b4YDsZjl54oFak4iEiZvr%2FyQ19prv6iCoPd%2Bx8k%2BoLF35c1%2FzL%2BKycUfk5UwYf9dn7lFagDJLd%2F9BDdLaV%2BXzwMv7Fy1YZIzU3S7p1DWbONpL2o7aQUWO6XrZbjHUI9UpjeRZ3bfJpT8vbzTRWIQfUeOs%2B9i0fx5PxvQqtfRSpsDpWvmfNHO155tjtp8oGARrbAukjPn4kjmxrDjgtpQDvjxV9Qcz4LHnibmM%2BLiQrKps%2FyrS0466h02jTww9631yzAHV%2BjGxYt04ihb5lwjM4YpbwyIQ%2F7ieL%2B2KzUM3gfMukgZJl54Jfp3QJL9G6xMFJjUl5rVEURORdbe629Zy7Hb5%2BA99ffkcYc1vZF9GYiYd4IlDW%2BAA4314TZxjJM%2FMnHpT%2BotR%2Bs3Z%2BsxTHrwo3uwhZCXZRI%2F1LlaNMAmt6A2sg8f%2BbTgOt8CpgFTLLHoiyc
        HttpResponse verifyQuestions1Rsp = ProxyUtil.execute(HttpUtil.createGet(host + verifyQuestions1Location)
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json; charset=utf-8")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",verifyBirthday1Rsp.header("sstt"))
                .cookie(account.getCookie())
        );
        account.updateLoginInfo(verifyQuestions1Rsp);

        JSON verifyQuestions1BodyJSON = JSONUtil.parse(verifyQuestions1Rsp.body());
        List<JSONObject> questions = verifyQuestions1BodyJSON.getByPath("questions",List.class);
        Map<Integer,String> answerMap = new HashMap<>(){{
            put(1,account.getAnswer1());
            put(2,account.getAnswer2());
            put(3,account.getAnswer3());
        }};
        for (JSONObject question : questions) {
            question.remove("locale");
            question.putOnce("answer",answerMap.get(question.getInt("number")));
        }
        Map<String,List<JSONObject>> bodyMap = new HashMap<>();
        bodyMap.put("questions",questions);
        ThreadUtil.sleep(500);
        HttpResponse verifyQuestions2Rsp = ProxyUtil.execute(HttpUtil.createPost(host + "/unenrollment/verify/questions")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",verifyQuestions1Rsp.header("sstt"))
                .cookie(account.getCookie())
                .body(JSONUtil.toJsonStr(bodyMap)));
        checkAndThrowUnavailableException(verifyBirthday2Rsp,"密保验证");
        if (verifyQuestions2Rsp.getStatus() != 302){
            String validationErrors = getValidationErrors(verifyQuestions2Rsp, "密保验证失败, 请检查密保答案是否正确");
            throw new ServiceException(validationErrors);
        }

        account.setNote("密保验证通过...");
        ThreadUtil.sleep(300);
        account.updateLoginInfo(verifyQuestions2Rsp);

        account.setNote("正在关闭双重认证");
        ThreadUtil.sleep(500);
        String unenrollment1Location = verifyQuestions2Rsp.header("Location");
        //"https://iforgot.apple.com/unenrollment?sstt=AZIzj7VgizCyJnmtou0%2BDGo%2F%2Br50CLtqMcROmb9DAif3y8PWzEhqeRF4MPhYlZcV5CmcNuRZBOQOyye%2BfUL1ERSWwYGVupMMTyVwUY7Z5s3uSfdT5N3spoRU5HKGtOud8JVpKn%2FzoWlZJKZiRGZoF%2BpaFBEWkUEaPqG2vnHzxfefNJk5j1V3%2BXYmYbWLiJrNHMh0UJxexLtY1X4NEjVMpQWSVfnppw05xQDslz%2BISm3uDHi%2B8t5E6pjZR4diQD5cQsTmCuU30G5%2F%2F2jtlytyhQ5QQ1pT2vGYDj%2BJIueOXciikYfGbsSWY8%2B4bYiXRktPOXGp9ZvpNI51DnS30XA7wJzDvDSiWL%2BsUQz4oWlzkX6UIY%2FYOm3Ak2GNfwiAWxtCyUiMhaPKsE%2F6seZL%2F6st71rV%2BGTchaRyRBWsMRGMahD%2B7Re715F%2F28OS55sd3xi7fJqoMS6QW5KotSF5cUM%2FcNrsOJBQYnn5EOaQkYR3Q04wtUFXPoLzOEhHHNWGa6L7fDm3I1z5Ty%2FIBSU6jF%2FtnbpAph7TWAYd3Ca7ga08ffWFqAqtwEK8ZCZFp6l4QHOHjw%3D%3D
        HttpResponse unenrollment1Rsp = ProxyUtil.execute(HttpUtil.createGet(host + unenrollment1Location)
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json; charset=utf-8")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/unenrollment/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",verifyQuestions1Rsp.header("sstt"))
                .cookie(account.getCookie())
        );
        account.updateLoginInfo(unenrollment1Rsp);
        ThreadUtil.sleep(500);
        HttpResponse unenrollment2Rsp = ProxyUtil.execute(HttpUtil.createPost(host + "/unenrollment")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",unenrollment1Rsp.header("sstt"))
                .cookie(account.getCookie())
        );
        if (302!=unenrollment2Rsp.getStatus()){
            throw new ServiceException("双重关闭成功");
        }
        account.updateLoginInfo(unenrollment2Rsp);
        account.setNote("双重关闭成功,等待重置密码...");
        ThreadUtil.sleep(300);
        account.setNote("正在重置密码...");
        ThreadUtil.sleep(500);
        HttpResponse unenrollmentReset1Rsp = ProxyUtil.execute(HttpUtil.createPost(host + "/unenrollment/reset")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json, text/javascript, *; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",unenrollment2Rsp.header("sstt"))
                .cookie(account.getCookie())
                .body("{\"password\":\""+newPwd+"\"}")
        );

        if (unenrollmentReset1Rsp.getStatus() != 260){
            String msg = getValidationErrors(unenrollmentReset1Rsp, "重设密码失败");
            throw new ServiceException(msg);
        }

        account.setNote("双重关闭成功，密码重置完成");
        account.setPwd(newPwd);
        return unenrollmentReset1Rsp;
    }


    /**
     * 生成支持pin
     */
    public static HttpResponse supportPin(Account account){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("scnt", ListUtil.toList(account.getScnt()));

        headers.put("Origin",ListUtil.toList("https://appleid.apple.com"));
        headers.put("Referer",ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Apple-I-Request-Context",ListUtil.toList("ca"));
        headers.put("X-Apple-Api-Key",ListUtil.toList("cbf64fd6843ee630b463f358ea0b707b"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        String url = "https://appleid.apple.com/account/manage/supportpin";
        HttpResponse supportPinRsp = ProxyUtil.execute(HttpUtil.createPost(url)
                        .header(headers));
        return supportPinRsp;
    }

    /**
     * 收款方式列表
     */
    public static HttpResponse paymentList(Account account){
        String url = "https://appleid.apple.com/account/manage/payment";
        HashMap<String, List<String>> header = buildHeader(account);

        HttpResponse paymentRsp = ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(header));
        return paymentRsp;
    }

    /**
     * 获取图形验证码
     */
    public static HttpResponse captchaPost(Account account){
        String url = "https://iforgot.apple.com/captcha?captchaType=IMAGE";
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("X-Apple-I-FD-Client-Info", ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Host",ListUtil.toList("iforgot.apple.com"));
        headers.put("Referer",ListUtil.toList("https://iforgot.apple.com/password/verify/appleid?language=zh_CN"));
        if(!StrUtil.isEmpty(account.getSstt())){
            headers.put("sstt",ListUtil.toList(account.getSstt()));
        }
        String body="{\"type\":\"IMAGE\"}";
        return ProxyUtil.execute(HttpUtil.createGet(url)
                .cookie(account.getCookie())
//                .body(body)
                        .header(headers));
    }

    /**
     * 验证码并且验证通过(如果验证码不通过则重试三次)
     */
    public static HttpResponse captchaAndVerifyPost(Account account) {
        return captchaAndVerifyPost(account,10);
    }
    public static HttpResponse captchaAndVerifyPost(Account account,Integer retry){
        account.setNote("正在获取验证码...");
        HttpResponse captchaRsp = captchaPost(account);
        account.updateLoginInfo(captchaRsp);

        String body = captchaRsp.body();
        if (StrUtil.isEmpty(body)){
            return captchaAndVerifyPost(account,--retry);
        }

        JSON captchaRspJSON = JSONUtil.parse(body);

        String  captBase64 = captchaRspJSON.getByPath("payload.content", String.class);
        if (StrUtil.isEmpty(captBase64)){
            return captchaAndVerifyPost(account,--retry);
        }

        Integer captId     = captchaRspJSON.getByPath("id", Integer.class);
        String  captToken  = captchaRspJSON.getByPath("token", String.class);
        String  captAnswer = OcrUtil.recognize(captBase64);

        String verifyAppleIdBody = "{\"id\":\"%s\",\"captcha\":{\"id\":%d,\"answer\":\"%s\",\"token\":\"%s\"}}";
        verifyAppleIdBody = String.format(verifyAppleIdBody,account.getAccount(),captId,captAnswer,captToken);
        account.setNote("正在验证账户...");
        HttpResponse verifyAppleIdRsp = AppleIDUtil.verifyAppleIdPost(account,verifyAppleIdBody);
        // 验证码错误才重新尝试
        if (verifyAppleIdRsp.getStatus() != 302 && retry > 0){
            if(verifyAppleIdRsp.getStatus() == 200){
                String message= hasFailMessage(verifyAppleIdRsp);
                throw new ServiceException(message);
            }else if(verifyAppleIdRsp.getStatus() == 400){
                String service_errors = JSONUtil.parse(verifyAppleIdRsp.body()).getByPath("service_errors",String.class);
                JSONArray jsonArray = JSONUtil.parseArray(service_errors);
                String code = JSONUtil.parseObj(jsonArray.get(0)).getStr("code");
                if("captchaAnswer.Invalid".equals(code)){
                    //延迟半秒
                    ThreadUtil.sleep(500);
                    return captchaAndVerifyPost(account,--retry);
                }else if("-20210".equals(code)){
                    throw new ServiceException("这个 Apple ID 没有被激活。");
                }
            }else {
                ThreadUtil.sleep(500);
                return captchaAndVerifyPost(account,--retry);
            }
            return verifyAppleIdRsp;
        }else{
            account.updateLoginInfo(verifyAppleIdRsp);
            return verifyAppleIdRsp;
        }
    }

    /**
     * 检查appleid是通过怎样的方式去校验(密保/邮件/短信)
     */
    public static HttpResponse verifyAppleIdPost(Account account,String body) {
        String url = "https://iforgot.apple.com/password/verify/appleid";

        HttpResponse verifyAppleIdRsp = ProxyUtil.execute(HttpUtil.createPost(url)

                        .header("Accept-Encoding","gzip, deflate, br")
                        .header("Accept-Language","zh-CN,zh;q=0.9")
                        .header("Accept","application/json, text/javascript, */*; q=0.01")
                        .header("Content-Type","application/json")
                        .header("User-Agent",Constant.BROWSER_USER_AGENT)
                        .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                        .header("Host","iforgot.apple.com")
                        .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                        .header("sstt",account.getSstt())
                        .body(body)
                        .cookie(account.getCookie()));
        return verifyAppleIdRsp;
    }

    /**
     * 通过密保修改密码(如果账号被锁则解锁)
     * @return (unlock && rsp.getStatus() == 206) || (!unlock && rsp.getStatus() == 260) -> success
     */
    public static HttpResponse updatePwdByProtection(HttpResponse verifyAppleIdRsp,Account account,String newPwd){
        String location = verifyAppleIdRsp.header("Location");
        HttpResponse rsp = null;
        boolean unlock = location.startsWith("/password/authenticationmethod");
        // 解锁并且改密 (六月抓包版本)
        if (unlock){
            rsp = AppleIDUtil.unlockAndUpdatePwdByProtection2(verifyAppleIdRsp,account,newPwd);
        }else{//忘记密码
            rsp = AppleIDUtil.verifyAppleIdByPwdProtection2(verifyAppleIdRsp,account,newPwd);
        }
        return rsp;
    }

    public static HttpResponse securityUpgradeLogin(Account account){
        account.setNote("正在处理...");
        String clientId=account.getClientId();
        String nHex = "AC6BDB41324A9A9BF166DE5E1389582FAF72B6651987EE07FC3192943DB56050A37329CBB4A099ED8193E0757767A13DD52312AB4B03310DCD7F48A9DA04FD50E8083969EDB767B0CF6095179A163AB3661A05FBD5FAAAE82918A9962F0B93B855F97993EC975EEAA80D740ADBF4FF747359D041D5C33EA71D281E446B14773BCA97B43A23FB801676BD207A436C6481F1D2B9078717461A5B9D32E688F87748544523B524B0D57D5EA77A2775D2ECFA032CFBDBF52FB3786160279004E57AE6AF874E7303CE53299CCC041C7BC308D82A5698F3A8D0C38271AE35F8E9DBFBB694B5C803D89F7AE435DE236D525F54759B65E372FCD68EF20FA7111F9E4AFF73";
        BigInteger n = new BigInteger(nHex,16);
        byte[] rb = RandomUtil.randomBytes(32);
        BigInteger ra = new BigInteger(1,rb);
        String a = GiftCardUtil.calA(ra,n);
        //step1  signin
        String frameId=account.getFrameId();
        account.setNote("正在登录...");
        String redirect_uri="https://appleid.apple.com";
        String url = "https://idmsa.apple.com/appleauth/auth/authorize/signin?frame_id="+frameId+"&skVersion=7&iframeId="+frameId
                +"&client_id="+clientId+"&redirect_uri="+redirect_uri+"&response_type=code&response_mode=web_message" +
                "&state="+frameId+"&authVersion=latest";
        HttpResponse signinRes = ProxyUtil.execute(HttpUtil.createGet(url)
                .header("Upgrade-Insecure-Requests","1")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                .header("Content-Type","application/x-www-form-urlencoded")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://appleid.apple.com/")
                .header("Host","idmsa.apple.com")
        );
        account.updateLoginInfo(signinRes);
        //step2  init
        account.setNote("正在验证账户...");
        String body = "{\"a\":\""+a+"\",\"accountName\":\""+ account.getAccount() +"\",\"protocols\":[\"s2k\",\"s2k_fo\"]}";
        url="https://idmsa.apple.com/appleauth/auth/signin/init";
        HttpResponse intRes = ProxyUtil.execute(HttpUtil.createPost(url)
                .header("scnt",account.getScnt())
                .header("X-Apple-Auth-Attributes",signinRes.header("X-Apple-Auth-Attributes"))
                .header("X-Apple-Widget-Key",clientId)
                .header("X-Requested-With","XMLHttpRequest")
                .header("X-Apple-OAuth-Redirect-URI"," https://appleid.apple.com")
                .header("X-Apple-OAuth-Client-Id",clientId)
                .header("X-Apple-OAuth-Client-Type","firstPartyAuth")
                .header("X-Apple-OAuth-Response-Type","code")
                .header("X-Apple-OAuth-Response-Mode","web_message")
                .header("X-Apple-OAuth-State",frameId)
                .header("X-Apple-Domain-Id","1")
                .header("X-Apple-Frame-Id",frameId)
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://appleid.apple.com/")
                .header("Host","idmsa.apple.com")
                .cookie(account.getCookie())
                .body(body)
        );
        account.updateLoginInfo(intRes);
        account.setNote("正在验证登录密码...");
        //step3 complete
        JSON intBodyJson=JSONUtil.parse(intRes.body());
        url="https://idmsa.apple.com/appleauth/auth/signin/complete?isRememberMeEnabled=true";
        int iter =intBodyJson.getByPath("iteration",Integer.class);
        String salt = intBodyJson.getByPath("salt",String.class);
        String b = intBodyJson.getByPath("b",String.class);
        String c = intBodyJson.getByPath("c",String.class);
        BigInteger g = new BigInteger("2");
        Map map = GiftCardUtil.calM(account.getAccount(), account.getPwd(), a, iter, salt, b, g, n, ra);
        Map<String,Object> bodyParas=new HashMap<>(){{
            put("accountName",account.getAccount());
            put("rememberMe",false);
            put("m1",map.get("m1"));
            put("c",c);
            put("m2",map.get("m2"));
        }};
        HttpResponse completeRes = ProxyUtil.execute(HttpUtil.createPost(url)
                .header("scnt",account.getScnt())
                .header("X-Apple-Auth-Attributes",signinRes.header("X-Apple-Auth-Attributes"))
                .header("X-Apple-Widget-Key",clientId)
                .header("X-Requested-With","XMLHttpRequest")
                .header("X-Apple-OAuth-Redirect-URI"," https://appleid.apple.com")
                .header("X-Apple-OAuth-Client-Id",clientId)
                .header("X-Apple-OAuth-Client-Type","firstPartyAuth")
                .header("X-Apple-OAuth-Response-Type","code")
                .header("X-Apple-OAuth-Response-Mode","web_message")
                .header("X-Apple-OAuth-State",frameId)
                .header("X-Apple-Domain-Id","1")
                .header("X-Apple-Frame-Id",frameId)
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://appleid.apple.com/")
                .header("Host","idmsa.apple.com")
                .cookie(account.getCookie())
                .body(JSONUtil.toJsonStr(bodyParas))
        );
        if(409!=completeRes.getStatus()){
            String message= hasFailMessage(completeRes);
            throw new ServiceException(StrUtil.isEmpty(message)?"登录失败":"登录失败："+message);
        }else{
            String authType=JSONUtil.parse(completeRes.body()).getByPath("authType",String.class);
            if("hsa2".equals(authType)){
                throw new ServiceException("操作失败，此该账户已开通双重认证");
            }
        }
        account.updateLoginInfo(intRes);
        //step4 auth
        url="https://idmsa.apple.com/appleauth/auth";
        HttpResponse authRes = ProxyUtil.execute(HttpUtil.createGet(url)
                .header("scnt",account.getScnt())
                .header("X-Apple-Auth-Attributes",completeRes.header("X-Apple-Auth-Attributes"))
                .header("X-Apple-Widget-Key",clientId)
                .header("X-Requested-With","XMLHttpRequest")
                .header("X-Apple-ID-Session-Id",completeRes.header("X-Apple-ID-Session-Id"))
                .header("X-Apple-OAuth-Redirect-URI"," https://appleid.apple.com")
                .header("X-Apple-OAuth-Client-Id",clientId)
                .header("X-Apple-OAuth-Client-Type","firstPartyAuth")
                .header("X-Apple-OAuth-Response-Type","code")
                .header("X-Apple-OAuth-Response-Mode","web_message")
                .header("X-Apple-OAuth-State",frameId)
                .header("X-Apple-Domain-Id","1")
                .header("X-Apple-Frame-Id",frameId)
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","text/html")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://idmsa.apple.com/appleauth/auth/signin?widgetKey=af1139274f266b22b68c2a3e7ad932cb3c0bbe854e13a79af78dcc73136882c3&language=zh_CN")
                .header("Host","idmsa.apple.com")
                .cookie(account.getCookie())
        );
        if(200!=authRes.getStatus()){
            String message= hasFailMessage(authRes);
            throw new ServiceException(StrUtil.isEmpty(message)?"登录失败":"登录失败："+message);
        }
        account.updateLoginInfo(authRes);
        account.setNote("账户密码验证通过...");
        ThreadUtil.sleep(300);

        //step4 questions
        account.setNote("正在验证密保问题...");
        String boot_args=StrUtils.getScriptByClass(authRes.body(),"boot_args");
        String questions = JSONUtil.parse(boot_args).getByPath("direct.twoSV.securityQuestions.questions",String.class);
        List<Question> qs = JSONUtil.toList(questions, Question.class);
        for (int i = 0; i < qs.size(); i++) {
            Question q = qs.get(i);
            if (q.getNumber() == 1) {
                q.setAnswer(account.getAnswer1());
            } else if (q.getNumber() == 2) {
                q.setAnswer(account.getAnswer2());
            } else if (q.getNumber() == 3) {
                q.setAnswer(account.getAnswer3());
            }
        }
        url = "https://idmsa.apple.com/appleauth/auth/verify/questions";
        body = "{\"questions\":" + JSONUtil.parse(qs) + "}";
        HttpResponse questionsResp = ProxyUtil.execute(HttpUtil.createPost(url)
                .header("X-Apple-App-Id",clientId)
                .header("scnt",account.getScnt())
                .header("X-Apple-Auth-Attributes",authRes.header("X-Apple-Auth-Attributes"))
                .header("X-Apple-Widget-Key",clientId)
                .header("X-Requested-With","XMLHttpRequest")
                .header("X-Apple-ID-Session-Id",authRes.header("X-Apple-ID-Session-Id"))
                .header("X-Apple-OAuth-Redirect-URI"," https://appleid.apple.com")
                .header("X-Apple-OAuth-Client-Id",clientId)
                .header("X-Apple-OAuth-Client-Type","firstPartyAuth")
                .header("X-Apple-OAuth-Response-Type","code")
                .header("X-Apple-OAuth-Response-Mode","web_message")
                .header("X-Apple-OAuth-State",frameId)
                .header("X-Apple-Domain-Id","1")
                .header("X-Apple-Frame-Id",frameId)
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://idmsa.apple.com/appleauth/auth/signin?widgetKey="+clientId+"&language=zh_CN")
                .header("Host","idmsa.apple.com")
                .body(body)
                .cookie(account.getCookie()));
        if(412!=questionsResp.getStatus()){
            String message= hasFailMessage(questionsResp);
            throw new ServiceException(StrUtil.isEmpty(message)?"密保问题验证失败":"密保问题验证失败："+message);
        }
        account.updateLoginInfo(questionsResp);

        account.getAuthData().put("questionsResp",questionsResp);
        account.setNote("密保问题通过...");
        ThreadUtil.sleep(300);

        //step5 repair
        account.setNote("获取阅读协议...");
        ThreadUtil.sleep(300);
        url = "https://appleid.apple.com/widget/account/repair?widgetKey="+clientId+"&rv=1&language=zh_CN_CHN";
        HttpResponse repairResp = ProxyUtil.execute(HttpUtil.createGet(url)
                .header("Upgrade-Insecure-Requests","1")
                .header("X-Apple-Widget-Key",clientId)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Content-Type","text/html;charset=UTF-8")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://idmsa.apple.com/")
                .header("Host","appleid.apple.com")
                .cookie(account.getCookie()));

        if (302 == repairResp.getStatus()){
            account.updateLoginInfo(repairResp);
            repairResp = ProxyUtil.execute(HttpUtil.createGet(repairResp.header("Location"))
                    .header("Upgrade-Insecure-Requests","1")
                    .header("X-Apple-Widget-Key",clientId)
                    .header("Accept-Encoding","gzip, deflate, br")
                    .header("Accept-Language","zh-CN,zh;q=0.9")
                    .header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Content-Type","text/html;charset=UTF-8")
                    .header("User-Agent",Constant.BROWSER_USER_AGENT)
                    .header("Referer","https://idmsa.apple.com/")
                    .header("Host","appleid.apple.com")
                    .cookie(account.getCookie()));
        }

        if(200!=repairResp.getStatus()){
            String message= hasFailMessage(repairResp);
            throw new ServiceException(StrUtil.isEmpty(message)?"获取阅读协议失败":"获取阅读协议失败："+message);
        }
        account.updateLoginInfo(repairResp);
        boot_args=StrUtils.getScriptById(repairResp.body(),"boot_args");
        String sessionId=JSONUtil.parse(boot_args).getByPath("direct.sessionId",String.class);
        //step6 options
        url = "https://appleid.apple.com/account/manage/repair/options";
        HttpResponse optionsResp = ProxyUtil.execute(HttpUtil.createGet(url)
                .header("scnt",account.getScnt())
                .header("X-Requested-With","XMLHttpRequest")
                .header("X-Apple-Skip-Repair-Attributes","[]")
                .header("X-Apple-OAuth-Context",questionsResp.header("X-Apple-OAuth-Context"))
                .header("X-Apple-Session-Token",questionsResp.header("X-Apple-Repair-Session-Token"))
                .header("X-Apple-ID-Session-Id",sessionId)
                .header("X-Apple-Widget-Key",clientId)
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json; charset=utf-8")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://appleid.apple.com/widget/account/repair?widgetKey="+clientId+"&language=zh_CN_CHN")
                .header("Host","appleid.apple.com")
                .cookie(account.getCookie()));
        account.setXAppleIDSessionId(sessionId);
        if(200!=optionsResp.getStatus()){
            String message= hasFailMessage(repairResp);
            throw new ServiceException(StrUtil.isEmpty(message)?"获取阅读协议失败":"获取阅读协议失败："+message);
        }
        account.updateLoginInfo(optionsResp);
        account.getAuthData().put("optionsResp",optionsResp);
        //step7 upgrade
        url = "https://appleid.apple.com//account/security/upgrade";
        HttpResponse upgradeResp = ProxyUtil.execute(HttpUtil.createGet(url)
                .header("scnt",account.getScnt())
                .header("X-Requested-With","XMLHttpRequest")
                .header("X-Apple-Skip-Repair-Attributes","[]")
                .header("X-Apple-OAuth-Context",questionsResp.header("X-Apple-OAuth-Context"))
                .header("X-Apple-Session-Token",questionsResp.header("X-Apple-Repair-Session-Token"))
                .header("X-Apple-ID-Session-Id",questionsResp.header("X-Apple-ID-Session-Id"))
                .header("X-Apple-Widget-Key",clientId)
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json; charset=utf-8")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://appleid.apple.com/")
                .header("Host","appleid.apple.com")
                .cookie(account.getCookie()));
        if(200!=upgradeResp.getStatus()){
            String message= hasFailMessage(repairResp);
            throw new ServiceException(StrUtil.isEmpty(message)?"获取阅读协议失败":"获取阅读协议失败："+message);
        }
        account.updateLoginInfo(upgradeResp);
        account.setNote("协议获取成功...");
        return upgradeResp;
    }

    /**
     * 忘记密码 1官网版本 2六月版本
     */
    public static HttpResponse verifyAppleIdByPwdProtection2(HttpResponse verifyAppleIdRsp,Account account,String newPwd) {
        String host = "https://iforgot.apple.com";

        account.setNote("正在获取重设方式...");
        String options1Location = verifyAppleIdRsp.header("Location");
        HttpResponse options1Rsp = ProxyUtil.execute(
                HttpUtil.createGet(host + options1Location)
                        .header("Accept-Encoding","gzip, deflate, br")
                        .header("Accept-Language","zh-CN,zh;q=0.9")
                        .header("X-Requested-With","XMLHttpRequest")
                        .header("Accept","application/json; charset=utf-8")
                        .header("Content-Type","application/json")
                        .header("User-Agent",Constant.BROWSER_USER_AGENT)
                        .header("Referer","https://iforgot.apple.com/")
                        .header("Host","iforgot.apple.com")
                        .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                        .header("sstt",verifyAppleIdRsp.header("sstt"))
                        .cookie(account.getCookie())

        );
        checkAndThrowUnavailableException(options1Rsp);
        account.updateLoginInfo(options1Rsp);
        ThreadUtil.sleep(500);
        HttpResponse options3Rsp = ProxyUtil.execute(HttpUtil.createPost(host + "/recovery/options")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",verifyAppleIdRsp.header("sstt"))
                .cookie(account.getCookie())
                .body("{\"recoveryOption\":\"reset_password\"}"));
        checkAndThrowUnavailableException(options3Rsp);
        account.updateLoginInfo(options3Rsp);
        ThreadUtil.sleep(500);
        String authMethod1Location = options3Rsp.header("Location");
        HttpResponse authMethod1Rsp = ProxyUtil.execute(HttpUtil.createGet(host + authMethod1Location)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json; charset=utf-8")
                .header("Content-Type","application/json")
                .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240")
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info","{\"U\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240\",\"L\":\"zh-CN\",\"Z\":\"GMT+08:00\",\"V\":\"1.1\",\"F\":\"Nla44j1e3NlY5BNlY5BSmHACVZXnNA9Mhp7HeumaururJhBR.uMp4UdHz13NlxXxfs.xLB.Tf1cK0DW_D7TL4y4Iy5JNlY5BNp55BNlan0Os5Apw.0WX\"}")
                .header("sstt",verifyAppleIdRsp.header("sstt"))
                .cookie(account.getCookie()));
        checkAndThrowUnavailableException(authMethod1Rsp);
        account.updateLoginInfo(authMethod1Rsp);
        String authMethodBody = authMethod1Rsp.body();
        if (StrUtil.isEmpty(authMethodBody) || !JSONUtil.parseObj(authMethodBody).containsKey("options")){
            throw new ServiceException("没有重设密码的方式");
        }
        List<String> authMethodOptions = JSONUtil.parse(authMethodBody).getByPath("options", List.class);
        if(!authMethodOptions.contains("questions")){
            throw new ServiceException("不支持密保问题方式解锁改密");
        }
        ThreadUtil.sleep(500);
        HttpResponse authMethod2Rsp = ProxyUtil.execute(HttpUtil.createPost(host + "/password/authenticationmethod")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",authMethod1Rsp.header("sstt"))
                .cookie(account.getCookie())
                .body("{\"type\":\"questions\"}"));
        checkAndThrowUnavailableException(authMethod2Rsp);
        account.updateLoginInfo(authMethod2Rsp);
        account.setNote("正在验证生日");
        String verifyBirthday1Location = authMethod2Rsp.header("Location");
        ThreadUtil.sleep(500);
        HttpResponse verifyBirthday1Rsp = ProxyUtil.execute(HttpUtil.createGet(host + verifyBirthday1Location)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json; charset=utf-8")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",authMethod1Rsp.header("sstt"))
                .cookie(account.getCookie()));
        checkAndThrowUnavailableException(verifyBirthday1Rsp);
        account.updateLoginInfo(verifyBirthday1Rsp);
        DateTime birthday=null;
        try {
            birthday = DateUtil.parse(account.getBirthday());
        }catch (Exception e){
            throw new ServiceException("出生日期输入错误！");
        }
        ThreadUtil.sleep(500);
        HttpResponse verifyBirthday2Rsp = ProxyUtil.execute(HttpUtil.createPost(host + "/password/verify/birthday")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",verifyBirthday1Rsp.header("sstt"))
                .body("{\"monthOfYear\":\""+(birthday.month()+1)+"\",\"dayOfMonth\":\""+birthday.dayOfMonth()+"\",\"year\":\""+birthday.year()+"\"}")
                .cookie(account.getCookie()));
        checkAndThrowUnavailableException(verifyBirthday2Rsp,"生日信息验证");
        account.updateLoginInfo(verifyBirthday2Rsp);

        account.setNote("正在验证密保...");
        if (StrUtil.isEmpty(account.getAnswer1()) || StrUtil.isEmpty(account.getAnswer2()) || StrUtil.isEmpty(account.getAnswer3())){
            throw new ServiceException("密保不能为空");
        }
        ThreadUtil.sleep(500);
        String verifyQuestions1Location = verifyBirthday2Rsp.header("Location");
        HttpResponse verifyQuestions1Rsp = ProxyUtil.execute(HttpUtil.createGet(host + verifyQuestions1Location)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json; charset=utf-8")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",verifyBirthday1Rsp.header("sstt"))
                .cookie(account.getCookie()));
        checkAndThrowUnavailableException(verifyQuestions1Rsp);
        account.updateLoginInfo(verifyQuestions1Rsp);

        JSON verifyQuestions1BodyJSON = JSONUtil.parse(verifyQuestions1Rsp.body());
        List<JSONObject> questions = verifyQuestions1BodyJSON.getByPath("questions",List.class);

        Map<Integer,String> answerMap = new HashMap<>(){{
            put(1,account.getAnswer1());
            put(2,account.getAnswer2());
            put(3,account.getAnswer3());
        }};
        for (JSONObject question : questions) {
            question.remove("locale");
            //重新排序
            int number=DataUtil.getQuestionIndex(question.getInt("id"));
            question.putOnce("answer",answerMap.get(number));
        }
        Map<String,List<JSONObject>> bodyMap = new HashMap<>();
        bodyMap.put("questions",questions);
        ThreadUtil.sleep(500);
        HttpResponse verifyQuestions2Rsp = ProxyUtil.execute(HttpUtil.createPost(host + "/password/verify/questions")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",verifyQuestions1Rsp.header("sstt"))
                .body(JSONUtil.toJsonStr(bodyMap))
                .cookie(account.getCookie()));
        checkAndThrowUnavailableException(verifyQuestions2Rsp,"密保信息验证");
        account.updateLoginInfo(verifyQuestions2Rsp);

        account.setNote("正在设置新密码");
        String resrtPasswordOptionLocation = verifyQuestions2Rsp.header("Location");
        ThreadUtil.sleep(500);
        HttpResponse resrtPasswordOptionRsp = ProxyUtil.execute(HttpUtil.createGet(host + resrtPasswordOptionLocation)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json; charset=utf-8")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",verifyQuestions1Rsp.header("sstt"))
                .cookie(account.getCookie()));
        checkAndThrowUnavailableException(resrtPasswordOptionRsp);
        account.updateLoginInfo(resrtPasswordOptionRsp);
        ThreadUtil.sleep(500);
        String passwordReset1Location = resrtPasswordOptionRsp.header("Location");
        HttpResponse passwordReset1Rsp = ProxyUtil.execute(HttpUtil.createGet(host + passwordReset1Location)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json; charset=utf-8")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",verifyQuestions1Rsp.header("sstt"))
                .cookie(account.getCookie()));
        checkAndThrowUnavailableException(passwordReset1Rsp);
        account.updateLoginInfo(passwordReset1Rsp);
        ThreadUtil.sleep(500);
        HttpResponse passwordReset2Rsp = ProxyUtil.execute(HttpUtil.createPost(host + "/password/reset")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent",Constant.BROWSER_USER_AGENT)
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("Content-Type","application/json")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",resrtPasswordOptionRsp.header("sstt"))
                .body("{\"password\":\""+newPwd+"\"}")
                .cookie(account.getCookie()));
        checkAndThrowUnavailableException(passwordReset2Rsp);
        account.updateLoginInfo(passwordReset2Rsp);

        return passwordReset2Rsp;
    }

    /**
     * 解锁改密 1官网版本 2六月版本
     */
    public static HttpResponse unlockAndUpdatePwdByProtection2(HttpResponse verifyAppleIdRsp,Account account,String newPwd) {
        String host = "https://iforgot.apple.com";

        account.setNote("正在获取重设方式...");
        String authMethod1Location = verifyAppleIdRsp.header("Location");
        HttpResponse authMethod1Rsp = ProxyUtil.execute(HttpUtil.createGet(host + authMethod1Location)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json; charset=utf-8")
                .header("Content-Type","application/json")
                .header("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36")
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",verifyAppleIdRsp.header("sstt"))
                .cookie(account.getCookie()));
        checkAndThrowUnavailableException(authMethod1Rsp);

        account.updateLoginInfo(authMethod1Rsp);
        String authMethodBody = authMethod1Rsp.body();
        if (StrUtil.isEmpty(authMethodBody) || !JSONUtil.parseObj(authMethodBody).containsKey("options")){
            throw new ServiceException("没有重设密码的方式");
        }
        List<String> authMethodOptions = JSONUtil.parse(authMethodBody).getByPath("options", List.class);
        if(!authMethodOptions.contains("questions")){
            throw new ServiceException("不支持密保问题方式解锁改密");
        }

        HttpResponse authMethod2Rsp = ProxyUtil.execute(HttpUtil.createPost(host + "/password/authenticationmethod")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36")
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",authMethod1Rsp.header("sstt"))
                .cookie(account.getCookie())
                .body("{\"type\":\"questions\"}"));

        checkAndThrowUnavailableException(authMethod2Rsp);
        account.updateLoginInfo(authMethod2Rsp);
        ThreadUtil.sleep(500);
        account.setNote("正在验证生日...");
        String verifyBirthday1Location = authMethod2Rsp.header("Location");
        HttpResponse verifyBirthday1Rsp = ProxyUtil.execute(HttpUtil.createGet(host + verifyBirthday1Location)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json; charset=utf-8")
                .header("Content-Type","application/json")
                .header("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36")
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",authMethod1Rsp.header("sstt"))
                .cookie(account.getCookie()));

        checkAndThrowUnavailableException(verifyBirthday1Rsp);
        account.updateLoginInfo(verifyBirthday1Rsp);

        DateTime birthday=null;
        try {
            birthday = DateUtil.parse(account.getBirthday());
        }catch (Exception e){
            throw new ServiceException("出生日期输入错误！");
        }
        HttpResponse verifyBirthday2Rsp = ProxyUtil.execute(HttpUtil.createPost(host + "/password/verify/birthday")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36")
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",verifyBirthday1Rsp.header("sstt"))
                .cookie(account.getCookie())
                .body("{\"monthOfYear\":\""+(birthday.month()+1)+"\",\"dayOfMonth\":\""+birthday.dayOfMonth()+"\",\"year\":\""+birthday.year()+"\"}"));
        checkAndThrowUnavailableException(verifyBirthday2Rsp,"生日信息验证");
        account.updateLoginInfo(verifyBirthday2Rsp);
        ThreadUtil.sleep(500);
        account.setNote("正在验证密保...");
        if (StrUtil.isEmpty(account.getAnswer1()) || StrUtil.isEmpty(account.getAnswer2()) || StrUtil.isEmpty(account.getAnswer3())){
            throw new ServiceException("密保不能为空");
        }

        String verifyQuestions1Location = verifyBirthday2Rsp.header("Location");
        HttpResponse verifyQuestions1Rsp = ProxyUtil.execute(HttpUtil.createGet(host + verifyQuestions1Location)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json; charset=utf-8")
                .header("Content-Type","application/json")
                .header("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36")
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",verifyBirthday1Rsp.header("sstt"))
                .cookie(account.getCookie()));

        checkAndThrowUnavailableException(verifyQuestions1Rsp);
        account.updateLoginInfo(verifyQuestions1Rsp);
        JSON verifyQuestions1BodyJSON = JSONUtil.parse(verifyQuestions1Rsp.body());
        List<JSONObject> questions = verifyQuestions1BodyJSON.getByPath("questions",List.class);
        Map<Integer,String> answerMap = new HashMap<>(){{
            put(1,account.getAnswer1());
            put(2,account.getAnswer2());
            put(3,account.getAnswer3());
        }};
        for (JSONObject question : questions) {
            question.remove("locale");
            //重新排序
            int number=DataUtil.getQuestionIndex(question.getInt("id"));
            question.putOnce("answer",answerMap.get(number));
        }

        Map<String,List<JSONObject>> bodyMap = new HashMap<>();
        bodyMap.put("questions",questions);
        HttpResponse verifyQuestions2Rsp = ProxyUtil.execute(HttpUtil.createPost(host + "/password/verify/questions")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36")
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",verifyQuestions1Rsp.header("sstt"))
                .cookie(account.getCookie())
                .body(JSONUtil.toJsonStr(bodyMap)));
        checkAndThrowUnavailableException(verifyQuestions2Rsp,"密保信息验证");
        account.updateLoginInfo(verifyQuestions2Rsp);
        String options1Location = verifyQuestions2Rsp.header("Location");
        HttpResponse options1Rsp = ProxyUtil.execute(HttpUtil.createGet(host + options1Location)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json; charset=utf-8")
                .header("Content-Type","application/json")
                .header("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36")
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",verifyQuestions1Rsp.header("sstt"))
                .cookie(account.getCookie()));
        checkAndThrowUnavailableException(options1Rsp);
        account.updateLoginInfo(options1Rsp);

        HttpResponse options2Rsp = ProxyUtil.execute(HttpUtil.createPost(host + "/password/reset/options")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36")
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",options1Rsp.header("sstt"))
                .body("{\"type\":\"unlock_account\"}")
                .cookie(account.getCookie()));
        checkAndThrowUnavailableException(options2Rsp);
        account.updateLoginInfo(options2Rsp);
        ThreadUtil.sleep(500);
        String unlock1Location = options2Rsp.header("Location");
        HttpResponse unlock1Rsp = ProxyUtil.execute(HttpUtil.createGet(host + unlock1Location)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json; charset=utf-8")
                .header("Content-Type","application/json")
                .header("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36")
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",options1Rsp.header("sstt"))
                .cookie(account.getCookie()));
        checkAndThrowUnavailableException(unlock1Rsp);
        account.updateLoginInfo(unlock1Rsp);
        account.setNote("正在解锁...");
        HttpResponse unlockForgot1Rsp = ProxyUtil.execute(HttpUtil.createPost(host + "/password/unlock/forgot")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36")
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",unlock1Rsp.header("sstt"))
                .body("{}")
                .cookie(account.getCookie()));
        checkAndThrowUnavailableException(unlockForgot1Rsp);
        account.updateLoginInfo(unlockForgot1Rsp);
        ThreadUtil.sleep(500);
        account.setNote("正在设置新密码...");
        String passwordReset1Location = unlockForgot1Rsp.header("Location");
        HttpResponse passwordReset1Rsp = ProxyUtil.execute(HttpUtil.createGet(host + passwordReset1Location)
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json; charset=utf-8")
                .header("Content-Type","application/json")
                .header("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36")
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",unlockForgot1Rsp.header("sstt"))
                .cookie(account.getCookie()));
        checkAndThrowUnavailableException(passwordReset1Rsp);
        account.updateLoginInfo(passwordReset1Rsp);

        HttpResponse passwordReset2Rsp = ProxyUtil.execute(HttpUtil.createPost(host + "/password/reset")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("X-Requested-With","XMLHttpRequest")
                .header("Accept","application/json, text/javascript, */*; q=0.01")
                .header("Content-Type","application/json")
                .header("User-Agent","Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36")
                .header("Referer","https://iforgot.apple.com/password/verify/appleid?language=zh_CN")
                .header("Host","iforgot.apple.com")
                .header("X-Apple-I-FD-Client-Info",Constant.BROWSER_CLIENT_INFO)
                .header("sstt",passwordReset1Rsp.header("sstt"))
                .cookie(account.getCookie())
                .body("{\"password\":\""+newPwd+"\"}"));

        checkAndThrowUnavailableException(passwordReset2Rsp);
        account.updateLoginInfo(passwordReset2Rsp);
        return passwordReset2Rsp;
    }

    private static HashMap<String, List<String>> buildHeader(Account account) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.9"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("X-Apple-Domain-Id", ListUtil.toList(account.getDomainId()));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(account.getFrameId()));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getClientId()));

        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));
        return headers;
    }

    public static void checkAndThrowUnavailableException(HttpResponse response,String title){
        if (response != null){
            if (response.getStatus() == 400){
                StringBuffer message=new StringBuffer();
                if(!StrUtil.isEmpty(title)){
                    message.append(title+"失败");
                    message.append(":");
                }
                message.append(getValidationErrors(response,"Bad Request"));
                throw new ServiceException(message.toString());
            }else if(response.getStatus() == 410){
                throw new ServiceException("网络异常，"+title+",410");
            }
        }
    }
    public static void checkAndThrowUnavailableException(HttpResponse response){
        checkAndThrowUnavailableException(response,"");
    }

    public static String getValidationErrors(HttpResponse response,String defaultMessage){
        return getValidationErrors("",response,defaultMessage);
    }

    public static String getValidationErrors(String action,HttpResponse response,String defaultMessage){
        if (response == null){
            return defaultMessage;
        }
        String body = response.body();
        if(StringUtils.isEmpty(body)){
            return defaultMessage;
        }
        List errorMessageList = new ArrayList();
        try{
            JSONObject jsonObject= JSONUtil.parseObj(body);
            List errorMessageList1 = jsonObject.getByPath("validationErrors.message", List.class);
            List errorMessageList2 = jsonObject.getByPath("serviceErrors.message", List.class);
            List errorMessageList3 = jsonObject.getByPath("service_errors.message", List.class);
            if (!CollUtil.isEmpty(errorMessageList1)){
                errorMessageList.addAll(errorMessageList1);
            }
            if (!CollUtil.isEmpty(errorMessageList2)){
                errorMessageList.addAll(errorMessageList2);
            }
            if (!CollUtil.isEmpty(errorMessageList3)){
                errorMessageList.addAll(errorMessageList3);
            }
            if (CollUtil.isEmpty(errorMessageList)){
                return defaultMessage;
            }
        }catch (Exception e){
            return defaultMessage;
        }

        if (StrUtil.isEmpty(action)){
            return String.join("、",errorMessageList);
        }

        return action + ":" + String.join("、",errorMessageList);
    }

    public static String hasFailMessage(HttpResponse response){
        return getValidationErrors(response,"");
    }
}

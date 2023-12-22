package com.sgswit.fx.utils;

import cn.hutool.cache.CacheUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
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
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.LoginInfo;
import com.sgswit.fx.model.Question;

import java.util.Arrays;
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
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("X-Apple-Domain-Id", ListUtil.toList(account.getDomainId()));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(account.getFrameId()));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getClientId()));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        String body = "{\"accountName\":\"%s\",\"password\":\"%s\",\"rememberMe\":false,\"trustTokens\":[]}";
        String scBogy = String.format(body, account.getAccount(), account.getPwd());
        String url = "https://idmsa.apple.com/appleauth/auth/signin?isRememberMeEnabled=false&isRememberMeEnabled=false";
        HttpResponse rsp = HttpUtil.createPost(url)
                .header(headers)
                .body(scBogy)
                .execute();
        account.updateLoginInfo(rsp);
        requestLog(url,headers,rsp);
        return rsp;
    }

    public static HttpResponse auth(Account account,HttpResponse signInRsp) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/html, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("X-Apple-Domain-Id", ListUtil.toList(account.getDomainId()));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(account.getFrameId()));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getClientId()));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(signInRsp.header("X-Apple-ID-Session-Id")));
        headers.put("scnt",ListUtil.toList(signInRsp.header("scnt")));

        String url = "https://idmsa.apple.com/appleauth/auth";
        HttpResponse rsp = HttpUtil.createPost(url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

        account.updateLoginInfo(rsp);
        requestLog(url,headers,rsp);
        return rsp;
    }

    public static HttpResponse securityCode(Account account,HttpResponse authRsp) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("X-Apple-Domain-Id", ListUtil.toList(account.getDomainId()));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(account.getFrameId()));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(account.getClientId()));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(authRsp.header("X-Apple-ID-Session-Id")));
        headers.put("scnt",ListUtil.toList(authRsp.header("scnt")));

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
            rsp = HttpUtil.createPost(url)
                    .header(headers)
                    .body(body)
                    .cookie(account.getCookie())
                    .execute();

            account.updateLoginInfo(rsp);
            requestLog(url,headers,rsp);

        }
        return rsp;
    }

    public static HttpResponse questions(Account account,HttpResponse authRsp) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/html, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
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
        HttpResponse rsp = HttpUtil.createPost(url)
                .header(headers)
                .body(body)
                .cookie(account.getCookie())
                .execute();

        account.updateLoginInfo(rsp);
        requestLog(url,headers,rsp);

        return rsp;
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
        HttpResponse rsp = HttpUtil.createGet(location)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

        account.updateLoginInfo(rsp);
        requestLog(location,headers,rsp);

        return rsp;
    }

    public static HttpResponse repareOptions(Account account,HttpResponse questionRsp,HttpResponse accountRepairRsp) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
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
        HttpResponse rsp = HttpUtil.createGet(url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

        account.updateLoginInfo(rsp);
        requestLog(url,headers,rsp);

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
        HttpResponse rsp = HttpUtil.createGet(url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

        account.updateLoginInfo(rsp);
        requestLog(url,headers,rsp);

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
        HttpResponse rsp = HttpUtil.createGet(url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

        account.updateLoginInfo(rsp);
        requestLog(url,headers,rsp);

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
        HttpResponse rsp = HttpUtil.createGet(url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

        account.updateLoginInfo(rsp);
        requestLog(url,headers,rsp);

        return rsp;
    }

    public static HttpResponse repareComplete(Account account,HttpResponse repareOptionsSecondRsp,HttpResponse questionRsp) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
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
        HttpResponse rsp = HttpUtil.createPost(url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

        account.updateLoginInfo(rsp);
        requestLog(url,headers,rsp);

        return rsp;
    }

    public static HttpResponse token(Account account,HttpResponse securityCodeOrReparCompleteRsp) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Apple-I-Request-Context",ListUtil.toList("ca"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("scnt",ListUtil.toList(securityCodeOrReparCompleteRsp.header("scnt")));

        String url = "https://appleid.apple.com/account/manage/gs/ws/token";
        HttpResponse rsp = HttpUtil.createGet(url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

        account.updateLoginInfo(rsp);
        account.setScnt(securityCodeOrReparCompleteRsp.header("scnt"));
        requestLog(url,headers,rsp);
        return rsp;
    }

    /**
     * 获取账户信息
     */
    public static HttpResponse account(Account account) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        headers.put("User-Agent",ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(account.getXAppleIDSessionId()));
        headers.put("scnt",ListUtil.toList(account.getScnt()));

        String url = "https://appleid.apple.com/account/manage";
        HttpResponse rsp = HttpUtil.createGet(url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

        account.updateLoginInfo(rsp);
        requestLog(url,headers,rsp);

        return rsp;
    }

    /**
     * 修改用户生日信息
     * @param birthday 生日 yyyy-MM-dd
     */
    public static HttpResponse updateBirthday(Account account,String birthday) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
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

        String url = "https://appleid.apple.com/account/manage/security/birthday";
        String[] birthdayArr = birthday.split("-");
        String format = "{\"dayOfMonth\":\"%s\",\"monthOfYear\":\"%s\",\"year\":\"%s\"}";
        String body = String.format(format, birthdayArr[2], birthdayArr[1], birthdayArr[0]);

        HttpResponse rsp = HttpUtil.createRequest(Method.PUT, url)
                .body(body)
                .header(headers)
                .execute();

        account.updateLoginInfo(rsp);
        requestLog(url,headers,rsp);

        return rsp;
    }

    /**
     * 移除救援邮箱
     */
    public static HttpResponse deleteRescueEmail(Account account) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
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
        HttpResponse rsp = HttpUtil.createRequest(Method.DELETE, url)
                .header(headers)
                .execute();

        int status = rsp.getStatus();
        rspLog(Method.DELETE,url,status);

        if(status == 302){
            Console.log("未设置救援电子邮件");
            return rsp;
        }

        // 需要验证密码
        if (status == 451){
            verifyPassword(rsp,account.getPwd());
            return deleteRescueEmail(account);
        }

        return rsp;
    }

    /**
     * 新增救援邮箱前置
     */
    public static HttpResponse addRescueEmailSendVerifyCode(String scnt,String password,String rescueEmail) {
        String url = "https://appleid.apple.com/account/manage/security/email/rescue/verification";
        HashMap<String, List<String>> headers = buildHeader();
        headers.put("scnt", ListUtil.toList(scnt));

        HttpResponse rsp = HttpUtil.createRequest(Method.POST, url)
                .header(headers)
                .body("{\"address\":\""+rescueEmail+"\"}")
                .execute();

        int status = rsp.getStatus();
        rspLog(Method.POST,url,status);

        // 需要验证密码
        if (status == 451){
            verifyPassword(rsp,password);
            return addRescueEmailSendVerifyCode(scnt,password,rescueEmail);
        }
        return rsp;
    }

    /**
     * 新增救援邮箱
     */
    public static HttpResponse addRescueEmail(HttpResponse verifyRsp,String rescueEmail,String answer) {
        String url = "https://appleid.apple.com/account/manage/security/email/rescue/verification";
        String body = "{\"address\":\""+rescueEmail+"\",\"verificationInfo\":{\"id\":\""+JSONUtil.parse(verifyRsp.body()).getByPath("verificationId")+"\",\"answer\":\""+answer+"\"}}";
        HttpResponse rsp = HttpUtil.createRequest(Method.PUT, url)
                .header("Accept","application/json, text/plain, */*")
                .header("Accept-Encoding","gzip, deflate, br")
                .header("Accept-Language","zh-CN,zh;q=0.9")
                .header("Cache-Control","no-cache")
                .header("Connection","keep-alive")
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
                .body(body)
                .execute();
        int status = rsp.getStatus();
        rspLog(Method.PUT,url,status);
        return rsp;
    }

    /**
     * 修改名称
     */
    public static HttpResponse updateName(Account account,String password,String firstName,String lastName) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
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

        HttpResponse rsp = HttpUtil.createRequest(Method.PUT, url)
                .header(headers)
                .body(body)
                .execute();

        int status = rsp.getStatus();
        rspLog(Method.PUT,url,status);

        // 需要验证密码
        if (status == 451){
            verifyPassword(rsp,password);
            return updateName(account,password,firstName,lastName);
        }
        account.updateLoginInfo(rsp);
        return rsp;
    }

    /**
     * 修改密码
     */
    public static HttpResponse updatePassword(Account account,String password,String newPassword){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
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

        String url = "https://appleid.apple.com/account/manage/security/password";
        String body = String.format("{\"currentPassword\":\"%s\",\"newPassword\":\"%s\"}",password,newPassword);
        HttpResponse rsp = HttpUtil.createRequest(Method.PUT, url)
                .header(headers)
                .body(body)
                .execute();

        int status = rsp.getStatus();
        account.updateLoginInfo(rsp);
        rspLog(Method.PUT,url,status);
        return rsp;
    }

    /**
     * 修改密保
     */
    public static HttpResponse updateQuestions(Account account,String body){
        String url = "https://appleid.apple.com/account/manage/security/questions";

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
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

        HttpResponse rsp = HttpUtil.createRequest(Method.PUT, url)
                .header(headers)
                .body(body)
                .execute();

        int status = rsp.getStatus();
        rspLog(Method.PUT,url,status);

        // 需要验证密码
        if (status == 451){
            verifyPassword(rsp,account.getPwd());
            return updateQuestions(account,body);
        }
        account.updateLoginInfo(rsp);
        return rsp;
    }

    /**
     * 验证密码
     */
    public static HttpResponse verifyPassword(HttpResponse rsp,String password){
        String verifyPasswordUrl = "https://appleid.apple.com" + rsp.header("Location");
        HttpResponse rsp1 = HttpUtil.createRequest(Method.POST, verifyPasswordUrl)
                .body("{\"password\":\""+password+"\"}")
                .header(rsp.headers())
                .execute();
        rspLog(Method.POST,verifyPasswordUrl,rsp1.getStatus());
        return rsp1;
    }

    /**
     * 获取设备列表
     * todo ? 为什么啥参数都不传可以确定是哪一个Appleid账号啊！
     */
    public static HttpResponse getDeviceList(Account account){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
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

        String url = "https://appleid.apple.com/account/manage/security/devices";
        HttpResponse rsp = HttpUtil.createGet(url)
                .header(headers)
                .execute();
        rspLog(Method.GET,url,rsp.getStatus());
        return rsp;
    }

    /**
     * 移除所有设备
     */
    public static void removeDevices(HttpResponse deviceListRsp){
        String body = deviceListRsp.body();
        JSONObject bodyJSON = JSONUtil.parseObj(body);
        List<String> deviceIdList = bodyJSON.getByPath("devices.id", List.class);

        if (!CollUtil.isEmpty(deviceIdList)){
            for (String deviceId : deviceIdList) {
                String url = "https://appleid.apple.com/account/manage/security/devices/" + deviceId;
                HttpResponse rsp = HttpUtil.createRequest(Method.DELETE,url)
                        .header(deviceListRsp.headers())
                        .execute();
                rspLog(Method.DELETE,url,rsp.getStatus());
            }
        }
    }



    /**
     * 修改appleId时发送邮件
     */
    public static HttpResponse updateAppleIdSendVerifyCode(String scnt,String password,String appleId){
        String url = "https://appleid.apple.com/account/manage/appleid/verification";
        HashMap<String, List<String>> header = buildHeader();
        header.put("scnt",List.of(scnt));
        String body = "{\"name\":\""+appleId+"\"}";
        HttpResponse verifyRsp = HttpUtil.createPost(url)
                .header(header)
                .body(body).execute();
        rspLog(Method.POST,url,verifyRsp.getStatus());

        int status = verifyRsp.getStatus();

        // 需要验证密码
        if (status == 451){
            verifyPassword(verifyRsp,password);
            return updateAppleIdSendVerifyCode(scnt,password,appleId);
        }
        return verifyRsp;
    }

    /**
     * 修改appleId
     */
    public static HttpResponse updateAppleId(HttpResponse verifyRsp,String appleId,String verifyCode){
        String verifyId = JSONUtil.parse(verifyRsp.body()).getByPath("verificationId",String.class);
        String url = "https://appleid.apple.com/account/manage/appleid/verification";
        String body = "{\"name\":\""+appleId+"\",\"verificationInfo\":{\"id\":\""+verifyId+"\",\"answer\":\""+verifyCode+"\"}}";
        HttpResponse updateAppleIdRsp = HttpUtil.createRequest(Method.PUT,url)
                .header(verifyRsp.headers())
                .body(body)
                .execute();
        rspLog(Method.PUT,url,updateAppleIdRsp.getStatus());
        return updateAppleIdRsp;
    }

    /**
     * 双重认证发送短信
     * @param body {"acceptedWarnings":[],"phoneNumberVerification":{"phoneNumber":{"countryCode":"CN","number":"17608177103","countryDialCode":"86","nonFTEU":true},"mode":"sms"}}
     */
    public static HttpResponse securityUpgradeVerifyPhone(Account account,String body){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
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

        String url = "https://appleid.apple.com/account/security/upgrade/verify/phone";
        HttpResponse rsp = HttpUtil.createRequest(Method.PUT, url)
                .header(headers)
                .body(body)
                .execute();
        int status = rsp.getStatus();
        rspLog(Method.PUT,url, status);

        // 需要验证密码
        if (status == 451){
            verifyPassword(rsp,account.getPwd());
            return securityUpgradeVerifyPhone(account,body);
        }
        account.updateLoginInfo(rsp);
        return rsp;
    }

    /**
     * 双重认证
     * @param body {"phoneNumberVerification":{"phoneNumber":{"id":20101,"number":"17608177103","countryCode":"CN","nonFTEU":true},"securityCode":{"code":"563973"},"mode":"sms"}}
     */
    public static HttpResponse securityUpgrade(HttpResponse securityUpgradeVerifyPhoneRsp,String body){
        String url = "https://appleid.apple.com/account/security/upgrade";
        HttpResponse securityUpgradeRsp = HttpUtil.createRequest(Method.POST,url)
                .header(securityUpgradeVerifyPhoneRsp.headers())
                .body(body)
                .execute();
        rspLog(Method.POST,url,securityUpgradeRsp.getStatus());
        return securityUpgradeRsp;
    }

    /**
     * 密保关闭双重认证
     */
    public static HttpResponse securityDowngrade(HttpResponse verifyAppleIdRsp,Account account,String newPwd) {
        String host = "https://iforgot.apple.com";
        String verifyPhone1Location = verifyAppleIdRsp.header("Location");

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
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

        HttpResponse verifyPhone1Rsp = HttpUtil.createGet(host + verifyPhone1Location)
                .header(headers)
                .execute();

        Boolean recoverable = JSONUtil.parse(verifyPhone1Rsp.body()).getByPath("recoverable",Boolean.class);
        if (recoverable == null || !recoverable){
            account.setNote("该账号不能关闭双重认证");
            return null;
        }

        HttpResponse verifyPhone2Rsp = HttpUtil.createGet(host + "/password/verify/phone")
                .header(verifyPhone1Rsp.headers())
                .execute();

        HttpResponse unenrollmentRsp = HttpUtil.createPost(host + "/password/verify/phone/unenrollment")
                .header(verifyPhone2Rsp.headers())
                .execute();

        String verifyBirthday1Location = unenrollmentRsp.header("Location");
        HttpResponse verifyBirthday1Rsp = HttpUtil.createGet(host + verifyBirthday1Location)
                .header(headers)
                .execute();

        DateTime birthday = DateUtil.parse(account.getBirthday());

        HttpResponse verifyBirthday2Rsp = HttpUtil.createPost(host + "/unenrollment/verify/birthday")
                .header(verifyBirthday1Rsp.headers())
                .header("Content-Type","application/json")
                .body("{\"monthOfYear\":\""+(birthday.month()+1)+"\",\"dayOfMonth\":\""+birthday.dayOfMonth()+"\",\"year\":\""+birthday.year()+"\"}")
                .execute();

        String verifyQuestions1Location = verifyBirthday2Rsp.header("Location");
        if (StrUtil.isEmpty(verifyQuestions1Location)){
            account.setNote("生日校验不通过");
            return null;
        }
        HttpResponse verifyQuestions1Rsp = HttpUtil.createGet(host + verifyQuestions1Location)
                .header(headers)
                .execute();

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
        HttpResponse verifyQuestions2Rsp = HttpUtil.createPost(host + "/unenrollment/verify/questions")
                .header(verifyQuestions1Rsp.headers())
                .header("Content-Type","application/json")
                .body(JSONUtil.toJsonStr(bodyMap))
                .execute();

        String unenrollment1Location = verifyQuestions2Rsp.header("Location");
        if (StrUtil.isEmpty(verifyQuestions1Location)){
            account.setNote("密保校验不通过");
            return null;
        }
        HttpResponse unenrollment1Rsp = HttpUtil.createGet(host + unenrollment1Location)
                .header(headers)
                .execute();

        HttpResponse unenrollment2Rsp = HttpUtil.createPost(host + "/unenrollment")
                .header(unenrollment1Rsp.headers())
                .execute();

        String unenrollmentReset1Location = unenrollment2Rsp.header("Location");
        HttpResponse unenrollmentReset1Rsp = HttpUtil.createGet(host + unenrollmentReset1Location)
                .header(headers)
                .execute();

        HttpResponse unenrollmentReset2Rsp = HttpUtil.createPost(host + "/unenrollment/reset")
                .header(unenrollmentReset1Rsp.headers())
                .header("Content-Type","application/json")
                .body("{\"password\":\""+newPwd+"\"}")
                .execute();

        return unenrollmentReset2Rsp;
    }


    /**
     * 生成支持pin
     */
    public static HttpResponse supportPin(String scnt){
        String url = "https://appleid.apple.com/account/manage/supportpin";
        HttpResponse supportPinRsp = HttpUtil.createPost(url)
                .header("scnt", scnt)
                .execute();
        rspLog(Method.POST,url,supportPinRsp.getStatus());
        return supportPinRsp;
    }

    /**
     * 收款方式列表
     */
    public static HttpResponse paymentList(String scnt){
        String url = "https://appleid.apple.com/account/manage/payment";
        HashMap<String, List<String>> header = buildHeader();
        header.put("scnt",List.of(scnt));

        HttpResponse paymentRsp = HttpUtil.createGet(url)
                .header(header)
                .execute();
        rspLog(Method.GET,url,paymentRsp.getStatus());
        return paymentRsp;
    }

    /**
     * 获取图形验证码
     */
    public static HttpResponse captcha(){
        String url = "https://iforgot.apple.com/captcha?captchaType=IMAGE";
        return HttpUtil.createGet(url)
                .header(buildHeader())
                .execute();
    }

    /**
     * 验证码并且验证通过(如果验证码不通过则重试三次)
     */
    public static HttpResponse captchaAndVerify(String appleId) {
        return captchaAndVerify(appleId,3);
    }
    public static HttpResponse captchaAndVerify(String appleId,Integer retry){
        HttpResponse captchaRsp = captcha();
        String body = captchaRsp.body();
        if (StrUtil.isEmpty(body)){
            return captchaAndVerify(appleId,--retry);
        }

        JSON captchaRspJSON = JSONUtil.parse(body);

        String  captBase64 = captchaRspJSON.getByPath("payload.content", String.class);
        if (StrUtil.isEmpty(captBase64)){
            return captchaAndVerify(appleId,--retry);
        }

        Integer captId     = captchaRspJSON.getByPath("id", Integer.class);
        String  captToken  = captchaRspJSON.getByPath("token", String.class);
        String  captAnswer = OcrUtil.recognize(captBase64);

        String verifyAppleIdBody = "{\"id\":\"%s\",\"captcha\":{\"id\":%d,\"answer\":\"%s\",\"token\":\"%s\"}}";
        verifyAppleIdBody = String.format(verifyAppleIdBody,appleId,captId,captAnswer,captToken);
        HttpResponse verifyAppleIdRsp = AppleIDUtil.verifyAppleId(verifyAppleIdBody);

        // 验证码错误才重新尝试
        if (verifyAppleIdRsp.getStatus() != 302 && retry > 0){
            Console.log("[验证码识别错误] Base64: " + captBase64);
            Console.log("[验证码识别错误] Answer: " + captAnswer);
            return captchaAndVerify(appleId,--retry);
        }

        return verifyAppleIdRsp;
    }

    public static void main(String[] args) {
        HttpResponse httpResponse = captchaAndVerify("davidicweaver@outlook.com",2);
        System.err.println(httpResponse);
    }

    /**
     * 检查appleid是通过怎样的方式去校验(密保/邮件/短信)
     */
    public static HttpResponse verifyAppleId(String body) {
        String url = "https://iforgot.apple.com/password/verify/appleid";
        HttpResponse verifyAppleIdRsp = HttpUtil.createPost(url)
                .header(buildHeader())
                .body(body)
                .execute();
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
        // 解锁并且改密
        if (unlock){
            rsp = AppleIDUtil.unlockAndUpdatePwdByProtection(verifyAppleIdRsp,account,newPwd);
        }else{//忘记密码
            rsp = AppleIDUtil.verifyAppleIdByPwdProtection(verifyAppleIdRsp,account,newPwd);
        }
        return rsp;
    }

    /**
     * 忘记密码
     */
    public static HttpResponse verifyAppleIdByPwdProtection(HttpResponse verifyAppleIdRsp,Account account,String newPwd) {
        String host = "https://iforgot.apple.com";
        String options1Location = verifyAppleIdRsp.header("Location");
        HttpResponse options1Rsp = HttpUtil.createGet(host + options1Location)
                .header(buildHeader())
                .execute();
        List<String> recoveryOptions = JSONUtil.parse(options1Rsp.body()).getByPath("recoveryOptions", List.class);
        Console.log("recoveryOptions:", recoveryOptions);

        HttpResponse options2Rsp = HttpUtil.createGet(host + "/recovery/options")
                .header(options1Rsp.headers())
                .execute();

        HttpResponse options3Rsp = HttpUtil.createPost(host + "/recovery/options")
                .header(options2Rsp.headers())
                .header("Content-Type","application/json")
                .body("{\"recoveryOption\":\"reset_password\"}")
                .execute();

        String authMethod1Location = options3Rsp.header("Location");
        HttpResponse authMethod1Rsp = HttpUtil.createGet(host + authMethod1Location)
                .header(buildHeader())
                .execute();
        List<String> authMethodOptions = JSONUtil.parse(authMethod1Rsp.body()).getByPath("options", List.class);
        Console.log("authMethodOptions:", authMethodOptions);

        HttpResponse authMethod2Rsp = HttpUtil.createPost(host + "/password/authenticationmethod")
                .header(authMethod1Rsp.headers())
                .header("Content-Type","application/json")
                .body("{\"type\":\"questions\"}")
                .execute();

        String verifyBirthday1Location = authMethod2Rsp.header("Location");
        HttpResponse verifyBirthday1Rsp = HttpUtil.createGet(host + verifyBirthday1Location)
                .header(buildHeader())
                .execute();

        DateTime birthday = DateUtil.parse(account.getBirthday());
        HttpResponse verifyBirthday2Rsp = HttpUtil.createPost(host + "/password/verify/birthday")
                .header(verifyBirthday1Rsp.headers())
                .header("Content-Type","application/json")
                .body("{\"monthOfYear\":\""+(birthday.month()+1)+"\",\"dayOfMonth\":\""+birthday.dayOfMonth()+"\",\"year\":\""+birthday.year()+"\"}")
                .execute();

        String verifyQuestions1Location = verifyBirthday2Rsp.header("Location");
        HttpResponse verifyQuestions1Rsp = HttpUtil.createGet(host + verifyQuestions1Location)
                .header(buildHeader())
                .execute();

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
        HttpResponse verifyQuestions2Rsp = HttpUtil.createPost(host + "/password/verify/questions")
                .header(verifyQuestions1Rsp.headers())
                .header("Content-Type","application/json")
                .body(JSONUtil.toJsonStr(bodyMap))
                .execute();

        String resrtPasswordOptionLocation = verifyQuestions2Rsp.header("Location");
        HttpResponse resrtPasswordOptionRsp = HttpUtil.createGet(host + resrtPasswordOptionLocation)
                .header(buildHeader())
                .execute();

        String passwordReset1Location = resrtPasswordOptionRsp.header("Location");
        HttpResponse passwordReset1Rsp = HttpUtil.createGet(host + passwordReset1Location)
                .header(buildHeader())
                .execute();

        HttpResponse passwordReset2Rsp = HttpUtil.createPost(host + "/password/reset")
                .header(passwordReset1Rsp.headers())
                .header("Content-Type","application/json")
                .body("{\"password\":\""+newPwd+"\"}")
                .execute();

        return passwordReset2Rsp;
    }

    /**
     * 解锁改密
     */
    public static HttpResponse unlockAndUpdatePwdByProtection(HttpResponse verifyAppleIdRsp,Account account,String newPwd) {
        String host = "https://iforgot.apple.com";

        String authMethod1Location = verifyAppleIdRsp.header("Location");
        HttpResponse authMethod1Rsp = HttpUtil.createGet(host + authMethod1Location)
                .header(buildHeader())
                .execute();
        List<String> authMethodOptions = JSONUtil.parse(authMethod1Rsp.body()).getByPath("options", List.class);
        Console.log("authMethodOptions:", authMethodOptions);

        HttpResponse authMethod2Rsp = HttpUtil.createPost(host + "/password/authenticationmethod")
                .header(authMethod1Rsp.headers())
                .header("Content-Type","application/json")
                .body("{\"type\":\"questions\"}")
                .execute();

        String verifyBirthday1Location = authMethod2Rsp.header("Location");
        HttpResponse verifyBirthday1Rsp = HttpUtil.createGet(host + verifyBirthday1Location)
                .header(buildHeader())
                .execute();

        DateTime birthday = DateUtil.parse(account.getBirthday());
        HttpResponse verifyBirthday2Rsp = HttpUtil.createPost(host + "/password/verify/birthday")
                .header(verifyBirthday1Rsp.headers())
                .header("Content-Type","application/json")
                .body("{\"monthOfYear\":\""+(birthday.month()+1)+"\",\"dayOfMonth\":\""+birthday.dayOfMonth()+"\",\"year\":\""+birthday.year()+"\"}")
                .execute();

        String verifyQuestions1Location = verifyBirthday2Rsp.header("Location");
        HttpResponse verifyQuestions1Rsp = HttpUtil.createGet(host + verifyQuestions1Location)
                .header(buildHeader())
                .execute();

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
        HttpResponse verifyQuestions2Rsp = HttpUtil.createPost(host + "/password/verify/questions")
                .header(verifyQuestions1Rsp.headers())
                .header("Content-Type","application/json")
                .body(JSONUtil.toJsonStr(bodyMap))
                .execute();

        String options1Location = verifyQuestions2Rsp.header("Location");

        HttpResponse options1Rsp = HttpUtil.createGet(host + options1Location)
                .header(buildHeader())
                .execute();
        List<String> recoveryOptions = JSONUtil.parse(options1Rsp.body()).getByPath("types", List.class);
        Console.log("types:", recoveryOptions);

        HttpResponse options2Rsp = HttpUtil.createPost(host + "/password/reset/options")
                .header(options1Rsp.headers())
                .header("Content-Type","application/json")
                .body("{\"type\":\"password_reset\"}")
                .execute();

        String passwordReset1Location = options2Rsp.header("Location");
        HttpResponse passwordReset1Rsp = HttpUtil.createGet(host + passwordReset1Location)
                .header(buildHeader())
                .execute();

        HttpResponse passwordReset2Rsp = HttpUtil.createPost(host + "/password/reset")
                .header(passwordReset1Rsp.headers())
                .header("Content-Type","application/json")
                .body("{\"password\":\""+newPwd+"\"}")
                .execute();

        return passwordReset2Rsp;
    }

    private static HashMap<String, List<String>> buildHeader() {
        return buildHeader(true);
    }

    private static HashMap<String, List<String>> buildHeader(boolean hasX) {
        return buildHeader(hasX, null);
    }

    private static HashMap<String, List<String>> buildHeader(HttpResponse step211Res) {
        return buildHeader(true, step211Res);
    }

    private static HashMap<String, List<String>> buildHeader(boolean hasX, HttpResponse step211Res) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("User-Agent", ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));
        if (hasX) {
            headers.put("X-Apple-Domain-Id", ListUtil.toList("1"));
            headers.put("X-Apple-Frame-Id", ListUtil.toList("auth-ac2s4hiu-l2as-1iqj-r1co-mplxcacq"));
            headers.put("X-Apple-Widget-Key", ListUtil.toList("af1139274f266b22b68c2a3e7ad932cb3c0bbe854e13a79af78dcc73136882c3"));
        }
        if (step211Res != null) {
            headers.put("X-Apple-ID-Session-Id", ListUtil.toList(step211Res.header("X-Apple-ID-Session-Id")));
            headers.put("scnt", ListUtil.toList(step211Res.header("scnt")));
        }
        return headers;
    }

    private static String getCookie(HttpResponse rsp) {
        StringBuilder cookieBuilder = new StringBuilder();
        List<String> res1Cookies = rsp.headers().get("Set-Cookie");
        List<String> res2Cookies = rsp.headers().get("set-cookie");

        if (res1Cookies != null) {
            for (String item : res1Cookies) {
                cookieBuilder.append(";").append(item);
            }
        }
        if (res2Cookies != null) {
            for (String item : res2Cookies) {
                cookieBuilder.append(";").append(item);
            }
        }
        String cookies = "";
        if(cookieBuilder.toString().length() > 0){
            cookies = cookieBuilder.toString().substring(1);
        }
        return cookies;
    }

    private static void rspLog(Method method,String url,Integer status){
        Console.log("[{}] {}  Response status: {}",method.name(),url,status);
    }

    private static void requestLog(String url,HashMap<String, List<String>> headers,HttpResponse rsp){
        Console.log("Req: {}  Response status: {}",url,rsp.getStatus());
        //Console.log(rsp.headers());
        //Console.log(rsp.headerList("Set-Cookie"));
        Console.log("X-Apple-ID-Session-Id:" + headers.get("X-Apple-ID-Session-Id"));
        Console.log("X-Apple-Repair-Session-Token:" + headers.get("X-Apple-Repair-Session-Token"));
        Console.log("X-Apple-Session-Token:" + headers.get("X-Apple-Session-Token"));
        Console.log("SCNT: " + headers.get("scnt"));

        Console.log("X-Apple-ID-Session-Id:" + rsp.header("X-Apple-ID-Session-Id"));
        Console.log("X-Apple-Repair-Session-Token:" + rsp.header("X-Apple-Repair-Session-Token"));
        Console.log("X-Apple-Session-Token:" + rsp.header("X-Apple-Session-Token"));
        Console.log("SCNT: " + rsp.header("scnt"));
        Console.log("------------------------------------------------------------------------------");
    }

    public boolean hasFailMessage(HttpResponse rsp) {
        String body = rsp.body();
        if (StrUtil.isEmpty(body) || JSONUtil.isTypeJSON(body)){
            return false;
        }
        Object hasError = JSONUtil.parseObj(body).getByPath("hasError");
        return null != hasError && (boolean) hasError;
    }

    public String failMessage(HttpResponse rsp) {
        String message = "";
        Object service_errors = JSONUtil.parseObj(rsp.body()).getByPath("service_errors");
        for (Object o : JSONUtil.parseArray(service_errors)) {
            JSONObject jsonObject = (JSONObject) o;
            message += jsonObject.getByPath("message") + ";";
        }
        return message;
    }
}

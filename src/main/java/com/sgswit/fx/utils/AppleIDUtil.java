package com.sgswit.fx.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.common.UnavailableException;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.LoginInfo;
import com.sgswit.fx.model.Question;
import com.sgswit.fx.utils.proxy.ProxyUtil;
import org.apache.commons.lang3.StringUtils;

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
        String url = "https://idmsa.apple.com/appleauth/auth/signin?isRememberMeEnabled=false";
        HttpResponse rsp = ProxyUtil.createPost(url)
                .header(headers)
                .body(scBogy)
                .execute();
        account.updateLoginInfo(rsp);
        return rsp;
    }

    public static HttpResponse auth(LoginInfo loginInfo, HttpResponse signInRsp) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/html, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
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
        HttpResponse rsp = ProxyUtil.createPost(url)
                .header(headers)
                .cookie(loginInfo.getCookie())
                .execute();

        loginInfo.updateLoginInfo(rsp);
        return rsp;
    }

    public static HttpResponse securityCode(LoginInfo loginInfo,HttpResponse authRsp) {
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
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
            rsp = ProxyUtil.createPost(url)
                    .header(headers)
                    .body(body)
                    .cookie(loginInfo.getCookie())
                    .execute();

            loginInfo.updateLoginInfo(rsp);
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
        HttpResponse rsp = ProxyUtil.createPost(url)
                .header(headers)
                .body(body)
                .cookie(account.getCookie())
                .execute();

        account.updateLoginInfo(rsp);
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
        HttpResponse rsp = ProxyUtil.createGet(location)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

        account.updateLoginInfo(rsp);
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
        HttpResponse rsp = ProxyUtil.createGet(url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

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
        HttpResponse rsp = ProxyUtil.createGet(url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

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
        HttpResponse rsp = ProxyUtil.createGet(url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

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
        HttpResponse rsp = ProxyUtil.createGet(url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

        account.updateLoginInfo(rsp);
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
        HttpResponse rsp = ProxyUtil.createPost(url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

        account.updateLoginInfo(rsp);


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
        HttpResponse rsp = ProxyUtil.createGet(url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

        account.updateLoginInfo(rsp);
        account.setScnt(securityCodeOrReparCompleteRsp.header("scnt"));

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
        HttpResponse rsp = ProxyUtil.createGet(url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

        account.updateLoginInfo(rsp);


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

        HttpResponse rsp = ProxyUtil.createRequest(Method.PUT, url)
                .body(body)
                .header(headers)
                .cookie(account.getCookie())
                .execute();
//        account.updateLoginInfo(rsp);
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
        HttpResponse rsp = ProxyUtil.createRequest(Method.DELETE, url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

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

        HttpResponse rsp = ProxyUtil.createRequest(Method.POST, url)
                .header(headers)
                .body("{\"address\":\""+account.getEmail()+"\"}")
                .cookie(account.getCookie())
                .execute();

        int status = rsp.getStatus();


        // 需要验证密码
        if (status == 451){
            verifyPassword(rsp,account);
            return addRescueEmailSendVerifyCode(account);
        }

        account.updateLoginInfo(rsp);
        return rsp;
    }

    /**
     * 新增救援邮箱
     */
    public static HttpResponse addRescueEmail(HttpResponse verifyRsp,Account account,String answer) {
        String url = "https://appleid.apple.com/account/manage/security/email/rescue/verification";
        String body = "{\"address\":\""+account.getEmail()+"\",\"verificationInfo\":{\"id\":\""+JSONUtil.parse(verifyRsp.body()).getByPath("verificationId")+"\",\"answer\":\""+answer+"\"}}";
        HttpResponse rsp = ProxyUtil.createRequest(Method.PUT, url)
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
        account.updateLoginInfo(rsp);
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

        HttpResponse rsp = ProxyUtil.createRequest(Method.PUT, url)
                .header(headers)
                .cookie(account.getCookie())
                .body(body)
                .execute();

        int status = rsp.getStatus();


        // 需要验证密码
        if (status == 451){
            verifyPassword(rsp,account);
            return updateName(account,password,firstName,lastName);
        }
//        account.updateLoginInfo(rsp);
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
        HttpResponse rsp = ProxyUtil.createRequest(Method.PUT, url)
                .header(headers)
                .cookie(account.getCookie())
                .body(body)
                .execute();
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


        account.getCookie();

        HttpResponse rsp = ProxyUtil.createRequest(Method.PUT, url)
                .header(headers)
                .cookie(account.getCookie())
                .body(body)
                .execute();

        int status = rsp.getStatus();


        // 需要验证密码
        if (status == 451){
            verifyPassword(rsp,account);
            return updateQuestions(account,body);
        }
//        account.updateLoginInfo(rsp);
        return rsp;
    }

    /**
     * 验证密码
     */
    public static HttpResponse verifyPassword(HttpResponse rsp,Account account){
        String verifyPasswordUrl = "https://appleid.apple.com" + rsp.header("Location");
        HttpResponse rsp1 = ProxyUtil.createRequest(Method.POST, verifyPasswordUrl)
                .body("{\"password\":\""+account.getPwd()+"\"}")
                .header(rsp.headers())
                .cookie(account.getCookie())
                .execute();
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
        HttpResponse rsp = ProxyUtil.createGet(url)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

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
                HttpResponse rsp = ProxyUtil.createRequest(Method.DELETE,url)
                        .header(deviceListRsp.headers())
                        .cookie(deviceListRsp.getCookies())
                        .execute();
            }
        }
    }

    /**
     * 修改显示语言
     */
    public static HttpResponse changeShowLanguage(Account account,String lang){
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

        String url = "https://appleid.apple.com/account/manage/preferences";
        String format = "{\"preferredLanguage\":\"%s\",\"marketingPreferences\":{\"appleUpdates\":true,\"iTunesUpdates\":true,\"appleNews\":false,\"appleMusic\":false},\"privacyPreferences\":{\"allowDeviceDiagnosticsAndUsage\":false,\"allowShareThirdPartyDevelopers\":false,\"allowICloudDataAnalytics\":false}}";
        String body = String.format(format, lang);

        HttpResponse rsp = ProxyUtil.createRequest(Method.PUT, url)
                .body(body)
                .header(headers)
                .cookie(account.getCookie())
                .execute();

//        account.updateLoginInfo(rsp);

        return rsp;
    }

    /**
     * 修改appleId时发送邮件
     */
    public static HttpResponse updateAppleIdSendVerifyCode(Account account){
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

        String url = "https://appleid.apple.com/account/manage/appleid/verification";
        String body = "{\"name\":\""+account.getEmail()+"\"}";
        HttpResponse verifyRsp = ProxyUtil.createPost(url)
                .header(headers)
                .cookie(account.getCookie())
                .body(body)
                .execute();

        int status = verifyRsp.getStatus();

        // 需要验证密码
        if (status == 451){
            verifyPassword(verifyRsp,account);
            return updateAppleIdSendVerifyCode(account);
        }
        account.updateLoginInfo(verifyRsp);
        return verifyRsp;
    }

    /**
     * 修改appleId
     */
    public static HttpResponse updateAppleId(HttpResponse verifyRsp,Account account,String verifyCode){
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

        String verifyId = JSONUtil.parse(verifyRsp.body()).getByPath("verificationId",String.class);
        String url = "https://appleid.apple.com/account/manage/appleid/verification";
        String body = "{\"name\":\""+account.getEmail()+"\",\"verificationInfo\":{\"id\":\""+verifyId+"\",\"answer\":\""+verifyCode+"\"}}";
        HttpResponse updateAppleIdRsp = ProxyUtil.createRequest(Method.PUT,url)
                .header(headers)
                .cookie(account.getCookie())
                .body(body)
                .execute();
        account.updateLoginInfo(verifyRsp);
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
        HttpResponse rsp = ProxyUtil.createRequest(Method.PUT, url)
                .header(headers)
                .body(body)
                .cookie(account.getCookie())
                .execute();
        int status = rsp.getStatus();

        // 需要验证密码
        if (status == 451){
            verifyPassword(rsp,account);
            return securityUpgradeVerifyPhone(account,body);
        }

        account.updateLoginInfo(rsp);
        return rsp;
    }

    /**
     * 双重认证
     * @param body {"phoneNumberVerification":{"phoneNumber":{"id":20101,"number":"17608177103","countryCode":"CN","nonFTEU":true},"securityCode":{"code":"563973"},"mode":"sms"}}
     */
    public static HttpResponse securityUpgrade(HttpResponse securityUpgradeVerifyPhoneRsp,Account account,String body){
        String url = "https://appleid.apple.com/account/security/upgrade";
        HttpResponse securityUpgradeRsp = ProxyUtil.createRequest(Method.POST,url)
                .header(securityUpgradeVerifyPhoneRsp.headers())
                .body(body)
                .cookie(account.getCookie())
                .execute();
        return securityUpgradeRsp;
    }

    /**
     * 密保关闭双重认证
     */
    public static HttpResponse securityDowngrade(HttpResponse verifyAppleIdRsp,Account account,String newPwd) {
        String host = "https://iforgot.apple.com";
        String verifyPhone1Location = verifyAppleIdRsp.header("Location");

        HttpResponse verifyPhone1Rsp = ProxyUtil.createGet(host + verifyPhone1Location)
                .header(buildHeader(account))
                .execute();

        Boolean recoverable = JSONUtil.parse(verifyPhone1Rsp.body()).getByPath("recoverable",Boolean.class);
        if (recoverable == null || !recoverable){
            account.setNote("该账号不能关闭双重认证");
            return null;
        }

        HttpResponse verifyPhone2Rsp = ProxyUtil.createGet(host + "/password/verify/phone")
                .header(verifyPhone1Rsp.headers())
                .execute();

        HttpResponse unenrollmentRsp = ProxyUtil.createPost(host + "/password/verify/phone/unenrollment")
                .header(verifyPhone2Rsp.headers())
                .execute();

        if (unenrollmentRsp.getStatus() == 503){
            account.setNote("操作频繁，请稍后重试！");
            return null;
        }

        String verifyBirthday1Location = unenrollmentRsp.header("Location");
        HttpResponse verifyBirthday1Rsp = ProxyUtil.createGet(host + verifyBirthday1Location)
                .header(buildHeader(account))
                .execute();

        DateTime birthday = DateUtil.parse(account.getBirthday());

        HttpResponse verifyBirthday2Rsp = ProxyUtil.createPost(host + "/unenrollment/verify/birthday")
                .header(verifyBirthday1Rsp.headers())
                .header("Content-Type","application/json")
                .body("{\"monthOfYear\":\""+(birthday.month()+1)+"\",\"dayOfMonth\":\""+birthday.dayOfMonth()+"\",\"year\":\""+birthday.year()+"\"}")
                .execute();

        if (verifyBirthday2Rsp.getStatus() == 503){
            account.setNote("操作频繁，请稍后重试！");
            return null;
        }

        String verifyQuestions1Location = verifyBirthday2Rsp.header("Location");
        if (StrUtil.isEmpty(verifyQuestions1Location)){
            account.setNote("生日校验不通过");
            return null;
        }
        HttpResponse verifyQuestions1Rsp = ProxyUtil.createGet(host + verifyQuestions1Location)
                .header(buildHeader(account))
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
        HttpResponse verifyQuestions2Rsp = ProxyUtil.createPost(host + "/unenrollment/verify/questions")
                .header(verifyQuestions1Rsp.headers())
                .header("Content-Type","application/json")
                .body(JSONUtil.toJsonStr(bodyMap))
                .execute();

        String unenrollment1Location = verifyQuestions2Rsp.header("Location");
        if (StrUtil.isEmpty(verifyQuestions1Location)){
            account.setNote("密保校验不通过");
            return null;
        }
        HttpResponse unenrollment1Rsp = ProxyUtil.createGet(host + unenrollment1Location)
                .header(buildHeader(account))
                .execute();

        HttpResponse unenrollment2Rsp = ProxyUtil.createPost(host + "/unenrollment")
                .header(unenrollment1Rsp.headers())
                .execute();

        String unenrollmentReset1Location = unenrollment2Rsp.header("Location");
        HttpResponse unenrollmentReset1Rsp = ProxyUtil.createGet(host + unenrollmentReset1Location)
                .header(buildHeader(account))
                .execute();

        HttpResponse unenrollmentReset2Rsp = ProxyUtil.createPost(host + "/unenrollment/reset")
                .header(unenrollmentReset1Rsp.headers())
                .header("Content-Type","application/json")
                .body("{\"password\":\""+newPwd+"\"}")
                .execute();

        return unenrollmentReset2Rsp;
    }


    /**
     * 生成支持pin
     */
    public static HttpResponse supportPin(Account account){
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

        String url = "https://appleid.apple.com/account/manage/supportpin";
        HttpResponse supportPinRsp = ProxyUtil.createPost(url)
                .header(headers)
                .execute();
        return supportPinRsp;
    }

    /**
     * 收款方式列表
     */
    public static HttpResponse paymentList(Account account){
        String url = "https://appleid.apple.com/account/manage/payment";
        HashMap<String, List<String>> header = buildHeader(account);

        HttpResponse paymentRsp = ProxyUtil.createGet(url)
                .header(header)
                .execute();
        return paymentRsp;
    }

    /**
     * 获取图形验证码
     */
    public static HttpResponse captcha(Account account){
        String url = "https://iforgot.apple.com/captcha?captchaType=IMAGE";
        return ProxyUtil.createGet(url)
                .header(buildHeader(account))
                .execute();
    }

    /**
     * 验证码并且验证通过(如果验证码不通过则重试三次)
     */
    public static HttpResponse captchaAndVerify(Account account) {
        return captchaAndVerify(account,10);
    }
    public static HttpResponse captchaAndVerify(Account account,Integer retry){
        HttpResponse captchaRsp = captcha(account);
        account.updateLoginInfo(captchaRsp);

        String body = captchaRsp.body();
        if (StrUtil.isEmpty(body)){
            return captchaAndVerify(account,--retry);
        }

        JSON captchaRspJSON = JSONUtil.parse(body);

        String  captBase64 = captchaRspJSON.getByPath("payload.content", String.class);
        if (StrUtil.isEmpty(captBase64)){
            return captchaAndVerify(account,--retry);
        }

        Integer captId     = captchaRspJSON.getByPath("id", Integer.class);
        String  captToken  = captchaRspJSON.getByPath("token", String.class);
        String  captAnswer = OcrUtil.recognize(captBase64);

        String verifyAppleIdBody = "{\"id\":\"%s\",\"captcha\":{\"id\":%d,\"answer\":\"%s\",\"token\":\"%s\"}}";
        verifyAppleIdBody = String.format(verifyAppleIdBody,account.getAccount(),captId,captAnswer,captToken);
        HttpResponse verifyAppleIdRsp = AppleIDUtil.verifyAppleId(account,verifyAppleIdBody);
        // 验证码错误才重新尝试
        if (verifyAppleIdRsp.getStatus() != 302 && retry > 0){
            return captchaAndVerify(account,--retry);
        }else{
            account.updateLoginInfo(verifyAppleIdRsp);
            return verifyAppleIdRsp;
        }
    }

    /**
     * 检查appleid是通过怎样的方式去校验(密保/邮件/短信)
     */
    public static HttpResponse verifyAppleId(Account account,String body) {
        String url = "https://iforgot.apple.com/password/verify/appleid";
        HashMap<String, List<String>> header = buildHeader(account);
        HttpResponse verifyAppleIdRsp = ProxyUtil.createPost(url)
                .header(header)
                .body(body)
                .cookie(account.getCookie())
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
        HashMap<String, List<String>> header = buildHeader(account);
        header.put("sstt",List.of(verifyAppleIdRsp.header("sstt")));

        String host = "https://iforgot.apple.com";
        String options1Location = verifyAppleIdRsp.header("Location");
        HttpResponse options1Rsp = ProxyUtil.createGet(host + options1Location)
                .header(header)
                .cookie(account.getCookie())
                .execute();
        checkAndThrowUnavailableException(options1Rsp);
        account.updateLoginInfo(options1Rsp);
        header.put("sstt",List.of(verifyAppleIdRsp.header("sstt")));

        List<String> recoveryOptions = JSONUtil.parse(options1Rsp.body()).getByPath("recoveryOptions", List.class);

        HttpResponse options2Rsp = ProxyUtil.createGet(host + "/recovery/options")
                .header(header)
                .cookie(account.getCookie())
                .execute();
        checkAndThrowUnavailableException(options2Rsp);
        account.updateLoginInfo(options2Rsp);

        HttpResponse options3Rsp = ProxyUtil.createPost(host + "/recovery/options")
                .header(header)
                .cookie(account.getCookie())
                .body("{\"recoveryOption\":\"reset_password\"}")
                .execute();
        checkAndThrowUnavailableException(options3Rsp);
        account.updateLoginInfo(options3Rsp);

        String authMethod1Location = options3Rsp.header("Location");
        HttpResponse authMethod1Rsp = ProxyUtil.createGet(host + authMethod1Location)
                .header(header)
                .cookie(account.getCookie())
                .execute();
        checkAndThrowUnavailableException(authMethod1Rsp);
        account.updateLoginInfo(authMethod1Rsp);
        header.put("sstt",List.of(authMethod1Rsp.header("sstt")));

        List<String> authMethodOptions = JSONUtil.parse(authMethod1Rsp.body()).getByPath("options", List.class);
        if(!authMethodOptions.contains("questions")){
            throw new ServiceException("不支持密保问题方式解锁改密");
        }
        HttpResponse authMethod2Rsp = ProxyUtil.createPost(host + "/password/authenticationmethod")
                .header(authMethod1Rsp.headers())
                .header("Content-Type","application/json")
                .cookie(account.getCookie())
                .body("{\"type\":\"questions\"}")
                .execute();
        checkAndThrowUnavailableException(authMethod2Rsp);
        account.updateLoginInfo(authMethod2Rsp);

        String verifyBirthday1Location = authMethod2Rsp.header("Location");
        HttpResponse verifyBirthday1Rsp = ProxyUtil.createGet(host + verifyBirthday1Location)
                .header(header)
                .cookie(account.getCookie())
                .execute();
        checkAndThrowUnavailableException(verifyBirthday1Rsp);
        account.updateLoginInfo(verifyBirthday1Rsp);
        header.put("sstt",List.of(verifyBirthday1Rsp.header("sstt")));
        DateTime birthday=null;
        try {
            birthday = DateUtil.parse(account.getBirthday());
        }catch (Exception e){
            throw new ServiceException("出生日期输入错误！");
        }
        HttpResponse verifyBirthday2Rsp = ProxyUtil.createPost(host + "/password/verify/birthday")
                .header(header)
                .header("Content-Type","application/json")
                .body("{\"monthOfYear\":\""+(birthday.month()+1)+"\",\"dayOfMonth\":\""+birthday.dayOfMonth()+"\",\"year\":\""+birthday.year()+"\"}")
                .cookie(account.getCookie())
                .execute();
        checkAndThrowUnavailableException(verifyBirthday2Rsp);
        account.updateLoginInfo(verifyBirthday2Rsp);

        if (StrUtil.isEmpty(account.getAnswer1()) || StrUtil.isEmpty(account.getAnswer2()) || StrUtil.isEmpty(account.getAnswer3())){
            throw new ServiceException("密保不能为空");
        }

        String verifyQuestions1Location = verifyBirthday2Rsp.header("Location");
        HttpResponse verifyQuestions1Rsp = ProxyUtil.createGet(host + verifyQuestions1Location)
                .header(header)
                .cookie(account.getCookie())
                .execute();
        checkAndThrowUnavailableException(verifyQuestions1Rsp);
        account.updateLoginInfo(verifyQuestions1Rsp);
        header.put("sstt",List.of(verifyQuestions1Rsp.header("sstt")));

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
        HttpResponse verifyQuestions2Rsp = ProxyUtil.createPost(host + "/password/verify/questions")
                .header(header)
                .body(JSONUtil.toJsonStr(bodyMap))
                .cookie(account.getCookie())
                .execute();
        checkAndThrowUnavailableException(verifyQuestions2Rsp);
        account.updateLoginInfo(verifyQuestions2Rsp);
        if(400==verifyQuestions2Rsp.getStatus()){
            JSON questionsResponseBodyJson= JSONUtil.parse(verifyQuestions2Rsp.body());
            StringBuffer m=new StringBuffer();
            JSONArray jsonArray=JSONUtil.parseArray(questionsResponseBodyJson.getByPath("serviceErrors"));
            for(Object o:jsonArray){
                JSONObject jsonObject=(JSONObject)o;
                String code=jsonObject.getStr("code");
                if("crIncorrect".equals(code)){
                    m.append("输入的密保答案错误；");
                }
            }
            throw new ServiceException(m.toString());
        }
        String resrtPasswordOptionLocation = verifyQuestions2Rsp.header("Location");
        HttpResponse resrtPasswordOptionRsp = ProxyUtil.createGet(host + resrtPasswordOptionLocation)
                .header(header)
                .cookie(account.getCookie())
                .execute();
        checkAndThrowUnavailableException(resrtPasswordOptionRsp);
        account.updateLoginInfo(resrtPasswordOptionRsp);
        header.put("sstt",List.of(resrtPasswordOptionRsp.header("sstt")));

        String passwordReset1Location = resrtPasswordOptionRsp.header("Location");
        HttpResponse passwordReset1Rsp = ProxyUtil.createGet(host + passwordReset1Location)
                .header(header)
                .cookie(account.getCookie())
                .execute();
        checkAndThrowUnavailableException(passwordReset1Rsp);
        account.updateLoginInfo(passwordReset1Rsp);
        header.put("sstt",List.of(passwordReset1Rsp.header("sstt")));

        HttpResponse passwordReset2Rsp = ProxyUtil.createPost(host + "/password/reset")
                .header(header)
                .header("Content-Type","application/json")
                .body("{\"password\":\""+newPwd+"\"}")
                .cookie(account.getCookie())
                .execute();
        checkAndThrowUnavailableException(passwordReset2Rsp);
        account.updateLoginInfo(passwordReset2Rsp);

        return passwordReset2Rsp;
    }

    /**
     * 解锁改密
     */
    public static HttpResponse unlockAndUpdatePwdByProtection(HttpResponse verifyAppleIdRsp,Account account,String newPwd) {
        HashMap<String, List<String>> header = buildHeader(account);
        header.put("sstt",List.of(verifyAppleIdRsp.header("sstt")));

        String host = "https://iforgot.apple.com";

        String authMethod1Location = verifyAppleIdRsp.header("Location");
        HttpResponse authMethod1Rsp = ProxyUtil.createGet(host + authMethod1Location)
                .header(header)
                .cookie(account.getCookie())
                .execute();
        checkAndThrowUnavailableException(authMethod1Rsp);

        account.updateLoginInfo(authMethod1Rsp);
        header.put("sstt",List.of(authMethod1Rsp.header("sstt")));

        List<String> authMethodOptions = JSONUtil.parse(authMethod1Rsp.body()).getByPath("options", List.class);
        if(!authMethodOptions.contains("questions")){
            throw new ServiceException("不支持密保问题方式解锁改密");
        }
        HttpResponse authMethod2Rsp = ProxyUtil.createPost(host + "/password/authenticationmethod")
                .header(header)
                .cookie(account.getCookie())
                .body("{\"type\":\"questions\"}")
                .execute();

        checkAndThrowUnavailableException(authMethod2Rsp);
        account.updateLoginInfo(authMethod2Rsp);
        String verifyBirthday1Location = authMethod2Rsp.header("Location");
        HttpResponse verifyBirthday1Rsp = ProxyUtil.createGet(host + verifyBirthday1Location)
                .header(header)
                .cookie(account.getCookie())
                .execute();

        checkAndThrowUnavailableException(verifyBirthday1Rsp);
        account.updateLoginInfo(verifyBirthday1Rsp);
        header.put("sstt",List.of(verifyBirthday1Rsp.header("sstt")));

        DateTime birthday=null;
        try {
            birthday = DateUtil.parse(account.getBirthday());
        }catch (Exception e){
            throw new ServiceException("出生日期输入错误！");
        }
        HttpResponse verifyBirthday2Rsp = ProxyUtil.createPost(host + "/password/verify/birthday")
                .header(header)
                .cookie(account.getCookie())
                .body("{\"monthOfYear\":\""+(birthday.month()+1)+"\",\"dayOfMonth\":\""+birthday.dayOfMonth()+"\",\"year\":\""+birthday.year()+"\"}")
                .execute();
        checkAndThrowUnavailableException(verifyBirthday2Rsp);
        account.updateLoginInfo(verifyBirthday2Rsp);
        if(400==verifyBirthday2Rsp.getStatus()){
            JSON birthdayResponseBodyJson= JSONUtil.parse(verifyBirthday2Rsp.body());
            StringBuffer m=new StringBuffer();
            JSONArray jsonArray=JSONUtil.parseArray(birthdayResponseBodyJson.getByPath("serviceErrors"));
            for(Object o:jsonArray){
                JSONObject jsonObject=(JSONObject)o;
                String code=jsonObject.getStr("code");
                if("crIncorrect".equals(code)){
                    m.append("输入的生日错误；");
                }
            }
            throw new ServiceException(m.toString());
        }

        if (StrUtil.isEmpty(account.getAnswer1()) || StrUtil.isEmpty(account.getAnswer2()) || StrUtil.isEmpty(account.getAnswer3())){
            throw new ServiceException("密保不能为空");
        }

        String verifyQuestions1Location = verifyBirthday2Rsp.header("Location");
        HttpResponse verifyQuestions1Rsp = ProxyUtil.createGet(host + verifyQuestions1Location)
                .header(header)
                .cookie(account.getCookie())
                .execute();

        checkAndThrowUnavailableException(verifyQuestions1Rsp);
        account.updateLoginInfo(verifyQuestions1Rsp);
        header.put("sstt",List.of(verifyQuestions1Rsp.header("sstt")));
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
        HttpResponse verifyQuestions2Rsp = ProxyUtil.createPost(host + "/password/verify/questions")
                .header(header)
                .cookie(account.getCookie())
                .body(JSONUtil.toJsonStr(bodyMap))
                .execute();
        checkAndThrowUnavailableException(verifyQuestions2Rsp);
        account.updateLoginInfo(verifyQuestions2Rsp);


        if(400==verifyQuestions2Rsp.getStatus()){
            JSON questionsResponseBodyJson= JSONUtil.parse(verifyQuestions2Rsp.body());
            StringBuffer m=new StringBuffer();
            JSONArray jsonArray=JSONUtil.parseArray(questionsResponseBodyJson.getByPath("serviceErrors"));
            for(Object o:jsonArray){
                JSONObject jsonObject=(JSONObject)o;
                String code=jsonObject.getStr("code");
                if("crIncorrect".equals(code)){
                    m.append("输入的密保答案错误；");
                }
            }
            throw new ServiceException(m.toString());
        }
        String options1Location = verifyQuestions2Rsp.header("Location");
        HttpResponse options1Rsp = ProxyUtil.createGet(host + options1Location)
                .header(header)
                .cookie(account.getCookie())
                .execute();
        checkAndThrowUnavailableException(options1Rsp);
        account.updateLoginInfo(options1Rsp);

        header.put("sstt",List.of(options1Rsp.header("sstt")));
        HttpResponse options2Rsp = ProxyUtil.createPost(host + "/password/reset/options")
                .header(header)
                .body("{\"type\":\"password_reset\"}")
                .cookie(account.getCookie())
                .execute();
        checkAndThrowUnavailableException(options1Rsp);
        account.updateLoginInfo(options1Rsp);

        String passwordReset1Location = options2Rsp.header("Location");
        HttpResponse passwordReset1Rsp = ProxyUtil.createGet(host + passwordReset1Location)
                .header(header)
                .cookie(account.getCookie())
                .execute();
        checkAndThrowUnavailableException(passwordReset1Rsp);
        account.updateLoginInfo(passwordReset1Rsp);
        header.put("sstt",List.of(passwordReset1Rsp.header("sstt")));

        HttpResponse passwordReset2Rsp = ProxyUtil.createPost(host + "/password/reset")
                .header(header)
                .cookie(account.getCookie())
                .body("{\"password\":\""+newPwd+"\"}")
                .execute();

        checkAndThrowUnavailableException(passwordReset2Rsp);
        account.updateLoginInfo(passwordReset2Rsp);
        return passwordReset2Rsp;
    }

    private static HashMap<String, List<String>> buildHeader(Account account) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
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

    public static void checkAndThrowUnavailableException(HttpResponse response){
        if (response != null){
            if (response.getStatus() == 503){
                throw new UnavailableException();
            }
            if (response.getStatus() == 400){
                throw new ServiceException(getValidationErrors(response,"Bad Request"));
            }
        }
    }

    public static String getValidationErrors(HttpResponse response,String defaultMessage){
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
        return String.join(";",errorMessageList);
    }
}

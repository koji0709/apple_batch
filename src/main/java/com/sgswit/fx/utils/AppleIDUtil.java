package com.sgswit.fx.utils;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.Question;

import java.util.HashMap;
import java.util.List;

/**
 * Hello world!
 *
 */
public class AppleIDUtil
{

    public static HttpResponse signin(Account account){
        HashMap<String, List<String>> headers = buildHeader();

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        String body = "{\"accountName\":\"%s\",\"password\":\"%s\",\"rememberMe\":false,\"trustTokens\":[]}";
        String scBogy = String.format(body,account.getAccount(),account.getPwd());
        HttpResponse res = HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/signin?isRememberMeEnabled=false&isRememberMeEnabled=false")
                .header(headers)
                .body(scBogy)
                .execute();
        return res;
    }

    public static HttpResponse auth(HttpResponse res1){
        HashMap<String, List<String>> headers = buildHeader(res1);
        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));
        HttpResponse res2 = HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth")
                .header(headers)
                .cookie(getCookie(res1))
                .execute();

        return res2;
    }

    public static HttpResponse securityCode(HttpResponse res1,String type,String code){
        HashMap<String, List<String>> headers = buildHeader(res1);
        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        String scDeviceBody = "{\"securityCode\":{\"code\":\"%s\"}}";
        String scPhoneBody  = "{\"phoneNumber\":{\"id\":1},\"securityCode\":{\"code\":\"%s\"},\"mode\":\"sms\"}";

        String scBody = "";
        String scUrl  = "";

        if("device".equals(type)){
            scBody = String.format(scDeviceBody,code);
            scUrl  = "https://idmsa.apple.com/appleauth/auth/verify/trusteddevice/securitycode";
        }else if("sms".equals(type)){
            scBody = String.format(scPhoneBody,code);
            scUrl  = "https://idmsa.apple.com/appleauth/auth/verify/phone/securitycode";
        }

        HttpResponse res2 = null;
        if(!"".equals(scBody)){
            res2 = HttpUtil.createPost(scUrl)
                    .header(headers)
                    .body(scBody)
                    .cookie(getCookie(res1))
                    .execute();
        }
        return res2;
    }

    public static HttpResponse token(HttpResponse res2){
        HashMap<String, List<String>> headers = buildHeader(res2);
        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        HttpResponse res3 = HttpUtil.createGet("https://appleid.apple.com/account/manage/gs/ws/token")
                .header(headers)
                .cookie(getCookie(res2))
                .execute();
        return res3;
    }

    public static HttpResponse manager(HttpResponse res3){
        HashMap<String, List<String>> headers = buildHeader(false, res3);
        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        HttpResponse res4 = HttpUtil.createGet("https://appleid.apple.com/account/manage")
                .header(headers)
                .cookie(getCookie(res3))
                .execute();
        return res4;
    }

    public static HttpResponse questions(HttpResponse res1,Account account){
        HashMap<String, List<String>> headers = buildHeader(res1);
        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        String content = res1.body();
        String questions = content.substring(content.indexOf("{\"direct\":{\"scriptSk7Url\""),content.indexOf("\"additional\":{\"canRoute2sv\":true}}")+35);

        String qj = JSONUtil.parse(questions).getByPath("direct.twoSV.securityQuestions.questions").toString();
        List<Question> qs = JSONUtil.toList(qj,Question.class);
        for (int i = 0 ; i < qs.size() ; i++){
            Question q = qs.get(i);
            if(q.getNumber() == 1){
                q.setAnswer(account.getAnswer1());
            }else if (q.getNumber() == 2){
                q.setAnswer(account.getAnswer2());
            }else  if(q.getNumber() == 3){
                q.setAnswer(account.getAnswer3());
            }
        }

        String scBody = "{\"questions\":" + JSONUtil.parse(qs) + "}";
        HttpResponse res2 = HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/verify/questions")
                .header(headers)
                .body(scBody)
                .cookie(getCookie(res1))
                .execute();

//        System.out.println("------------------questions-----------------------------------------------");
//        System.out.println(res2.getStatus());
//        System.out.println(res2.headers());
//        System.out.println(res2.headerList("Set-Cookie"));
//        System.out.println("------------------questions-----------------------------------------------");

        return res2;
    }

    public static HttpResponse accountRepair(HttpResponse res1){
        HashMap<String, List<String>> headers = buildHeader(false);
        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("Sec-Fetch-Dest",ListUtil.toList("iframe"));
        headers.put("Sec-Fetch-Mode",ListUtil.toList("navigate"));
        headers.put("navigate",ListUtil.toList("same-site"));

        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));

        String location = res1.header("Location");
        HttpResponse res2 = HttpUtil.createGet(location)
                .header(headers)
                .cookie(getCookie(res1))
                .execute();

//        System.out.println("------------------accountRepair-----------------------------------------------");
//        System.out.println(res2.getStatus());
//        System.out.println(res2.headers());
//        System.out.println(res2.headerList("Set-Cookie"));
//        System.out.println("------------------accountRepair-----------------------------------------------");

        return res2;
    }

    public static HttpResponse repareOptions(HttpResponse step211Res,HttpResponse step212Res){
        HashMap<String, List<String>> headers = buildHeader(step211Res);
        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        //headers.put("X-Apple-Skip-Repair-Attributes",ListUtil.toList("[\"hsa2_enrollment\"]"));
        headers.put("X-Apple-Skip-Repair-Attributes",ListUtil.toList("[]"));
        headers.put("X-Apple-Session-Token",ListUtil.toList(step211Res.header("X-Apple-Repair-Session-Token")));

        StringBuilder cookieBuilder = new StringBuilder();
        List<String> res211Cookies = step211Res.headerList("Set-Cookie");
        for(String item : res211Cookies){
            cookieBuilder.append(";").append(item);
        }

        List<String> res212Cookies = step212Res.headerList("Set-Cookie");
        for(String item : res212Cookies){
            cookieBuilder.append(";").append(item);
        }

        //System.out.println(cookieBuilder.toString());
        String scUrl  = "https://appleid.apple.com/account/manage/repair/options";
        HttpResponse res2 = HttpUtil.createGet(scUrl)
                .header(headers)
                .cookie(cookieBuilder.toString())
                .execute();

//        System.out.println("------------------repareOptions-----------------------------------------------");
//        System.out.println(res2.getStatus());
//        System.out.println(res2.headers());
//        System.out.println(res2.headerList("Set-Cookie"));
//        System.out.println("------------------repareOptions-----------------------------------------------");
        return res2;
    }

    public static HttpResponse securityUpgrade(HttpResponse res1,String XAppleIDSessionId,String scnt){
        HashMap<String, List<String>> headers = buildHeader();
        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(XAppleIDSessionId));
        headers.put("scnt",ListUtil.toList(scnt));
        headers.put("X-Apple-Session-Token",ListUtil.toList(res1.header("X-Apple-Session-Token")));
        headers.put("X-Apple-Skip-Repair-Attributes",ListUtil.toList("[]"));

        String scUrl  = "https://appleid.apple.com/account/security/upgrade";
        HttpResponse res2 = HttpUtil.createGet(scUrl)
                .header(headers)
                .cookie(getCookie(res1))
                .execute();

//        System.out.println("------------------securityUpgrade-----------------------------------------------");
//        System.out.println(res2.getStatus());
//        System.out.println(res2.headers());
//        System.out.println(res2.headerList("Set-Cookie"));
//        System.out.println("------------------securityUpgrade-----------------------------------------------");
        return res2;
    }


    public static HttpResponse securityUpgradeSetuplater(HttpResponse res1,String XAppleIDSessionId,String scnt){
        HashMap<String, List<String>> headers = buildHeader();

        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(XAppleIDSessionId));
        headers.put("scnt",ListUtil.toList(scnt));
        headers.put("X-Apple-Session-Token",ListUtil.toList(res1.header("X-Apple-Session-Token")));
        headers.put("X-Apple-Skip-Repair-Attributes",ListUtil.toList("[]"));

        String scUrl  = "https://appleid.apple.com/account/security/upgrade/setuplater";
        HttpResponse res2 = HttpUtil.createGet(scUrl)
                .header(headers)
                .cookie(getCookie(res1))
                .execute();

//        System.out.println("------------------securityUpgradeSetuplater-----------------------------------------------");
//        System.out.println(res2.getStatus());
//        System.out.println(res2.headers());
//        System.out.println(res2.headerList("Set-Cookie"));
//        System.out.println("------------------securityUpgradeSetuplater-----------------------------------------------");
        return res2;
    }


    public static HttpResponse repareOptionsSecond(HttpResponse res1,String XAppleIDSessionId,String scnt){
        HashMap<String, List<String>> headers = buildHeader();

        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(XAppleIDSessionId));
        headers.put("scnt",ListUtil.toList(scnt));
        headers.put("X-Apple-Session-Token",ListUtil.toList(res1.header("X-Apple-Session-Token")));
        headers.put("X-Apple-Skip-Repair-Attributes",ListUtil.toList("[\"hsa2_enrollment\"]"));

        String scUrl  = "https://appleid.apple.com/account/manage/repair/options";
        HttpResponse res2 = HttpUtil.createGet(scUrl)
                .header(headers)
                .cookie(getCookie(res1))
                .execute();

//        System.out.println("------------------repair/options -----------------------------------------------");
//        System.out.println(res2.getStatus());
//        System.out.println(res2.headers());
//        System.out.println(res2.headerList("Set-Cookie"));
//        System.out.println("------------------repair/options -----------------------------------------------");
        return res2;
    }

    public static HttpResponse repareComplete(HttpResponse res1,HttpResponse step211Res){
        HashMap<String, List<String>> headers = buildHeader(step211Res);
        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("X-Apple-Repair-Session-Token",ListUtil.toList(res1.header("X-Apple-Session-Token")));

        String scUrl  = "https://idmsa.apple.com/appleauth/auth/repair/complete";
        HttpResponse res2 = HttpUtil.createPost(scUrl)
                .header(headers)
                .cookie(getCookie(res1))
                .execute();

//        System.out.println("------------------repareComplete-----------------------------------------------");
//        System.out.println(res2.getStatus());
//        System.out.println(res2.headers());
//        System.out.println(res2.headerList("Set-Cookie"));
//        System.out.println("------------------repareComplete-----------------------------------------------");
        return res2;
    }




    private static HashMap<String, List<String>> buildHeader() {
        return buildHeader(true);
    }

    private static HashMap<String, List<String>> buildHeader(boolean hasX){
        return buildHeader(hasX,null);
    }

    private static HashMap<String, List<String>> buildHeader(HttpResponse step211Res){
        return buildHeader(true,step211Res);
    }

    private static HashMap<String, List<String>> buildHeader(boolean hasX,HttpResponse step211Res){
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("User-Agent",ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));
        if (hasX){
            headers.put("X-Apple-Domain-Id", ListUtil.toList("1"));
            headers.put("X-Apple-Frame-Id", ListUtil.toList("auth-ac2s4hiu-l2as-1iqj-r1co-mplxcacq"));
            headers.put("X-Apple-Widget-Key", ListUtil.toList("af1139274f266b22b68c2a3e7ad932cb3c0bbe854e13a79af78dcc73136882c3"));
        }
        if (step211Res != null){
            headers.put("X-Apple-ID-Session-Id",ListUtil.toList(step211Res.header("X-Apple-ID-Session-Id")));
            headers.put("scnt",ListUtil.toList(step211Res.header("scnt")));
        }
        return headers;
    }


    private static String getCookie(HttpResponse resp){
        StringBuilder cookieBuilder = new StringBuilder();
        List<String> res1Cookies = resp.headerList("Set-Cookie");
        for(String item : res1Cookies){
            cookieBuilder.append(";").append(item);
        }
        return cookieBuilder.toString();
    }

}

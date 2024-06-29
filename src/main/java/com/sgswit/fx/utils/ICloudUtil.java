package com.sgswit.fx.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.model.LoginInfo;
import com.sgswit.fx.utils.proxy.ProxyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author DeZh
 * @title: ICloudUtil
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/1320:38
 */
public class ICloudUtil {
    public static HttpResponse checkCloudAccount(String clientId, String appleId, String password){
        //clientId从数据库中获取每个appleId生成一个
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("setup.icloud.com"));
        headers.put("Referer", ListUtil.toList("https://setup.icloud.com/setup/iosbuddy/loginDelegates"));
        headers.put("Content-Type", ListUtil.toList("application/x-apple-plist; Charset=UTF-8"));
        headers.put("Accept", ListUtil.toList("*/*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.9,en;q=0"));
        headers.put("X-MMe-Country", ListUtil.toList("CN"));
        headers.put("User-Agent", ListUtil.toList("Accounts/113 CFNetwork/711.2.23 Darwin/14.0.0"));
        String auth= Base64.encode(appleId+":"+password);
        headers.put("Authorization", ListUtil.toList("Basic " + auth));
        headers.put("X-MMe-Client-Info", ListUtil.toList("<iPhone4,1> <iPhone OS;8.4.1;12H321> <com.apple.AppleAccount/1.0 (com.apple.Preferences/1.0)>"));

        String body =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<plist version=\"1.0\">" +
                "<dict>" +
                "<key>apple-id</key>" +
                "<string>"+appleId+"</string>" +
                "<key>client-id</key>" +
                "<string>"+clientId+"</string>" +
                "<key>delegates</key>" +
                "<dict><key>com.apple.gamecenter</key>" +
                "<dict/>" +
                "<key>com.apple.mobileme</key>" +
                "<dict/>" +
                "<key>com.apple.private.ids</key>" +
                "<dict>" +
                "<key>protocol-version</key>" +
                "<string>4</string>" +
                "</dict> </dict>" +
                "<key>password</key>" +
                "<string>"+password+"</string>" +
                "</dict>" +
                "</plist>";
        HttpResponse response = ProxyUtil.execute(HttpUtil.createPost("https://setup.icloud.com/setup/iosbuddy/loginDelegates")
                        .header(headers)
                        .body(body));
        return response;
    }

    public static String getAuthByHttResponse(HttpResponse response){
        String rb = response.charset("UTF-8").body();
        JSONObject rspJSON = PListUtil.parse(rb);
        String dsid = rspJSON.getStr("dsid");
        String mmeAuthToken= rspJSON.getJSONObject("delegates").getJSONObject("com.apple.mobileme").getByPath("service-data.tokens.mmeAuthToken",String.class);
        String auth = Base64.encode(dsid+":"+mmeAuthToken);
        return auth;
    }
    public static Map<String,Object> getFamilyDetails(String auth,String appleId){
        Map<String,Object> res=new HashMap<>();
        res.put("code",Constant.SUCCESS);
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("setup.icloud.com"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language", ListUtil.toList("zh-cn"));
        headers.put("User-Agent", ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10) AppleWebKit/600.1.3 (KHTML, like Gecko)"));
        headers.put("X-MMe-LoggedIn-AppleID",ListUtil.toList(appleId));
        headers.put("Accept",ListUtil.toList("*/*"));
        headers.put("X-MMe-Client-Info",ListUtil.toList("<MacBook Pro> <Mac OS X;10.10;14A314h> <com.apple.AOSKit/203 (com.apple.systempreferences/14.0)>"));
        headers.put("Authorization",ListUtil.toList("Basic "+auth));

        HttpResponse response = ProxyUtil.execute(HttpUtil.createGet("https://setup.icloud.com/setup/family/getFamilyDetails")
                        .header(headers));

        if(200==response.getStatus()){
            String rb = response.charset("UTF-8").body();
            JSONObject rspJSON = PListUtil.parse(rb);
            String dsid = rspJSON.getStr("dsid");
            res.put("dsid",dsid);
            boolean isMemberOfFamily = rspJSON.getByPath("is-member-of-family",Boolean.class);
            if(!isMemberOfFamily){
                res.put("familyDetails","未加入家庭共享");
            }else{
                //判断账户是否为组织者
                JSONArray array= JSONUtil.parseArray(rspJSON.getStr("family-members"));
                List<String> members=new ArrayList<>(array.size());
                for(Object object:array){
                    JSONObject jsonObject= (JSONObject) object;
                    String memberDisplayLabel=jsonObject.getStr("member-display-label");
                    String memberAppleId=jsonObject.getStr("member-apple-id");
                    if(appleId.equalsIgnoreCase(memberAppleId)){
                        members.add(memberDisplayLabel+"(此账号)");
                    }else{
                        members.add(memberDisplayLabel+"("+memberAppleId+")");
                    }
                }
                res.put("familyDetails",String.join("|",members));
            }
        }else if(response.getStatus()==401){
            res.put("code","1");
            res.put("msg","未登录或登录超时");
        }else {
            res.put("code",response.getStatus());
            res.put("msg",response.body());
        }
        return res;


    }
    /**
    　* 开通家庭共享 xxx@xx.com----密码--付款AppleID账号--付款密码
      * @param
     * @param auth
     * @param appleId
     * @param pwd
     * @param payAppleId
     * @param payAppleIdPwd
    　* @return java.util.Map<java.lang.String,java.lang.Object>
    　* @throws
    　* @author DeZh
    　* @date 2023/12/11 10:53
    */
    public static Map<String,Object> createFamily(String auth,String appleId,String pwd,String payAppleId,String payAppleIdPwd){
        Map<String,Object> res=new HashMap<>();
        res.put("code",Constant.SUCCESS);
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("setup.icloud.com"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language", ListUtil.toList("zh-cn"));
        headers.put("User-Agent", ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10) AppleWebKit/600.1.3 (KHTML, like Gecko)"));
        headers.put("X-MMe-LoggedIn-AppleID",ListUtil.toList(appleId));
        headers.put("Accept",ListUtil.toList("*/*"));
        headers.put("Referer",ListUtil.toList("https://setup.icloud.com/setup/mac/family/setupFamilyUI"));
        headers.put("X-MMe-Client-Info",ListUtil.toList("<MacBook Pro> <Mac OS X;10.10;14A314h> <com.apple.AOSKit/203 (com.apple.systempreferences/14.0)>"));
        headers.put("Authorization",ListUtil.toList("Basic "+auth));

        Map<String,Object> bodyMap=new HashMap<>();
        bodyMap.put("organizerAppleId",appleId);
        bodyMap.put("organizerAppleIdForPurchases",payAppleId);
        bodyMap.put("organizerAppleIdForPurchasesPassword",payAppleIdPwd);
        bodyMap.put("organizerShareMyLocationEnabledDefault",true);

        HttpResponse response = ProxyUtil.execute(HttpUtil.createPost("https://setup.icloud.com/setup/mac/family/createFamily")
                        .header(headers)
                        .body(JSONUtil.toJsonStr(bodyMap)));
        if(200==response.getStatus()){
            String rb = response.charset("UTF-8").body();
            if("0".equals(JSONUtil.parse(rb).getByPath("status",String.class))){
                String familyId=JSONUtil.parse(rb).getByPath("family.familyId",String.class);
                res.put("msg","开通成功，家庭共享ID"+familyId);
            }
        }else if(response.getStatus()==401){
            res.put("code","1");
            res.put("msg","未登录或登录超时");
        }else if(response.getStatus()==422) {
            String rb = response.charset("UTF-8").body();
            res.put("code",JSONUtil.parse(rb).getByPath("status"));
            res.put("msg",JSONUtil.parse(rb).getByPath("status-message"));
        }
        return res;
    }
    /**
    　*关闭家庭共享
      * @param
     * @param auth
     * @param appleId
    　* @return java.util.Map<java.lang.String,java.lang.Object>
    　* @throws
    　* @author DeZh
    　* @date 2023/12/11 13:18
    */
    public static Map<String,Object> leaveFamily(String auth,String appleId){
        Map<String,Object> res=new HashMap<>();
        res.put("code",Constant.SUCCESS);
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("setup.icloud.com"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language", ListUtil.toList("zh-cn"));
        headers.put("User-Agent", ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10) AppleWebKit/600.1.3 (KHTML, like Gecko)"));
        headers.put("X-MMe-LoggedIn-AppleID",ListUtil.toList(appleId));
        headers.put("Accept",ListUtil.toList("*/*"));
        headers.put("Referer",ListUtil.toList("https://setup.icloud.com/setup/mac/family/setupFamilyUI"));
        headers.put("X-MMe-Client-Info",ListUtil.toList("<MacBook Pro> <Mac OS X;10.10;14A314h> <com.apple.AOSKit/203 (com.apple.systempreferences/14.0)>"));
        headers.put("Authorization",ListUtil.toList("Basic "+auth));
        HttpResponse response = ProxyUtil.execute(HttpUtil.createPost("https://setup.icloud.com/setup/mac/family/leaveFamily")
                        .header(headers));
        if(200==response.getStatus()){
            String rb = response.charset("UTF-8").body();
            JSON jsonBody=JSONUtil.parse(rb);
            if("0".equals(jsonBody.getByPath("status",String.class))){
                res.put("msg",JSONUtil.parse(rb).getByPath("status-message"));
                if(!jsonBody.getByPath("isMemberOfFamily",Boolean.class)){
                    res.put("code","1");
                    res.put("msg","未加入家庭共享");
                }
            }
        }else if(response.getStatus()==401){
            res.put("code","1");
            res.put("msg","未登录或登录超时");
        }else if(response.getStatus()==422) {
            String rb = response.charset("UTF-8").body();
            res.put("code",JSONUtil.parse(rb).getByPath("status"));
            res.put("msg",JSONUtil.parse(rb).getByPath("status-message"));
        }
        return res;
    }
    /**
    　* 获取付款方式
      * @param
     * @param auth
     * @param appleId
     * @param organizerDsid
    　* @return java.util.Map<java.lang.String,java.lang.Object>
    　* @throws
    　* @author DeZh
    　* @date 2023/12/12 13:34
    */
    public static Map<String,Object> getiTunesAccountPaymentInfo(String auth,String appleId,String organizerDsid){
        Map<String,Object> res=new HashMap<>();
        res.put("code",Constant.SUCCESS);
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("setup.icloud.com"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language", ListUtil.toList("zh-cn"));
        headers.put("User-Agent", ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10) AppleWebKit/600.1.3 (KHTML, like Gecko)"));
        headers.put("X-MMe-LoggedIn-AppleID",ListUtil.toList(appleId));
        headers.put("Accept",ListUtil.toList("*/*"));
        headers.put("Referer",ListUtil.toList("https://setup.icloud.com/setup/mac/family/setupFamilyUI"));
        headers.put("X-MMe-Client-Info",ListUtil.toList("<MacBook Pro> <Mac OS X;10.10;14A314h> <com.apple.AOSKit/203 (com.apple.systempreferences/14.0)>"));
        headers.put("Authorization",ListUtil.toList("Basic "+auth));

        String format="{\"organizerDSID\":\"%s\",\"userAction\":\"ADDING_FAMILY_MEMBER\",\"sendSMS\":true}";
        String body=String.format(format,organizerDsid);

        HttpResponse response = ProxyUtil.execute(HttpUtil.createPost("https://setup.icloud.com/setup/mac/family/getiTunesAccountPaymentInfo")
                        .header(headers)
                        .body(body));
        if(200==response.getStatus()){
            String rb = response.charset("UTF-8").body();
            if("0".equals(JSONUtil.parse(rb).getByPath("status",String.class))){
                res.put("msg",JSONUtil.parse(rb).getByPath("status-message"));
            }
        }else if(response.getStatus()==401){
            res.put("code","1");
            res.put("msg","未登录或登录超时");
        }else if(response.getStatus()==422) {
            String rb = response.charset("UTF-8").body();
            res.put("code",JSONUtil.parse(rb).getByPath("status"));
            res.put("msg",JSONUtil.parse(rb).getByPath("status-message"));
        }
        return res;
    }
    public static Map<String,Object> verifyCVV(String auth,String appleId,String creditCardId,String creditCardLastFourDigits,String securityCode,String verificationType,String billingType){
        Map<String,Object> res=new HashMap<>();
        res.put("code",Constant.SUCCESS);
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("setup.icloud.com"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language", ListUtil.toList("zh-cn"));
        headers.put("User-Agent", ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10) AppleWebKit/600.1.3 (KHTML, like Gecko)"));
        headers.put("X-MMe-LoggedIn-AppleID",ListUtil.toList(appleId));
        headers.put("Accept",ListUtil.toList("*/*"));
        headers.put("Referer",ListUtil.toList("https://setup.icloud.com/setup/mac/family/setupFamilyUI"));
        headers.put("X-MMe-Client-Info",ListUtil.toList("<MacBook Pro> <Mac OS X;10.10;14A314h> <com.apple.AOSKit/203 (com.apple.systempreferences/14.0)>"));
        headers.put("Authorization",ListUtil.toList("Basic "+auth));


        Map<String,Object> bodyMap=new HashMap<>();
        bodyMap.put("creditCardId",creditCardId);
        bodyMap.put("creditCardLastFourDigits",creditCardLastFourDigits);
        bodyMap.put("securityCode",securityCode);
        bodyMap.put("verificationType",verificationType);
        bodyMap.put("billingType",billingType);


        HttpResponse response = ProxyUtil.execute(HttpUtil.createPost("https://setup.icloud.com/setup/mac/family/verifyCVV")
                        .header(headers)
                        .body(JSONUtil.toJsonStr(bodyMap)));
        if(200==response.getStatus()){
            String rb = response.charset("UTF-8").body();
            if("0".equals(JSONUtil.parse(rb).getByPath("status",String.class))){
                res.put("msg",JSONUtil.parse(rb).getByPath("status-message"));
            }
        }else if(response.getStatus()==401){
            res.put("code","1");
            res.put("msg","未登录或登录超时");
        }else if(response.getStatus()==422) {
            String rb = response.charset("UTF-8").body();
            res.put("code",JSONUtil.parse(rb).getByPath("status"));
            res.put("msg",JSONUtil.parse(rb).getByPath("status-message"));
        }
        return res;
    }

    public static HttpResponse appleIDrepair(HttpResponse signInRes){
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Accept",ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("Upgrade-Insecure-Requests",ListUtil.toList("1"));

        headers.put("sec-fetch-dest",ListUtil.toList("iframe"));
        headers.put("sec-fetch-mode",ListUtil.toList("navigate"));
        headers.put("sec-fetch-site",ListUtil.toList("same-site"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-user",ListUtil.toList("?1"));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        headers.put("Referer",ListUtil.toList("https://idmsa.apple.com/"));

        HttpResponse res =  ProxyUtil.execute(HttpUtil.createGet(signInRes.header("Location"))
                        .header(headers));
        return res;
    }
    public static HttpResponse appleIDrepairOptions(HttpResponse signInRes,HttpResponse repairRes,String clientId,String sessionId){

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));
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

        headers.put("scnt",ListUtil.toList(repairRes.header("scnt")));
        headers.put("X-Apple-Session-Token",ListUtil.toList(signInRes.header("X-Apple-Repair-Session-Token")));
        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(sessionId));

        String url = "https://appleid.apple.com/account/manage/repair/options";
        HttpResponse res =  ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers)
                        .disableCookie());
        return res;
    }
    public static HttpResponse appleIDUpgrade(HttpResponse signInRes,HttpResponse optionsRes,String clientId,String sessionId){

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));
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

        headers.put("scnt",ListUtil.toList(optionsRes.header("scnt")));
        headers.put("X-Apple-Session-Token",ListUtil.toList(optionsRes.header("X-Apple-Session-Token")));
        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(sessionId));

        String url = "https://appleid.apple.com/account/security/upgrade";
        HttpResponse res =  ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers));
        return res;
    }
    public static HttpResponse appleIDSetuplater(HttpResponse signInRes,HttpResponse optionsRes,HttpResponse upgradeRes,String clientId,String sessionId){

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));
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

        headers.put("scnt",ListUtil.toList(optionsRes.header("scnt")));
        headers.put("X-Apple-Session-Token",ListUtil.toList(upgradeRes.header("X-Apple-Session-Token")));
        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(sessionId));

        String url = "https://appleid.apple.com/account/security/upgrade/setuplater";
        HttpResponse res =  ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers));
        return res;
    }
    public static HttpResponse appleIDrepairOptions2(HttpResponse signInRes,HttpResponse optionsRes, HttpResponse setuplaterRes,String clientId,String sessionId){

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));
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

        headers.put("scnt",ListUtil.toList(optionsRes.header("scnt")));
        headers.put("X-Apple-Session-Token",ListUtil.toList(setuplaterRes.header("X-Apple-Session-Token")));
        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(sessionId));

        String url = "https://appleid.apple.com/account/manage/repair/options";
        HttpResponse res =  ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers)
                        .disableCookie());
        return res;
    }
    public static HttpResponse appleIDrepairComplete(HttpResponse signInRes,HttpResponse options2Res,String clientId,String sessionId){

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));
        //headers.put("X-Apple-Frame-Id",ListUtil.toList(""));
        headers.put("X-Apple-OAuth-Client-Id",ListUtil.toList(clientId));
        //headers.put("X-Apple-OAuth-State",ListUtil.toList(frameId));
        headers.put("X-Apple-OAuth-Require-Grant-Code",ListUtil.toList("true"));
        //headers.put("X-Apple-OAuth-Redirect-URI",ListUtil.toList(signInMap.get("redirectUri")));
        headers.put("X-Apple-OAuth-Response-Type",ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-Response-Mode",ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-Client-Type",ListUtil.toList("firstPartyAuth"));

        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("X-Apple-Offer-Security-Upgrade",ListUtil.toList("1"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        headers.put("Referer",ListUtil.toList("https://appleid.apple.com/"));

        headers.put("scnt",ListUtil.toList(signInRes.header("scnt")));
        headers.put("X-Apple-Repair-Session-Token",ListUtil.toList(options2Res.header("X-Apple-Session-Token")));
        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(signInRes.header("X-Apple-ID-Session-Id")));
        headers.put("X-Apple-Auth-Attributes",ListUtil.toList(signInRes.header("X-Apple-Auth-Attributes")));

        String url = "https://idmsa.apple.com/appleauth/auth/repair/complete";
        HttpResponse res =  ProxyUtil.execute(HttpUtil.createPost(url)
                        .header(headers));
        return res;
    }

    public static HttpResponse auth(HttpResponse signInRes,String frameId,String clientId,String domain){

        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent",ListUtil.toList(Constant.BROWSER_USER_AGENT));
        headers.put("Accept", ListUtil.toList("text/html"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language",ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-origin"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));
        headers.put("Sec-Fetch-User",ListUtil.toList("?1"));

        headers.put("X-Apple-Auth-Attributes",ListUtil.toList(signInRes.header("X-Apple-Auth-Attributes")));
        headers.put("X-Apple-Domain-Id", ListUtil.toList("35"));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(frameId));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));

        headers.put("X-Apple-OAuth-Client-Id",ListUtil.toList(clientId));
        headers.put("X-Apple-OAuth-Client-Type", ListUtil.toList("firstPartyAuth"));
        headers.put("X-Apple-OAuth-Redirect-URI", ListUtil.toList("https://www." + domain));
        headers.put("X-Apple-OAuth-Require-Grant-Code", ListUtil.toList("true"));
        headers.put("X-Apple-OAuth-Response-Mode",ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-Response-Type",ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-State",ListUtil.toList(frameId));

        headers.put("X-Apple-Offer-Security-Upgrade", ListUtil.toList("1"));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));

        headers.put("scnt",ListUtil.toList(signInRes.header("scnt")));
        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(signInRes.header("X-Apple-ID-Session-Id")));

        String url = "https://idmsa.apple.com/appleauth/auth";
        HttpResponse res =  ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers)
                        .cookie(CookieUtils.getCookiesFromHeader(signInRes)));
        return res;
    }

    public static HttpResponse securityCode(Map<String,String> signInMap,HttpResponse authRsp) {
        String clientId = signInMap.get("clientId");
        String frameId  = signInMap.get("frameId");
        String redirectUri   = signInMap.get("redirectUri");
        String securityCode   = signInMap.get("securityCode");

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

        headers.put("X-Apple-App-Id", ListUtil.toList(clientId));
        headers.put("X-Apple-Auth-Attributes",ListUtil.toList(authRsp.header("X-Apple-Auth-Attributes")));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));

        headers.put("X-Apple-Domain-Id", ListUtil.toList("35"));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(frameId));

        headers.put("X-Apple-OAuth-Client-Id",ListUtil.toList(clientId));
        headers.put("X-Apple-OAuth-Client-Type", ListUtil.toList("firstPartyAuth"));
        headers.put("X-Apple-OAuth-Redirect-URI", ListUtil.toList(redirectUri));
        headers.put("X-Apple-OAuth-Require-Grant-Code", ListUtil.toList("true"));
        headers.put("X-Apple-OAuth-Response-Mode",ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-Response-Type",ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-State",ListUtil.toList(frameId));

        headers.put("X-Apple-Offer-Security-Upgrade", ListUtil.toList("1"));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));

        headers.put("Scnt",List.of(authRsp.header("scnt")));
        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(authRsp.header("X-Apple-ID-Session-Id")));

        String scDeviceBody = "{\"securityCode\":{\"code\":\"%s\"}}";
        String scPhoneBody = "{\"phoneNumber\":{\"id\":1},\"securityCode\":{\"code\":\"%s\"},\"mode\":\"sms\"}";

        String url = "";
        String body = "";

        String type = securityCode.split("-")[0];
        String code = securityCode.split("-")[1];

        if ("device".equals(type)) {
            url = "https://idmsa.apple.com/appleauth/auth/verify/trusteddevice/securitycode";
            body = String.format(scDeviceBody, code);
        } else if ("sms".equals(type)) {
            url = "https://idmsa.apple.com/appleauth/auth/verify/phone/securitycode";
            body = String.format(scPhoneBody, code);
        }

        HttpResponse rsp = null;
        if (!"".equals(body)) {
            rsp = ProxyUtil.execute(HttpUtil.createPost(url)
                            .header(headers)
                            .body(body)
                            .disableCookie());
        }
        return rsp;
    }

    public static HttpResponse trust(Map<String, String> signInMap, HttpResponse securityCodeRsp) {
        String clientId = signInMap.get("clientId");
        String frameId  = signInMap.get("frameId");
        String redirectUri   = signInMap.get("redirectUri");
        String securityCode   = signInMap.get("securityCode");

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

        headers.put("X-Apple-App-Id", ListUtil.toList(clientId));
        headers.put("X-Apple-Auth-Attributes",ListUtil.toList(securityCodeRsp.header("X-Apple-Auth-Attributes")));
        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));

        headers.put("X-Apple-Domain-Id", ListUtil.toList("35"));
        headers.put("X-Apple-Frame-Id", ListUtil.toList(frameId));

        headers.put("X-Apple-OAuth-Client-Id",ListUtil.toList(clientId));
        headers.put("X-Apple-OAuth-Client-Type", ListUtil.toList("firstPartyAuth"));
        headers.put("X-Apple-OAuth-Redirect-URI", ListUtil.toList(redirectUri));
        headers.put("X-Apple-OAuth-Require-Grant-Code", ListUtil.toList("true"));
        headers.put("X-Apple-OAuth-Response-Mode",ListUtil.toList("web_message"));
        headers.put("X-Apple-OAuth-Response-Type",ListUtil.toList("code"));
        headers.put("X-Apple-OAuth-State",ListUtil.toList(frameId));

        headers.put("X-Apple-Offer-Security-Upgrade", ListUtil.toList("1"));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));

        headers.put("Scnt",List.of(securityCodeRsp.header("scnt")));
        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(securityCodeRsp.header("X-Apple-ID-Session-Id")));

        return ProxyUtil.execute(HttpUtil.createGet("https://idmsa.apple.com/appleauth/auth/2sv/trust")
                        .header(headers)
                        .cookie(CookieUtils.getCookiesFromHeader(securityCodeRsp)));
    }

    public static HttpResponse accountLogin(HttpResponse singInLocalRes,String domain){
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("text/plain;charset=UTF-8"));
        headers.put("Accept",ListUtil.toList("*/*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-site"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        headers.put("Referer",ListUtil.toList("https://www."+domain+"/"));
        headers.put("Origin",ListUtil.toList("https://www."+domain));


//        String url = "https://setup.icloud.com/setup/ws/1/accountLogin?clientBuildNumber=2404Project58&clientMasteringNumber=2404B21&clientId=f8f619cb-b355-442c-bba0-1a172766b162";
        //String url = "https://setup.icloud.com.cn/setup/ws/1/accountLogin?clientBuildNumber=2404Project58&clientMasteringNumber=2404B21&clientId=74c7c902-3472-4d63-adf8-bb651ec76266";
        String url = "https://setup."+domain+"/setup/ws/1/accountLogin";
        String body = "{\"dsWebAuthToken\":\""+singInLocalRes.header("X-Apple-Session-Token")+"\",\"accountCountryCode\":\"" +singInLocalRes.header("X-Apple-ID-Account-Country")+ "\",\"extended_login\":false}";

        HttpResponse loginRsp =  ProxyUtil.execute(HttpUtil.createPost(url)
                        .header(headers)
                        .body(body));
        return loginRsp;
    }

    public static HttpResponse repairWebICloud(HttpResponse authRsp,String domain) {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("text/plain;charset=UTF-8"));
        headers.put("Accept",ListUtil.toList("*/*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-site"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        headers.put("Referer",ListUtil.toList("https://www."+domain+"/"));
        headers.put("Origin",ListUtil.toList("https://www."+domain));

        JSONObject body = JSONUtil.parseObj(authRsp.body());
        String url = "https://setup."+domain+"/setup/ws/1/getTerms";
        String languageCode = body.getByPath("dsInfo.languageCode", String.class);
        HttpResponse getTermsRsp = ProxyUtil.execute(HttpUtil.createPost(url)
                        .header(headers)
                        .body("{\"locale\":\"" + languageCode + "\"}")
                        .cookie(CookieUtils.getCookiesFromHeader(authRsp)));

        JSONObject termsRspBody = JSONUtil.parseObj(getTermsRsp.body());
        String termsVersion = termsRspBody.getByPath("iCloudTerms.version",String.class);

        String url1 = "https://setup."+domain+"/setup/ws/1/repairDone";
        HttpResponse repairDoneRsp = ProxyUtil.execute(HttpUtil.createPost(url1)
                        .header(headers)
                        .body("{\"acceptedICloudTerms\":"+termsVersion+",\"gcbdPrivacyNoticeAccepted\":true}")
                        .cookie(CookieUtils.getCookiesFromHeader(authRsp)));
        return repairDoneRsp;
    }

    public static HttpResponse emailSuggestions(LoginInfo loginInfo,String pNum,String domain){
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("*/*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-site"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        headers.put("Referer",ListUtil.toList("https://www."+domain+"/"));
        headers.put("Origin",ListUtil.toList("https://www."+domain));
        //headers.put("Host",ListUtil.toList(""));

        String url = "https://p"+pNum+"-mccgateway."+domain+"/mailacct/v1/web/emailSuggestions";
        HttpResponse emailAvailabilityRsp = ProxyUtil.execute(HttpUtil.createGet(url)
                        .header(headers)
                        .cookie(loginInfo.getCookie()));
        return emailAvailabilityRsp;
    }

    public static HttpResponse emailAvailability(LoginInfo loginInfo,String pNum,String domain,String email){
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("*/*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-site"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        headers.put("Referer",ListUtil.toList("https://www."+domain+"/"));
        headers.put("Origin",ListUtil.toList("https://www."+domain));

        String url = "https://p"+pNum+"-mccgateway."+domain+"/mailacct/v1/web/emailAvailability";
        HttpResponse emailAvailabilityRsp = ProxyUtil.execute(HttpUtil.createPost(url)
                        .header(headers)
                        .body("{\"email\":\""+email+"\",\"entryPoint\":\"APP_LIBRARY\"}")
                        .cookie(loginInfo.getCookie()));
        return emailAvailabilityRsp;
    }

    public static HttpResponse activateEmail(LoginInfo loginInfo,String pNum,String domain,String email){
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("*/*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-site"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        headers.put("Referer",ListUtil.toList("https://www."+domain+"/"));
        headers.put("Origin",ListUtil.toList("https://www."+domain));
        //headers.put("Host",ListUtil.toList(""));

        String url = "https://p"+pNum+"-mccgateway."+domain+"/mailacct/v1/web/activateEmail";
        HttpResponse activateEmailRsp = ProxyUtil.execute(HttpUtil.createPost(url)
                        .header(headers)
                        .body("{\"email\":\""+email+"\"}")
                        .cookie(loginInfo.getCookie()));
        return activateEmailRsp;
    }



    public static String getSessionId(HttpResponse res){
        String sessionId = "";
        if(null != res.headers().get("Set-Cookie")) {
            for (String c : res.headers().get("Set-Cookie")) {
                String aidsp = c.substring(0, c.indexOf(";"));
                String[] item = aidsp.split("=");
                if (item.length < 2) {
                    continue;
                }
                if("aidsp".equals(item[0])) {
                    sessionId = aidsp.substring(aidsp.indexOf("=")+1);
                }
            }
        }
        return  sessionId;
    }
    public static String getClientId(){
        String clientId = "";
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Accept",ListUtil.toList("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));


        headers.put("sec-fetch-dest",ListUtil.toList("empty"));
        headers.put("sec-fetch-mode",ListUtil.toList("cors"));
        headers.put("sec-fetch-site",ListUtil.toList("same-site"));
        headers.put("sec-ch-ua",ListUtil.toList("\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\""));
        headers.put("sec-ch-ua-mobile",ListUtil.toList("?0"));
        headers.put("sec-ch-ua-platform",ListUtil.toList("\"macOS\""));

        headers.put("Referer",ListUtil.toList("https://www.icloud.com/"));

        String url = "https://setup.icloud.com/setup/ws/1/validate";

        HttpResponse res =  ProxyUtil.execute(HttpUtil.createPost(url)
                        .header(headers));
        JSONObject jo = JSONUtil.parseObj(res.body());
        String aui = (String)jo.getByPath("configBag.urls.accountAuthorizeUI");
        if(StrUtil.isNotEmpty(aui)){
            clientId = aui.split("=")[1];
        }
        return clientId;
    }



}

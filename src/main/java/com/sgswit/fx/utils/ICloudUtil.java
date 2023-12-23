package com.sgswit.fx.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;

import java.util.*;

/**
 * @author DeZh
 * @title: ICloudUtil
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/1320:38
 */
public class ICloudUtil {
    public static void main(String[] args) throws Exception {
        //HttpResponse response= checkCloudAccount(IdUtil.fastUUID().toUpperCase(),"shabagga222@tutanota.com","Xx97595031.2121" );
//        HttpResponse response= checkCloudAccount(IdUtil.fastUUID().toUpperCase(),"djli0506@163.com","!!B0527s0207!!" );
//        HttpResponse response= checkCloudAccount(DataUtil.getClientIdByAppleId("djli0506@163.com"),"djli0506@163.com","!!B0527s0207!!" );
//        getiTunesAccountPaymentInfo(getAuthByHttResponse(response),"djli0506@163.com","8135448658");
//        verifyCVV(getAuthByHttResponse(response),"djli0506@163.com",null,null,null,null,null);
//        getFamilyDetails(getAuthByHttResponse(response),"djli0506@163.com");
//        createFamily(getAuthByHttResponse(response),"djli0506@163.com","!!B0527s0207!!","djli0506@163.com","!!B0527s0207!!");
//        leaveFamily(getAuthByHttResponse(response),"djli0506@163.com");

        accountLoginDemo();
    }
    public static HttpResponse checkCloudAccount(String clientId, String appleId, String password){
        //clientId从数据库中获取每个appleId生成一个
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Host", ListUtil.toList("setup.icloud.com"));
        headers.put("Referer", ListUtil.toList("https://setup.icloud.com/setup/iosbuddy/loginDelegates"));
        headers.put("Content-Type", ListUtil.toList("application/x-apple-plist; Charset=UTF-8"));
        headers.put("Accept", ListUtil.toList("*/*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.9,en;q=0"));

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

        HttpResponse response = HttpUtil.createGet("https://setup.icloud.com/setup/family/getFamilyDetails")
                .header(headers)
                .execute();

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

        HttpResponse response = HttpUtil.createPost("https://setup.icloud.com/setup/mac/family/createFamily")
                .header(headers)
                .body(JSONUtil.toJsonStr(bodyMap))
                .execute();
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
        HttpResponse response = HttpUtil.createPost("https://setup.icloud.com/setup/mac/family/leaveFamily")
                .header(headers)
                .execute();
        if(200==response.getStatus()){
            String rb = response.charset("UTF-8").body();
            System.out.println(JSONUtil.parse(rb).getByPath("status"));
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
        HttpResponse response = HttpUtil.createPost("https://setup.icloud.com/setup/mac/family/getiTunesAccountPaymentInfo")
                .header(headers)
                .execute();
        if(200==response.getStatus()){
            String rb = response.charset("UTF-8").body();
            System.out.println(JSONUtil.parse(rb).getByPath("status"));
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
//        bodyMap.put("creditCardId",creditCardId);
        bodyMap.put("creditCardId","UPCC");
//        bodyMap.put("creditCardLastFourDigits",creditCardLastFourDigits);
        bodyMap.put("creditCardLastFourDigits","5639");
//        bodyMap.put("securityCode",securityCode);
        bodyMap.put("securityCode","864");
//        bodyMap.put("verificationType",verificationType);
        bodyMap.put("verificationType","CVV");
//        bodyMap.put("billingType",billingType);
        bodyMap.put("billingType","Card");



        HttpResponse response = HttpUtil.createPost("https://setup.icloud.com/setup/mac/family/verifyCVV")
                .header(headers)
                .body(JSONUtil.toJsonStr(bodyMap))
                .execute();
        if(200==response.getStatus()){
            String rb = response.charset("UTF-8").body();
            System.out.println(JSONUtil.parse(rb).getByPath("status"));
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


        //String url = "https://setup.icloud.com/setup/ws/1/accountLogin?clientBuildNumber=2404Project58&clientMasteringNumber=2404B21&clientId=f8f619cb-b355-442c-bba0-1a172766b162";
        String url = "https://setup."+domain+"/setup/ws/1/accountLogin";
        String body = "{\"dsWebAuthToken\":\""+singInLocalRes.header("X-Apple-Session-Token")+"\",\"accountCountryCode\":\"" +singInLocalRes.header("X-Apple-ID-Account-Country")+ "\",\"extended_login\":false}";

        HttpResponse loginRsp =  HttpUtil.createPost(url)
                .header(headers)
                .body(body)
                .execute();

        System.out.println("------------------accountLogin-----------------------------------------------");
        System.out.println(loginRsp.getStatus());
//        System.out.println(loginRsp.headers());
//        System.out.println(loginRsp.body());

        if(loginRsp.getStatus()==302){
            //非 www.icloud.com账户（亦即美国账户），需要到具体国家的icloud域名上获取账户信息
            JSONObject jo = JSONUtil.parseObj(loginRsp.body());
            String iCloudUrl = jo.getStr("domainToUse");
            return accountLogin(singInLocalRes,iCloudUrl.toLowerCase());
        }
        return loginRsp;
    }
    public static HttpResponse appleIDrepair(HttpResponse singInRes){
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

        HttpResponse res =  HttpUtil.createGet(singInRes.header("Location"))
                .header(headers)
                .execute();

        System.out.println("------------------appleIDrepair-----------------------------------------------");
        System.out.println(res.getStatus());
        System.out.println(res.headers());

        System.out.println("------------------appleIDrepair----------------------------------------------");

        return res;
    }
    public static HttpResponse appleIDrepairOptions(HttpResponse singInRes,HttpResponse repairRes,String clientId,String sessionId){

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("X-Apple-OAuth-Context",ListUtil.toList(singInRes.header("X-Apple-OAuth-Context")));
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
        headers.put("X-Apple-Session-Token",ListUtil.toList(singInRes.header("X-Apple-Repair-Session-Token")));
        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(sessionId));

        String url = "https://appleid.apple.com/account/manage/repair/options";
        HttpResponse res =  HttpUtil.createGet(url)
                .header(headers)
                .disableCookie()
                .execute();

        System.out.println("------------------appleIDrepairOptions-----------------------------------------------");
        System.out.println(res.getStatus());
        System.out.println(res.headers());
        System.out.println(res.body());

        System.out.println("------------------appleIDrepairOptions----------------------------------------------");

        return res;
    }
    public static HttpResponse appleIDUpgrade(HttpResponse singInRes,HttpResponse optionsRes,String clientId,String sessionId){

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("X-Apple-OAuth-Context",ListUtil.toList(singInRes.header("X-Apple-OAuth-Context")));
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
        HttpResponse res =  HttpUtil.createGet(url)
                .header(headers)
                .execute();

        System.out.println("------------------account/security/upgrade-----------------------------------------------");
        System.out.println(res.getStatus());
        System.out.println(res.headers());

        System.out.println("------------------account/security/upgrade----------------------------------------------");

        return res;
    }
    public static HttpResponse appleIDSetuplater(HttpResponse singInRes,HttpResponse optionsRes,HttpResponse upgradeRes,String clientId,String sessionId){

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("X-Apple-OAuth-Context",ListUtil.toList(singInRes.header("X-Apple-OAuth-Context")));
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
        HttpResponse res =  HttpUtil.createGet(url)
                .header(headers)
                .execute();

        System.out.println("-----------------/account/security/upgrade/setuplater----------------------------------------------");
        System.out.println(res.getStatus());
        System.out.println(res.headers());

        System.out.println("------------------/account/security/upgrade/setuplater--------------------------------------------");

        return res;
    }
    public static HttpResponse appleIDrepairOptions2(HttpResponse singInRes,HttpResponse optionsRes, HttpResponse setuplaterRes,String clientId,String sessionId){

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("User-Agent", ListUtil.toList(Constant.BROWSER_USER_AGENT));

        headers.put("Content-Type", ListUtil.toList("application/json"));
        headers.put("Accept",ListUtil.toList("application/json, text/javascript, */*; q=0.01"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));

        headers.put("X-Apple-Widget-Key", ListUtil.toList(clientId));
        headers.put("X-Apple-I-FD-Client-Info",ListUtil.toList(Constant.BROWSER_CLIENT_INFO));
        headers.put("X-Requested-With",ListUtil.toList("XMLHttpRequest"));
        headers.put("X-Apple-OAuth-Context",ListUtil.toList(singInRes.header("X-Apple-OAuth-Context")));
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
        HttpResponse res =  HttpUtil.createGet(url)
                .header(headers)
                .disableCookie()
                .execute();

        System.out.println("------------------appleIDrepairOptions 22222222-----------------------------------------------");
        System.out.println(res.getStatus());
        System.out.println(res.headers());
        System.out.println(res.body());

        System.out.println("------------------appleIDrepairOptions 222222222----------------------------------------------");

        return res;
    }
    public static HttpResponse appleIDrepairComplete(HttpResponse singInRes,HttpResponse options2Res,String clientId,String sessionId){

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

        headers.put("scnt",ListUtil.toList(singInRes.header("scnt")));
        headers.put("X-Apple-Repair-Session-Token",ListUtil.toList(options2Res.header("X-Apple-Session-Token")));
        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(singInRes.header("X-Apple-ID-Session-Id")));
        headers.put("X-Apple-Auth-Attributes",ListUtil.toList(singInRes.header("X-Apple-Auth-Attributes")));

        String url = "https://idmsa.apple.com/appleauth/auth/repair/complete";
        HttpResponse res =  HttpUtil.createPost(url)
                .header(headers)
                .execute();

        System.out.println("------------------appleIDrepairComplete-----------------------------------------------");
        System.out.println(res.getStatus());
        System.out.println(res.headers());
        System.out.println(res.body());

        System.out.println("------------------appleIDrepairComplete----------------------------------------------");

        return res;
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
                System.out.println("-------cookies--------" + aidsp);
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

        HttpResponse res =  HttpUtil.createPost(url)
                .header(headers)
                .execute();

        System.out.println("------------------getclient id -----------------------------------------------");
        System.out.println(res.getStatus());
        System.out.println(res.headers());

        JSONObject jo = JSONUtil.parseObj(res.body());
        String aui = (String)jo.getByPath("configBag.urls.accountAuthorizeUI");
        if(StrUtil.isNotEmpty(aui)){
            clientId = aui.split("=")[1];
        }
        System.out.println("==========clientid==========" + clientId);
        System.out.println("------------------getclient id ----------------------------------------------");

        return clientId;
    }

    private static void accountLoginDemo(){
        String redirectUri = "https://www.icloud.com";
        String clientId = getClientId();

        Map<String,String> signInMap = new HashMap<>();
        signInMap.put("clientId",clientId);
        signInMap.put("redirectUri",redirectUri);
        signInMap.put("account","djli0506@163.com");
        signInMap.put("pwd","!!B0527s0207!!");

        //登录 通用 www.icloud.com
        HttpResponse singInRes = ICloudWeblogin.signin(signInMap);
        // 设置账号对应的国家
        String domain = "icloud.com";
        //非双重认证账号，登录后，http code = 412；
        //        此时返回的header中 有 X-Apple-Repair-Session-Token ，无 X-Apple-Session-Token，
        //        需先进行处理后，才能返回X-Apple-Session-Token（为了后续继续使用非双重认证，此时仅过一趟，不做处理）
        // 双重认证账号，登录成功后， http code = 409；
        //        但此时返回的header中包含 X-Apple-Session-Token，可直接使用获取icloud账户信息
        int status = singInRes.getStatus();

        if (status != 412 && status != 409){
            Console.log("status: {}",status);
            return;
        }

        // 412普通登陆, 409双重登陆
        HttpResponse singInLocalRes = singInRes;
        if (status == 412){
            HttpResponse repairRes = appleIDrepair(singInRes);
            //获取session-id，后续操作需基于该id进行处理
            String sessionId = getSessionId(repairRes);
            HttpResponse optionsRes = appleIDrepairOptions(singInRes,repairRes,clientId,sessionId);
            HttpResponse upgradeRes = appleIDUpgrade(singInRes,optionsRes,clientId,sessionId);
            HttpResponse setuplaterRes = appleIDSetuplater(singInRes,optionsRes,upgradeRes,clientId,sessionId);
            HttpResponse options2Res = appleIDrepairOptions2(singInRes,optionsRes,setuplaterRes,clientId,sessionId);
            HttpResponse completeRes = appleIDrepairComplete(singInRes,options2Res,clientId,sessionId);

            //处理后，获取account 信息
            singInLocalRes = completeRes;
        }

        HttpResponse authRsp = null;
        String sessionToken = singInLocalRes.header("X-Apple-Session-Token");
        if (!StrUtil.isEmpty(sessionToken)){
            authRsp = accountLogin(singInLocalRes, domain);
        }

        if (authRsp != null){
            JSONObject jo = JSONUtil.parseObj(authRsp.body());
            System.err.println(jo);
            String icloudMail = jo.getByPath("dsInfo.iCloudAppleIdAlias").toString();
            if(StrUtil.isNotEmpty(icloudMail)){
                //
                System.out.println("-------icloud mail is : " + icloudMail + "-----------");
            }

            JSONObject webservices = (JSONObject) jo.getByPath("webservices");
            if(webservices.containsKey("mail")){
                String mailStatus = webservices.getByPath("mail.status").toString();
                if("active".equals(mailStatus)){
                    System.out.println("------------mail service is actived ---------------");
                }
            }

            Boolean isRepairNeeded = (Boolean)jo.getByPath("isRepairNeeded");
            if(isRepairNeeded){
                System.out.println("---------icloud need to be repired");
            }
            System.out.println("------------------accountLogin----------------------------------------------");
        }
    }

}

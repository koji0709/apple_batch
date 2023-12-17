package com.sgswit.fx.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
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
        HttpResponse response= checkCloudAccount(IdUtil.fastUUID().toUpperCase(),"shabagga222@tutanota.com","Xx97595031.2121" );
//        HttpResponse response= checkCloudAccount(IdUtil.fastUUID().toUpperCase(),"djli0506@163.com","!!B0527s0207!!" );
//        HttpResponse response= checkCloudAccount(DataUtil.getClientIdByAppleId("djli0506@163.com"),"djli0506@163.com","!!B0527s0207!!" );
//        getiTunesAccountPaymentInfo(getAuthByHttResponse(response),"djli0506@163.com","8135448658");
//        verifyCVV(getAuthByHttResponse(response),"djli0506@163.com",null,null,null,null,null);
//        getFamilyDetails(getAuthByHttResponse(response),"djli0506@163.com");
//        createFamily(getAuthByHttResponse(response),"djli0506@163.com","!!B0527s0207!!","djli0506@163.com","!!B0527s0207!!");
//        leaveFamily(getAuthByHttResponse(response),"djli0506@163.com");
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
        res.put("code","200");
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
        res.put("code","200");
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
        res.put("code","200");
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
        res.put("code","200");
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
        res.put("code","200");
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
}

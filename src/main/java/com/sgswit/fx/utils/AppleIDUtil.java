package com.sgswit.fx.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.model.Account;
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
        HashMap<String, List<String>> headers = buildHeader();

        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        String body = "{\"accountName\":\"%s\",\"password\":\"%s\",\"rememberMe\":false,\"trustTokens\":[]}";
        String scBogy = String.format(body, account.getAccount(), account.getPwd());
        HttpResponse res = HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth/signin?isRememberMeEnabled=false&isRememberMeEnabled=false")
                .header(headers)
                .body(scBogy)
                .execute();
        return res;
    }

    public static HttpResponse auth(HttpResponse res1) {
        HashMap<String, List<String>> headers = buildHeader(res1);
        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));
        HttpResponse res2 = HttpUtil.createPost("https://idmsa.apple.com/appleauth/auth")
                .header(headers)
                //.cookie(getCookie(res1))
                .execute();

        return res2;
    }

    public static HttpResponse securityCode(HttpResponse res1, String type, String code) {
        HashMap<String, List<String>> headers = buildHeader(res1);
        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        String scDeviceBody = "{\"securityCode\":{\"code\":\"%s\"}}";
        String scPhoneBody = "{\"phoneNumber\":{\"id\":1},\"securityCode\":{\"code\":\"%s\"},\"mode\":\"sms\"}";

        String scBody = "";
        String scUrl = "";

        if ("device".equals(type)) {
            scBody = String.format(scDeviceBody, code);
            scUrl = "https://idmsa.apple.com/appleauth/auth/verify/trusteddevice/securitycode";
        } else if ("sms".equals(type)) {
            scBody = String.format(scPhoneBody, code);
            scUrl = "https://idmsa.apple.com/appleauth/auth/verify/phone/securitycode";
        }

        HttpResponse res2 = null;
        if (!"".equals(scBody)) {
            res2 = HttpUtil.createPost(scUrl)
                    .header(headers)
                    .body(scBody)
                    .cookie(getCookie(res1))
                    .execute();
        }
        return res2;
    }

    public static HttpResponse token(HttpResponse res2) {
        HashMap<String, List<String>> headers = buildHeader(res2);
        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        HttpResponse res3 = HttpUtil.createGet("https://appleid.apple.com/account/manage/gs/ws/token")
                .header(headers)
                .cookie(getCookie(res2))
                .execute();
        return res3;
    }

    public static HttpResponse questions(HttpResponse res1, Account account) {
        HashMap<String, List<String>> headers = buildHeader(res1);
        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        String content = res1.body();
        String questions = JSONUtil.parseObj(content).getJSONObject("securityQuestions").get("questions").toString();
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

    public static HttpResponse accountRepair(HttpResponse res1) {
        HashMap<String, List<String>> headers = buildHeader(false);
        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("Sec-Fetch-Dest", ListUtil.toList("iframe"));
        headers.put("Sec-Fetch-Mode", ListUtil.toList("navigate"));
        headers.put("navigate", ListUtil.toList("same-site"));

        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));
        headers.remove("Content-Type");
        String location = res1.header("Location");
        HttpResponse res2 = HttpUtil.createGet(location)
                .header(headers)
                //.cookie(getCookie(res1))
                .execute();

//        System.out.println("------------------accountRepair-----------------------------------------------");
//        System.out.println(res2.getStatus());
//        System.out.println(res2.headers());
//        System.out.println(res2.headerList("Set-Cookie"));
//        System.out.println("------------------accountRepair-----------------------------------------------");

        return res2;
    }

    public static HttpResponse repareOptions(HttpResponse step211Res, HttpResponse step212Res) {
        HashMap<String, List<String>> headers = buildHeader(step211Res);
        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        //headers.put("X-Apple-Skip-Repair-Attributes",ListUtil.toList("[\"hsa2_enrollment\"]"));
        headers.put("X-Apple-Skip-Repair-Attributes", ListUtil.toList("[]"));
        headers.put("X-Apple-Session-Token", ListUtil.toList(step211Res.header("X-Apple-Repair-Session-Token")));

        StringBuilder cookieBuilder = new StringBuilder();
        List<String> res211Cookies = step211Res.headerList("Set-Cookie");
        for (String item : res211Cookies) {
            cookieBuilder.append(";").append(item);
        }

        List<String> res212Cookies = step212Res.headerList("Set-Cookie");
        for (String item : res212Cookies) {
            cookieBuilder.append(";").append(item);
        }

        //System.out.println(cookieBuilder.toString());
        String scUrl = "https://appleid.apple.com/account/manage/repair/options";
        HttpResponse res2 = HttpUtil.createGet(scUrl)
                .header(headers)
                //.cookie(cookieBuilder.toString())
                .execute();

//        System.out.println("------------------repareOptions-----------------------------------------------");
//        System.out.println(res2.getStatus());
//        System.out.println(res2.headers());
//        System.out.println(res2.headerList("Set-Cookie"));
//        System.out.println("------------------repareOptions-----------------------------------------------");
        return res2;
    }

    public static HttpResponse securityUpgrade(HttpResponse res1, String XAppleIDSessionId, String scnt) {
        HashMap<String, List<String>> headers = buildHeader();
        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));

        headers.put("X-Apple-ID-Session-Id", ListUtil.toList(XAppleIDSessionId));
        headers.put("scnt", ListUtil.toList(scnt));
        headers.put("X-Apple-Session-Token", ListUtil.toList(res1.header("X-Apple-Session-Token")));
        headers.put("X-Apple-Skip-Repair-Attributes", ListUtil.toList("[]"));

        String scUrl = "https://appleid.apple.com/account/security/upgrade";
        HttpResponse res2 = HttpUtil.createGet(scUrl)
                .header(headers)
                //.cookie(getCookie(res1))
                .execute();

//        System.out.println("------------------securityUpgrade-----------------------------------------------");
//        System.out.println(res2.getStatus());
//        System.out.println(res2.headers());
//        System.out.println(res2.headerList("Set-Cookie"));
//        System.out.println("------------------securityUpgrade-----------------------------------------------");
        return res2;
    }

    public static HttpResponse securityUpgradeSetuplater(HttpResponse res1, String XAppleIDSessionId, String scnt) {
        HashMap<String, List<String>> headers = buildHeader();

        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));

        headers.put("X-Apple-ID-Session-Id", ListUtil.toList(XAppleIDSessionId));
        headers.put("scnt", ListUtil.toList(scnt));
        headers.put("X-Apple-Session-Token", ListUtil.toList(res1.header("X-Apple-Session-Token")));
        headers.put("X-Apple-Skip-Repair-Attributes", ListUtil.toList("[]"));

        String scUrl = "https://appleid.apple.com/account/security/upgrade/setuplater";
        HttpResponse res2 = HttpUtil.createGet(scUrl)
                .header(headers)
                //.cookie(getCookie(res1))
                .execute();

//        System.out.println("------------------securityUpgradeSetuplater-----------------------------------------------");
//        System.out.println(res2.getStatus());
//        System.out.println(res2.headers());
//        System.out.println(res2.headerList("Set-Cookie"));
//        System.out.println("------------------securityUpgradeSetuplater-----------------------------------------------");
        return res2;
    }

    public static HttpResponse repareOptionsSecond(HttpResponse res1, String XAppleIDSessionId, String scnt) {
        HashMap<String, List<String>> headers = buildHeader();

        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));
        headers.put("Accept-Language", ListUtil.toList("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2"));

        headers.put("X-Apple-ID-Session-Id", ListUtil.toList(XAppleIDSessionId));
        headers.put("scnt", ListUtil.toList(scnt));
        headers.put("X-Apple-Session-Token", ListUtil.toList(res1.header("X-Apple-Session-Token")));
        headers.put("X-Apple-Skip-Repair-Attributes", ListUtil.toList("[\"hsa2_enrollment\"]"));

        String scUrl = "https://appleid.apple.com/account/manage/repair/options";
        HttpResponse res2 = HttpUtil.createGet(scUrl)
                .header(headers)
                //.cookie(getCookie(res1))
                .execute();

//        System.out.println("------------------repair/options -----------------------------------------------");
//        System.out.println(res2.getStatus());
//        System.out.println(res2.headers());
//        System.out.println(res2.headerList("Set-Cookie"));
//        System.out.println("------------------repair/options -----------------------------------------------");
        return res2;
    }

    public static HttpResponse repareComplete(HttpResponse res1, HttpResponse step211Res) {
        HashMap<String, List<String>> headers = buildHeader(step211Res);
        headers.put("Host", ListUtil.toList("idmsa.apple.com"));
        headers.put("Origin", ListUtil.toList("https://idmsa.apple.com"));
        headers.put("Referer", ListUtil.toList("https://idmsa.apple.com/"));

        headers.put("X-Apple-Repair-Session-Token", ListUtil.toList(res1.header("X-Apple-Session-Token")));

        String scUrl = "https://idmsa.apple.com/appleauth/auth/repair/complete";
        HttpResponse res2 = HttpUtil.createPost(scUrl)
                .header(headers)
                //.cookie(getCookie(res1))
                .execute();

//        System.out.println("------------------repareComplete-----------------------------------------------");
//        System.out.println(res2.getStatus());
//        System.out.println(res2.headers());
//        System.out.println(res2.headerList("Set-Cookie"));
//        System.out.println("------------------repareComplete-----------------------------------------------");
        return res2;
    }

    /**
     * 获取账户信息
     */
    public static HttpResponse account(HttpResponse res3) {
        HashMap<String, List<String>> headers = buildHeader(false, res3);
        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        HttpResponse res4 = HttpUtil.createGet("https://appleid.apple.com/account/manage")
                .header(headers)
                //.cookie(getCookie(res3))
                .execute();
        return res4;
    }

    /**
     * 修改用户生日信息
     * @param birthday 生日 yyyy-MM-dd
     */
    public static HttpResponse updateBirthday(String tokenScnt, String birthday) {
        String url = "https://appleid.apple.com/account/manage/security/birthday";
        HashMap<String, List<String>> headers = buildHeader();
        headers.put("scnt", ListUtil.toList(tokenScnt));

        String[] birthdayArr = birthday.split("-");
        String format = "{\"dayOfMonth\":\"%s\",\"monthOfYear\":\"%s\",\"year\":\"%s\"}";
        String body = String.format(format, birthdayArr[2], birthdayArr[1], birthdayArr[0]);

        return HttpUtil.createRequest(Method.PUT, url)
                .body(body)
                .header(headers)
                .execute();
    }

    /**
     * 移除救援邮箱
     */
    public static HttpResponse deleteRescueEmail(String tokenScnt,String password) {
        String url = "https://appleid.apple.com/account/manage/security/email/rescue";
        HashMap<String, List<String>> headers = buildHeader();
        headers.put("scnt", ListUtil.toList(tokenScnt));
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
            verifyPassword(rsp,password);
            return deleteRescueEmail(tokenScnt,password);
        }

        return rsp;
    }

    /**
     * 修改名称
     */
    public static HttpResponse updateName(String tokenScnt,String password,String firstName,String lastName) {
        String url = "https://appleid.apple.com/account/manage/name";
        String body = String.format("{\"firstName\":\"%s\",\"middleName\":\"\",\"lastName\":\"%s\"}",firstName,lastName);

        HashMap<String, List<String>> headers = buildHeader();
        headers.put("scnt", ListUtil.toList(tokenScnt));

        HttpResponse rsp = HttpUtil.createRequest(Method.PUT, url)
                .header(headers)
                .body(body)
                .execute();

        int status = rsp.getStatus();
        rspLog(Method.PUT,url,status);

        // 需要验证密码
        if (status == 451){
            verifyPassword(rsp,password);
            return updateName(tokenScnt,password,firstName,lastName);
        }
        return rsp;
    }

    /**
     * 修改密码
     */
    public static HttpResponse updatePassword(String tokenScnt,String password,String newPassword){
        String url = "https://appleid.apple.com/account/manage/security/password";
        String body = String.format("{\"currentPassword\":\"%s\",\"newPassword\":\"%s\"}",password,newPassword);

        HashMap<String, List<String>> headers = buildHeader();
        headers.put("scnt", ListUtil.toList(tokenScnt));

        HttpResponse rsp = HttpUtil.createRequest(Method.PUT, url)
                .header(headers)
                .body(body)
                .execute();

        int status = rsp.getStatus();
        rspLog(Method.PUT,url,status);

        return rsp;
    }

    /**
     * 修改密保
     */
    public static HttpResponse updateQuestions(String tokenScnt,String password,String body){
        String url = "https://appleid.apple.com/account/manage/security/questions";

        HashMap<String, List<String>> headers = buildHeader();
        headers.put("scnt", ListUtil.toList(tokenScnt));

        HttpResponse rsp = HttpUtil.createRequest(Method.PUT, url)
                .header(headers)
                .body(body)
                .execute();

        int status = rsp.getStatus();
        rspLog(Method.PUT,url,status);

        // 需要验证密码
        if (status == 451){
            verifyPassword(rsp,password);
            return updateQuestions(tokenScnt,password,body);
        }
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
    public static HttpResponse getDeviceList(){
        String url = "https://appleid.apple.com/account/manage/security/devices";
        HashMap<String, List<String>> headers = buildHeader();
        HttpResponse rsp = HttpUtil.createGet(url)
                .header(headers)
                .execute();
        rspLog(Method.GET,url,rsp.getStatus());
        return rsp;
    }

    /**
     * 移除所有设备
     */
    public static void removeDevices(){
        HttpResponse deviceListRsp = getDeviceList();
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
    public static HttpResponse updateAppleIdSendVerifyCode(String tokenScnt,String password,String appleId){
        String url = "https://appleid.apple.com/account/manage/appleid/verification";
        HashMap<String, List<String>> header = buildHeader();
        header.put("scnt",List.of(tokenScnt));
        String body = "{\"name\":\""+appleId+"\"}";
        HttpResponse verifyRsp = HttpUtil.createPost(url)
                .header(header)
                .body(body).execute();
        rspLog(Method.POST,url,verifyRsp.getStatus());

        int status = verifyRsp.getStatus();

        // 需要验证密码
        if (status == 451){
            verifyPassword(verifyRsp,password);
            return updateAppleIdSendVerifyCode(tokenScnt,password,appleId);
        }
        return verifyRsp;
    }

    /**
     * 修改appleId
     */
    public static HttpResponse updateAppleId(HttpResponse rsp,String appleId,String verifyId,String verifyCode){
        String url = "https://appleid.apple.com/account/manage/appleid/verification";
        String body = "{\"name\":\""+appleId+"\",\"verificationInfo\":{\"id\":\""+verifyId+"\",\"answer\":\""+verifyCode+"\"}}";
        HttpResponse updateAppleIdRsp = HttpUtil.createRequest(Method.PUT,url)
                .header(rsp.headers())
                .body(body)
                .execute();
        rspLog(Method.PUT,url,updateAppleIdRsp.getStatus());
        return updateAppleIdRsp;
    }

    /**
     * 双重认证发送短信
     * @param body {"acceptedWarnings":[],"phoneNumberVerification":{"phoneNumber":{"countryCode":"CN","number":"17608177103","countryDialCode":"86","nonFTEU":true},"mode":"sms"}}
     */
    public static HttpResponse securityUpgradeVerifyPhone(String tokenScnt,String password,String body){
        String url = "https://appleid.apple.com/account/security/upgrade/verify/phone";
        HashMap<String, List<String>> header = buildHeader();
        header.put("scnt",List.of(tokenScnt));

        HttpResponse securityUpgradeVerifyPhoneRsp = HttpUtil.createRequest(Method.PUT, url)
                .header(header)
                .body(body)
                .execute();
        int status = securityUpgradeVerifyPhoneRsp.getStatus();
        rspLog(Method.PUT,url, status);

        // 需要验证密码
        if (status == 451){
            verifyPassword(securityUpgradeVerifyPhoneRsp,password);
            return securityUpgradeVerifyPhone(tokenScnt,password,body);
        }

        return securityUpgradeVerifyPhoneRsp;
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
     * 关闭双重认证
     * @return httpstatus == 302 代表成功
     */
    public static HttpResponse securityDowngrade(String key,String newPassword){
        String base = "https://iforgot.apple.com";
        HttpResponse rsp = HttpUtil.createGet(base + "/withdraw?key=" + key)
                .execute();
        if (rsp.getStatus() == 302){
            HttpResponse unenrollmentRsp = HttpUtil.createPost(base + rsp.header("Location")).execute();
            if (unenrollmentRsp.getStatus() == 302){
                HttpResponse unenrollmentResetRsp = HttpUtil.createPost(base + unenrollmentRsp.header("Location"))
                        .header(unenrollmentRsp.headers())
                        .cookie(unenrollmentRsp.getCookies())
                        .body("{\"password\":\""+newPassword+"\"}")
                        .execute();
                return unenrollmentResetRsp;
            }
        }
        return null;
    }

    /**
     * 密保关闭双重认证
     */
    public static HttpResponse securityDowngrade(HttpResponse verifyAppleIdRsp,Account account,String newPwd) {
        String host = "https://iforgot.apple.com";
        String verifyPhone1Location = verifyAppleIdRsp.header("Location");

        HttpResponse verifyPhone1Rsp = HttpUtil.createGet(host + verifyPhone1Location)
                .header(buildHeader())
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
                .header(buildHeader())
                .execute();

        DateTime birthday = DateUtil.parse(account.getBirthday());

        HttpResponse verifyBirthday2Rsp = HttpUtil.createPost(host + "/unenrollment/verify/birthday")
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
        HttpResponse verifyQuestions2Rsp = HttpUtil.createPost(host + "/unenrollment/verify/questions")
                .header(verifyQuestions1Rsp.headers())
                .header("Content-Type","application/json")
                //.header("sstt",verifyQuestions1BodyJSON.getByPath("sstt",String.class))
                .body(JSONUtil.toJsonStr(bodyMap))
                .execute();

        String unenrollment1Location = verifyQuestions2Rsp.header("Location");
        HttpResponse unenrollment1Rsp = HttpUtil.createGet(host + unenrollment1Location)
                .header(buildHeader())
                .execute();

        HttpResponse unenrollment2Rsp = HttpUtil.createPost(host + "/unenrollment")
                .header(unenrollment1Rsp.headers())
                .execute();

        String unenrollmentReset1Location = unenrollment2Rsp.header("Location");
        HttpResponse unenrollmentReset1Rsp = HttpUtil.createGet(host + unenrollmentReset1Location)
                .header(buildHeader())
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
    public static HttpResponse supportPin(String tokenScnt){
        String url = "https://appleid.apple.com/account/manage/supportpin";
        HttpResponse supportPinRsp = HttpUtil.createPost(url)
                .header("scnt", tokenScnt)
                .execute();
        rspLog(Method.POST,url,supportPinRsp.getStatus());
        return supportPinRsp;
    }

    /**
     * 收款方式列表
     */
    public static HttpResponse paymentList(String tokenScnt){
        String url = "https://appleid.apple.com/account/manage/payment";
        HashMap<String, List<String>> header = buildHeader();
        header.put("scnt",List.of(tokenScnt));

        HttpResponse paymentRsp = HttpUtil.createGet(url)
                .header(header)
                .execute();
        rspLog(Method.GET,url,paymentRsp.getStatus());
        return paymentRsp;
    }

    /**
     * 获取验证码
     */
    public static HttpResponse captcha(){
        String url = "https://iforgot.apple.com/captcha?captchaType=IMAGE";
        return HttpUtil.createGet(url)
                .header(buildHeader())
                .execute();
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
     * 检查appleid是通过怎样的方式去校验(密保/邮件/短信)
     */
    public static HttpResponse verifyAppleIdByPwdProtection(HttpResponse verifyAppleIdRsp) {
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

        HttpResponse verifyBirthday2Rsp = HttpUtil.createPost(host + "/password/verify/birthday")
                .header(verifyBirthday1Rsp.headers())
                .header("Content-Type","application/json")
                .body("{\"monthOfYear\":\"08\",\"dayOfMonth\":\"10\",\"year\":\"1996\"}")
                .execute();

        String verifyQuestions1Location = verifyBirthday2Rsp.header("Location");
        HttpResponse verifyQuestions1Rsp = HttpUtil.createGet(host + verifyQuestions1Location)
                .header(buildHeader())
                .execute();

        JSON verifyQuestions1BodyJSON = JSONUtil.parse(verifyQuestions1Rsp.body());
        List<JSONObject> questions = verifyQuestions1BodyJSON.getByPath("questions",List.class);
        Map<Integer,String> answerMap = new HashMap<>(){{
            put(1,"猪");
            put(2,"狗");
            put(3,"牛");
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
                //.header("sstt",verifyQuestions1BodyJSON.getByPath("sstt",String.class))
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
                .body("{\"password\":\"Xx97595031.2\"}")
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

    private static String getCookie(HttpResponse resp) {
        StringBuilder cookieBuilder = new StringBuilder();
        List<String> res1Cookies = resp.headerList("Set-Cookie");
        if (res1Cookies != null) {
            for (String item : res1Cookies) {
                cookieBuilder.append(";").append(item);
            }
        }
        return cookieBuilder.toString();
    }

    private static void rspLog(Method method,String url,Integer status){
        Console.log("[{}] {}  Response status: {}",method.name(),url,status);
    }
}

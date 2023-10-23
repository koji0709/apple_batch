package com.sgswit.fx;

import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.base.TableView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;

import java.util.*;

/**
 * AppleId测试类
 */
public class AppleIDTest {


    public static void main(String[] args) {
        //securityCodeLoginDemo();
        //passwordProtectionDemo();
        notLoginDemo();
    }

    // 双重认证登陆
    public static void securityCodeLoginDemo(){
        Console.log("请输入账号密码（账号-密码）：");
        String[] input = Console.input().split("-");

        Account account = new Account();
        account.setAccount(input[0]);
        account.setPwd(input[1]);

        // SignIn
        HttpResponse signInRsp = AppleIDUtil.signin(account);
        Console.log("SignInRsp status:{}",signInRsp.getStatus());

        // Auth
        HttpResponse authRsp = AppleIDUtil.auth(signInRsp);
        Console.log("AuthRsp status:{}",authRsp.getStatus());

        String authType = JSONUtil.parse(signInRsp.body()).getByPath("authType",String.class);
        if (!"hsa2".equals(authType)) {
            Console.error("仅支持双重验证逻辑");
            return;
        }

        // SecurityCode
        // type = device,sms
        Console.log("请输入双重认证代码（type-code）：");
        String[] code = Console.input().split("-");
        HttpResponse securityCodeRsp = AppleIDUtil.securityCode(authRsp, code[0], code[1]);
        Console.log("SecurityCodeRsp status:{}",securityCodeRsp.getStatus());

        // Token
        HttpResponse tokenRsp = AppleIDUtil.token(securityCodeRsp);
        Console.log("TokenRsp status:{} , Cookie:{}",tokenRsp.getStatus(),tokenRsp.getCookieStr());

        // Account
//        HttpResponse accountRsp = AppleIDUtil.account(tokenRsp);
//        JSON accountJSON = JSONUtil.parse(accountRsp.body());
//        String fullName = accountJSON.getByPath("name.fullName",String.class);
//        Console.log("accountRsp status:{} fullName:{}",accountRsp.getStatus(),fullName);

        String sessionId = tokenRsp.header("X-Apple-ID-Session-Id");
        String scnt      = tokenRsp.header("scnt");

        // 修改用户生日信息
//        HttpResponse updateBirthdayRsp = AppleIDUtil.updateBirthday(scnt,"1996-08-10");
//        Console.log("UpdateBirthdayRsp status:{} body:{}",updateBirthdayRsp.getStatus(),updateBirthdayRsp.body());

//        HttpResponse deleteRescueEmailRsp = AppleIDUtil.deleteRescueEmail(scnt,"");
//        Console.log("deleteRescueEmailRsp status:{}",deleteRescueEmailRsp.getStatus());

//        HttpResponse supportPinRsp = AppleIDUtil.supportPin(scnt);
//        Console.log("supportPinRsp Body: {}",supportPinRsp.body());
//        if (supportPinRsp.getStatus() == 200){
//            JSON parse = JSONUtil.parse(supportPinRsp.body());
//            String pin = parse.getByPath("pin", String.class);
//            System.err.println(pin);
//        }

//        HttpResponse httpResponse = AppleIDUtil.paymentList(scnt);
//        System.err.println(httpResponse.body());
    }

    // 密保登陆
    public static void passwordProtectionDemo() {
        Console.log("请输入账号密码（账号-密码-密保答案1-密保答案2-密保问题3）：");
        // 3631408@qq.com-blbgkKP52-朋友-工作-父母
        //List<String> input = Arrays.asList(Console.input().split("-"));
        List<String> input = Arrays.asList("shabagga222@tutanota.com","Xx97595031.","猪","狗","牛");

        Account account = new Account();
        account.setAccount(input.get(0));
        account.setPwd(input.get(1));
        account.setAnswer1(input.get(2));
        account.setAnswer2(input.get(3));
        account.setAnswer3(input.get(4));

        TableView tableView = new TableView();
        String tokenScnt = tableView.getTokenScnt(account);

        // 查询账户信息
//        HttpResponse accountRsp = AppleIDUtil.account(tokenRsp);
//        JSON accountJSON = JSONUtil.parse(accountRsp.body());
//        String fullName = accountJSON.getByPath("name.fullName",String.class);
//        Console.log("accountRsp status:{} fullName:{}",accountRsp.getStatus(),fullName);


        // 修改用户生日信息
//        HttpResponse updateBirthdayRsp = AppleIDUtil.updateBirthday(tokenRsp.header("scnt")
//                ,"1996-08-11");
//        Console.log("UpdateBirthdayRsp status:{} body:{}",updateBirthdayRsp.getStatus(),updateBirthdayRsp.body());

//        HttpResponse deleteRescueEmailRsp = AppleIDUtil.deleteRescueEmail(tokenRsp.header("scnt"),"blbgkKP52");
//        Console.log("deleteRescueEmailRsp status:{}",deleteRescueEmailRsp.getStatus());

//         修改姓氏
//        AppleIDUtil.updateName(tokenScnt,"blbgkKP52","洋","伍");
        
        // 修改密码
        // AppleIDUtil.updatePassword(tokenScnt,"blbgkKP52","--");

        // 修改密保
//        String body = "{\"questions\":[{\"answer\":\"朋友\",\"id\":\"130\",\"question\":\"你少年时代最好的朋友叫什么名字？\"},{\"answer\":\"工作\",\"id\":\"136\",\"question\":\"你的理想工作是什么？\"},{\"answer\":\"父母\",\"id\":\"142\",\"question\":\"你的父母是在哪里认识的？\"}]}";
//        AppleIDUtil.updateQuestions(tokenScnt,"blbgkKP52",body);

        // 删除所有设备
        //AppleIDUtil.removeDevices();

//        String appleId = "3631408@qq.com";
//        // 修改appleId
//        HttpResponse verifyRsp = AppleIDUtil.updateAppleIdSendVerifyCode(tokenScnt, account.getPwd(), appleId);
//
//        String verifyId = JSONUtil.parse(verifyRsp.body()).getByPath("verificationId",String.class);
//        Console.log("请输入验证码：");
//        String verifyCode = Console.input().trim();
//
//        // http status 302 200 都是成功
//        HttpResponse updateAppleIdRsp = AppleIDUtil.updateAppleId(verifyRsp, appleId, verifyId, verifyCode);

//        String phone = "17608177103";
//        String body = "{\"acceptedWarnings\":[],\"phoneNumberVerification\":{\"phoneNumber\":{\"countryCode\":\"CN\",\"number\":\""+phone+"\",\"countryDialCode\":\"86\",\"nonFTEU\":true},\"mode\":\"sms\"}}";
//        HttpResponse securityUpgradeVerifyPhoneRsp = AppleIDUtil.securityUpgradeVerifyPhone(tokenScnt, account.getPwd(), body);
//
//        Console.log("请输入验证码：");
//        String verifyCode = Console.input().trim();
//
//        // todo 要获取手机相关信息
//        JSON jsonBody = JSONUtil.parse(securityUpgradeVerifyPhoneRsp.body());
//        String body2 = "{\"phoneNumberVerification\":{\"phoneNumber\":{\"id\":20101,\"number\":\""+phone+"\",\"countryCode\":\"CN\",\"nonFTEU\":true},\"securityCode\":{\"code\":\""+verifyCode+"\"},\"mode\":\"sms\"}}";
//        HttpResponse securityUpgradeRsp = AppleIDUtil.securityUpgrade(securityUpgradeVerifyPhoneRsp, body2);

//        HttpResponse supportPinRsp = AppleIDUtil.supportPin(tokenScnt);
//        Console.log("supportPinRsp Body: {}",supportPinRsp.body());
//        if (supportPinRsp.getStatus() == 200){
//            JSON parse = JSONUtil.parse(supportPinRsp.body());
//            String pin = parse.getByPath("pin", String.class);
//            System.err.println(pin);
//        }

    }

    // 不需要登陆(重置密码/关闭双重认证)
    public static void notLoginDemo(){
        // 获取验证码
        HttpResponse captchaRsp = AppleIDUtil.captcha();
        JSON captchaRspJSON = JSONUtil.parse(captchaRsp.body());
        String capContent = captchaRspJSON.getByPath("payload.content", String.class);
        System.err.println("base64编码:");
        System.err.println(capContent);

        Console.log("请输入验证码:");
        String  captAnswer   = Console.input();
        Integer captId    = captchaRspJSON.getByPath("id", Integer.class);
        String  captToken = captchaRspJSON.getByPath("token", String.class);

        // 校验appleId
        Account account = new Account();
        account.setAccount("shabagga222@tutanota.com");
        account.setAnswer1("猪");
        account.setAnswer2("狗");
        account.setAnswer3("牛");
        account.setBirthday("1996-08-10");
        account.setPwd("Xx97595031.21222");//新密码
        //account.setName(Console.input());
        String verifyAppleIdBody = "{\"id\":\"%s\",\"captcha\":{\"id\":%d,\"answer\":\"%s\",\"token\":\"%s\"}}";
        verifyAppleIdBody = String.format(verifyAppleIdBody,account.getAccount(),captId,captAnswer,captToken);
        HttpResponse verifyAppleIdRsp = AppleIDUtil.verifyAppleId(verifyAppleIdBody);

        HttpResponse securityDowngradeRsp = AppleIDUtil.securityDowngrade(verifyAppleIdRsp,account);
        Console.log("Security Downgrade: " + securityDowngradeRsp.getStatus());

        //HttpResponse verifyAppleIdRsp2 = AppleIDUtil.verifyAppleIdByPwdProtection(verifyAppleIdRsp);
        //Console.log("Password Reset: " + JSONUtil.parse(verifyAppleIdRsp2.body()).getByPath("resetCompleted"));


    }

}

package com.sgswit.fx;

import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;

import java.util.*;

/**
 * AppleId测试类
 */
public class AppleIDTest {


    public static void main(String[] args) {
        //securityCodeLoginDemo();
        passwordProtectionDemo();
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
        HttpResponse accountRsp = AppleIDUtil.account(tokenRsp);
        JSON accountJSON = JSONUtil.parse(accountRsp.body());
        String fullName = accountJSON.getByPath("name.fullName",String.class);
        Console.log("accountRsp status:{} fullName:{}",accountRsp.getStatus(),fullName);

        // todo 可以简单理解为token? 那密保认证又是怎样的?
        String sessionId = tokenRsp.header("X-Apple-ID-Session-Id");
        String scnt      = tokenRsp.header("scnt");

        // 修改用户生日信息
        HttpResponse updateBirthdayRsp = AppleIDUtil.updateBirthday(scnt,"1996-08-10");
        Console.log("UpdateBirthdayRsp status:{} body:{}",updateBirthdayRsp.getStatus(),updateBirthdayRsp.body());

        HttpResponse deleteRescueEmailRsp = AppleIDUtil.deleteRescueEmail(scnt,"");
        Console.log("deleteRescueEmailRsp status:{}",deleteRescueEmailRsp.getStatus());
    }

    // 密保登陆
    public static void passwordProtectionDemo() {
        Console.log("请输入账号密码（账号-密码-密保答案1-密保答案2-密保问题3）：");
        // wuyang0001@2980.com-blbgkKP52-朋友-工作-父母
        //List<String> input = Arrays.asList(Console.input().split("-"));
        List<String> input = Arrays.asList("wuyang0001@2980.com","blbgkKP52","朋友","工作","父母");

        Account account = new Account();
        account.setAccount(input.get(0));
        account.setPwd(input.get(1));
        account.setAnswer1(input.get(2));
        account.setAnswer2(input.get(3));
        account.setAnswer3(input.get(4));

        // SignIn
        HttpResponse signInRsp = AppleIDUtil.signin(account);
        Console.log("SignInRsp status:{}",signInRsp.getStatus());

        // Auth
        HttpResponse authRsp = AppleIDUtil.auth(signInRsp);
        Console.log("AuthRsp status:{}",authRsp.getStatus());

        String authType = JSONUtil.parse(signInRsp.body()).getByPath("authType",String.class);
        if (!"sa".equals(authType)) {
            Console.error("仅支持密保验证逻辑");
        }

        // 密保认证
        HttpResponse questionRsp = AppleIDUtil.questions(authRsp, account);
        if (questionRsp.getStatus() != 412) {
            return;
        }
        HttpResponse accountRepairRsp = AppleIDUtil.accountRepair(questionRsp);
        String XAppleIDSessionId = "";
        String scnt = accountRepairRsp.header("scnt");
        List<String> cookies = accountRepairRsp.headerList("Set-Cookie");
        for (String item : cookies) {
            if (item.startsWith("aidsp")) {
                XAppleIDSessionId = item.substring(item.indexOf("aidsp=") + 6, item.indexOf("; Domain=appleid.apple.com"));
            }
        }
        HttpResponse repareOptionsRsp = AppleIDUtil.repareOptions(questionRsp, accountRepairRsp);
        Console.log("repareOptionsRsp status:{}",signInRsp.getStatus());

        HttpResponse securityUpgradeRsp = AppleIDUtil.securityUpgrade(repareOptionsRsp, XAppleIDSessionId, scnt);
        Console.log("securityUpgradeRsp status:{}",signInRsp.getStatus());

        HttpResponse securityUpgradeSetuplaterRsp = AppleIDUtil.securityUpgradeSetuplater(securityUpgradeRsp, XAppleIDSessionId, scnt);
        Console.log("securityUpgradeSetuplaterRsp status:{}",signInRsp.getStatus());

        HttpResponse repareOptionsSecondRsp = AppleIDUtil.repareOptionsSecond(securityUpgradeSetuplaterRsp, XAppleIDSessionId, scnt);
        Console.log("repareOptionsSecondRsp status:{}",signInRsp.getStatus());

        HttpResponse repareCompleteRsp  = AppleIDUtil.repareComplete(repareOptionsSecondRsp, questionRsp);
        Console.log("repareCompleteRsp status:{}",signInRsp.getStatus());

        HttpResponse tokenRsp   = AppleIDUtil.token(repareCompleteRsp);
        Console.log("tokenRsp status:{}",tokenRsp.getStatus());

        String tokenScnt = tokenRsp.header("scnt");

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
        AppleIDUtil.updateName(tokenScnt,"blbgkKP52","洋","伍");
        
        // 修改密码
        // AppleIDUtil.updatePassword(tokenScnt,"blbgkKP52","--");

        // 修改密保
//        String body = "{\"questions\":[{\"answer\":\"朋友啊\",\"id\":\"130\",\"question\":\"你少年时代最好的朋友叫什么名字？\"},{\"answer\":\"工作啊\",\"id\":\"136\",\"question\":\"你的理想工作是什么？\"},{\"answer\":\"父母啊\",\"id\":\"142\",\"question\":\"你的父母是在哪里认识的？\"}]}";
//        AppleIDUtil.updateQuestions(tokenScnt,"blbgkKP52",body);

        // 删除所有设备
        //AppleIDUtil.removeDevices();

    }


}

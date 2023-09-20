package com.sgswit.fx;

import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;

/**
 * AppleId测试类
 */
public class AppleIDTest {


    public static void main(String[] args) {
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
            Console.error("不支持非双重验证逻辑");
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
        HttpResponse updateBirthdayRsp = AppleIDUtil.updateBirthday(sessionId,scnt,"1996-08-10");
        Console.log("UpdateBirthdayRsp status:{} body:{}",updateBirthdayRsp.getStatus(),updateBirthdayRsp.body());

    }

}

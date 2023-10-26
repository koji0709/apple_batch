package com.sgswit.fx.controller.base;

import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;

import java.util.List;

/**
 *
 */
public class AppleIdView extends TableView {

    /**
     * appleid官网登陆
     */
    public HttpResponse login(Account account){
        // SignIn
        HttpResponse signInRsp = AppleIDUtil.signin(account);

        // Auth
        HttpResponse authRsp = AppleIDUtil.auth(signInRsp);

        String authType = JSONUtil.parse(signInRsp.body()).getByPath("authType",String.class);
        if (!"sa".equals(authType)) {
            Console.error("仅支持密保验证逻辑");
            account.setNote("仅支持密保验证逻辑");
            refresh();
            return null;
        }

        // 密保认证
        HttpResponse questionRsp = AppleIDUtil.questions(authRsp, account);
        if (questionRsp.getStatus() != 412) {
            Console.error("密保认证异常！");
            account.setNote("密保认证异常");
            refresh();
            return null;
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

        HttpResponse securityUpgradeRsp = AppleIDUtil.securityUpgrade(repareOptionsRsp, XAppleIDSessionId, scnt);

        HttpResponse securityUpgradeSetuplaterRsp = AppleIDUtil.securityUpgradeSetuplater(securityUpgradeRsp, XAppleIDSessionId, scnt);

        HttpResponse repareOptionsSecondRsp = AppleIDUtil.repareOptionsSecond(securityUpgradeSetuplaterRsp, XAppleIDSessionId, scnt);

        HttpResponse repareCompleteRsp  = AppleIDUtil.repareComplete(repareOptionsSecondRsp, questionRsp);

        HttpResponse tokenRsp   = AppleIDUtil.token(repareCompleteRsp);
        if (tokenRsp.getStatus() != 200){
            account.setNote("登陆异常");
            refresh();
            return null;
        }
        return tokenRsp;
    }

    public String getTokenScnt(Account account){
        HttpResponse tokenRsp = login(account);
        if (tokenRsp == null){
            account.setNote("登陆失败");
            refresh();
            return null;
        }
        String tokenScnt = tokenRsp.header("scnt");
        return tokenScnt;
    }
}

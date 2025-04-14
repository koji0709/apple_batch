package com.sgswit.fx.controller.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.operation.AccountInfoModifyController;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.scene.input.ContextMenuEvent;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AppleIdView extends CustomTableView<Account> {


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }

    /**
     * appleid官网登录(不区分登录方式)
     */
    public void login(Account account) {
        if (account.isLogin()) {
            return;
        }

        HttpResponse securityCodeOrReparCompleteRsp = null;

        // 代表属于验证码再次执行
        if (!StrUtil.isEmpty(account.getSecurityCode())) {
            Map<String, Object> authData = account.getAuthData();
            HttpResponse authRsp = (HttpResponse) authData.get("authRsp");
            if (authRsp == null) {
                throw new ServiceException("请先执行程序;");
            }
            // todo 双重认证调整
            securityCodeOrReparCompleteRsp = AppleIDUtil.securityCode(account, authRsp);
            checkAndThrowUnavailableException(securityCodeOrReparCompleteRsp);
            return;
        }


        // 走密保流程
        setAndRefreshNote(account, "正在验证账号密码...");

        // SignIn
        HttpResponse signInRsp = AppleIDUtil.signin(account);
        String status = "正常";
        String code = "";
        String authType = "";
        if (!StrUtil.isEmpty(signInRsp.body())) {
            authType = JSONUtil.parse(signInRsp.body()).getByPath("authType", String.class);
            JSONArray errorArr = JSONUtil.parseObj(signInRsp.body())
                    .getByPath("serviceErrors", JSONArray.class);
            if (errorArr != null && errorArr.size() > 0) {
                JSONObject err = (JSONObject) (errorArr.get(0));
                if ("-20209".equals(err.getStr("code"))) {
                    status = "锁定";
                    code = "-1";
                }
            }
            account.setStatus(status);
        }

        // 如果账号未设置生日和密保
        if ("non-sa".equals(authType)) {
            if (!(this instanceof AccountInfoModifyController)){
                throw new ServiceException("该账号未补全认证信息");
            }

            account.setNote("查询账号认证信息..");
            HttpResponse accountRepairRsp = AppleIDUtil.accountRepair(account, signInRsp);
            String XAppleIDSessionId = "";

            for (String item : accountRepairRsp.headerList("Set-Cookie")) {
                if (item.startsWith("aidsp")) {
                    XAppleIDSessionId = item.substring(item.indexOf("aidsp=") + 6, item.indexOf("; Domain=appleid.apple.com"));
                }
            }

            HttpResponse repareOptions2Rsp = AppleIDUtil.repareOptions2(account, signInRsp, XAppleIDSessionId);
            Integer type = checkRepareType(repareOptions2Rsp);
            if (type == -1) {
                throw new ServiceException("查询账号认证信息失败,请重试");
            }

            AccountInfoModifyController _this = (AccountInfoModifyController) this;
            Map<String, String> repareInfoMap = _this.getRepareInfo();

            // 设置生日
            if (type == 1) {
                String birthday = repareInfoMap.get("birthday");
                if (StrUtil.isEmpty(birthday)) {
                    throw new ServiceException("该账号需要补全生日信息,请设置生日");
                }

                account.setNote("开始补全生日信息..");
                HttpResponse repareBirthdayRsp = AppleIDUtil.repareBirthday(account, signInRsp, XAppleIDSessionId,birthday);
                if (repareBirthdayRsp == null || repareBirthdayRsp.getStatus() != 200){
                    throw new ServiceException("补全生日信息失败");
                }

                account.setNote("补全生日信息成功..");
                account.setBirthday(birthday);
                repareOptions2Rsp = AppleIDUtil.repareOptions2(account, signInRsp, XAppleIDSessionId);
                type = checkRepareType(repareOptions2Rsp);
            }

            // 设置密保
            if (type == 2) {
                String body = repareInfoMap.get("ppBody");
                if (StrUtil.isEmpty(body)) {
                    throw new ServiceException("该账号需要补全密保信息,请配置密保");
                }

                account.setNote("开始补全密保信息..");
                HttpResponse repareQuestionsRsp = AppleIDUtil.repareQuestions(account, signInRsp, XAppleIDSessionId,body);
                if (repareQuestionsRsp == null || repareQuestionsRsp.getStatus() != 200){
                    throw new ServiceException("补全密保信息失败");
                }
                account.setAnswer1(repareInfoMap.get("answer1"));
                account.setAnswer2(repareInfoMap.get("answer2"));
                account.setAnswer3(repareInfoMap.get("answer3"));

                repareOptions2Rsp = AppleIDUtil.repareOptions2(account, signInRsp, XAppleIDSessionId);
                type = checkRepareType(repareOptions2Rsp);
            }

            if (type == 0) {
                account.setNote("补全账号认证信息成功, 将进行重新登录");
                // 重新登录
                account.clearLoginInfo();
                login(account);
                return;
            }

        } else {
            if (signInRsp.getStatus() != 409) {
                String failMessage = AppleIDUtil.getValidationErrors(signInRsp, "登录失败，响应状态：" + signInRsp.getStatus());
                if ("-1".equals(code)) {
                    failMessage = "此账号已被锁定";
                }
                throw new ServiceException(failMessage);
            }
            // Auth
            HttpResponse authRsp = AppleIDUtil.auth(account, signInRsp);
            checkAndThrowUnavailableException(authRsp);

            // 双重认证
            if ("hsa2".equals(authType)) {
                account.getAuthData().put("authRsp", authRsp);
                throw new ServiceException("此账号已开启双重认证;");
            } else { // sa 密保认证
                if (StrUtil.isEmpty(account.getAnswer1()) || StrUtil.isEmpty(account.getAnswer2()) || StrUtil.isEmpty(account.getAnswer3())) {
                    throw new ServiceException("密保认证必须输入密保问题;");
                }
                // 密保认证
                setAndRefreshNote(account, "正在验证密保问题...");
                HttpResponse questionRsp = AppleIDUtil.questions(account, authRsp);
                if (questionRsp.getStatus() != 412 && questionRsp.getStatus() != 204) {
                    throw new ServiceException("密保问题验证失败;");
                }

                setAndRefreshNote(account, "密保问题验证通过");
                ThreadUtil.sleep(500);
                if (questionRsp.getStatus() == 204){
                    securityCodeOrReparCompleteRsp = questionRsp;
                }else if (questionRsp.getStatus() == 412){
                    setAndRefreshNote(account, "正在获取协议...");
                    HttpResponse accountRepairRsp = AppleIDUtil.accountRepair(account, questionRsp);
                    if (200 != accountRepairRsp.getStatus()) {
                        String message = AppleIDUtil.getValidationErrors(accountRepairRsp, "获取阅读协议失败");
                        throw new ServiceException(message);
                    }

                    String XAppleIDSessionId = "";
                    String scnt = accountRepairRsp.header("scnt");

                    for (String item : accountRepairRsp.headerList("Set-Cookie")) {
                        if (item.startsWith("aidsp")) {
                            XAppleIDSessionId = item.substring(item.indexOf("aidsp=") + 6, item.indexOf("; Domain=appleid.apple.com"));
                        }
                    }
                    setAndRefreshNote(account, "正在同意协议...");
                    HttpResponse repareOptionsRsp = AppleIDUtil.repareOptions(account, questionRsp, accountRepairRsp);
                    checkAndThrowUnavailableException(repareOptionsRsp);

                    HttpResponse securityUpgradeRsp = AppleIDUtil.securityUpgrade(account, repareOptionsRsp, XAppleIDSessionId, scnt);
                    checkAndThrowUnavailableException(securityUpgradeRsp);

                    HttpResponse securityUpgradeSetuplaterRsp = AppleIDUtil.securityUpgradeSetuplater(account, securityUpgradeRsp, XAppleIDSessionId, scnt);
                    checkAndThrowUnavailableException(securityUpgradeSetuplaterRsp);

                    HttpResponse repareOptionsSecondRsp = AppleIDUtil.repareOptionsSecond(account, securityUpgradeSetuplaterRsp, XAppleIDSessionId, scnt);
                    checkAndThrowUnavailableException(repareOptionsSecondRsp);

                    securityCodeOrReparCompleteRsp = AppleIDUtil.repareComplete(account, repareOptionsSecondRsp, questionRsp);
                    checkAndThrowUnavailableException(securityCodeOrReparCompleteRsp);
                }
            }
        }

        HttpResponse tokenRsp = AppleIDUtil.token(account, securityCodeOrReparCompleteRsp);
        checkAndThrowUnavailableException(tokenRsp);
        if (tokenRsp.getStatus() != 200) {
            throw new PointDeduException(FunctionListEnum.ACCOUNT_INFO_MODIFY_INFOERR.getCode(), "登录异常;");
        }
        setAndRefreshNote(account, "登录成功;");
        account.setIsLogin(true);
    }

    public Integer checkRepareType(HttpResponse repareOptions2Rsp) {
        if (repareOptions2Rsp == null || repareOptions2Rsp.getStatus() != 200
                || StrUtil.isEmpty(repareOptions2Rsp.body())) {
            return -1;
        }
        JSON body = JSONUtil.parse(repareOptions2Rsp.body());

        String type = body.getByPath("type", String.class);
        if (!"basic".equals(type)) {
            return 0;
        }

        List<String> repairMissingData = body.getByPath("repairMissingData", List.class);
        if (CollUtil.isNotEmpty(repairMissingData)) {
            if (repairMissingData.contains("birthday")) {
                return 1;
            }
        }

        String repairItem = body.getByPath("repairItem", String.class);
        if ("questions".equals(repairItem)) {
            return 2;
        }

        return -1;
    }

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        onContentMenuClick(contextMenuEvent, new ArrayList<>(menuItem));
    }

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent, List<String> menuItemList) {
        super.onContentMenuClick(contextMenuEvent, accountTableView, menuItemList, new ArrayList<>());
    }


}

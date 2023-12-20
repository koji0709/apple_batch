package com.sgswit.fx.controller.operation;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.operation.viewData.UnlockChangePasswordView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * 账号解锁改密controller
 */
public class UnlockChangePasswordController extends UnlockChangePasswordView {

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction() {
        openImportAccountView(List.of("account----answer1-answer2-answer3-birthday"));
    }

    @Override
    public boolean executeButtonActionBefore() {
        String newPassword = pwdTextField.getText();
        if (StrUtil.isEmpty(newPassword)){
            alert("必须填写新密码！");
            return false;
        }
        return true;
    }

    @Override
    public void accountHandler(Account account) {
        String newPassword = pwdTextField.getText();
        // 识别验证码
        HttpResponse verifyAppleIdRsp = AppleIDUtil.captchaAndVerify(account.getAccount());
        if (verifyAppleIdRsp.getStatus() == 503){
            setAndRefreshNote(account,"操作频繁");
            return;
        }
        if (verifyAppleIdRsp.getStatus() != 302) {
            setAndRefreshNote(account,"验证码自动识别失败");
            return;
        }

        // 修改密码 (如果账号被锁定,则解锁改密)
        HttpResponse updatePwdByProtectionRsp = AppleIDUtil.updatePwdByProtection(verifyAppleIdRsp, account, account.getPwd());
        boolean unlock = verifyAppleIdRsp.header("Location").startsWith("/password/authenticationmethod");
        if ((unlock && updatePwdByProtectionRsp.getStatus() == 206) || (!unlock && updatePwdByProtectionRsp.getStatus() == 260)){
            account.setPwd(newPassword);
            setAndRefreshNote(account,"解锁改密成功");
        }else{
            String failMessage = hasFailMessage(updatePwdByProtectionRsp) ? failMessage(updatePwdByProtectionRsp) : "解锁改密失败";
            setAndRefreshNote(account,failMessage);
        }
    }

    public boolean hasFailMessage(HttpResponse rsp) {
        String body = rsp.body();
        if (StrUtil.isEmpty(body) || JSONUtil.isTypeJSON(body)){
            return false;
        }
        Object hasError = JSONUtil.parseObj(body).getByPath("hasError");
        return null != hasError && (boolean) hasError;
    }

    public String failMessage(HttpResponse rsp) {
        String message = "";
        Object service_errors = JSONUtil.parseObj(rsp.body()).getByPath("service_errors");
        for (Object o : JSONUtil.parseArray(service_errors)) {
            JSONObject jsonObject = (JSONObject) o;
            message += jsonObject.getByPath("message") + ";";
        }
        return message;
    }
}

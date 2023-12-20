package com.sgswit.fx.controller.operation;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import com.sgswit.fx.controller.operation.viewData.SecurityDowngradeView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * 关闭双重认证controller
 */
public class SecurityDowngradeController extends SecurityDowngradeView {

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction() {
        openImportAccountView(List.of("account----pwd-answer1-answer2-answer3-birthday"));
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

        // 关闭双重认证
        HttpResponse securityDowngradeRsp = AppleIDUtil.securityDowngrade(verifyAppleIdRsp,account,newPassword);
        if (securityDowngradeRsp != null){
            if (securityDowngradeRsp.getStatus() == 302){
                account.setPwd(newPassword);
                setAndRefreshNote(account,"关闭双重验证成功");
            }else{
                // todo 看是否能够获取到失败原因
                setAndRefreshNote(account,"关闭双重验证失败");
            }
        }
    }
}

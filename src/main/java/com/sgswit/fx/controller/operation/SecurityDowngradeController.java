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
        openImportAccountView("account----pwd-answer1-answer2-answer3-birthday");
    }

    /**
     * 开始执行按钮点击
     */
    public void executeButtonAction(){
        // 校验
        if (accountList.isEmpty()){
            alert("请先导入账号！");
            return;
        }

        String newPassword = pwdTextField.getText();
        if (StrUtil.isEmpty(newPassword)){
            alert("必须填写新密码！");
            return;
        }
        List<Account> recordList = new ArrayList<>();
        for (Account account : accountList) {
            // 检测账号是否被处理过
            boolean processed = isProcessed(account);
            if (processed){
                continue;
            }

            setAndRefreshNote(account,"执行中");

            // 识别验证码
            HttpResponse verifyAppleIdRsp = AppleIDUtil.captchaAndVerify(account.getAccount());
            if (verifyAppleIdRsp.getStatus() != 302) {
                setAndRefreshNote(account,"验证码自动识别失败");
                continue;
            }

            // 关闭双重认证
            HttpResponse securityDowngradeRsp = AppleIDUtil.securityDowngrade(verifyAppleIdRsp,account,newPassword);
            if (securityDowngradeRsp != null){
                if (securityDowngradeRsp.getStatus() == 302){
                    account.setPwd(newPassword);
                    setAndRefreshNote(account,"关闭双重验证成功");
                }else{
                    setAndRefreshNote(account,"关闭双重验证失败");
                }
            }
            recordList.add(account);
        }
        insertLocalHistory(recordList);

    }
}

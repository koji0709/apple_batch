package com.sgswit.fx.controller.operation;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import com.sgswit.fx.controller.operation.viewData.UnlockChangePasswordView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;


/**
 * 账号解锁改密controller
 */
public class UnlockChangePasswordController extends UnlockChangePasswordView {

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction() {
        openImportAccountView("account----answer1-answer2-answer3-birthday");
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

        for (Account account : accountList) {
            // 检测账号是否被处理过
            boolean processed = isProcessed(account);
            if (processed){
                continue;
            }

            // 识别验证码
            HttpResponse verifyAppleIdRsp = AppleIDUtil.captchaAndVerify(account.getAccount());
            if (verifyAppleIdRsp.getStatus() == 503){
                account.setNote("操作频繁");
                continue;
            }
            if (verifyAppleIdRsp.getStatus() != 302) {
                account.setNote("验证码自动识别失败");
                continue;
            }

            // 修改密码 (如果账号被锁定,则解锁改密)
            HttpResponse updatePwdByProtectionRsp = AppleIDUtil.updatePwdByProtection(verifyAppleIdRsp, account, account.getPwd());
            boolean unlock = verifyAppleIdRsp.header("Location").startsWith("/password/authenticationmethod");
            if ((unlock && updatePwdByProtectionRsp.getStatus() == 206) || (!unlock && updatePwdByProtectionRsp.getStatus() == 260)){
                account.setNote("解锁改密成功");
                account.setPwd(newPassword);
            }else{
                String failMessage = hasFailMessage(updatePwdByProtectionRsp) ? failMessage(updatePwdByProtectionRsp) : "解锁改密失败";
                account.setNote(failMessage);
            }
        }
    }
}

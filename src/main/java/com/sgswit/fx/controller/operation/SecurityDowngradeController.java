package com.sgswit.fx.controller.operation;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import com.sgswit.fx.controller.operation.viewData.SecurityDowngradeView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;

import java.net.URL;
import java.util.ResourceBundle;


/**
 * 关闭双重认证controller
 */
public class SecurityDowngradeController extends SecurityDowngradeView {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
    }

    /**
     * shabagga222@tutanota.com----猪-狗-牛-19960810
     */
    @Override
    public void importAccountButtonAction() {
        super.importAccountButtonAction("account----pwd-answer1-answer2-answer3-birthday");
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
            if (verifyAppleIdRsp.getStatus() != 302) {
                account.setNote("验证码自动识别失败");
                continue;
            }

            // 关闭双重认证
            HttpResponse securityDowngradeRsp = AppleIDUtil.securityDowngrade(verifyAppleIdRsp,account,newPassword);
            if (securityDowngradeRsp != null){
                if (securityDowngradeRsp.getStatus() == 302){
                    account.setNote("关闭双重验证成功");
                    account.setPwd(newPassword);
                }else{
                    account.setNote("关闭双重验证失败");
                }
            }
        }
        this.refreshTableView();
    }
}

package com.sgswit.fx.controller.operation;

import cn.hutool.core.util.StrUtil;
import com.sgswit.fx.controller.operation.viewData.UnlockChangePasswordView;
import com.sgswit.fx.utils.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;


/**
 * 账号解锁改密controller
 */
public class UnlockChangePasswordController extends UnlockChangePasswordView {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
    }

    @Override
    public void importAccountButtonAction() {
        super.importAccountButtonAction("account-pwd-answer1-answer2-answer3-birthday");
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

        // todo 讯果不需要验证码 我们需要?

        accountTableView.refresh();
    }
}

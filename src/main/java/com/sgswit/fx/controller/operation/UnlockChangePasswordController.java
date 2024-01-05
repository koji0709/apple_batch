package com.sgswit.fx.controller.operation;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.operation.viewData.UnlockChangePasswordView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.scene.input.ContextMenuEvent;

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
        HttpResponse verifyAppleIdRsp = AppleIDUtil.captchaAndVerify(account);
        if (verifyAppleIdRsp.getStatus() == 503){
            setAndRefreshNote(account,"操作频繁");
            return;
        }
        if (verifyAppleIdRsp.getStatus() != 302) {
            setAndRefreshNote(account,"验证码自动识别失败");
            return;
        }

        // 修改密码 (如果账号被锁定,则解锁改密)
        HttpResponse updatePwdByProtectionRsp = AppleIDUtil.updatePwdByProtection(verifyAppleIdRsp, account, newPassword);
        boolean unlock = verifyAppleIdRsp.header("Location").startsWith("/password/authenticationmethod");
        if ((unlock && updatePwdByProtectionRsp.getStatus() == 206) || (!unlock && updatePwdByProtectionRsp.getStatus() == 260)){
            account.setPwd(newPassword);
            setAndRefreshNote(account,"解锁改密成功");
        }else{
            String failMessage = hasFailMessage(updatePwdByProtectionRsp) ? failMessage(updatePwdByProtectionRsp) : "解锁改密失败";
            setAndRefreshNote(account,failMessage);
        }
    }

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> menuItem =new ArrayList<>(){{
            add(Constant.RightContextMenu.DELETE.getCode());
            add(Constant.RightContextMenu.REEXECUTE.getCode());
            add(Constant.RightContextMenu.COPY.getCode());
        }};
        super.onContentMenuClick(contextMenuEvent,accountTableView,menuItem,new ArrayList<>());
    }

    @Override
    protected void reExecute(Account account) {
        accountHandlerExpand(account);
    }

}

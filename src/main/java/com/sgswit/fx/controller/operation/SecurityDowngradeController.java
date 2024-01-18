package com.sgswit.fx.controller.operation;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.operation.viewData.SecurityDowngradeView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.PointUtil;
import javafx.scene.input.ContextMenuEvent;

import java.net.URL;
import java.util.*;


/**
 * 关闭双重认证controller
 */
public class SecurityDowngradeController extends SecurityDowngradeView {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.SECURITY_DOWNGRADE.getCode())));
        super.initialize(url, resourceBundle);
    }
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
        setAndRefreshNote(account,"开始获取验证码..");
        HttpResponse verifyAppleIdRsp = AppleIDUtil.captchaAndVerify(account);
        if (verifyAppleIdRsp.getStatus() == 503){
            throw new ServiceException("操作频繁，请稍后重试！");
        }
        if (verifyAppleIdRsp.getStatus() != 302) {
            throw new ServiceException("验证码自动识别失败");
        }

        setAndRefreshNote(account,"验证码自动校验完毕");

        // 关闭双重认证
        setAndRefreshNote(account,"开始进入关闭双重认证流程...");
        HttpResponse securityDowngradeRsp = AppleIDUtil.securityDowngrade(verifyAppleIdRsp,account,newPassword);
        if (securityDowngradeRsp == null){
            setAndRefreshNote(account,account.getNote());
            return;
        }

        if (securityDowngradeRsp.getStatus() != 302){
            throw new ServiceException("关闭双重验证失败");
        }

        account.setPwd(newPassword);
        setAndRefreshNote(account,"关闭双重验证成功");
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

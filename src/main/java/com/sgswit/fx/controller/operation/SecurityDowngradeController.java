package com.sgswit.fx.controller.operation;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.operation.viewData.SecurityDowngradeView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.scene.control.TableView;
import javafx.scene.input.ContextMenuEvent;

import java.net.URL;
import java.util.*;


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
        setAndRefreshNote(account,"开始获取验证码..",false);
        HttpResponse verifyAppleIdRsp = AppleIDUtil.captchaAndVerify(account);
        if (verifyAppleIdRsp.getStatus() == 503){
            setAndRefreshNote(account,"操作频繁");
            return;
        }
        if (verifyAppleIdRsp.getStatus() != 302) {
            setAndRefreshNote(account,"验证码自动识别失败");
            return;
        }
        setAndRefreshNote(account,"验证码自动校验完毕",false);

        // 关闭双重认证
        setAndRefreshNote(account,"开始进入关闭双重认证流程...",false);
        HttpResponse securityDowngradeRsp = AppleIDUtil.securityDowngrade(verifyAppleIdRsp,account,newPassword);
        if (securityDowngradeRsp == null){
            setAndRefreshNote(account,account.getNote());
            return;
        }

        if (securityDowngradeRsp.getStatus() == 302){
            account.setPwd(newPassword);
            setAndRefreshNote(account,"关闭双重验证成功");
        }else{
            setAndRefreshNote(account,"关闭双重验证失败");
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
        ThreadUtil.execute(() -> {
            try {
                setAndRefreshNote(account, "执行中", false);
                accountHandler(account);
            } catch (ServiceException e) {
                // 异常不做处理只是做一个停止程序作用
            } catch (Exception e) {
                setAndRefreshNote(account, "数据处理异常", true);
                e.printStackTrace();
            }
        });
    }


}

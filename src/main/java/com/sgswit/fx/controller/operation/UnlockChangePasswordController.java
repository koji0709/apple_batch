package com.sgswit.fx.controller.operation;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.exception.ServiceException;
import com.sgswit.fx.controller.operation.viewData.UnlockChangePasswordView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.*;
import com.sgswit.fx.utils.proxy.ProxyUtil;
import javafx.event.ActionEvent;
import javafx.scene.input.ContextMenuEvent;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * 账号解锁改密controller
 */
public class UnlockChangePasswordController extends UnlockChangePasswordView {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.UNLOCK_CHANGE_PASSWORD.getCode())));
        super.initialize(url, resourceBundle);

        String pwdText= PropertiesUtil.getOtherConfig("txtRecoveryPwd","");
        pwdTextField.setText(pwdText);
    }
    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction(ActionEvent actionEvent) {
        openImportAccountView(List.of("account----answer1-answer2-answer3-birthday"),actionEvent);
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
        String url = "https://iforgot.apple.com/password/verify/appleid?language=zh_CN";
        HttpResponse verifyAppleIdInitRsp= ProxyUtil.execute(
                HttpUtil.createGet(url)
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Accept-Language", "zh-CN,zh;q=0.9")
                        .header("Accept", "*")
                        .header("Host", "iforgot.apple.com")
                        .header("User-Agent", Constant.BROWSER_USER_AGENT)
        );
        String boot_args= StrUtils.getScriptById(verifyAppleIdInitRsp.body(),"boot_args");
        String sstt= JSONUtil.parse(boot_args).getByPath("sstt", String.class);
        try {
            sstt = URLEncoder.encode(sstt, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        account.setSstt(sstt);
        CookieUtils.setCookiesToMap(verifyAppleIdInitRsp,account.getCookieMap());
        // 识别验证码
        HttpResponse verifyAppleIdRsp = AppleIDUtil.captchaAndVerifyPost(account);
        if (verifyAppleIdRsp.getStatus() != 302) {
            throw new ServiceException("验证码自动识别失败");
        }
        setAndRefreshNote(account,"账户验证成功...");
        // 修改密码 (如果账号被锁定,则解锁改密)
        HttpResponse updatePwdByProtectionRsp = AppleIDUtil.updatePwdByProtection(verifyAppleIdRsp, account, newPassword);
        if (updatePwdByProtectionRsp.getStatus() == 260){
            account.setPwd(newPassword);
            setAndRefreshNote(account,"解锁改密成功");
        }else{
            String failMessage = hasFailMessage(updatePwdByProtectionRsp) ? failMessage(updatePwdByProtectionRsp) : "解锁改密失败";
            throw new ServiceException(failMessage);
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
    public void closeStageActionBefore() {
        String pwdText= pwdTextField.getText();
        PropertiesUtil.setOtherConfig("txtRecoveryPwd",pwdText);
    }
}

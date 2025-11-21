package com.sgswit.fx.controller.operation;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.exception.ServiceException;
import com.sgswit.fx.controller.operation.viewData.SecurityDowngradeView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AccountImportUtil;
import com.sgswit.fx.utils.PointUtil;
import com.sgswit.fx.utils.StrUtils;
import com.sgswit.fx.utils.proxy.ProxyUtil;
import com.sgswit.fx.utils.web.AppleIDUtil;
import javafx.event.ActionEvent;
import javafx.scene.input.ContextMenuEvent;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;


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
        //判断数据是否完善
        if(StrUtil.isBlankIfStr(account.getAccount())){
            throw new ServiceException("ID不能为空");
        }else if(StrUtil.isBlankIfStr(account.getAnswer1())){
            throw new ServiceException("问题1不能为空");
        }else if(StrUtil.isBlankIfStr(account.getAnswer1())){
            throw new ServiceException("问题2不能为空");
        }else if(StrUtil.isBlankIfStr(account.getAnswer1())){
            throw new ServiceException("问题3不能为空");
        }else if(StrUtil.isBlankIfStr(account.getBirthday())){
            throw new ServiceException("生日不能为空");
        }
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
        account.updateLoginInfo(verifyAppleIdInitRsp);
        // 识别验证码
        setAndRefreshNote(account,"开始获取验证码..");
        account.setSstt(sstt);
        HttpResponse verifyAppleIdRsp = AppleIDUtil.captchaAndVerifyPost(account);
        if (verifyAppleIdRsp.getStatus() != 302) {
            throw new ServiceException("验证码自动识别失败");
        }
        setAndRefreshNote(account,"验证码自动校验完毕");
        // 关闭双重认证
        AppleIDUtil.securityDowngrade(verifyAppleIdRsp,account,newPassword,sstt);
        super.accountTableView.refresh();
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
    public List<Account> parseAccount(String accountStr){
        List<Account>accountArrayList =new ArrayList<>();
        String[] accList = accountStr.split("\n");
        if (accList.length == 0){
            return new ArrayList<>();
        }
        for (String acc : accList){
            Account account= new Account();
            Map<String, String> map=AccountImportUtil.AccountParser.parseAccountToMap(acc);
            account.setAccount(map.get("username"));
            account.setPwd(map.get("password"));
            account.setBirthday(map.get("birthDate"));
            account.setAnswer1(map.get("securityQuestion1"));
            account.setAnswer2(map.get("securityQuestion2"));
            account.setAnswer3(map.get("securityQuestion3"));
            accountArrayList.add(account);
        }
        return accountArrayList;
    }
}

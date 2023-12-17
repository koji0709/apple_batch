package com.sgswit.fx.controller.common;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.SecuritycodePopupController;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AppleIdView extends CustomTableView<Account> {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }

    /**
     * appleid官网登录(不区分登录方式)
     */
    public HttpResponse login(Account account){
        // SignIn
        HttpResponse signInRsp = AppleIDUtil.signin(account);
        if(signInRsp.getStatus()!=409){
            setAndRefreshNote(account,"请检查用户名密码是否正确");
            return null;
        }

        // Auth
        HttpResponse authRsp = AppleIDUtil.auth(signInRsp);
        String authType = JSONUtil.parse(signInRsp.body()).getByPath("authType",String.class);

        // 双重认证
        if ("hsa2".equals(authType)) {
            return hsa2Login(account,authRsp);
        } else { // sa 密保认证
            if (StrUtil.isEmpty(account.getAnswer1()) || StrUtil.isEmpty(account.getAnswer2()) || StrUtil.isEmpty(account.getAnswer3())){
                setAndRefreshNote(account,"密保认证必须输入密保问题");
                return null;
            }
            return saLogin(account,authRsp);
        }
    }

    /**
     * appleid官网登录(固定登陆方式,如果账号authtype不匹配报错)
     */
    public HttpResponse login(Account account,String at){
        // SignIn
        HttpResponse signInRsp = AppleIDUtil.signin(account);
        if(signInRsp.getStatus()!=409){
            setAndRefreshNote(account,"请检查用户名密码是否正确");
            return null;
        }

        // Auth
        HttpResponse authRsp = AppleIDUtil.auth(signInRsp);
        String authType = JSONUtil.parse(signInRsp.body()).getByPath("authType",String.class);

        // 检查账号的登陆方式与该功能是否匹配
        if (!authType.equals(at)){
            String note = "hsa2".equals(authType) ? "此账号已开启双重认证。" : "此账号未开启双重认证";
            setAndRefreshNote(account,note);
            return null;
        }

        // 双重认证
        if ("hsa2".equals(authType)) {
            return hsa2Login(account,authRsp);
        } else { // sa 密保认证
            if (StrUtil.isEmpty(account.getAnswer1()) || StrUtil.isEmpty(account.getAnswer2()) || StrUtil.isEmpty(account.getAnswer3())){
                setAndRefreshNote(account,"密保认证必须输入密保问题");
                return null;
            }
            return saLogin(account,authRsp);
        }
    }

    /**
     * 双重验证登陆
     */
    public HttpResponse hsa2Login(Account account,HttpResponse authRsp){
        String typeCode = this.openSecurityCodePopupView(account);
        if (StrUtil.isEmpty(typeCode)){
            setAndRefreshNote(account,"未输入验证码");
            return null;
        }
        String[] code = typeCode.split("-");
        HttpResponse securityCodeRsp = AppleIDUtil.securityCode(authRsp, code[0], code[1]);

        // Token
        HttpResponse tokenRsp = AppleIDUtil.token(securityCodeRsp);
        return tokenRsp;
    }

    /**
     * 密保登陆
     */
    public HttpResponse saLogin(Account account,HttpResponse authRsp){
        // 密保认证
        HttpResponse questionRsp = AppleIDUtil.questions(authRsp, account);
        if (questionRsp.getStatus() != 412) {
            setAndRefreshNote(account,"密保问题验证失败");
            return null;
        }

        HttpResponse accountRepairRsp = AppleIDUtil.accountRepair(questionRsp);
        String XAppleIDSessionId = "";
        String scnt = accountRepairRsp.header("scnt");
        List<String> cookies = accountRepairRsp.headerList("Set-Cookie");
        for (String item : cookies) {
            if (item.startsWith("aidsp")) {
                XAppleIDSessionId = item.substring(item.indexOf("aidsp=") + 6, item.indexOf("; Domain=appleid.apple.com"));
            }
        }
        HttpResponse repareOptionsRsp = AppleIDUtil.repareOptions(questionRsp, accountRepairRsp);
        HttpResponse securityUpgradeRsp = AppleIDUtil.securityUpgrade(repareOptionsRsp, XAppleIDSessionId, scnt);
        HttpResponse securityUpgradeSetuplaterRsp = AppleIDUtil.securityUpgradeSetuplater(securityUpgradeRsp, XAppleIDSessionId, scnt);
        HttpResponse repareOptionsSecondRsp = AppleIDUtil.repareOptionsSecond(securityUpgradeSetuplaterRsp, XAppleIDSessionId, scnt);
        HttpResponse repareCompleteRsp  = AppleIDUtil.repareComplete(repareOptionsSecondRsp, questionRsp);
        // Token
        HttpResponse tokenRsp   = AppleIDUtil.token(repareCompleteRsp);
        if (tokenRsp.getStatus() != 200){
            setAndRefreshNote(account,"登录异常");
            return null;
        }
        return tokenRsp;
    }

    public String loginAndGetScnt(Account account){
        String authType = StrUtil.isEmpty(account.getAnswer1()) ? "hsa2" : "sa";
        HttpResponse tokenRsp = login(account,authType);
        if (tokenRsp == null){
            return "";
        }
        return getTokenScnt(tokenRsp);
    }

    public String getTokenScnt(HttpResponse rsp){
        String tokenScnt = rsp.header("scnt");
        return tokenScnt;
    }

    /**
     * 打开双重认证视图
     */
    public String openSecurityCodePopupView(Account account){
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/securitycode-popup.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), 600, 350);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        SecuritycodePopupController s = fxmlLoader.getController();
        s.setAccount(account.getAccount());

        Stage popupStage = new Stage();
        popupStage.setTitle("双重验证码输入页面");
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.showAndWait();

        String type = s.getSecurityType();
        String code = s.getSecurityCode();
        if (StrUtil.isEmpty(type) || StrUtil.isEmpty(code)){
            return "";
        }
        return type + "-" + code;
    }

}

package com.sgswit.fx.controller.common;

import cn.hutool.core.lang.Console;
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

        if(signInRsp.getStatus()==503){
            setAndRefreshNote(account,"操作频繁");
            return null;
        }

        if(signInRsp.getStatus()!=409){
            setAndRefreshNote(account,"请检查用户名密码是否正确");
            return null;
        }

        // Auth
        HttpResponse authRsp = AppleIDUtil.auth(account,signInRsp);
        String authType = JSONUtil.parse(signInRsp.body()).getByPath("authType",String.class);

        HttpResponse securityCodeOrReparCompleteRsp = null;
        // 双重认证
        if ("hsa2".equals(authType)) {
            // todo 后续调整为双重认证右键输入验证码登陆
            // 方便测试
            setAndRefreshNote(account,"此账号已开启双重认证，请输入双重验证码。");
            Console.log("请输入双重验证码(device-xxx,sms-xxx)：");
            String typeCode = Console.input();
            account.setSecurityCode(typeCode);
            securityCodeOrReparCompleteRsp = AppleIDUtil.securityCode(account,authRsp);
            //return null;
        } else { // sa 密保认证
            if (StrUtil.isEmpty(account.getAnswer1()) || StrUtil.isEmpty(account.getAnswer2()) || StrUtil.isEmpty(account.getAnswer3())){
                setAndRefreshNote(account,"密保认证必须输入密保问题");
                return null;
            }
            // 密保认证
            HttpResponse questionRsp = AppleIDUtil.questions(account,authRsp);
            if (questionRsp.getStatus() != 412) {
                setAndRefreshNote(account,"密保问题验证失败");
                return null;
            }

            HttpResponse accountRepairRsp = AppleIDUtil.accountRepair(account,questionRsp);
            String XAppleIDSessionId = "";
            String scnt              = accountRepairRsp.header("scnt");

            for (String item : accountRepairRsp.headerList("Set-Cookie")) {
                if (item.startsWith("aidsp")) {
                    XAppleIDSessionId = item.substring(item.indexOf("aidsp=") + 6, item.indexOf("; Domain=appleid.apple.com"));
                }
            }
            HttpResponse repareOptionsRsp = AppleIDUtil.repareOptions(account, questionRsp, accountRepairRsp);
            HttpResponse securityUpgradeRsp = AppleIDUtil.securityUpgrade(account,repareOptionsRsp,XAppleIDSessionId,scnt);
            HttpResponse securityUpgradeSetuplaterRsp = AppleIDUtil.securityUpgradeSetuplater(account,securityUpgradeRsp,XAppleIDSessionId,scnt);
            HttpResponse repareOptionsSecondRsp = AppleIDUtil.repareOptionsSecond(account,securityUpgradeSetuplaterRsp,XAppleIDSessionId,scnt);
            securityCodeOrReparCompleteRsp =  AppleIDUtil.repareComplete(account,repareOptionsSecondRsp,questionRsp);
        }

        HttpResponse tokenRsp = AppleIDUtil.token(account,securityCodeOrReparCompleteRsp);

        account.setScnt(tokenRsp.header("scnt"));
        account.setXAppleIDSessionId(tokenRsp.header("X-Apple-ID-Session-Id"));

        if (tokenRsp.getStatus() != 200){
            setAndRefreshNote(account,"登录异常");
            return null;
        }
        return tokenRsp;
    }

    public String loginAndGetScnt(Account account){
//        String authType = StrUtil.isEmpty(account.getAnswer1()) ? "hsa2" : "sa";
        HttpResponse tokenRsp = login(account);
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

package com.sgswit.fx.controller.base;

import cn.hutool.core.lang.Console;
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
import java.util.List;

public class AppleIdView extends TableView {

    /**
     * appleid官网登录
     */
    public HttpResponse login(Account account){
        // SignIn
        HttpResponse signInRsp = AppleIDUtil.signin(account);
        if(signInRsp.getStatus()!=409){
            account.setNote("请检查用户名密码是否正确");
            this.refreshTableView();
            return null;
        }

        // Auth
        HttpResponse authRsp = AppleIDUtil.auth(signInRsp);
        String authType = JSONUtil.parse(signInRsp.body()).getByPath("authType",String.class);

        // 双重认证
        if ("hsa2".equals(authType)) {
            String typeCode = this.openSecurityCodePopupView(account);
            String[] code = typeCode.split("-");
            HttpResponse securityCodeRsp = AppleIDUtil.securityCode(authRsp, code[0], code[1]);

            // Token
            HttpResponse tokenRsp = AppleIDUtil.token(securityCodeRsp);
            return tokenRsp;
        }else{
            // 密保认证
            HttpResponse questionRsp = AppleIDUtil.questions(authRsp, account);
            if (questionRsp.getStatus() != 412) {
                Console.error("密保认证异常！");
                account.setNote("密保问题验证失败");
                this.refreshTableView();
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
                account.setNote("登录异常");
                this.refreshTableView();
                return null;
            }
            return tokenRsp;
        }
    }

    public String loginAndGetScnt(Account account){
        HttpResponse tokenRsp = login(account);
        if (tokenRsp == null){
            this.refreshTableView();
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
        return type + "-" + code;
    }

}

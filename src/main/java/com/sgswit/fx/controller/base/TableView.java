package com.sgswit.fx.controller.base;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.controller.iTunes.AccountInputPopupController;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * account表格视图
 */
public class TableView implements Initializable {

    @FXML
    public javafx.scene.control.TableView<Account> tableViewDataList;

    /**
     * 总账号数量
     */
    @FXML
    protected Label accountNumLable;

    protected ObservableList<Account> accountList = FXCollections.observableArrayList();

    @FXML
    private TableColumn seq;

    @FXML
    private TableColumn account;

    @FXML
    private TableColumn pwd;

    @FXML
    private TableColumn name;

    @FXML
    private TableColumn state;

    @FXML
    private TableColumn aera;

    @FXML
    private TableColumn status;

    @FXML
    private TableColumn note;

    @FXML
    private TableColumn answer1;

    @FXML
    private TableColumn answer2;

    @FXML
    private TableColumn answer3;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initAccountTableView();
    }

    /**
     * 初始化字段绑定
     */
    public void initAccountTableView(){
        seq.setCellValueFactory(new PropertyValueFactory<Account,Integer>("seq"));
        account.setCellValueFactory(new PropertyValueFactory<Account,String>("account"));
        pwd.setCellValueFactory(new PropertyValueFactory<Account,String>("pwd"));
        state.setCellValueFactory(new PropertyValueFactory<Account,String>("state"));
        aera.setCellValueFactory(new PropertyValueFactory<Account,String>("aera"));
        name.setCellValueFactory(new PropertyValueFactory<Account,String>("name"));
        status.setCellValueFactory(new PropertyValueFactory<Account,String>("status"));
        answer1.setCellValueFactory(new PropertyValueFactory<Account,String>("answer1"));
        answer2.setCellValueFactory(new PropertyValueFactory<Account,String>("answer2"));
        answer3.setCellValueFactory(new PropertyValueFactory<Account,String>("answer3"));
        note.setCellValueFactory(new PropertyValueFactory<Account,String>("note"));
    }

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction(){
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/iTunes/account-input-popup.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), 600, 450);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        Stage popupStage = new Stage();

        popupStage.setTitle("账户导入");

        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.showAndWait();

        AccountInputPopupController viewCtr = fxmlLoader.getController();

        String[] lineArray = viewCtr.getAccounts().split("\n");
        for(String item : lineArray){
            String[] its = item.split("----");
            Account account = new Account();
            account.setAccount(its[0]);

            String[] pas = its[1].split("-");
            if(pas.length == 4){
                account.setPwd(pas[0]);
                account.setAnswer1(pas[1]);
                account.setAnswer2(pas[2]);
                account.setAnswer3(pas[3]);
            }else{
                account.setPwd(its[1]);
            }
            accountList.add(account);
        }
        if (!CollectionUtil.isEmpty(accountList)){
            for (int i = 0; i < accountList.size(); i++) {
                Account account = accountList.get(i);
                account.setSeq(i+1);
            }
        }
        tableViewDataList.setItems(accountList);
        accountNumLable.setText(accountList.size()+"");
    }

    /**
     * 清空列表按钮点击
     */
    public void clearAccountListButtonAction(){
        accountList.clear();
    }

    public void alert(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示信息");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public HttpResponse login(Account account){
        // SignIn
        HttpResponse signInRsp = AppleIDUtil.signin(account);

        // Auth
        HttpResponse authRsp = AppleIDUtil.auth(signInRsp);

        String authType = JSONUtil.parse(signInRsp.body()).getByPath("authType",String.class);
        if (!"sa".equals(authType)) {
            Console.error("仅支持密保验证逻辑");
            account.appendNote("仅支持密保验证逻辑;");
            return null;
        }

        // 密保认证
        HttpResponse questionRsp = AppleIDUtil.questions(authRsp, account);
        if (questionRsp.getStatus() != 412) {
            Console.error("密保认证异常！");
            account.appendNote("密保认证异常;");
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

        HttpResponse tokenRsp   = AppleIDUtil.token(repareCompleteRsp);
        if (tokenRsp.getStatus() != 200){
            account.appendNote("登陆异常;");
            return null;
        }
        return tokenRsp;
    }

    public String getTokenScnt(Account account){
        HttpResponse tokenRsp = login(account);

        String tokenScnt = tokenRsp.header("scnt");
        return tokenScnt;
    }
}

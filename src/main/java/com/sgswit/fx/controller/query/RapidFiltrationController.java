package com.sgswit.fx.controller.query;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.controller.iTunes.AccountInputPopupController;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.Question;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 *  急速过滤密正
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class RapidFiltrationController {

    @FXML
    public Button birthdayCountryQueryBtn;
    @FXML
    private Label accountNum;
    @FXML
    private TableView accountTableView;
    @FXML
    private TableColumn seq;
    @FXML
    private TableColumn account;
    @FXML
    private TableColumn pwd;
    @FXML
    private TableColumn note;

    private ObservableList<Account> list = FXCollections.observableArrayList();

    @FXML
    public void onRapidBtnClick(ActionEvent actionEvent) {

        if (list.size() < 1) {
            return;
        }

        Account account = list.get(0);

        //非双重认证
        birthdayCountryQueryBtn.setText("正在查询");
        birthdayCountryQueryBtn.setTextFill(Paint.valueOf("#FF0000"));
        birthdayCountryQueryBtn.setDisable(true);

        account.setNote("正在查询");
        accountTableView.refresh();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    noSecondSec(account);
                } finally {
                    //JavaFX Application Thread会逐个阻塞的执行这些任务
                    Platform.runLater(new Task<Integer>() {
                        @Override
                        protected Integer call() {
                            birthdayCountryQueryBtn.setDisable(false);
                            birthdayCountryQueryBtn.setText("开始执行");
                            birthdayCountryQueryBtn.setTextFill(Paint.valueOf("#238142"));
                            return 1;
                        }
                    });
                }
            }
        }).start();
    }


    private boolean noSecondSec(Account account) {
        //step1 sign 登录
        HttpResponse step1Res = AppleIDUtil.signin(account);

        if (step1Res.getStatus() != 409) {
            queryFail(account,step1Res.body());
            return false;
        }
        String step1Body = step1Res.body();
        JSON json = JSONUtil.parse(step1Body);
        if (json == null) {
            queryFail(account,step1Res.body());
            return false;
        }


        //step2 获取认证信息 -- 需要输入密保
        HttpResponse step21Res = AppleIDUtil.auth(step1Res);
        String authType = (String) json.getByPath("authType");
        if ("sa".equals(authType)) {
            //非双重认证
            String body = step21Res.body();
            String questions = JSONUtil.parseObj(body).getJSONObject("securityQuestions").get("questions").toString();
            List<Question> qs = JSONUtil.toList(questions, Question.class);

            account.setNote("密码正确");
            accountTableView.refresh();
        } else if ("hsa2".equals(authType)) {
            account.setNote("此账号已开启双重认证");
            accountTableView.refresh();
        }

        txt(account);
        return true;
    }

    @FXML
    protected void onAreaQueryLogBtnClick() throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/query/rapid-filtration-querylog-popup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 950, 600);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("账户查询记录");
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    @FXML
    protected void onAccountExportBtnClick() throws Exception{
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("友情提示");
        alert.setHeaderText("功能建设中，敬请期待");
        alert.show();
    }

    @FXML
    protected void onAccountInputBtnClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/iTunes/account-input-popup.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 600, 450);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        Stage popupStage = new Stage();

        popupStage.setTitle("账户导入");

        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.showAndWait();

        AccountInputPopupController c = fxmlLoader.getController();
        if(null == c.getAccounts() || "".equals(c.getAccounts())){
            return;
        }
        String[] lineArray = c.getAccounts().split("\n");
        accountNum.setText(String.valueOf(lineArray.length));
        for(String item : lineArray){
            String[] its = item.split("----");
            Account account = new Account();
            account.setSeq(list.size()+1);
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
            list.add(account);
        }
        initAccountTableView();
        accountTableView.setItems(list);
    }

    private void initAccountTableView(){
        seq.setCellValueFactory(new PropertyValueFactory<Account,Integer>("seq"));
        account.setCellValueFactory(new PropertyValueFactory<Account,String>("account"));
        pwd.setCellValueFactory(new PropertyValueFactory<Account,String>("pwd"));
        note.setCellValueFactory(new PropertyValueFactory<Account,String>("note"));
    }

    private void queryFail(Account account,Object body) {
        JSONArray serviceErrors = JSONUtil.parseArray(JSONUtil.parseObj(body.toString()).get("serviceErrors").toString());
        String message = JSONUtil.parseObj(serviceErrors.get(0)).get("message").toString();
        if(message.contains("锁定")){
            account.setNote("账号已锁定");
        }else
        if(message.contains("密码")){
            account.setNote("Apple ID 或密码不正确");
        }else{
            account.setNote("Apple ID 未激活");
        }
        accountTableView.refresh();
        txt(account);
    }

    private void txt(Account account){
        account.setLogtime(DateUtil.format(DateUtil.date(),"yyyy-MM-dd HH:mm:ss"));
        try {
            File file = FileUtil.file("rapidQuery.txt");
            FileAppender appender = new FileAppender(file, 16, true);
            appender.append(JSONUtil.toJsonStr(list.get(0)));

            appender.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onAccountClearBtnClick() throws Exception{
        this.list.clear();
        accountTableView.refresh();
    }



}

package com.sgswit.fx.controller.query;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.SystemUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.controller.iTunes.AccountInputPopupController;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.Problem;
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
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 查询密保问题
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class SecurityQuestionQueryController {

    @FXML
    public Button questionCountryQueryBtn;
    @FXML
    private Label accountNum;
    @FXML
    private TableView questionTableView;
    @FXML
    private TableColumn seq;
    @FXML
    private TableColumn account;
    @FXML
    private TableColumn pwd;
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
    @FXML
    private TableColumn problem1;
    @FXML
    private TableColumn problem2;
    @FXML
    private TableColumn problem3;

    private ObservableList<Problem> list = FXCollections.observableArrayList();

    @FXML
    public void onSecurityQuestionQueryBtnClick(ActionEvent actionEvent) {

        if (list.size() < 1) {
            return;
        }
        questionCountryQueryBtn.setText("正在查询");
        questionCountryQueryBtn.setTextFill(Paint.valueOf("#FF0000"));
        questionCountryQueryBtn.setDisable(true);
        for(Problem problem:list){
            if(!StrUtil.isEmptyIfStr(problem.getNote())){
                continue;
            }
            problem.setNote("正在查询...");
            questionTableView.refresh();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        noSecondSec(problem);
                    } finally {
                        //JavaFX Application Thread会逐个阻塞的执行这些任务
                        Platform.runLater(new Task<Integer>() {
                            @Override
                            protected Integer call() {
                                questionCountryQueryBtn.setDisable(false);
                                questionCountryQueryBtn.setText("开始执行");
                                questionCountryQueryBtn.setTextFill(Paint.valueOf("#238142"));
                                return 1;
                            }
                        });
                    }
                }
            }).start();
        }
    }


    private boolean noSecondSec(Problem problem) {
        //step1 sign 登录
        Account account = new Account();
        account.setAccount(problem.getAccount());
        account.setPwd(problem.getPwd());
        HttpResponse step1Res = AppleIDUtil.signin(account);

        if (step1Res.getStatus() != 409) {
            queryFail(problem);
            return false;
        }
        String step1Body = step1Res.body();
        JSON json = JSONUtil.parse(step1Body);
        if (json == null) {
            queryFail(problem);
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
            problem.setProblem1(qs.get(0).getQuestion());
            problem.setProblem2(qs.get(1).getQuestion());
            problem.setNote("查询完毕");
            questionTableView.refresh();
        } else if ("hsa2".equals(authType)) {
            problem.setNote("此账号已开启双重认证");
            questionTableView.refresh();
        }
        problem.setLogtime(DateUtil.format(DateUtil.date(),"yyyy-MM-dd HH:mm:ss"));
        try {
            File file = FileUtil.file("questionQuery.txt");
            FileAppender appender = new FileAppender(file, 16, true);
            appender.append(JSONUtil.toJsonStr(list.get(0)));

            appender.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @FXML
    protected void onAreaQueryLogBtnClick() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/query/securuty-question-querylog-popup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 950, 600);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("账户查询记录");
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    @FXML
    protected void onAccountExportBtnClick() throws Exception {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("友情提示");
        alert.setHeaderText("功能建设中，敬请期待");
        alert.show();
    }

    @FXML
    protected void onAccountInputBtnClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/query/account-input-popup.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 600, 450);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        Stage popupStage = new Stage();

        popupStage.setTitle("账户导入");

        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.showAndWait();

        AccountInputPopupController c = fxmlLoader.getController();
        if (null == c.getAccounts() || "".equals(c.getAccounts())) {
            return;
        }
        String[] lineArray = c.getAccounts().split("\n");

        for (String item : lineArray) {
            String[] its = item.split("----");
            Problem account = new Problem();
            account.setSeq(list.size() + 1);
            account.setAccount(its[0]);
            account.setPwd(its[1]);
            list.add(account);
        }
        initAccountTableView();
        accountNum.setText(String.valueOf(list.size()));
        questionTableView.setItems(list);
    }

    private void initAccountTableView() {
        seq.setCellValueFactory(new PropertyValueFactory<Account, Integer>("seq"));
        account.setCellValueFactory(new PropertyValueFactory<Account, String>("account"));
        pwd.setCellValueFactory(new PropertyValueFactory<Account, String>("pwd"));
        note.setCellValueFactory(new PropertyValueFactory<Account, String>("note"));
        problem1.setCellValueFactory(new PropertyValueFactory<Problem,String>("problem1"));
        problem2.setCellValueFactory(new PropertyValueFactory<Problem,String>("problem2"));
    }

    private void queryFail(Problem problem) {
        String note = "查询失败，请确认用户名密码是否正确";
        problem.setNote(note);
        questionTableView.refresh();
    }

    @FXML
    protected void onAccountClearBtnClick() throws Exception {
        this.list.clear();
        accountNum.setText("0");
        questionTableView.refresh();
    }


}

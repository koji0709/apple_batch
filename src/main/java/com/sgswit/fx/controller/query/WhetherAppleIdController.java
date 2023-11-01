package com.sgswit.fx.controller.query;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.controller.iTunes.AccountInputPopupController;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.StringUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 检测是否AppleID
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class WhetherAppleIdController {

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
    private TableColumn status;
    @FXML
    private TableColumn note;
    @FXML
    private TableColumn verify;

    private ObservableList<Account> list = FXCollections.observableArrayList();

    @FXML
    public void onWhetherAppIdBtnClick(ActionEvent actionEvent) {

        if (list.size() < 1) {
            return;
        }

        Account account = list.get(0);


        birthdayCountryQueryBtn.setText("正在查询");
        birthdayCountryQueryBtn.setTextFill(Paint.valueOf("#FF0000"));
        birthdayCountryQueryBtn.setDisable(true);

        account.setNote("正在查询");
        accountTableView.refresh();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    verify(account);
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
        });

    }


    private void verify(Account account) {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", ListUtil.toList("application/json, text/javascript, */*"));
        headers.put("Accept-Encoding", ListUtil.toList("gzip, deflate, br"));
        String url = "https://iforgot.apple.com/captcha?captchaType=IMAGE";
        HttpResponse execute = HttpUtil.createGet(url)
                .header(headers)
                .execute();
        String body = execute.body();
        JSONObject object = JSONUtil.parseObj(body);
        String capId = object.get("id").toString();
        String capToken = object.get("token").toString();

        //解析图片
        JSONObject object1 = JSONUtil.parseObj(JSONUtil.parseObj(body).get("payload").toString());
        Object content = object1.get("content");
        byte[] decode = Base64.getDecoder().decode(content.toString());
        BorderPane root = new BorderPane();
        ImageView imageView = new ImageView();

        imageView.setImage(new Image(new ByteArrayInputStream(decode)));

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("验证码");
        dialog.setHeaderText("验证码为：");
        root.setCenter(imageView);
        dialog.setContentText("请输入验证码:");
        dialog.setGraphic(root);
        Optional<String> result = dialog.showAndWait();



        if (result.isPresent()) {
            String s = result.get();
            String bodys = "{\"id\":\"" + account.getAccount() + "\",\"captcha\":{\"id\":" + capId + ",\"answer\":\"" + s + "\",\"token\":\"" + capToken + "\"}}\n";
            HttpResponse execute1 = HttpUtil.createPost("https://iforgot.apple.com/password/verify/appleid")
                    .body(bodys)
                    .header(headers)
                    .execute();

            if (!StringUtils.isEmpty(execute1.body())) {
                JSONObject object2 = JSONUtil.parseObj(execute1.body());
                if (object2.getStr("service_errors") != null) {
                    account.setStatus("这个AppleID没有被激活");
                    account.setNote("查询成功");
                    accountTableView.refresh();
                } else if (object2.getStr("serviceErrors") != null) {
                    account.setStatus("此AppleID无效或不受支持");
                    account.setNote("查询成功");
                    accountTableView.refresh();
                }


            } else {
                HttpResponse location = HttpUtil.createGet("https://iforgot.apple.com" + execute1.header("Location"))
                        .header(headers)
                        .execute();

                if (StringUtils.isEmpty(location.body())) {
                    account.setStatus("此AppleID已开启双重认证");
                    account.setNote("查询成功");
                    accountTableView.refresh();

                } else if (JSONUtil.parseObj(location.body()).get("account") != null) {
                    account.setStatus("此AppleID已被锁定");
                    account.setNote("查询成功");
                    accountTableView.refresh();
                } else {
                    account.setStatus("此AppleID正常");
                    account.setNote("查询成功");
                    accountTableView.refresh();
                }
            }

        }
        account.setLogtime(DateUtil.format(DateUtil.date(),"yyyy-MM-dd HH:mm:ss"));
        try {
            File file = FileUtil.file("appleIDVerify.txt");
            FileAppender appender = new FileAppender(file, 16, true);
            appender.append(JSONUtil.toJsonStr(list.get(0)));

            appender.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    protected void onAreaQueryLogBtnClick() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/query/whether-appleid-querylog-popup.fxml"));
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
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/iTunes/account-input-popup.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 600, 450);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        Stage popupStage = new Stage();

        popupStage.setTitle("账户导入");

        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.showAndWait();

        AccountInputPopupController c = fxmlLoader.getController();
        if (null == c.getAccounts() || "".equals(c.getAccounts())) {
            return;
        }
        String[] lineArray = c.getAccounts().split("\n");
        accountNum.setText(String.valueOf(lineArray.length));
        for (String item : lineArray) {
            Account account = new Account();
            account.setSeq(list.size() + 1);
            account.setAccount(item);
            list.add(account);
        }
        initAccountTableView();
        accountTableView.setItems(list);
    }


    private void initAccountTableView() {
        seq.setCellValueFactory(new PropertyValueFactory<Account, Integer>("seq"));
        account.setCellValueFactory(new PropertyValueFactory<Account, String>("account"));
        status.setCellValueFactory(new PropertyValueFactory<Account, String>("status"));
        note.setCellValueFactory(new PropertyValueFactory<Account, String>("note"));
    }

    private void queryFail(Account account) {
        String note = "查询失败，请确认用户名密码是否正确";
        account.setNote(note);
        accountTableView.refresh();
    }

    @FXML
    protected void onAccountClearBtnClick() throws Exception {
        this.list.clear();
        accountTableView.refresh();
    }


}

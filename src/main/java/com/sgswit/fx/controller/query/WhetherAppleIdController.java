package com.sgswit.fx.controller.query;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.controller.common.CommonView;
import com.sgswit.fx.controller.iTunes.AccountInputPopupController;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.OcrUtil;
import com.sgswit.fx.utils.StringUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 检测是否AppleID
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class WhetherAppleIdController extends CommonView {

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
        birthdayCountryQueryBtn.setText("正在查询");
        birthdayCountryQueryBtn.setTextFill(Paint.valueOf("#FF0000"));
        birthdayCountryQueryBtn.setDisable(true);

        for (Account account : list) {
            if (!StrUtil.isEmptyIfStr(account.getNote())) {
                continue;
            }

            account.setNote("正在查询");
            accountTableView.refresh();

            verify(account);

            // 200 302
//            HttpResponse captchaAndVerifyRsp = AppleIDUtil.captchaAndVerify(account.getAccount());
//            if(captchaAndVerifyRsp.body().startsWith("<html>")){
//                account.setStatus("操作频繁！");
//                account.setNote("查询失败");
//                accountTableView.refresh();
//
//                account.setLogtime(DateUtil.format(DateUtil.date(), "yyyy-MM-dd HH:mm:ss"));
//                try {
//                    File file = FileUtil.file("appleIDVerify.txt");
//                    FileAppender appender = new FileAppender(file, 16, true);
//                    appender.append(JSONUtil.toJsonStr(account));
//
//                    appender.flush();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                continue;
//            }
//            String failMessage = hasFailMessage(captchaAndVerifyRsp) ? failMessage(captchaAndVerifyRsp) : "";
//            if (!StrUtil.isEmpty(failMessage)){
//                account.setNote(failMessage);
//            }else{
//                String location = captchaAndVerifyRsp.header("Location");
//                if (location.startsWith("/password/verify/resetmethod")){
//                    // 双重认证
//                    account.setStatus("此AppleID已开启双重认证");
//                    account.setNote("查询成功");
//                    accountTableView.refresh();
//                }else if (location.startsWith("/password/authenticationmethod")){
//                    // 账号被锁
//                    account.setStatus("此AppleID已被锁定");
//                    account.setNote("查询成功");
//                    accountTableView.refresh();
//                }else if (location.startsWith("/recovery/options")){
//                    // 正常状态
//                    account.setStatus("此AppleID正常");
//                    account.setNote("查询成功");
//                    accountTableView.refresh();
//                }
//            }
//            account.setLogtime(DateUtil.format(DateUtil.date(), "yyyy-MM-dd HH:mm:ss"));
//            try {
//                File file = FileUtil.file("appleIDVerify.txt");
//                FileAppender appender = new FileAppender(file, 16, true);
//                appender.append(JSONUtil.toJsonStr(account));
//
//                appender.flush();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }

        birthdayCountryQueryBtn.setDisable(false);
        birthdayCountryQueryBtn.setText("开始执行");
        birthdayCountryQueryBtn.setTextFill(Paint.valueOf("#238142"));
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
        String predict = OcrUtil.recognize(content.toString());
            String bodys = "{\"id\":\"" + account.getAccount() + "\",\"captcha\":{\"id\":" + capId + ",\"answer\":\"" + predict + "\",\"token\":\"" + capToken + "\"}}\n";
            HttpResponse execute1 = HttpUtil.createPost("https://iforgot.apple.com/password/verify/appleid")
                    .body(bodys)
                    .header(headers)
                    .execute();
            String body1 = execute1.body();
            if(body1.startsWith("<html>")){
                account.setStatus("网页503");
                account.setNote("查询失败");
                accountTableView.refresh();
                return;
            }
            if (!StringUtils.isEmpty(execute1.body())) {
                JSONObject object2 = JSONUtil.parseObj(execute1.body());
                String service_errors = object2.getStr("service_errors");
                if (service_errors != null) {
                    JSONArray service_errors1 = JSONUtil.parseArray(service_errors);
                    String message = JSONUtil.parseObj(service_errors1.get(0)).getStr("message");
                    if(message.startsWith("请输入你")){
                       verify(account);
                    }else {
                        account.setStatus(message);
                        account.setNote("查询成功");
                        accountTableView.refresh();
                    }

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
//        }

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
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/query/account-input-popup1.fxml"));

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

        for (String item : lineArray) {
            Account account = new Account();
            account.setSeq(list.size() + 1);
            account.setAccount(item);
            list.add(account);
        }
        initAccountTableView();
        accountNum.setText(String.valueOf(list.size()));
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
        accountNum.setText("0");
        accountTableView.refresh();
    }


}

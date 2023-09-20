package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.SecuritycodePopupController;
import com.sgswit.fx.controller.iTunes.model.UserNationalModel;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.KeyValuePair;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.StringUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;


/**
 * @author DELL
 */
public class CountryModifyController implements Initializable {
    @FXML
    public ChoiceBox customCountryBox;
    @FXML
    public HBox customCountrySelectId;
    @FXML
    private TableView accountTableView;
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

    @FXML
    private Button accoutQueryBtn;

    @FXML
    private Button accountExportBtn;

    @FXML
    private ChoiceBox<KeyValuePair> countryBox;
    private List<KeyValuePair> countryList=new ArrayList<>(){{
        add(new KeyValuePair("","请选择"));
        add(new KeyValuePair("CHN","中国"));
        add(new KeyValuePair("CHL","智利"));
    }};

    private ObservableList<Account> list = FXCollections.observableArrayList();

    public CountryModifyController(){


    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
       //初始化国家
        countryBox.getItems().addAll(countryList);
        countryBox.converterProperty().set(new StringConverter<KeyValuePair>() {
            @Override
            public String toString(KeyValuePair object) {
                return object.getValue();
            }

            @Override
            public KeyValuePair fromString(String string) {
                return null;
            }
        });

        setShowCustomCountryData();

    }
    protected void setShowCustomCountryData(){
        //判断是否显示 自定义国家下拉框
        List<UserNationalModel> list=new ArrayList<>();
        File jsonFile = new File("userNationalData.json");
        if(!jsonFile.exists()){
            customCountrySelectId.setVisible(false);
            return;
        }
        String jsonString = FileUtil.readUtf8String(jsonFile);
        if(!StringUtils.isEmpty(jsonString)){
            list = JSONUtil.toList(jsonString,UserNationalModel.class);
        }
        if(list.size()>0){
            customCountrySelectId.setVisible(true);
        }else{
            customCountrySelectId.setVisible(false);
        }
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

    @FXML
    protected void onAccountExportBtnClick() throws Exception{
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("友情提示");
        alert.setContentText("功能建设中，敬请期待");
        alert.show();
    }

    @FXML
    protected void onAccountClearBtnClick() throws Exception{
        this.list.clear();
        accountTableView.refresh();
    }

    @FXML
    protected void onAccoutQueryBtnClick() throws Exception{

        if(list.size() < 1){
            return;
        }

        Account account = list.get(0);
        if(StrUtil.hasEmpty(account.getAnswer1())){
            //双重认证
            secondSec(account);
        }else{
            //非双重认证
            accoutQueryBtn.setText("正在查询");
            accoutQueryBtn.setTextFill(Paint.valueOf("#FF0000"));
            accoutQueryBtn.setDisable(true);

            account.setNote("正在查询");
            accountTableView.refresh();

            new Thread(new Runnable() {
                @Override
                public void run(){
                    try {
                        noSecondSec(account);
                    }finally {
                        //JavaFX Application Thread会逐个阻塞的执行这些任务
                        Platform.runLater(new Task<Integer>() {
                            @Override
                            protected Integer call() {
                                accoutQueryBtn.setDisable(false);
                                accoutQueryBtn.setText("开始执行");
                                accoutQueryBtn.setTextFill(Paint.valueOf("#238142"));
                                return 1;
                            }
                        });
                    }
                }
            }).start();
        }
    }



    private boolean secondSec(Account account) {
        //step1 sign 登录
        HttpResponse step1Res = AppleIDUtil.signin(account);

        if (step1Res.getStatus() != 409) {
            queryFail(account);
            return false;
        }
        String step1Body = step1Res.body();
        JSON json = JSONUtil.parse(step1Body);
        if (json == null) {
            queryFail(account);
            return false;
        }

        //step2 auth 获取认证信息
        HttpResponse step21Res = AppleIDUtil.auth(step1Res);
        String authType = (String) json.getByPath("authType");
        if ("hsa2".equals(authType)) {
            // 双重验证
            //step2.2 输入验证码
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/securitycode-popup.fxml"));

                Scene scene = new Scene(fxmlLoader.load(), 600, 350);
                scene.getRoot().setStyle("-fx-font-family: 'serif'");

                SecuritycodePopupController s = (SecuritycodePopupController) fxmlLoader.getController();
                s.setAccount(account.getAccount());

                Stage popupStage = new Stage();
                popupStage.setTitle("双重验证码输入页面");
                popupStage.initModality(Modality.WINDOW_MODAL);
                popupStage.setScene(scene);
                popupStage.showAndWait();

                String type = s.getSecurityType();
                String code = s.getSecurityCode();

                accoutQueryBtn.setText("正在查询");
                accoutQueryBtn.setTextFill(Paint.valueOf("#FF0000"));
                accoutQueryBtn.setDisable(true);

                account.setNote("正在查询");
                accountTableView.refresh();

                new Thread(new Runnable() {
                    @Override
                    public void run(){
                        try {
                            HttpResponse step22Res = AppleIDUtil.securityCode(step21Res, type, code);

                            if (step22Res.getStatus() != 204 && step22Res.getStatus() != 200) {
                                queryFail(account);
                            }
                            manager(account, step22Res);
                        }finally {
                            //JavaFX Application Thread会逐个阻塞的执行这些任务
                            Platform.runLater(new Task<Integer>() {
                                @Override
                                protected Integer call() {
                                    accoutQueryBtn.setDisable(false);
                                    accoutQueryBtn.setText("开始执行");
                                    accoutQueryBtn.setTextFill(Paint.valueOf("#238142"));
                                    return 1;
                                }
                            });
                        }
                    }
                }).start();


            }catch (Exception e){
                return false;
            }

        }else if ("sa".equals(authType)) {
            account.setNote("该账户为非双重认证模式，请输入密保信息后重试");
            accountTableView.refresh();
        }
        return true;
    }

    private boolean noSecondSec(Account account) {
        //step1 sign 登录

        HttpResponse step1Res = AppleIDUtil.signin(account);

        if (step1Res.getStatus() != 409) {
            queryFail(account);
            return false;
        }
        String step1Body = step1Res.body();
        JSON json = JSONUtil.parse(step1Body);
        if (json == null) {
            queryFail(account);
            return false;
        }


        //step2 获取认证信息 -- 需要输入密保
        HttpResponse step21Res = AppleIDUtil.auth(step1Res);
        String authType = (String) json.getByPath("authType");
        if ("sa".equals(authType)) {
            //非双重认证
            HttpResponse step211Res = AppleIDUtil.questions(step21Res, account);
            if (step211Res.getStatus() != 412) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("密保信息有误，请确认");
                alert.show();
                return false;
            }
            HttpResponse step212Res = AppleIDUtil.accountRepair(step211Res);
            String XAppleIDSessionId = "";
            String scnt = step212Res.header("scnt");
            List<String> cookies = step212Res.headerList("Set-Cookie");
            for (String item : cookies) {
                if (item.startsWith("aidsp")) {
                    XAppleIDSessionId = item.substring(item.indexOf("aidsp=") + 6, item.indexOf("; Domain=appleid.apple.com"));
                }
            }
            HttpResponse step213Res = AppleIDUtil.repareOptions(step211Res, step212Res);
            HttpResponse step214Res = AppleIDUtil.securityUpgrade(step213Res, XAppleIDSessionId, scnt);
            HttpResponse step215Res = AppleIDUtil.securityUpgradeSetuplater(step214Res, XAppleIDSessionId, scnt);
            HttpResponse step216Res = AppleIDUtil.repareOptionsSecond(step215Res, XAppleIDSessionId, scnt);
            HttpResponse step22Res = AppleIDUtil.repareComplete(step216Res, step211Res);

//            manager(account, step22Res);
            countryModify(account, step22Res);
        }else if ("hsa2".equals(authType)) {
            account.setNote("该账户为双重认证模式，请清空密保信息后重试");
            accountTableView.refresh();
        }
        return true;
    }

    private void countryModify(Account account, HttpResponse step1Res) {
        //step3 token
        HttpResponse step3Res = AppleIDUtil.token(step1Res);

        //step4 manager
        if(step3Res.getStatus() != 200){
            queryFail(account);
        }
        HashMap<String, List<String>> headers = new HashMap<>();

        headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
        headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
        headers.put("Content-Type", ListUtil.toList("application/json"));

        headers.put("Host", ListUtil.toList("appleid.apple.com"));
        headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

        headers.put("User-Agent",ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));

        headers.put("X-Apple-ID-Session-Id",ListUtil.toList(step3Res.header("X-Apple-ID-Session-Id")));
        headers.put("scnt",ListUtil.toList(step3Res.header("scnt")));

        StringBuilder cookieBuilder = new StringBuilder();
        List<String> resCookies = step3Res.headerList("Set-Cookie");
        for(String item : resCookies){
            cookieBuilder.append(";").append(item);
        }


        HttpResponse res4 = HttpUtil.createRequest(Method.PUT,"https://appleid.apple.com/account/manage/payment/method/none/1")
                .header(headers)
//                .body("{\"billingAddress\":{\"countryCode\":\"CHL\"},\"id\":1}")
                .body("{\"ownerName\":{\"firstName\":\"\",\"lastName\":\"\"},\"phoneNumber\":{\"areaCode\":\"\",\"number\":\"\",\"countryCode\":\"\"},\"billingAddress\":{\"line1\":\"\",\"line2\":\"\",\"line3\":\"\",\"suburb\":\"\",\"county\":\"\",\"city\":\"\",\"countryCode\":\"CHN\",\"postalCode\":\"\",\"stateProvinceName\":\"\"},\"id\":1}")
                .cookie(cookieBuilder.toString())
                .execute();
        System.out.println(JSONUtil.toJsonStr(res4.body()));

    }
    private void manager(Account account, HttpResponse step1Res) {
        //step3 token
        HttpResponse step3Res = AppleIDUtil.token(step1Res);

        //step4 manager
        if(step3Res.getStatus() != 200){
            queryFail(account);
        }
        HttpResponse step4Res = AppleIDUtil.account(step3Res);
        String managerBody = step4Res.body();
        JSON manager = JSONUtil.parse(managerBody);

        String state = (String) manager.getByPath("account.person.primaryAddress.countryCode");
        String area = (String) manager.getByPath("account.person.primaryAddress.countryName");
        String name = (String) manager.getByPath("name.fullName");
        String status = "正常";
        String note = "查询成功";

        account.setStatus(status);
        account.setState(state);
        account.setName(name);
        account.setNote(note);
        account.setAera(area);
        account.setLogtime(DateUtil.format(DateUtil.date(),"yyyy-MM-dd HH:mm:ss"));

        accountTableView.refresh();

        try {
            File file = FileUtil.file("qlog.txt");
            FileAppender appender = new FileAppender(file, 16, true);
            appender.append(JSONUtil.toJsonStr(list.get(0)));

            appender.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void queryFail(Account account) {
        String note = "查询失败，请确认用户名密码是否正确";
        account.setNote(note);
        accountTableView.refresh();
    }

    @FXML
    protected void onAreaQueryLogBtnClick() throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/account-querylog-popup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 950, 600);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("账户查询记录");
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    private void initAccountTableView(){
        seq.setCellValueFactory(new PropertyValueFactory<Account,Integer>("seq"));
        account.setCellValueFactory(new PropertyValueFactory<Account,String>("account"));
        pwd.setCellValueFactory(new PropertyValueFactory<Account,String>("pwd"));
        state.setCellValueFactory(new PropertyValueFactory<Account,String>("state"));
        aera.setCellValueFactory(new PropertyValueFactory<Account,String>("aera"));
        name.setCellValueFactory(new PropertyValueFactory<Account,String>("name"));
        status.setCellValueFactory(new PropertyValueFactory<Account,String>("status"));
        note.setCellValueFactory(new PropertyValueFactory<Account,String>("note"));
        answer1.setCellValueFactory(new PropertyValueFactory<Account,String>("answer1"));
        answer2.setCellValueFactory(new PropertyValueFactory<Account,String>("answer2"));
        answer3.setCellValueFactory(new PropertyValueFactory<Account,String>("answer3"));
    }

    public void onAddCountryBtnClick(MouseEvent mouseEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/iTunes/custom-country-popup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 450, 390);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("新增国家");
        //模块化，对应用里的所有窗口起作用
        popupStage.initModality(Modality.APPLICATION_MODAL);

        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.showAndWait();
        setShowCustomCountryData();
    }
}

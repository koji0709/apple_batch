package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CommRightContextMenuView;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.model.CreditCard;
import com.sgswit.fx.model.GiftCard;
import com.sgswit.fx.utils.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.awt.event.WindowListener;
import java.io.IOException;
import java.math.BigInteger;
import java.net.CookieStore;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DeZh
 * @title: GiftCardBlanceCheckController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/2714:40
 */
public class GiftCardBalanceCheckController  extends CustomTableView<GiftCard> {

    @FXML
    public TableColumn seq;
    @FXML
    public TableColumn giftCardCode;
    @FXML
    public TableColumn balance;
    @FXML
    public TableColumn giftCardNumber;
    @FXML
    public TableColumn logTime;
    @FXML
    public TableColumn note;
    @FXML
    public ComboBox<Map<String,String>> countryBox;
    @FXML
    public TextField account_pwd;
    @FXML
    public Button executeButton;
    @FXML
    public Label alertMessage;
    @FXML
    public Button loginBtn;
//
    @FXML
    private TableView accountTableView;

    private ObservableList<GiftCard> accountList = FXCollections.observableArrayList();

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Map<String,Object> hashMap;
    private boolean hasInit=false;
    private static ExecutorService executor = ThreadUtil.newExecutor(1);
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.GIFTCARD_BLANCE.getCode())));
        getCountry();
        String cardAccount= PropertiesUtil.getOtherConfig("cardAccount");
        account_pwd.setText(cardAccount);
        if(StringUtils.isEmpty(account_pwd.getText())){
            alertMessage.setLabelFor(loginBtn);
            alertMessage.setText("等待初始化....");
        }else{
            new Thread(() -> {
                try {
                    loginAndInit();
                }catch (Exception e){

                }
            }).start();
        }
        super.initialize(url,resourceBundle);
    }
    private void getCountry(){
        String country = ResourceUtil.readUtf8Str("data/giftCard_query_support_country.json");
        for(Object o:JSONUtil.parseArray(country)){
            JSONObject jsonObject=(JSONObject)o;
            countryBox.getItems().add(new HashMap<>(){{
                put("name",jsonObject.getStr("name"));
                put("code",jsonObject.getStr("code"));
            }});
        }
        //默认美国
        countryBox.getSelectionModel().select(0);

        countryBox.converterProperty().set(new StringConverter<>() {
            @Override
            public String toString(Map<String, String> map) {
                return map.get("name");
            }
            @Override
            public Map<String, String> fromString(String string) {
                return null;
            }
        });
        countryBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            loginAndInit();
                        }catch (Exception e){

                        }
                    }
                }).start();
            }
        });
    }


    @FXML
    protected void onAccountInputBtnClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/iTunes/giftCard-input-popup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 300);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        Stage popupStage = new Stage();

        popupStage.setTitle("礼品卡导入");
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.showAndWait();

        GiftCardInputPopupController c = fxmlLoader.getController();
        if(null == c.getData() || "".equals(c.getData())){
            return;
        }
        String[] lineArray = c.getData().split("\n");
        for(String item : lineArray){
            GiftCard giftCard = new GiftCard();
            giftCard.setSeq(accountList.size()+1);
            giftCard.setGiftCardCode(StringUtils.deleteWhitespace(item));
            accountList.add(giftCard);
        }
        initAccountTableView();
        accountTableView.setItems(accountList);
        super.accountList=accountList;
        setAccountNumLabel();
    }


    @FXML
    public void onClickLoginBtn(ActionEvent actionEvent) {
        new Thread(() -> {
            try {
                loginAndInit();
            }catch (Exception e){

            }
        }).start();
    }

    @Override
    @FXML
        public void executeButtonAction(){
        if(StringUtils.isEmpty(account_pwd.getText()) ||  !hasInit){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("信息");
            alert.setHeaderText("请输入一个AppleID作为初始化，账号格式为：账号----密码");
            alert.show();
            return;
        }
        if(accountList.size() < 1){
            return;
        }
        AtomicInteger n=new AtomicInteger();
        for(GiftCard giftCard:accountList){
            //判断是否已执行或执行中,避免重复执行
            if(!StrUtil.isEmptyIfStr(giftCard.getNote())){
                continue;
            }else{
                if(null==hashMap || hashMap.size()==0){
                    loginAndInit();
                }
                executeButton.setText("正在查询");
                executeButton.setTextFill(Paint.valueOf("#FF0000"));
                executeButton.setDisable(true);
                new Thread(new Runnable() {
                    @Override
                    public void run(){
                        try {
                            try {
                                if(n.get()==4){
                                    n.set(0);
                                    Thread.sleep(1000);
                                    loginAndInit();
                                }
                                n.addAndGet(1);
                                checkBalance(giftCard, hashMap);
                            } catch (Exception e) {
                                executeButton.setDisable(false);
                                executeButton.setText("开始执行");
                                executeButton.setTextFill(Paint.valueOf("#238142"));
                                e.printStackTrace();
                            }
                        }finally {
                            //JavaFX Application Thread会逐个阻塞的执行这些任务
                            Platform.runLater(new Task<Integer>() {
                                @Override
                                protected Integer call() {
                                    setAccountNumLabel();
                                    executeButton.setDisable(false);
                                    executeButton.setText("开始执行");
                                    executeButton.setTextFill(Paint.valueOf("#238142"));
                                    return 1;
                                }
                            });
                        }
                    }
                }).start();
            }
        }
    }
    /**
     　* 登录并初始化
     * @param
    　* @return cn.hutool.http.HttpResponse
    　* @throws
    　* @author DeZh
    　* @date 2023/11/1 11:15
     */
    protected void loginAndInit(){
        String msg="初始化成功，下次启动将自动执行初始化";
        String color = "#238142";
        try {
            boolean f=false;
            //校验账号格式是否正确
            if(StringUtils.isEmpty(account_pwd.getText())){

            }else{
                String regex = ".+----.+";
                if(account_pwd.getText().matches(regex)){
                    f=true;
                }
            }
            if(!f){
                Platform.runLater(new Task<Integer>() {
                    @Override
                    protected Integer call() {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("信息");
                        alert.setHeaderText("请输入一个AppleID作为初始化，账号格式为：账号----密码");
                        alert.show();
                        return 1;
                    }
                });
                return;
            }
            updateNodeStatus(true);
            String[] its =account_pwd.getText().split("----");
            String account=its[0];
            String pwd=its[1];
            String countryCode=countryBox.getSelectionModel().getSelectedItem().get("code");
            Platform.runLater(new Task<Integer>() {
                @Override
                protected Integer call() {
                    alertMessage.setText("正在初始化...");
                    alertMessage.setTextFill(Paint.valueOf("black"));
                    return 1;
                }
            });
            //https://secure.store.apple.com/shop/giftcard/balance
            HttpResponse pre1 = GiftCardUtil.shopPre1(countryCode);
            if(pre1.getStatus() != 303){
                msg="初始化失败，请重试";
                color="red";
                hasInit=false;
                updateUI(msg,color);
                return ;
            }

            //https://secure4.store.apple.com/shop/giftcard/balance
            HttpResponse pre2 = GiftCardUtil.shopPre2(pre1);
            if(pre2.getStatus() != 302){
                msg="初始化失败，请重试";
                color="red";
                hasInit=false;
                updateUI(msg,color);
                return ;
            }

            //https://secure4.store.apple.com/shop/signIn?ssi=1AAABiatkunsgRa-aWEWPTDH2TWsHul_CZ2TC62v9QxcThhc-EPUrFW8AAAA3aHR0cHM6Ly9zZWN1cmU0LnN0b3JlLmFwcGxlLmNvbS9zaG9wL2dpZnRjYXJkL2JhbGFuY2V8fAACAf0PkQUMMDk-ffBr4IVwBmhKDAsCeTbIe2k-7oOanvAP
            HttpResponse pre3 = GiftCardUtil.shopPre3(pre1,pre2);
            if(null==hashMap){
                hashMap=new HashMap<>();
            }
            hashMap=GiftCardUtil.jXDocument(pre2, pre3,hashMap);
            if(null!=hashMap.get("code") && MapUtils.getStr(hashMap,"code").equalsIgnoreCase("503")){
                msg="初始化失败，请重试";
                color="red";
                hasInit=false;
                updateUI(msg,color);
                return ;
            }

            HttpResponse step0Res = GiftCardUtil.federate(account,hashMap);
            String a= MapUtil.getStr(hashMap,"a");
            HttpResponse step1Res = GiftCardUtil.signinInit(account,a,step0Res,hashMap);
            if(503==step1Res.getStatus()){
                msg="初始化失败，请重试";
                color="red";
                hasInit=false;
                updateUI(msg,color);
                return ;
            }
            HttpResponse step2Res = GiftCardUtil.signinCompete(account,pwd,hashMap,step1Res,pre1,pre3);
            if(409==step2Res.getStatus()){
                String authType=JSONUtil.parse(step2Res.body()).getByPath("authType",String.class);
                if(authType.equals("hsa2")){
                    msg="您的Apple ID已受双重认证保护";
                    color="red";
                    hasInit=false;
                    updateUI(msg,color);
                    return ;
                }
            }else if(200==step2Res.getStatus()){
                hasInit=true;
            } else{
                System.out.println(step2Res.body());
                if(null!=JSONUtil.parse(step2Res.body()).getByPath("serviceErrors")){
                    msg=JSONUtil.parse(step2Res.body()).getByPath("serviceErrors.message",String.class);
                    color="red";
                    hasInit=false;
                    updateUI(msg,color);
                    return ;
                }
            }
            //step3 shop signin
            HttpResponse step3Res= GiftCardUtil.shopSignin(step2Res,pre1,hashMap);


            StringBuilder cookieBuilder = new StringBuilder();
            List<String> resCookies = step3Res.headerList("Set-Cookie");
            for(String item : resCookies){
                cookieBuilder.append(";").append(item);
            }
            cookieBuilder.append(";").append(hashMap.get("as_sfa_cookie"));
            Map<String,String> cookieMap=new HashMap<>();
            cookieMap=CookieUtils.setCookiesToMap(step3Res,cookieMap);
            hashMap.put("cookies",MapUtil.join(cookieMap,";","=",true));

            PropertiesUtil.setOtherConfig("cardAccount",account_pwd.getText());
            hasInit=true;
            updateUI(msg,color);
        }catch (Exception e){
            msg="登录失败";
            color="red";
            updateUI(msg,color);
        }
    }

    void updateUI(String finalMsg,String finalColor){
        Platform.runLater(new Task<Integer>() {
            @Override
            protected Integer call() {
            updateNodeStatus(false);
            alertMessage.setText(finalMsg);
            alertMessage.setTextFill(Paint.valueOf(finalColor));
            return 1;
            }
        });
    }
    protected void checkBalance(GiftCard giftCard,Map<String,Object> paras) {
        Date nowDate=new Date();
        //判断礼品卡的格式是否正确
        String regex = "X[a-zA-Z0-9]{15}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(giftCard.getGiftCardCode().toUpperCase());
        giftCard.setLogTime(sdf.format(nowDate));
        if (!matcher.matches()) {
            giftCard.setDataStatus("0");
            tableRefreshAndInsertLocal(giftCard,"输入代码不符合查询格式");
            return;
        }
        giftCard.setHasFinished(false);
        tableRefresh(giftCard,"正在查询...");
        ThreadUtil.sleep(1000);
        HttpResponse step4Res = GiftCardUtil.checkBalance(paras,giftCard.getGiftCardCode());
        if(503==step4Res.getStatus()){
            giftCard.setDataStatus("0");
            tableRefreshAndInsertLocal(giftCard,"当前服务不可用，请稍后重试");
        }else if(step4Res.getStatus()!=200){
            giftCard.setDataStatus("0");
            tableRefreshAndInsertLocal(giftCard,"余额查询失败");
        }else{
            JSON bodyJson= JSONUtil.parse(step4Res.body());
            String status=bodyJson.getByPath("head.status").toString();
            if(!Constant.SUCCESS.equals(status)){
                giftCard.setDataStatus("0");
                tableRefreshAndInsertLocal(giftCard,"余额查询失败");
                return;
            }
            String balance=bodyJson.getByPath("body.giftCardBalanceCheck.d.balance",String.class);
            String giftCardNumber=bodyJson.getByPath("body.giftCardBalanceCheck.d.giftCardNumber",String.class);
            if(null==balance){
                giftCard.setDataStatus("0");
                tableRefreshAndInsertLocal(giftCard,"这不是有效的礼品(或已兑换)");
            }else{
                giftCard.setDataStatus("1");
                giftCard.setBalance(balance);
                giftCard.setGiftCardNumber(giftCardNumber.split(";")[1]);
                tableRefreshAndInsertLocal(giftCard,"查询成功");
            }
        }
        giftCard.setHasFinished(true);
    }

    private void initAccountTableView(){
        seq.setCellValueFactory(new PropertyValueFactory<GiftCard,Integer>("seq"));
        giftCardCode.setCellValueFactory(new PropertyValueFactory<GiftCard,String>("giftCardCode"));
        balance.setCellValueFactory(new PropertyValueFactory<GiftCard,String>("balance"));
        logTime.setCellValueFactory(new PropertyValueFactory<GiftCard,String>("logTime"));
        giftCardNumber.setCellValueFactory(new PropertyValueFactory<GiftCard,String>("giftCardNumber"));
        note.setCellValueFactory(new PropertyValueFactory<GiftCard,String>("note"));
    }
    private void tableRefresh(GiftCard giftCard,String message){
        giftCard.setNote(message);
        accountTableView.refresh();
    }

    public void stopTaskButtonAction(ActionEvent actionEvent) {

    }
    protected void updateNodeStatus(boolean status){
        countryBox.setDisable(status);
        account_pwd.setDisable(status);
        loginBtn.setDisable(status);
        executeButton.setDisable(status);
    }
    @Override
    public void accountHandler(GiftCard giftCard) {
        checkBalance(giftCard, hashMap);
    }
    @FXML
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> items=new ArrayList<>(super.menuItem) ;
        super.onContentMenuClick(contextMenuEvent,accountTableView,items);
    }
}

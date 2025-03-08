package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.GiftCard;
import com.sgswit.fx.utils.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author DeZh
 * @title: GiftCardBalanceCheckController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/2714:40
 */
public class GiftCardBalanceCheckController extends CustomTableView<GiftCard> {

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
    public ComboBox<Map<String, String>> countryBox;
    @FXML
    public TextField account_pwd;
    @FXML
    public Button executeButton;
    @FXML
    public Label alertMessage;
    @FXML
    public Button loginBtn;

    private ObservableList<GiftCard> accountList = FXCollections.observableArrayList();

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Map<String, Map<String,Object>> loginCookiesMap = new ConcurrentHashMap<>();
    private final Map<String, GiftCard> waitMap = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private static String redColor = "red";
    private static String successColor = "#238142";
    private static ScheduledExecutorService scheduledExecutorService;
    private static ScheduledFuture scheduledFuture;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.GIFTCARD_BALANCE.getCode())));
        getCountry();
        String cardAccount = PropertiesUtil.getOtherConfig("cardAccount");
        account_pwd.setText(cardAccount);
        // 注册粘贴事件的监听器
        account_pwd.setOnContextMenuRequested((ContextMenuEvent event) -> {
        });
        account_pwd.setOnKeyReleased(event -> {
            if (event.isShortcutDown()) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                String content = clipboard.getString().replaceAll("\t", " ");
                account_pwd.setText(content);
            }
        });
        if (StringUtils.isEmpty(account_pwd.getText())) {
            alertMessage.setLabelFor(loginBtn);
            alertMessage.setText("等待初始化....");
        } else {
            ThreadUtil.execAsync(() -> {
                try {
                    loginAndInit();
                } catch (Exception e) {

                }
            });
        }
        super.initialize(url, resourceBundle);
    }

    private void getCountry() {
        String country = ResourceUtil.readUtf8Str("data/giftCard_query_support_country.json");
        for (Object o : JSONUtil.parseArray(country)) {
            JSONObject jsonObject = (JSONObject) o;
            countryBox.getItems().add(new HashMap<>() {{
                put("name", jsonObject.getStr("name"));
                put("code", jsonObject.getStr("code"));
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
                ThreadUtil.execAsync(() -> {
                    try {
                        loginAndInit();
                    } catch (Exception e) {

                    }
                });
            }
        });
    }


    @FXML
    protected void onAccountInputBtnClick(ActionEvent actionEvent) throws IOException {
        if (StringUtils.isEmpty(account_pwd.getText()) || loginCookiesMap.isEmpty()) {
            alert("请输入一个AppleID作为初始化，账号格式为：账号----密码", Alert.AlertType.ERROR);
            return;
        }
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
        if (null == c.getData() || "".equals(c.getData())) {
            return;
        }
        String[] accountPwdArray = AccountImportUtil.parseAccountAndPwd(account_pwd.getText());

        String[] lineArray = c.getData().split("\n");
        for (String item : lineArray) {
            if (StringUtils.isEmpty(item)) {
                continue;
            }
            GiftCard giftCard = new GiftCard();
            giftCard.setSeq(accountList.size() + 1);
            giftCard.setPwd(accountPwdArray[1]);
            giftCard.setAccount(accountPwdArray[0]);
            giftCard.setGiftCardCode(StringUtils.deleteWhitespace(item));
            accountList.add(giftCard);
        }
        initAccountTableView();
        accountTableView.setItems(accountList);
        super.accountList = accountList;
        setAccountNumLabel();
        scrollToLastRow();
    }


    @FXML
    public void onClickLoginBtn(ActionEvent actionEvent) {
        ThreadUtil.execAsync(() -> {
            try {
                loginAndInit();
            } catch (Exception e) {

            }
        });
    }

    @Override
    public boolean executeButtonActionBefore() {
        if (StringUtils.isEmpty(account_pwd.getText()) || loginCookiesMap.isEmpty()) {
            alert("请输入一个AppleID作为初始化，账号格式为：账号----密码", Alert.AlertType.ERROR);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 　* 登录并初始化
     *
     * @param  　* @return cn.hutool.http.HttpResponse
     *         　* @throws
     *         　* @author DeZh
     *         　* @date 2023/11/1 11:15
     */
    protected void loginAndInit() {
        String account = null;
        String pwd = null;
        try {
            Map<String,Object> authMap=new HashMap<>();
            boolean f = false;
            //校验账号格式是否正确
            if (!StringUtils.isEmpty(account_pwd.getText())) {
                String[] its = AccountImportUtil.parseAccountAndPwd(account_pwd.getText());
                if (its.length == 2) {
                    f = true;
                    account = its[0];
                    pwd = its[1];
                }
            }
            if (!f) {
                Platform.runLater(new Task<Integer>() {
                    @Override
                    protected Integer call() {
                        alert("请输入一个AppleID作为初始化，账号格式为：账号----密码", Alert.AlertType.ERROR);
                        return 1;
                    }
                });
                return;
            }
            updateNodeStatus(true);
            String countryCode = countryBox.getSelectionModel().getSelectedItem().get("code");
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
            if (pre1.getStatus() != 303) {
                updateUI("初始化失败，请重试", redColor);
                return;
            }

            //https://secure4.store.apple.com/shop/giftcard/balance
            HttpResponse pre2 = GiftCardUtil.shopPre2(pre1);
            if (pre2.getStatus() != 302) {
                updateUI("初始化失败，请重试", redColor);
                return;
            }

            //https://secure4.store.apple.com/shop/signIn?ssi=1AAABiatkunsgRa-aWEWPTDH2TWsHul_CZ2TC62v9QxcThhc-EPUrFW8AAAA3aHR0cHM6Ly9zZWN1cmU0LnN0b3JlLmFwcGxlLmNvbS9zaG9wL2dpZnRjYXJkL2JhbGFuY2V8fAACAf0PkQUMMDk-ffBr4IVwBmhKDAsCeTbIe2k-7oOanvAP
            HttpResponse pre3 = GiftCardUtil.shopPre3(pre1, pre2);
            authMap = GiftCardUtil.jXDocument(pre2, pre3, authMap);
            HttpResponse step0Res = GiftCardUtil.federate(account, authMap);
            if (200 != step0Res.getStatus()) {
                updateUI("初始化失败，请重试", redColor);
                return;
            }
            HttpResponse step1Res = GiftCardUtil.signinInit(account, step0Res, authMap);
            if (200 != step1Res.getStatus()) {
                updateUI("初始化失败，请重试", redColor);
                return;
            }
            HttpResponse step2Res = GiftCardUtil.signinCompete(account, pwd, authMap, step1Res, pre1, pre3);
            if (409 == step2Res.getStatus()) {
                String authType = JSONUtil.parse(step2Res.body()).getByPath("authType", String.class);
                if ("hsa2".equals(authType)) {
                    updateUI("您的Apple ID已受双重认证保护", redColor);
                    return;
                }
            } else if (200 == step2Res.getStatus()) {

            } else {
                StringBuffer m = new StringBuffer();
                String serviceErrors = JSONUtil.parse(step2Res.body()).getByPath("serviceErrors", String.class);
                if (null != serviceErrors) {
                    JSONArray jsonArray = JSONUtil.parseArray(serviceErrors);
                    Iterator iterator = jsonArray.iterator();
                    while (iterator.hasNext()) {
                        JSONObject jsonObject = (JSONObject) iterator.next();
                        m.append(jsonObject.getStr("message"));
                        m.append(";");
                    }
                    updateUI(m.toString(), redColor);
                    return;
                }
            }
            //step3 shop signin
            HttpResponse step3Res = GiftCardUtil.shopSignin(step2Res, pre1, authMap);
            StringBuilder cookieBuilder = new StringBuilder();
            List<String> resCookies = step3Res.headerList("Set-Cookie");
            for (String item : resCookies) {
                cookieBuilder.append(";").append(item);
            }
            cookieBuilder.append(";").append(authMap.get("as_sfa_cookie"));
            Map<String, String> cookieMap = new HashMap<>();
            cookieMap = CookieUtils.setCookiesToMap(step3Res, cookieMap);
            authMap.put("cookies", MapUtil.join(cookieMap, ";", "=", true));

            PropertiesUtil.setOtherConfig("cardAccount", account_pwd.getText());
            updateUI("初始化成功，下次启动将自动执行初始化", successColor);

            loginCookiesMap.put(IdUtil.fastSimpleUUID(),authMap);
        } catch (ServiceException e) {
            updateUI(e.getMessage(), redColor);
        } catch (Exception e) {
            updateUI("登录失败", redColor);
        }
    }

    void updateUI(String finalMsg, String finalColor) {
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

    protected void checkBalance(GiftCard giftCard) {
        //开启任务
        timerStart();
        Date nowDate = new Date();
        if (!StrUtils.giftCardCodeVerify(giftCard.getGiftCardCode())) {
            giftCard.setDataStatus("0");
            tableRefreshAndInsertLocal(giftCard, "输入代码不符合查询格式");
            return;
        }
        giftCard.setLogTime(sdf.format(nowDate));
        giftCard.setHasFinished(false);
        if(giftCard.getFailCount()==0){
            setAndRefreshNote(giftCard, "正在查询...");
        }else{
            setAndRefreshNote(giftCard, "查询失败，正在进行"+giftCard.getFailCount()+1+"次查询...");
        }

        ThreadUtil.sleep(100);
        if(loginCookiesMap.isEmpty()){
            setAndRefreshNote(giftCard, "登录信息失效，正在重新登录...");
            login();
            checkBalance(giftCard);
        }else {
            Object[] entries = loginCookiesMap.entrySet().toArray();
            Map.Entry<String, Map<String,Object>> entry = (Map.Entry<String, Map<String, Object>>) entries[random.nextInt(entries.length)];
            HttpResponse step4Res = GiftCardUtil.checkBalance(entry.getValue(), giftCard.getGiftCardCode());
            if (step4Res.getStatus() != 200) {
                if (step4Res.getStatus() == 541) {
                    loginCookiesMap.remove(entry.getKey());
                    checkBalance(giftCard);
                    return;
                } else {
                    if (giftCard.getFailCount() > 10) {
                        throw new ServiceException("余额查询失败，请稍后重试！");
                    } else {
                        giftCard.setFailCount(giftCard.getFailCount() + 1);
                    }
                    checkBalance(giftCard);
                }
            }


            JSON bodyJson = JSONUtil.parse(step4Res.body());
            try {
                String status = bodyJson.getByPath("head.status", String.class);
                if (Constant.REDIRECT_CODE.equals(status)) {
                    if (giftCard.getFailCount() > 10) {
                        throw new ServiceException("余额查询失败，请稍后重试！");
                    } else {
                        giftCard.setFailCount(giftCard.getFailCount() + 1);
                    }
                    checkBalance(giftCard);
                } else if (!Constant.SUCCESS.equals(status)) {
                    throw new ServiceException("余额查询失败，请稍后重试！");
                } else {
                    giftCard.setDataStatus("1");
                    Object giftCardBalanceError = bodyJson.getByPath("body.giftCardBalanceCheck.t.giftCardBalanceError.microEvents");
                    if (null != giftCardBalanceError) {
                        JSONArray jsonArray = JSONUtil.parseArray(giftCardBalanceError);
                        String message = "";
                        for (Object object : jsonArray) {
                            JSONObject jsonObject = (JSONObject) object;
                            if ("transaction.gc_balance.alert.invalid_giftcard".equals(jsonObject.getStr("value"))) {
                                message = message + "输入的礼品卡无效；";
                            } else if ("transaction.gc_balance.alert.invalid_country_giftcard".equals(jsonObject.getStr("value"))) {
                                String countryCode = countryBox.getSelectionModel().getSelectedItem().get("code");
                                message = message + "此代码不属于【" + DataUtil.getNameByCountryCode(countryCode) + "】地区；";
                            }
                        }
                        setAndRefreshNote(giftCard, message);
                    } else {
                        String balance = bodyJson.getByPath("body.giftCardBalanceCheck.d.balance", String.class);
                        String giftCardNumber = bodyJson.getByPath("body.giftCardBalanceCheck.d.giftCardNumber", String.class);
                        if (null == balance) {
                            setAndRefreshNote(giftCard, "此代码已被兑换");
                        } else {
                            giftCard.setBalance(balance);
                            giftCard.setGiftCardNumber(giftCardNumber.split(";")[1]);
                            setAndRefreshNote(giftCard, "查询成功.");
                        }
                    }
                }
            } catch (Exception e) {
                throw new ServiceException("余额查询失败，请稍后重试！");
            }
        }

    }

    public void login() {
        try {
            //判断是否正在登录
            Map<String, Object> loginMap = new HashMap<>(10);
            String account = null;
            String pwd = null;
            if (!StringUtils.isEmpty(account_pwd.getText())) {
                String[] its = AccountImportUtil.parseAccountAndPwd(account_pwd.getText());
                if (its.length == 2) {
                    account = its[0];
                    pwd = its[1];
                }
            }
            String countryCode = countryBox.getSelectionModel().getSelectedItem().get("code");
            //https://secure.store.apple.com/shop/giftcard/balance
            HttpResponse pre1 = GiftCardUtil.shopPre1(countryCode);
            if (303 != pre1.getStatus()) {
                return ;
            }
            ThreadUtil.sleep(500);
            //https://secure4.store.apple.com/shop/giftcard/balance
            HttpResponse pre2 = GiftCardUtil.shopPre2(pre1);
            if (302 != pre2.getStatus()) {
                return ;
            }
            ThreadUtil.sleep(200);
            //https://secure4.store.apple.com/shop/signIn?ssi=1AAABiatkunsgRa-aWEWPTDH2TWsHul_CZ2TC62v9QxcThhc-EPUrFW8AAAA3aHR0cHM6Ly9zZWN1cmU0LnN0b3JlLmFwcGxlLmNvbS9zaG9wL2dpZnRjYXJkL2JhbGFuY2V8fAACAf0PkQUMMDk-ffBr4IVwBmhKDAsCeTbIe2k-7oOanvAP
            HttpResponse pre3 = GiftCardUtil.shopPre3(pre1, pre2);
            loginMap = GiftCardUtil.jXDocument(pre2, pre3, loginMap);
            ThreadUtil.sleep(200);
            HttpResponse step0Res = GiftCardUtil.federate(account, loginMap);
            ThreadUtil.sleep(200);
            HttpResponse step1Res = GiftCardUtil.signinInit(account, step0Res, loginMap);
            ThreadUtil.sleep(200);
            HttpResponse step2Res = GiftCardUtil.signinCompete(account, pwd, loginMap, step1Res, pre1, pre3);
            if (409 == step2Res.getStatus()) {
                String authType = JSONUtil.parse(step2Res.body()).getByPath("authType", String.class);
                if ("hsa2".equals(authType)) {
                    return ;
                }
            } else if (200 != step2Res.getStatus()) {
                if (null != JSONUtil.parse(step2Res.body()).getByPath("serviceErrors")) {
                    return ;
                }
            }
            //step3 shop signin
            ThreadUtil.sleep(200);
            HttpResponse step3Res = GiftCardUtil.shopSignin(step2Res, pre1, loginMap);
            StringBuilder cookieBuilder = new StringBuilder();
            List<String> resCookies = step3Res.headerList("Set-Cookie");
            for (String item : resCookies) {
                cookieBuilder.append(";").append(item);
            }
            cookieBuilder.append(";").append(loginMap.get("as_sfa_cookie"));
            Map<String, String> cookieMap = new HashMap<>();
            cookieMap = CookieUtils.setCookiesToMap(step3Res, cookieMap);
            loginMap.put("cookies", MapUtil.join(cookieMap, ";", "=", true));
            PropertiesUtil.setOtherConfig("cardAccount", account_pwd.getText());
            loginCookiesMap.put(IdUtil.fastSimpleUUID(),loginMap);
        } catch (Exception e) {

        }
    }

    private void initAccountTableView() {
        seq.setCellValueFactory(new PropertyValueFactory<GiftCard, Integer>("seq"));
        giftCardCode.setCellValueFactory(new PropertyValueFactory<GiftCard, String>("giftCardCode"));
        balance.setCellValueFactory(new PropertyValueFactory<GiftCard, String>("balance"));
        logTime.setCellValueFactory(new PropertyValueFactory<GiftCard, String>("logTime"));
        giftCardNumber.setCellValueFactory(new PropertyValueFactory<GiftCard, String>("giftCardNumber"));
        note.setCellValueFactory(new PropertyValueFactory<GiftCard, String>("note"));
    }

    protected void updateNodeStatus(boolean status) {
        countryBox.setDisable(status);
        account_pwd.setDisable(status);
        loginBtn.setDisable(status);
        executeButton.setDisable(status);
    }

    @Override
    public void accountHandler(GiftCard giftCard) {
        checkBalance(giftCard);
    }

    @FXML
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> items = new ArrayList<>(super.menuItem);
        super.onContentMenuClick(contextMenuEvent, accountTableView, items);
    }

    @Override
    public void closeStageActionBefore() {
        loginCookiesMap.clear();
        //关闭任务
        scheduledFuture.cancel(true);
        //关闭线程池
        scheduledExecutorService.shutdown();
    }

    private void timerStart() {
        if (null == scheduledExecutorService || scheduledExecutorService.isShutdown()) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        }
        // 创建一个定时任务，延迟0秒执行，之后每10秒执行一次
        scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            login();
        }, 0, 7, TimeUnit.SECONDS);
    }
    @Override
    protected void stopExecutorService(){
        if(null==threadPoolExecutor || threadPoolExecutor.isShutdown()){

        }else{
            //方法会尝试立即停止所有正在执行的任务，并返回等待执行的任务列表
            threadPoolExecutor.shutdownNow();
        }
        //关闭任务
        scheduledFuture.cancel(true);
        //关闭线程池
        scheduledExecutorService.shutdown();
    }
}

package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CommonView;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.exception.ServiceException;
import com.sgswit.fx.controller.iTunes.vo.giftCard.AccountManager;
import com.sgswit.fx.controller.iTunes.vo.giftCard.SessionManager;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.GiftCard;
import com.sgswit.fx.utils.*;
import com.sgswit.fx.utils.cache.CacheDataUtil;
import com.sgswit.fx.utils.cache.DataUtil;
import com.sgswit.fx.utils.sign.CrossPlatformAesUtil;
import com.sgswit.fx.utils.stage.StageToSystemTrayUtil;
import com.sgswit.fx.utils.web.GiftCardUtil;
import com.sgswit.fx.utils.web.PoWSolver;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    public TextField txtGiftCardLoadAccountTextField;
    @FXML
    public Button executeButton;
    @FXML
    public Button loginBtn;

    @FXML
    public CheckBox enableScheduleCheckBox;

    @FXML
    public CheckBox balanceAlertCheckBox;

    @FXML
    public TextField intervalField;
    /**
     * 启停任务按钮
     */
    @FXML
    public Button startStopButton;
    /**
     * 直接执行按钮
     */
    @FXML
    public Button executeNowButton;
    /**
     * 导入定时任务礼品卡按钮
     */
    @FXML
    public Button importScheduleCardsButton;
    /**
     * 定时任务国家下拉选择
     */
    @FXML
    public ComboBox<Map<String, String>> scheduleCountryComboBox;
    /**
     * 定时任务进程消息
     */
    @FXML
    public Label processMessageLabel;
    @FXML
    public Label sessionCountLabel;
    /**
     * 定时任务table
     */
    @FXML
    public TableView<GiftCard> scheduleTableView;
    @FXML
    public Label scheduledProcessMessage;

    @FXML
    public TabPane tabPane;

    private ObservableList<GiftCard> scheduleAccountList = FXCollections.observableArrayList();

    private ObservableList<GiftCard> accountList = FXCollections.observableArrayList();

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static int lastSelectedCountryIndex = 0;
    private static  Queue<SessionManager.SessionInfo> scheduleSessions = new ConcurrentLinkedQueue<>();

    private static String countryCode="US";
    private static String scheduleCountryCode="US";
    //定时查卡
    private static ThreadPoolExecutor scheduledExecutor=new  ThreadPoolExecutor(4, 4, 30L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    /**
     * 定时任务服务
     */
    private CountdownService scheduledService = new CountdownService();
    /**
     * 定时任务运行标识
     */
    private boolean scheduledRunning = false;

    //导入的多个登录ID
    private static final Queue<AccountManager.AccountForQuery> accountsForQueryQueue = new ConcurrentLinkedQueue<>();
    // 常量配置
    private static final int MAX_SESSION_COUNT = 40;
    private static final int MAX_CONCURRENT_LOGIN = 3;

    private static final long SCHEDULER_INTERVAL_MS = 1000;
    // 核心管理器
    private final SessionManager sessionManager = new SessionManager();
    private final AccountManager accountManager = new AccountManager();

    // 并发控制
    private final AtomicInteger activeLoginCount = new AtomicInteger(0);
    private final AtomicInteger activeTaskCount = new AtomicInteger(0);
    private final Semaphore loginSemaphore = new Semaphore(MAX_CONCURRENT_LOGIN);
    private final AtomicBoolean allLoginFailed = new AtomicBoolean(false);
    private final HashSet<String> scheduleQueryCardRunningLogin = new HashSet<>();

    // 执行器服务
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduleTask;
    private final ExecutorService loginExecutor = Executors.newCachedThreadPool();
    private final Set<Future<?>> loginRunningTasks = ConcurrentHashMap.newKeySet();

    private static boolean hasLoginRunning=false;
    private static String failureMessage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.GIFTCARD_BALANCE.getCode())));
        getCountry();
        //清空备用查询账户集合
        accountsForQueryQueue.clear();
        parseTxtGiftCardLoadAccount(CrossPlatformAesUtil.decryptWithCompression(PropertiesUtil.getOtherConfig("txtGiftCardLoadAccount")));
        // 注册粘贴事件的监听器
        txtGiftCardLoadAccountTextField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // 精确匹配 Ctrl+V / Cmd+V
            if (event.isShortcutDown() && event.getCode() == KeyCode.V) {
                event.consume(); // 阻止默认粘贴行为
                Clipboard clipboard = Clipboard.getSystemClipboard();
                if (clipboard.hasString()) {
                    String content = clipboard.getString().replaceAll("\t", " ");
                    if (StrUtil.isNotEmpty(content)) {
                        Platform.runLater(() -> {
                            txtGiftCardLoadAccountTextField.replaceSelection(content);
                            String[] its = AccountImportUtil.parseAccountAndPwd(content);
                            if (its.length >= 2) {
                                accountsForQueryQueue.offer(new AccountManager.AccountForQuery(its[0],its[1]));
                            }
                            AccountManager.addAccountForQueryQueue(accountsForQueryQueue);
                        });
                    }
                }
            }else{
                String text = txtGiftCardLoadAccountTextField.getText();
                if (StrUtil.isNotEmpty(text)) {
                    Platform.runLater(() -> {
                        String[] its = AccountImportUtil.parseAccountAndPwd(text);
                        if (its.length >= 2) {
                            accountsForQueryQueue.offer(new AccountManager.AccountForQuery(its[0],its[1]));
                        }
                        AccountManager.addAccountForQueryQueue(accountsForQueryQueue);
                    });
                }
            }
        });
        if (CollectionUtil.isEmpty(accountsForQueryQueue)) {
            updateUI("等待初始化....","0","");
        }
        // 定时查询礼品卡相关组件初始化
        scheduleTableViewInitialize();
        //初始化已保存的sessions
        SessionManager.setSessions(CacheDataUtil.getQueryCardSessions());

        super.initialize(url, resourceBundle);
    }

    /**
     * 加载国家信息
     */
    private void getCountry() {
        String country = ResourceUtil.readUtf8Str("data/giftCard_query_support_country.json");
        for (Object o : JSONUtil.parseArray(country)) {
            JSONObject jsonObject = (JSONObject) o;
            countryBox.getItems().add(new HashMap<>() {{
                put("name", jsonObject.getStr("name"));
                put("code", jsonObject.getStr("code"));
            }});
            scheduleCountryComboBox.getItems().add(new HashMap<>() {{
                put("name", jsonObject.getStr("name"));
                put("code", jsonObject.getStr("code"));
            }});
        }
        //默认美国
        countryBox.getSelectionModel().select(0);
        scheduleCountryComboBox.getSelectionModel().select(0);

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
        //礼品卡查询 国家信息变更监听
        countryBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {

                countryCode= countryBox.getSelectionModel().getSelectedItem().toString();
                ThreadUtil.execAsync(() -> {
                    try {
                        setLoginBtnStatus(true);
                        startScheduler(true);
                    } catch (Exception e) {
                    }
                });
            }
        });
        //定时礼品卡查询 国家信息变更监听
        scheduleCountryComboBox.converterProperty().set(new StringConverter<>() {
            @Override
            public String toString(Map<String, String> map) {
                return map.get("name");
            }

            @Override
            public Map<String, String> fromString(String string) {
                return null;
            }
        });

        lastSelectedCountryIndex = scheduleCountryComboBox.getSelectionModel().getSelectedIndex();

        scheduleCountryComboBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.intValue() == lastSelectedCountryIndex) {
                return;
            }
            boolean confirmed = CommonView.showConfirmationDialog("提示", "确定更改国家吗？");
            if (confirmed) {
                lastSelectedCountryIndex = newValue.intValue();
                scheduleCountryCode =scheduleCountryComboBox.getItems().get(lastSelectedCountryIndex).get("code");
                scheduleQueryCardLogin();
            } else {
                Platform.runLater(() ->scheduleCountryComboBox.getSelectionModel().select(lastSelectedCountryIndex));
            }
        });
    }

    /**
     * 导入礼品卡操作
     * @param actionEvent
     * @throws IOException
     */
    @FXML
    protected void onAccountInputBtnClick(ActionEvent actionEvent) throws IOException {
        if (!executeButtonActionBefore()) {
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
        String[] lineArray = c.getData().split("\n");
        for (String item : lineArray) {
            if (StringUtils.isEmpty(item)) {
                continue;
            }
            GiftCard giftCard = new GiftCard();
            giftCard.setSeq(accountList.size() + 1);
            giftCard.setGiftCardCode(StringUtils.deleteWhitespace(item));
            accountList.add(giftCard);
        }
        initAccountTableView();
        accountTableView.setItems(accountList);
        super.accountList = accountList;
        setAccountNumLabel();
        scrollToLastRow();
        switchToTableView(0);
    }

    /**
     *登录
     */
    @FXML
    public void onClickLoginBtn(ActionEvent actionEvent) {
        if (!executeButtonActionBefore()) {

        }else{
            if (accountManager.getAvailableAccounts().size()==0 ){
                accountManager.addAccountForQueryQueue(accountsForQueryQueue);
            }
            ThreadUtil.execAsync(() -> {
                setLoginBtnStatus(true);
                startScheduler(true);
            });
        }
    }

    @Override
    public boolean executeButtonActionBefore() {
        if (accountsForQueryQueue.size()==0) {
            alert("请输入一个AppleID作为初始化，账号格式为：账号----密码", Alert.AlertType.ERROR);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 登录并初始化
     */
    protected Map<String, Object> loginAndInit(AccountManager.AccountForQuery accountForQuery,Boolean scheduleFlag,String scheduleCountryCode) throws InterruptedException {
        String accountName=accountForQuery.getTxtAccount();
        String password=accountForQuery.getTxtPassword();
        try {
            //校验账号格式是否正确
            if(accountsForQueryQueue.size()==0){
                return null;
            }
            Map<String,Object> authParas=new HashMap<>();
            String countryCodeForLogin=scheduleFlag?scheduleCountryCode:countryCode;
            HttpResponse initBalanceResponse=GiftCardUtil.initBalance(countryCodeForLogin);
            if (initBalanceResponse.getStatus() != 303) {
                accountForQuery.setPasswordError(false);
                return null;
            }


            authParas.put("as_pcts_cookies", CookieUtils.getSomeCookieFromHeader(initBalanceResponse,"as_pcts"));
            HttpResponse initBalanceWithTunesResponse=GiftCardUtil.initBalanceWithTunes(initBalanceResponse);
            if (initBalanceWithTunesResponse.getStatus() != 302) {
                accountForQuery.setPasswordError(false);
                return null;
            }
            String location=initBalanceWithTunesResponse.header("Location");
            String locationBase=location.substring(0,location.indexOf("shop")-1);
            authParas.put("locationBase",locationBase);
            HttpResponse shopSignInInitResponse=GiftCardUtil.shopSignInInit(initBalanceWithTunesResponse,authParas);
            Map<String, Object> parseShopSignInInitResponseDocumentMap=GiftCardUtil.parseShopSignInInitResponseDocument(shopSignInInitResponse);
            for (Map.Entry<String, Object> entry : parseShopSignInInitResponseDocumentMap.entrySet()) {
                authParas.put(entry.getKey(), entry.getValue());
            }
            GiftCardUtil.authorizeSignin(initBalanceWithTunesResponse,authParas);

            HttpResponse shldBtCkGeneratorGetResponse=GiftCardUtil.shldBtCkGenerator(initBalanceResponse,initBalanceWithTunesResponse,shopSignInInitResponse,null,"get",authParas);
            JSONObject shldBtJsonObject= JSONUtil.parseObj(shldBtCkGeneratorGetResponse.body());
            // 添加 flagskv 对象
            JSONObject flagskv = new JSONObject();
            flagskv.set("patSkip", true);
            shldBtJsonObject.set("flagskv", flagskv);
            Map<String,Object> solvePoWMap= PoWSolver.solvePoW(shldBtJsonObject.getInt("low"),shldBtJsonObject.getInt("high")
                    ,shldBtJsonObject.getInt("parts"),shldBtJsonObject.getBigInteger("result"),shldBtJsonObject.getLong("timeout"));
            // 添加 number 和 took
            shldBtJsonObject.set("number", solvePoWMap.get("numbers"));
            shldBtJsonObject.set("took", solvePoWMap.get("took"));
            HttpResponse shldBtCkGeneratorPostResponse=GiftCardUtil.shldBtCkGenerator(initBalanceResponse,initBalanceWithTunesResponse,shopSignInInitResponse,shldBtJsonObject.toStringPretty(),"post",authParas);
            authParas.put("shld_bt_ck",CookieUtils.getSomeCookieFromHeader(shldBtCkGeneratorPostResponse,"shld_bt_ck"));

            GiftCardUtil.challenge(authParas);
            HttpResponse jslogResponse=GiftCardUtil.jslog(authParas);

            String aa_cookies=CookieUtils.getSomeCookieFromHeader(jslogResponse,"aa");
            authParas.put("aa_cookies",aa_cookies);
            HttpResponse authFederateResponse=GiftCardUtil.authFederate(accountName,authParas);
            HttpResponse authSigninInitResponse=GiftCardUtil.authSigninInit(accountName,authFederateResponse,authParas);

            String as_pcts_cookies=CookieUtils.getSomeCookieFromHeader(initBalanceResponse,"as_pcts");
            authParas.put("as_pcts_cookies",as_pcts_cookies);
            HttpResponse signinCompeteResponse=GiftCardUtil.signinCompete(accountName,password,authSigninInitResponse,authParas);
            if (409 == signinCompeteResponse.getStatus()) {
                String authType = JSONUtil.parse(signinCompeteResponse.body()).getByPath("authType", String.class);
                if ("hsa2".equals(authType)) {
                    accountForQuery.setPasswordError(true);
                    failureMessage="此Apple ID已开通双重认证，请更换Apple ID";
                    return null;
                }
            } else {
                StringBuffer m = new StringBuffer();
                String serviceErrors = JSONUtil.parse(signinCompeteResponse.body()).getByPath("serviceErrors", String.class);
                if (null != serviceErrors) {
                    JSONArray jsonArray = JSONUtil.parseArray(serviceErrors);
                    Iterator iterator = jsonArray.iterator();
                    while (iterator.hasNext()) {
                        JSONObject jsonObject = (JSONObject) iterator.next();
                        m.append(jsonObject.getStr("code"));
                        m.append("|");
                        m.append(jsonObject.getStr("message"));
                    }
                    accountForQuery.setPasswordError(true);
                    failureMessage=m.toString();
                    return null;
                }
            }

            HttpResponse idmsAuthxResponse= GiftCardUtil.idmsAuthx(initBalanceResponse,signinCompeteResponse,authParas);
            HttpResponse checkBalanceGetRs= GiftCardUtil.checkBalanceGet(idmsAuthxResponse ,location,locationBase );
            Document prodDoc = Jsoup.parse(checkBalanceGetRs.body());
            Elements initDataElement = prodDoc.select("script[id=init_data]");
            JSONObject meta = JSONUtil.parseObj(initDataElement.html());
            String x_as_actk = meta.getByPath("meta.h.x-as-actk",String.class);

            Map<String, Object> cookiesMap=new  HashMap<>();
            cookiesMap.put("countryCode",countryCodeForLogin);
            cookiesMap.put("x-as-actk", x_as_actk);
            cookiesMap.put("locationBase", MapUtil.getStr(authParas,"locationBase"));
            cookiesMap.put("x_aos_stk", MapUtil.getStr(authParas,"x_aos_stk"));
            cookiesMap.put("modelVersion", MapUtil.getStr(authParas,"modelVersion"));
            cookiesMap.put("syntax", MapUtil.getStr(authParas,"syntax"));
            cookiesMap.put("cookies", MapUtil.getStr(authParas,"cookies"));
            authParas.clear();
            if (scheduleFlag){
                sessionManager.updateSession(scheduleSessions);
                scheduleSessions.add(new SessionManager.SessionInfo(countryCodeForLogin,cookiesMap,accountForQuery.getTxtAccount()));
            }
            return cookiesMap;
        } catch (ServiceException e) {
            failureMessage=e.getMessage();
            accountManager.markLoginFailure(accountForQuery,false);
            throw e;
        } catch (Exception e) {
            failureMessage="登录失败，请稍后或更换后ID重试！";
            accountManager.markLoginFailure(accountForQuery,false);
            throw e;
        }finally {
            if (activeTaskCount.get()>0){
                activeTaskCount.decrementAndGet();
            }
            scheduleQueryCardRunningLogin.remove(accountForQuery.getAccountId());
        }
    }

    /**
     * 定时查卡登录
     */
    private void scheduleQueryCardLogin(){
        //随机获取
        if(AccountManager.getAvailableAccounts().size()==0){
            scheduleQueryCardLogin();
            return;
        }
        List<AccountManager.AccountForQuery> list = new ArrayList<>(AccountManager.getAvailableAccounts());
        int randomIndex = ThreadLocalRandom.current().nextInt(list.size());
        AccountManager.AccountForQuery account=  list.get(randomIndex);
        if(!scheduleQueryCardRunningLogin.contains(account.getAccountId())){
            scheduleQueryCardRunningLogin.add(account.getAccountId());
            ThreadUtil.execAsync(() -> loginAndInit(account,true,scheduleCountryCode));
        }
    }


    /**
     * 查询余额操作
     * @param giftCard
     * @param countryCode
     */
    protected void checkBalance(GiftCard giftCard,String countryCode) throws InterruptedException {
        //开启任务
        if((giftCard.isHasBalance() || giftCard.isRunning()) && giftCard.isScheduledFlag()){
            return;
        }
        giftCard.runningProperty().set(true);
        if (!StrUtils.giftCardCodeVerify(giftCard.getGiftCardCode())) {
            giftCard.setDataStatus("0");
            tableRefreshAndInsertLocal(giftCard, "输入代码不符合查询格式");
            return;
        }

        giftCard.setHasFinished(false);
        if(giftCard.getFailCount()>=20){
            giftCard.setDataStatus("0");
            setAndRefreshNote(giftCard, "查询失败，请稍后重试。");
            return;
        }else if(giftCard.getFailCount()==0){
            giftCard.setLogTime(DateUtil.now());
            setAndRefreshNote(giftCard, "正在查询...");
            giftCard.setFailCount(1);
        }else{
            int failCount=giftCard.getFailCount()+1;
            giftCard.setFailCount(failCount);
            setAndRefreshNote(giftCard, "查询失败，正在进行"+failCount+"次查询...");
        }

        ThreadUtil.sleep(200 + (long)(Math.random() * 200));

        checkBalanceInternal(giftCard, countryCode);
    }
    private void checkBalanceInternal(GiftCard giftCard, String countryCode) throws InterruptedException {
        SessionManager.SessionInfo session;
        //定时查看
        if (giftCard.isScheduledFlag()) {
            session=scheduleSessions.poll();
            if (session != null) {
                scheduleSessions.add(session);
            }else{
                scheduleQueryCardLogin();
            }
        } else {
            session =  sessionManager.acquireAvailableSession(countryCode);
        }
        if(null==session){
            setAndRefreshNote(giftCard, "正在登录...");
            giftCard.runningProperty().set(false);
            ThreadUtil.sleep(1500 + (long)(Math.random() * 500));
            checkBalanceInternal(giftCard, countryCode);
            return;
        }

        HttpResponse checkBalanceRes = GiftCardUtil.checkBalance(session.getCookies(), giftCard.getGiftCardCode());
        //设置查询次数
        giftCard.setQueryCount(giftCard.getQueryCount()+1);
        //设置已查询
        giftCard.runningProperty().set(false);
        if (checkBalanceRes.getStatus() != 200) {
            if(checkBalanceRes.getStatus() == 541) {
                //短时间内调用频繁，更换session重新查询
                sessionManager.updateSession(session.getId());
            }else {
                handleFailCount(giftCard);
            }
            ThreadUtil.sleep(300 + (long)(Math.random() * 500));
            checkBalance(giftCard, countryCode);
            return;
        }
        try {
            JSON bodyJson = JSONUtil.parse(checkBalanceRes.body());
            String status = bodyJson.getByPath("head.status", String.class);

            if (Constant.REDIRECT_CODE.equals(status)) {
                handleFailCount(giftCard);
                checkBalance(giftCard, countryCode);
            } else if (!Constant.SUCCESS.equals(status)) {
                giftCard.setDataStatus("0");
                throw new ServiceException("余额查询失败，请稍后重试！");
            } else {
                giftCard.setDataStatus("1");
                handleBalanceResult(giftCard, bodyJson, countryCode);
            }
        } catch (Exception e) {
            throw new ServiceException("余额查询失败，请稍后重试！");
        }
    }

    private void handleFailCount(GiftCard giftCard) {
        if (giftCard.getFailCount() > 20) {
            throw new ServiceException("余额查询失败，请稍后重试！");
        } else {
            giftCard.setFailCount(giftCard.getFailCount() + 1);
        }
    }

    private void handleBalanceResult(GiftCard giftCard, JSON bodyJson, String countryCode) {
        Object giftCardBalanceError = bodyJson.getByPath("body.giftCardBalanceCheck.t.giftCardBalanceError.microEvents");
        if (giftCardBalanceError != null) {
            JSONArray jsonArray = JSONUtil.parseArray(giftCardBalanceError);
            StringBuilder message = new StringBuilder();
            for (Object object : jsonArray) {
                JSONObject jsonObject = (JSONObject) object;
                switch (jsonObject.getStr("value")) {
                    case "transaction.gc_balance.alert.invalid_giftcard":
                        message.append("输入的礼品卡无效；");
                        break;
                    case "transaction.gc_balance.alert.invalid_country_giftcard":
                        message.append("此代码不属于【").append(DataUtil.getNameByCountryCode(countryCode)).append("】地区；");
                        break;
                }
            }
            setAndRefreshNote(giftCard, message.toString());
        } else {
            String balance = bodyJson.getByPath("body.giftCardBalanceCheck.d.balance", String.class);
            String giftCardNumber = bodyJson.getByPath("body.giftCardBalanceCheck.d.giftCardNumber", String.class);
            if (balance == null) {
                setAndRefreshNote(giftCard, "已被兑换或无效的代码");
            } else {
                if(extractMoneyValue(balance).compareTo(BigDecimal.ZERO)>0){
                    giftCard.hasBalanceProperty().set(true);
                    giftCard.setBalance(balance);
                    giftCard.setGiftCardNumber(giftCardNumber.split(";")[1]);
                    setAndRefreshNote(giftCard, "查询成功.");
                }else{
                    giftCard.setBalance(balance);
                    if(!StrUtil.isEmpty(giftCardNumber)){
                        giftCard.setGiftCardNumber(giftCardNumber.split(";")[1]);
                    }
                    setAndRefreshNote(giftCard, "查询成功,金额为："+balance);
                }
            }
        }
    }

    private void handleFinishAllData(TableView<GiftCard> tableView) {
        for(GiftCard giftCard:tableView.getItems()){
            giftCard.setHasFinished(true);
            giftCard.runningProperty().set(false);
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


    @Override
    public void accountHandler(GiftCard giftCard) {
        try {
            checkBalance(giftCard,countryCode);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }catch (Exception e){
            throw e;
        }
    }

    @FXML
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> items = new ArrayList<>(super.menuItem);
        super.onContentMenuClick(contextMenuEvent, accountTableView, items);
    }

    @FXML
    public void onScheduleTableClick(ContextMenuEvent contextMenuEvent) {
        List<String> items = new ArrayList<>(super.menuItem);
        super.onContentMenuClick(contextMenuEvent, scheduleTableView, items);
    }


    /**
     * 关闭当前页面前需要执行的方法
     */
    @Override
    public void closeStageActionBefore() {
        //关闭查卡登录任务
        shutdownSystem();
        //保存查卡session信息
        CacheDataUtil.saveSessions(sessionManager.getSessions());
        //关闭定时任务
        scheduledExecutor.shutdown();
        scheduledService.cancel();
    }

    public void startScheduler(boolean relogin) {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        allLoginFailed.set(false);

        scheduleTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                executeLoginCycle();
            } catch (Exception e) {
            }
        }, 0, SCHEDULER_INTERVAL_MS, TimeUnit.MILLISECONDS);
        long sessionSize = sessionManager.getSizeByCountry(countryCode);
        if(sessionSize==0){
            updateUI("🔄 ID登录中...", "0","");
        }else{
            if(relogin){
                updateUI("🔄 ID重新登录中...", String.valueOf(sessionSize),"");
            }else{
                updateUI("✅ 登录完成", String.valueOf(sessionSize),"2");
            }
        }

    }

    /**
     * 支持队列优先级的调度逻辑
     */
    private void executeLoginCycle() {
        try {
            //计算需要的session数量
            int sessionSize=sessionManager.getSizeByCountry(countryCode);
            long sessionsNeeded = MAX_SESSION_COUNT - sessionSize;
            if (sessionsNeeded > 0) {
                sessionCountLabel.setText(String.valueOf(sessionSize));
            } else {
                sessionCountLabel.setText(String.valueOf(MAX_SESSION_COUNT));
                return;
            }
            // 4. 计算任务数（使用修复后的逻辑）
            int tasksToSubmit = calculateTasks();
            if (tasksToSubmit <= 0) {
                return;
            }
            if (activeTaskCount.get()>=MAX_SESSION_COUNT ||
                    (activeTaskCount.get()>=1 && accountManager.getAvailableAccountsCount()<=3)){
                return;
            }
            activeTaskCount.getAndIncrement();

            // 5. 提交任务
            submitTasks(tasksToSubmit);

        } catch (Exception e) {
        }
    }


    /**
     * 检查停止条件
     */
    private boolean shouldStopLoginCycle() {
        int sessionSize=sessionManager.getSizeByCountry(countryCode);
        // 条件1: Session已满（关键修复）
        if (sessionSize >= MAX_SESSION_COUNT) {
            // 关键修复：不关闭调度器，只记录状态
            return true;
        }


        // 条件3: 所有账号都失败
        if (allLoginFailed.get()) {
            return true;
        }

        // 条件4: 没有可用账号
        if (accountManager.getAvailableAccountsCount()==0 ) {
            // Session未满但无账号，检查是否所有账号都处理完成
            if (accountManager.getProcessingAccounts().size()==0) {
                handleFinalStatus();
                return true;
            }
            return false; // 关键修复：返回false，允许继续检查
        }

        return false;
    }
    /**
     * 修复：最终状态处理
     */
    private void handleFinalStatus() {
        setLoginBtnStatus(false);
        long sessionSize = sessionManager.getSizeByCountry(countryCode);
        if (sessionSize == 0 && AccountManager.getAvailableAccountsCount()==0) {
            // 所有账号都失败
            allLoginFailed.set(true);
//            updateUI("❌ 所有账号登录失败", "0","1");
            updateUI(failureMessage, "0","1");
            shutdownScheduler();
        } else if (sessionSize < MAX_SESSION_COUNT) {
            // 关键修复：部分成功但Session未满，继续等待
            // 不关闭调度器，继续等待
        } else {
            // Session已满
            updateUI("✅ 登录完成", String.valueOf(sessionSize),"2");
            shutdownScheduler();
        }
    }
    /**
     * 基于队列优先级的任务计算
     */
    private int calculateTasks() {
        // 1. 检查Session需求
        int sessionsNeeded = MAX_SESSION_COUNT - sessionManager.getSizeByCountry(countryCode);
        if (sessionsNeeded <= 0) {
            return 0;
        }

        // 2. 检查可用账号
        if (accountManager.getAvailableAccountsCount()==0) {
            return 0;
        }

        // 3. 检查执行中的账户
        if (activeLoginCount.get()>=MAX_CONCURRENT_LOGIN) {
            return 0;
        }

        // 4. 检查许可可用性
        int availablePermits = loginSemaphore.availablePermits();
        if (availablePermits <= 0) {
            return 0;
        }

        // 5. 核心修复：正确的并发计算
        int calculatedTasks = calculateOptimalConcurrency(AccountManager.getAvailableAccountsCount(), sessionsNeeded);


        return calculatedTasks;
    }
    /**
     * 修复：计算最优并发数
     */
    private int calculateOptimalConcurrency(int availableAccounts, int sessionsNeeded) {
        // 规则1：账户少于3个时，只允许1个并发
        if (availableAccounts <= 3) {
            return Math.min(1, sessionsNeeded);
        }

        // 规则2：账户大于等于3个时，最大3个并发
        int byAccountCount = Math.min(availableAccounts, MAX_CONCURRENT_LOGIN);

        // 规则3：基于Session需求计算
        int bySessionNeed = sessionsNeeded;
        if (sessionsNeeded < 3) {
            bySessionNeed = sessionsNeeded; // 需要少于3个时，按需分配
        } else {
            bySessionNeed = Math.min(sessionsNeeded, MAX_CONCURRENT_LOGIN);
        }

        // 取最小值，确保不超过许可数
        int result = Math.min(byAccountCount, bySessionNeed);
        result = Math.min(result, loginSemaphore.availablePermits());

        if (accountManager.getProcessingAccounts().size()>=sessionsNeeded) {
            result=0;
        }else{
            result=sessionsNeeded-accountManager.getProcessingAccounts().size();
        }


        return Math.max(0, result);
    }
    /**
     * 基于队列优先级的任务提交
     */
    private void submitTasks(int maxTasks) {

        for (int i = 0; i < maxTasks; i++) {
            // 实时检查Session状态
            if (sessionManager.getSizeByCountry(countryCode) >= MAX_SESSION_COUNT) {
                activeTaskCount.decrementAndGet();
                break;
            }

            // 尝试获取许可
            if (!loginSemaphore.tryAcquire()) {
                activeTaskCount.decrementAndGet();
                break;
            }
            try {
                AccountManager.AccountForQuery account = accountManager.getNextAccount();
                if (account == null) {
                    loginSemaphore.release();
                    activeTaskCount.decrementAndGet();
                    break;
                }

                // 检查账户执行状态
                if (accountManager.getProcessingAccounts().contains(account.getAccountId())) {
                    loginSemaphore.release();
                    continue;
                }

                // 提交任务
                submitLoginTask(account);

            } catch (Exception e) {
                loginSemaphore.release();
                break;
            }
        }

    }

    /**
     * 提交单个登录任务
     */
    private boolean submitLoginTask(AccountManager.AccountForQuery account) {
        AccountManager.getProcessingAccounts().add(account.getAccountId());
        activeLoginCount.incrementAndGet();
        Future<?> future = loginExecutor.submit(() -> {
            boolean loginSuccess = false;
            boolean isPasswordError = false;
            try {
                SessionManager.SessionInfo session = performLogin(account);
                if (session != null) {
                    sessionManager.add(session);
                    accountManager.markLoginSuccess(account);
                    updateUI("✅ 登录成功", String.valueOf(sessionManager.getSizeByCountry(countryCode)),"2");
                    loginSuccess = true;
                    setLoginBtnStatus(false);
                } else {
                    isPasswordError = account.isPasswordError();
                }
            } catch (Exception e) {

            } finally {
                handleTaskCompletion(account, loginSuccess, isPasswordError);
            }
        });

        loginRunningTasks.add(future);
        return true;
    }

    /**
     * 处理任务完成
     */
    private void handleTaskCompletion(AccountManager.AccountForQuery account, boolean success, boolean isPasswordError) {
        try {
            if (success) {
                accountManager.markLoginSuccess(account);
            } else {
                accountManager.markLoginFailure(account, isPasswordError);
            }
        } finally {
            if (activeLoginCount.get()>0){
                activeLoginCount.decrementAndGet();
            }
            loginSemaphore.release();
            loginRunningTasks.remove(Thread.currentThread());
            accountManager.getProcessingAccounts().remove(account.getAccountId());
            //检查并提交下一个任务
            scheduler.schedule(() -> {
                if (activeLoginCount.get() == 0 && !shouldStopLoginCycle()) {
                    executeLoginCycle();
                }
            }, 100, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 执行登录逻辑
     */
    private SessionManager.SessionInfo performLogin(AccountManager.AccountForQuery account) {
        try {
            Map<String, Object> cookies = loginAndInit(account,false,"");
            if(null!= cookies) {
                return new SessionManager.SessionInfo(MapUtil.getStr(cookies,"countryCode"), cookies,account.getTxtAccount());
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            return null;
        }
    }


    // =================== 控制方法 ===================

    /**
     * 中断所有任务
     */
    public void interruptAllTasks() {
        for (Future<?> task : loginRunningTasks) {
            if (!task.isDone()) {
                task.cancel(true);
            }
        }
        activeLoginCount.set(0);
        AccountManager.clearProcessingAccounts();
        loginSemaphore.drainPermits();
        loginSemaphore.release(MAX_CONCURRENT_LOGIN);
        loginRunningTasks.clear();
    }

    /**
     * 安全关闭调度器
     */
    private void shutdownScheduler() {
        if (scheduleTask != null) {
            scheduleTask.cancel(false);
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    /**
     * 完全关闭系统
     */
    public void shutdownSystem() {
        interruptAllTasks();
        shutdownScheduler();
        if (loginExecutor != null) {
            loginExecutor.shutdown();
        }
    }

    // =================== UI更新方法 ===================
    private void updateUI(String processMessage,String sessionCount,String colorCode) {
        if (Platform.isFxApplicationThread()) {
            setLabelText(colorCode,sessionCount,processMessage);
        }else{
            Platform.runLater(() -> {
                setLabelText(colorCode,sessionCount,processMessage);
            });
        }
    }

    private void setLabelText(String colorCode,String sessionCount,String processMessage){
        processMessageLabel.setText(processMessage);
        processMessageLabel.setStyle("-fx-font-weight: bold;");
        if("1".equals(colorCode)) {
            processMessageLabel.setTextFill(Paint.valueOf("red"));
        }else if("2".equals(colorCode)) {
            processMessageLabel.setTextFill(Paint.valueOf("#238142"));
        }else {
            processMessageLabel.setTextFill(Paint.valueOf("#238142"));
        }
        sessionCountLabel.setText(sessionCount);
    }


    /**
     * 设置登录按钮状态
     * @param isLogin
     */
    private void setLoginBtnStatus(boolean isLogin) {
        hasLoginRunning=isLogin;
        if (Platform.isFxApplicationThread()) {
            loginBtn.setDisable(hasLoginRunning);
        }else{
            Platform.runLater(() -> {
                loginBtn.setDisable(hasLoginRunning);
            });
        }
    }

    private void parseTxtGiftCardLoadAccount(String txtGiftCardLoadAccountStr){
        if(StrUtil.isNotEmpty(txtGiftCardLoadAccountStr.trim())){
            String[] accList = txtGiftCardLoadAccountStr.split("\n");
            for(int i=0;i<accList.length;i++){
                String[] its = AccountImportUtil.parseAccountAndPwd(accList[i]);
                if (its.length >= 2) {
                    accountsForQueryQueue.offer(new AccountManager.AccountForQuery(its[0],its[1]));
                }
            }
            if (accountsForQueryQueue.size() ==1){
                AccountManager.AccountForQuery accountsForQuery=accountsForQueryQueue.peek();
                txtGiftCardLoadAccountTextField.setText(accountsForQuery.getTxtAccount()+"----"+accountsForQuery.getTxtPassword());
            }else {
                txtGiftCardLoadAccountTextField.setText("导入的登录ID数量："+accountsForQueryQueue.size());
                txtGiftCardLoadAccountTextField.setDisable(true);
                loginBtn.setDisable(true);
            }
            AccountManager.addAccountForQueryQueue(accountsForQueryQueue);
            if(accountsForQueryQueue.size()>0){
                startScheduler(false);
            }
        }else{
            AccountManager.addAccountForQueryQueue(accountsForQueryQueue);
            txtGiftCardLoadAccountTextField.setText("");
            txtGiftCardLoadAccountTextField.setDisable(false);
            loginBtn.setDisable(false);
            AccountManager.addAccountForQueryQueue(new ConcurrentLinkedQueue());
        }
    }

    /**
     *  导入多个登录ID
     */
    @Override
    public void openImportAccountView(List<String> formats,String title, String desc,Stage parentStage) {
        Stage stage = new Stage();
        Label descLabel = new Label(desc);
        descLabel.setWrapText(true);

        TextArea area = new TextArea();
        area.setPrefHeight(250);
        area.setPrefWidth(560);

        VBox vBox2 = new VBox();
        vBox2.setPadding(new Insets(0, 0, 0, 205));
        Button button = new Button("导入");
        button.setTextFill(Paint.valueOf("#067019"));
        button.setPrefWidth(150);
        button.setPrefHeight(50);

        area.setText(CrossPlatformAesUtil.decryptWithCompression(PropertiesUtil.getOtherConfig("txtGiftCardLoadAccount")));
        button.setOnAction(event -> {
            txtGiftCardLoadAccountTextField.setDisable(false);
            loginBtn.setDisable(false);
            accountsForQueryQueue.clear();
            String txtGiftCardLoadAccount=area.getText();
            parseTxtGiftCardLoadAccount(txtGiftCardLoadAccount);
            PropertiesUtil.setOtherConfig("txtGiftCardLoadAccount",CrossPlatformAesUtil.encryptWithCompression(txtGiftCardLoadAccount));
            if(null!=parentStage){
                StageToSystemTrayUtil.showWindow(parentStage);
            }
            stage.close();
        });
        vBox2.getChildren().addAll(button);

        VBox mainVbox = new VBox();
        mainVbox.setSpacing(20);
        mainVbox.setPadding(new Insets(20));
        mainVbox.getChildren().addAll(descLabel, area, vBox2);

        Group root = new Group(mainVbox);
        stage.setTitle(title);
        stage.setScene(new Scene(root, 600, 450));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        String logImg= PropertiesUtil.getConfig("softwareInfo.log.path");
        stage.getIcons().add(new Image(Objects.requireNonNull(this.getClass().getResource(logImg)).toString()));
        stage.initStyle(StageStyle.DECORATED);
        stage.showAndWait();
    }

    /****************** 定时查询礼品卡余额 **************/

    /**
     * 定时查询礼品卡相关组件初始化
     */
    private void scheduleTableViewInitialize(){
        balanceAlertCheckBox.setSelected(true);
        // 数据绑定
        ObservableList<TableColumn<GiftCard, ?>> columns = scheduleTableView.getColumns();
        for (TableColumn<GiftCard, ?> column : columns) {
            String id = column.getId().substring(2);
            // 序号自动增长
            if ("seq".equals(id)) {
                column.setCellFactory(new Callback() {
                    @Override
                    public Object call(Object param) {
                        TableCell cell = new TableCell() {
                            @Override
                            protected void updateItem(Object item, boolean empty) {
                                super.updateItem(item, empty);
                                this.setText(null);
                                this.setGraphic(null);
                                if (!empty) {
                                    int rowIndex = this.getIndex() + 1;
                                    this.setText(String.valueOf(rowIndex));
                                }
                            }
                        };
                        return cell;
                    }
                });
            }else{
                column.setCellValueFactory(new PropertyValueFactory(id));
            }
        }
        // 监听note变化,刷新table
        // 简化列表监听器
        scheduleAccountList.addListener((ListChangeListener<GiftCard>) change -> {
            // 只需要处理添加/移除，不需要监听单个属性变化
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    Platform.runLater(() -> {
                        scheduleTableView.refresh();
                    });
                }
            }
        });


        // 设置多选模式
        scheduleTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // 鼠标右键清空选中行
        scheduleTableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton() == MouseButton.PRIMARY) { // 左键点击事件
                if (event.getClickCount() == 1) {
                    double y = event.getY();
                    double headerHeight = scheduleTableView.lookup(".column-header-background").getBoundsInParent().getHeight();
                    double contentHeight = scheduleTableView.getItems().size() * 24;
                    // 如果点击了空行，取消所有选中
                    if (y > headerHeight + contentHeight) {
                        accountTableView.getSelectionModel().clearSelection();
                    }
                }
            }
        });

        // enableScheduleCheckBox 监听状态
        enableScheduleCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            boolean disable = !enableScheduleCheckBox.isSelected();
            balanceAlertCheckBox.setDisable(disable);
            intervalField.setDisable(disable);
            scheduleCountryComboBox.setDisable(disable);
            startStopButton.setDisable(disable);
            executeNowButton.setDisable(disable);
            importScheduleCardsButton.setDisable(disable);

            Paint textColor = disable ? Paint.valueOf("#808080") : Paint.valueOf("black");
        });
    }

    @FXML
    private void startStopExecute(){
        if (scheduledRunning) {
            scheduledProcessMessage.setText("未开始");
            scheduledProcessMessage.setTextFill(Paint.valueOf("#238142"));
            startStopButton.setText("开始计时");
            startStopButton.setTextFill(Paint.valueOf("#0F8DE2"));
            intervalField.setDisable(false);
            //关闭定时任务
            scheduledService.cancel();
            scheduledExecutor.shutdown();
            //所有数据置为已完成
            handleFinishAllData(scheduleTableView);
        }else {
            String intervalFieldText = intervalField.getText();
            if (StringUtils.isEmpty(intervalFieldText) || !NumberUtil.isInteger(intervalFieldText)) {
                alert("请输入正确执行间隔分钟数", Alert.AlertType.ERROR);
                return;
            }
            intervalField.setDisable(true);
            startStopButton.setText("停止");
            scheduledService.setCountdownSeconds(Integer.valueOf(intervalFieldText)*60);
            scheduledService.restart();
        }
        scheduledRunning=!scheduledRunning;
    }
    @FXML
    private void handleScheduledExecute() {
        if (CollUtil.isEmpty(scheduleAccountList)) {
            alert("请先导入定时查询的礼品卡", Alert.AlertType.ERROR);
            return;
        }
        for (GiftCard giftCard : scheduleAccountList) {
            if (!enableScheduleCheckBox.isSelected()) {
                break;
            }
            if (giftCard.isHasBalance()) {
                continue;
            }
            scheduledExecutor.submit(()->{
                try {
                    checkBalance(giftCard,scheduleCountryCode);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (giftCard.isHasBalance() && balanceAlertCheckBox.isSelected()){
                    // 播放提示音
                    SoundUtil.playSound();
                }
            });
        }
    }

    /**
     * 定时查询礼品卡导入
     * @param event
     * @throws IOException
     */
    @FXML
    private void handleImportScheduleCards(ActionEvent event) throws IOException {
        if (StringUtils.isEmpty(txtGiftCardLoadAccountTextField.getText())) {
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
        String[] lineArray = c.getData().split("\n");
        for (String item : lineArray) {
            if (StrUtil.isEmpty(item)) {
                continue;
            }
            GiftCard giftCard = new GiftCard();
            giftCard.setSeq(scheduleAccountList.size() + 1);
            giftCard.setGiftCardCode(StringUtils.deleteWhitespace(item));
            giftCard.scheduledFlagProperty().set(true);
            scheduleAccountList.add(giftCard);
            scrollToLastRow();
        }
        scheduleTableView.setItems(scheduleAccountList);
        if (!scheduleTableView.getItems().isEmpty()) {
            int lastIndex = scheduleTableView.getItems().size() - 1;
            scheduleTableView.scrollTo(lastIndex);
        }
        switchToTableView(1);
    }
    // 代码切换Tab
    private void switchToTableView(int tableIndex) {
        if (tableIndex >= 0 && tableIndex < tabPane.getTabs().size()) {
            tabPane.getSelectionModel().select(tableIndex);
        }
    }

    public void importAccountButtonAction(ActionEvent actionEvent) {
        String desc = "说明：\n" +
                "    1.格式为: " + AccountImportUtil.buildNote(List.of("account----pwd")) + ", 或空格分割\n" +
                "    2.一次可以输入多条账户信息，每条账户单独一行; 如果数据中包含“-”符号, 只能使用空格分割。";
        Button button = (Button) actionEvent.getSource();
        // 获取按钮所在的场景
        Scene scene = button.getScene();
        // 获取场景所属的舞台
        Stage stage = (Stage) scene.getWindow();
        openImportAccountView(List.of("account----pwd"),"导入账号", desc,stage);
    }

    class CountdownService extends Service<Void> {
        private int countdownSeconds = 60;
        private final ScheduledExecutorService scheduler;
        private ScheduledFuture<?> countdownFuture;

        public CountdownService() {
            this.scheduler = Executors.newSingleThreadScheduledExecutor();
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() throws Exception {
                    CompletableFuture<Void> completionFuture = new CompletableFuture<>();

                    // 使用原子变量确保线程安全
                    AtomicInteger timeLeft = new AtomicInteger(countdownSeconds);

                    countdownFuture = scheduler.scheduleAtFixedRate(() -> {
                        if (isCancelled() || completionFuture.isDone()) {
                            countdownFuture.cancel(false);
                            return;
                        }

                        int currentTime = timeLeft.getAndDecrement();

                        if (currentTime >= 0) {
                            // 更新UI
                            Platform.runLater(() -> {
                                if (currentTime > 0) {
                                    String timeRemaining = String.format("倒计时：%s分%s秒",
                                            currentTime / 60, currentTime % 60);
                                    scheduledProcessMessage.setText(timeRemaining);
                                } else {
                                    scheduledProcessMessage.setText("执行中...");
                                }
                            });
                        }

                        // 倒计时结束
                        if (currentTime <= 0) {
                            Platform.runLater(() -> {
                                try {
                                    handleScheduledExecute();
                                } finally {
                                    // 任务执行完成后，准备下一轮
                                    timeLeft.set(countdownSeconds);

                                    // 1秒后重新开始倒计时
                                    scheduler.schedule(() -> {
                                        if (!isCancelled()) {
                                            timeLeft.set(countdownSeconds);
                                        }
                                    }, 1, TimeUnit.SECONDS);
                                }
                            });
                        }

                    }, 0, 1, TimeUnit.SECONDS); // 立即开始，每秒执行一次
                    // 等待服务被取消
                    try {
                        completionFuture.get();
                    } catch (CancellationException e) {
                        // 正常取消
                    }
                    return null;
                }
            };
        }

        @Override
        protected void cancelled() {
            super.cancelled();
            if (countdownFuture != null) {
                countdownFuture.cancel(false);
            }
            if (scheduler != null) {
                scheduler.shutdown();
            }
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            if (scheduler != null) {
                scheduler.shutdown();
            }
        }

        @Override
        protected void failed() {
            super.failed();
            if (scheduler != null) {
                scheduler.shutdown();
            }
        }

        public void setCountdownSeconds(int seconds) {
            this.countdownSeconds = seconds;
        }
    }

    public static BigDecimal extractMoneyValue(String input) {
        try {
            String numStr = input.replaceAll("[^0-9.-]", "");
            return new BigDecimal(numStr);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}

package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.resource.ResourceUtil;
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
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.GiftCard;
import com.sgswit.fx.utils.*;
import com.sgswit.fx.utils.sign.CrossPlatformAesUtil;
import com.sgswit.fx.utils.stage.StageToSystemTrayUtil;
import com.sgswit.fx.utils.web.GiftCardUtil;
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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
    public TextField txtGiftcardLoadAccountTextField;
    @FXML
    public Button executeButton;
    @FXML
    public Label alertMessage;
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

    @FXML
    public Label label1;
    @FXML
    public Label label2;
    @FXML
    public Label label3;
    /**
     * 定时任务进程消息
     */
    @FXML
    public Label processMessage;
    /**
     * 定时任务table
     */
    @FXML
    public TableView<GiftCard> scheduleTableView;

    @FXML
    public TabPane tabPane;

    private ObservableList<GiftCard> scheduleAccountList = FXCollections.observableArrayList();

    private ObservableList<GiftCard> accountList = FXCollections.observableArrayList();

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Map<String, Map<String, Object>> loginCookiesMap = new ConcurrentHashMap<>();

    private final Map<String, Map<String, Object>> scheduleLoginCookiesMap = new ConcurrentHashMap<>();

    private final Random random = new Random();

    private static String redColor = "red";
    private static String successColor = "#238142";
    private static ScheduledExecutorService scheduledExecutorService;
    private static ScheduledFuture scheduledFuture;
    private static ThreadPoolExecutor executor;
    private static ThreadPoolExecutor scheduledExecutor=new  ThreadPoolExecutor(4, 4, 30L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    private CountdownService service = new CountdownService();
    private boolean running = false;

    private static int lastSelectedCountryIndex = 0;

    private static int txtGiftcardLoadAccountNumber = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.GIFTCARD_BALANCE.getCode())));
        getCountry();
        String txtGiftcardLoadAccount = PropertiesUtil.getOtherConfig("txtGiftcardLoadAccount");
        txtGiftcardLoadAccount=CrossPlatformAesUtil.decryptWithCompression(txtGiftcardLoadAccount);
        if(StrUtil.isNotEmpty(txtGiftcardLoadAccount.trim())){
            String[] accList = txtGiftcardLoadAccount.split("\n");
            txtGiftcardLoadAccountNumber=accList.length;
            if (accList.length ==1){
                txtGiftcardLoadAccount=accList[0];
                txtGiftcardLoadAccountTextField.setText(txtGiftcardLoadAccount);
            }else {
                txtGiftcardLoadAccountTextField.setText("导入的登录ID数量："+accList.length);
                txtGiftcardLoadAccountTextField.setDisable(true);
                loginBtn.setDisable(true);
            }
        }
        // 注册粘贴事件的监听器
        txtGiftcardLoadAccountTextField.setOnContextMenuRequested((ContextMenuEvent event) -> {

        });
        txtGiftcardLoadAccountTextField.setOnKeyReleased(event -> {
            if (event.isShortcutDown()) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                String content = clipboard.getString().replaceAll("\t", " ");
                txtGiftcardLoadAccountTextField.setText(content);
            }
        });
        if (StringUtils.isEmpty(txtGiftcardLoadAccountTextField.getText())) {
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
        // 定时查询礼品卡相关组件初始化
        scheduleTableViewInitialize();

        super.initialize(url, resourceBundle);
    }

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
        scheduleAccountList.addListener((ListChangeListener<GiftCard>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(giftCard -> {
                        giftCard.noteProperty().addListener((obs, oldVal, newVal) -> {
                            int index = scheduleAccountList.indexOf(giftCard);
                            if (index >= 0) {
                                // 重新set一遍，局部刷新
                                scheduleTableView.getItems().set(index, giftCard);
                            }
                        });
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
            label1.setTextFill(textColor);
            label2.setTextFill(textColor);
            label3.setTextFill(textColor);
        });
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
                ThreadUtil.execAsync(() -> {
                    try {
                        loginAndInit();
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
                ThreadUtil.execAsync(() -> loginAndInit());
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
        if (StringUtils.isEmpty(txtGiftcardLoadAccountTextField.getText()) || loginCookiesMap.isEmpty()) {
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
        String[] accountPwdArray = AccountImportUtil.parseAccountAndPwd(txtGiftcardLoadAccountTextField.getText());

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
        switchToTableView(0);
    }

    /**
     *登录
     * @param actionEvent
     */
    @FXML
    public void onClickLoginBtn(ActionEvent actionEvent) {
        ThreadUtil.execAsync(() -> {
            try {
                loginAndInit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public boolean executeButtonActionBefore() {
        if (txtGiftcardLoadAccountNumber==0) {
            alert("请输入一个AppleID作为初始化，账号格式为：账号----密码", Alert.AlertType.ERROR);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 登录并初始化
     */
    protected void loginAndInit() {
        String account = null;
        String pwd = null;
        try {
            Map<String,Object> authMap=new HashMap<>();
            boolean f = false;
            //校验账号格式是否正确
            if(txtGiftcardLoadAccountNumber==0){
                Platform.runLater(new Task<Integer>() {
                    protected Integer call() {
                        alert("请输入一个AppleID作为初始化，账号格式为：账号----密码", Alert.AlertType.ERROR);
                        return 1;
                    }
                });
                return;
            }else{
                String txtGiftcardLoadAccount = PropertiesUtil.getOtherConfig("txtGiftcardLoadAccount");
                txtGiftcardLoadAccount=CrossPlatformAesUtil.decryptWithCompression(txtGiftcardLoadAccount);
                String[] accList = txtGiftcardLoadAccount.split("\n");
                int randomIndex = ThreadLocalRandom.current().nextInt(accList.length);
                String[] its = AccountImportUtil.parseAccountAndPwd(accList[randomIndex]);
                if (its.length >= 2) {
                    f = true;
                    account = its[0];
                    pwd = its[1];
                }
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
            HttpResponse initBalanceRes = GiftCardUtil.initBalance(countryCode);
            if (initBalanceRes.getStatus() != 303) {
                updateUI("初始化失败，请重试", redColor);
                return;
            }

            //https://secure7.store.apple.com/shop/giftcard/balance
            HttpResponse initBalanceWitTunesRes = GiftCardUtil.initBalanceWithTunes(initBalanceRes);
            if (initBalanceWitTunesRes.getStatus() != 302) {
                updateUI("初始化失败，请重试", redColor);
                return;
            }

            //https://secure4.store.apple.com/shop/signIn?ssi=1AAABiatkunsgRa-aWEWPTDH2TWsHul_CZ2TC62v9QxcThhc-EPUrFW8AAAA3aHR0cHM6Ly9zZWN1cmU0LnN0b3JlLmFwcGxlLmNvbS9zaG9wL2dpZnRjYXJkL2JhbGFuY2V8fAACAf0PkQUMMDk-ffBr4IVwBmhKDAsCeTbIe2k-7oOanvAP
//            HttpResponse pre3 = GiftCardUtil.shopSignInInit(initBalanceWitTunesRes);
//            authMap = GiftCardUtil.jXDocument(initBalanceWitTunesRes, pre3, authMap);


//            HttpResponse shldBtCkGeneratorResponse= GiftCardUtil.shldBtCkGeneratorByGet(initBalanceWitTunesRes,initBalanceRes);
//            JSONObject shldBtJsonObject=JSONUtil.parseObj(shldBtCkGeneratorResponse.body());
//            // 添加 flagskv 对象
//            JSONObject flagskv = new JSONObject();
//            flagskv.set("patSkip", true);
//            shldBtJsonObject.set("flagskv", flagskv);
//            Map<String,Object> solvePoWMap=PoWSolver.solvePoW(shldBtJsonObject.getInt("low"),shldBtJsonObject.getInt("high")
//                    ,shldBtJsonObject.getInt("parts"),shldBtJsonObject.getBigInteger("result"),shldBtJsonObject.getLong("timeout"));
//            // 添加 number 和 took
//            shldBtJsonObject.set("number", solvePoWMap.get("number"));
//            shldBtJsonObject.set("took", solvePoWMap.get("took"));
//
//            HttpResponse shldBtCkGeneratorResponse2=GiftCardUtil.shldBtCkGeneratorByPost(reloadBalanceRes,initBalanceRes,shldBtJsonObject.toStringPretty());
//            Map<String,String> cookiesMap=new HashMap<>();
//            Map<String,String> cookiesMap2= CookieUtils.setCookiesToMap(shldBtCkGeneratorResponse2,cookiesMap);
//
//            authMap.put("shld_bt_ck",cookiesMap2.get("shld_bt_ck"));
//            HttpResponse signFrameResponse= (HttpResponse) authMap.get("signFrameResponse");
//
//            HttpResponse challengeResponse= GiftCardUtil.challenge(authMap, signFrameResponse , pre3);
//
//            //https://idmsa.apple.com/appleauth/auth/federate?isRememberMeEnabled=true
//            HttpResponse step0Res = GiftCardUtil.federate(account, authMap);
//            if (200 != step0Res.getStatus()) {
//                updateUI("初始化失败，请重试", redColor);
//                return;
//            }
//            //"https://idmsa.apple.com/appleauth/auth/signin/init
//            HttpResponse step1Res = GiftCardUtil.signinInit(account, step0Res, authMap);
//            if (200 != step1Res.getStatus()) {
//                updateUI("初始化失败，请重试", redColor);
//                return;
//            }
//            //https://idmsa.apple.com/appleauth/auth/signin/complete?isRememberMeEnabled=true
//            HttpResponse step2Res = GiftCardUtil.signinCompete(account, pwd, authMap, step1Res, initBalanceRes, pre3);
//            if (409 == step2Res.getStatus()) {
//                String authType = JSONUtil.parse(step2Res.body()).getByPath("authType", String.class);
//                if ("hsa2".equals(authType)) {
//                    updateUI("您的Apple ID已受双重认证保护", redColor);
//                    return;
//                }
//            } else if (200 == step2Res.getStatus()) {
//
//            } else {
//                StringBuffer m = new StringBuffer();
//                String serviceErrors = JSONUtil.parse(step2Res.body()).getByPath("serviceErrors", String.class);
//                if (null != serviceErrors) {
//                    JSONArray jsonArray = JSONUtil.parseArray(serviceErrors);
//                    Iterator iterator = jsonArray.iterator();
//                    while (iterator.hasNext()) {
//                        JSONObject jsonObject = (JSONObject) iterator.next();
//                        m.append(jsonObject.getStr("message"));
//                        m.append(";");
//                    }
//                    updateUI(m.toString(), redColor);
//                    return;
//                }
//            }
//            //step3
//            //https://secure8.store.apple.com//shop/signIn/idms/authx?ssi=4AAABmH5N90oBIPvf7kFnJIjxmrYRdXrtzJnYQ_wRJICNB4hly1RrEM18AAAAOGh0dHBzOi8vc2VjdXJlOC5zdG9yZS5hcHBsZS5jb20vc2hvcC9naWZ0Y2FyZC9iYWxhbmNlfHx8AAIBFgcDpqECCN-LpfFSpJnKrmkeX6cM6T5uRbvnqvhqgYA
//
//            HttpResponse step3Res = GiftCardUtil.shopSignin(step2Res, initBalanceRes, authMap);
//            StringBuilder cookieBuilder = new StringBuilder();
//            List<String> resCookies = step3Res.headerList("Set-Cookie");
//            for (String item : resCookies) {
//                cookieBuilder.append(";").append(item);
//            }
//            cookieBuilder.append(";").append(authMap.get("as_sfa_cookie"));
//            Map<String, String> cookieMap = new HashMap<>();
//            cookieMap = CookieUtils.setCookiesToMap(step3Res, cookieMap);
//            authMap.put("cookies", MapUtil.join(cookieMap, ";", "=", true));
//            //https://secure4.store.apple.com/shop/giftcard/balance
//            HttpResponse reload2BalanceRes = GiftCardUtil.reload2Balance(step3Res,initBalanceRes,reloadBalanceRes);
//            Document prodDoc = Jsoup.parse(reload2BalanceRes.body());
//            Elements initDataElement = prodDoc.select("script[id=init_data]");
//            JSONObject meta = JSONUtil.parseObj(initDataElement.html());
//            String x_as_actk = meta.getByPath("meta.h.x-as-actk",String.class);
//            authMap.put("x-as-actk", x_as_actk);
//            loginCookiesMap.put(IdUtil.fastSimpleUUID(), authMap);
//            scheduleLoginCookiesMap.put(IdUtil.fastSimpleUUID(), authMap);
//            PropertiesUtil.setOtherConfig("txtGiftcardLoadAccount", txtGiftcardLoadAccountTextField.getText());
//            updateUI("初始化成功，下次启动将自动执行初始化", successColor);
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

    /**
     * 查询余额操作
     * @param giftCard
     * @param countryCode
     */
    protected void checkBalance(GiftCard giftCard,String countryCode) {
        //开启任务
        if((giftCard.isHasBalance() || giftCard.isRunning()) && giftCard.isScheduledFlag()){

            return;
        }
        if(!giftCard.isScheduledFlag()){
            timerStart();
        }else{
            giftCard.runningProperty().set(true);
        }
        if (!StrUtils.giftCardCodeVerify(giftCard.getGiftCardCode())) {
            giftCard.setDataStatus("0");
            tableRefreshAndInsertLocal(giftCard, "输入代码不符合查询格式");
            return;
        }
        giftCard.setLogTime(DateUtil.now());
        giftCard.setHasFinished(false);
        if(giftCard.getFailCount()==0){
            setAndRefreshNote(giftCard, "正在查询...");
        }else{
            int failCount=giftCard.getFailCount()+1;
            setAndRefreshNote(giftCard, "查询失败，正在进行"+failCount+"次查询...");
        }

        ThreadUtil.sleep(100);

        if (!giftCard.isScheduledFlag()) {
            checkBalanceInternal(giftCard, countryCode, loginCookiesMap);
        } else {
            checkBalanceInternal(giftCard, countryCode, scheduleLoginCookiesMap);
        }
    }
    private void checkBalanceInternal(GiftCard giftCard, String countryCode, Map<String, Map<String, Object>> cookieMap) {
        if (cookieMap.isEmpty()) {
            setAndRefreshNote(giftCard, "正在登录...");
            login(countryCode, giftCard.isScheduledFlag());
            giftCard.runningProperty().set(false);
            checkBalance(giftCard, countryCode);
            return;
        }
        Object[] entries = cookieMap.entrySet().toArray();
        Map.Entry<String, Map<String, Object>> entry = (Map.Entry<String, Map<String, Object>>) entries[random.nextInt(entries.length)];
        HttpResponse step4Res = GiftCardUtil.checkBalance(entry.getValue(), giftCard.getGiftCardCode());
        //设置查询次数
        giftCard.setQueryCount(giftCard.getQueryCount()+1);
        //设置已查询
        giftCard.runningProperty().set(false);
        if (step4Res.getStatus() != 200) {
            if (step4Res.getStatus() == 541) {
                cookieMap.remove(entry.getKey());
                checkBalance(giftCard, countryCode);
                return;
            } else {
                handleFailCount(giftCard);
                checkBalance(giftCard, countryCode);
                return;
            }
        }
        try {
            JSON bodyJson = JSONUtil.parse(step4Res.body());
            String status = bodyJson.getByPath("head.status", String.class);

            if (Constant.REDIRECT_CODE.equals(status)) {
                handleFailCount(giftCard);
                checkBalance(giftCard, countryCode);
            } else if (!Constant.SUCCESS.equals(status)) {
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
        if (giftCard.getFailCount() > 10) {
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



    /**
     * 登录操作
     * @param countryCode
     */
    public void login(String countryCode,Boolean scheduledFlag) {
        try {
            //判断当前登录信息是否够使用
//            Map<String, Object> authMap = new HashMap<>(10);
//            String account = null;
//            String pwd = null;
//            if (!StringUtils.isEmpty(txtGiftcardLoadAccountTextField.getText())) {
//                String[] its = AccountImportUtil.parseAccountAndPwd(txtGiftcardLoadAccountTextField.getText());
//                if (its.length == 2) {
//                    account = its[0];
//                    pwd = its[1];
//                }
//            }
//            //https://secure.store.apple.com/shop/giftcard/balance
//            HttpResponse initBalanceRes = GiftCardUtil.initBalance(countryCode);
//            if (303 != initBalanceRes.getStatus()) {
//                return ;
//            }
//            ThreadUtil.sleep(300);
//            //https://secure4.store.apple.com/shop/giftcard/balance
//            HttpResponse reloadBalanceRes = GiftCardUtil.initBalanceWithTunes(initBalanceRes);
//            if (302 != reloadBalanceRes.getStatus()) {
//                return ;
//            }
//            //https://secure4.store.apple.com/shop/signIn?ssi=1AAABiatkunsgRa-aWEWPTDH2TWsHul_CZ2TC62v9QxcThhc-EPUrFW8AAAA3aHR0cHM6Ly9zZWN1cmU0LnN0b3JlLmFwcGxlLmNvbS9zaG9wL2dpZnRjYXJkL2JhbGFuY2V8fAACAf0PkQUMMDk-ffBr4IVwBmhKDAsCeTbIe2k-7oOanvAP
//            HttpResponse pre3 = GiftCardUtil.shopSignInInit(initBalanceRes, reloadBalanceRes);
//            authMap = GiftCardUtil.jXDocument(reloadBalanceRes, pre3, authMap);
//            HttpResponse shldBtCkGeneratorResponse= GiftCardUtil.shldBtCkGeneratorByGet(reloadBalanceRes,initBalanceRes);
//            JSONObject shldBtJsonObject=JSONUtil.parseObj(shldBtCkGeneratorResponse.body());
//            // 添加 flagskv 对象
//            JSONObject flagskv = new JSONObject();
//            flagskv.set("patSkip", true);
//            shldBtJsonObject.set("flagskv", flagskv);
//            Map<String,Object> solvePoWMap=GiftCardUtil.solvePoW(shldBtJsonObject.getStr("salt"),shldBtJsonObject.getStr("challenge"));
//            // 添加 number 和 took
//            shldBtJsonObject.set("number", solvePoWMap.get("number"));
//            shldBtJsonObject.set("took", solvePoWMap.get("took"));
//
//            HttpResponse shldBtCkGeneratorResponse2=GiftCardUtil.shldBtCkGeneratorByPost(reloadBalanceRes,initBalanceRes,shldBtJsonObject.toStringPretty());
//            Map<String,String> cookiesMap=new HashMap<>();
//            Map<String,String> cookiesMap2= CookieUtils.setCookiesToMap(shldBtCkGeneratorResponse2,cookiesMap);
//
//            HttpResponse step0Res = GiftCardUtil.federate(account, authMap);
//            HttpResponse step1Res = GiftCardUtil.signinInit(account, step0Res, authMap);
//            HttpResponse step2Res = GiftCardUtil.signinCompete(account, pwd, authMap, step1Res, initBalanceRes, pre3);
//            if (409 == step2Res.getStatus()) {
//                String authType = JSONUtil.parse(step2Res.body()).getByPath("authType", String.class);
//                if ("hsa2".equals(authType)) {
//                    return;
//                }
//            } else if (200 != step2Res.getStatus()) {
//                if (null != JSONUtil.parse(step2Res.body()).getByPath("serviceErrors")) {
//                    return;
//                }
//            }
//            //step3 shop signin
//            authMap.put("shld_bt_ck",cookiesMap2.get("shld_bt_ck"));
//            HttpResponse step3Res = GiftCardUtil.shopSignin(step2Res, initBalanceRes, authMap);
//            StringBuilder cookieBuilder = new StringBuilder();
//            List<String> resCookies = step3Res.headerList("Set-Cookie");
//            for (String item : resCookies) {
//                cookieBuilder.append(";").append(item);
//            }
//            cookieBuilder.append(";").append(authMap.get("as_sfa_cookie"));
//            Map<String, String> cookieMap = new HashMap<>();
//            cookieMap = CookieUtils.setCookiesToMap(step3Res, cookieMap);
//            authMap.put("cookies", MapUtil.join(cookieMap, ";", "=", true));
//            PropertiesUtil.setOtherConfig("txtGiftcardLoadAccount", txtGiftcardLoadAccountTextField.getText());
//
//
//            //https://secure4.store.apple.com/shop/giftcard/balance
//            HttpResponse reload2BalanceRes = GiftCardUtil.reload2Balance(step3Res,initBalanceRes,reloadBalanceRes);
//            Document prodDoc = Jsoup.parse(reload2BalanceRes.body());
//            Elements initDataElement = prodDoc.select("script[id=init_data]");
//            JSONObject meta = JSONUtil.parseObj(initDataElement.html());
//            String x_as_actk = meta.getByPath("meta.h.x-as-actk",String.class);
//            authMap.put("x-as-actk", x_as_actk);
//            if(scheduledFlag){
//                scheduleLoginCookiesMap.put(IdUtil.fastSimpleUUID(),authMap);
//            }else{
//                loginCookiesMap.put(IdUtil.fastSimpleUUID(),authMap);
//            }

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
        txtGiftcardLoadAccountTextField.setDisable(status);
        loginBtn.setDisable(status);
        executeButton.setDisable(status);
    }

    @Override
    public void accountHandler(GiftCard giftCard) {
        String countryCode = countryBox.getSelectionModel().getSelectedItem().get("code");
        checkBalance(giftCard,countryCode);
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
        loginCookiesMap.clear();
        //关闭任务
        scheduledFuture.cancel(true);
        //关闭线程池
        scheduledExecutorService.shutdown();
        executor.shutdown();
        //关闭定时任务
        scheduledExecutor.shutdown();
        service.cancel();
    }

    private void timerStart() {
        if (null == scheduledExecutorService || scheduledExecutorService.isShutdown()) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        }
        // 创建一个定时任务，延迟0秒执行，之后每10秒执行一次
        scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if(loginCookiesMap.size()<5){
                getThreadPoolExecutor().execute(()->login(countryBox.getSelectionModel().getSelectedItem().get("code"),false));
            }
        }, 0, 10, TimeUnit.SECONDS);
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
    protected ThreadPoolExecutor getThreadPoolExecutor(){
        if(null==executor ){
            // 核心线程数（最少保持1个线程）
            int corePoolSize = 1;
            // 最大线程数（最多允许10个线程）
            int maxPoolSize = 5;
            // 空闲线程存活时间（单位：秒）
            long keepAliveTime = 30;
            // 任务队列（有界队列，容量100）
            int taskCount=5;
            executor = new ThreadPoolExecutor(
                    corePoolSize,
                    maxPoolSize,
                    keepAliveTime,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(taskCount),
                    // 队列满时由提交线程执行任务
                    new ThreadPoolExecutor.AbortPolicy()
            );
        }else{

        }
        return executor;
    }

    /****************** 定时查询礼品卡余额 **************/

    @FXML
    private void startStopExecute(){
        if (running) {
            processMessage.setText("未开始");
            processMessage.setTextFill(Paint.valueOf("#238142"));
            startStopButton.setText("开始计时");
            startStopButton.setTextFill(Paint.valueOf("#0F8DE2"));
            intervalField.setDisable(false);
            //关闭定时任务
            service.cancel();
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
            service.setCountdownSeconds(Integer.valueOf(intervalFieldText)*60);
            service.restart();
        }
        running=!running;
    }
    @FXML
    private void handleExecute() {
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
                checkBalance(giftCard,countryBox.getSelectionModel().getSelectedItem().get("code"));
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
        if (StringUtils.isEmpty(txtGiftcardLoadAccountTextField.getText())) {
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

        String[] accountPwdArray = AccountImportUtil.parseAccountAndPwd(txtGiftcardLoadAccountTextField.getText());
        String[] lineArray = c.getData().split("\n");
        for (String item : lineArray) {
            if (StrUtil.isEmpty(item)) {
                continue;
            }
            GiftCard giftCard = new GiftCard();
            giftCard.setSeq(scheduleAccountList.size() + 1);
            giftCard.setPwd(accountPwdArray[1]);
            giftCard.setAccount(accountPwdArray[0]);
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

        area.setText(CrossPlatformAesUtil.decryptWithCompression(PropertiesUtil.getOtherConfig("txtGiftcardLoadAccount")));
        button.setOnAction(event -> {
            txtGiftcardLoadAccountTextField.setDisable(false);
            loginBtn.setDisable(false);
            String txtGiftcardLoadAccount=area.getText();
            if(StrUtil.isNotEmpty(txtGiftcardLoadAccount.trim())){
                String[] accList = txtGiftcardLoadAccount.split("\n");
                txtGiftcardLoadAccountNumber=accList.length;
                if (accList.length ==1){
                    txtGiftcardLoadAccount=accList[0];
                    loginBtn.setDisable(false);
                    txtGiftcardLoadAccountTextField.setText(txtGiftcardLoadAccount);
                }else {
                    txtGiftcardLoadAccountTextField.setText("导入的登录ID数量："+accList.length);
                    txtGiftcardLoadAccountTextField.setDisable(true);
                    loginBtn.setDisable(true);
                }
                PropertiesUtil.setOtherConfig("txtGiftcardLoadAccount",CrossPlatformAesUtil.encryptWithCompression(txtGiftcardLoadAccount));
            }else{
                txtGiftcardLoadAccountNumber=0;
            }
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
        stage.getIcons().add(new Image(this.getClass().getResource(logImg).toString()));
        stage.initStyle(StageStyle.DECORATED);
        stage.showAndWait();
    }


    class CountdownService extends Service<Void> {
        private int countdownSeconds = 60; // 倒计时总秒数
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    while (!isCancelled()) { // 无限循环，直到服务被取消
                        int timeLeft = countdownSeconds;

                        // 倒计时循环
                        while (timeLeft >= 0 && !isCancelled()) {
                            final int currentTime = timeLeft;
                            String TimeRemaining=String.format("倒计时：%s分%s秒",currentTime/60,currentTime%60);
                            // 更新UI显示
                            Platform.runLater(() -> {
                                processMessage.setText(currentTime > 0 ?TimeRemaining : "执行中...");
                            });
                            if (timeLeft > 0) {
                                Thread.sleep(1000); // 每秒更新一次
                            }
                            timeLeft--;
                        }
                        // 倒计时结束，执行任务
                        if (!isCancelled()) {
                            Platform.runLater(() -> handleExecute());
                            // 执行完成后立即开始新一轮倒计时
                            if (!isCancelled()) {
                                Thread.sleep(1000); // 等待1秒再开始下一轮
                            }
                        }
                    }
                    return null;
                }
            };
        }
        // 可以设置不同的倒计时时间
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

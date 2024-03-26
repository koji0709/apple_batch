package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.constant.StoreFontsUtils;
import com.sgswit.fx.controller.common.CommCodePopupView;
import com.sgswit.fx.controller.common.ItunesView;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.iTunes.vo.GiftCardRedeem;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GiftCardBatchRedeemController extends ItunesView<GiftCardRedeem> {

    @FXML
    ComboBox<String> accountComboBox;

    @FXML
    Label accountComboxSelectLabel;

    @FXML
    Label countryLabel;

    @FXML
    Label balanceLabel;

    @FXML
    Label statusLabel;

    @FXML
    Label checkAccountDescLabel;

    @FXML
    Button checkAccountBtn;

    @FXML
    Button open2FAViewBtn;

    @FXML
    Button editOrImportAccountListBtn;

    @FXML
    CheckBox hidePwdCheckBox;

    @FXML
    CheckBox scrollToLastRowCheckBox;

    @FXML
    CheckBox accountGroupCheckBox;

    @FXML
    CheckBox execAgainCheckBox;

    private GiftCardRedeem singleGiftCardRedeem = new GiftCardRedeem();

    private Integer processNum = 0;

    Stage redeemLogStage;

    private Map<String, AtomicReference<BigDecimal>> atomicBalanceMap = new HashMap<>();

    private static Map<String, List<Long>> countMap = new HashMap<>();

    private static Set<String> lockSet = new HashSet<>();

    private Map<String, String> currencyMap = new HashMap<>();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        hidePwdCheckBox.setSelected(true);
        scrollToLastRowCheckBox.setSelected(true);
        accountGroupCheckBox.setSelected(true);

        hidePwdCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            // newValue true = 隐藏密码, false = 展示密码
            accountTableView.getColumns().forEach(tableColumn -> {
                if ("pwd".equals(tableColumn.getId())) {
                    tableColumn.setVisible(!newValue);
                }
            });
        });
        // 设置表格cell样式
        setCellStyle();
        // 注册粘贴事件的监听器
        accountComboBox.setOnKeyReleased(event -> {
            if (event.isShortcutDown()){
                Clipboard clipboard = Clipboard.getSystemClipboard();
                String content = clipboard.getString().replaceAll("\t", " ");
                if (content != null) {
                    accountComboBox.getSelectionModel().select(content);
                }
            }
        });
        accountComboBox.getEditor().setOnContextMenuRequested((ContextMenuEvent event) -> {
        });

        // 初始化检测账号列表
        File file = new File(Constant.LOCAL_FILE_STORAGE_PATH + "/兑换账号列表/account.txt");
        if (file.exists()){
            String content = FileUtil.readUtf8String(file);
            if (StrUtil.isNotEmpty(content)){
                String[] arrayWithEmpty = content.split("\n");
                accountComboBox.setValue(arrayWithEmpty[0]);
                accountComboBox.getItems().clear();
                // 使用Hutool的ArrayUtil.removeEmpty方法删除数组中的空值
                String[] resultArray = ArrayUtil.removeEmpty(arrayWithEmpty);
                accountComboBox.getItems().addAll(resultArray);
                accountComboxSelectLabel.setText("1/" + resultArray.length);
            }
        }
        accountComboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            // 自己输入的值
            if (newValue.intValue() == -1){
                accountComboxSelectLabel.setText("0/0");
            }else{
                accountComboxSelectLabel.setText((newValue.intValue() + 1) + "/" + accountComboBox.getItems().size());
            }
        });

    }
    /**
     * 导入账号
     */
    public void importAccountButtonAction(ActionEvent actionEvent) {
        String desc = "说明：\n" +
                "    1.格式为: 账号----密码----礼品卡(可多个) 或 单礼品卡\n" +
                "    2.一次可以输入多条账户信息，每条账户单独一行; 如果数据中有“-”符号,则使用{-}替换。";
        Button button = (Button) actionEvent.getSource();
        // 获取按钮所在的场景
        Scene scene = button.getScene();
        // 获取场景所属的舞台
        Stage stage = (Stage) scene.getWindow();
        openImportAccountView(Collections.emptyList(),"导入账号",desc,stage);
        boolean selected = scrollToLastRowCheckBox.isSelected();
        if (selected){
            accountTableView.scrollTo(accountList.size()-1);
        }
    }
    /**
     * 设置表格样式
     */
    public void setCellStyle() {
        ObservableList<TableColumn<GiftCardRedeem, ?>> columns = accountTableView.getColumns();
        for (TableColumn<GiftCardRedeem, ?> column : columns) {
            String id = column.getId();
            if (!"seq".equals(id)){
                column.setCellFactory(col -> new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item.toString());
                            GiftCardRedeem row = (GiftCardRedeem) getTableRow().getItem();
                            if (row != null) {
                                if ("giftCardStatus".equals(id)) {
                                    if("无效卡".equals(row.getGiftCardStatus()) || "僵尸卡".equals(row.getGiftCardStatus())
                                            || "旧卡".equals(row.getGiftCardStatus()) || "兑换失败".equals(row.getGiftCardStatus())){
                                        setTextFill(Color.RED);
                                    }else{
                                        setTextFill(Color.BLACK);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    }
    @Override
    public List<GiftCardRedeem> parseAccount(String accountStr){
        List<GiftCardRedeem> accountList1 = new ArrayList<>();
        String[] accList = accountStr.split("\n");

        for (int i = 0; i < accList.length; i++) {
            String acc = accList[i];
            if (StrUtil.isEmpty(acc)){
                continue;
            }
            String[] accountAndPwdArr=AccountImportUtil.parseAccountAndPwd(acc);
            // 单礼品卡模式
            if (accountAndPwdArr.length==1){
                String accountComboBoxValue = accountComboBox.getValue();
                if (StrUtil.isEmpty(accountComboBoxValue)){
                    continue;
                }
                String[] accountComboBoxValueArr = AccountImportUtil.parseAccountAndPwd(accountComboBoxValue);
                if (accountComboBoxValueArr.length != 2){
                    continue;
                }
                String giftCardCode = acc;
                String account = accountComboBoxValueArr[0];
                String pwd     = accountComboBoxValueArr[1];
                GiftCardRedeem giftCardRedeem = new GiftCardRedeem();
                giftCardRedeem.setAccount(account);
                giftCardRedeem.setPwd(pwd);
                giftCardRedeem.setGiftCardCode(giftCardCode);

                boolean success = giftCardCodeVerify(giftCardCode);
                if (!success){
                    giftCardRedeem.setGiftCardStatus("无效卡");
                }
                accountList1.add(giftCardRedeem);
            }else{
                // 账号礼品卡模式
                if (accountAndPwdArr.length >= 3){
                    String account =accountAndPwdArr[0];
                    String pwd     = accountAndPwdArr[1];
                    for (int j = 2; j < accountAndPwdArr.length; j++) {
                        String giftCardCode = accountAndPwdArr[j];
                        GiftCardRedeem giftCardRedeem = new GiftCardRedeem();
                        giftCardRedeem.setAccount(account);
                        giftCardRedeem.setPwd(pwd);
                        giftCardRedeem.setGiftCardCode(giftCardCode);

                        boolean success = giftCardCodeVerify(giftCardCode);
                        if (!success){
                            giftCardRedeem.setGiftCardStatus("无效卡");
                        }

                        accountList1.add(giftCardRedeem);
                    }
                }
            }
        }

        return accountList1;
    }

    @Override
    public void stopTaskButtonAction() {
        super.stopTaskButtonAction();
        for (GiftCardRedeem giftCardRedeem : accountList) {
            if ("兑换暂不可用，将在一分钟之后执行".equals(giftCardRedeem.getNote())){
                setAndRefreshNote(giftCardRedeem,"");
            }
        }
    }

    /**
     * 执行按钮点击
     */
    @Override
    public void executeButtonAction() {
        // 如果没有选择同账号单线程工作就使用默认的执行方法
        boolean selected = accountGroupCheckBox.isSelected();
        if (!selected){
            super.executeButtonAction();
            return;
        }

        // 同账号单线程工作
        // 校验
        if (accountList.isEmpty()) {
            alert("请先导入账号！");
            return;
        }

        boolean isProcessed = true;
        for (GiftCardRedeem giftCardRedeem : accountList) {
            if (!isProcessed(giftCardRedeem)){
                isProcessed = false;
                break;
            }
        }
        if (isProcessed) {
            //alert("账号都已处理！");
            return;
        }

        // 修改按钮为执行状态
        setExecuteButtonStatus(true);

        // 每一次执行前都释放锁
        if (reentrantLock.isLocked()) {
            reentrantLock.unlock();
            ThreadUtil.sleep(500);
        }

        // 此处的线程是为了处理,按钮状态等文案显示
        ThreadUtil.execute(() -> {
            LinkedHashMap<String,List<GiftCardRedeem>> accountGroupMap = new LinkedHashMap<>();
            for (GiftCardRedeem giftCardRedeem : accountList) {
                if (isProcessed(giftCardRedeem)){
                    continue;
                }
                ++processNum;
                String account = giftCardRedeem.getAccount();
                List<GiftCardRedeem> giftCardRedeemList = accountGroupMap.get(account);
                if (giftCardRedeemList==null){
                    giftCardRedeemList = new ArrayList<>();
                }
                giftCardRedeemList.add(giftCardRedeem);
                accountGroupMap.put(account,giftCardRedeemList);
            }

            for (Map.Entry<String, List<GiftCardRedeem>> entry : accountGroupMap.entrySet()) {
                ThreadUtil.execute(()->{
                    List<GiftCardRedeem> accountList = entry.getValue();

                    // 处理账号
                    for (int i = 0; i < accountList.size(); i++) {
                        GiftCardRedeem giftCardRedeem = accountList.get(i);
                        String account = giftCardRedeem.getAccount();

                        // 校验 (一分钟内一个id最多只能五张卡、每日兑换上限)
                        redeemCheck(account);

                        if (reentrantLock.isLocked()) {
                            return;
                        }

                        accountHandlerExpand(giftCardRedeem,false);
                        ThreadUtil.sleep(2000);

                        if (--processNum <= 0) {
                            // 任务执行结束, 恢复执行按钮状态
                            Platform.runLater(() -> setExecuteButtonStatus(false));
                        }
                    }

                });

            }
        });
    }

    public void redeemCheck(String account){
        synchronized (getClass()){
            List<Long> countList = countMap.get(account);
            if (!CollUtil.isEmpty(countList)){
                // 清理掉2分钟前的数据
                countList = countList.stream().filter(time -> {
                    long second = DateUtil.between(new Date(time), new Date(System.currentTimeMillis()), DateUnit.SECOND);
                    return second < 120;
                }).collect(Collectors.toList());
            }

            if (!CollUtil.isEmpty(countList) && countList.size() >= 5){
                //将相同appleID下的未对换所有卡号设置成 一分钟之后执行
                for(GiftCardRedeem g:accountList){
                    if(StringUtils.isEmpty(g.getNote())){
                        setAndRefreshNote(g,"兑换暂不可用，将在一分钟之后执行");
                    }
                }
                lockSet.add(account);
            }
        }

        if (lockSet.contains(account)){
            ThreadUtil.sleep(1000 * 63);
        }

        synchronized (getClass()){
            if (lockSet.contains(account)){
                lockSet.remove(account);
                countMap.remove(account);
            }
            List<Long> countList = countMap.get(account);
            if (CollUtil.isEmpty(countList)){
                countList = new ArrayList<>();
            }
            countList.add(System.currentTimeMillis());
            countMap.put(account,countList);
        }
    }

    /**
     * qewqeq@2980.com----Ac223388----XMPC3HRMNM6K5FXP
     * shabagga222@tutanota.com----Ac223388----XMPC3HRMNM6K5FXP
     * cncots@gmail.com----Xx97595031.----XMPC3HRMNM6K5FXP
     */
    @Override
    public void accountHandler(GiftCardRedeem giftCardRedeem) {
        giftCardRedeem.setExecTime(DateUtil.now());

        // 登录并缓存
        itunesLogin(giftCardRedeem);

        boolean success = giftCardCodeVerify(giftCardRedeem.getGiftCardCode());
        if (!success){
            giftCardRedeem.setGiftCardStatus("无效卡");
            throw new ServiceException("输入的代码无效。");
        }

        String account = giftCardRedeem.getAccount();

        // 查询礼品卡信息
        String giftCardCode = giftCardRedeem.getGiftCardCode();

        // 获取礼品卡信息
        setAndRefreshNote(giftCardRedeem,"查询礼品卡信息中...");
        HttpResponse codeInfoSrvRsp = ITunesUtil.getCodeInfoSrv(giftCardRedeem, giftCardCode);
        if (codeInfoSrvRsp.getStatus() == 403){
            // 删除登陆信息,再次获取礼品卡信息
            giftCardRedeem.setIsLogin(false);
            String id = super.createId(giftCardRedeem.getAccount(),giftCardRedeem.getPwd());
            loginSuccessMap.remove(id);
            itunesLogin(giftCardRedeem);
            codeInfoSrvRsp = ITunesUtil.getCodeInfoSrv(giftCardRedeem, giftCardCode);
        }

        JSONObject bodyJSON = JSONUtil.parseObj(codeInfoSrvRsp.body());
        JSONObject codeInfo = bodyJSON.getJSONObject("codeInfo");

        if (codeInfo == null){
            throw new ServiceException("礼品卡信息读取失败，请稍后重试");
        }

        setAndRefreshNote(giftCardRedeem,"礼品卡信息查询成功，兑换中...");
        String amount = codeInfo.getStr("amount", "0");
        giftCardRedeem.setGiftCardAmount(amount);

        // 获取现有金额
        synchronized (this){
            AtomicReference<BigDecimal> balanceReference = atomicBalanceMap.get(account);
            if (balanceReference == null) {
                HashMap<String, Object> params = new HashMap<>();
                params.put("itspod",giftCardRedeem.getItspod());
                params.put("dsPersonId",giftCardRedeem.getDsPersonId());
                params.put("storeFront",giftCardRedeem.getStoreFront());
                params.put("passwordToken",giftCardRedeem.getPasswordToken());
                params.put("cookies",giftCardRedeem.getCookie());
                PurchaseBillUtil.accountSummary(params);
                String balanceStr;
                if(null!=params.get("balance")){
                    balanceStr = MapUtil.getStr(params, "balance");
                }else{
                    balanceStr="";
                }
                balanceReference = new AtomicReference<>(getBalance(balanceStr));
                atomicBalanceMap.put(account,balanceReference);
                currencyMap.put(account,getCurrency(balanceStr));
            }
        }

        setAndRefreshNote(giftCardRedeem,"兑换中...");
        HttpResponse redeemRsp = ITunesUtil.redeem(giftCardRedeem,"");
        String body = redeemRsp.body();

        // 如果操作频繁，重新执行一次
        if (giftCardRedeem.getMaxTryNumber() < 5 && execAgainCheckBox.isSelected() && StrUtil.isEmpty(body)){
            int i=giftCardRedeem.getMaxTryNumber()+1;
            ThreadUtil.sleep(2000L);
            setAndRefreshNote(giftCardRedeem,"操作频繁，重新执行中"+i+"...");
            redeemRsp = ITunesUtil.redeem(giftCardRedeem,"");
            giftCardRedeem.setMaxTryNumber(i);
            body      = redeemRsp.body();
        }

        if (redeemRsp.getStatus() != 200 || StrUtil.isEmpty(body)){
            String message = "礼品卡兑换失败!兑换过于频繁，请稍后重试！";
            throw new ServiceException(message);
        }

        // 兑换
        JSONObject redeemBody = JSONUtil.parseObj(body);
        if (redeemBody.keySet().contains("plist")){
            redeemBody = PListUtil.parse(body);
        }
        Integer status = redeemBody.getInt("status",-1);
        if (status != 0){
            String userPresentableErrorMessage = redeemBody.getStr("userPresentableErrorMessage","");
            String messageKey = redeemBody.getStr("errorMessageKey","");
            String message = "兑换失败! %s";
            // 礼品卡无效
            if ("MZCommerce.GiftCertificateAlreadyRedeemed".equals(messageKey)){
                // 礼品卡已兑换
                giftCardRedeem.setGiftCardStatus("旧卡");
                String recipientDsId = codeInfo.getStr("recipientDsId");
                if (bodyJSON.getInt("status") != 0 || StrUtil.isEmpty(recipientDsId)){
                    message = String.format(message,"此代码已被兑换");
                }else{
                    message = String.format(message,"此代码已被[dsid:"+recipientDsId+"]兑换");
                }
            } else if ("MZCommerce.GiftCertificateDisabled".equals(messageKey)){
                // 僵尸卡
                giftCardRedeem.setGiftCardStatus("僵尸卡");
                message = String.format(message,"此凭证已停用，所以无法兑换");
            } else if ("MZFinance.RedeemCodeSrvLoginRequired".equals(messageKey)){
                //重新执行一次登录操作
                if (giftCardRedeem.getFailCount() == 0){
                    // 需要重新登录
                    giftCardRedeem.setIsLogin(false);
                    String id=super.createId(giftCardRedeem.getAccount(),giftCardRedeem.getPwd());
                    loginSuccessMap.remove(id);
                    giftCardRedeem.setFailCount(1);
                    accountHandler(giftCardRedeem);
                }
                return;
            }else if("MZCommerce.GiftCertRedeemStoreFrontMismatch".equals(messageKey)){
                //卡正常, 但是和账号商城不匹配
                message = String.format(message,userPresentableErrorMessage);
                giftCardRedeem.setGiftCardStatus("有效卡");
            }else if("MZFreeProductCode.NoBalance".equals(messageKey) || "MZFreeProductCode.NoSuch".equals(messageKey)){
                message = String.format(message,userPresentableErrorMessage);
                giftCardRedeem.setGiftCardStatus("无效卡");
            }else{
                message = String.format(message,userPresentableErrorMessage);
                giftCardRedeem.setGiftCardStatus("兑换失败");
            }
            throw new ServiceException(message);
        }

        AtomicReference<BigDecimal> balanceReference = atomicBalanceMap.get(account);
        String initBalance = balanceReference.get().toString();
        ThreadUtil.execute(()->{
            Map<String,Object> params1 = new HashMap<>(){{
                put("code",giftCardRedeem.getGiftCardCode());
                put("user",SM4Util.decryptBase64(PropertiesUtil.getOtherConfig("login.userName")));
                put("recipientAccount", account);
                put("recipientDsid",giftCardRedeem.getDsPersonId());
                put("initBalance", initBalance);
                put("redeemBalance",giftCardRedeem.getGiftCardAmount());
                put("redeemTime",DateUtil.now());
            }};
            HttpResponse addGiftcardRedeemLogRsp = HttpUtils.post("/giftcardRedeemLog", params1);
            boolean addSuccess = HttpUtils.verifyRsp(addGiftcardRedeemLogRsp);
            if (!addSuccess){
                LoggerManger.info("同步兑换记录失败: status: " + addGiftcardRedeemLogRsp.getStatus() + ", params: " + JSONUtil.toJsonStr(params1));
            }
        });

        // 礼品卡兑换成功
        String message = "兑换成功,加载金额:%s%s, ID总金额: %s%s";
        giftCardRedeem.setGiftCardStatus("已兑换");
        balanceReference.set(new BigDecimal(giftCardRedeem.getGiftCardAmount()).add(balanceReference.get()));
        message = String.format(message
                , currencyMap.get(account), giftCardRedeem.getGiftCardAmount()
                , currencyMap.get(account), balanceReference.get());
        setAndRefreshNote(giftCardRedeem,message);
    }

    private static String getCurrency(String balanceStr){
        for (int i = 0; i < balanceStr.length(); i++) {
            char c = balanceStr.charAt(i);
            boolean isNumber = NumberUtil.isNumber(String.valueOf(c));
            if (isNumber){
                return balanceStr.substring(0,i);
            }
        }
        return "";
    }

    private static BigDecimal getBalance(String balanceStr){
        balanceStr=balanceStr.replace(",","");
        for (int i = 0; i < balanceStr.length(); i++) {
            char c = balanceStr.charAt(i);
            boolean isNumber = NumberUtil.isNumber(String.valueOf(c));
            if (isNumber){
                return new BigDecimal(balanceStr.substring(i));
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    protected void secondStepHandler(GiftCardRedeem account, String code) {
        account.setAuthCode(code);
        accountHandlerExpand(account);
    }

    /**
     * 检测账号按钮点击
     */
    public void checkAccountBtnAction(){
        new Thread(() -> {
            try{
                String accountComboBoxValue = accountComboBox.getValue();
                if (StrUtil.isEmpty(accountComboBoxValue)){
                    Platform.runLater(() -> checkAccountDescLabel.setText("请先输入账号信息"));
                    return;
                }
                String[] accountComboBoxValueArr = AccountImportUtil.parseAccountAndPwd(accountComboBoxValue);
                if (accountComboBoxValueArr.length != 2){
                    Platform.runLater(() -> checkAccountDescLabel.setText("账号信息格式不正确！格式：账号----密码"));
                    return;
                }

                // 扣除点数
                pointCost(singleGiftCardRedeem,PointUtil.out, FunctionListEnum.GIFTCARD_BATCH_REDEEM_QUERY.getCode());

                Platform.runLater(() -> {
                    checkAccountDescLabel.setText("");
                    statusLabel.setText( "状态：" + "正在检测...");
                    checkAccountBtn.setDisable(true);
                    open2FAViewBtn.setDisable(true);
                    editOrImportAccountListBtn.setDisable(true);
                });
                String account = accountComboBoxValueArr[0];
                String pwd     = accountComboBoxValueArr[1];
                String guid = DataUtil.getGuidByAppleId(account);
                // 修复个人信息读取上一个登陆的用户的bug
                if ((StrUtil.isEmpty(singleGiftCardRedeem.getAccount())
                        || !account.equals(singleGiftCardRedeem.getAccount())
                        || !pwd.equals(singleGiftCardRedeem.getAccount())
                        || !singleGiftCardRedeem.isLogin()
                    )  && StrUtil.isEmpty(singleGiftCardRedeem.getAuthCode())
                    ) {
                    singleGiftCardRedeem = new GiftCardRedeem();
                    singleGiftCardRedeem.setAccount(account);
                    singleGiftCardRedeem.setPwd(pwd);
                    singleGiftCardRedeem.setGuid(guid);
                    singleGiftCardRedeem.setNote("");
                }

                itunesLogin(singleGiftCardRedeem);

                HashMap<String, Object> params = new HashMap<>();
                params.put("itspod",singleGiftCardRedeem.getItspod());
                params.put("dsPersonId",singleGiftCardRedeem.getDsPersonId());
                params.put("storeFront",singleGiftCardRedeem.getStoreFront());
                params.put("passwordToken",singleGiftCardRedeem.getPasswordToken());
                params.put("cookies",singleGiftCardRedeem.getCookie());
                PurchaseBillUtil.accountSummary(params);

                HttpResponse authRsp = (HttpResponse) singleGiftCardRedeem.getAuthData().get("authRsp");
                String storeFront = authRsp.header(Constant.HTTPHeaderStoreFront);
                String country = StoreFontsUtils.getCountryCode(StrUtil.split(storeFront, "-").get(0));
                country = StrUtil.isEmpty(country) ? "未知" : country.split("-")[1];
                JSONObject rspJSON = PListUtil.parse(authRsp.body());
//                String  balance           = rspJSON.getStr("creditDisplay","0");
                Boolean isDisabledAccount = rspJSON.getBool("accountFlags.isDisabledAccount",false);
                String  status            = !isDisabledAccount ? "正常" : "禁用";
                String finalCountry = country;

                String balance = params.get("balance").toString();
                Platform.runLater(() -> {
                    countryLabel.setText("国家：" + finalCountry);
                    balanceLabel.setText( "余额：" + (StrUtil.isEmpty(balance) ? "0" : balance));
                    statusLabel.setText( "状态：" + status);
                });
            }catch (ServiceException e){
                Platform.runLater(() -> {
                    countryLabel.setText("国家：" + "");
                    balanceLabel.setText( "余额：" + "");
                    statusLabel.setText( "状态：" + e.getMessage());
                    checkAccountDescLabel.setText(e.getMessage());
                });
                // 异常返回点数
                pointCost(singleGiftCardRedeem,PointUtil.in,FunctionListEnum.GIFTCARD_BATCH_REDEEM_QUERY.getCode());
            } catch (IORuntimeException e) {
                Platform.runLater(() -> {
                    countryLabel.setText("国家：" + "");
                    balanceLabel.setText( "余额：" + "");
                    statusLabel.setText( "状态：" + "连接异常，请检查网络");
                    checkAccountDescLabel.setText("连接异常，请检查网络");
                });
                // 异常返回点数
                pointCost(singleGiftCardRedeem,PointUtil.in,FunctionListEnum.GIFTCARD_BATCH_REDEEM_QUERY.getCode());
            }catch (Exception e){
                Platform.runLater(() -> {
                    countryLabel.setText("国家：" + "");
                    balanceLabel.setText( "余额：" + "");
                    statusLabel.setText( "状态：" + "数据处理异常");
                    checkAccountDescLabel.setText("数据处理异常");
                });
                // 异常返回点数
                pointCost(singleGiftCardRedeem,PointUtil.in,FunctionListEnum.GIFTCARD_BATCH_REDEEM_QUERY.getCode());
            } finally {
                Platform.runLater(() -> {
                    checkAccountBtn.setDisable(false);
                    open2FAViewBtn.setDisable(false);
                    editOrImportAccountListBtn.setDisable(false);
                });
            }
        }).start();
    }

    /**
     * 输入双重验证码
     */
    public void open2FAViewBtnAction(){
        if (StrUtil.isEmpty(singleGiftCardRedeem.getAccount())){
            alert("请先检测账号");
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/comm-code-popup.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), 385, 170);
        } catch (IOException e) {
            //throw new RuntimeException(e);
            alert(e.getMessage());
            return;
        }

        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        CommCodePopupView fxmlLoaderController = fxmlLoader.getController();
        fxmlLoaderController.setAccount(singleGiftCardRedeem.getAccount());
        Stage popupStage = new Stage();
        popupStage.setTitle("输入验证码");
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.showAndWait();
        String code = fxmlLoaderController.getSecurityCode();
        if(StringUtils.isEmpty(code)){
            return;
        }
        singleGiftCardRedeem.setAuthCode(code);
        checkAccountBtnAction();
    }

    /**
     * 礼品卡查询余额按钮点击
     */
    public void giftCardBalanceBtnAction(){
        StageUtil.show(StageEnum.GIFTCARD_BALANCE);
    }

    public void giftCardDetectionProfessionalEditionBtnAction(){
        StageUtil.show(StageEnum.GIFTCARD_DETECTION_PROFESSIONAL_EDITION);
    }

    /**
     * 编辑或导入列表中的账号
     */
    public void editOrImportAccountListBtnAction(){
        // Constant.LOCAL_FILE_STORAGE_PATH
        Stage stage = new Stage();

        Label descLabel = new Label("在编辑框中编辑兑换列表中的工作账号，格式为账号----密码，一行一条。");
        descLabel.setWrapText(true);

        TextArea area = new TextArea();
        area.setPrefHeight(250);
        area.setPrefWidth(560);
        String path = Constant.LOCAL_FILE_STORAGE_PATH + "/兑换账号列表/account.txt";
        File file = new File(path);
        if (file.exists()){
            String content = FileUtil.readUtf8String(file);
            area.setText(content);
        }

        VBox vBox2 = new VBox();
        vBox2.setPadding(new Insets(0, 0, 0, 205));

        Button button = new Button("保存修改并重新加载");
        button.setTextFill(Paint.valueOf("#067019"));
        button.setPrefWidth(150);
        button.setPrefHeight(30);

        button.setOnAction(event -> {
            String content = area.getText();
            File file1 = new File(path);
            if (file1.exists()){
                FileUtil.del(file1);
            }
            if (StrUtil.isNotEmpty(content)){
                String[] arrayWithEmpty = content.split("\n");
                accountComboBox.setValue(arrayWithEmpty[0]);
                accountComboBox.getItems().clear();
                // 使用Hutool的ArrayUtil.removeEmpty方法删除数组中的空值
                String[] resultArray = ArrayUtil.removeEmpty(arrayWithEmpty);
                accountComboBox.getItems().addAll(resultArray);
            }else{
                accountComboBox.setValue("");
                accountComboBox.getItems().clear();
            }
            file1 = FileUtil.newFile(path);
            FileUtil.appendUtf8String(content,file1);
            stage.close();
        });

        vBox2.getChildren().addAll(button);

        VBox mainVbox = new VBox();
        mainVbox.setSpacing(20);
        mainVbox.setPadding(new Insets(20));
        mainVbox.getChildren().addAll(descLabel, area, vBox2);

        Group root = new Group(mainVbox);
        stage.setTitle("编辑/添加兑换列表账号");
        stage.setScene(new Scene(root, 600, 380));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        String logImg= PropertiesUtil.getConfig("softwareInfo.log.path");
        stage.getIcons().add(new Image(this.getClass().getResource(logImg).toString()));
        stage.initStyle(StageStyle.DECORATED);
        stage.showAndWait();
    }

    /**
     * 兑换记录查询 X8HZ8Z7KT7DW8PML
     */
    public void redeemLogQueryAction(){
        if (redeemLogStage != null && redeemLogStage.isShowing()){
            return;
        }

        HBox box1 = new HBox();
        Label label1 = new Label("输入礼品卡号");
        TextField codeTextField = new TextField();

        Button queryButton = new Button("查询");
        queryButton.setPrefWidth(100);

        Label empty = new Label("");
        empty.setPrefWidth(120);

        Label label2 = new Label("消耗点数:");

        Label pointLabel = new Label(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.GIFTCARD_REDEEM_LOG_QUERY.getCode())));

        pointLabel.setStyle("-fx-font-size: 16px");
        pointLabel.setTextFill(Paint.valueOf("red"));
        box1.getChildren().addAll(label1,codeTextField,queryButton,empty,label2,pointLabel);

        box1.setAlignment(Pos.CENTER_LEFT);
        box1.setSpacing(8);

        HBox box2 = new HBox();
        Label label3 = new Label("兑换历史记录:");
        Label label4 = new Label("(至多显示最新500条兑换记录)");
        label4.setTextFill(Paint.valueOf("#b2bed1"));
        box2.getChildren().addAll(label3,label4);

        TableView<GiftCardRedeem> tableView = new TableView();
        TableColumn seqCol = new TableColumn("序号");
        seqCol.setId("seq");
        seqCol.setPrefWidth(60);
        seqCol.setCellFactory(new Callback() {
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
        TableColumn redeemTimeCol = new TableColumn("兑换时间");
        redeemTimeCol.setId("execTime");
        redeemTimeCol.setPrefWidth(150);
        redeemTimeCol.setCellValueFactory(new PropertyValueFactory(redeemTimeCol.getId()));

        TableColumn noteCol = new TableColumn("执行信息");
        noteCol.setId("note");
        noteCol.setPrefWidth(360);
        noteCol.setCellValueFactory(new PropertyValueFactory(noteCol.getId()));
        tableView.getColumns().addAll(seqCol,redeemTimeCol,noteCol);

        VBox mainVbox = new VBox();
        mainVbox.setSpacing(20);
        mainVbox.setPadding(new Insets(10));
        mainVbox.getChildren().addAll(box1, box2,tableView);

        queryButton.setOnAction(event -> {
            String code = codeTextField.getText();
            if (StrUtil.isEmpty(code)){
                alert("请输入礼品卡号");
                return;
            }

            // 扣除点数
            Map<String,String> pointCost = PointUtil.pointCost(FunctionListEnum.GIFTCARD_REDEEM_LOG_QUERY.getCode(),PointUtil.out,"");
            if(!Constant.SUCCESS.equals(pointCost.get("code"))){
                alert(pointCost.get("msg"));
                return;
            }

            Map<String,Object> params = new HashMap<>();
            params.put("code",code);
            HttpResponse giftcardRedeemLogRsp = HttpUtils.get("/giftcardRedeemLog",params);
            boolean verify = HttpUtils.verifyRsp(giftcardRedeemLogRsp);
            if (!verify){
                alert(HttpUtils.message(giftcardRedeemLogRsp));
                PointUtil.pointCost(FunctionListEnum.GIFTCARD_REDEEM_LOG_QUERY.getCode(),PointUtil.in,"");
                return;
            }

            JSONArray dataList = HttpUtils.dataList(giftcardRedeemLogRsp);
            ObservableList<GiftCardRedeem> items = tableView.getItems();
            items.clear();

            if (CollUtil.isEmpty(dataList)){
                alert("暂无相关记录");
                // 返回点数
                PointUtil.pointCost(FunctionListEnum.GIFTCARD_REDEEM_LOG_QUERY.getCode(),PointUtil.in,"");
                return;
            }

            String format = "此代码已被dsid[%s]兑换";
            dataList.forEach(o ->{
                JSONObject json = (JSONObject) o;
                GiftCardRedeem giftCardRedeem = new GiftCardRedeem();
                giftCardRedeem.setExecTime(json.getStr("redeemTime"));
                giftCardRedeem.setNote(String.format(format,json.getStr("recipientDsid")));
                items.add(giftCardRedeem);
            });
        });

        Group root = new Group(mainVbox);
        Stage stage = new Stage();
        redeemLogStage = stage;
        stage.setTitle("礼品卡大数据");
        stage.setScene(new Scene(root, 600, 505));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
        stage.initStyle(StageStyle.DECORATED);
        stage.showAndWait();
    }

    /**
     * 礼品卡校验
     */
    public boolean giftCardCodeVerify(String giftCardCode){
        //判断礼品卡的格式是否正确
        giftCardCode=StringUtils.deleteWhitespace(giftCardCode);
        String regex = "X[a-zA-Z0-9]{15}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(giftCardCode.toUpperCase());
        return matcher.matches();
    }

    public void show2WindowAction(){
        StageUtil.show(StageEnum.GIFTCARD_BATCH_REDEEM2);
    }

    public void show3WindowAction(){
        StageUtil.show(StageEnum.GIFTCARD_BATCH_REDEEM3);
    }
    @FXML
    public void chnAppleIdValidateBtnAction(ActionEvent actionEvent) {
        StageUtil.show(StageEnum.CHN_APPLE_ID_VALIDATE);
    }
}

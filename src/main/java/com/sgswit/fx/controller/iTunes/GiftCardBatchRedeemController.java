package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.constant.StoreFontsUtils;
import com.sgswit.fx.controller.common.*;
import com.sgswit.fx.controller.exception.PointCostException;
import com.sgswit.fx.controller.exception.ResponseTimeoutException;
import com.sgswit.fx.controller.exception.ServiceException;
import com.sgswit.fx.controller.iTunes.vo.GiftCardRedeem;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.model.LoginInfo;
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
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GiftCardBatchRedeemController extends ItunesView<GiftCardRedeem> {
    @FXML
    public Button show2WindowBtn;
    @FXML
    public Button show3WindowBtn;
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

    Stage redeemLogStage;

    private static int intervalTime=65;
    private static Map<String, Map<String,Long>> countMap = new HashMap<>();
    private static Map<String, LinkedHashMap<String,GiftCardRedeem>> toBeExecutedMap = new HashMap<>();
    //定时任务
    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture scheduledFuture;
    private int limitRedeem=5;
    private final Pattern pattern = Pattern.compile("\\d+");
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        hidePwdCheckBox.setSelected(true);
        scrollToLastRowCheckBox.setSelected(true);
        accountGroupCheckBox.setSelected(true);

        menuItem.add(Constant.RightContextMenu.COPY_CARD_NO.getCode());

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
                if (clipboard != null && StrUtil.isNotEmpty(clipboard.getString())){
                    String content = clipboard.getString().replaceAll("\t", " ");
                    if (content != null) {
                        ObservableList<String> items = accountComboBox.getItems();
                        if (!items.contains(content)){
                            items.add(content);
                        }
                        accountComboBox.getSelectionModel().select(content);
                    }
                }
            }
        });
        accountComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!CollUtil.isEmpty(accountComboBox.getItems())){
                for (String item : accountComboBox.getItems()) {
                    if (item.replaceAll(" ","").equals(newValue)){
                        accountComboBox.getSelectionModel().select(item);
                        break;
                    }
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
                content = content.replaceAll("\t"," ");
                content=StrUtils.removeBlankLines(content);
                String[] split = content.split("\n");
                accountComboBox.setValue(split[0]);
                accountComboBox.getItems().clear();
                accountComboBox.getItems().addAll(split);
                accountComboxSelectLabel.setText("1/" + split.length);
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
                "    1.格式为: 账号----密码-礼品卡(可多个) 或 单礼品卡\n" +
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
                                    }else if ("有效卡".equals(row.getGiftCardStatus())){
                                        setTextFill(Color.GREEN);
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
    public List<GiftCardRedeem> parseAccount(String accountStr) {
        List<GiftCardRedeem> accountList = new ArrayList<>();

        if (StrUtil.isEmpty(accountStr)) {
            return accountList;
        }

        // 分割账户字符串为多行
        String[] accList = accountStr.split("\n");

        for (String acc : accList) {
            if (StrUtil.isEmpty(acc)) {
                continue;
            }

            // 解析账户和密码
            String[] array = AccountImportUtil.parseAccountAndPwd(acc);

            // 处理有效账户格式
            if (isValidAccountFormat(array)) {
                processValidAccount(array, accountList);
            }
            // 处理组合框账户格式
            else {
                processComboBoxAccount(acc, accountList);
            }
        }

        return accountList;
    }

    /**
     * 验证账户格式是否有效
     */
    private boolean isValidAccountFormat(String[] accountParts) {
        return accountParts != null &&
                accountParts.length >= 3 &&
                (Validator.isEmail(accountParts[0]) || Validator.isNumber(accountParts[0]));
    }

    /**
     * 处理有效账户格式
     */
    private void processValidAccount(String[] accountParts, List<GiftCardRedeem> accountList) {
        String account = accountParts[0];
        String pwd = accountParts[1];
        String giftCardData = String.join("", Arrays.copyOfRange(accountParts, 2, accountParts.length));
        // 分割礼品卡数据
        List<String> segments = List.of(StrUtil.cut(giftCardData, 16));
        // 创建礼品卡对象
        for (String giftCardCode : segments) {
            GiftCardRedeem giftCard = createGiftCard(account, pwd, giftCardCode);
            accountList.add(giftCard);
        }
    }

    /**
     * 处理组合框账户格式
     */
    private void processComboBoxAccount(String accountData, List<GiftCardRedeem> accountList) {
        String accountComboBoxValue = accountComboBox.getValue();

        if (StrUtil.isEmpty(accountComboBoxValue)) {
            return;
        }

        String[] comboBoxParts = AccountImportUtil.parseAccountAndPwd(accountComboBoxValue);

        if (comboBoxParts.length != 2) {
            return;
        }

        String giftCardCode = StrUtils.replaceMultipleSpaces(accountData, "");
        GiftCardRedeem giftCard = createGiftCard(comboBoxParts[0], comboBoxParts[1], giftCardCode);
        accountList.add(giftCard);
    }

    /**
     * 创建礼品卡对象
     */
    private GiftCardRedeem createGiftCard(String account, String pwd, String giftCardCode) {
        GiftCardRedeem giftCard = new GiftCardRedeem();
        giftCard.setAccount(account);
        giftCard.setPwd(pwd);
        giftCard.setGiftCardCode(giftCardCode);

        if (!StrUtils.giftCardCodeVerify(giftCardCode)) {
            giftCard.setGiftCardStatus("无效卡");
        }

        return giftCard;
    }

    @Override
    public void stopTaskButtonAction() {
        super.stopTaskButtonAction();
        for (GiftCardRedeem giftCardRedeem : accountList) {
            if (Constant.REDEEM_WAIT1_DESC.equals(giftCardRedeem.getNote())){
                ReflectUtil.invoke(giftCardRedeem,"setHasFinished",true);
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
        boolean execAgainCheckBoxSelected = execAgainCheckBox.isSelected();
        // 同账号单线程工作
        // 校验
        if (accountList.isEmpty()) {
            alert("请先导入账号！");
            return;
        }

        boolean isProcessed = true;
        for (GiftCardRedeem giftCardRedeem : accountList) {
            if (StrUtil.isEmpty(giftCardRedeem.getNote())){
                isProcessed = false;
                break;
            }
        }
        if (isProcessed) {
            //alert("账号都已处理！");
            return;
        }

        String taskNo = RandomUtil.randomNumbers(6);
        LoggerManger.info("【"+stage.getTitle()+"】" + "开始任务; 任务编号:" + taskNo);

        // 修改按钮为执行状态
        Platform.runLater(() -> setExecuteButtonStatus(true));
        timer();
        // 将账号分组
        LinkedHashMap<String,List<GiftCardRedeem>> accountGroupMap = new LinkedHashMap<>();
        for (GiftCardRedeem giftCardRedeem : accountList) {
            if (isProcessed(giftCardRedeem)){
                continue;
            }
            runningList.add(giftCardRedeem);
            giftCardRedeem.setNote("");
            boolean hasTaskNo = ReflectUtil.hasField(giftCardRedeem.getClass(), "taskNo");
            if (hasTaskNo){
                ReflectUtil.setFieldValue(giftCardRedeem,"taskNo",taskNo + ":" + giftCardRedeem.getGiftCardCode());
            }
            String account = giftCardRedeem.getAccount();
            List<GiftCardRedeem> giftCardRedeemList = accountGroupMap.get(account);
            if (giftCardRedeemList==null){
                giftCardRedeemList = new ArrayList<>();
            }
            giftCardRedeemList.add(giftCardRedeem);
            accountGroupMap.put(account,giftCardRedeemList);
        }
        for (String key : accountGroupMap.keySet()) {
            threadPoolExecutor=super.getExecutorService(threadCount);
            Future<?> future= threadPoolExecutor.submit(()->{
                List<GiftCardRedeem> accountList = accountGroupMap.get(key);
                // 使用迭代器进行遍历和修改
                Iterator<GiftCardRedeem> iterator = accountList.iterator();
                while (!Thread.currentThread().isInterrupted() && iterator.hasNext()) {
                    GiftCardRedeem giftCardRedeem = iterator.next();
                    String account=giftCardRedeem.getAccount();
                    Map<String,Long> countList = countMap.get(account);
                    if(null==countList){
                        countList=new HashMap<>();
                    }
                    if(null!=countList && countList.size()>=limitRedeem){
                        ThreadUtil.sleep(100);
                        giftCardRedeem.setNote(execAgainCheckBoxSelected?Constant.REDEEM_WAIT1_DESC:Constant.REDEEM_WAIT2_DESC);
                        if(execAgainCheckBoxSelected){
                            giftCardRedeem.setStartRecordTime(System.currentTimeMillis());
                            LinkedHashMap<String, GiftCardRedeem> toBeExecutedList = toBeExecutedMap.get(account);
                            if(null==toBeExecutedList){
                                toBeExecutedList=new LinkedHashMap<>();
                            }
                            toBeExecutedList.put(account+giftCardRedeem.getGiftCardCode()+RandomUtil.randomNumbers(4),giftCardRedeem);
                            toBeExecutedMap.put(account,toBeExecutedList);
                        }else{
                            runningList.remove(giftCardRedeem);
                            insertLocalHistory(new ArrayList<>(){{
                                add(giftCardRedeem);
                            }});
                        }
                        iterator.remove();
                    }else{
                        accountHandlerExpand(giftCardRedeem, false);
                    }
                }
            });
            List<Future<?>> futureList = threadMap.get(stage);
            if (CollUtil.isEmpty(futureList)){
                futureList = new ArrayList<>();
            }
            futureList.add(future);
            threadMap.put(stage,futureList);
        }
    }

    private void timer(){
        scheduledExecutorService= getScheduledExecutorService();
        scheduledFuture= scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
               setExecuteButtonStatus();
               Iterator<Map.Entry<String, Map<String, Long>>> countMapIterator = countMap.entrySet().iterator();
               while (countMapIterator.hasNext()){
                   Map<String,Long> map = countMapIterator.next().getValue();
                   map.entrySet().removeIf(entry -> DateUtil.between(new Date(entry.getValue()), new Date(System.currentTimeMillis()), DateUnit.SECOND)>intervalTime);
                   if(map.size()==0){
                       countMapIterator.remove();
                   }
               }
               boolean execAgainCheckBoxSelected = execAgainCheckBox.isSelected();
               Iterator<Map.Entry<String, LinkedHashMap<String, GiftCardRedeem>>> toBeExecutedMapIterator = toBeExecutedMap.entrySet().iterator();
               while (toBeExecutedMapIterator.hasNext()){
                   Map<String, GiftCardRedeem> map = toBeExecutedMapIterator.next().getValue();
                   Iterator<Map.Entry<String, GiftCardRedeem>> iterator = map.entrySet().iterator();
                   while (iterator.hasNext()) {
                       Map.Entry<String, GiftCardRedeem> entry = iterator.next();
                       GiftCardRedeem giftCardRedeem=entry.getValue();
                       if(DateUtil.between(new Date(giftCardRedeem.getStartRecordTime()), new Date(System.currentTimeMillis()), DateUnit.SECOND)>intervalTime){
                           // 删除满足条件的元素
                           iterator.remove();
                           if(execAgainCheckBoxSelected){
                               accountHandlerExpand(giftCardRedeem, false);
                           }
                       }
                   }
                   if(map.size()==0){
                       toBeExecutedMapIterator.remove();
                   }
               }
            }catch (Exception e){

            }
        }, 0, 3, TimeUnit.SECONDS);
    }
    /*
    　* 获取线程池
      * @param
    　* @return java.util.concurrent.ScheduledExecutorService
    　* @throws
    　* @author DeZh
    　* @date 2024/7/8 15:33
    */
    private ScheduledExecutorService getScheduledExecutorService(){
        if(null==scheduledExecutorService || scheduledExecutorService.isShutdown()){
            scheduledExecutorService = Executors.newScheduledThreadPool(1);
        }
        return scheduledExecutorService;
    }


    public void setExecuteButtonStatus(){
        Platform.runLater(() -> setExecuteButtonStatus(true));
        // 任务执行结束, 恢复执行按钮状态
        if (runningList.size()==0 || accountTableView.getItems().size()==0){
            Platform.runLater(() -> setExecuteButtonStatus(false));
            stopExecutorService();
        }
    }

    @Override
    public void setExecuteButtonStatus(boolean isRunning) {
        super.setExecuteButtonStatus(isRunning);
    }

    @Override
    public boolean isProcessed(GiftCardRedeem account) {
        return !(StrUtil.isEmpty(account.getNote()) || Constant.REDEEM_WAIT1_DESC.equals(account.getNote())) ;
    }

    /**
     * qewqeq@2980.com----Ac223388----XMPC3HRMNM6K5FXP
     */
    public boolean redeemCheck(GiftCardRedeem giftCardRedeem){
        if(null==scheduledFuture){
            timer();
        }
        setAndRefreshNote(giftCardRedeem,"兑换中...");
        Map<String,Long> countList = countMap.get(giftCardRedeem.getAccount());
        if(null==countList){
            countList=new HashMap<>();
        }
        if(null!=countList && countList.size()>=limitRedeem){
            ThreadUtil.sleep(200);
            boolean execAgainCheckBoxSelected = execAgainCheckBox.isSelected();
            giftCardRedeem.setNote(execAgainCheckBoxSelected?Constant.REDEEM_WAIT1_DESC:Constant.REDEEM_WAIT2_DESC);
            if(execAgainCheckBoxSelected){
                String account=giftCardRedeem.getAccount();
                LinkedHashMap<String, GiftCardRedeem> toBeExecutedList = toBeExecutedMap.get(account);
                if(null==toBeExecutedList){
                    toBeExecutedList=new LinkedHashMap<>();
                }
                toBeExecutedList.put(account+giftCardRedeem.getGiftCardCode()+RandomUtil.randomNumbers(4),giftCardRedeem);
                toBeExecutedMap.put(account,toBeExecutedList);
            }
            return false;
        }else{
            return true;
        }
    }
    @Override
    public void accountHandler(GiftCardRedeem giftCardRedeem) {
        boolean success = StrUtils.giftCardCodeVerify(giftCardRedeem.getGiftCardCode());
        if (!success){
            giftCardRedeem.setGiftCardStatus("无效卡");
            throw new ServiceException("输入的代码无效。");
        }
        String account=giftCardRedeem.getAccount();
        String giftCardCode=giftCardRedeem.getGiftCardCode();
        setAndRefreshNote(giftCardRedeem,"账户登录中...");
        String id=super.createId(giftCardRedeem.getAccount(),giftCardRedeem.getPwd());
        LoginInfo loginInfo = loginSuccessMap.get(id);
        if (loginInfo != null){
            ThreadUtil.sleep(200);
        }
        giftCardRedeem.setExecTime(DateUtil.now());
        Map<String, Long> countList = countMap.get(account);
        if(null==countList){
            countList=new HashMap<>();
        }
        LinkedHashMap<String, GiftCardRedeem> toBeExecutedList = toBeExecutedMap.get(account);
        if(null==toBeExecutedList){
            toBeExecutedList=new LinkedHashMap<>();
        }
        // 登录并缓存
        itunesLogin(giftCardRedeem);

        HttpResponse authRsp = (HttpResponse) giftCardRedeem.getAuthData().get("authRsp");
        JSONObject rspJSON = PListUtil.parse(authRsp.body());
        Boolean isDisabledAccount = rspJSON.getBool("accountFlags.isDisabledAccount",false);
        if (isDisabledAccount){
            throw new ServiceException("账户已被单禁。");
        }

        ThreadUtil.sleep(300);
        setAndRefreshNote(giftCardRedeem,"兑换中...");
        String key = account + giftCardCode + RandomUtil.randomNumbers(4);
        countList.put(key, System.currentTimeMillis());
        countMap.put(account,countList);

        HttpResponse redeemRsp;
        String body;
        try{
            redeemRsp= ITunesUtil.redeem(giftCardRedeem,"");
            body = redeemRsp.body();
            checkAndThrowUnavailableException(redeemRsp);
        }catch (ResponseTimeoutException e){// 响应超时不计入次数统计
            countList.remove(key);
            countMap.put(account,countList);
            throw new ServiceException(e.getMessage());
        }

        // status = 429重试
        Integer i = 3;
        while (i-- >= 0 && redeemRsp.getStatus() == 429){
            ThreadUtil.sleep(200L);
            redeemRsp= ITunesUtil.redeem(giftCardRedeem,"");
            body = redeemRsp.body();
        }

        // 如果是status = 429则不计入统计
        if(redeemRsp.getStatus() == 429) {
            countList.remove(key);
            countMap.put(account,countList);
            throw new ServiceException("响应状态:429,"+Constant.REDEEM_WAIT2_DESC);
        }
        if (StrUtil.isEmpty(body)){
            throw new ServiceException("响应状态:"+redeemRsp.getStatus()+", 响应数据为空, 请检查兑换状态");
        }
        // 兑换
        JSONObject redeemBody = new JSONObject();
        try {
            redeemBody = JSONUtil.parseObj(body);
        }catch (Exception e) {
            LoggerManger.info("响应状态 = "+redeemRsp.getStatus()+", 响应数据 = " + redeemBody);
            throw new ServiceException("响应状态:"+redeemRsp.getStatus()+", 响应数据异常, 请检查兑换状态");
        }

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
                message = String.format(message,"此代码已被兑换");
                Map<String,Object> params = new HashMap<>();
                params.put("code",giftCardRedeem.getGiftCardCode());
                HttpResponse giftcardRedeemLogRsp = HttpUtils.get("/giftcardRedeemLog",params);
                boolean verify = HttpUtils.verifyRsp(giftcardRedeemLogRsp);
                if (!verify){

                }else{
                    JSONArray dataList = HttpUtils.dataList(giftcardRedeemLogRsp);
                    if (!CollUtil.isEmpty(dataList)){
                        String format = "由%s于%s兑换成功。";
                        JSONObject json = (JSONObject) dataList.get(0);
                        message=String.format(format,StrUtils.maskData(json.getStr("recipientAccount")),json.getStr("redeemTime"));
                    }
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
                    loginSuccessMap.remove(id);
                    giftCardRedeem.setFailCount(1);
                    accountHandler(giftCardRedeem);
                    return;
                }
            }else if("MZCommerce.GiftCertRedeemStoreFrontMismatch".equals(messageKey)){
                //卡正常, 但是和账号商城不匹配
                message = String.format(message,userPresentableErrorMessage);
                giftCardRedeem.setGiftCardStatus("有效卡");
            }else if("MZFreeProductCode.NoBalance".equals(messageKey) || "MZFreeProductCode.NoSuch".equals(messageKey)){
                message = String.format(message,userPresentableErrorMessage);
                giftCardRedeem.setGiftCardStatus("无效卡");
            }else if ("MZCommerce.XCardProblem".equals(messageKey)){
                // 访问你的店面可用金额时服务器发生问题。请稍后再试
                if (giftCardRedeem.getFailCount() == 0){
                    giftCardRedeem.setFailCount(1);
                    accountHandler(giftCardRedeem);
                    return;
                }
            }else{
                message = String.format(message,userPresentableErrorMessage);
                giftCardRedeem.setGiftCardStatus("兑换失败");
            }
            LoggerManger.info("【礼品卡兑换】messageKey:" + messageKey + ", message=" + message);
            throw new PointCostException(message);
        }

        // 礼品卡兑换成功
        giftCardRedeem.setGiftCardStatus("已兑换");
        String creditDisplay= redeemBody.getByPath("creditDisplay",String.class);
        String totalMoneyRaw= redeemBody.getByPath("totalCredit.moneyRaw",String.class);
        String giftCardAmount= redeemBody.getByPath("redeemedCredit.money",String.class);
        String giftCardMoneyRaw= redeemBody.getByPath("redeemedCredit.moneyRaw",String.class);
        String message = MessageFormat.format("兑换成功,加载金额:{0}, ID总金额: {1}",new String[]{giftCardAmount,creditDisplay});
        setAndRefreshNote(giftCardRedeem,message);
        ThreadUtil.execute(()->{
            Map<String,Object> params = new HashMap<>(){{
                put("code",giftCardRedeem.getGiftCardCode());
                put("user",SignUtil.decryptBase64(PropertiesUtil.getOtherConfig("login.userName")));
                put("recipientAccount", account);
                put("recipientDsid",giftCardRedeem.getDsPersonId());
                put("initBalance", new BigDecimal(totalMoneyRaw).subtract(new BigDecimal(giftCardMoneyRaw)));
                put("redeemBalance",giftCardMoneyRaw);
                put("salesOrg",StoreFontsUtils.getCountryCodeFromStoreFront(giftCardRedeem.getStoreFront()));
            }};
            HttpResponse addGiftcardRedeemLogRsp = HttpUtils.post("/giftcardRedeemLog", params);
            boolean addSuccess = HttpUtils.verifyRsp(addGiftcardRedeemLogRsp);
            if (!addSuccess){
                LoggerManger.info("同步兑换记录失败: status: " + addGiftcardRedeemLogRsp.getStatus() + ", params: " + JSONUtil.toJsonStr(params));
            }
        });
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
        ThreadUtil.execAsync(() -> {
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
                String account = accountComboBoxValueArr[0];
                String pwd     = accountComboBoxValueArr[1];
                String guid = DataUtil.getGuidByAppleId(account);
                // 扣除点数
                singleGiftCardRedeem.setAccount(account);
                pointCost(singleGiftCardRedeem,PointUtil.out, FunctionListEnum.GIFTCARD_BATCH_REDEEM_QUERY.getCode());

                Platform.runLater(() -> {
                    checkAccountDescLabel.setText("");
                    statusLabel.setText( "状态：" + "正在检测...");
                    checkAccountBtn.setDisable(true);
                    open2FAViewBtn.setDisable(true);
                    editOrImportAccountListBtn.setDisable(true);
                });
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
                String id=super.createId(account,pwd);
                loginSuccessMap.remove(id);
                itunesLogin(singleGiftCardRedeem);

                HttpResponse authRsp = (HttpResponse) singleGiftCardRedeem.getAuthData().get("authRsp");
                String storeFront = authRsp.header(Constant.HTTPHeaderStoreFront);
                String country = StoreFontsUtils.getCountryCode(StrUtil.split(storeFront, "-").get(0));
                country = StrUtil.isEmpty(country) ? "未知" : country.split("-")[1];
                JSONObject rspJSON = PListUtil.parse(authRsp.body());
                String  balance           = StrUtil.isEmpty(rspJSON.getStr("creditDisplay")) ? "0" : rspJSON.getStr("creditDisplay");
                Boolean isDisabledAccount  = rspJSON.getByPath("accountFlags.isDisabledAccount",Boolean.class);
                String finalCountry = country;
                Platform.runLater(() -> {
                    countryLabel.setText("国家：" + finalCountry);
                    balanceLabel.setText( "余额：" + balance);
                    statusLabel.setText( "状态：" + (!isDisabledAccount ? "正常" : "禁用"));
                    statusLabel.setTextFill(isDisabledAccount ? Color.RED : Color.BLACK);
                    if (isDisabledAccount){
                        checkAccountDescLabel.setText("账号已被单禁");
                    }
                });
            }catch (ServiceException e){
                Platform.runLater(() -> {
                    countryLabel.setText("国家：" + "");
                    balanceLabel.setText( "余额：" + "");
                    statusLabel.setText( "状态：" + "");
                    checkAccountDescLabel.setText(e.getMessage());
                });
                // 异常返回点数
                singleGiftCardRedeem.setNote(e.getMessage());
                pointCost(singleGiftCardRedeem,PointUtil.in,FunctionListEnum.GIFTCARD_BATCH_REDEEM_QUERY.getCode());
            } catch (IORuntimeException | HttpException e) {
                Platform.runLater(() -> {
                    countryLabel.setText("国家：" + "");
                    balanceLabel.setText( "余额：" + "");
                    statusLabel.setText( "状态：" + "");
                    checkAccountDescLabel.setText("连接异常，请检查网络");
                });
                // 异常返回点数
                singleGiftCardRedeem.setNote("连接异常，请检查网络");
                pointCost(singleGiftCardRedeem,PointUtil.in,FunctionListEnum.GIFTCARD_BATCH_REDEEM_QUERY.getCode());
            }catch (Exception e){
                Platform.runLater(() -> {
                    countryLabel.setText("国家：" + "");
                    balanceLabel.setText( "余额：" + "");
                    statusLabel.setText( "状态：" + "");
                    checkAccountDescLabel.setText("数据处理异常");
                });
                // 异常返回点数
                singleGiftCardRedeem.setNote("数据处理异常");
                pointCost(singleGiftCardRedeem,PointUtil.in,FunctionListEnum.GIFTCARD_BATCH_REDEEM_QUERY.getCode());
            } finally {
                Platform.runLater(() -> {
                    checkAccountBtn.setDisable(false);
                    open2FAViewBtn.setDisable(false);
                    editOrImportAccountListBtn.setDisable(false);
                });
            }
        });
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
            content = content.replaceAll("\t"," ");
            File file1 = new File(path);
            if (file1.exists()){
                FileUtil.del(file1);
            }
            if (StrUtil.isNotEmpty(content)){
                content=StrUtils.removeBlankLines(content);
                String[] split = content.split("\n");
                accountComboBox.setValue(split[0]);
                accountComboBox.getItems().clear();
                accountComboBox.getItems().addAll(split);
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
        stage.initStyle(StageStyle.UTILITY);
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

            String format = "由%s于%s兑换成功。";
            dataList.forEach(o ->{
                JSONObject json = (JSONObject) o;
                GiftCardRedeem giftCardRedeem = new GiftCardRedeem();
                giftCardRedeem.setExecTime(json.getStr("redeemTime"));
                String message=String.format(format,StrUtils.maskData(json.getStr("recipientAccount")),json.getStr("redeemTime"));
                giftCardRedeem.setNote(message);
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
     * 批量ID加卡
     */
    public void batchAccountParseAction(){
       StageUtil.show(StageEnum.GIFTCARD_BATCH_PARSE_ACCOUNT);
    }

    @FXML
    public void chnAppleIdValidateBtnAction(ActionEvent actionEvent) {
        StageUtil.show(StageEnum.CHN_APPLE_ID_VALIDATE);
    }
    @Override
    public void closeStageActionBefore(){

    }
    @Override
    public void initStageAction(Object userData) {
        String index="1";
        if(null!=userData){
            index=userData.toString();
        }
        if(index.equals("1")){
            show2WindowBtn.setText("工作2窗口");
            show3WindowBtn.setText("工作3窗口");
        }else if(index.equals("2")){
            show2WindowBtn.setText("工作1窗口");
            show3WindowBtn.setText("工作3窗口");
        }else if(index.equals("3")){
            show2WindowBtn.setText("工作1窗口");
            show3WindowBtn.setText("工作2窗口");
        }


    }
    @FXML
    public void show2WindowAction(ActionEvent actionEvent){
        String text=  ((Button)actionEvent.getSource()).getText();
        String index="1";
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            System.out.println(matcher.group());
            index=matcher.group();
        }
        if(index.equals("1")){
            StageUtil.show(StageEnum.GIFTCARD_BATCH_REDEEM,"1");
        }else if(index.equals("2")){
            StageUtil.show(StageEnum.GIFTCARD_BATCH_REDEEM2,"2");
        }else if(index.equals("3")){
            StageUtil.show(StageEnum.GIFTCARD_BATCH_REDEEM3,"3");
        }
    }

}

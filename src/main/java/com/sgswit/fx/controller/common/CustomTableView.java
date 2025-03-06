package com.sgswit.fx.controller.common;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.*;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.LocalhistoryTask;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.LoginInfo;
import com.sgswit.fx.utils.*;
import com.sgswit.fx.utils.db.DataSourceFactory;
import com.sgswit.fx.utils.db.SQLiteUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * account表格视图
 *
 * @author HeHongdong
 */
public class CustomTableView<T> extends CommRightContextMenuView<T> {
    protected List runningList=new ArrayList<>();
    /**登录成功的账号缓存(缓存20分钟)**/
    private static final long time=20*60*1000;

    protected static Map<StageEnum,List<Future<?>>> threadMap = new HashMap<>();

    protected static TimedCache<String, LoginInfo> loginSuccessMap = CacheUtil.newTimedCache(time);
    static {
        loginSuccessMap.schedulePrune(time);
    }
    public Set<String> menuItem =new LinkedHashSet<>(){{
        add(Constant.RightContextMenu.DELETE.getCode());
        add(Constant.RightContextMenu.REEXECUTE.getCode());
        add(Constant.RightContextMenu.COPY.getCode());
    }};

    @FXML
    protected Button executeButton;

    @FXML
    protected Button stopButton;

    @FXML
    protected Button importAccountButton;

    @FXML
    public TableView<T> accountTableView;
    @FXML
    protected Label accountNumLabel;
    @FXML
    protected Label successNumLabel;
    @FXML
    protected Label failNumLabel;
    @FXML
    public Label pointLabel;

    protected ObservableList<T> accountList = FXCollections.observableArrayList();

    protected StageEnum stage;
    /**线程池**/
    protected ThreadPoolExecutor threadPoolExecutor;
    /**线程池大小**/
    protected static int threadCount=Integer.valueOf(PropertiesUtil.getOtherConfig("ThreadCount","4"));

    private Class clz = Account.class;
    private List<String> formats;

    protected String funCode="0";
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        //禁用停止按钮
        stopButton.setDisable(true);
        // 获取当前stage
        if (url != null) {
            String file = url.getFile();
            if (file != null) {
                String view = file.substring(file.indexOf("views/"));
                LinkedHashMap<String, StageEnum> stageMap = EnumUtil.getEnumMap(StageEnum.class);
                stageMap.forEach((stageName, stageEnum) -> {
                    if (view.equals(stageEnum.getView())) {
                        stage = stageEnum;
                        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.getFunEnumByDesc(stageEnum.getTitle()).getCode())));
                        funCode = FunctionListEnum.getFunEnumByDesc(stageEnum.getTitle()).getCode();
                    }
                });
            }
        }

        // 监听窗口大小的变化, 变更accountTableView的高度
        accountTableView.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                // 监听 Scene 宽高变化
                newScene.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                    accountTableView.setPrefWidth(newWidth.doubleValue());
                });
                newScene.heightProperty().addListener((obs, oldHeight, newHeight) -> {
                    accountTableView.setPrefHeight(newHeight.doubleValue()); // 减去一些高度，给其他控件留空间
                });
            }
        });

        // 数据绑定
        ObservableList<TableColumn<T, ?>> columns = accountTableView.getColumns();
        for (TableColumn<T, ?> column : columns) {
            String id=column.getId();
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

        // 初始化泛型
        Map<Type, Type> typeMap = TypeUtil.getTypeMap(this.getClass());
        this.clz = Account.class;
        if (!typeMap.isEmpty()) {
            for (Map.Entry<Type, Type> typeEntry : typeMap.entrySet()) {
                this.clz = ReflectUtil.newInstance(typeEntry.getValue().getTypeName()).getClass();
            }
        }
        // 监听note变化,刷新table
        accountList.addListener((ListChangeListener<T>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    List<? extends T> addedSubList = change.getAddedSubList();
                    if (!CollUtil.isEmpty(addedSubList)){
                        for (T t : addedSubList) {
                            if (t instanceof Account){
                                ((Account) t).noteProperty().addListener((observable, oldValue, newValue) -> accountTableView.refresh());
                            }
                        }
                    }
                }
            }
        });
        // 设置多选模式
        accountTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // 鼠标右键清空选中行
        accountTableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton() == MouseButton.PRIMARY) { // 左键点击事件
                if (event.getClickCount() == 1) {
                    double y = event.getY();
                    double headerHeight = accountTableView.lookup(".column-header-background").getBoundsInParent().getHeight();
                    double contentHeight = accountTableView.getItems().size() * 24;
                    // 如果点击了空行，取消所有选中
                    if (y > headerHeight + contentHeight) {
                        accountTableView.getSelectionModel().clearSelection();
                    }
                }
            }
        });
    }

    /**
     * 执行前, 一般做一些参数校验
     */
    @Override
    public boolean executeButtonActionBefore() {
        return true;
    }

    /**
     * 执行按钮点击
     */
    public void executeButtonAction() {
        // 校验
        if (accountList.isEmpty()) {
            alert("请先导入账号！");
            return;
        }
        boolean verify = executeButtonActionBefore();
        if (!verify) {
            return;
        }

        // 检测最后一个是否被处理过,如果被处理过说明整个列表都被处理过,无需再次处理
        if (isProcessed(accountList.get(accountList.size() - 1))) {
            //alert("账号都已处理！");
            return;
        }

        //计算需要执行的总数量
        Map<String,String> pointCost= PointUtil.pointCost(funCode,accountList);
        if(!Constant.SUCCESS.equals(pointCost.get("code"))){
            alertUI(pointCost.get("msg"), Alert.AlertType.ERROR);
            return;
        }

        String taskNo = RandomUtil.randomNumbers(6);
        LoggerManger.info("【"+stage.getTitle()+"】" + "开始任务; 任务编号:" + taskNo);

        // 修改按钮为执行状态
        setExecuteButtonStatus(true);


        // 此处的线程是为了处理,按钮状态等文案显示
        for (int i = 0; i < accountList.size(); i++) {
            T account = accountList.get(i);
            boolean processed = isProcessed(account);
            if (processed) {
                continue;
            }
            runningList.add(account);
        }
        // 处理账号
        for (int i = 0; i < accountList.size(); i++) {
            T account = accountList.get(i);
            boolean processed = isProcessed(account);
            if (processed) {
                continue;
            }
            executorServiceSubmit(account);
        }
    }

    @Override
    public void accountHandlerExpand(T account){
        if(!executeButton.isDisabled()){
            Platform.runLater(() -> setExecuteButtonStatus(true));
        }
        if(!runningList.contains(account)){
            runningList.add(account);
        }
        accountHandlerExpand(account,true);
    }

    /**
     * 设置执行期间的间隔频率
     */
    public Long getIntervalFrequency(){
        return 0L;
    }

   /**
   　* 提交线程任务
     * @param
    * @param account
   　* @return void
   　* @throws
   　* @author DeZh
   　* @date 2024/7/4 23:44
   */
    private void executorServiceSubmit(T account){
        threadPoolExecutor=getExecutorService(threadCount);
        Future<?> future= threadPoolExecutor.submit(()->{
            try {
                if(!runningList.contains(account)){
                    runningList.add(account);
                }
                accountHandlerExpandX(account);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });
        List<Future<?>> futureList = threadMap.get(stage);
        if (CollUtil.isEmpty(futureList)){
            futureList = new ArrayList<>();
        }
        futureList.add(future);
        threadMap.put(stage,futureList);
    }


    public void accountHandlerExpand(T account,boolean isAsyn){
        Long intervalFrequency = getIntervalFrequency();
        if (intervalFrequency > 0){
            ThreadUtil.sleep(intervalFrequency);
        }
        if(isAsyn){
            executorServiceSubmit(account);
        }else{
            accountHandlerExpandX(account);
        }

    }

    public void accountHandlerExpandX(T account){
        boolean hasField = ReflectUtil.hasField(account.getClass(), "hasFinished");
        try {
            // 扣除点数
            pointDedu(account);
            if (hasField){
                ReflectUtil.invoke(account,"setHasFinished",false);
            }
            setAndRefreshNote(account, "正在处理...");
            accountHandler(account);
            setDataStatus(account,true);
        } catch (ServiceException e) {// 业务异常
            LoggerManger.info("ServiceException",e);
            setAndRefreshNote(account,e.getMessage());
            setNote(account,e.getMessage(),"");
            pointIncr(account);
            setDataStatus(account,false);
        }catch (PointDeduException e){// 部分业务抛出异常,但是还是要扣除点数
            pointCost(account,PointUtil.out,e.getFunCode());
            setAndRefreshNote(account,e.getMessage());
            setNote(account,e.getMessage(),"");
            pointIncr(account);
            setDataStatus(account,false);
        }catch (PointCostException e){
            setAndRefreshNote(account,e.getMessage());
            setDataStatus(account,false);
            String type = e.getType();
            // todo 如果返回点数失败怎么处理
            if (PointUtil.in.equals(type)){
            }
        }catch (UnavailableException e){
            //判断是否开启代理
            String proxyMode=PropertiesUtil.getOtherConfig("proxyMode");
            String message=e.getMessage();
            if(StringUtils.isEmpty(proxyMode) || "0".equals(proxyMode)){
                message="操作频繁，请稍后重试。或开启代理模式";
            }
            setAndRefreshNote(account, message);
            pointIncr(account);
            setDataStatus(account,false);
            LoggerManger.info("UnavailableException",e);
        } catch (HttpException e) {
            setAndRefreshNote(account, e.getMessage());
            pointIncr(account);
            setDataStatus(account,false);
            LoggerManger.info("连接异常，请检查网络",e);
        }catch (ResponseTimeoutException e){
            setAndRefreshNote(account, e.getMessage());
            pointIncr(account);
            setDataStatus(account,false);
            LoggerManger.info("响应超时",e);
        } catch (Exception e) {// 程序异常
            setAndRefreshNote(account, "数据处理异常");
            pointIncr(account);
            setDataStatus(account,false);
            LoggerManger.info("数据处理异常",e);
        } finally {
            ReflectUtil.invoke(account,"setFailCount",0);
            setAccountNumLabel();
            if (hasField){
                ReflectUtil.invoke(account,"setHasFinished",true);
            }
            // 方法名称
            String methodName = "clearLoginInfo";

            // 检查并调用方法
            if (ReflectUtil.getMethod(account.getClass(), methodName) != null) {
                ReflectUtil.invoke(account, methodName);
            }

            ThreadUtil.execute(() -> {
                insertLocalHistory(List.of(account));
            });
            runningList.remove(account);
            if(runningList.size()==0 || accountTableView.getItems().size()==0){
                Platform.runLater(() -> setExecuteButtonStatus(false));
                stopExecutorService();
            }
        }
    }


    /**
     * 导入账号按钮点击
     * ⚠注意：如果是一些特殊的解析账号方法,可以自定义说明文案，以及重写TableView.parseAccount方法。可参考 GiftCardBatchRedeemController.java)
     */
    public void openImportAccountView(List<String> formats, ActionEvent actionEvent) {
        String desc = "说明：\n" +
                "    1.格式为: " + AccountImportUtil.buildNote(formats) + ", 或空格分割\n" +
                "    2.一次可以输入多条账户信息，每条账户单独一行; 如果数据中包含“-”符号, 只能使用空格分割。";
        Button button = (Button) actionEvent.getSource();
        // 获取按钮所在的场景
        Scene scene = button.getScene();
        // 获取场景所属的舞台
        Stage stage = (Stage) scene.getWindow();
        openImportAccountView(formats,"导入账号", desc,stage);
    }

    public void openImportAccountView(List<String> formats) {
        openImportAccountView(formats,null);
    }

    public void openImportAccountView(String titile,String desc) {
        openImportAccountView(Collections.emptyList(),titile, desc,null);
    }

    public void openImportAccountView(List<String> formats,String title, String desc,Stage parentStage) {
        if (!CollUtil.isEmpty(formats)) {
            this.formats = formats;
        }
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

        button.setOnAction(event -> {
            List<T> accountList1 = parseAccount(area.getText());
            if (!accountList1.isEmpty()) {
                accountList.addAll(accountList1);
                accountTableView.setItems(accountList);
                setAccountNumLabel();
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

    /**
     * 如果有自己的解析方法,则重写这个方法
     *
     * @param accountStr
     * @return
     */
    public List<T> parseAccount(String accountStr) {
        if (this.clz == null || this.formats.isEmpty()) {
            return Collections.emptyList();
        }
        return new AccountImportUtil().parseAccount(clz, accountStr, formats);
    }


    /**
     * 本地记录按钮点击
     */
    public void localHistoryButtonAction() {
        // 操作区
        String branch = stage != null ? stage.getTitle() : "";
        Label branchLabel = new Label("当前数据分支:" + branch);
        branchLabel.setPrefWidth(310);

        Label keywordsLabel = new Label("输入关键字");
        TextField keywordsTextField = new TextField();

        HBox hbox = new HBox();
        hbox.setSpacing(3);
        Label label1 = new Label("显示");

        ComboBox<String> comboBox = new ComboBox();
        comboBox.setPrefWidth(110);
        comboBox.getItems().addAll("全部","20","50","100","200","500");
        comboBox.setValue("全部");
        comboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER;"); // 设置文本居中
                }
            }
        });
        comboBox.setEditable(true);
        Label label2 = new Label("条");
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().addAll(label1,comboBox,label2);

        Button searchBtn = new Button("搜索");
        searchBtn.setPrefWidth(150);

        Button search100Btn = new Button("显示最新100条数据");
        search100Btn.setPrefWidth(150);



        Button clearBtn = new Button("清空该数据分支数据");
        clearBtn.setPrefWidth(150);

        Label countLabel = new Label("匹配数量：" + "0");
        countLabel.setPrefWidth(180);

        HBox box1 = new HBox();
        box1.setSpacing(15);
        box1.setAlignment(Pos.CENTER_LEFT);
        box1.getChildren().addAll(branchLabel, keywordsLabel, keywordsTextField,hbox, searchBtn, clearBtn, countLabel);

        // 表格区
        HBox box2 = new HBox();
        TableView localHistoryTableView = new TableView();
        localHistoryTableView.setPrefWidth(1180);
        localHistoryTableView.setPrefHeight(490);
        localHistoryTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


        // 动态渲染列,且增加操作时间字段
        for (TableColumn<T, ?> tableViewColumn : this.accountTableView.getColumns()) {
            TableColumn<T, ?> tableColumn = new TableColumn<>(tableViewColumn.getText());
            tableColumn.setId(tableViewColumn.getId());
            tableColumn.setPrefWidth(tableViewColumn.getPrefWidth());
            tableColumn.setCellValueFactory(new PropertyValueFactory<>(tableColumn.getId()));
            localHistoryTableView.getColumns().add(tableColumn);
        }
        // 把序号列删除掉
        localHistoryTableView.getColumns().remove(0);

        // 获取当前controller上的泛型(数据对象)
        Map<Type, Type> typeMap = TypeUtil.getTypeMap(this.getClass());
        Class clz = Account.class;
        if (!typeMap.isEmpty()) {
            for (Map.Entry<Type, Type> typeEntry : typeMap.entrySet()) {
                clz = ReflectUtil.newInstance(typeEntry.getValue().getTypeName()).getClass();
            }
        }

        // 添加入库时间
        if (ReflectUtil.hasField(clz,"createTime")){
            TableColumn<T, String> createTime = new TableColumn<>("入库时间");
            createTime.setPrefWidth(120);
            createTime.setCellValueFactory(new PropertyValueFactory<>("createTime"));
            localHistoryTableView.getColumns().add(createTime);
        }

        // 按钮绑定事件
        Class finalClz = clz;
        searchBtn.setOnAction(actionEvent -> {
            HashMap<Object, Object> params = new HashMap<>();
            params.put("clz_name", ClassUtil.getClassName(this, false));
            if (!StrUtil.isEmpty(keywordsTextField.getText())) {
                params.put("row_json", keywordsTextField.getText().trim());
            }else{
                params.remove("row_json");
            }
            String value = comboBox.getValue().replaceAll(" ","");
            if (StrUtil.isNotEmpty(value) && !"全部".equals(value) && !NumberUtil.isInteger(value)){
                alert("查询条数只能为整数");
                return;
            }
            if (StrUtil.isEmpty(value) || "全部".equals(value)){
                params.remove("limit");
            }else{
                params.put("limit", "LIMIT " + value);
            }

            List<T> accountList = SQLiteUtil.selectLocalHistoryList(params, finalClz);
            countLabel.setText("匹配数量：" + accountList.size());
            localHistoryTableView.getItems().clear();
            localHistoryTableView.getItems().addAll(accountList);
        });

        search100Btn.setOnAction(actionEvent -> {
            HashMap<Object, Object> params = new HashMap<>();
            params.put("clz_name", ClassUtil.getClassName(this, false));
            if (!StrUtil.isEmpty(keywordsTextField.getText())) {
                params.put("row_json", keywordsTextField.getText().trim());
            }
            params.put("limit", "LIMIT 100");
            List<T> accountList = SQLiteUtil.selectLocalHistoryList(params, finalClz);
            countLabel.setText("匹配数量：" + accountList.size());
            localHistoryTableView.getItems().clear();
            localHistoryTableView.getItems().addAll(accountList);
        });

        clearBtn.setOnAction(actionEvent -> {
            // 创建确认对话框
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("确认清空");
            confirmationAlert.setHeaderText(null);
            confirmationAlert.setContentText("您确定要清空该数据分支吗？\n此操作不可撤销，请确认！");
            // 显示对话框并等待用户选择
            Optional<ButtonType> result = confirmationAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                SQLiteUtil.clearLocalHistoryByClzName(ClassUtil.getClassName(this, false));
                localHistoryTableView.getItems().clear();
            }
        });

        box2.getChildren().add(localHistoryTableView);
        //添加右键事件
        localHistoryTableView.setOnContextMenuRequested(contextMenuEvent->{
            Set<String> menuItem =new LinkedHashSet<>(){{
                add(Constant.RightContextMenu.COPY_ALL.getCode());
                add(Constant.RightContextMenu.COPY.getCode());
            }};
            List<String> items=new ArrayList<>(menuItem) ;
            super.onContentMenuClick(contextMenuEvent,localHistoryTableView,items);
        });

        VBox mainVbox = new VBox();
        mainVbox.setSpacing(15);
        mainVbox.setPadding(new Insets(10));
        mainVbox.getChildren().addAll(box1, box2);

        Group root = new Group(mainVbox);
        Stage stage = new Stage();
        stage.setTitle("查看本地记录");
        stage.setScene(new Scene(root, 1200, 550));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
        stage.initStyle(StageStyle.DECORATED);
        stage.setAlwaysOnTop(false);
        String logImg= PropertiesUtil.getConfig("softwareInfo.log.path");
        stage.getIcons().add(new Image(this.getClass().getResource(logImg).toString()));
        stage.showAndWait();
    }

    /**
     * 停止任务按钮点击
     */
    public void stopTaskButtonAction() {
        try {
            if(runningList.size()==0){
                return;
            }
            Platform.runLater(() -> {
                executeButton.setText("正在停止");
                executeButton.setTextFill(Paint.valueOf("#FF0000"));
                executeButton.setDisable(true);
            });
            // 不使用杀线程的方式停止
            List<Future<?>> futureList = threadMap.get(stage);
            if (!CollUtil.isEmpty(futureList)){
                // 立即关闭服务，并尝试停止所有任务,返回待执行的任务集合
                stopExecutorService();
                for (Future<?> future:futureList){
                    future.cancel(true);
                }
            }
            Platform.runLater(() -> {
                setExecuteButtonStatus(false);
            });
            runningList.clear();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            LoggerManger.info("【"+stage.getTitle()+"】" + "停止任务");
        }
    }

    /**
     * 清空列表按钮点击
     */
    public void clearAccountListButtonAction() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("");
        confirm.setContentText("确认删除列表数据？");
        Stage stage = (Stage) confirm.getDialogPane().getScene().getWindow();
        String logImg= PropertiesUtil.getConfig("softwareInfo.log.path");
        stage.getIcons().add(new Image(this.getClass().getResource(logImg).toString()));
        Optional<ButtonType> type = confirm.showAndWait();
        if (type.get()==ButtonType.OK){
        }else{
            return;
        }
        //判断任务是否进行中
        if (validateData()){
            return;
        }
        accountList.clear();
        runningList.clear();
        stopExecutorService();
        setAccountNumLabel();
        accountTableView.refresh();
    }

    public boolean validateData(){
        if(runningList.size()>0){
            alert("有工作正在进行中，无法执行当前操作！", Alert.AlertType.ERROR);
            return true;
        }
        return false;
    }


    /**
     * 插入本地执行记录
     */
    public void insertLocalHistory(List<T> accountList) {
        if (accountList.isEmpty()) {
            return;
        }
        List<Entity> insertList = new ArrayList<>();
        for (T account : accountList) {
            Entity entity = new Entity();
            entity.setTableName("local_history");
            entity.set("clz_name", ClassUtil.getClassName(this, false));
            entity.set("row_json", JSONUtil.toJsonStr(account));
            entity.set("create_time", System.currentTimeMillis());
            insertList.add(entity);
        }
        try {
            Db.use(DataSourceFactory.getDataSource()).insert(insertList);
        } catch (SQLException e) {
            LoggerManger.info("【" + stage.getTitle() + "】" + "插入本地记录失败; data:" + JSONUtil.toJsonStr(insertList),e);
            LocalhistoryTask.entityList.addAll(insertList);
        }
    }

    /**
     * 导出Excel按钮点击
     */
    public void exportExcelButtonAction() {
        TableView tableView = this.accountTableView;
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        StringBuilder clipboardString = new StringBuilder();
        // 输出表头标题
        for (Object column : tableView.getColumns()) {
            TableColumn tableColumn = (TableColumn) column;
            String text = tableColumn.getText();
            clipboardString.append(text);
            clipboardString.append('\t');
        }
        clipboardString.append('\n');

        ObservableList<T> rowList = (ObservableList) tableView.getItems();
        int index=1;
        for (T selectModel:rowList){
            for (Object column : accountTableView.getColumns()) {
                TableColumn tableColumn = (TableColumn) column;
                String id = tableColumn.getId();
                String text = "";
                if (!"seq".equals(id)) {
                    if (ReflectUtil.hasField(selectModel.getClass(), id)) {
                        Object value = ReflectUtil.invoke(
                                selectModel
                                , "get" + id.substring(0, 1).toUpperCase() + id.substring(1));
                        if (value != null && StrUtil.isNotEmpty(value.toString())) {
                            text = value.toString();
                        }
                    }

                }else{
                    text=String.valueOf(index);
                }
                clipboardString.append(text);
                clipboardString.append('\t');
            }
            clipboardString.append('\n');
            index++;
        }
        final ClipboardContent content = new ClipboardContent();
        content.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(content);
        alert("表格中所有数据已复制到剪辑版，并且已经适应Excel。\n" +
                "直接按Ctrl+V粘贴到Excel中!\n", Alert.AlertType.INFORMATION);
    }
    /**
     * 账号是否被处理过
     */
    public boolean isProcessed(T account) {
        boolean hasNote = ReflectUtil.hasField(account.getClass(), "note");
        if (!hasNote) {
            return false;
        }
        Object noteObj = ReflectUtil.getFieldValue(account, "note");
        if (noteObj instanceof StringProperty) {
            StringProperty note = (StringProperty) noteObj;
            return !StrUtil.isEmpty(note.getValue());
        }
        if (noteObj instanceof String) {
            String note = (String) noteObj;
            return !StrUtil.isEmpty(note);
        }
        return !StrUtil.isEmpty(noteObj.toString());
    }


    public void setExecuteButtonStatus(boolean isRunning) {
        // 修改按钮状态
        if (executeButton != null) {
            boolean disabled = executeButton.isDisabled();
            if (isRunning) {
                if (!disabled){
                    executeButton.setText("正在查询");
                    executeButton.setTextFill(Paint.valueOf("#FF0000"));
                    executeButton.setDisable(true);
                }
            } else {
                if (disabled){
                    executeButton.setTextFill(Paint.valueOf("#238142"));
                    executeButton.setText("开始执行");
                    executeButton.setDisable(false);
                }
            }
        }
        if (importAccountButton != null){
            boolean disabled = importAccountButton.isDisabled();
            if (isRunning != disabled){
                importAccountButton.setDisable(isRunning);
            }
        }
        if (stopButton != null){
            boolean disabled = stopButton.isDisabled();
            if (isRunning == disabled){
                stopButton.setDisable(!isRunning);
            }
        }
    }

    @Override
    public void setAccountNumLabel() {
        int successNum=0;
        int failNum=0;
        if(accountList.size()>0){
           T account= accountList.get(0);
            boolean hasDataStatus = ReflectUtil.hasField(account.getClass(), "dataStatus");
            if (hasDataStatus) {
                for(T acc:accountList){
                    String dataStatus = ReflectUtil.invoke(acc, "getDataStatus");
                    if("1".equals(dataStatus)){
                        successNum++;
                    }else if("0".equals(dataStatus)){
                        failNum++;
                    }
                }

            }
        }
        int finalSuccessNum = successNum;
        int finalFailNum = failNum;
        Platform.runLater(()->{
            if(null!=accountNumLabel){
                accountNumLabel.setText(String.valueOf(accountList.size()));
            }
            if(null!=successNumLabel){
                successNumLabel.setText(String.valueOf(finalSuccessNum));
            }
            if(null!=failNumLabel){
                failNumLabel.setText(String.valueOf(finalFailNum));
            }
        });
    }


    public String getAccountNo(T account){
        Object account1 = ReflectUtil.getFieldValue(account, "account");
        if (account1 instanceof SimpleStringProperty){
            return ((SimpleStringProperty) account1).getValue();
        }else{
            return account1.toString();
        }
    }
    public Object getAccountField(T account,String field){
        Object fieldValue = ReflectUtil.getFieldValue(account, field);
        if (fieldValue instanceof SimpleStringProperty){
            return ((SimpleStringProperty) fieldValue).getValue();
        } else if (fieldValue instanceof SimpleIntegerProperty){
            return ((SimpleIntegerProperty) fieldValue).getValue();
        }else{
            return fieldValue;
        }
    }

    /**
     * 点数扣除
     * @param account
     */
    public void pointDedu(T account){
        pointCost(account,PointUtil.out,funCode);
    }

    /**
     * 点数返回
     */
    public void pointIncr(T account){
        pointCost(account,PointUtil.in,funCode);
    }

    /**
     * 点数操作
     */
    public void pointCost(T account,String type,String funcCode){
        Object note=getAccountField(account,"note");
        Map<String,String> pointCost = PointUtil.pointCost(funcCode,type,getAccountNo(account),null==note?"":note.toString());
        if(!Constant.SUCCESS.equals(pointCost.get("code"))){
            throw new PointCostException(type,pointCost.get("msg"));
        }
    }

    public void setDataStatus(T account,Boolean success){
        boolean hasDataStatus = ReflectUtil.hasField(account.getClass(), "dataStatus");
        if (hasDataStatus) {
            ReflectUtil.invoke(account, "setDataStatus",success ? "1" : "0");
        }
    }

    /**
     * 设置账号的执行信息,以及刷新列表
     */
    public void setAndRefreshNote(T account, String note) {
        setAndRefreshNote(account, note,"");
    }
    protected void tableRefreshAndInsertLocal(T account, String message){
        setAndRefreshNote(account,message);
        insertLocalHistory(List.of(account));
    }
    /**
     * 设置账号的执行信息,以及刷新列表保存本地记录
     */
    public void setAndRefreshNote(T account, String note, String defaultNote) {
        note = StrUtil.isEmpty(note) ? defaultNote : note;
        boolean hasNote = ReflectUtil.hasField(account.getClass(), "note");
        if (hasNote) {
            ReflectUtil.invoke(account, "setNote", note);
        }
        accountTableView.refresh();

        // 插入本地日志
        boolean hasField = ReflectUtil.hasField(account.getClass(), "taskNo");
        if (hasField){
            String taskNo = (String) ReflectUtil.getFieldValue(account,"taskNo");
            LoggerManger.info(StrUtil.isEmpty(taskNo) ? note : taskNo + "->" + note);
        }else{
            String account1 = (String) ReflectUtil.getFieldValue(account,"account");
            LoggerManger.info(account1 + "->" + note);
        }
    }

    public void setNote(T account, String note, String defaultNote) {
        note = StrUtil.isEmpty(note) ? defaultNote : note;
        boolean hasNote = ReflectUtil.hasField(account.getClass(), "note");
        if (hasNote) {
            ReflectUtil.invoke(account, "setNote", note);
        }
    }
    public void appendAndRefreshNote(T account, String note) {
        appendAndRefreshNote(account, "", note);
    }

    public void appendAndRefreshNote(T account, String note, String defaultNote) {
        note = StrUtil.isEmpty(note) ? defaultNote : note;
        boolean hasNote = ReflectUtil.hasField(account.getClass(), "note");
        if (hasNote) {
            String note1 = ReflectUtil.invoke(account, "getNote");
            note1 = StrUtil.isEmpty(note1) ? "" : note1;
            note1 = note1.startsWith("正在处理...") ? "" : note1;
            note = note1 + note + "。";
            ReflectUtil.invoke(account, "setNote", note);
        }
        accountTableView.refresh();
    }

    public void checkAndThrowUnavailableException(HttpResponse response){
        if (response != null && response.getStatus() == 503){
            throw new UnavailableException();
        }
    }

    /*
     **获取线程池
     * @param
     * @return java.util.concurrent.ScheduledExecutorService
     * @throws
     * @author DeZh
     * @date 2024/7/8 15:33
     */
    protected ThreadPoolExecutor getExecutorService(int threadCount){
        // 核心线程数
        int corePoolSize = threadCount;
        // 最大线程数
        int maximumPoolSize = threadCount;
        // 空闲线程存活时间（单位：秒）
        long keepAliveTime = 60L;
        if(null==threadPoolExecutor || threadPoolExecutor.isShutdown()){
            threadPoolExecutor = new ThreadPoolExecutor(
                    corePoolSize,
                    maximumPoolSize,
                    keepAliveTime,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>());
        }
        return threadPoolExecutor;
    }
    /*
     ** 停止线程池服务
     * @param
     * @return java.util.concurrent.ScheduledExecutorService
     * @throws
     * @author DeZh
     * @date 2024/7/8 15:33
     */
    protected void stopExecutorService(){
        if(null==threadPoolExecutor || threadPoolExecutor.isShutdown()){

        }else{
            //方法会尝试立即停止所有正在执行的任务，并返回等待执行的任务列表
            threadPoolExecutor.shutdownNow();
        }
    }
    protected String createId(String account,String password){
        return account+":"+password;
    }
}

package com.sgswit.fx.controller.common;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.*;
import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.LoginInfo;
import com.sgswit.fx.utils.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.lang.reflect.Type;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * account表格视图
 *
 * @author HeHongdong
 */
public class CustomTableView<T> extends CommRightContextMenuView<T> {
    // 登录成功的账号缓存(缓存5分钟,能刷新)
    protected static TimedCache<String, LoginInfo> loginSuccessMap = CacheUtil.newTimedCache(5*60*1000);
    static {
        loginSuccessMap.schedulePrune(300000);
    }
    private static final Log logger = LogFactory.get();
    public Set<String> menuItem =new LinkedHashSet<>(){{
        add(Constant.RightContextMenu.DELETE.getCode());
        add(Constant.RightContextMenu.REEXECUTE.getCode());
        add(Constant.RightContextMenu.COPY.getCode());
    }};

    @FXML
    protected Button executeButton;

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

    protected ReentrantLock reentrantLock = new ReentrantLock();

    protected AtomicInteger atomicInteger = new AtomicInteger(0);
    protected Integer threadCount = Integer.valueOf(PropertiesUtil.getOtherConfig("ThreadCount","3"));

    private Class clz = Account.class;
    private List<String> formats;

    protected String funCode="0";
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);

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

        // 数据绑定
        ObservableList<TableColumn<T, ?>> columns = accountTableView.getColumns();
        for (TableColumn<T, ?> column : columns) {
            // 序号自动增长
            if ("seq".equals(column.getId())) {
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
                column.setCellValueFactory(new PropertyValueFactory(column.getId()));
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

        // 修改按钮为执行状态
        setExecuteButtonStatus(true);

        // 每一次执行前都释放锁
        if (reentrantLock.isLocked()) {
            reentrantLock.unlock();
            ThreadUtil.sleep(500);
        }

        // 此处的线程是为了处理,按钮状态等文案显示
        ThreadUtil.execute(() -> {
            // 处理账号
            for (int i = 0; i < accountList.size(); i++) {
                T account = accountList.get(i);
                if (reentrantLock.isLocked()) {
                    return;
                }
                boolean processed = isProcessed(account);
                if (processed) {
                    continue;
                }

                while (atomicInteger.get() >= threadCount){
                    ThreadUtil.sleep(1000);
                }

                atomicInteger.incrementAndGet();
                accountHandlerExpand(account);
                ThreadUtil.sleep(1000);

                if (i == accountList.size() - 1) {
                    // 任务执行结束, 恢复执行按钮状态
                    Platform.runLater(() -> setExecuteButtonStatus(false));
                }
            }
        });
    }

    @Override
    public void accountHandlerExpand(T account){
        accountHandlerExpand(account,true);
    }

    public void accountHandlerExpand(T account,boolean isAsyn){
        if (isAsyn){
            new Thread(() -> accountHandlerExpandX(account)).start();
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
            setAndRefreshNote(account, "执行中");
            accountHandler(account);
            setDataStatus(account,true);
        } catch (ServiceException e) {// 业务异常
            setAndRefreshNote(account,e.getMessage());
            pointIncr(account);
            setDataStatus(account,false);
            LoggerManger.info(e.getMessage());
        }catch (PointDeduException e){// 部分业务抛出异常,但是还是要扣除点数
            pointCost(account,PointUtil.out,e.getFunCode());
            setAndRefreshNote(account,e.getMessage());
            pointIncr(account);
            setDataStatus(account,false);
            LoggerManger.info(e.getMessage());
        }
        catch (PointCostException e){
            setAndRefreshNote(account,e.getMessage());
            setDataStatus(account,false);
            String type = e.getType();
            // todo 如果返回点数失败怎么处理
            if (PointUtil.in.equals(type)){
            }
        }catch (UnavailableException e){
            setAndRefreshNote(account, e.getMessage());
            pointIncr(account);
            setDataStatus(account,false);
            LoggerManger.info("UnavailableException",e);
        } catch (IORuntimeException e) {
            setAndRefreshNote(account, "连接异常，请检查网络");
            pointIncr(account);
            setDataStatus(account,false);
            LoggerManger.info("连接异常，请检查网络",e);
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
            ThreadUtil.execute(() -> {
                insertLocalHistory(List.of(account));
            });
            atomicInteger.decrementAndGet();
        }
    }

    /**
     * 导入账号按钮点击
     * ⚠注意：如果是一些特殊的解析账号方法,可以自定义说明文案，以及重写TableView.parseAccount方法。可参考GiftCardBatchRedeemController.java)
     */
    public void openImportAccountView(List<String> formats, ActionEvent actionEvent) {
        String desc = "说明：\n" +
                "    1.格式为: " + AccountImportUtil.buildNote(formats) + "\n" +
                "    2.一次可以输入多条账户信息，每条账户单独一行; 如果数据中有“-”符号,则使用{-}替换。";
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

    public  void  openImportAccountView(List<String> formats,String title, String desc,Stage parentStage) {
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
        box1.getChildren().addAll(branchLabel, keywordsLabel, keywordsTextField, searchBtn, search100Btn, clearBtn, countLabel);

        // 表格区
        HBox box2 = new HBox();
        javafx.scene.control.TableView localHistoryTableView = new javafx.scene.control.TableView();
        localHistoryTableView.setPrefWidth(1180);

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
                params.put("row_json", keywordsTextField.getText());
            }else{
                params.remove("row_json");
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
                params.put("row_json", keywordsTextField.getText());
            }
            params.put("limit", "LIMIT 100");
            List<T> accountList = SQLiteUtil.selectLocalHistoryList(params, finalClz);
            countLabel.setText("匹配数量：" + accountList.size());
            localHistoryTableView.getItems().clear();
            localHistoryTableView.getItems().addAll(accountList);
        });
        clearBtn.setOnAction(actionEvent -> {
            SQLiteUtil.clearLocalHistoryByClzName(ClassUtil.getClassName(this, false));
            localHistoryTableView.getItems().clear();
        });

        box2.getChildren().add(localHistoryTableView);

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
        String logImg= PropertiesUtil.getConfig("softwareInfo.log.path");
        stage.getIcons().add(new Image(this.getClass().getResource(logImg).toString()));
        stage.showAndWait();
    }

    /**
     * 停止任务按钮点击
     */
    public void stopTaskButtonAction() {
        try {
            // 停止任务, 恢复按钮状态
            setExecuteButtonStatus(false);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            reentrantLock.lock();
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
        for(T account:accountList){
            Boolean hasFinished= (Boolean) ReflectUtil.getFieldValue(account, "hasFinished");
            if(!hasFinished){
                alert("有工作正在进行中，无法执行当前操作！", Alert.AlertType.ERROR);
                return;
            }
        }
        accountList.clear();
        setAccountNumLabel();
        accountTableView.refresh();
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
            DbUtil.use().insert(insertList);
        } catch (SQLException e) {
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
            if (isRunning) {
                executeButton.setText("正在查询");
                executeButton.setTextFill(Paint.valueOf("#FF0000"));
                executeButton.setDisable(true);
            } else {
                executeButton.setTextFill(Paint.valueOf("#238142"));
                executeButton.setText("开始执行");
                executeButton.setDisable(false);
            }
        }
        if (importAccountButton != null){
            importAccountButton.setDisable(isRunning);
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
                    if(dataStatus.equals("1")){
                        successNum++;
                    }else if(dataStatus.equals("0")){
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
        Map<String,String> pointCost = PointUtil.pointCost(funcCode,type,getAccountNo(account));
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
        Platform.runLater(new Task<Integer>() {
            @Override
            protected Integer call() {
                setAndRefreshNote(account, note,"");
                return 1;
            }
        });
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
            note1 = note1.startsWith("执行中") ? "" : note1;
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

    protected String createId(String account,String password){
        return account+":"+password;
    }
}

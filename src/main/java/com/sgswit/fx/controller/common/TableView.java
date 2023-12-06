package com.sgswit.fx.controller.common;

import cn.hutool.core.lang.Console;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.*;
import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AccountImportUtil;
import com.sgswit.fx.utils.SQLiteUtil;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.lang.reflect.Type;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * account表格视图
 */
public class TableView<T> extends CommonView {

    @FXML
    protected Button executeButton;

    @FXML
    public javafx.scene.control.TableView<T> accountTableView;

    @FXML
    protected Label accountNumLable;

    protected ObservableList<T> accountList = FXCollections.observableArrayList();

    StageEnum stage;

    ReentrantLock reentrantLock = new ReentrantLock();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);

        // 获取当前stage
        if ( url != null ){
            String file = url.getFile();
            if (file != null){
                String view = file.substring(file.indexOf("views/"));
                LinkedHashMap<String, StageEnum> stageMap = EnumUtil.getEnumMap(StageEnum.class);
                stageMap.forEach((stageName,stageEnum)->{
                    if (view.equals(stageEnum.getView())){
                        stage = stageEnum;
                    }
                });
            }
        }

        // 数据绑定
        ObservableList<TableColumn<T, ?>> columns = accountTableView.getColumns();
        for (TableColumn<T, ?> column : columns) {
            column.setCellValueFactory(new PropertyValueFactory(column.getId()));
        }
    }

    /**
     * 执行前, 一般做一些参数校验
     */
    public boolean executeButtonActionBefore(){
        return true;
    }

    /**
     * 按钮点击
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

        // 修改按钮为执行状态
        setExecuteButtonStatus(true);

        // 每一次执行前都释放锁
        if (reentrantLock.isLocked()) {
            reentrantLock.unlock();
            ThreadUtil.sleep(500);
        }

        // 处理账号
        Runnable task = () -> {
            for (int i = 0; i < accountList.size(); i++) {
                T account = accountList.get(i);
                if (reentrantLock.isLocked()){
                    return;
                }
                boolean processed = isProcessed(account);
                if (processed){
                    continue;
                }

                // 有些方法执行太快会显示过于频繁,每处理十个账号休息1s
                if (i != 0 && i % 10 == 0){
                    ThreadUtil.sleep(500);
                }
                ThreadUtil.sleep(500);
                ThreadUtil.execute(()->{
                    setAndRefreshNote(account,"执行中",false);
                    accountHandler(account);
                });
            }

            Platform.runLater(() -> {
                // 任务执行结束, 恢复执行按钮状态
                setExecuteButtonStatus(false);
            });
        };
        ThreadUtil.execute(task);

    }

    public void setExecuteButtonStatus(boolean isRunning){
        // 修改按钮状态
        if (executeButton != null) {
            if (isRunning){
                executeButton.setText("正在查询");
                executeButton.setTextFill(Paint.valueOf("#FF0000"));
                executeButton.setDisable(true);
            } else {
                executeButton.setTextFill(Paint.valueOf("#238142"));
                executeButton.setText("开始执行");
                executeButton.setDisable(false);
            }
        }
    }

    /**
     * 每一个账号的处理器
     */
    public void accountHandler(T account){}

    /**
     * 停止任务按钮点击
     */
    public void stopTaskButtonAction(){
        reentrantLock.lock();
        // 停止任务, 恢复按钮状态
        setExecuteButtonStatus(false);
    }

    /**
     * 导入账号
     */
    public void openImportAccountView(String... formats){
        openImportAccountView(Account.class,formats);
    }

    /**
     * todo 如果不是Account.class, 请调用此方法
     */
    public void openImportAccountView(Class clz,String... formats){

        Stage stage = new Stage();
        Insets padding = new Insets(0, 0, 0, 20);

        Label label1 = new Label("说明：");
        Label label2 = new Label("1.格式为: " + AccountImportUtil.buildNote(formats) + "。");
        label2.setPadding(padding);
        Label label3 = new Label("2.一次可以输入多条账户信息，每条账户单独一行; 如果数据中有“-”符号,则使用{-}替换。");
        label3.setPadding(padding);

        VBox vBox = new VBox();
        vBox.setSpacing(5);
        vBox.setPadding(new Insets(5, 5, 5, 5));
        vBox.getChildren().addAll(label1,label2,label3);

        TextArea area = new TextArea();
        area.setPrefHeight(250);
        area.setPrefWidth(560);

        VBox vBox2 = new VBox();
        vBox2.setPadding(new Insets(0,0,0,205));
        Button button = new Button("导入账号");
        button.setTextFill(Paint.valueOf("#067019"));
        button.setPrefWidth(150);
        button.setPrefHeight(50);

        button.setOnAction(event -> {
            List<T> accountList1 = new AccountImportUtil().parseAccount(area.getText(),Arrays.asList(formats), clz);
            accountList.addAll(accountList1);

            for (int i = 0; i < accountList.size(); i++) {
                ReflectUtil.invoke(
                        accountList.get(i)
                        , "setSeq"
                        , i+1);
            }
            accountTableView.setItems(accountList);
            accountNumLable.setText(accountList.size()+"");
            stage.close();
        });
        vBox2.getChildren().addAll(button);

        VBox mainVbox = new VBox();
        mainVbox.setSpacing(20);
        mainVbox.setPadding(new Insets(20));
        mainVbox.getChildren().addAll(vBox,area,vBox2);

        Group root = new Group(mainVbox);
        stage.setTitle("账号导入");
        stage.setScene(new Scene(root, 600, 450));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.initStyle(StageStyle.UTILITY);
        stage.showAndWait();
    }

    /**
     * 清空列表按钮点击
     */
    public void clearAccountListButtonAction(){
        accountList.clear();
        accountNumLable.setText("");
    }

    /**
     * 插入本地执行记录
     */
    public void insertLocalHistory(List<T> accountList){
        if (accountList.isEmpty()){
            return;
        }

        List<Entity> insertList = new ArrayList<>();
        for (T account : accountList) {
            Entity entity = new Entity();
            entity.setTableName("local_history");
            entity.set("clz_name",ClassUtil.getClassName(this,false));
            entity.set("row_json", JSONUtil.toJsonStr(account));
            entity.set("create_time",System.currentTimeMillis());
            insertList.add(entity);
        }
        try {
            DbUtil.use().insert(insertList);
        } catch (SQLException e) {
            Console.log("SQLite保存失败！ saveList: {}",insertList);
        }
    }

    /**
     * 本地记录按钮点击
     */
    public void localHistoryButtonAction(){
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
        box1.getChildren().addAll(branchLabel,keywordsLabel,keywordsTextField,searchBtn,search100Btn,clearBtn,countLabel);

        // 表格区
        HBox box2 = new HBox();
        javafx.scene.control.TableView localHistoryTableView = new javafx.scene.control.TableView();
        localHistoryTableView.setPrefWidth(1180);

        // 动态渲染列,且增加操作时间字段
        for (TableColumn<T, ?> tableViewColumn : this.accountTableView.getColumns()) {
            TableColumn<T,?> tableColumn = new TableColumn<>(tableViewColumn.getText());
            tableColumn.setId(tableViewColumn.getId());
            tableColumn.setPrefWidth(tableViewColumn.getPrefWidth());
            tableColumn.setCellValueFactory(new PropertyValueFactory<>(tableColumn.getId()));
            localHistoryTableView.getColumns().add(tableColumn);
        }
        // 把序号列删除掉
        localHistoryTableView.getColumns().remove(0);
        // 添加入库时间
        TableColumn<Account,String> createTime = new TableColumn<>("入库时间");
        createTime.setPrefWidth(120);
        createTime.setCellValueFactory(new PropertyValueFactory<>("createTime"));
        localHistoryTableView.getColumns().add(createTime);

        // 按钮绑定事件
        HashMap<Object, Object> params = new HashMap<>();
        params.put("clz_name",ClassUtil.getClassName(this, false));
        Map<Type, Type> typeMap = TypeUtil.getTypeMap(this.getClass());
        // 获取当前controller上的泛型(数据对象)
        Class clz = Account.class;
        if (!typeMap.isEmpty()){
            for (Map.Entry<Type, Type> typeEntry : typeMap.entrySet()) {
                clz = ReflectUtil.newInstance(typeEntry.getValue().getTypeName()).getClass();
            }
        }
        Class finalClz = clz;
        searchBtn.setOnAction(actionEvent -> {
            if (!StrUtil.isEmpty(keywordsTextField.getText())){
                params.put("row_json",keywordsTextField.getText());
            }
            List<T> accountList = SQLiteUtil.selectLocalHistoryList(params, finalClz);
            countLabel.setText("匹配数量：" + accountList.size());
            localHistoryTableView.getItems().clear();
            localHistoryTableView.getItems().addAll(accountList);
        });

        search100Btn.setOnAction(actionEvent -> {
            if (!StrUtil.isEmpty(keywordsTextField.getText())){
                params.put("row_json",keywordsTextField.getText());
            }
            params.put("limit","LIMIT 100");
            List<T> accountList = SQLiteUtil.selectLocalHistoryList(params,finalClz);
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
        mainVbox.getChildren().addAll(box1,box2);

        Group root = new Group(mainVbox);
        Stage stage = new Stage();
        stage.setTitle("查看本地记录");
        stage.setScene(new Scene(root, 1200, 550));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
        stage.initStyle(StageStyle.DECORATED);
        stage.showAndWait();
    }

    /**
     * 导出Excel按钮点击
     */
    public void exportExcelButtonAction(){
        alert("导出Excel按钮点击");
    }

    /**
     * 账号是否被处理过
     */
    public boolean isProcessed(T account){
        boolean hasNote = ReflectUtil.hasField(account.getClass(), "note");
        if (!hasNote){
            return false;
        }
        Object noteObj = ReflectUtil.getFieldValue(account, "note");
        if (noteObj instanceof StringProperty){
            StringProperty note = (StringProperty) noteObj;
            return !StrUtil.isEmpty(note.getValue());
        }
        if (noteObj instanceof String){
            String note = (String) noteObj;
            return !StrUtil.isEmpty(note);
        }
        return !StrUtil.isEmpty(noteObj.toString());
    }

    /**
     * 设置账号的执行信息,以及刷新列表保存本地记录
     */
    public void setAndRefreshNote(T account,String note){
        setAndRefreshNote(account,note,true);
    }

    /**
     * 设置账号的执行信息,以及刷新列表保存本地记录
     */
    public void setAndRefreshNote(T account,String note,boolean saveLog){
        boolean hasNote = ReflectUtil.hasField(account.getClass(), "note");
        if (hasNote){
            ReflectUtil.invoke(account,"setNote",note);
        }
        accountTableView.refresh();
        if (saveLog){
            insertLocalHistory(List.of(account));
        }
    }
}

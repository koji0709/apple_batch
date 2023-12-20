package com.sgswit.fx.controller.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.*;
import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AccountImportUtil;
import com.sgswit.fx.utils.PListUtil;
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
import javafx.util.Callback;

import java.lang.reflect.Type;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

/**
 * account表格视图
 *
 * @author HeHongdong
 */
public class CustomTableView<T> extends CommonView {

    @FXML
    protected Button executeButton;

    @FXML
    public javafx.scene.control.TableView<T> accountTableView;

    @FXML
    protected Label accountNumLable;

    protected ObservableList<T> accountList = FXCollections.observableArrayList();

    StageEnum stage;

    ReentrantLock reentrantLock = new ReentrantLock();

    private Class clz = Account.class;
    private List<String> formats;
    private static ExecutorService executor = ThreadUtil.newExecutor(5);

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
            } else {
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
        if (isProcessed(accountList.get(accountList.size()-1))){
            alert("账号都已处理！");
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
                if (reentrantLock.isLocked()) {
                    return;
                }
                boolean processed = isProcessed(account);
                if (processed) {
                    continue;
                }

                // 有些方法执行太快会显示过于频繁,每处理十个账号休息1s
                if (i != 0 && i % 10 == 0) {
                    ThreadUtil.sleep(500);
                }
                ThreadUtil.sleep(500);
                ThreadUtil.execute(() -> {
                    setAndRefreshNote(account, "执行中", false);
                    try {
                        accountHandler(account);
                    } catch (Exception e) {
                        setAndRefreshNote(account, "接口数据处理异常", true);
                        e.printStackTrace();
                    }
                });
                if (i == accountList.size() - 1) {
                    // 任务执行结束, 恢复执行按钮状态
                    Platform.runLater(() -> setExecuteButtonStatus(false));
                }
            }

        };

        executor.execute(task);
    }

    /**
     * 每一个账号的处理器
     */
    public void accountHandler(T account) {
    }

    /**
     * 导入账号按钮点击
     * ⚠注意：如果是一些特殊的解析账号方法,可以自定义说明文案，以及重写TableView.parseAccount方法。可参考GiftCardBatchRedeemController.java)
     */
    public void openImportAccountView(List<String> formats) {
        String desc = "说明：\n" +
                "    1.格式为: " + AccountImportUtil.buildNote(formats) + "\n" +
                "    2.一次可以输入多条账户信息，每条账户单独一行; 如果数据中有“-”符号,则使用{-}替换。";
        openImportAccountView(formats, desc);
    }

    public void openImportAccountView(List<String> formats, String desc) {
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
        Button button = new Button("导入账号");
        button.setTextFill(Paint.valueOf("#067019"));
        button.setPrefWidth(150);
        button.setPrefHeight(50);

        button.setOnAction(event -> {
            List<T> accountList1 = parseAccount(area.getText());
            if (!accountList1.isEmpty()) {
                accountList.addAll(accountList1);
                accountTableView.setItems(accountList);
                accountNumLable.setText(accountList.size() + "");
            }
            stage.close();
        });
        vBox2.getChildren().addAll(button);

        VBox mainVbox = new VBox();
        mainVbox.setSpacing(20);
        mainVbox.setPadding(new Insets(20));
        mainVbox.getChildren().addAll(descLabel, area, vBox2);

        Group root = new Group(mainVbox);
        stage.setTitle("账号导入");
        stage.setScene(new Scene(root, 600, 450));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.initStyle(StageStyle.UTILITY);
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
            Console.log("需要先确定TableView上的泛型,以及初始化导入账号格式");
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
        // 添加入库时间
        TableColumn<Account, String> createTime = new TableColumn<>("入库时间");
        createTime.setPrefWidth(120);
        createTime.setCellValueFactory(new PropertyValueFactory<>("createTime"));
        localHistoryTableView.getColumns().add(createTime);

        // 按钮绑定事件
        HashMap<Object, Object> params = new HashMap<>();
        params.put("clz_name", ClassUtil.getClassName(this, false));
        // 获取当前controller上的泛型(数据对象)
        Map<Type, Type> typeMap = TypeUtil.getTypeMap(this.getClass());
        Class clz = Account.class;
        if (!typeMap.isEmpty()) {
            for (Map.Entry<Type, Type> typeEntry : typeMap.entrySet()) {
                clz = ReflectUtil.newInstance(typeEntry.getValue().getTypeName()).getClass();
            }
        }
        Class finalClz = clz;
        searchBtn.setOnAction(actionEvent -> {
            if (!StrUtil.isEmpty(keywordsTextField.getText())) {
                params.put("row_json", keywordsTextField.getText());
            }
            List<T> accountList = SQLiteUtil.selectLocalHistoryList(params, finalClz);
            countLabel.setText("匹配数量：" + accountList.size());
            localHistoryTableView.getItems().clear();
            localHistoryTableView.getItems().addAll(accountList);
        });

        search100Btn.setOnAction(actionEvent -> {
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
        stage.showAndWait();
    }

    /**
     * 停止任务按钮点击
     */
    public void stopTaskButtonAction() {
        reentrantLock.lock();
        // 停止任务, 恢复按钮状态
        setExecuteButtonStatus(false);
    }

    /**
     * 清空列表按钮点击
     */
    public void clearAccountListButtonAction() {
        accountList.clear();
        accountNumLable.setText("");
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
            Console.log("SQLite保存失败！ saveList: {}", insertList);
        }
    }

    /**
     * 导出Excel按钮点击
     */
    public void exportExcelButtonAction() {
        alert("导出Excel按钮点击");
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
    }

    /**
     * 设置账号的执行信息,以及刷新列表保存本地记录
     */
    public void setAndRefreshNote(T account, String note) {
        setAndRefreshNote(account, note, true);
    }

    /**
     * 设置账号的执行信息,以及刷新列表保存本地记录
     */
    public void setAndRefreshNote(T account, String note, String defaultNote) {
        note = StrUtil.isEmpty(note) ? defaultNote : note;
        setAndRefreshNote(account, note, true);
    }

    /**
     * 设置账号的执行信息,以及刷新列表保存本地记录
     */
    public void setAndRefreshNote(T account, String note, boolean saveLog) {
        boolean hasNote = ReflectUtil.hasField(account.getClass(), "note");
        if (hasNote) {
            ReflectUtil.invoke(account, "setNote", note);
        }
        accountTableView.refresh();
        if (saveLog) {
            ThreadUtil.execute(() -> {
                insertLocalHistory(List.of(account));
            });
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
            note = note1 + note + ";";
            ReflectUtil.invoke(account, "setNote", note);
        }
        accountTableView.refresh();
    }

}

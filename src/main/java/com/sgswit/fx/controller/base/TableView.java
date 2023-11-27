package com.sgswit.fx.controller.base;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReferenceUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AccountImportUtil;
import com.sgswit.fx.utils.SQLiteUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;

/**
 * account表格视图
 */
public class TableView<T> extends CommonView {

    @FXML
    public javafx.scene.control.TableView<T> accountTableView;

    @FXML
    protected Label accountNumLable;

    protected ObservableList<T> accountList = FXCollections.observableArrayList();

    /**
     * 导入账号
     */
    public void openImportAccountView(String... formats){

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
            // 数据绑定
            ObservableList<TableColumn<T, ?>> columns = accountTableView.getColumns();
            for (TableColumn<T, ?> column : columns) {
                column.setCellValueFactory(new PropertyValueFactory(column.getId()));
            }

            List<T> accountList1 = new AccountImportUtil().parseAccount(area.getText(),Arrays.asList(formats),Account.class);
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
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
        stage.initStyle(StageStyle.DECORATED);
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
        Label branchLabel = new Label("当前数据分支:" + "国家区域余额");
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

        searchBtn.setOnAction(actionEvent -> {
            if (!StrUtil.isEmpty(keywordsTextField.getText())){
                params.put("row_json",keywordsTextField.getText());
            }
            List<Account> accountList = SQLiteUtil.selectLocalHistoryList(params,Account.class);
            countLabel.setText("匹配数量：" + accountList.size());
            localHistoryTableView.getItems().clear();
            localHistoryTableView.getItems().addAll(accountList);
        });

        search100Btn.setOnAction(actionEvent -> {
            if (!StrUtil.isEmpty(keywordsTextField.getText())){
                params.put("row_json",keywordsTextField.getText());
            }
            params.put("limit","LIMIT 100");
            List<Account> accountList = SQLiteUtil.selectLocalHistoryList(params,Account.class);
            countLabel.setText("匹配数量：" + accountList.size());
            localHistoryTableView.getItems().clear();
            localHistoryTableView.getItems().addAll(accountList);
        });
        clearBtn.setOnAction(actionEvent -> {
            SQLiteUtil.clearLocalHistoryByClzName(ClassUtil.getClassName(this, false));
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
     * 停止任务按钮点击
     */
    public void stopTaskButtonAction(){
        alert("停止任务按钮点击");
    }

    /**
     * 账号是否被处理过
     * @param account
     * @return
     */
    public boolean isProcessed(Account account){
        return !StrUtil.isEmpty(account.getNote());
    }

    public void setAndRefreshNote(Account account,String note){
        account.setNote(note);
        accountTableView.refresh();
    }
}

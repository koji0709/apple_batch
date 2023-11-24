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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
public class TableView extends CommonView {

    @FXML
    public javafx.scene.control.TableView<Account> accountTableView;

    @FXML
    protected Label accountNumLable;

    protected ObservableList<Account> accountList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 数据绑定
        ObservableList<TableColumn<Account, ?>> columns = accountTableView.getColumns();
        for (TableColumn<Account, ?> column : columns) {
            column.setCellValueFactory(new PropertyValueFactory(column.getId()));
        }
    }

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
            List<Account> accountList1 = AccountImportUtil.parseAccount(area.getText(),Arrays.asList(formats));
            accountList.addAll(accountList1);
            for (int i = 0; i < accountList.size(); i++) {
                accountList.get(i).setSeq(i+1);
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
    }

    /**
     * 本地记录按钮点击
     */
    public void localHistoryButtonAction(){
        ObservableList<TableColumn<Account, ?>> columns = this.accountTableView.getColumns();

        List<Entity> localHistoryList = new ArrayList<>();
        try {
            HashMap<Object, Object> params = new HashMap<>();
            params.put("clz_name",ClassUtil.getClassName(this, false));
            localHistoryList = DbUtil.use().query("SELECT * FROM local_history ORDER BY create_time DESC LIMIT 100",params);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<Account> accountList = new ArrayList<>();
        if (!localHistoryList.isEmpty()){
            for (Entity entity : localHistoryList) {
                String rowJson = entity.getStr("row_json");
                JSON dbAccount = JSONUtil.parse(rowJson);
                Account account = new Account();
                for (TableColumn<Account, ?> column : columns) {
                    String colName = column.getId();
                    Object colValue = dbAccount.getByPath(colName);
                    if (colValue != null){
                        ReflectUtil.invoke(
                                account
                                , "set" + colName.substring(0, 1).toUpperCase() + colName.substring(1)
                                , colValue);
                    }
                }
                accountList.add(account);
            }
        }

        // todo 打开弹出框,动态渲染表格
        Stage stage = new Stage();
        javafx.scene.control.TableView tableView = new javafx.scene.control.TableView();

        tableView.getColumns().addAll(columns);
        tableView.getItems().addAll(accountList);


        VBox mainVbox = new VBox();
        mainVbox.setSpacing(30);
        mainVbox.setPadding(new Insets(20));
        mainVbox.getChildren().addAll(tableView);

        Group root = new Group(mainVbox);
        stage.setTitle("查看本地记录");
        stage.setScene(new Scene(root, 1000, 650));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.initStyle(StageStyle.UTILITY);
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
     * 插入本地执行记录
     */
    public void insertLocalHistory(List<Account> accountList){
        if (accountList.isEmpty()){
            return;
        }

        List<Entity> insertList = new ArrayList<>();
        for (Account account : accountList) {
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

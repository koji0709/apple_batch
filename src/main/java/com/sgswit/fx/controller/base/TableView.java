package com.sgswit.fx.controller.base;

import cn.hutool.core.util.StrUtil;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AccountImportUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * account表格视图
 */
public class TableView extends CommonView {

    @FXML
    public javafx.scene.control.TableView<Account> accountTableView;

    /**
     * 总账号数量
     */
    @FXML
    protected Label accountNumLable;

    protected ObservableList<Account> accountList = FXCollections.observableArrayList();

    @FXML
    private TableColumn seq;

    @FXML
    private TableColumn account;

    @FXML
    private TableColumn pwd;

    @FXML
    private TableColumn name;

    @FXML
    private TableColumn state;

    @FXML
    private TableColumn area;

    @FXML
    private TableColumn areaCode;

    @FXML
    private TableColumn status;

    @FXML
    private TableColumn note;

    @FXML
    private TableColumn answer1;

    @FXML
    private TableColumn answer2;

    @FXML
    private TableColumn answer3;

    @FXML
    private TableColumn birthday;

    @FXML
    private TableColumn phone;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initAccountTableView();
    }

    /**
     * 初始化字段绑定
     */
    public void initAccountTableView(){
        if (seq != null){
            seq.setCellValueFactory(new PropertyValueFactory<Account,Integer>("seq"));
        }
        if (account != null){
            account.setCellValueFactory(new PropertyValueFactory<Account,String>("account"));
        }
        if (pwd != null){
            pwd.setCellValueFactory(new PropertyValueFactory<Account,String>("pwd"));
        }
        if (state != null){
            state.setCellValueFactory(new PropertyValueFactory<Account,String>("state"));
        }
        if (area != null){
            area.setCellValueFactory(new PropertyValueFactory<Account,String>("area"));
        }
        if (areaCode != null){
            areaCode.setCellValueFactory(new PropertyValueFactory<Account,String>("areaCode"));
        }
        if (name != null){
            name.setCellValueFactory(new PropertyValueFactory<Account,String>("name"));
        }
        if (status != null){
            status.setCellValueFactory(new PropertyValueFactory<Account,String>("status"));
        }
        if (answer1 != null){
            answer1.setCellValueFactory(new PropertyValueFactory<Account,String>("answer1"));
        }
        if (answer2 != null){
            answer2.setCellValueFactory(new PropertyValueFactory<Account,String>("answer2"));
        }
        if (answer3 != null){
            answer3.setCellValueFactory(new PropertyValueFactory<Account,String>("answer3"));
        }
        if (note != null){
            note.setCellValueFactory(new PropertyValueFactory<Account,String>("note"));
        }
        if (birthday != null){
            birthday.setCellValueFactory(new PropertyValueFactory<Account,String>("birthday"));
        }
        if (phone != null){
            phone.setCellValueFactory(new PropertyValueFactory<Account,String>("phone"));
        }
    }

    /**
     * 导入账号
     */
    public void importAccountButtonAction(){
        // 给一种默认导入格式
        importAccountButtonAction("account----pwd-answer1-answer2-answer3");
    }

    /**
     * 导入账号
     */
    public void importAccountButtonAction(String... formats){

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
     * 账号是否被处理过
     * @param account
     * @return
     */
    public boolean isProcessed(Account account){
        return !StrUtil.isEmpty(account.getNote());
    }

    /**
     * 刷新列表数据
     */
    public void refreshTableView(){
        accountTableView.refresh();
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
        alert("本地记录按钮点击");
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
}

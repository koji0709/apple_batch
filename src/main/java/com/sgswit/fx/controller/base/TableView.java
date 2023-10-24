package com.sgswit.fx.controller.base;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.AccountImportUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * account表格视图
 */
public class TableView implements Initializable {

    public static final String ACTION_COLUMN_NAME = "操作";

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
    private TableColumn aera;

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
        if (aera != null){
            aera.setCellValueFactory(new PropertyValueFactory<Account,String>("aera"));
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
        importAccountButtonAction("account-pwd-answer1-answer2-answer3");
    }

    /**
     * 导入账号
     */
    public void importAccountButtonAction(String format){
        Stage stage = new Stage();
        Label label1 = new Label("说明：");
        Insets padding = new Insets(0, 0, 0, 20);
        Label label2 = new Label("1.导入格式为: " + AccountImportUtil.buildNote(format) +"; 如果数据中有“-”符号,则使用{-}替换。");
        label2.setPadding(padding);
        Label label3 = new Label("2.一次可以输入多条账户信息，每条账户单独一行");
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
            List<Account> accountList1 = AccountImportUtil.parseAccount(format, area.getText());
            accountList.addAll(accountList1);
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
        stage.showAndWait();
    }

    /**
     * 绑定按钮
     * @apiNote ⚠️如果使用这个方法必须重写父类的buildTableCell方法！
     */
    public void bindActions(){
        Set<String> columnSet = accountTableView.getColumns().stream()
                .map(TableColumn::getText)
                .collect(Collectors.toSet());
        if (!columnSet.contains(ACTION_COLUMN_NAME)){
            TableColumn<Account, Void> actionsColumn = new TableColumn(ACTION_COLUMN_NAME);
            accountTableView.getColumns().add(actionsColumn);
            Callback<TableColumn<Account, Void>, TableCell<Account, Void>> cellFactory = params -> buildTableCell();
            actionsColumn.setCellFactory(cellFactory);
        }
    }

    /**
     * 结合bindActions()使用
     */
    public TableCell<Account, Void> buildTableCell(){
//        TableCell<Account,Void> cell = new TableCell<>() {
//            private final Button btn1 = new Button("按钮1");
//            private final VBox vBox = new VBox(btn1);
//            {
//                btn1.setOnAction((ActionEvent event) -> {
//                    // 按钮所在行账号
//                    // Account account = getTableView().getItems().get(getIndex());
//
//                    // todo
//                });
//            }
//            @Override
//            public void updateItem(Void item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty) {
//                    setGraphic(null);
//                } else {
//                    setGraphic(vBox);
//                }
//            }
//        };
//        return cell;
        return new TableCell<>();
    }


    /**
     * 清空列表按钮点击
     */
    public void clearAccountListButtonAction(){
        accountList.clear();
    }

    /**
     * 消息框
     */
    public void alert(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示信息");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 弹出验证码框
     */
    public String captchaDialog(String base64){
        byte[] decode = Base64.getDecoder().decode(base64);
        BorderPane root = new BorderPane();
        ImageView imageView = new ImageView();
        imageView.setImage(new Image(new ByteArrayInputStream(decode)));
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setHeaderText("验证码:");
        root.setCenter(imageView);
        dialog.setContentText("请输入验证码:");
        dialog.setGraphic(root);
        Optional<String> result = dialog.showAndWait();
        return result.isPresent() ? result.get() : "";
    }

}

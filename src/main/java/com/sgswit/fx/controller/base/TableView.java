package com.sgswit.fx.controller.base;

import com.sgswit.fx.model.Account;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * account表格视图
 */
public class TableView implements Initializable {

    @FXML
    public javafx.scene.control.TableView<Account> tableViewDataList;

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

    public void initAccountTableView(){
        seq.setCellValueFactory(new PropertyValueFactory<Account,Integer>("seq"));
        account.setCellValueFactory(new PropertyValueFactory<Account,String>("account"));
        pwd.setCellValueFactory(new PropertyValueFactory<Account,String>("pwd"));
        state.setCellValueFactory(new PropertyValueFactory<Account,String>("state"));
        aera.setCellValueFactory(new PropertyValueFactory<Account,String>("aera"));
        name.setCellValueFactory(new PropertyValueFactory<Account,String>("name"));
        status.setCellValueFactory(new PropertyValueFactory<Account,String>("status"));
        note.setCellValueFactory(new PropertyValueFactory<Account,String>("note"));
        answer1.setCellValueFactory(new PropertyValueFactory<Account,String>("answer1"));
        answer2.setCellValueFactory(new PropertyValueFactory<Account,String>("answer2"));
        answer3.setCellValueFactory(new PropertyValueFactory<Account,String>("answer3"));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initAccountTableView();
    }

    public void alert(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示信息");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

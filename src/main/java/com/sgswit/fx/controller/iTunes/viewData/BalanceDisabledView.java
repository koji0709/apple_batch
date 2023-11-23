package com.sgswit.fx.controller.iTunes.viewData;

import com.sgswit.fx.controller.base.TableView;
import com.sgswit.fx.model.Account;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class BalanceDisabledView extends TableView {

    /**
     * 新邮箱(账号)或救援邮箱
     */
    @FXML
    private TableColumn balance;

    @FXML
    private TableColumn disableStatus;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        if (balance != null){
            balance.setCellValueFactory(new PropertyValueFactory<Account,String>("balance"));
        }
        if (disableStatus != null){
            disableStatus.setCellValueFactory(new PropertyValueFactory<Account,String>("disableStatus"));
        }
    }

}

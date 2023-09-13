package com.sgswit.fx.controller.base;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.SecuritycodePopupController;
import com.sgswit.fx.controller.AccountInputPopupController;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.KeyValuePair;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;


/**
 * @author DELL
 */
public class SelfServiceChargeController implements Initializable {

    @FXML
    public Button cancelBtn;
    @FXML
    public Button confirmBtn;
    @FXML
    public TextField cardNoField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void onCancelBtnClick(ActionEvent actionEvent) {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    public void onConfirmBtnClick(ActionEvent actionEvent) {
        String cardNo="";
        if(null != cardNoField.getText().trim() && !"".equals(cardNoField.getText().trim())){
            cardNo=cardNoField.getText().trim();
        }

        if("".equals(cardNo)){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("充值提示");
            alert.setHeaderText("充值卡号不能为空！");
            alert.show();
        }else{
            if(cardNo.length()<10){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("充值提示");
                alert.setHeaderText("充值卡号不正确！");
                alert.show();
            }else{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("充值提示");
                alert.setHeaderText("充值成功！");
                alert.show();
            }
            Stage stage = (Stage) confirmBtn.getScene().getWindow();
            stage.close();
        }
    }
}

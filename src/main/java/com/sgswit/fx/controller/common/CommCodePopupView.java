package com.sgswit.fx.controller.common;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

/**
 * @author DELL
 */
public class CommCodePopupView {
    @FXML
    private TextField smsTextField;
    @FXML
    private Label sCodeLabel;

    private String securityCode = "";
    private String account      = "";

    public CommCodePopupView(){

    }


    @FXML
    private void onConfirmBtnClick(ActionEvent actionEvent) throws Exception{
        securityCode=smsTextField.getText();
        if(StringUtils.isEmpty(securityCode)){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("验证码不能为空");
            alert.show();
            return;
        }
        Button button= (Button) actionEvent.getSource();
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }

    public String getSecurityCode(){
        return this.securityCode;
    }

    public void setAccount(String account){
        this.account = account;
        this.sCodeLabel.setText("请输入账户[ "+ account +" ]的验证码");
    }

    public void onCancelBtnClick(ActionEvent actionEvent) {
        Button cancelBtn= (Button) actionEvent.getSource();
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}

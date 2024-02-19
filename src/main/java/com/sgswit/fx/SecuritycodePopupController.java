package com.sgswit.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SecuritycodePopupController {

    @FXML
    private TextField deviceTextField;

    @FXML
    private TextField smsTextField;

    @FXML
    private Button securityBtn;

    @FXML
    private Label sCodeLabel;

    private String securityCode = "";
    private String securityType = "";
    private String account      = "";

    public SecuritycodePopupController(){

    }


    @FXML
    private void onSecurityBtnClick() throws Exception{

        if(null != deviceTextField.getText() && !"".equals(deviceTextField.getText())){
            securityCode=deviceTextField.getText();
            securityType="device";
        }

        if(null != smsTextField.getText() && !"".equals(smsTextField.getText())){
            securityCode=smsTextField.getText();
            securityType="sms";
        }

        if("".equals(securityCode)){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("验证码不能为空");
            alert.show();
        }else{
            // get a handle to the stage
            Stage stage = (Stage) securityBtn.getScene().getWindow();
            // do what you have to do
            stage.close();
        }

    }

    public String getSecurityCode(){
        return this.securityCode;
    }
    public String getSecurityType(){return  this.securityType;}

    public void setAccount(String account){
        this.account = account;
        this.sCodeLabel.setText("请输入账户[ "+ account +" ]双重验证码");
    }
}

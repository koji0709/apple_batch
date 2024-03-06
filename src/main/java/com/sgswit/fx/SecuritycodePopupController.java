package com.sgswit.fx;

import com.sgswit.fx.controller.common.CommonView;
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
            CommonView.alert("验证码不能为空",Alert.AlertType.WARNING);
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

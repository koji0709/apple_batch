package com.sgswit.fx.controller.iTunes;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class AccountInputPopupController {

    @FXML
    private TextArea accountTextArea;

    @FXML
    private Button accountImportBtn;


    @FXML
    protected void onAccountImportBtnClick() throws Exception{
        // get a handle to the stage
        Stage stage = (Stage) accountImportBtn.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    public String getAccounts(){
        return this.accountTextArea.getText();
    }
}

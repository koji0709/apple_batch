package com.sgswit.fx.controller.iTunes;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * @author DELL
 */
public class VirtualCardInputPopupController {

    @FXML
    private TextArea dataTextArea;

    @FXML
    private Button dataImportBtn;


    @FXML
    protected void onDataImportBtnClick() throws Exception{
        // get a handle to the stage
        Stage stage = (Stage) dataImportBtn.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    public String getData(){
        return this.dataTextArea.getText();
    }
}

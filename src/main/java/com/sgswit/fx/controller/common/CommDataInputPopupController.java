package com.sgswit.fx.controller.common;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.controller.iTunes.AccountInputPopupController;
import com.sgswit.fx.enums.DataImportEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AccountImportUtil;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author DELL
 */
public class CommDataInputPopupController<T> implements Initializable {
    @FXML
    public VBox notes;
    @FXML
    private TextArea accountTextArea;
    private static String kk=null;
    private static DataImportEnum dataImportEnum=null;
    @FXML
    private Button accountImportBtn;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Insets padding = new Insets(0, 0, 0, 20);
        int index=1;
        for (String s:dataImportEnum.getDescription()){
            Label label = new Label(index+"."+s);
            label.setPadding(padding);
            notes.setSpacing(5);
            notes.setPadding(new Insets(5, 5, 5, 5));
            notes.getChildren().add(label);
            index++;
        }
    }

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
    public void importData(ObservableList<T> list,DataImportEnum importEnum) throws IOException {
        dataImportEnum=importEnum;
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/base/comm-data-input-popup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 450);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        Stage popupStage = new Stage();
        popupStage.setScene(scene);



        popupStage.setTitle("账户导入");

        popupStage.initModality(Modality.APPLICATION_MODAL);

        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.showAndWait();


    }
}

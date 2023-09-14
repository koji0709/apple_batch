package com.sgswit.fx.controller.iTunes;

import com.sgswit.fx.MainApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author DeZh
 * @title: CustomCountryDetPopupController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/1320:45
 */
public class CustomCountryPopupController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void onAddCustomCountryDet(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/iTunes/custom-country-det-popup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 490, 600);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("新增国家");
        //模块化，对应用里的所有窗口起作用
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.show();
    }
}

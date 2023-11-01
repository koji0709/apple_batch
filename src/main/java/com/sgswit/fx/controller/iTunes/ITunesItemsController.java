package com.sgswit.fx.controller.iTunes;

import com.sgswit.fx.MainApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * @author DeZh
 * @title: ITunesItemsController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/1321:39
 */
public class ITunesItemsController {

    public void onCountryModifyBtnClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/iTunes/country-modify.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 650);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("账号国家修改");
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.show();
    }
    public void onDeletePaymentBtnClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/iTunes/payment-method.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 650);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("删除付款方式");
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.show();
    }

    public void onGiftCardBlanceCheck(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/iTunes/giftCard-balance-check.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 650);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("礼品卡查余额");
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.show();
    }
}

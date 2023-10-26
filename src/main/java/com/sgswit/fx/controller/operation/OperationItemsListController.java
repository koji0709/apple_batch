package com.sgswit.fx.controller.operation;

import com.sgswit.fx.MainApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * 修改操作专区list controller
 */
public class OperationItemsListController {

    public void onAccountInfoModifyBtnClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/operation/account-info-modify.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1530, 760);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("官方修改资料");
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(true);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.show();
    }

    public void onUnlockChangePasswordBtnClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/operation/unlock-change-password.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1260, 760);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("账号解锁改密");
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.show();
    }

    public void onUpdateAppleIdBtnClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/operation/update-appleid.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1530, 760);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("苹果更改账号");
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(true);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.show();
    }

    public void onSecurityUpgradeBtnClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/operation/security-upgrade.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1260, 760);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("开通双重认证");
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.show();
    }

    public void onSecurityDowngradeBtnClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/operation/security-downgrade.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1260, 760);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("关闭双重认证");
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.show();
    }

    public void onSupportPinBtnClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/operation/support-pin.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1260, 760);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("生成支持PIN");
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.show();
    }

}

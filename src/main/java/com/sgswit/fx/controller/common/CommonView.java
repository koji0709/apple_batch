package com.sgswit.fx.controller.common;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.utils.PropertiesUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 *
 * @author DELL
 */
public class CommonView implements Initializable {
    /**
     * 消息框
     */
    public static void alert(String message) {
        alert(message, Alert.AlertType.INFORMATION,false);
    }
    public static void alert(String message, Alert.AlertType alertType) {
        alert(message, alertType,false);
    }
    public static void alert(String message, Alert.AlertType alertType,boolean alwaysOnTop) {
        Alert alert = new Alert(alertType);
        alert.setTitle("提示信息");
        alert.setHeaderText(null);
        alert.setContentText(message);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        String logImg= PropertiesUtil.getConfig("softwareInfo.log.path");
        stage.getIcons().add(new Image(CommonView.class.getResource(logImg).toString()));
        if(alwaysOnTop){
            stage.setAlwaysOnTop(true);
        }
        alert.show();
    }
    public static void alertUI(String message, Alert.AlertType alertType) {
        Platform.runLater(new Task<Integer>() {
            @Override
            protected Integer call() {
                alert(message, alertType);
                return 1;
            }
        });
    }

    /**
     * 输入框
     */
    public String dialog(String title, String contentText) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("");
        dialog.setTitle(title);
        dialog.setContentText(contentText);
        Optional<String> result = dialog.showAndWait();
        return result.isPresent() ? result.get() : "";
    }

    /**
     * 弹出验证码框
     */
    public String captchaDialog(String base64) {
        byte[] decode = Base64.getDecoder().decode(base64);
        BorderPane root = new BorderPane();
        ImageView imageView = new ImageView();
        imageView.setImage(new Image(new ByteArrayInputStream(decode)));
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("验证码:");
        root.setCenter(imageView);
        dialog.setContentText("请输入验证码:");
        dialog.setGraphic(root);
        Optional<String> result = dialog.showAndWait();
        return result.isPresent() ? result.get() : "";
    }


    public static boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        // 自定义按钮文本（可选）
        ButtonType yesButton = new ButtonType("确定");
        ButtonType noButton = new ButtonType("取消");
        alert.getButtonTypes().setAll(yesButton, noButton);
        // 显示对话框并等待用户响应
        return alert.showAndWait().filter(response -> response == yesButton).isPresent();
    }


    public Boolean hasFailMessage(HttpResponse rsp) {
        String body = rsp.body();
        if (StrUtil.isEmpty(body) || !JSONUtil.isTypeJSON(body)){
            return false;
        }
        Object hasError = JSONUtil.parseObj(body).getByPath("hasError");
        return null != hasError && (Boolean) hasError;
    }

    public String failMessage(HttpResponse rsp) {
        StringBuffer stringBuffer=new StringBuffer();
        Object service_errors = JSONUtil.parseObj(rsp.body()).getByPath("service_errors");
        for (Object o : JSONUtil.parseArray(service_errors)) {
            JSONObject jsonObject = (JSONObject) o;
            stringBuffer.append(jsonObject.getByPath("message") + ";");
        }
        return stringBuffer.toString();
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}

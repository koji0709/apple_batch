package com.sgswit.fx.controller.common;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 *
 */
public class CommonView implements Initializable {

    /**
     * 消息框
     */
    public void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示信息");
        alert.setHeaderText(message);
        alert.show();
    }

    public void alert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("提示信息");
        alert.setHeaderText(message);
        alert.show();
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

    public Boolean hasFailMessage(HttpResponse rsp) {
        String body = rsp.body();
        if (StrUtil.isEmpty(body) || !JSONUtil.isTypeJSON(body)){
            return false;
        }
        Object hasError = JSONUtil.parseObj(body).getByPath("hasError");
        return null != hasError && (Boolean) hasError;
    }

    public String failMessage(HttpResponse rsp) {
        String message = "";
        Object service_errors = JSONUtil.parseObj(rsp.body()).getByPath("service_errors");
        for (Object o : JSONUtil.parseArray(service_errors)) {
            JSONObject jsonObject = (JSONObject) o;
            message += jsonObject.getByPath("message") + ";";
        }
        return message;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}

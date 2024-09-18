package com.sgswit.fx.utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.application.HostServices;
import javafx.util.Duration;

/**
 * 桌面消息提示框工具，用于显示新版本检测和下载链接
 */
public class ToastUtil {

    private static HostServices hostServices;
    private static PauseTransition autoCloseTransition;
    private static Stage existingToastStage; // 记录现有的提示框

    // 初始化 HostServices，主程序调用时传入
    public static void init(HostServices hostServices) {
        ToastUtil.hostServices = hostServices;
    }

    public static void show(String message, String downloadUrl) {
        // 如果已有提示框显示，则返回
        if (existingToastStage != null && existingToastStage.isShowing()) {
            return;
        }

        // 创建新的 Stage 来显示消息提示框
        Stage toastStage = new Stage();
        existingToastStage = toastStage; // 设置现有提示框为当前提示框
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        // 创建消息的内容
        Label toastLabel = new Label(message);
        toastLabel.setTextFill(Color.WHITE);
        toastLabel.setFont(new Font("Arial", 12)); // 缩小字体大小
        toastLabel.setWrapText(true); // 允许换行

        // 创建超链接
        Hyperlink downloadLink = new Hyperlink("下载新版本");
        downloadLink.setTextFill(Color.WHITE);
        downloadLink.setFont(new Font("Arial", 12)); // 与消息相同的字体大小
        downloadLink.setOnAction(e -> {
            if (hostServices != null && downloadUrl != null) {
                hostServices.showDocument(downloadUrl); // 打开默认浏览器下载新版本
            }
        });
        downloadLink.setStyle("-fx-text-fill: white; -fx-underline: true; -fx-border-color: transparent; -fx-border-width: 0;");

        // 使用 VBox 布局来放置消息文本和超链接
        VBox contentBox = new VBox(5, toastLabel, downloadLink);
        contentBox.setAlignment(Pos.CENTER_LEFT);
        contentBox.setStyle("-fx-padding: 10px; -fx-background-color: rgba(0, 0, 0, 0.8);"); // 设置深色背景色，并去掉圆角

        // 创建关闭按钮
        Button closeButton = new Button("X");
        styleCloseButton(closeButton);
        closeButton.setOnAction(e -> {
            // 创建淡出效果
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), toastStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                toastStage.close();
                existingToastStage = null; // 关闭后，重置现有提示框
                if (autoCloseTransition != null) {
                    autoCloseTransition.stop(); // 停止自动关闭计时器
                }
            });
            fadeOut.play();
        });

        // 增加移入效果
        closeButton.setOnMouseEntered(e -> {
            closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff0000; -fx-font-size: 14px; -fx-cursor: hand;");
        });
        closeButton.setOnMouseExited(e -> {
            closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        });

        // 使用 StackPane 将内容和关闭按钮放置在一起
        StackPane root = new StackPane(contentBox, closeButton);
        root.setAlignment(Pos.BOTTOM_LEFT);
        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);

        // 创建 Scene
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT); // 确保场景背景透明
        toastStage.setScene(scene);

        // 计算提示框的宽度和高度
        double toastWidth = 230;
        double toastHeight = 90;

        // 获取屏幕的大小
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        // 计算弹出框的位置，确保完全显示在屏幕内
        double xPosition = Math.max(screenBounds.getMaxX() - toastWidth - 20, 0);
        double yPosition = Math.max(screenBounds.getMaxY() - toastHeight - 20, 0);

        // 设置弹出框位置
        toastStage.setX(xPosition);
        toastStage.setY(yPosition);

        // 设置提示框的宽度和高度
        toastStage.setWidth(toastWidth);
        toastStage.setHeight(toastHeight);

        // 添加拖动功能
        addDragSupport(toastStage, root);

        // 显示弹出框
        toastStage.show();

        // 创建淡入效果
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), toastStage.getScene().getRoot());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        // 创建一个 PauseTransition 以在 1 分钟后关闭弹出框
        autoCloseTransition = new PauseTransition(Duration.minutes(1));
        autoCloseTransition.setOnFinished(e -> {
            // 创建淡出效果
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), toastStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                toastStage.close();
                existingToastStage = null; // 关闭后，重置现有提示框
            });
            fadeOut.play();
        });
        autoCloseTransition.play();

        // 停止自动关闭计时器当点击时
        root.setOnMouseClicked(e -> {
            if (autoCloseTransition != null) {
                autoCloseTransition.stop(); // 停止计时器
            }
        });
    }

    // 样式化关闭按钮方法
    private static void styleCloseButton(Button button) {
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-border-color: transparent;"); // 去掉边框
        button.setPrefSize(30, 30); // 设置按钮大小
        button.setPadding(new javafx.geometry.Insets(0)); // 设置按钮内部的间距
    }

    // 添加拖动支持
    private static void addDragSupport(Stage stage, StackPane root) {
        final double[] dragStartX = {0};
        final double[] dragStartY = {0};

        root.setOnMousePressed((MouseEvent event) -> {
            dragStartX[0] = event.getScreenX() - stage.getX();
            dragStartY[0] = event.getScreenY() - stage.getY();
        });

        root.setOnMouseDragged((MouseEvent event) -> {
            stage.setX(event.getScreenX() - dragStartX[0]);
            stage.setY(event.getScreenY() - dragStartY[0]);
        });
    }
}

package com.sgswit.fx.utils;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author DeZh
 * @title: StageToSystemTrayCopy
 * @projectName appleBatch
 * @description: TODO
 * @date 2024/1/2414:33
 */
public class StageToSystemTrayUtil {
    private static TrayIcon trayIcon;
    //创建图标
    protected static void createTrayIcon(Stage primaryStage) {
        if (SystemTray.isSupported()) {
            // 获取系统托盘
            SystemTray tray = SystemTray.getSystemTray();
            // 创建弹出菜单
            PopupMenu popupMenu = new PopupMenu();
            // 创建系统托盘图标
            String logImg=PropertiesUtil.getConfig("softwareInfo.log.path");
            Image image = new Image(logImg);
            String toolTip=PropertiesUtil.getConfig("softwareInfo.name")+" - Apple批处理";

            trayIcon = new TrayIcon(SwingFXUtils.fromFXImage(image, null), toolTip, popupMenu);
            trayIcon.setImageAutoSize(true);
            // 创建打开菜单项

            MenuItem openMenuItem = new MenuItem("显示主窗口");
            openMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Platform.runLater(() -> {
                        showWindow(primaryStage);
                    });
                }
            });
            // 创建退出菜单项
            MenuItem exitMenuItem = new MenuItem("退出");
            exitMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tray.remove(trayIcon);
                    System.exit(0);
                }
            });
            // 将菜单项添加到弹出菜单
            popupMenu.add(openMenuItem);
            popupMenu.add(exitMenuItem);
            // 将弹出菜单设置到系统托盘图标
            trayIcon.setPopupMenu(popupMenu);
            // 将系统托盘图标添加到系统托盘
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
            }
            // 设置托盘图标鼠标左键事件
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    //鼠标左键
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        showWindow(primaryStage);
                    }
                }
            });
        } else {
            System.out.println("System tray is not supported.");
        }
    }
    //打开窗口
    public static void showWindow(Stage primaryStage) {
        if (primaryStage != null) {
            Platform.runLater(() -> {
                if (primaryStage.isIconified()) {
                    primaryStage.setIconified(false);
                }
                if (!primaryStage.isShowing()) {
                    primaryStage.show();
                }
                primaryStage.toFront();
            });
        }
    }
    //关闭窗口
    protected static void hideWindow(Stage primaryStage) {
        if (primaryStage != null) {
            Platform.runLater(() -> {
                primaryStage.hide();
            });
        }
    }
}

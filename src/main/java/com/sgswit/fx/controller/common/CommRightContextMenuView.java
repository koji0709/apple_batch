package com.sgswit.fx.controller.common;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.SecuritycodePopupController;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.model.ConsumptionBill;
import com.sgswit.fx.model.KeyValuePair;
import com.sgswit.fx.model.LoginInfo;
import com.sgswit.fx.utils.ClipboardManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author DELL
 */
public class CommRightContextMenuView<T> extends CommonView {
    private Integer currentMenuIndex;
    private static double leftMenuWidth = 120;
    private static double buttonHeight = 20;
    /**
     * 主题色
     **/
    private static String COLOR_PRIMARY = "#e0e0e0";
    /**
     * 被选中的字体颜色
     **/
    private static String COLOR_SELECTED = "black";
    /**
     * 被选中的按钮背景颜色
     **/
    private static String COLOR_HOVER = "#169bd5";
    private Stage stage = null;
    private static TableView accountTableView;

    private static List<KeyValuePair> allList = getAllList();

    private static List<KeyValuePair> getAllList() {
        List<KeyValuePair> allList = new ArrayList<>();
        Class<Constant.RightContextMenu> emClass = Constant.RightContextMenu.class;
        Constant.RightContextMenu[] arr = emClass.getEnumConstants();
        for (int i = 0; i < arr.length; i++) {
            KeyValuePair keyValuePair = new KeyValuePair(arr[i].getCode(), arr[i].getTitle(), arr[i].getPath());
            allList.add(keyValuePair);
        }
        return allList;
    }

    /**
     * 　* 右键点击事件
     *
     * @param
     * @param contextMenuEvent
     * @param tableView
     * @param items            菜单
     * @param fields           需要复制的字段
   　* @return void
     * @throws
   　* @author DeZh
  　 * @date 2023/12/22 13:52
     */
    protected void onContentMenuClick(ContextMenuEvent contextMenuEvent, TableView tableView, List<String> items, List<String> fields) {
        accountTableView = tableView;
        try {
            if (items.size() == 0) {
                return;
            }

            List<KeyValuePair> list = new ArrayList<>();
            for (String k : items) {
                Optional<KeyValuePair> cartOptional = allList.stream().filter(item -> item.getKey().equals(k)).findFirst();
                if (cartOptional.isPresent()) {
                    // 存在
                    list.add(cartOptional.get());
                }
            }
            if (list.size() == 0) {
                return;
            }
            ObservableList<T> selectedRows = accountTableView.getSelectionModel().getSelectedItems();
            if (selectedRows.size() == 0) {
                return;
            }
            openMenu(contextMenuEvent, list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected void onContentMenuClick(ContextMenuEvent contextMenuEvent, TableView tableView, List<String> items) {
        onContentMenuClick(contextMenuEvent, tableView, items, new ArrayList<>());
    }

    /**
     * 　* 打开右键菜单
     *
     * @param
     * @param contextMenuEvent
     * @param items            　* @return void
     *                         　* @throws
     *                         　* @author DeZh
     *                         　* @date 2023/12/22 20:52
     */
    private void openMenu(ContextMenuEvent contextMenuEvent, List<KeyValuePair> items) {
        double x = contextMenuEvent.getScreenX();
        double y = contextMenuEvent.getScreenY();
        if (stage != null) {
            stage.close();
        }
        stage = new Stage();
        stage.setX(x + 1);
        stage.setY(y);
        Group root = new Group(getMenu(items));
        stage.setScene(new Scene(root, leftMenuWidth, Region.USE_PREF_SIZE));
        stage.initModality(Modality.NONE);
        stage.setResizable(false);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
        //焦点失去事件，关闭窗口
        stage.focusedProperty().addListener((ov, onHidden, onShown) -> stage.close());
    }


    private Node getMenu(List<KeyValuePair> items) {
        VBox vbox = new VBox();
        vbox.setMinHeight(buttonHeight);
        vbox.setMinWidth(leftMenuWidth);
        setPaneBackground(vbox, Color.web(COLOR_PRIMARY));
        // 增加菜单中的项目
        vbox.getChildren().addAll(getMenuItemList(leftMenuWidth, items));
        return vbox;
    }

    /**
     * 生成左侧菜单按钮
     */
    private List<Button> getMenuItemList(double width, List<KeyValuePair> itemNameList) {
        List<Button> buttonList = new ArrayList<>(itemNameList.size());
        for (KeyValuePair keyValuePair : itemNameList) {
            String title = keyValuePair.getValue();
            Map<String, Object> userData = new HashMap<>();
            userData.put("title", keyValuePair.getValue());
            Button button = new Button(keyValuePair.getValue());
            button.setMinWidth(width);
            button.setPrefHeight(buttonHeight);
            button.setId(keyValuePair.getKey());
            setButtonBackground(button, Color.web(COLOR_PRIMARY), Color.BLACK);
            //增加鼠标移动到菜单上到hover效果
            button.setOnMouseMoved(event -> {
                if (currentMenuIndex == null || !button.getId().equals(itemNameList.get(currentMenuIndex).getKey())) {
                    setButtonBackground(button, Color.web(COLOR_HOVER), Color.BLACK);
                    setFont(button, Color.BLACK, -1);
                } else {
                    setButtonBackground(button, Color.web(COLOR_HOVER), Color.web(COLOR_SELECTED));
                }
            });
            button.setOnMouseExited(event -> {
                if (currentMenuIndex == null || !button.getId().equals(itemNameList.get(currentMenuIndex).getKey())) {
                    setButtonBackground(button, Color.web(COLOR_PRIMARY), Color.BLACK);
                } else {
                    setButtonBackground(button, Color.web(COLOR_PRIMARY), Color.web(COLOR_SELECTED));
                }

            });
            button.setOnMouseClicked(event -> {
                String buttonId = button.getId();
                ObservableList<T> selectedRows = accountTableView.getSelectionModel().getSelectedItems();
                if (selectedRows.size() == 0) {
                    return;
                }
                T account = selectedRows.get(0);
                if (buttonId.equalsIgnoreCase(Constant.RightContextMenu.COPY.getCode())) {
                    copyInfo(account);
                }else if (buttonId.equalsIgnoreCase(Constant.RightContextMenu.COPY_ALL.getCode())) {
                    copyAllInfo(accountTableView);
                } else if (buttonId.equalsIgnoreCase(Constant.RightContextMenu.DELETE.getCode())) {
                    Boolean hasFinished= (Boolean) ReflectUtil.getFieldValue(account, "hasFinished");
                    if(!hasFinished){
                        alert("有工作正在进行中，当前数据无法删除！", Alert.AlertType.ERROR);
                        return;
                    }else{
                        accountTableView.getItems().remove(account);
                        accountTableView.refresh();
                        setAccountNumLabel();
                    }
                } else if (buttonId.equalsIgnoreCase(Constant.RightContextMenu.CODE.getCode())) {
                    openCodePopup(account, title, Constant.RightContextMenu.CODE.getCode());
                } else if (buttonId.equalsIgnoreCase(Constant.RightContextMenu.REEXECUTE.getCode())) {
                    Boolean hasFinished= (Boolean) ReflectUtil.getFieldValue(account, "hasFinished");
                    String note = ReflectUtil.invoke(account, "getNote");

                    if(!hasFinished || Constant.REDEEM_WAIT1_DESC.equals(note)){
                        alert("执行中，不可重复执行。");
                        return;
                    }
                    boolean securityCode = ReflectUtil.hasField(account.getClass(), "securityCode");
                    if(securityCode){
                        ReflectUtil.invoke(account,"setSecurityCode","");
                    }
                    boolean authCode = ReflectUtil.hasField(account.getClass(), "authCode");
                    if(authCode){
                        ReflectUtil.invoke(account,"setAuthCode","");
                    }
                    boolean step = ReflectUtil.hasField(account.getClass(), "step");
                    if(step){
                        ReflectUtil.invoke(account,"setStep","");
                    }
                    boolean verify = executeButtonActionBefore();
                    if (!verify) {
                        return;
                    }
                    if ("GiftCardBatchRedeemController".equals(this.getClass().getSimpleName())){
                        ThreadUtil.execute(()-> {
                            Boolean redeemCheck = ReflectUtil.invoke(this, "redeemCheck", account);
                            if (redeemCheck){
                                accountHandlerExpand(account);
                            }
                        });
                    }else{
                        accountHandlerExpand(account);
                    }
                } else if (buttonId.equalsIgnoreCase(Constant.RightContextMenu.TWO_FACTOR_CODE.getCode())) {
                    openCodePopup(account, title, Constant.RightContextMenu.TWO_FACTOR_CODE.getCode());
                } else if (buttonId.equalsIgnoreCase(Constant.RightContextMenu.WEB_TWO_FACTOR_CODE.getCode())) {
                    if (account instanceof LoginInfo) {
                        LoginInfo account1 = (LoginInfo) account;
                        String accountNo = ((SimpleStringProperty) ReflectUtil.getFieldValue(account, "account")).getValue();
                        String securityCode = openSecurityCodePopupView(accountNo);
                        if (!StrUtil.isEmpty(securityCode)) {
                            account1.setSecurityCode(securityCode);
                            accountHandlerExpand(account);
                        }
                    } else if (account instanceof ConsumptionBill) {
                        String accountNo = ((SimpleStringProperty) ReflectUtil.getFieldValue(account, "account")).getValue();
                        String securityCode = openSecurityCodePopupView(accountNo);
                        if (!StrUtil.isEmpty(securityCode)) {
                            twoFactorCodeExecute(account, securityCode);
                        }
                    }
                }
                stage.close();
            });
            buttonList.add(button);
        }
        return buttonList;
    }

    /**
     * 　* 复制当前行信息
     *
     * @param
     * @param selectModel 　* @return void
     *                    　* @throws
     *                    　* @author DeZh
     *                    　* @date 2023/12/22 17:55
     */
    private void copyInfo(T selectModel) {
        List<String> resultList = new ArrayList<>();
        for (Object column : accountTableView.getColumns()) {
            TableColumn tableColumn = (TableColumn) column;
            String id = tableColumn.getId();
            String text = tableColumn.getText();
            if (!"seq".equals(id)) {
                if (ReflectUtil.hasField(selectModel.getClass(), id)) {
                    Object value = ReflectUtil.invoke(
                            selectModel
                            , "get" + id.substring(0, 1).toUpperCase() + id.substring(1));
                    if (value != null && StrUtil.isNotEmpty(value.toString())) {
                        text = value.toString();
                    }
                }
                resultList.add(text);
            }
        }
        String str = resultList.stream().collect(Collectors.joining("----"));
        try {
            ClipboardManager.setClipboard(str);
        } catch (Exception e) {
            alert("复制失败！");
        }
        alert("复制成功！");
    }
    /**
    　* 复制全部信息
      * @param
     * @param tableView
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2024/6/20 18:19
    */
    private void copyAllInfo(TableView tableView) {
        try {
            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            StringBuilder clipboardString = new StringBuilder();
            // 输出表头标题
            for (Object column : tableView.getColumns()) {
                TableColumn tableColumn = (TableColumn) column;
                String text = tableColumn.getText();
                clipboardString.append(text);
                clipboardString.append('\t');
            }
            clipboardString.append('\n');

            ObservableList<T> rowList = (ObservableList) tableView.getItems();
            int index=1;
            for (T selectModel:rowList){
                for (Object column : tableView.getColumns()) {
                    TableColumn tableColumn = (TableColumn) column;
                    String id = tableColumn.getId();
                    String text = "";
                    if (!"seq".equals(id)) {
                        if (ReflectUtil.hasField(selectModel.getClass(), id)) {
                            Object value = ReflectUtil.invoke(
                                    selectModel
                                    , "get" + id.substring(0, 1).toUpperCase() + id.substring(1));
                            if (value != null && StrUtil.isNotEmpty(value.toString())) {
                                text = value.toString();
                            }
                        }
                    }else{
                        text=String.valueOf(index);
                    }
                    clipboardString.append(text);
                    clipboardString.append('\t');
                }
                clipboardString.append('\n');
                index++;
            }
            final ClipboardContent content = new ClipboardContent();
            content.putString(clipboardString.toString());
            Clipboard.getSystemClipboard().setContent(content);
        } catch (Exception e) {
            alert("复制失败！");
        }
        alert("复制成功！");
    }

    /**
     * 　* 弹出验证码弹出框
     *
     * @param
     * @param o 　* @return void
     *          　* @throws
     *          　* @author DeZh
     *          　* @date 2023/12/22 17:56
     */
    private void openCodePopup(T o, String title, String type) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/comm-code-popup.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 385, 170);
            scene.getRoot().setStyle("-fx-font-family: 'serif'");
            CommCodePopupView fxmlLoaderController = fxmlLoader.getController();
            String account = ((SimpleStringProperty) ReflectUtil.getFieldValue(o, "account")).getValue();
            fxmlLoaderController.setAccount(account);
            Stage popupStage = new Stage();
            popupStage.setTitle(title);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(scene);
            popupStage.setResizable(false);
            popupStage.initStyle(StageStyle.UTILITY);
            popupStage.showAndWait();
            String code = fxmlLoaderController.getSecurityCode();
            if (StringUtils.isEmpty(code)) {
                return;
            }
            if (Constant.RightContextMenu.CODE.getCode().equals(type)) {
                secondStepHandler(o, code);
            } else if (Constant.RightContextMenu.TWO_FACTOR_CODE.getCode().equals(type)) {
                twoFactorCodeExecute(o, code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 　* 第二步操作
     *
     * @param  　* @return void
     *         　* @throws
     *         　* @author DeZh
     *         　* @date 2023/12/22 17:38
     */
    protected void secondStepHandler(T account, String code) {
    }

    /**
     * 　* 双重验证
     *
     * @param
     * @param o 　* @return void
     *          　* @throws
     *          　* @author DeZh
     *          　* @date 2023/12/22 18:04
     */
    protected void twoFactorCodeExecute(T o, String code) {
    }

    /**
     * 打开双重认证视图
     */
    public String openSecurityCodePopupView(String account) {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/securitycode-popup.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), 600, 350);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        SecuritycodePopupController s = fxmlLoader.getController();
        s.setAccount(account);

        Stage popupStage = new Stage();
        popupStage.setTitle("双重验证码输入页面");
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.showAndWait();

        String type = s.getSecurityType();
        String code = s.getSecurityCode();
        if (StrUtil.isEmpty(type) || StrUtil.isEmpty(code)) {
            return "";
        }
        return type + "-" + code;
    }

    /**
     * 每一个账号的处理器
     */
    public void accountHandlerExpand(T account) {
    }
    public void accountHandler(T account){}

    /**
     * 执行前, 一般做一些参数校验
     */
    public boolean executeButtonActionBefore() {
        return true;
    }

    public void setAccountNumLabel() {
    }

    public static void setPaneBackground(Pane pane, Color color) {
        pane.setBackground(new Background(new BackgroundFill(color, null, null)));
    }

    public static void setButtonBackground(Button button, Color bg, Color text) {
        button.setBackground(new Background(new BackgroundFill(bg, null, null)));
        button.setTextFill(text);
        button.setCursor(Cursor.HAND);
        button.setPadding(new Insets(5, 0, 5, 5));
    }

    public static void setFont(Labeled node, Color color, double fontSize) {
        node.setTextFill(color);
        node.setFont(Font.font(null, FontWeight.BOLD, fontSize));
    }
    /**
     * 窗口关闭前，做一些数据操作
     */
    public void closeStageActionBefore() {

    }
}

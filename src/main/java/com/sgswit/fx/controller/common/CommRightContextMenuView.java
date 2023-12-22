package com.sgswit.fx.controller.common;

import cn.hutool.core.swing.clipboard.ClipboardMonitor;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.model.CreditCard;
import com.sgswit.fx.model.KeyValuePair;
import com.sgswit.fx.annotation.CustomAnnotation;
import com.sgswit.fx.utils.ClipboardManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author DELL
 */
public class CommRightContextMenuView<T> extends CommonView{
    private Integer currentMenuIndex;
    private static double leftMenuWidth = 120;
    private static double buttonHeight = 20;
    /**主题色**/
    private static String COLOR_PRIMARY = "#e0e0e0";
    /**被选中的字体颜色**/
    private static String COLOR_SELECTED  = "black";
    /**被选中的按钮背景颜色**/
    private static String COLOR_HOVER = "#169bd5";
    private Stage stage = new Stage();

    private static TableView accountTableView;

    private static List<KeyValuePair> allList=new ArrayList<>(){{
        add(new KeyValuePair("delete","删除",""));
        add(new KeyValuePair("reexecute","重新执行",""));
        add(new KeyValuePair("copy","复制账号信息",""));
        add(new KeyValuePair("twoFactorCode","输入双重验证码","views/comm-securitycode-popup.fxml"));
        add(new KeyValuePair("smsCode","输入验证码","views/comm-securitycode-popup.fxml"));
    }};


    /**
     　* 右键点击事件
     * @param
     * @param contextMenuEvent
     * @param tableView
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2023/12/22 13:52
     */
    protected void onContentMenuClick(ContextMenuEvent contextMenuEvent,TableView tableView,String op) {
        accountTableView=tableView;
        try {
            if(StringUtils.isEmpty(op) || op.split("-").length==0){
                return;
            }

            List<KeyValuePair> list=new ArrayList<>();
            for(String k:op.split("-")){
                Optional<KeyValuePair> cartOptional = allList.stream().filter(item -> item.getKey().equals(k)).findFirst();
                if (cartOptional.isPresent()) {
                    // 存在
                    list.add(cartOptional.get());
                }
            }
            if(list.size()==0){
                return;
            }
            ObservableList<T> selectedRows=accountTableView.getSelectionModel().getSelectedItems();
            if(selectedRows.size()==0){
                return;
            }
            CommRightContextMenuView commRightContextMenuView=new CommRightContextMenuView();
            commRightContextMenuView.openMenu(contextMenuEvent,list);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    private void openMenu(ContextMenuEvent contextMenuEvent, List<KeyValuePair> items) {
        double x= contextMenuEvent.getScreenX();
        double y=contextMenuEvent.getScreenY();
        stage.setX(x+1);
        stage.setY(y);
        Group root = new Group(getMenu(items));
        stage.setScene(new Scene(root, leftMenuWidth,Region.USE_PREF_SIZE));
        stage.initModality(Modality.NONE);
        stage.setResizable(false);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
        //焦点失去事件，关闭窗口
        stage.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean onHidden, Boolean onShown)
            {
                stage.close();
            }
        });
    }


    private Node getMenu(List<KeyValuePair> items) {
        VBox vbox = new VBox();
        vbox.setMinHeight(buttonHeight);
        vbox.setMinWidth(leftMenuWidth);
        setPaneBackground(vbox, Color.web(COLOR_PRIMARY));
        // 增加菜单中的项目
        vbox.getChildren().addAll(getMenuItemList(leftMenuWidth,items));
        return vbox;
    }

    /**
     * 生成左侧菜单按钮
     */
    private List<Button> getMenuItemList(double width, List<KeyValuePair> itemNameList) {
        List<Button> buttonList = new ArrayList<>(itemNameList.size());
        for (KeyValuePair keyValuePair : itemNameList) {
            Button button = new Button(keyValuePair.getValue());
            button.setMinWidth(width);
            button.setPrefHeight(buttonHeight);
            button.setId(keyValuePair.getKey());
            setButtonBackground(button, Color.web(COLOR_PRIMARY), Color.BLACK);
            //增加鼠标移动到菜单上到hover效果
            button.setOnMouseMoved(event->{
                if(currentMenuIndex==null||!button.getId().equals(itemNameList.get(currentMenuIndex).getKey())) {
                    setButtonBackground(button, Color.web(COLOR_HOVER), Color.BLACK);
                    setFont(button, Color.BLACK, -1);
                }else {
                    setButtonBackground(button, Color.web(COLOR_HOVER), Color.web(COLOR_SELECTED));
                }
            });
            button.setOnMouseExited(event->{
                if(currentMenuIndex==null||!button.getId().equals(itemNameList.get(currentMenuIndex).getKey())) {
                    setButtonBackground(button, Color.web(COLOR_PRIMARY), Color.BLACK);
                }else {
                    setButtonBackground(button, Color.web(COLOR_PRIMARY), Color.web(COLOR_SELECTED));
                }

            });
            button.setOnMouseClicked(event->{
                String buttonId= button.getId();
                ObservableList<T> selectedRows=accountTableView.getSelectionModel().getSelectedItems();
                if(selectedRows.size()==0){
                    return;
                }
                if(buttonId.equalsIgnoreCase("copy")){
                    copyInfo(selectedRows.get(0));
                }else if(buttonId.equalsIgnoreCase("delete")){
                    Integer seqNo= ((SimpleIntegerProperty) ReflectUtil.getFieldValue(selectedRows.get(0), "seq")).getValue();
                    accountTableView.getItems().remove(seqNo-1);
                    accountTableView.refresh();
                }else if(buttonId.equalsIgnoreCase("smsCode")){
                    openCodePopup(selectedRows.get(0));
                }else if(buttonId.equalsIgnoreCase("reexecute")){
                    reExecute(selectedRows.get(0));
                }else if(buttonId.equalsIgnoreCase("twoFactorCode")){
                    twoFactorCodeExecute(selectedRows.get(0));
                }
                stage.close();
            });
            buttonList.add(button);
        }
        return buttonList;
    }
    /**
    　* 复制当前行信息
      * @param
     * @param selectedRow
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2023/12/22 17:55
    */
    private void copyInfo(T selectedRow){
        try {
            List<String> out=new ArrayList<>();
            JSON jsonObject=JSONUtil.parse(selectedRow);
            Class clazz = selectedRow.getClass();
            // 获取所有的属性
            Field[] fields = clazz.getDeclaredFields();
            // 遍历属性
            for (Field field : fields) {
                // 判断属性是否有注解
                if (field.isAnnotationPresent(CustomAnnotation.class)) {
                    CustomAnnotation annotation = field.getAnnotation(CustomAnnotation.class);
                    boolean copy = annotation.copy();
                    if(copy){
                        Object value=jsonObject.getByPath(field.getName());
                        if(null!=value && !StringUtils.isEmpty(value.toString())){
                            out.add(value.toString());
                        }else{
                            out.add(annotation.desc());
                        }
                    }
                }
            }
            String str = out.stream().collect(Collectors.joining("----"));
            ClipboardManager.setClipboard(str);
            super.alert("复制成功！");
        }catch (Exception e){

        }
    }
    /**
    　* 弹出验证码弹出框
      * @param
     * @param o
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2023/12/22 17:56
    */
    private void openCodePopup(T o){
       try {
           FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/comm-securitycode-popup.fxml"));
           Scene scene = new Scene(fxmlLoader.load(), 385, 170);
           scene.getRoot().setStyle("-fx-font-family: 'serif'");
           CommSecuritycodePopupView fxmlLoaderController = fxmlLoader.getController();
           String account= ((SimpleStringProperty)ReflectUtil.getFieldValue(o,"account")).getValue();
           fxmlLoaderController.setAccount(account);
           Stage popupStage = new Stage();
           popupStage.setTitle("双重验证码输入页面");
           popupStage.initModality(Modality.APPLICATION_MODAL);
           popupStage.setScene(scene);
           popupStage.setResizable(false);
           popupStage.initStyle(StageStyle.UTILITY);
           popupStage.showAndWait();
           String code = fxmlLoaderController.getSecurityCode();
           secondStepHandler(o,code);
       }catch (Exception e){

       }
    }

    /**
    　* 第二步操作
      * @param
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2023/12/22 17:38
    */
    protected void secondStepHandler(T o,String code){ }
    /**
    　* 重新执行
      * @param
     * @param o
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2023/12/22 18:04
    */
    protected void reExecute(T o){ }
    /**
     　* 双重验证
     * @param
     * @param o
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2023/12/22 18:04
     */
    protected void twoFactorCodeExecute(T o){ }


    public static void setPaneBackground(Pane pane, Color color) {
        pane.setBackground(new Background(new BackgroundFill(color, null, null)));
    }

    public static void setButtonBackground(Button button, Color bg, Color text) {
        button.setBackground(new Background(new BackgroundFill(bg, null, null)));
        button.setTextFill(text);
        button.setCursor(Cursor.HAND);
        button.setPadding(new Insets(5,0,5,5));
    }

    public static void setFont(Labeled node, Color color, double fontSize) {
        node.setTextFill(color);
        node.setFont(Font.font(null, FontWeight.BOLD, fontSize));
    }
}

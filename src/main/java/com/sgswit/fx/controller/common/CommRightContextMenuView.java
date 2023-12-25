package com.sgswit.fx.controller.common;

import cn.hutool.core.swing.clipboard.ClipboardMonitor;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.SecuritycodePopupController;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.model.Account;
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

import java.io.IOException;
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
    private Stage stage=null;
    private static List<String> copyFields=null;

    private static TableView accountTableView;





    private static List<KeyValuePair> allList=getAllList();

    private static List<KeyValuePair> getAllList(){
        List<KeyValuePair> allList=new ArrayList<>();
        Class<Constant.RightContextMenu> emClass = Constant.RightContextMenu.class;
        Constant.RightContextMenu[] arr = emClass.getEnumConstants();
        for (int i=0; i<arr.length;i++){
            KeyValuePair keyValuePair=new KeyValuePair(arr[i].getCode(),arr[i].getTitle(),arr[i].getPath());
            allList.add(keyValuePair);
        }
        return allList;
    }

    /**
     　* 右键点击事件
     * @param
     * @param contextMenuEvent
     * @param tableView
     * @param items 菜单
     * @param fields 需要复制的字段
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2023/12/22 13:52
     */
    protected void onContentMenuClick(ContextMenuEvent contextMenuEvent,TableView tableView,List<String> items,List<String> fields) {
        copyFields=fields;
        accountTableView=tableView;
        try {
            if(items.size()==0){
                return;
            }

            List<KeyValuePair> list=new ArrayList<>();
            for(String k:items){
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
            openMenu(contextMenuEvent,list);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


   /**
   　* 打开右键菜单
     * @param
    * @param contextMenuEvent
    * @param items
   　* @return void
   　* @throws
   　* @author DeZh
   　* @date 2023/12/22 20:52
   */
    private void openMenu(ContextMenuEvent contextMenuEvent, List<KeyValuePair> items) {
        double x= contextMenuEvent.getScreenX();
        double y=contextMenuEvent.getScreenY();
        stage = new Stage();
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
            String title=keyValuePair.getValue();
            Map<String,Object> userData=new HashMap<>();
            userData.put("title",keyValuePair.getValue());
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
                T account = selectedRows.get(0);
                if(buttonId.equalsIgnoreCase(Constant.RightContextMenu.COPY.getCode())){
                    copyInfo(account);
                }else if(buttonId.equalsIgnoreCase(Constant.RightContextMenu.DELETE.getCode())){
                    accountTableView.getItems().remove(account);
                    accountTableView.refresh();
                }else if(buttonId.equalsIgnoreCase(Constant.RightContextMenu.CODE.getCode())){
                    openCodePopup(account,title,Constant.RightContextMenu.CODE.getCode());
                }else if(buttonId.equalsIgnoreCase(Constant.RightContextMenu.REEXECUTE.getCode())){
                    reExecute(account);
                }else if(buttonId.equalsIgnoreCase(Constant.RightContextMenu.TWO_FACTOR_CODE.getCode())){
                    openCodePopup(account,title,Constant.RightContextMenu.TWO_FACTOR_CODE.getCode());
                }else if(buttonId.equalsIgnoreCase(Constant.RightContextMenu.WEB_TWO_FACTOR_CODE.getCode())){
                    if (account instanceof Account){
                        Account account1 = (Account) account;
                        String securityCode = openSecurityCodePopupView(account1.getAccount());
                        account1.setSecurityCode(securityCode);
                        accountHandler(account);
                    }
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
                    boolean copyFlag=false;
                    CustomAnnotation annotation = field.getAnnotation(CustomAnnotation.class);
                    boolean copy = annotation.copy();
                    if(copy){
                        if(null==copyFields ||copyFields.size()==0){
                            copyFlag=true;
                        }else if(copyFields.contains(field.getName())){
                            copyFlag=true;
                        }else{

                        }

                    }
                    if(copyFlag){
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
    private void openCodePopup(T o,String title,String type){
       try {
           FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/comm-code-popup.fxml"));
           Scene scene = new Scene(fxmlLoader.load(), 385, 170);
           scene.getRoot().setStyle("-fx-font-family: 'serif'");
           CommCodePopupView fxmlLoaderController = fxmlLoader.getController();
           String account= ((SimpleStringProperty)ReflectUtil.getFieldValue(o,"account")).getValue();
           fxmlLoaderController.setAccount(account);
           Stage popupStage = new Stage();
           popupStage.setTitle(title);
           popupStage.initModality(Modality.APPLICATION_MODAL);
           popupStage.setScene(scene);
           popupStage.setResizable(false);
           popupStage.initStyle(StageStyle.UTILITY);
           popupStage.showAndWait();
           String code = fxmlLoaderController.getSecurityCode();
           if(StringUtils.isEmpty(code)){
               return;
           }
           if(Constant.RightContextMenu.CODE.getCode().equals(type)){
               secondStepHandler(o,code);
           }else if(Constant.RightContextMenu.TWO_FACTOR_CODE.getCode().equals(type)){
               twoFactorCodeExecute(o,code);
           }
       }catch (Exception e){
            e.printStackTrace();
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
    protected void secondStepHandler(T account, String code){
    }

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
    protected void twoFactorCodeExecute(T o, String code){ }

    /**
     * 打开双重认证视图
     */
    public String openSecurityCodePopupView(String account){
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
        if (StrUtil.isEmpty(type) || StrUtil.isEmpty(code)){
            return "";
        }
        return type + "-" + code;
    }

    /**
     * 每一个账号的处理器
     */
    public void accountHandler(T account) {
    }

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

package com.sgswit.fx;

import cn.hutool.core.codec.Base64;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.model.KeyValuePair;
import com.sgswit.fx.utils.ProjectValues;
import com.sgswit.fx.utils.PropertiesUtil;
import com.sgswit.fx.utils.StringUtils;
import com.sgswit.fx.utils.StyleUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * @author DeZh
 */
public class MainController implements Initializable {
    @FXML
    public Pane rightMainPane;
    @FXML
    public Label remainingPoints;
    @FXML
    private ChoiceBox<KeyValuePair> agencyMode = new ChoiceBox<KeyValuePair>();
    @FXML
    private CheckBox isAutoLogin;
    @FXML
    private VBox leftMenu;
    private Integer currentMenuIndex;
    private Integer tempIndex;


    private final List<KeyValuePair> agencyModeList =new ArrayList<>(){{
        add(new KeyValuePair("none","不使用代理"));
        add(new KeyValuePair("api","API或导入代理"));
        add(new KeyValuePair("tunnel","使用隧道代理"));
        add(new KeyValuePair("in","使用内置代理"));
    }};

    /**
    　* @description:初始化页面数据
      * @param
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2023/9/7 15:01
    */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //初始化代理模式
        agencyMode.getItems().addAll(agencyModeList);
        agencyMode.converterProperty().set(new StringConverter<KeyValuePair>() {
            @Override
            public String toString(KeyValuePair object) {
                return object.getValue();
            }

            @Override
            public KeyValuePair fromString(String string) {
                return null;
            }
        });
        agencyMode.getSelectionModel().select(0);
        agencyModeListener();

        //初始化是否自动登录
        isAutoLogin.setSelected(PropertiesUtil.getOtherBool("login.auto",false));
        isAutoLoginModeListener();
        //初始化左侧菜单
        leftMenu.getChildren().add(getLeftMenu());
        leftMenu.setBackground(Background.EMPTY);
        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            Pane p = fxmlLoader.load(MainApplication.class.getResource("views/base/notice-view.fxml").openStream());
            rightMainPane.getChildren().add(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshRemainingPoints();

    }

    private Node getLeftMenu() {
        double leftWidth = ProjectValues.leftMenuWidth;
        VBox vbox = new VBox();
        vbox.setMinHeight(30);
        vbox.setMinWidth(leftWidth);
        StyleUtil.setPaneBackground(vbox, Color.web(ProjectValues.COLOR_PRIMARY));
        // 增加菜单中的项目
        vbox.getChildren().addAll(getLeftMenuItemList(leftWidth));
        return vbox;
    }

    /**
     * 生成左侧菜单按钮
     */
    private List<Button> getLeftMenuItemList(double width) {
        List<KeyValuePair> itemNameList=new ArrayList<>(){{
            add(new KeyValuePair("iTunes","iTunes专区","views/iTunes/iTunes-items-list.fxml"));
            add(new KeyValuePair("iCloud","iCloud专区","views/iCloud/iCloud-items-list.fxml"));
            add(new KeyValuePair("operation","修改操作专区","views/operation/operation-items-list.fxml"));
            add(new KeyValuePair("query","查询检测专区","views/query/query-items-list.fxml"));
            add(new KeyValuePair("toolbox","工具箱","views/tool/toolbox-items-list.fxml"));
        }};
        double buttonHeight = 30;
        List<Button> buttonList = new ArrayList<>(3);
        for (KeyValuePair keyValuePair : itemNameList) {
            Button button = new Button(keyValuePair.getValue());
            button.setMinWidth(width);
            button.setMinHeight(buttonHeight);
            button.setId(keyValuePair.getKey());
            StyleUtil.setButtonBackground(button, Color.web(ProjectValues.COLOR_PRIMARY), Color.WHITE);
            //增加鼠标移动到菜单上到hover效果
            button.setOnMouseMoved(event->{
                if(currentMenuIndex==null||!button.getId().equals(itemNameList.get(currentMenuIndex).getKey())) {
                    StyleUtil.setButtonBackground(button, Color.web(ProjectValues.COLOR_HOVER), Color.WHITE);
                    StyleUtil.setFont(button, Color.WHITE, -1);
                }else {
                    StyleUtil.setButtonBackground(button, Color.web(ProjectValues.COLOR_HOVER), Color.web(ProjectValues.COLOR_SELECTED));
                }
            });
            button.setOnMouseExited(event->{
                if(currentMenuIndex==null||!button.getId().equals(itemNameList.get(currentMenuIndex).getKey())) {
                    StyleUtil.setButtonBackground(button, Color.web(ProjectValues.COLOR_PRIMARY), Color.WHITE);
                }else {
                    StyleUtil.setButtonBackground(button, Color.web(ProjectValues.COLOR_PRIMARY), Color.web(ProjectValues.COLOR_SELECTED));
                }

            });
            button.setOnMouseClicked(event->{
                int index=0;
                Optional<Integer> first = itemNameList.stream().filter(i -> Objects.equals(i.getKey(), button.getId())). map(itemNameList::indexOf).findFirst();
                if (first.isPresent()) {
                    if(!button.getId().equalsIgnoreCase("toolbox")){
                        currentMenuIndex= first.get();
                    }
                    index=first.get();
                }
                if(!button.getId().equalsIgnoreCase("toolbox")){
                    if(rightMainPane.getChildren().size()>0){
                        rightMainPane.getChildren().clear();
                    }
                    try {
                        FXMLLoader fxmlLoader = new FXMLLoader();
                        Pane p = fxmlLoader.load(MainApplication.class.getResource(itemNameList.get(currentMenuIndex).getPath()).openStream());
                        rightMainPane.getChildren().add(p);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    StyleUtil.setFont(button, Color.web(ProjectValues.COLOR_SELECTED), -1);
                    //选中状态逻辑
                    if(tempIndex!=null) {
                        VBox vBox= (VBox) leftMenu.getChildren().get(0);
                        Button node = (Button) vBox.getChildren().get(tempIndex);
                        //清空选中状态样式
                        StyleUtil.setFont(node, Color.WHITE, -1);
                        StyleUtil.setButtonBackground(node, Color.web(ProjectValues.COLOR_PRIMARY), Color.WHITE);
                    }
                    //设置选中样式
                    StyleUtil.setFont(button, Color.web(ProjectValues.COLOR_SELECTED), -1);
                    tempIndex = currentMenuIndex;
                }else{
                    try {
                        FXMLLoader fxmlLoader = new FXMLLoader();
                        Pane p = fxmlLoader.load(MainApplication.class.getResource(itemNameList.get(index).getPath()).openStream());
                        rightMainPane.getChildren().add(p);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            });
            buttonList.add(button);
        }
        return buttonList;
    }





    /**代理模式监听*/
    protected void agencyModeListener(){
//        loginMode.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {
//            @Override
//            public void changed(ObservableValue observableValue, Object o, Object t1) {
//
//                Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                alert.setTitle("提示");
//                String msg= MessageFormat.format("{0}修改成功，配置已生效！",loginModeList.get(Integer.valueOf(t1.toString())).getValue());
//                alert.setHeaderText(msg);
//                alert.show();
//                //修改本地配置文件
//            }
//        });

    }
    /**是否自动登录监听*/
    protected void isAutoLoginModeListener(){
        isAutoLogin.selectedProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object autoLogin) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("提示");
                String msg="";
                if((boolean)autoLogin){
                    msg= "已开启自动登录模式，下次登录时将自动登录软件！";
                }else{
                    msg= "已关闭自动登录模式，下次登录时将执行手动登录！";
                }
                alert.setHeaderText(msg);
                alert.show();
                // 修改本地配置文件
                PropertiesUtil.setOtherConfig("login.auto",autoLogin.toString());
                if ((boolean)autoLogin){
                    PropertiesUtil.setOtherConfig("login.rememberMe","true");
                }
            }
        });

    }
   /**在线购买**/
    @FXML
    protected void onLineBuyBtnClick(ActionEvent actionEvent) throws IOException, URISyntaxException {
        String url = "https://www.baidu.com";
        browse(url);
    }
    private static void browse(String url) {
        try {
            String osName = System.getProperty("os.name", "");
            if (osName.startsWith("Mac OS")) {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
                openURL.invoke(null, url);
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else {
                // Unix or Linux
                String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++){
                    // 执行代码，在brower有值后跳出，
                    // 这里是如果进程创建成功了，==0是表示正常结束。
                    if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0){
                        browser = browsers[count];
                    }
                }
                if(browser == null) {
                    throw new Exception("Could not find web browser");
                }else{
                    // 这个值在上面已经成功的得到了一个进程。
                    Runtime.getRuntime().exec(new String[]{browser, url});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    protected void onIpProxyAndThreadSettingsBtnClick(ActionEvent actionEvent) throws IOException {

    }
    /**打开自助充值页面**/
    @FXML
    protected void onSelfServiceTopUpBtnClick(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/base/selfServiceCharge.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 385, 170);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        Stage popupStage = new Stage();

        popupStage.setTitle("自助充值");

        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.show();
    }
    @FXML
    protected void callQQ(){
        String url = "http://wpa.qq.com/msgrd?v=3&uin=748895431&site=qq&menu=yes";
        browse(url);
    }
    @FXML
    public void refreshRemainingPoints() {
        //加载点数
        String s = PropertiesUtil.getOtherConfig("login.info");
        JSONObject object = JSONUtil.parseObj(Base64.decodeStr(s));
        Object remainingPointsObj= object.getByPath("remainingPoints");
        if(StringUtils.isEmpty(remainingPointsObj)){
            remainingPoints.setText("0");
        }else{
            remainingPoints.setText(remainingPointsObj.toString());
        }
    }
}

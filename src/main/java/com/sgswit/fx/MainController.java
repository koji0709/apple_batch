package com.sgswit.fx;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CommonView;
import com.sgswit.fx.enums.ProxyEnum;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.model.KeyValuePair;
import com.sgswit.fx.utils.*;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * @author DeZh
 */
public class MainController implements Initializable {
    @FXML
    public Pane rightMainPane;
    @FXML
    public Label remainingPoints;
    @FXML
    private ChoiceBox<Map> proxyMode = new ChoiceBox<>();
    @FXML
    private CheckBox isAutoLogin;
    @FXML
    private VBox leftMenu;
    private Integer currentMenuIndex;
    private Integer tempIndex;
    private final List<Map<String, Object>> proxyModeList = DataUtil.getProxyModeList();

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
        proxyMode.getItems().addAll(proxyModeList);
        proxyMode.converterProperty().set(new StringConverter<Map>() {
            @Override
            public String toString(Map map) {
                return MapUtil.getStr(map,"value");
            }

            @Override
            public Map fromString(String string) {
                return null;
            }
        });
        Integer proxyModeIndex=PropertiesUtil.getOtherInt("proxyMode");
        proxyMode.getSelectionModel().select(proxyModeIndex);
        proxyModeListener();

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
        //获取剩余点数
        Map<String,Object> userInfo=DataUtil.getUserInfo();
        remainingPoints.setText(StrUtil.isEmptyIfStr(userInfo.get("remainingPoints"))?"0":MapUtil.getStr(userInfo,"remainingPoints"));

    }

    private Node getLeftMenu() {
        double leftWidth = StyleUtil.leftMenuWidth;
        VBox vbox = new VBox();
        vbox.setMinHeight(30);
        vbox.setMinWidth(leftWidth);
        StyleUtil.setPaneBackground(vbox, Color.web(StyleUtil.COLOR_PRIMARY));
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
        List<Button> buttonList = new ArrayList<>(itemNameList.size());
        int buttonIndex=0;
        for (KeyValuePair keyValuePair : itemNameList) {
            Button button = new Button(keyValuePair.getValue());
            button.setMinWidth(width);
            button.setMinHeight(buttonHeight);
            button.setId(keyValuePair.getKey());
            button.setUserData(buttonIndex);
            StyleUtil.setButtonBackground(button, Color.web(StyleUtil.COLOR_PRIMARY), Color.WHITE);
            //增加鼠标移动到菜单上到hover效果
            button.setOnMouseMoved(event->{
                if(currentMenuIndex==null||!button.getId().equals(itemNameList.get(currentMenuIndex).getKey())) {
                    StyleUtil.setButtonBackground(button, Color.web(StyleUtil.COLOR_HOVER), Color.WHITE);
                    StyleUtil.setFont(button, Color.WHITE, -1);
                }else {
                    StyleUtil.setButtonBackground(button, Color.web(StyleUtil.COLOR_HOVER), Color.web(StyleUtil.COLOR_SELECTED));
                }
            });
            button.setOnMouseExited(event->{
                if(currentMenuIndex==null||!button.getId().equals(itemNameList.get(currentMenuIndex).getKey())) {
                    StyleUtil.setButtonBackground(button, Color.web(StyleUtil.COLOR_PRIMARY), Color.WHITE);
                }else {
                    StyleUtil.setButtonBackground(button, Color.web(StyleUtil.COLOR_PRIMARY), Color.web(StyleUtil.COLOR_SELECTED));
                }

            });
            button.setOnMouseClicked(event->{
                int index=0;
                Optional<Integer> first = itemNameList.stream().filter(i -> Objects.equals(i.getKey(), button.getId())). map(itemNameList::indexOf).findFirst();
                if (first.isPresent()) {
                    if(!"toolbox".equalsIgnoreCase(button.getId())){
                        currentMenuIndex= first.get();
                    }
                    index=first.get();
                }
                if(!"toolbox".equalsIgnoreCase(button.getId())){
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

                    StyleUtil.setFont(button, Color.web(StyleUtil.COLOR_SELECTED), -1);
                    //选中状态逻辑
                    if(tempIndex!=null) {
                        VBox vBox= (VBox) leftMenu.getChildren().get(0);
                        Button node = (Button) vBox.getChildren().get(tempIndex);
                        //清空选中状态样式
                        StyleUtil.setFont(node, Color.WHITE, -1);
                        StyleUtil.setButtonBackground(node, Color.web(StyleUtil.COLOR_PRIMARY), Color.WHITE);
                    }
                    //设置选中样式
                    StyleUtil.setFont(button, Color.web(StyleUtil.COLOR_SELECTED), -1);
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
            buttonIndex++;
            buttonList.add(button);
        }
        StyleUtil.first=true;
        return buttonList;
    }





    /**代理模式监听*/
    protected void proxyModeListener(){
        proxyMode.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                String oldV=o.toString();
                String v=t1.toString();
                if(ProxyEnum.Mode.API.getKey().equals(v)){
                   String proxyApiUrl= PropertiesUtil.getOtherConfig("proxyApiUrl");
                   String proxyApiUser= PropertiesUtil.getOtherConfig("proxyApiUser");
                   String proxyApiPass= PropertiesUtil.getOtherConfig("proxyApiPass");
                   boolean proxyApiNeedPass= PropertiesUtil.getOtherBool("proxyApiNeedPass",false);
                    if(StringUtils.isEmpty(proxyApiUrl)){
                        CommonView.alert(ProxyEnum.Mode.API.getAlertMessage());
                        proxyMode.getSelectionModel().select(Integer.valueOf(oldV));
                    }else if( !proxyApiNeedPass&& (StringUtils.isEmpty(proxyApiUser) ||StringUtils.isEmpty(proxyApiPass))){
                        CommonView.alert(ProxyEnum.Mode.API.getAlertMessage());
                        proxyMode.getSelectionModel().select(Integer.valueOf(oldV));
                    }
                }else if(ProxyEnum.Mode.TUNNEL.getKey().equals(v)){
                    String proxyTunnelAddress= PropertiesUtil.getOtherConfig("proxyTunnelAddress");
                    String proxyTunnelUser= PropertiesUtil.getOtherConfig("proxyTunnelUser");
                    String proxyTunnelPass= PropertiesUtil.getOtherConfig("proxyTunnelPass");
                    if(StringUtils.isEmpty(proxyTunnelAddress) || StringUtils.isEmpty(proxyTunnelUser) ||StringUtils.isEmpty(proxyTunnelPass)){
                        CommonView.alert(ProxyEnum.Mode.TUNNEL.getAlertMessage());
                        proxyMode.getSelectionModel().select(Integer.valueOf(oldV));
                    }
                }
                //修改本地配置文件
                PropertiesUtil.setOtherConfig("proxyMode",v);
            }
        });

    }
    /**是否自动登录监听*/
    protected void isAutoLoginModeListener(){
        isAutoLogin.selectedProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object autoLogin) {
                String msg="";
                if((boolean)autoLogin){
                    msg= "已开启自动登录模式，下次登录时将自动登录软件！";
                }else{
                    msg= "已关闭自动登录模式，下次登录时将执行手动登录！";
                }
                CommonView.alert(msg);
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
        String url = PropertiesUtil.getConfig("online.buyUrl");
        browse(url);
    }
    private static void browse(String url) {
        try {
            String osName = System.getProperty("os.name", "");
            if (osName.startsWith("Mac OS")) {
                URI uri = new URI(url);
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(uri);
                } else {
                    //CommonView.alert("自动打开系统默认浏览器失败，请手动下载。\n" + "地址：" + url);
                }
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
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/base/integrated-settings.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getRoot().setStyle("-fx-font-family: 'serif';-fx-padding: 14;");

        Stage popupStage = new Stage();
        popupStage.setTitle("综合设置");
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.setAlwaysOnTop(true);
        String logImg= PropertiesUtil.getConfig("softwareInfo.log.path");
        popupStage.getIcons().add(new Image(this.getClass().getResource(logImg).toString()));
        popupStage.initStyle(StageStyle.DECORATED);
        popupStage.show();

    }
    /**打开自助充值页面**/
    @FXML
    protected void onSelfServiceTopUpBtnClick(ActionEvent actionEvent) throws IOException {
        StageUtil.show(StageEnum.SELF_SERVICE_CHARGE);
    }
    @FXML
    public void refreshRemainingPoints() {
        ThreadUtil.execAsync(()->{
            String base64UserName=PropertiesUtil.getOtherConfig("login.userName");
            HttpResponse rsp = HttpUtils.get("/userInfo/getInfoByUserName/"+SignUtil.decryptBase64(base64UserName));
            JSON json= JSONUtil.parse(rsp.body());
            if (json.getByPath("code",String.class).equals(Constant.SUCCESS)){
                remainingPoints.setText( json.getByPath("data.remainingPoints",String.class));
            }else {
                remainingPoints.setText("0");
            }
        });
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(100), remainingPoints);
        translateTransition.setFromY(-1);
        translateTransition.setToY(1);
        translateTransition.setFromX(-1);
        translateTransition.setToX(1);
        translateTransition.setCycleCount(1);
        translateTransition.play();
    }
    @FXML
    public void onLineUpgrader(ActionEvent actionEvent) {
        String url = PropertiesUtil.getConfig("online.upgraderUrl");
        browse(url);
    }
}

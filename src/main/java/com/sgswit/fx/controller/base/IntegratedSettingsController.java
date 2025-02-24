package com.sgswit.fx.controller.base;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.common.CommonView;
import com.sgswit.fx.enums.ProxyEnum;
import com.sgswit.fx.utils.PropertiesUtil;
import com.sgswit.fx.utils.proxy.ProxyAuthenticator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.net.Authenticator;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author DeZh
 * @title: IntegratedSettingsController
 * @projectName appleBatch
 * @description: TODO
 * @date 2024/1/2915:14
 */
public class IntegratedSettingsController implements Initializable {
    @FXML
    public ComboBox<Map<String,String>> proxyType;
    @FXML
    public TextField proxyApiUrl;
    @FXML
    public CheckBox proxyApiNeedPass;
    @FXML
    public TextField proxyApiUser;
    @FXML
    public TextField proxyApiPass;
    @FXML
    public TextField proxyTunnelAddress;
    @FXML
    public TextField proxyTunnelUser;
    @FXML
    public TextField proxyTunnelPass;
    @FXML
    public TextField sendTimeOut;
    @FXML
    public TextField readTimeOut;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        proxyTypeFn();
        initParas();
    }
    protected void proxyTypeFn(){
        proxyType.getItems().addAll(ProxyEnum.Type.getProxyTypeList());
        proxyType.converterProperty().set(new StringConverter<>() {
            @Override
            public String toString(Map<String, String> map) {
                if(null==map){
                    return "1";
                }
                return map.get("value");
            }
            @Override
            public Map<String, String> fromString(String string) {
                return null;
            }
        });
    }
    protected void initParas(){
        proxyApiUrl.setText(PropertiesUtil.getOtherConfig("proxyApiUrl"));
        proxyApiUser.setText(PropertiesUtil.getOtherConfig("proxyApiUser"));
        proxyApiPass.setText(PropertiesUtil.getOtherConfig("proxyApiPass"));
        proxyTunnelAddress.setText(PropertiesUtil.getOtherConfig("proxyTunnelAddress"));
        proxyTunnelUser.setText(PropertiesUtil.getOtherConfig("proxyTunnelUser"));
        proxyTunnelPass.setText(PropertiesUtil.getOtherConfig("proxyTunnelPass"));
        sendTimeOut.setText(PropertiesUtil.getOtherConfig("sendTimeOut"));
        readTimeOut.setText(PropertiesUtil.getOtherConfig("readTimeOut"));
        if(StringUtils.isEmpty(PropertiesUtil.getOtherConfig("proxyApiNeedPass")) || !Boolean.valueOf(PropertiesUtil.getOtherConfig("proxyApiNeedPass"))){
            proxyApiNeedPass.setSelected(false);
        }else{
            proxyApiNeedPass.setSelected(true);
        }
        String proxyType= PropertiesUtil.getOtherConfig("proxyType");
        if(!StringUtils.isEmpty(proxyType)){
            this.proxyType.getSelectionModel().select(Integer.valueOf(proxyType)-1);
        }
    }
    @FXML
    public void proxyTunnelAddressCheckAction(ActionEvent actionEvent) {
        try{
            String address= PropertiesUtil.getOtherConfig("proxyTunnelAddress");
            String proxyHost=address.split(":")[0];
            int proxyPort=Integer.valueOf(address.split(":")[1]);
            String authUser=PropertiesUtil.getOtherConfig("proxyTunnelUser");
            String authPassword= PropertiesUtil.getOtherConfig("proxyTunnelPass");
            String url = PropertiesUtil.getOtherConfig("online.checkIp");
            // JDK 8u111版本后，目标页面为HTTPS协议，启用proxy用户密码鉴权
            System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
            Authenticator.setDefault(new ProxyAuthenticator(authUser, authPassword));
            // 发送请求
            HttpResponse result = HttpUtil.createRequest(Method.GET,url)
                    .setHttpProxy(proxyHost, proxyPort )
                    .timeout(20000)
                    .execute();
            if(result.getStatus()==200){
                JSON json=JSONUtil.parse(result.body());
                if("0".equals(json.getByPath("data.code",String.class))){
                    String country= json.getByPath("data.country",String.class);
                    String region= json.getByPath("data.region",String.class);
                    String city= json.getByPath("data.city",String.class);
                    String isp= json.getByPath("data.isp",String.class);
                    String ip= json.getByPath("data.ip",String.class);
                    String m= MessageFormat.format("IP:{0}，所属国家：{1}，省份：{2}，城市：{3}，运营商：{4}",new String[]{ip,country,region,city,isp});
                    CommonView.alert(m, Alert.AlertType.INFORMATION,true);
                }else{
                    CommonView.alert("有效的隧道代理", Alert.AlertType.INFORMATION,true);
                }
            }else {
                CommonView.alert("无效的隧道代理", Alert.AlertType.INFORMATION,true);
            }
        }catch (Exception e){

        }

    }
    @FXML
    public void saveAction(ActionEvent actionEvent) throws Exception {
        PropertiesUtil.setOtherConfig("proxyApiUrl", proxyApiUrl.getText());
        PropertiesUtil.setOtherConfig("proxyApiUser", proxyApiUser.getText());
        PropertiesUtil.setOtherConfig("proxyApiPass", proxyApiPass.getText());
        PropertiesUtil.setOtherConfig("proxyTunnelAddress", proxyTunnelAddress.getText());
        PropertiesUtil.setOtherConfig("proxyTunnelUser", proxyTunnelUser.getText());
        PropertiesUtil.setOtherConfig("proxyTunnelPass", proxyTunnelPass.getText());
        PropertiesUtil.setOtherConfig("sendTimeOut", sendTimeOut.getText());
        PropertiesUtil.setOtherConfig("readTimeOut", readTimeOut.getText());
        PropertiesUtil.setOtherConfig("proxyApiNeedPass", proxyApiNeedPass.isSelected()?"true":"");
        int index=proxyType.getSelectionModel().getSelectedIndex()+1;
        PropertiesUtil.setOtherConfig("proxyType", index>0?String.valueOf(index):"");
        Button button= (Button) actionEvent.getSource();
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }
}

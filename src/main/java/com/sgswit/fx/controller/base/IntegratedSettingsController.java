package com.sgswit.fx.controller.base;

import com.sgswit.fx.enums.ProxyEnum;
import com.sgswit.fx.utils.PropertiesUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
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
    public GridPane rootPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        proxyTypeFn();
        initParas();
    }
    /**快捷国家资料下拉**/
    protected void proxyTypeFn(){
        proxyType.getItems().addAll(ProxyEnum.Type.getProxyTypeList());
        proxyType.converterProperty().set(new StringConverter<>() {
            @Override
            public String toString(Map<String, String> map) {
                if(null==map){
                    return "";
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
        PropertiesUtil.setOtherConfig("proxyApiNeedPass", proxyApiNeedPass.isSelected()?"true":"");
        int index=proxyType.getSelectionModel().getSelectedIndex()+1;
        PropertiesUtil.setOtherConfig("proxyType", index>0?String.valueOf(index):"");
        Button button= (Button) actionEvent.getSource();
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }
}

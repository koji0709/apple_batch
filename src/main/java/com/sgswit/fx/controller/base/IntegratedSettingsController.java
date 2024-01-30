package com.sgswit.fx.controller.base;

import com.sgswit.fx.enums.ProxyEnum;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        proxyTypeFn();
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
}

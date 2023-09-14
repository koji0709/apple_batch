package com.sgswit.fx.controller.iTunes;

import com.sgswit.fx.model.KeyValuePair;
import com.sgswit.fx.utils.StringUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TitledPane;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author DeZh
 * @title: CustomCountryDetPopupController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/1320:45
 */
public class CustomCountryDetPopupController implements Initializable {
    @FXML
    public ChoiceBox countryListBox;
    @FXML
    public TitledPane billMailingAddressId;

    private List<KeyValuePair> countryList=new ArrayList<>(){{
        add(new KeyValuePair("","请选择"));
        add(new KeyValuePair("CHN","中国"));
        add(new KeyValuePair("CHL","智利"));
    }};

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        selectCountryListener();
    }

    protected void selectCountryListener(){
        countryListBox.getItems().addAll(countryList);
        countryListBox.converterProperty().set(new StringConverter<KeyValuePair>() {
            @Override
            public String toString(KeyValuePair object) {
                return object.getValue();
            }

            @Override
            public KeyValuePair fromString(String string) {
                return null;
            }
        });
        countryListBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {

                if(!StringUtils.isEmpty(countryList.get(Integer.valueOf(t1.toString())).getKey())){
                    billMailingAddressId.setDisable(false);
                }else{
                    billMailingAddressId.setDisable(true);
                }
            }
        });

    }
}

package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.iTunes.model.*;
import com.sgswit.fx.model.KeyValuePair;
import com.sgswit.fx.utils.StringUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.nio.charset.Charset;
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
    public TitledPane billMailingAddressPane;
    @FXML
    public TextField firstName;
    @FXML
    public TextField lastName;
    @FXML
    public TextField line1;
    @FXML
    public TextField line2;
    @FXML
    public TextField suburb;
    @FXML
    public TextField county;
    @FXML
    public TextField city;
    @FXML
    public TextField postalCode;
    @FXML
    public TextField stateProvinceName;
    @FXML
    public TextField areaCode;
    @FXML
    public TextField number;
    @FXML
    public TextField name;

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
                    billMailingAddressPane.setDisable(false);
                }else{
                    billMailingAddressPane.setDisable(true);
                }
            }
        });

    }

    public void onSaveUserNationalDataBtnClick(ActionEvent actionEvent) {
        try {
            List<UserNationalModel> list=new ArrayList<>();
            UserNationalModel userNationalModel=new UserNationalModel();
            File userNationalDataFile = FileUtil.file("userNationalData.json");
            if(!userNationalDataFile.exists()){
                new File("userNationalData.json").createNewFile();
            }
            // 创建json文件对象
            File jsonFile = new File("userNationalData.json");
            String jsonString = FileUtil.readString(jsonFile,Charset.defaultCharset());
            if(!StringUtils.isEmpty(jsonString)){
                list = JSONUtil.toList(jsonString,UserNationalModel.class);
            }
            userNationalModel.setId(IdUtil.simpleUUID());
            userNationalModel.setName(name.getText());
            Payment payment=new Payment();
            //地址信息
            BillingAddress billingAddress=new BillingAddress();
            billingAddress.setCity(city.getText());
            billingAddress.setCountryCode("");
            billingAddress.setCounty(county.getText());
            billingAddress.setLine1(line1.getText());
            billingAddress.setLine2(line2.getText());
            billingAddress.setLine3("");
            billingAddress.setPostalCode(postalCode.getText());
            billingAddress.setStateProvinceName(stateProvinceName.getText());
            billingAddress.setSuburb(suburb.getText());
            //姓名
            OwnerName ownerName=new OwnerName();
            ownerName.setFirstName(firstName.getText());
            ownerName.setLastName(lastName.getText());
            //电话
            PhoneNumber phoneNumber=new PhoneNumber();
            phoneNumber.setAreaCode(areaCode.getText());
            phoneNumber.setCountryCode("");
            phoneNumber.setNumber(number.getText());
            payment.setBillingAddress(billingAddress);
            payment.setOwnerName(ownerName);
            payment.setPhoneNumber(phoneNumber);
            userNationalModel.setPayment(payment);
            list.add(userNationalModel);
            FileWriter fw = new FileWriter("userNationalData.json", Charset.defaultCharset(),false);
            fw.write(JSONUtil.toJsonStr(list));
            fw.flush();
            fw.close();

            Stage stage=(Stage)(((Node)(actionEvent.getSource())).getScene().getWindow());
            stage.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.iTunes.bo.BillingAddressParas;
import com.sgswit.fx.controller.iTunes.bo.FieldModel;
import com.sgswit.fx.controller.iTunes.bo.PaymentModel;
import com.sgswit.fx.controller.iTunes.bo.UserNationalModel;
import com.sgswit.fx.model.BaseAreaInfo;
import com.sgswit.fx.model.KeyValuePair;
import com.sgswit.fx.utils.DataUtil;
import com.sgswit.fx.utils.StringUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author DeZh
 * @title: CustomCountryDetPopupController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/1320:45
 */
public class CustomCountryDetPopupController implements Initializable {
    @FXML
    public ChoiceBox<KeyValuePair> countryBox;
    @FXML
    public TextField name;
    @FXML
    public Pane billMailingAddressPane;

    private List<KeyValuePair> countryList=new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        selectCountryListener();
    }
    protected void getAddressFormat(String countryCode){
        billMailingAddressPane.getChildren().clear();
        String jsonString= DataUtil.getAddressFormat(countryCode);
        List<FieldModel> fieldModelList=JSONUtil.parseObj(jsonString).getBeanList("addressFormatList", FieldModel.class);
        Iterator iterator = fieldModelList.iterator();
        while (iterator.hasNext()){
            FieldModel fieldModel = (FieldModel) iterator.next();
            if(fieldModel.getId().equals("country")){
                iterator.remove();
            }
        }
        GridPane gridPane=new GridPane();
        //是否显示边框
        gridPane.setGridLinesVisible(true);
        gridPane.setPrefWidth(474);
        Label l1=createLabel("提示", true,158,25,14);
        Label l2=createLabel("项目", true,158,25,14);
        Label l3=createLabel("资料", true,158,25,14);
        gridPane.addRow(0,l1,l2,l3);

        int index=1;
        for(FieldModel fieldModel:fieldModelList){
            Label label1=createLabel(fieldModel.isRequired()?"必填":"选填", false,158,25,14);
            if(fieldModel.isRequired()){
                label1.setTextFill(Color.RED);
            }
            Label label2=createLabel(fieldModel.getTitle(), false,158,25,14);
            if(fieldModel.getType().equals("dropdown")){
                ChoiceBox<KeyValuePair> choiceBox=new ChoiceBox<>();
                for(Map<String,String> map:fieldModel.getValues()){
                    choiceBox.getItems().add(new KeyValuePair(map.get("name"),map.get("title")));
                }
                choiceBox.setPrefWidth(158);
                choiceBox.converterProperty().set(new StringConverter<KeyValuePair>() {
                    @Override
                    public String toString(KeyValuePair keyValuePair) {
                        return keyValuePair.getKey()+"-"+keyValuePair.getValue();
                    }

                    @Override
                    public KeyValuePair fromString(String string) {
                        return null;
                    }
                });
                BillingAddressParas.Paras keyValuePair= BillingAddressParas.getParasInfoByKey(fieldModel.getId());
                if(null!=keyValuePair){
                    choiceBox.setId(keyValuePair.getPath());
                }else{
                    choiceBox.setId("");
                }
                gridPane.addRow(index,label1,label2,choiceBox);
            }else{
                TextField textField=new TextField();
                textField.setEditable(true);
                textField.setAlignment(Pos.CENTER);
                textField.setPrefWidth(158);
                BillingAddressParas.Paras keyValuePair= BillingAddressParas.getParasInfoByKey(fieldModel.getId());
                if(null!=keyValuePair){
                    textField.setId(keyValuePair.getPath());
                }else{
                    textField.setId("");
                }
                gridPane.addRow(index,label1,label2,textField);
            }
            index++;

        }
        billMailingAddressPane.getChildren().add(gridPane);
    }
   //创建label标签
    protected Label createLabel(String title,boolean isBold, double width,double height,int fontSize){
        Label label=new Label(title);
        label.setPrefWidth(width);
        label.setPrefHeight(height);
        label.setAlignment(Pos.CENTER);
        label.setFont(Font.font(null, isBold?FontWeight.BOLD:FontWeight.NORMAL,fontSize));
        return label;
    }

    protected void selectCountryListener(){
        //loading国家信息
        List<BaseAreaInfo> list= DataUtil.getCountry();
        for(BaseAreaInfo baseAreaInfo: list){
            countryList.add(new KeyValuePair(baseAreaInfo.getCode(),baseAreaInfo.getNameZh()+" - "+baseAreaInfo.getCode()));
        }
        countryBox.getItems().addAll(countryList);
        countryBox.converterProperty().set(new StringConverter<KeyValuePair>() {
            @Override
            public String toString(KeyValuePair object) {
                return object.getValue();
            }

            @Override
            public KeyValuePair fromString(String string) {
                return null;
            }
        });
        countryBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                if(!StringUtils.isEmpty(countryList.get(Integer.valueOf(t1.toString())).getKey())){
                    getAddressFormat(countryList.get(Integer.valueOf(t1.toString())).getKey());
                }else{

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

            GridPane gridPane= (GridPane) billMailingAddressPane.getChildren().get(0);
            if(StringUtils.isEmpty(name.getText())){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("提示");
                alert.setHeaderText("请检查必填项");
                alert.show();
                return;
            }
            String addressFormatListStr=DataUtil.getAddressFormat(countryBox.getSelectionModel().getSelectedItem().getKey());
//            List<FieldModel> addressFormatList=JSONUtil.parseObj(addressFormatListStr).getBeanList("addressFormatList", FieldModel.class);
            JSON json=JSONUtil.createObj();
            boolean defaultPhoneNumberCountryCode=true;
            for(Node node:gridPane.getChildren()){
                if(null!=node.getId() && BillingAddressParas.hasObjByPath(node.getId())){
                    String newId=node.getId().replace("_",".");
                    BillingAddressParas.Paras parasObj=BillingAddressParas.getParasInfoByPath(node.getId());
                    if(parasObj.getType().equals("text")){
                        String value=((TextField)node).getText();
//                        if(StringUtils.isEmpty(value)){
//                            Alert alert = new Alert(Alert.AlertType.ERROR);
//                            alert.setTitle("提示");
//                            alert.setHeaderText("请检查必填项");
//                            alert.show();
//                            return;
//                        }
                        json.putByPath(newId,value);
                        if(newId.equals("phoneNumber.countryCode")){
                            defaultPhoneNumberCountryCode=false;
                        }
                    }else{
                        KeyValuePair keyValuePair=  (KeyValuePair)((ChoiceBox)node).getSelectionModel().getSelectedItem();
//                        if(StringUtils.isEmpty(value)){
//                            Alert alert = new Alert(Alert.AlertType.ERROR);
//                            alert.setTitle("提示");
//                            alert.setHeaderText("请检查必填项");
//                            alert.show();
//                            return;
//                        }
                        json.putByPath(newId,keyValuePair.getKey());
                    }

                }
            }
            String countryCode=countryBox.getSelectionModel().getSelectedItem().getKey();
            json.putByPath("billingAddress.countryCode",countryCode);
            if(defaultPhoneNumberCountryCode){
                json.putByPath("phoneNumber.countryCode",DataUtil.getInfoByCountryCode(countryCode).getDialCode());
            }

            PaymentModel paymentModel=JSONUtil.toBean(json, PaymentModel.class,true);
            UserNationalModel u=  new UserNationalModel();
            u.setName(name.getText());
            u.setId(IdUtil.simpleUUID());
            u.setPayment(paymentModel);
            list.add(u);
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

package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.controller.iTunes.bo.UserNationalModel;
import com.sgswit.fx.controller.iTunes.vo.CountryVo;
import com.sgswit.fx.utils.StringUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author DeZh
 * @title: CustomCountryDetPopupController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/1320:45
 */
public class CustomCountryPopupController implements Initializable {
    @FXML
    public TableColumn countryName;
    @FXML
    public TableColumn seqNo;
    @FXML
    public TableView<CountryVo> tableView;

    private ObservableList<CountryVo> countryObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        togetherTableView();
        //设置多选
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void onAddCustomCountryDet(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/iTunes/custom-country-det-popup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 490, 600);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("新增国家");
        //模块化，对应用里的所有窗口起作用
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.showAndWait();
        togetherTableView();
    }

    private  void togetherTableView(){
        this.countryObservableList.clear();
        File userNationalDataFile = FileUtil.file("userNationalData.json");
        if(!userNationalDataFile.exists()){
            return;
        }
        // 创建json文件对象
        List<UserNationalModel> list=new ArrayList<>();
        File jsonFile = new File("userNationalData.json");
        String jsonString = FileUtil.readString(jsonFile,Charset.defaultCharset());
        if(!StringUtils.isEmpty(jsonString)){
            list = JSONUtil.toList(jsonString,UserNationalModel.class);
        }
        for(UserNationalModel userNationalModel:list){
            CountryVo country= new CountryVo();
            country.setId(userNationalModel.getId());
            country.setCountryName(userNationalModel.getName());
            country.setSeqNo(countryObservableList.size()+1);
            countryObservableList.add(country);
        }
        initTableView();
        tableView.setItems(countryObservableList);
    }

    private void initTableView(){
        seqNo.setCellValueFactory(new PropertyValueFactory<CountryVo,Integer>("seqNo"));
        countryName.setCellValueFactory(new PropertyValueFactory<CountryVo,String>("countryName"));
    }
    //删除选择的自定国家
    public void onDeleteCustomCountry(ActionEvent actionEvent) throws IOException {
        int length=tableView.getSelectionModel().getSelectedCells().size();
        if(length==0){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("提示");
            alert.setHeaderText("请选中需要删除的资料");
            alert.show();
        }else{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("提示");
            alert.setHeaderText("删除选中资料？");
            Optional<ButtonType> option = alert.showAndWait();
            if (option.get() == null) {
                return;
            } else if (option.get() == ButtonType.OK) {
                if(countryObservableList.size()==length){
                    File jsonFile = new File("userNationalData.json");
                    jsonFile.delete();
                    this.countryObservableList.clear();
                }else{
                    List<UserNationalModel> userNationalModels=new ArrayList<>();
                    File jsonFile = new File("userNationalData.json");
                    String jsonString = FileUtil.readString(jsonFile,Charset.defaultCharset());
                    if(!StringUtils.isEmpty(jsonString)){
                        userNationalModels = JSONUtil.toList(jsonString,UserNationalModel.class);
                    }
                    ObservableList<CountryVo> tempList=tableView.getSelectionModel().getSelectedItems();
                    List<CountryVo> list=new ArrayList();
                    list.addAll(tempList);
                    for(CountryVo country:list){
                        String sn=country.getId();
                        userNationalModels.removeIf(p -> p.getId().equals(sn));
                    }
                    FileWriter fw = new FileWriter("userNationalData.json", Charset.defaultCharset(),false);
                    fw.write(JSONUtil.toJsonStr(userNationalModels));
                    fw.flush();
                    fw.close();
                }
                togetherTableView();
            } else if (option.get() == ButtonType.CANCEL) {
                return;
            } else {
                return;
            }
        }
    }
}

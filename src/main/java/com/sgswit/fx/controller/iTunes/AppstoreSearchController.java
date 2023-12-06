package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.common.CommonView;
import com.sgswit.fx.controller.iTunes.vo.AppstoreItemVo;
import com.sgswit.fx.utils.ITunesUtil;
import com.sgswit.fx.utils.StoreFontsUtils;
import com.sgswit.fx.utils.StringUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AppstoreSearchController extends CommonView implements Initializable{

    @FXML
    ToggleGroup itemTypeToggleGroup;

    @FXML
    ChoiceBox countryChoiceBox;

    @FXML
    TextField keywordsTextField;

    @FXML
    ChoiceBox limitChoiceBox;

    @FXML
    CheckBox loadLogoCheckBox;

    @FXML
    public javafx.scene.control.TableView<AppstoreItemVo> tableView;

    protected ObservableList<AppstoreItemVo> appList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 表格列绑定
        ObservableList<TableColumn<AppstoreItemVo, ?>> columns = tableView.getColumns();
        for (TableColumn column : columns) {
            column.setCellValueFactory(new PropertyValueFactory(column.getId()));
            if ("select".equals(column.getId())){
                column.setCellFactory(CheckBoxTableCell.forTableColumn(column));
                column.setEditable(true);
                tableView.setEditable(true);
            }
        }

        // 国家
        List<String> countryList = StoreFontsUtils.getCountryList();
        countryChoiceBox.setValue("CN-中国大陆");
        countryChoiceBox.getItems().addAll(countryList);

        // 搜索限制条数
        limitChoiceBox.setValue("20");
    }



    public void searchBtnAction(){
        String keywords = keywordsTextField.getText();
        if (StringUtils.isEmpty(keywords)){
            alert("请输入关键字");
            return;
        }

        String country = countryChoiceBox.getValue().toString();
        Integer limit = Integer.valueOf(limitChoiceBox.getValue().toString());
        HttpResponse appstoreSearchRsp = ITunesUtil.appstoreSearch(country.split("-")[0], keywords, limit);
        if (appstoreSearchRsp.getStatus() != 200) {
            alert("搜索失败!");
            return;
        }

        String appstoreSearchBody = appstoreSearchRsp.body();
        JSONObject entries = JSONUtil.parseObj(appstoreSearchBody);
        JSONArray results = entries.getJSONArray("results");

        if (CollUtil.isEmpty(results)){
            alert("未查询到对应数据！");
            return;
        }

        Double size = 60.0;
        boolean isLoadLogo = loadLogoCheckBox.isSelected();
        tableView.getItems().clear();
        for (Object result : results) {
            JSONObject track = (JSONObject) result;
            Long trackId = track.getLong("trackId");
            String trackName = track.getStr("trackName");
            String artworkUrl100 = track.getStr("artworkUrl100");
            Double price = track.getDouble("price");
            AppstoreItemVo appstoreItemVo = new AppstoreItemVo();
            appstoreItemVo.setPrice(String.valueOf(price));
            appstoreItemVo.setTrackName(trackName);
            appstoreItemVo.setTrackId(trackId.toString());
            appstoreItemVo.setArtworkUrl100(artworkUrl100);

            if (isLoadLogo){
                ImageView imageView = new ImageView(new Image(artworkUrl100));
                imageView.setFitWidth(size);
                imageView.setFitHeight(size);
                Rectangle rectangle = new Rectangle(size,size);
                rectangle.setArcWidth(25);
                rectangle.setArcHeight(25);
                imageView.setClip(rectangle);
                appstoreItemVo.setIconImage(imageView);
            }

            tableView.getItems().add(appstoreItemVo);
        }
        for (int i = 0; i < tableView.getItems().size(); i++) {
            tableView.getItems().get(i).setSeq(i+1);
        }
    }

    public void selectAllClickAction(){
        updateSelectStatus(true);
    }
    public void unselectAllClickAction(){
        updateSelectStatus(false);
    }

    private void updateSelectStatus(boolean status){
        if (tableView.getItems().isEmpty()){
            return;
        }

        for (AppstoreItemVo appstoreItemVo : tableView.getItems()) {
            appstoreItemVo.setSelect(status);
        }
    }


}

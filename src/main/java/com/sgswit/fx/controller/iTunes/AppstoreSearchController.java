package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.common.CommRightContextMenuView;
import com.sgswit.fx.controller.common.CommonView;
import com.sgswit.fx.controller.iTunes.vo.AppstoreItemVo;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.ITunesUtil;
import com.sgswit.fx.utils.StageUtil;
import com.sgswit.fx.utils.StoreFontsUtils;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

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
    Label itemNumLabel;

    @FXML
    public TableView<AppstoreItemVo> tableView;

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


    /**
     * 搜索
     */
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
            String trackName = track.getStr("trackName");
            String artworkUrl100 = track.getStr("artworkUrl100");
            Double price = track.getDouble("price");
            AppstoreItemVo appstoreItemVo = new AppstoreItemVo();
            appstoreItemVo.setPrice(String.valueOf(price));
            appstoreItemVo.setTrackName(trackName);
            appstoreItemVo.setTrackJson(track);
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

    /**
     * 全选/取消全选
     */
    public void selectAllBtnAction(){
        updateSelectStatus(true);
    }
    public void unselectAllBtnAction(){
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

    /**
     * 复制选中URL
     */
    public void copySelectItemUrlBtnAction(){
        List<AppstoreItemVo> selectedItemList = getSelectedItem();
        if (selectedItemList.isEmpty()){
            alert("未选中数据");
            return;
        }
        String clipboardWords = "";
        for (AppstoreItemVo appstoreItemVo : selectedItemList) {
            String trackViewUrl = appstoreItemVo.getTrackJson().getStr("trackViewUrl");
            clipboardWords+=trackViewUrl.substring(0,trackViewUrl.indexOf("?")) + "\r\n";
        }
        ClipboardUtil.setStr(clipboardWords);
        alert("复制成功！");
    }

    /**
     * 将选中的URL地址添加到txt文件
     */
    public void selectItemUrlToTxtBtnAction(){
        List<AppstoreItemVo> selectedItemList = getSelectedItem();
        if (selectedItemList.isEmpty()){
            alert("未选中数据");
            return;
        }

        Stage stage = StageUtil.get(StageEnum.APPSTORE_SEARCH);
        AppstoreDownloadController appstoreDownload = (AppstoreDownloadController) stage.getUserData();
        boolean selected = appstoreDownload.useUrlCheckBox.isSelected();
        if (!selected){
            alert("请先返回选中URL模式");
            return;
        }

        String localUrl = appstoreDownload.localUrlTextField.getText();
        if (StrUtil.isEmpty(localUrl)){
            alert("未配置URL文件地址");
            return;
        }

        if (!FileUtil.exist(localUrl)){
            alert("没找到对应的文件");
            return;
        }

        String words = "";
        for (AppstoreItemVo appstoreItemVo : selectedItemList) {
            String trackViewUrl = appstoreItemVo.getTrackJson().getStr("trackViewUrl");
            words+=trackViewUrl.substring(0,trackViewUrl.indexOf("?")) + "\r\n";
        }

        FileUtil.appendUtf8String(words, new File(localUrl));
        appstoreDownload.urlModeHandler();
        alert("写入成功！");
    }

    /**
     * 将选中的项目添加到本地文件
     */
    public void selectItemToLocalFileBtnAction(){
        List<AppstoreItemVo> selectedItem = getSelectedItem();
        Stage stage = StageUtil.get(StageEnum.APPSTORE_SEARCH);
        AppstoreDownloadController appstoreDownloadController = (AppstoreDownloadController) stage.getUserData();
        appstoreDownloadController.selectItemToLocalFile(selectedItem);
        alert("保存至本地文件成功");
    }

    /**
     * 将选中的项目添加到缓存list总
     */
    public void selectItemToCacheListBtnAction(){
        List<AppstoreItemVo> selectedItem = getSelectedItem();
        Stage stage = StageUtil.get(StageEnum.APPSTORE_SEARCH);
        AppstoreDownloadController appstoreDownload = (AppstoreDownloadController) stage.getUserData();

        // 去重
        List<String> trackIdList = appstoreDownload.appDataList
                                                .stream()
                                                .map(appstoreItemVo -> appstoreItemVo.getTrackJson().getStr("trackId"))
                                                .collect(Collectors.toList());

        List<AppstoreItemVo> selectList = selectedItem
                .stream()
                .filter(appstoreItemVo -> !trackIdList.contains(appstoreItemVo.getTrackJson().getStr("trackId")))
                .collect(Collectors.toList());

        appstoreDownload.appDataList.addAll(selectList);
        itemNumLabel.setText("已添加 "+appstoreDownload.appDataList.size()+" 个项目");
        appstoreDownload.refreshAppNumLabel();
        alert("已添加"+selectList.size()+" 个项目");
    }

    /**
     * 清空已导入项目
     */
    public void clearCacheListBtnAction(){
        Stage stage = StageUtil.get(StageEnum.APPSTORE_SEARCH);
        AppstoreDownloadController appstoreDownload = (AppstoreDownloadController) stage.getUserData();
        appstoreDownload.clearCacheListBtnAction();
    }

    private List<AppstoreItemVo> getSelectedItem(){
        List<AppstoreItemVo> appstoreItemVoList = new ArrayList<>();
        for (AppstoreItemVo appstoreItemVo : tableView.getItems()) {
            if (appstoreItemVo.isSelect()){
                appstoreItemVoList.add(appstoreItemVo);
            }
        }
        return appstoreItemVoList;
    }



}

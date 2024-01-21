package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.constant.StoreFontsUtils;
import com.sgswit.fx.controller.common.ItunesView;
import com.sgswit.fx.controller.iTunes.vo.AppstoreDownloadVo;

import com.sgswit.fx.controller.iTunes.vo.AppstoreItemVo;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AppstoreDownloadController extends ItunesView<AppstoreDownloadVo> {

    @FXML
    CheckBox useUrlCheckBox;

    @FXML
    Button chooseFileButton;

    @FXML
    TextField localUrlTextField;

    @FXML
    Label appNumLabel;

    List<AppstoreItemVo> appDataList = new ArrayList<>();

    List<String> appData4UrlList = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.APPSTORE_DOWNLOAD.getCode())));
        super.initialize(url, resourceBundle);
        useUrlCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chooseFileButton.setDisable(false);
                urlModeHandler();
            } else {
                chooseFileButton.setDisable(true);
                refreshAppNumLabel();
            }
        });
    }

    public void urlModeHandler(){
        String filePath = localUrlTextField.getText();
        if (!StrUtil.isEmpty(filePath)){
            boolean isFile = FileUtil.isFile(filePath);
            if (isFile){
                appData4UrlList.clear();
                List<String> lines = FileUtil.readLines(filePath, Charset.defaultCharset())
                        .stream()
                        .filter(line -> !StrUtil.isEmpty(line.trim()))
                        .collect(Collectors.toList());
                if (lines != null && lines.size() > 0){
                    appData4UrlList.addAll(lines);
                }
            }
        }
        refreshAppNumLabel();
    }

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction() {
        openImportAccountView(List.of("account----pwd"));
    }

    @Override
    public boolean executeButtonActionBefore() {
        boolean verify = !useUrlCheckBox.isSelected() ? appDataList.size() > 0 : appData4UrlList.size() > 0;
        if (!verify){
            alert("请导入至少一个项目");
        }
        return verify;
    }

    /**
     * 账号处理 qewqeq@2980.com----dPFb6cSD41
     */
    @Override
    public void accountHandler(AppstoreDownloadVo appstoreDownloadVo) {
        // 鉴权
        itunesLogin(appstoreDownloadVo);

        String storeFront = appstoreDownloadVo.getStoreFront();
        String country = StoreFontsUtils.getCountryCode(StrUtil.split(storeFront, "-").get(0));
        appstoreDownloadVo.setArea(country);

        boolean isUrlMode = useUrlCheckBox.isSelected();

        Integer successNum = 0;
        Integer failNum    = 0;
        appstoreDownloadVo.setSuccessNum("0");
        appstoreDownloadVo.setFailNum("0");
        if (isUrlMode){
            appstoreDownloadVo.setItemNum(appData4UrlList.size()+"");
            for (String previewUrl : appData4UrlList) {
                previewUrl = previewUrl.trim();
                if (StrUtil.isEmpty(previewUrl)){
                    continue;
                }
                int i = previewUrl.indexOf("/id");
                if (i <= 0){
                    appstoreDownloadVo.setNote("URL格式不正确！");
                    Console.log("地址格式不正确！ previewUrl:{}",previewUrl);
                    continue;
                }
                String trackId = previewUrl.substring(previewUrl.indexOf("/id")+3);
                boolean success = purchaseAnddownloadApp(appstoreDownloadVo, appstoreDownloadVo.getGuid(), trackId, trackId);
                successNum +=  success ? 1:0;
                failNum    += !success ? 1:0;
                appstoreDownloadVo.setSuccessNum(successNum+"");
                appstoreDownloadVo.setFailNum(failNum+"");
            }
        } else {
            appstoreDownloadVo.setItemNum(appDataList.size()+"");
            for (AppstoreItemVo appstoreItemVo : appDataList) {
                String trackName = appstoreItemVo.getTrackName();
                String trackId = appstoreItemVo.getTrackJson().getStr("trackId");
                if (Double.valueOf(appstoreItemVo.getPrice()) > 0){
                    failNum+=1;
                    Console.log("暂只支持免费应用！[{}] 价格:{}", trackName,appstoreItemVo.getPrice());
                    continue;
                }
                boolean success = purchaseAnddownloadApp(appstoreDownloadVo, appstoreDownloadVo.getGuid(), trackId, trackName);
                successNum +=  success ? 1:0;
                failNum    += !success ? 1:0;
                appstoreDownloadVo.setSuccessNum(successNum+"");
                appstoreDownloadVo.setFailNum(failNum+"");
            }
        }
        setAndRefreshNote(appstoreDownloadVo,"下载完毕");
    }

    @Override
    protected void secondStepHandler(AppstoreDownloadVo account, String code) {
        account.setAuthCode(code);
        accountHandlerExpand(account);
    }

    public boolean purchaseAnddownloadApp(AppstoreDownloadVo appstoreDownloadVo,String guid,String trackId,String trackName){
        trackName = StrUtil.isEmpty(trackName) ? trackId : trackName;
        if (trackName.length() > 6){
            trackName = trackName.substring(0,6) + "..";
        }
        HttpResponse purchaseRsp = ITunesUtil.purchase(appstoreDownloadVo, trackId,"");
        String purchaseBody = purchaseRsp.body();
        if (!StrUtil.isEmpty(purchaseBody) && JSONUtil.isTypeJSON(purchaseBody)){
            JSONObject purchaseJSON = JSONUtil.parseObj(purchaseBody);
            String purchaseJdt = purchaseJSON.getStr("jingleDocType","");
            String purchaseStatus   = purchaseJSON.getStr("status","");
            if(!"purchaseSuccess".equals(purchaseJdt) || !"0".equals(purchaseStatus)){
                String failureType = purchaseJSON.getStr("failureType");
                String customerMessage = purchaseJSON.getStr("customerMessage");
                Console.log("[{}] 购买失败！ failureType:{}, customerMessage:{}",trackName,failureType,customerMessage);
                appstoreDownloadVo.setNote(String.format("[%s] 购买失败! failureType:%s, customerMessage:%s",trackName,failureType,customerMessage));
                return false;
            }
        }

        Console.log("[{}] 购买成功", trackName);
        appstoreDownloadVo.setNote(String.format("[%s] 购买成功",trackName));

        HttpResponse appstoreDownloadUrlRsp = ITunesUtil.appstoreDownloadUrl( appstoreDownloadVo, trackId.toString(), "");
        JSONObject appstoreDownloadUrlBody = PListUtil.parse(appstoreDownloadUrlRsp.body());
        String appstoreDownloadUrlJdt = appstoreDownloadUrlBody.getStr("jingleDocType","");
        String appstoreDownloadUrlStatus   = appstoreDownloadUrlBody.getStr("status","");
        if(!"purchaseSuccess".equals(appstoreDownloadUrlJdt) || !"0".equals(appstoreDownloadUrlStatus)){
            String failureType = appstoreDownloadUrlBody.getStr("failureType");
            String customerMessage = appstoreDownloadUrlBody.getStr("customerMessage");
            Console.log("[{}] 获取下载链接失败！ failureType:{}, customerMessage:{}",trackName,failureType,customerMessage);
            appstoreDownloadVo.setNote(String.format("[%s] 获取下载链接失败! failureType:%s, customerMessage:%s",trackName,failureType,customerMessage));
            return false;
        }

        JSONArray songList = appstoreDownloadUrlBody.getJSONArray("songList");
        if (songList.isEmpty()){
            Console.log("songList is empty");
            appstoreDownloadVo.setNote(String.format("[%s] 下载失败！songList is empty",trackName));
            return false;
        }

        JSONObject song = (JSONObject)songList.get(0);
        String url = song.getStr("URL");
        JSONObject metadata = song.getJSONObject("metadata");

        String filePath = Constant.LOCAL_FILE_STORAGE_PATH + "/下载/";
        String fileName = String.format("%s-%s-%s.ipa"
                ,metadata.getStr("softwareVersionBundleId")
                ,metadata.getStr("artistId")
                ,metadata.getStr("bundleShortVersionString"));

        Console.log("[{}] 获取下载链接成功; fileName:{}, url:{}",trackName,fileName,url);
        appstoreDownloadVo.setNote(String.format("[%s] 获取下载链接成功!",trackName));

        // 下载
        appstoreDownloadVo.setNote(String.format("[%s] 开始下载...",trackName));
        HttpUtil.downloadFile(url, new File(filePath + fileName));
        appstoreDownloadVo.setNote(String.format("[%s] 下载完成, 正在整合...",trackName));

        // zip
        try {
            Path zipPath = Paths.get(filePath + fileName);
            File tmpFile = new File("tmp.plist");
            FileUtil.appendUtf8String(metadata.toString(),tmpFile);
            ZipUtil.append(zipPath,tmpFile.toPath());
            FileUtil.del(tmpFile);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            FileUtil.del(new File("tmp.plist"));
        }
        return true;
    }

    /**
     * 从苹果商店添加
     */
    public void showAppstoreSearchStage(){
        StageUtil.show(StageEnum.APPSTORE_SEARCH,this);
    }

    /**
     * 从本地文件添加
     */
    public void addItemFromLocalBtnAction(){
        Stage stage = new Stage();

        List<String> fileNames = FileUtil.listFileNames(Constant.LOCAL_FILE_STORAGE_PATH)
                .stream()
                .filter(str -> StrUtil.endWith(str,Constant.LOCAL_FILE_EXTENSION))
                .map(str -> str.replaceAll(Constant.LOCAL_FILE_EXTENSION,"").trim())
                .collect(Collectors.toList());

        // 顶部区域
        HBox hBox1 = new HBox();
        hBox1.setSpacing(5);
        hBox1.setAlignment(Pos.CENTER_LEFT);
        Button selectBtn    = new Button("全选");
        Button unselectBtn  = new Button("全不选");
        Label  label1       = new Label("选择文件");
        ChoiceBox choiceBox = new ChoiceBox();
        choiceBox.getItems().addAll(fileNames);

        Label  widthLabel = new Label("");
        widthLabel.setPrefWidth(196);
        hBox1.getChildren().addAll(selectBtn,unselectBtn,widthLabel,label1,choiceBox);

        // 表格区域
        javafx.scene.control.TableView<AppstoreItemVo> tableView = new javafx.scene.control.TableView();
        tableView.setPrefHeight(500);
        tableView.setEditable(true);
        TableColumn col1 = new TableColumn<>("");
        col1.setPrefWidth(50);
        col1.setStyle("-fx-alignment: CENTER;");
        col1.setCellFactory(CheckBoxTableCell.forTableColumn(col1));
        col1.setCellValueFactory(new PropertyValueFactory("select"));
        col1.setEditable(true);

        TableColumn col2 = new TableColumn<>("序号");
        col2.setPrefWidth(60);
        col2.setStyle("-fx-alignment: CENTER;");
        col2.setCellFactory(new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                TableCell cell = new TableCell() {
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        this.setText(null);
                        this.setGraphic(null);
                        if (!empty) {
                            int rowIndex = this.getIndex() + 1;
                            this.setText(String.valueOf(rowIndex));
                        }
                    }
                };
                return  cell;
            }
        }); //通过这个类实现自动增长的序号

        TableColumn col3 = new TableColumn<>("APP名称");
        col3.setPrefWidth(450);
        col3.setCellValueFactory(new PropertyValueFactory<AppstoreItemVo,String>("trackName"));
        tableView.getColumns().addAll(col1,col2,col3);

        // 底部区域-label
        Label labe2 = new Label("如需删除某个项目请取消该项目选中状态。  请注意：本程序不兼容2.x版本到APP文件。");

        // 底部区域-按钮
        HBox hBox2 = new HBox();
        hBox2.setSpacing(5);
        hBox2.setAlignment(Pos.CENTER_LEFT);
        Button generNewFileBtn = new Button("将已选中的APP生成新的文件");
        Button clearItemBtn    = new Button("清空所有项目");
        Button addItemBtn      = new Button("添加项目");

        Label  widthLabel2 = new Label("");
        widthLabel2.setPrefWidth(226);
        hBox2.getChildren().addAll(generNewFileBtn,widthLabel2,clearItemBtn,addItemBtn);

        // 添加监听器
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String path = Constant.LOCAL_FILE_STORAGE_PATH + "/" + newValue + Constant.LOCAL_FILE_EXTENSION;
            boolean exist = FileUtil.exist(path);
            if (!exist){
                alert("请检查文件是否存在");
                return;
            }
            tableView.getItems().clear();
            String encrypt = FileUtil.readUtf8String(path);
            String jsonData = decrypt(encrypt);
            JSONArray jsonArray = JSONUtil.parseArray(jsonData);
            for (Object o : jsonArray) {
                JSONObject json = (JSONObject) o;
                AppstoreItemVo appstoreItemVo = new AppstoreItemVo();
                appstoreItemVo.setSelect(false);
                appstoreItemVo.setTrackName(json.getStr("trackName"));
                appstoreItemVo.setTrackJson(json.getJSONObject("trackJson"));
                tableView.getItems().add(appstoreItemVo);
            }

        });

        // 全选
        selectBtn.setOnAction(event -> {
            for (AppstoreItemVo item : tableView.getItems()) {
                item.setSelect(true);
            }
        });
        //全不选
        unselectBtn.setOnAction(event -> {
            for (AppstoreItemVo item : tableView.getItems()) {
                item.setSelect(false);
            }
        });
        // 生成文件
        generNewFileBtn.setOnAction(event -> {
            List<AppstoreItemVo> appstoreItemVoList = new ArrayList<>();
            for (AppstoreItemVo appstoreItemVo : tableView.getItems()) {
                if (appstoreItemVo.isSelect()){
                    appstoreItemVoList.add(appstoreItemVo);
                }
            }
            File file = selectItemToLocalFile(appstoreItemVoList);
            String fileName = file.getName().replaceAll(Constant.LOCAL_FILE_EXTENSION,"").trim();
            choiceBox.getItems().add(fileName);
        });

        // 清空所有项目
        clearItemBtn.setOnAction(event -> clearCacheListBtnAction());

        // 添加选中项目
        addItemBtn.setOnAction(event -> {
            List<AppstoreItemVo> appstoreItemVoList = new ArrayList<>();
            for (AppstoreItemVo appstoreItemVo : tableView.getItems()) {
                if (appstoreItemVo.isSelect()){
                    appstoreItemVoList.add(appstoreItemVo);
                }
            }

            // 去重
            List<String> trackIdList = appDataList
                    .stream()
                    .map(appstoreItemVo -> appstoreItemVo.getTrackJson().getStr("trackId"))
                    .collect(Collectors.toList());

            List<AppstoreItemVo> selectList = appstoreItemVoList
                    .stream()
                    .filter(appstoreItemVo -> !trackIdList.contains(appstoreItemVo.getTrackJson().getStr("trackId")))
                    .collect(Collectors.toList());

            appDataList.addAll(selectList);
            refreshAppNumLabel();
            alert("已添加"+selectList.size()+" 个项目");
        });

        VBox mainVbox = new VBox();
        mainVbox.setSpacing(5);
        mainVbox.setPadding(new Insets(5));
        mainVbox.getChildren().addAll(hBox1,tableView,labe2,hBox2);

        Group root = new Group(mainVbox);
        stage.setTitle("账号导入");
        stage.setScene(new Scene(root, 600, 600));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.initStyle(StageStyle.UTILITY);
        stage.showAndWait();
    }

    public void localUrlFileChooseBtnAction(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TXT", "*.txt")
        );
        File file = fileChooser.showOpenDialog(StageUtil.get(StageEnum.APPSTORE_DOWNLOAD));
        if (file != null){
            localUrlTextField.setText(file.getAbsolutePath());
            urlModeHandler();
        }
    }

    /**
     * 清空已导入项目
     */
    public void clearCacheListBtnAction(){
        this.appDataList.clear();
        this.appData4UrlList.clear();
        refreshAppNumLabel();
        alert("已清空导入项目");
    }

    public void refreshAppNumLabel(){
        boolean isUrlMode = useUrlCheckBox.isSelected();
        if (isUrlMode){
            appNumLabel.setText("已导入项目："+appData4UrlList.size());
        }else{
            appNumLabel.setText("已导入项目："+appDataList.size());
        }
    }

    /**
     * 将选中的项目添加到本地文件
     */
    public File selectItemToLocalFile(List<AppstoreItemVo> selectedItem){
        String fileName = "(%s) - %s个APP%s" + Constant.LOCAL_FILE_EXTENSION;
        boolean exist = FileUtil.exist(Constant.LOCAL_FILE_STORAGE_PATH +"/" + fileName);
        String now = DateUtil.format(new Date(), "MM月dd日HH时mm分");
        String random = exist ? RandomUtil.randomNumbers(4) : "";
        fileName = String.format(fileName,now,selectedItem.size(),random);

        File file = FileUtil.newFile(Constant.LOCAL_FILE_STORAGE_PATH + "/" + fileName);
        FileUtil.appendUtf8String(encrypt(JSONUtil.toJsonStr(selectedItem)),file);
        alert("保存至本地文件成功");
        return file;
    }

    /**
     * 加密
     * key：AES模式下，key必须为16位
     * iv：偏移量，ECB模式不需要，CBC模式下必须为16位
     */
    public String encrypt(String text){
//        String key = "1234567812345678";
//        String iv = "1234567812345678";
//        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, key.getBytes(), iv.getBytes());
//        return aes.encryptBase64(text);
        return text;
    }

    /**
     * 解密
     */
    public String decrypt(String encrypt){
//        String key = "1234567812345678";
//        String iv = "1234567812345678";
//        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, key.getBytes(), iv.getBytes());
//        return aes.decryptStr(encrypt);
        return encrypt;
    }

}

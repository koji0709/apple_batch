package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.javafaker.Address;
import com.github.javafaker.Faker;
import com.mifmif.common.regex.Generex;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.CommController;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.iTunes.bo.FieldModel;
import com.sgswit.fx.controller.iTunes.bo.UserNationalModel;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.BaseAreaInfo;
import com.sgswit.fx.model.CreditCard;
import com.sgswit.fx.model.KeyValuePair;
import com.sgswit.fx.utils.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;


/**
 　* iTunes账号修改
 * @param
　* @throws
　* @author DeZh
　* @date 2023/10/27 10:10
 */
public class CountryModifyController extends CustomTableView<Account> implements Initializable  {
    @FXML
    public TableColumn originalCountry;
    @FXML
    public TableColumn targetCountry;
    @FXML
    private TableView accountTableView;
    @FXML
    private TableColumn seq;
    @FXML
    private TableColumn account;
    @FXML
    private TableColumn pwd;
    @FXML
    private TableColumn note;

    @FXML
    private Button accountQueryBtn;

    @FXML
    private Button accountExportBtn;

    @FXML
    private ChoiceBox<KeyValuePair> countryBox;

    private List<KeyValuePair> countryList=new ArrayList<>();

    @FXML
    public ChoiceBox<KeyValuePair> customCountryBox;
    private List<KeyValuePair> customCountryList=new ArrayList<>();
    @FXML
    public HBox customCountrySelectId;

    private String fromType=null;


    private ObservableList<Account> list = FXCollections.observableArrayList();

    public CountryModifyController(){


    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        countryDataFun();
        customCountryDataFun();
        super.initialize(url,resourceBundle);
    }
    /**快捷国家资料下拉**/
    protected void countryDataFun(){
        for(BaseAreaInfo baseAreaInfo: DataUtil.getFastCountry()){
            countryList.add(new KeyValuePair(baseAreaInfo.getCode(),baseAreaInfo.getNameZh()));
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
                if(!t1.toString().equals("-1")){
                    customCountryBox.getSelectionModel().clearSelection();
                    fromType="1";
                }else{

                }
            }
        });
    }
    /**自定义国家信息下拉**/
    protected void customCountryDataFun(){
        customCountryBox.getItems().clear();
        customCountryList.clear();
        //判断是否显示 自定义国家下拉框
        List<UserNationalModel> list=new ArrayList<>();
        File jsonFile = new File("userNationalData.json");
        if(!jsonFile.exists()){
            customCountrySelectId.setVisible(false);
            return;
        }
        String jsonString = FileUtil.readString(jsonFile, Charset.defaultCharset());
        if(!StringUtils.isEmpty(jsonString)){
            list = JSONUtil.toList(jsonString,UserNationalModel.class);
        }
        if(list.size()>0){
            customCountrySelectId.setVisible(true);
            for(UserNationalModel baseAreaInfo: list){
                customCountryList.add(new KeyValuePair(baseAreaInfo.getId(),baseAreaInfo.getName()));
            }
            customCountryBox.getItems().addAll(customCountryList);
        }else{
            customCountrySelectId.setVisible(false);
        }
        customCountryBox.converterProperty().set(new StringConverter<KeyValuePair>() {
            @Override
            public String toString(KeyValuePair object) {
                return object.getValue();
            }
            @Override
            public KeyValuePair fromString(String string) {
                return null;
            }
        });
        customCountryBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                if(!t1.toString().equals("-1")){
                    countryBox.getSelectionModel().clearSelection();
                    fromType="2";
                }else{

                }
            }
        });
    }


    @FXML
    protected void onAccountInputBtnClick() throws IOException {
        super.openImportAccountView(List.of("account----pwd"));
    }

    @FXML
    protected void onAccountExportBtnClick() throws Exception{
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("友情提示");
        alert.setHeaderText("功能建设中，敬请期待");
        alert.show();
    }

    @FXML
    protected void onAccountClearBtnClick() throws Exception{
        this.list.clear();
        accountTableView.refresh();
    }

    @FXML
    protected void onAccountQueryBtnClick() throws Exception{
        list=accountTableView.getItems();
        if(list.size() < 1){
            return;
        }
        if(StringUtils.isEmpty(fromType)){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("提示");
            alert.setHeaderText("请设置要修改的国家！");
            alert.show();
            return;
        }
        for(Account account:list){
            if (!StrUtil.isEmptyIfStr(account.getNote())) {
                continue;
            }else{
                executeFun(account);
            }
        }
    }
    private void executeFun(Account account){
        new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    try {
                        accountQueryBtn.setText("正在查询");
                        accountQueryBtn.setTextFill(Paint.valueOf("#FF0000"));
                        accountQueryBtn.setDisable(true);
                        account.setNote("正在登录...");
                        String step= StringUtils.isEmpty(account.getStep())?"01":account.getStep();
                        Map<String,Object> res=account.getAuthData();
                        if("00".equals(step)){
                            String authCode=account.getAuthCode();
                            res= PurchaseBillUtil.iTunesAuth(authCode,res);
                        }else if("01".equals(step)){
                            res= PurchaseBillUtil.iTunesAuth(account.getAccount(),account.getPwd());
                        }else{
                            res=new HashMap<>();
                        }
                        if(Constant.TWO_FACTOR_AUTHENTICATION.equals(res.get("code"))) {
                            account.setNote(String.valueOf(res.get("msg")));
                            account.setAuthData(MapUtils.mapConvertToObservableMap(res));
                        }else if(!Constant.SUCCESS.equals(res.get("code"))){
                            account.setNote(String.valueOf(res.get("msg")));
                        }else{
                            account.setNote("登录成功，正在修改...");
                            accountTableView.refresh();
                            String body="",targetCountry="";
                            //自定义国家信息
                            if(fromType.equals("2")){
                                // 创建json文件对象
                                File jsonFile = new File("userNationalData.json");
                                String jsonString = FileUtil.readString(jsonFile,Charset.defaultCharset());
                                List<UserNationalModel> list = JSONUtil.toList(jsonString,UserNationalModel.class);
                                UserNationalModel u=list.stream().filter(e->e.getId().equals(customCountryBox.getSelectionModel().getSelectedItem().getKey())).collect(Collectors.toList()).get(0);
                                targetCountry=DataUtil.getInfoByCountryCode(u.getPayment().getBillingAddress().getCountryCode()).getNameZh();
                                Map<String,Object> bodyMap=new HashMap<>();
                                bodyMap.put("iso3CountryCode",u.getPayment().getBillingAddress().getCountryCode());
                                bodyMap.put("addressOfficialCountryCode",u.getPayment().getBillingAddress().getCountryCode());
                                bodyMap.put("agreedToTerms","1");
                                bodyMap.put("paymentMethodVersion","2.0");
                                bodyMap.put("needsTopUp",false);
                                bodyMap.put("paymentMethodType","None");
                                bodyMap.put("billingFirstName",u.getPayment().getOwnerName().getFirstName());
                                bodyMap.put("billingLastName",u.getPayment().getOwnerName().getLastName());
                                bodyMap.put("addressOfficialLineFirst",u.getPayment().getBillingAddress().getLine1());
                                bodyMap.put("addressOfficialLineSecond",u.getPayment().getBillingAddress().getLine2());
                                bodyMap.put("addressOfficialLineThird",u.getPayment().getBillingAddress().getLine3());
                                bodyMap.put("addressOfficialCity",u.getPayment().getBillingAddress().getCity());
                                bodyMap.put("addressOfficialPostalCode",u.getPayment().getBillingAddress().getPostalCode());
                                bodyMap.put("addressOfficialStateProvince",u.getPayment().getBillingAddress().getStateProvinceName());
                                bodyMap.put("phoneOfficeNumber",u.getPayment().getPhoneNumber().getNumber());
                                bodyMap.put("phoneOfficeAreaCode",u.getPayment().getPhoneNumber().getCountryCode());
                                bodyMap.put("addressOfficialStateProvince",u.getPayment().getBillingAddress().getStateProvinceName());
//                                bodyMap.put("addressOfficialStateProvince",u.getPayment().getBillingAddress().getSuburb());
                                body=MapUtil.join(bodyMap,"&","=",true);
                            }else{
                                //快捷国家信息
                                targetCountry=countryBox.getSelectionModel().getSelectedItem().getValue();
                                String countryCode=countryBox.getSelectionModel().getSelectedItem().getKey();
                                //生成填充数据
                                body=generateFillData(countryCode);
                            }
                            account.setTargetCountry(targetCountry);
                            accountTableView.refresh();

                            res.put("addressInfo",body);
                            Map<String,Object> editBillingInfoRes= ITunesUtil.editBillingInfo(res);
                            account.setNote(MapUtil.getStr(editBillingInfoRes,"message"));
                        }
                        accountTableView.refresh();
                    } catch (Exception e) {
                        account.setNote("操作失败，接口异常");
                        accountTableView.refresh();
                        accountQueryBtn.setDisable(false);
                        accountQueryBtn.setText("开始执行");
                        accountQueryBtn.setTextFill(Paint.valueOf("#238142"));
                        e.printStackTrace();
                    }
                }finally {
                    //JavaFX Application Thread会逐个阻塞的执行这些任务
                    Platform.runLater(new Task<Integer>() {
                        @Override
                        protected Integer call() {
                            accountQueryBtn.setDisable(false);
                            accountQueryBtn.setText("开始执行");
                            accountQueryBtn.setTextFill(Paint.valueOf("#238142"));
                            return 1;
                        }
                    });
                }
            }
        }).start();
    }
    /**重新执行**/
    @Override
    protected void reExecute(Account account){
        executeFun(account);
    }
    /**
     　* 双重验证
     * @param
     * @param account
     * @param authCode
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2023/12/23 18:10
     */
    @Override
    protected void twoFactorCodeExecute(Account account, String authCode){
        try{
            Map<String,Object> res=account.getAuthData();
            if(Constant.TWO_FACTOR_AUTHENTICATION.equals(MapUtils.getStr(res,"code"))){
                account.setAuthCode(authCode);
                account.setStep("00");
                executeFun(account);
            }else{
                alert("未下发双重验证码");
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    /**
     　*生成填充数据
     * @param
    　* @return java.lang.String
    　* @throws
    　* @author DeZh
    　* @date 2023/10/8 15:37
     */
    private static String generateFillData(String countryCode){
        String body="";
        try {
            if("USA".equals(countryCode)){
                body="iso3CountryCode=USA&addressOfficialCountryCode=USA&agreedToTerms=1&paymentMethodVersion=2.0&needsTopUp=false&paymentMethodType=None&billingFirstName=ZhuJu&billingLastName=Mao&addressOfficialLineFirst=ZuoLingZhen379Hao&addressOfficialLineSecond=19Chuang4DanYuan801Shi&addressOfficialCity=luobin&addressOfficialPostalCode=99775&phoneOfficeNumber=3562000&phoneOfficeAreaCode=410&addressOfficialStateProvince=AK";
            }else if("CHN".equals(countryCode)){
                body="iso3CountryCode=CHN&addressOfficialCountryCode=CHN&agreedToTerms=1&paymentMethodVersion=2.0&needsTopUp=false&paymentMethodType=None&billingFirstName=%e8%ae%ba%e8%bf%b0&billingLastName=%e7%89%b9&addressOfficialLineFirst=asfaga124&addressOfficialLineSecond=fasfa125&addressOfficialCity=%e5%b9%bf%e5%b7%9e&addressOfficialPostalCode=510000&phoneOfficeNumber=18377114211&addressOfficialStateProvince=%e5%b9%bf%e4%b8%9c";
            }else if("CAN".equals(countryCode)){
                body="iso3CountryCode=CAN&addressOfficialCountryCode=CAN&agreedToTerms=1&paymentMethodVersion=2.0&needsTopUp=false&paymentMethodType=None&billingFirstName=NnpMW6d&billingLastName=Z1sIxex&addressOfficialLineFirst=hfghfg1&addressOfficialLineSecond=terter2&addressOfficialCity=terter2&addressOfficialPostalCode=T9X+1Z4&phoneOfficeNumber=4488258&phoneOfficeAreaCode=403&addressOfficialStateProvince=AB";
            }else if("AUS".equals(countryCode)){
                body="iso3CountryCode=AUS&addressOfficialCountryCode=AUS&agreedToTerms=1&paymentMethodVersion=2.0&needsTopUp=false&paymentMethodType=None&billingFirstName=r0QjeVm&billingLastName=SgUWhG4&addressOfficialLineFirst=dsad1&addressOfficialLineSecond=fdsfds1&addressOfficialCity=fdsfds1&addressOfficialPostalCode=7009&phoneOfficeNumber=40517322&phoneOfficeAreaCode=61&addressOfficialStateProvince=Tasmania";
            }else if("JPN".equals(countryCode)){
                body="iso3CountryCode=JPN&addressOfficialCountryCode=JPN&agreedToTerms=1&paymentMethodVersion=2.0&needsTopUp=false&paymentMethodType=None&billingFirstName=cikej&billingLastName=kxzfh&addressOfficialLineFirst=JiuJiQiFuFen&addressOfficialLineSecond=&addressOfficialCity=KeChuHuiJiaFei&addressOfficialPostalCode=786-7875&phoneOfficeNumber=78757862&phoneOfficeAreaCode=3951&addressOfficialStateProvince=%e7%be%a4%e9%a6%ac%e7%9c%8c&phoneticBillingLastName=xingpingying&phoneticBillingFirstName=mingpingyin";
            }else if("GBR".equals(countryCode)){
                body="iso3CountryCode=GBR&addressOfficialCountryCode=GBR&agreedToTerms=1&paymentMethodVersion=2.0&needsTopUp=false&paymentMethodType=None&billingFirstName=ShiMei&billingLastName=Shao&addressOfficialLineFirst=YinZuZhen239Hao&addressOfficialLineSecond=42Chuang4DanYuan502Shi&addressOfficialCity=YunChengShi&addressOfficialPostalCode=YI3+3PR&phoneOfficeNumber=72333032&phoneOfficeAreaCode=44";
            }else if("DEU".equals(countryCode)){
                body="iso3CountryCode=DEU&addressOfficialCountryCode=DEU&agreedToTerms=1&paymentMethodVersion=2.0&needsTopUp=false&paymentMethodType=None&billingFirstName=YaoTao&billingLastName=Li&addressOfficialLineFirst=PanKouXiang294Hao&addressOfficialLineSecond=27Chuang4DanYuan903Shi&addressOfficialCity=luobin&addressOfficialPostalCode=83141&phoneOfficeNumber=83141954&phoneOfficeAreaCode=2427";
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return body;
    }

    @FXML
    protected void onAreaQueryLogBtnClick() throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/account-querylog-popup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 950, 600);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("账户查询记录");
        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    private void initAccountTableView(){
        seq.setCellValueFactory(new PropertyValueFactory<Account,Integer>("seq"));
        account.setCellValueFactory(new PropertyValueFactory<Account,String>("account"));
        pwd.setCellValueFactory(new PropertyValueFactory<Account,String>("pwd"));
        originalCountry.setCellValueFactory(new PropertyValueFactory<Account,String>("originalCountry"));
        targetCountry.setCellValueFactory(new PropertyValueFactory<Account,String>("targetCountry"));
        note.setCellValueFactory(new PropertyValueFactory<Account,String>("note"));
    }
    public void onAddCountryBtnClick(MouseEvent mouseEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/iTunes/custom-country-popup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 450, 390);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        Stage popupStage = new Stage();
        popupStage.setTitle("新增国家");
        //模块化，对应用里的所有窗口起作用
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.showAndWait();
        customCountryDataFun();
    }
    @FXML
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> items=new ArrayList<>(){{
            add(Constant.RightContextMenu.DELETE.getCode());
            add(Constant.RightContextMenu.REEXECUTE.getCode());
            add(Constant.RightContextMenu.COPY.getCode());
            add(Constant.RightContextMenu.TWO_FACTOR_CODE.getCode());
        }};
        List<String> fields=new ArrayList<>(){{
            add("account");
            add("pwd");
            add("originalCountry");
            add("targetCountry");
            add("note");
        }};
        super.onContentMenuClick(contextMenuEvent,accountTableView,items,fields);
    }
    @FXML
    public void onStopBtnClick(ActionEvent actionEvent) {
    }
}

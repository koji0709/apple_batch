package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
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
import com.sgswit.fx.controller.CommController;
import com.sgswit.fx.controller.iTunes.bo.FieldModel;
import com.sgswit.fx.controller.iTunes.bo.UserNationalModel;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.BaseAreaInfo;
import com.sgswit.fx.model.KeyValuePair;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.DataUtil;
import com.sgswit.fx.utils.StringUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

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
public class CountryModifyController extends CommController<Account> implements Initializable  {
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
    private TableColumn answer1;
    @FXML
    private TableColumn answer2;
    @FXML
    private TableColumn answer3;

    @FXML
    private Button accoutQueryBtn;

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
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/iTunes/account-input-popup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 450);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        Stage popupStage = new Stage();

        popupStage.setTitle("账户导入");

        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.showAndWait();
        AccountInputPopupController c = fxmlLoader.getController();
        if(null == c.getAccounts() || "".equals(c.getAccounts())){
            return;
        }
        String[] lineArray = c.getAccounts().split("\n");
        for(String item : lineArray){
            String[] its = item.split("----");
            Account account = new Account();
            account.setSeq(list.size()+1);
            account.setAccount(its[0]);

            String[] pas = its[1].split("-");
            if(pas.length == 4){
                account.setPwd(pas[0]);
                account.setAnswer1(pas[1]);
                account.setAnswer2(pas[2]);
                account.setAnswer3(pas[3]);
            }else{
                account.setPwd(its[1]);
            }
            list.add(account);
        }
        initAccountTableView();
        accountTableView.setEditable(true);
        accountTableView.setItems(list);
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
    protected void onAccoutQueryBtnClick() throws Exception{

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
        super.onAccoutQueryBtnClick(accoutQueryBtn,accountTableView,list);
    }




    @Override
    protected void queryOrUpdate(Account account, HttpResponse step1Res) {
        try {
            account.setNote("正在修改");
            accountTableView.refresh();
            //step3 token
            HttpResponse step3Res = AppleIDUtil.token(step1Res);

            //step4 manager
            if(step3Res.getStatus() != 200){
                queryFail(account);
            }
            HashMap<String, List<String>> headers = new HashMap<>();

            headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
            headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
            headers.put("Content-Type", ListUtil.toList("application/json"));

            headers.put("Host", ListUtil.toList("appleid.apple.com"));
            headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));

            headers.put("User-Agent",ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));

            headers.put("X-Apple-ID-Session-Id",ListUtil.toList(step3Res.header("X-Apple-ID-Session-Id")));
            headers.put("scnt",ListUtil.toList(step3Res.header("scnt")));

            StringBuilder cookieBuilder = new StringBuilder();
            List<String> resCookies = step3Res.headerList("Set-Cookie");
            for(String item : resCookies){
                cookieBuilder.append(";").append(item);
            }
            String body="",targetCountry="";
            //自定义国家信息
            if(fromType.equals("2")){
                // 创建json文件对象
                File jsonFile = new File("userNationalData.json");
                String jsonString = FileUtil.readString(jsonFile,Charset.defaultCharset());
                List<UserNationalModel> list = JSONUtil.toList(jsonString,UserNationalModel.class);
                UserNationalModel u=list.stream().filter(e->e.getId().equals(customCountryBox.getSelectionModel().getSelectedItem().getKey())).collect(Collectors.toList()).get(0);
                body=JSONUtil.toJsonStr(u.getPayment());
                targetCountry=DataUtil.getInfoByCountryCode(u.getPayment().getBillingAddress().getCountryCode()).getNameZh();
            }else{
                //快捷国家信息
                targetCountry=countryBox.getSelectionModel().getSelectedItem().getValue();
                String countryCode=countryBox.getSelectionModel().getSelectedItem().getKey();
                //生成填充数据
                body=generateFillData(countryCode);
            }
            HttpResponse step4Res = AppleIDUtil.account(step3Res);
            String managerBody = step4Res.body();
            JSON manager = JSONUtil.parse(managerBody);
            String area = (String) manager.getByPath("account.person.primaryAddress.countryName");
            account.setOriginalCountry(area);
            accountTableView.refresh();

            step4Res = HttpUtil.createRequest(Method.PUT,"https://appleid.apple.com/account/manage/payment/method/none/1")
                    .header(headers)
                    .body(body)
                    .cookie(cookieBuilder.toString())
                    .execute();
            if(step4Res.getStatus() == 400){
                String message="";
                JSONObject response= JSONUtil.parseObj(step4Res.body());
                String messageJsonStr="";
                if(!StringUtils.isEmpty(response.getStr("service_errors"))){
                    messageJsonStr=response.getStr("service_errors");
                }else{
                    messageJsonStr=response.getStr("validationErrors");
                }
                JSONArray service_errors= JSONUtil.parseArray(messageJsonStr);
                for(Object jsonObject:service_errors){
                    message+= JSONUtil.parseObj(jsonObject).getStr("message");
                }
                messageFun(account,"修改失败,"+message);
            }else if(step4Res.getStatus() != 200){
                messageFun(account,"修改失败");
            }else{
                messageFun(account,"修改成功");
            }
            account.setTargetCountry(targetCountry);
            accountTableView.refresh();
        }catch (Exception e){
            messageFun(account,"修改失败");
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
            JSON json=JSONUtil.createObj();
            json.putByPath("billingAddress.countryCode",countryCode);
            json.putByPath("phoneNumber.countryCode",DataUtil.getInfoByCountryCode(countryCode).getDialCode());
            String addressFormatListStr=DataUtil.getAddressFormat(countryCode);
            List<FieldModel> addressFormatList=JSONUtil.parseObj(addressFormatListStr).getBeanList("addressFormatList", FieldModel.class);
            Faker faker = new Faker(new Locale("en-"+countryCode));
            Address address  =faker.address();
            for(FieldModel fieldModel:addressFormatList){
                String fieldId=fieldModel.getId();
                if(fieldId.equals("firstName")){
                    json.putByPath("ownerName.firstName",faker.name().firstName());
                }else if(fieldId.equals("lastName")){
                    json.putByPath("ownerName.lastName",faker.name().lastName());
                }else if(fieldId.equals("line1")){
                    json.putByPath("billingAddress.line1",address.streetAddress());
                }else if(fieldId.equals("line2")){
                    json.putByPath("billingAddress.line2",address.buildingNumber());
                }else if(fieldId.equals("line3")){
                    json.putByPath("billingAddress.line3",address.streetName());
                }else if(fieldId.equals("city")){
                    json.putByPath("billingAddress.city",address.cityName());
                }else if(fieldId.equals("suburb")){
                    json.putByPath("billingAddress.suburb",address.streetAddress());
                }else if(fieldId.equals("county")){
                    json.putByPath("billingAddress.county",address.citySuffix());
                }else if(fieldId.equals("stateProvince")){
                    int size=fieldModel.getValues().size();
                    if(fieldModel.getValues().size()>0){
                        int r= (int) (Math.random()*(size-1));
                        json.putByPath("billingAddress.stateProvinceName",fieldModel.getValues().get(r).get("name"));
                    }else{
                        json.putByPath("billingAddress.stateProvinceName","");
                    }
                }else if(fieldId.equals("postalCode")){
//                    json.putByPath("billingAddress.postalCode",address.zipCode());
                    String regExp="[012345789]{6}";
                    Generex generex = new Generex(regExp);
                    json.putByPath("billingAddress.postalCode",generex.random());
                }else if(fieldId.equals("phoneNumber")){
//                    json.putByPath("phoneNumber.number",faker.phoneNumber().cellPhone());
                    String regExp="1[345789]\\d{9}";
                    Generex generex = new Generex(regExp);
                    json.putByPath("phoneNumber.number",generex.random());
                }else if(fieldId.equals("phoneAreaCode")){
//                    json.putByPath("phoneNumber.areaCode",faker.phoneNumber().subscriberNumber());
                    json.putByPath("phoneNumber.areaCode","");
                }
            }
            body= JSONUtil.toJsonStr(json);
        }catch (Exception e){
            e.printStackTrace();
        }
        return body;
    }

    private void queryFail(Account account) {
        String note = "查询失败，请确认用户名密码是否正确";
        account.setNote(note);
        accountTableView.refresh();
    }
    private void messageFun(Account account,String message) {
        account.setNote(message);
        accountTableView.refresh();
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
        answer1.setCellValueFactory(new PropertyValueFactory<Account,String>("answer1"));
        answer2.setCellValueFactory(new PropertyValueFactory<Account,String>("answer2"));
        answer3.setCellValueFactory(new PropertyValueFactory<Account,String>("answer3"));
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

}

package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.github.javafaker.Faker;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.iTunes.bo.UserNationalModel;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.BaseAreaInfo;
import com.sgswit.fx.utils.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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
public class CountryModifyController extends CustomTableView<Account>{
    @FXML
    private ComboBox<Map<String,String>> countryBox;
    @FXML
    public ComboBox<Map<String,String>> customCountryBox;

    @FXML
    public HBox customCountrySelectId;

    private String fromType=null;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.COUNTRY_MODIFY.getCode())));
        countryDataFun();
        customCountryDataFun();
        super.initialize(url,resourceBundle);
    }
    /**快捷国家资料下拉**/
    protected void countryDataFun(){
        for(BaseAreaInfo baseAreaInfo: DataUtil.getFastCountry()){
            countryBox.getItems().add(new HashMap<>(){{
                put("name",baseAreaInfo.getNameZh());
                put("code",baseAreaInfo.getCode());
            }});
        }
        countryBox.converterProperty().set(new StringConverter<>() {
            @Override
            public String toString(Map<String, String> map) {
                if(null==map){
                    return "";
                }
                return map.get("name");
            }
            @Override
            public Map<String, String> fromString(String string) {
                return null;
            }
        });
        countryBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                if(!"-1".equals(t1.toString())){
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
                customCountryBox.getItems().add(new HashMap<>(){{
                    put("name",baseAreaInfo.getName());
                    put("code",baseAreaInfo.getId());
                }});
            }
        }else{
            customCountrySelectId.setVisible(false);
        }
        customCountryBox.converterProperty().set(new StringConverter<>() {
            @Override
            public String toString(Map<String,String> map) {
                if(null==map){
                    return "";
                }
                return map.get("name");
            }
            @Override
            public Map<String,String> fromString(String string) {
                return null;
            }
        });
        customCountryBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                if(!"-1".equals(t1.toString())){
                    countryBox.getSelectionModel().clearSelection();
                    fromType="2";
                }else{

                }
            }
        });
    }


    @FXML
    protected void onAccountInputBtnClick(ActionEvent actionEvent) throws IOException {
        super.openImportAccountView(List.of("account----pwd"),actionEvent);
    }

    @Override
    public boolean executeButtonActionBefore() {
        if(StringUtils.isEmpty(fromType)){
            alert("请设置要修改的国家!");
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void accountHandler(Account account){
        try {
            account.setHasFinished(false);
            setAndRefreshNote(account,"正在登录...");
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
            Boolean isDisabledAccount = MapUtil.getBool(res, "isDisabledAccount", Boolean.FALSE);
            if (isDisabledAccount){
                throw new ServiceException("账户已被单禁, 不支持修改国家。");
            }
            account.setOriginalCountry(MapUtil.getStr(res,"countryName"));
            if(Constant.TWO_FACTOR_AUTHENTICATION.equals(res.get("code"))) {
                account.setNote(String.valueOf(res.get("msg")));
                account.setAuthData(res);
                throw new ServiceException(String.valueOf(res.get("msg")));
            }else if(!Constant.SUCCESS.equals(res.get("code"))){
                account.setNote(String.valueOf(res.get("msg")));
                account.setDataStatus("0");
                throw new ServiceException(String.valueOf(res.get("msg")));
            }else{
                setAndRefreshNote(account,"登录成功，正在修改...");
                String body="",targetCountry="";
                //自定义国家信息
                if("2".equals(fromType)){
                    // 创建json文件对象
                    File jsonFile = new File("userNationalData.json");
                    String jsonString = FileUtil.readString(jsonFile,Charset.defaultCharset());
                    List<UserNationalModel> list = JSONUtil.toList(jsonString,UserNationalModel.class);
                    UserNationalModel u=list.stream().filter(e->e.getId().equals(customCountryBox.getSelectionModel().getSelectedItem().get("code"))).collect(Collectors.toList()).get(0);
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
                    bodyMap.put("phoneOfficeAreaCode",u.getPayment().getPhoneNumber().getAreaCode());
                    body=MapUtil.join(bodyMap,"&","=",true);
                }else{
                    //快捷国家信息
                    targetCountry=countryBox.getSelectionModel().getSelectedItem().get("name");
                    String countryCode=countryBox.getSelectionModel().getSelectedItem().get("code");
                    //生成填充数据
                    body=generateFillData(countryCode);
                }
                account.setTargetCountry(targetCountry);
                accountTableView.refresh();

                res.put("addressInfo",body);
                Map<String,Object> editBillingInfoRes= ITunesUtil.editBillingInfo(res);
                account.setNote(MapUtil.getStr(editBillingInfoRes,"message"));
                if(Constant.SUCCESS.equals(MapUtil.getStr(editBillingInfoRes,"code"))){
                    account.setDataStatus("1");
                    //修改成功之后，清除iTunes登录缓存信息
                    String id=super.createId(account.getAccount(),account.getPwd());
                    loginSuccessMap.remove(id);
                }else{
                    LoggerManger.info(String.format("修改国家失败, code = %s, Request.body = ",MapUtil.getStr(editBillingInfoRes,"code"),body));
                    account.setDataStatus("0");
                    throw new ServiceException(account.getNote());
                }
            }
            super.accountTableView.refresh();
        }catch (Exception e){
            throw e;
        }
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
            if(Constant.TWO_FACTOR_AUTHENTICATION.equals(MapUtil.getStr(res,"code"))){
                account.setAuthCode(authCode);
                account.setStep("00");
                accountHandlerExpand(account);
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
            Map<String,String> bodyMap=  new HashMap<>();
            bodyMap.put("iso3CountryCode",countryCode);
            bodyMap.put("addressOfficialCountryCode",countryCode);
            bodyMap.put("agreedToTerms","1");
            bodyMap.put("paymentMethodVersion","2.0");
            bodyMap.put("needsTopUp","false");
            bodyMap.put("paymentMethodType","None");
            Faker faker ;
            if("CHN".equals(countryCode)){
                faker = new Faker(Locale.CHINA);
            }else if("USA".equals(countryCode)){
                faker = new Faker(Locale.US);
            }else if("CAN".equals(countryCode)){
                faker = new Faker(Locale.CANADA);
            }else if("JPN".equals(countryCode)){
                faker = new Faker(Locale.JAPAN);
                Faker faker2 = new Faker();
                bodyMap.put("phoneticBillingFirstName",faker2.name().firstName());
                bodyMap.put("phoneticBillingLastName",faker2.name().lastName());
            }else if("GBR".equals(countryCode)){
                faker = new Faker(Locale.ENGLISH);
            }else if("DEU".equals(countryCode)){
                faker = new Faker(Locale.GERMANY);
            }else{
                faker = new Faker();
            }
            bodyMap.put("billingLastName",faker.name().lastName());
            bodyMap.put("billingFirstName",faker.name().firstName());
            Map<String,String> resMap=DataUtil.getAddressInfo(countryCode);
            // 合并map1和map2
            bodyMap.putAll(resMap);
            body=MapUtil.join(bodyMap,"&","=",true);

        }catch (Exception e){
            e.printStackTrace();
        }
        return body;
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
        List<String> items=new ArrayList<>(super.menuItem) ;
        items.add(Constant.RightContextMenu.TWO_FACTOR_CODE.getCode());
        super.onContentMenuClick(contextMenuEvent,accountTableView,items);
    }
}

package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
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
import com.sgswit.fx.controller.iTunes.bo.FieldModel;
import com.sgswit.fx.controller.iTunes.bo.UserNationalModel;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.ConsumptionBill;
import com.sgswit.fx.model.GiftCard;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.DataUtil;
import com.sgswit.fx.utils.StringUtils;
import com.sgswit.fx.utils.UrlParasUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author DeZh
 * @title: ConsumptionBillController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/219:01
 */
public class ConsumptionBillController {
    @FXML
    public TableColumn seq;
    @FXML
    public TableColumn account;
    @FXML
    public TableColumn notes;
    @FXML
    public TableColumn area;
    @FXML
    public TableColumn status;
    @FXML
    public TableColumn lastPurchaseDate;
    @FXML
    public TableColumn earliestPurchaseDate;
    @FXML
    public TableColumn totalConsumption;
    @FXML
    public TableColumn totalRefundAmount;
    @FXML
    public TableColumn purchaseRecord;
    @FXML
    public TableColumn paymentInformation;
    @FXML
    public TableColumn shippingAddress;
    @FXML
    public TableColumn pwd;
    @FXML
    public TableView accountTableView;
    @FXML
    public TableColumn accountBalance;

    private ObservableList<ConsumptionBill> list = FXCollections.observableArrayList();

    public ConsumptionBillController(){
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
        GiftCardInputPopupController c = fxmlLoader.getController();
        if(null == c.getData() || "".equals(c.getData())){
            return;
        }
        String[] lineArray = c.getData().split("\n");
        for(String item : lineArray){
            ConsumptionBill consumptionBill = new ConsumptionBill();
            String[] its = item.split("----");
            consumptionBill.setSeq(list.size()+1);
            consumptionBill.setAccount(its[0]);
            consumptionBill.setPwd(its[1]);
            list.add(consumptionBill);
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
    protected void onAccountQueryBtnClick() throws Exception{

        if(list.size() < 1){
            return;
        }
//        if(StringUtils.isEmpty(fromType)){
//            Alert alert = new Alert(Alert.AlertType.INFORMATION);
//            alert.setTitle("提示");
//            alert.setHeaderText("请设置要修改的国家！");
//            alert.show();
//            return;
//        }
//        super.onAccountQueryBtnClick(accoutQueryBtn,accountTableView,list);
    }




//    @Override
//    protected void queryOrUpdate(Account account, HttpResponse step1Res) {
//        try {
//            account.setNote("正在修改");
//            accountTableView.refresh();
//            //step3 token
//            HttpResponse step3Res = AppleIDUtil.token(step1Res);
//
//            //step4 manager
//            if(step3Res.getStatus() != 200){
//                queryFail(account);
//            }
//            HashMap<String, List<String>> headers = new HashMap<>();
//
//            headers.put("Accept", ListUtil.toList("application/json, text/plain, */*"));
//            headers.put("Accept-Encoding",ListUtil.toList("gzip, deflate, br"));
//            headers.put("Content-Type", ListUtil.toList("application/json"));
//
//            headers.put("Host", ListUtil.toList("appleid.apple.com"));
//            headers.put("Referer", ListUtil.toList("https://appleid.apple.com/"));
//
//            headers.put("User-Agent",ListUtil.toList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/114.0"));
//
//            headers.put("X-Apple-ID-Session-Id",ListUtil.toList(step3Res.header("X-Apple-ID-Session-Id")));
//            headers.put("scnt",ListUtil.toList(step3Res.header("scnt")));
//
//            StringBuilder cookieBuilder = new StringBuilder();
//            List<String> resCookies = step3Res.headerList("Set-Cookie");
//            for(String item : resCookies){
//                cookieBuilder.append(";").append(item);
//            }
//            String body="",targetCountry="";
//            //自定义国家信息
//            if(fromType.equals("2")){
//                // 创建json文件对象
//                File jsonFile = new File("userNationalData.json");
//                String jsonString = FileUtil.readString(jsonFile, Charset.defaultCharset());
//                List<UserNationalModel> list = JSONUtil.toList(jsonString,UserNationalModel.class);
//                UserNationalModel u=list.stream().filter(e->e.getId().equals(customCountryBox.getSelectionModel().getSelectedItem().getKey())).collect(Collectors.toList()).get(0);
//                body=JSONUtil.toJsonStr(u.getPayment());
//                targetCountry= DataUtil.getInfoByCountryCode(u.getPayment().getBillingAddress().getCountryCode()).getNameZh();
//            }else{
//                //快捷国家信息
//                targetCountry=countryBox.getSelectionModel().getSelectedItem().getValue();
//                String countryCode=countryBox.getSelectionModel().getSelectedItem().getKey();
//                //生成填充数据
//                body=generateFillData(countryCode);
//            }
//            HttpResponse step4Res = AppleIDUtil.account(step3Res);
//            String managerBody = step4Res.body();
//            JSON manager = JSONUtil.parse(managerBody);
//            String area = (String) manager.getByPath("account.person.primaryAddress.countryName");
//            account.setOriginalCountry(area);
//            accountTableView.refresh();
//
//            step4Res = HttpUtil.createRequest(Method.PUT,"https://appleid.apple.com/account/manage/payment/method/none/1")
//                    .header(headers)
//                    .body(body)
//                    .cookie(cookieBuilder.toString())
//                    .execute();
//            if(step4Res.getStatus() == 400){
//                String message="";
//                JSONObject response= JSONUtil.parseObj(step4Res.body());
//                String messageJsonStr="";
//                if(!StringUtils.isEmpty(response.getStr("service_errors"))){
//                    messageJsonStr=response.getStr("service_errors");
//                }else{
//                    messageJsonStr=response.getStr("validationErrors");
//                }
//                JSONArray service_errors= JSONUtil.parseArray(messageJsonStr);
//                for(Object jsonObject:service_errors){
//                    message+= JSONUtil.parseObj(jsonObject).getStr("message");
//                }
//                messageFun(account,"修改失败,"+message);
//            }else if(step4Res.getStatus() != 200){
//                messageFun(account,"修改失败");
//            }else{
//                messageFun(account,"修改成功");
//            }
//            account.setTargetCountry(targetCountry);
//            accountTableView.refresh();
//        }catch (Exception e){
//            messageFun(account,"修改失败");
//        }
//    }

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
        area.setCellValueFactory(new PropertyValueFactory<Account,String>("area"));
        accountBalance.setCellValueFactory(new PropertyValueFactory<Account,String>("accountBalance"));
        status.setCellValueFactory(new PropertyValueFactory<Account,String>("status"));
        lastPurchaseDate.setCellValueFactory(new PropertyValueFactory<Account,String>("lastPurchaseDate"));
        earliestPurchaseDate.setCellValueFactory(new PropertyValueFactory<Account,String>("earliestPurchaseDate"));
        totalConsumption.setCellValueFactory(new PropertyValueFactory<Account,String>("totalConsumption"));
        totalRefundAmount.setCellValueFactory(new PropertyValueFactory<Account,String>("totalRefundAmount"));
        purchaseRecord.setCellValueFactory(new PropertyValueFactory<Account,String>("purchaseRecord"));
        paymentInformation.setCellValueFactory(new PropertyValueFactory<Account,String>("paymentInformation"));
        shippingAddress.setCellValueFactory(new PropertyValueFactory<Account,String>("shippingAddress"));
    }

    public void onStopBtnClick(ActionEvent actionEvent) {

    }
}

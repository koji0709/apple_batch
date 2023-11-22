package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.controller.CommController;
import com.sgswit.fx.controller.iTunes.bo.BillingAddress;
import com.sgswit.fx.controller.iTunes.bo.OwnerName;
import com.sgswit.fx.controller.iTunes.bo.PaymentModel;
import com.sgswit.fx.controller.iTunes.bo.PhoneNumber;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.StringUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;


/**
　* iTunes付款方式管理
  * @param
　* @throws
　* @author DeZh
　* @date 2023/10/27 10:10
 */
public class PaymentMethodController extends CommController<Account> implements Initializable  {
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

    private ObservableList<Account> list = FXCollections.observableArrayList();

    public PaymentMethodController(){


    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

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
            //查询付款方式信息
            HttpResponse paymentRes = HttpUtil.createRequest(Method.GET,"https://appleid.apple.com/account/manage/payment")
                    .header(headers)
                    .cookie(cookieBuilder.toString())
                    .execute();
            if(paymentRes.getStatus()!=200){
                messageFun(account,"修改失败");
            }
            String bodyString=paymentRes.body();
            Object id=JSONUtil.parse(bodyString).getByPath("primaryPaymentMethod.id");
            Object phoneNumberObj=JSONUtil.parse(bodyString).getByPath("primaryPaymentMethod.phoneNumber");
            PhoneNumber phoneNumber=new PhoneNumber();
            if(null!=phoneNumber){
                phoneNumber=JSONUtil.toBean(phoneNumberObj.toString(),PhoneNumber.class);
            }
            Object ownerNameObj=JSONUtil.parse(bodyString).getByPath("primaryPaymentMethod.ownerName");
            OwnerName ownerName=new OwnerName();
            if(null!=ownerName){
                ownerName=JSONUtil.toBean(ownerNameObj.toString(),OwnerName.class);
            }
            JSONObject billingAddressJson= (JSONObject) JSONUtil.parse(bodyString).getByPath("primaryPaymentMethod.billingAddress");
            billingAddressJson.putByPath("stateProvinceName",billingAddressJson.getByPath("stateProvinceCode"));
            BillingAddress billingAddress=JSONUtil.toBean(billingAddressJson.toString(),BillingAddress.class);

            PaymentModel paymentModel=new PaymentModel();
            paymentModel.setBillingAddress(billingAddress);
            paymentModel.setPhoneNumber(phoneNumber);
            paymentModel.setOwnerName(ownerName);
            HttpResponse step4Res = HttpUtil.createRequest(Method.PUT,"https://appleid.apple.com/account/manage/payment/method/none/1")
                    .header(headers)
                    .body(JSONUtil.toJsonStr(paymentModel))
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
            accountTableView.refresh();
        }catch (Exception e){
            messageFun(account,"修改失败");
        }
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
        note.setCellValueFactory(new PropertyValueFactory<Account,String>("note"));
        answer1.setCellValueFactory(new PropertyValueFactory<Account,String>("answer1"));
        answer2.setCellValueFactory(new PropertyValueFactory<Account,String>("answer2"));
        answer3.setCellValueFactory(new PropertyValueFactory<Account,String>("answer3"));

    }


}

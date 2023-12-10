package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.TableView;
import com.sgswit.fx.model.ConsumptionBill;
import com.sgswit.fx.utils.DataUtil;
import com.sgswit.fx.utils.ICloudUtil;
import com.sgswit.fx.utils.PListUtil;
import com.sgswit.fx.utils.PurchaseBillUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Paint;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author DeZh
 * @title: ConsumptionBillController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/219:01
 */
public class QueryAccountInfoController extends TableView<ConsumptionBill> implements Initializable {
    @FXML
    public TableColumn seq;
    @FXML
    public TableColumn account;
    @FXML
    public TableColumn note;
    @FXML
    public TableColumn area;
    @FXML
    public TableColumn purchaseRecord;
    @FXML
    public TableColumn paymentInformation;
    @FXML
    public TableColumn shippingAddress;
    @FXML
    public TableColumn pwd;
    @FXML
    public TableColumn accountBalance;
    @FXML
    public javafx.scene.control.TableView accountTableView;
    @FXML
    public Button accountQueryBtn;
    @FXML
    public TableColumn whetherArrearage;
    @FXML
    public TableColumn name;
    @FXML
    public TableColumn familyDetails;

    private ObservableList<ConsumptionBill> accountList = FXCollections.observableArrayList();

    public QueryAccountInfoController(){

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
    }

    @FXML
    protected void onAccountInputBtnClick() throws IOException {
        super.openImportAccountView(ConsumptionBill.class,"account----pwd");
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
        super.clearAccountListButtonAction();
    }

    @FXML
    protected void onAccountQueryBtnClick(){
        try {
            accountList=accountTableView.getItems();
            if(accountList.size() < 1){
                return;
            }
            for(ConsumptionBill account:accountList){
                //判断是否已执行或执行中,避免重复执行
                if(!StrUtil.isEmptyIfStr(account.getNote())){
                    continue;
                }else{
                    accountQueryBtn.setText("正在查询");
                    accountQueryBtn.setTextFill(Paint.valueOf("#FF0000"));
                    accountQueryBtn.setDisable(true);

                    account.setNote("正在登录...");
                    accountTableView.refresh();

                    new Thread(new Runnable() {
                        @Override
                        public void run(){
                            try {
                                try {
                                    Map<String,Object> accountInfoMap=PurchaseBillUtil.authenticate(account.getAccount(),account.getPwd());
                                    if(accountInfoMap.get("code").equals("200")){
                                        account.setNote("查询成功");
                                        accountTableView.refresh();
                                        account.setAccountBalance(accountInfoMap.get("creditDisplay").toString());

                                        account.setArea(accountInfoMap.get("countryName").toString());
                                        account.setShippingAddress(accountInfoMap.get("address").toString());
                                        account.setPaymentInformation(accountInfoMap.get("paymentMethod").toString());
                                        account.setName(accountInfoMap.get("name").toString());
                                        account.setPurchaseRecord(accountInfoMap.get("purchasesLast90Count").toString());
                                        //家庭共享信息
                                        HttpResponse response= ICloudUtil.checkCloudAccount(DataUtil.getClientIdByAppleId(account.getAccount()),account.getAccount(),account.getPwd() );
                                        if(response.getStatus()==200){
                                            try {
                                                String rb = response.charset("UTF-8").body();
                                                JSONObject rspJSON = PListUtil.parse(rb);
                                                if("0".equals(rspJSON.getStr("status"))){
                                                    JSONObject delegates= rspJSON.getJSONObject("delegates");
                                                    JSON comAppleMobileme =JSONUtil.parse(delegates.get("com.apple.mobileme"));
                                                    String status= comAppleMobileme.getByPath("status",String.class);
                                                    if("0".equals(status)){
                                                        //获取家庭共享
                                                        Map<String,Object> res=ICloudUtil.getFamilyDetails(ICloudUtil.getAuthByHttResponse(response),account.getAccount());
                                                        if("200".equals(res.get("code"))){
                                                            account.setFamilyDetails(res.get("familyDetails").toString());
                                                        }
                                                    }else{
                                                        if(Constant.ACCOUNT_INVALID_HSA_TOKEN.equals(comAppleMobileme.getByPath("status-error",String.class))){
                                                            account.setFamilyDetails("-");
                                                        }else{
                                                            account.setFamilyDetails("-");
                                                        }
                                                    }
                                                }else{
                                                }
                                            }catch (Exception e){
                                                account.setFamilyDetails("-");
                                            }
                                        }else {
                                            account.setFamilyDetails("-");
                                        }



                                        account.setNote("查询完成");
                                        accountTableView.refresh();
                                    }else{
                                        account.setNote(accountInfoMap.get("msg").toString());
                                        accountTableView.refresh();
                                    }
                                } catch (Exception e) {
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
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void queryFail(ConsumptionBill account) {
        String note = "查询失败，请确认用户名密码是否正确";
        account.setNote(note);
        accountTableView.refresh();
    }
    private void messageFun(ConsumptionBill account,String message) {
        account.setNote(message);
        accountTableView.refresh();
    }

    @FXML
    protected void onAreaQueryLogBtnClick() throws Exception{
        super.localHistoryButtonAction();
    }

    private void initAccountTableView(){
        seq.setCellValueFactory(new PropertyValueFactory<ConsumptionBill,Integer>("seq"));
        account.setCellValueFactory(new PropertyValueFactory<ConsumptionBill,String>("account"));
        pwd.setCellValueFactory(new PropertyValueFactory<ConsumptionBill,String>("pwd"));
        area.setCellValueFactory(new PropertyValueFactory<ConsumptionBill,String>("area"));
        accountBalance.setCellValueFactory(new PropertyValueFactory<ConsumptionBill,String>("accountBalance"));
        name.setCellValueFactory(new PropertyValueFactory<ConsumptionBill,String>("name"));
        familyDetails.setCellValueFactory(new PropertyValueFactory<ConsumptionBill,String>("familyDetails"));
        whetherArrearage.setCellValueFactory(new PropertyValueFactory<ConsumptionBill,String>("whetherArrearage"));
        purchaseRecord.setCellValueFactory(new PropertyValueFactory<ConsumptionBill,String>("purchaseRecord"));
        paymentInformation.setCellValueFactory(new PropertyValueFactory<ConsumptionBill,String>("paymentInformation"));
        shippingAddress.setCellValueFactory(new PropertyValueFactory<ConsumptionBill,String>("shippingAddress"));
    }

    @FXML
    protected void onStopBtnClick(ActionEvent actionEvent) {

    }
    private  void integratedData(ConsumptionBill consumptionBill,List<String> datas) {
        Date nowDate= new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SSSXXX");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        BigDecimal costTotalAmount=BigDecimal.ZERO;
        String appleId=consumptionBill.getAccount();
        try {
            //删除历史记录
            Db.use().del("purchase_record","apple_id",consumptionBill.getAccount());
            String currency="";
            if(datas.size()>0){
                for(String s:datas){
                    JSONArray purchaseInfoJsonArr=JSONUtil.parseArray(JSONUtil.parse(s).getByPath("purchases"));
                    for(Object o:purchaseInfoJsonArr){
                        JSONObject json= (JSONObject) o;
                        //写入记录
                        Entity entity=new Entity();
                        entity.setTableName("purchase_record");
                        entity.set("apple_id",consumptionBill.getAccount());
                        entity.set("purchase_id",json.getByPath("purchaseId"));
                        entity.set("weborder",json.getByPath("weborder"));
                        String purchaseDateStr= json.getStr("purchaseDate");
                        Date  purchaseDate = purchaseDateStr.contains(".")?format2.parse(purchaseDateStr):format.parse(purchaseDateStr);
                        entity.set("purchase_date",purchaseDate.getTime());
                        entity.set("estimated_total_amount",json.getByPath("estimatedTotalAmount"));
                        entity.set("plis",json.getByPath("plis"));
                        Db.use().insert(entity);
                    }
                }
            }

            Entity entityLast=Db.use().queryOne("SELECT * FROM purchase_record WHERE apple_id='"+appleId+"' ORDER BY purchase_date ASC LIMIT 1;");
            nowDate.setTime(entityLast.getLong("purchase_date"));
            consumptionBill.setLastPurchaseDate(sdf.format(nowDate));

            Entity entityEarliest=Db.use().queryOne("SELECT * FROM purchase_record WHERE apple_id='"+appleId+"' ORDER BY purchase_date desc LIMIT 1;");
            nowDate.setTime(entityEarliest.getLong("purchase_date"));
            consumptionBill.setEarliestPurchaseDate(sdf.format(nowDate));





        }catch (Exception e){

        }
    }
}

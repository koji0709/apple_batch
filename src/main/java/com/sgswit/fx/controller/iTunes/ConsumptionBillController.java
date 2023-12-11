package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.controller.common.TableView;
import com.sgswit.fx.model.ConsumptionBill;
import com.sgswit.fx.model.ConsumptionBill;
import com.sgswit.fx.utils.*;
import javafx.application.Platform;
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
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author DeZh
 * @title: ConsumptionBillController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/219:01
 */
public class ConsumptionBillController extends TableView<ConsumptionBill> implements Initializable {
    @FXML
    public TableColumn seq;
    @FXML
    public TableColumn account;
    @FXML
    public TableColumn note;
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
    public TableColumn accountBalance;
    @FXML
    public javafx.scene.control.TableView accountTableView;
    @FXML
    public Button accountQueryBtn;
    @FXML
    public CheckBox isFilterFree;
    @FXML
    public CheckBox customRange;
    @FXML
    public TextField days;
    @FXML
    public ChoiceBox<String> rangeSelect;

    private ObservableList<ConsumptionBill> accountList = FXCollections.observableArrayList();

    public ConsumptionBillController(){

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> list = FXCollections.observableArrayList();
        list.add("30天");
        list.add("60天");
        list.add("90天");
        list.add("全部");
        rangeSelect.setItems(list);
        rangeSelect.setValue(list.get(list.size()-1));
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
                                    Map<String,Object> res= PurchaseBillUtil.loginAndAuth(account.getAccount(),account.getPwd());
                                    if(res.get("code").equals("200")){
                                        account.setNote("登录成功，数据查询中...");
                                        accountTableView.refresh();
                                        Map<String,Object> loginResult= (Map<String, Object>) res.get("loginResult");
                                        String token=loginResult.get("token").toString();
                                        String dsid=loginResult.get("dsid").toString();
                                        String searchCookies=loginResult.get("searchCookies").toString();
                                        account.setArea(loginResult.get("countryName").toString());
                                        List<String > jsonStrList=new ArrayList<>();
                                        PurchaseBillUtil.search(jsonStrList,dsid,"",token,searchCookies);
                                        //整合数据
                                        integratedData(account,jsonStrList);

                                        Map<String,Object> accountInfoMap=PurchaseBillUtil.authenticate(account.getAccount(),account.getPwd());
                                        account.setStatus(Boolean.valueOf(accountInfoMap.get("isDisabledAccount").toString())?"禁用":"正常");
                                        account.setAccountBalance(accountInfoMap.get("creditDisplay").toString());
                                        account.setShippingAddress(accountInfoMap.get("address").toString());
                                        account.setPaymentInformation(accountInfoMap.get("paymentMethod").toString());
                                        account.setNote("查询完成");
                                        accountTableView.refresh();
                                    }else{
                                        account.setNote(res.get("msg").toString());
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
        status.setCellValueFactory(new PropertyValueFactory<ConsumptionBill,String>("status"));
        lastPurchaseDate.setCellValueFactory(new PropertyValueFactory<ConsumptionBill,String>("lastPurchaseDate"));
        earliestPurchaseDate.setCellValueFactory(new PropertyValueFactory<ConsumptionBill,String>("earliestPurchaseDate"));
        totalConsumption.setCellValueFactory(new PropertyValueFactory<ConsumptionBill,String>("totalConsumption"));
        totalRefundAmount.setCellValueFactory(new PropertyValueFactory<ConsumptionBill,String>("totalRefundAmount"));
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
            //最近记录
            Entity entityLast=Db.use().queryOne("SELECT * FROM purchase_record WHERE apple_id='"+appleId+"' ORDER BY purchase_date ASC LIMIT 1;");
            nowDate.setTime(entityLast.getLong("purchase_date"));
            consumptionBill.setLastPurchaseDate(sdf.format(nowDate));
            //最早记录
            Entity entityEarliest=Db.use().queryOne("SELECT * FROM purchase_record WHERE apple_id='"+appleId+"' ORDER BY purchase_date desc LIMIT 1;");
            nowDate.setTime(entityEarliest.getLong("purchase_date"));
            consumptionBill.setEarliestPurchaseDate(sdf.format(nowDate));
            //消费总额
            String total_amount=Db.use().queryString("SELECT sum(CAST(SUBSTR(estimated_total_amount,2 ) AS REAL)) FROM purchase_record;");
            consumptionBill.setTotalConsumption(entityEarliest.getStr("estimated_total_amount").substring(0,1)+total_amount);
            String countSql="select COUNT(purchase_id) as count,strftime('%Y', datetime(purchase_date/1000, 'unixepoch', 'localtime'))  as yyyy FROM purchase_record GROUP BY  strftime('%Y', datetime(purchase_date/1000, 'unixepoch', 'localtime')) ;";
            List<Entity> countInfo=Db.use().query(countSql);
            List<String> purchaseRecord=new ArrayList<>(countInfo.size());
            for(Entity entity:countInfo){
                String s=String.format("%s[%s]",entity.getStr("yyyy"),entity.getStr("count"));
                purchaseRecord.add(s);
            }
            consumptionBill.setPurchaseRecord(String.join("|",purchaseRecord));
        }catch (Exception e){

        }
    }
}

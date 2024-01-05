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
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.ConsumptionBill;
import com.sgswit.fx.model.CreditCard;
import com.sgswit.fx.utils.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;

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
public class QueryAccountInfoController extends CustomTableView<ConsumptionBill> implements Initializable {
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
                if(!StrUtil.isEmptyIfStr(account.getNote())){
                    continue;
                }else{
                    executeFun(account);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

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
    @FXML
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> items=new ArrayList<>(super.menuItem) ;
        items.add(Constant.RightContextMenu.TWO_FACTOR_CODE.getCode());
        super.onContentMenuClick(contextMenuEvent,accountTableView,items);
    }

    /**重新执行**/
    @Override
    protected void reExecute(ConsumptionBill account){
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
    protected void twoFactorCodeExecute(ConsumptionBill account, String authCode){
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
    private void executeFun(ConsumptionBill account){
        accountQueryBtn.setText("正在查询");
        accountQueryBtn.setTextFill(Paint.valueOf("#FF0000"));
        accountQueryBtn.setDisable(true);
        new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    try {
                        account.setHasFinished(false);
                        setAndRefreshNote(account,"正在登录...",false);
                        String step= StringUtils.isEmpty(account.getStep())?"01":account.getStep();
                        Map<String,Object> accountInfoMap=account.getAuthData();
                        if("00".equals(step)){
                            String authCode=account.getAuthCode();
                            accountInfoMap= PurchaseBillUtil.iTunesAuth(authCode,accountInfoMap);
                        }else if("01".equals(step)){
                            accountInfoMap= PurchaseBillUtil.iTunesAuth(account.getAccount(),account.getPwd());
                        }else{
                            accountInfoMap=new HashMap<>();
                        }
                        if(Constant.TWO_FACTOR_AUTHENTICATION.equals(accountInfoMap.get("code"))) {
                            account.setNote(String.valueOf(accountInfoMap.get("msg")));
                            account.setAuthData(accountInfoMap);
                        }else if(!Constant.SUCCESS.equals(accountInfoMap.get("code"))){
                            account.setNote(String.valueOf(accountInfoMap.get("msg")));
                            accountTableView.refresh();
                        }else {
                            boolean hasInspectionFlag= (boolean) accountInfoMap.get("hasInspectionFlag");
                            if(!hasInspectionFlag){
                                tableRefreshAndInsertLocal(account, "此 Apple ID 尚未用于 App Store。");
                                return;
                            }

                            setAndRefreshNote(account,"登录成功，读取用户信息...",false);
                            Map<String,Object> accountSummaryMap=PurchaseBillUtil.accountSummary(accountInfoMap);
                            account.setArea(accountInfoMap.get("countryName").toString());
                            account.setAccountBalance(accountInfoMap.get("creditDisplay").toString());
                            account.setShippingAddress(accountSummaryMap.get("address").toString());
                            account.setPaymentInformation(accountSummaryMap.get("paymentMethod").toString());
                            account.setName(accountInfoMap.get("name").toString());
                            int purchasesLast90Count=PurchaseBillUtil.accountPurchasesLast90Count(accountInfoMap);
                            account.setPurchaseRecord(String.valueOf(purchasesLast90Count));
                            setAndRefreshNote(account,"查询成功，获取家庭共享信息...",false);
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
                                            if(Constant.SUCCESS.equals(res.get("code"))){
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
                            account.setHasFinished(true);
                            tableRefreshAndInsertLocal(account, "查询完成");
                            accountTableView.refresh();
                        }
                    } catch (Exception e) {
                        account.setHasFinished(true);
                        tableRefreshAndInsertLocal(account, "操作失败，接口异常");
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
    private void tableRefreshAndInsertLocal(ConsumptionBill account, String message){
        account.setNote(message);
        accountTableView.refresh();
        super.insertLocalHistory(List.of(account));
    }
}

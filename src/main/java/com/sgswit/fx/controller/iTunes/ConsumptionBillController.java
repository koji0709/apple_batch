package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.controller.common.UnavailableException;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.ConsumptionBill;
import com.sgswit.fx.utils.AppleIDUtil;
import com.sgswit.fx.utils.PointUtil;
import com.sgswit.fx.utils.PurchaseBillUtil;
import com.sgswit.fx.utils.db.DataSourceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;

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
public class ConsumptionBillController extends CustomTableView<ConsumptionBill>{
    @FXML
    public CheckBox isFilterFree;
    @FXML
    public CheckBox customRange;
    @FXML
    public TextField days;
    @FXML
    public ChoiceBox<String> rangeSelect;

    public ConsumptionBillController(){

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.CONSUMPTION_BILL.getCode())));
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
    protected void onAccountInputBtnClick(ActionEvent actionEvent) throws IOException {
        super.openImportAccountView(List.of("account----pwd"),actionEvent);
    }

    @Override
    public void accountHandler(ConsumptionBill account) {
        account.setHasFinished(false);
        setAndRefreshNote(account,"登录中...");
        //判断账号是否为双重认证的账号
        Account a=new Account();
        a.setPwd(account.getPwd());
        a.setAccount(account.getAccount());
        HttpResponse step1Res = AppleIDUtil.signin(a);
        ThreadUtil.sleep(2000);
        if (step1Res.getStatus() == 503){
            throw new UnavailableException();
        }else if (step1Res.getStatus() != 409) {
            throw new ServiceException("Apple ID 或密码不正确");
        }
        String step1Body = step1Res.body();
        JSON json = JSONUtil.parse(step1Body);
        if (json == null) {
            throw new ServiceException("Apple ID 或密码不正确");
        }
        //step2 auth 获取认证信息
        String authType = (String) json.getByPath("authType");
        if ("hsa2".equals(authType)) {
            throw new ServiceException("此 Apple ID已开启双重验证，请输入双重验证码");
        }
        int accountPurchasesLast90Count=0;
        Map<String,Object> accountInfoMap=PurchaseBillUtil.iTunesAuth(account.getAccount(),account.getPwd());
        if(!accountInfoMap.get("code").equals(Constant.SUCCESS)){
            account.setHasFinished(true);
            setAndRefreshNote(account,String.valueOf(accountInfoMap.get("msg")));
            return;
        }else{
            boolean hasInspectionFlag= (boolean) accountInfoMap.get("hasInspectionFlag");
            if(!hasInspectionFlag){
                throw new ServiceException("此 Apple ID 尚未用于 App Store。");
            }
            accountInfoMap=PurchaseBillUtil.accountSummary(accountInfoMap);
            account.setStatus(Boolean.valueOf(accountInfoMap.get("isDisabledAccount").toString())?"禁用":"正常");
            account.setAccountBalance(accountInfoMap.get("creditDisplay").toString());
            account.setShippingAddress(accountInfoMap.get("address").toString());
            account.setPaymentInformation(accountInfoMap.get("paymentMethod").toString());

            accountPurchasesLast90Count=PurchaseBillUtil.accountPurchasesLast90Count(accountInfoMap);
            setAndRefreshNote(account,"数据加载中...");
        }

        Map<String,Object> res= PurchaseBillUtil.webLoginAndAuth(account.getAccount(),account.getPwd());
        if(res.get("code").equals(Constant.SUCCESS)){
            account.setHasFinished(true);
            accountTableView.refresh();
            Map<String,Object> loginResult= (Map<String, Object>) res.get("loginResult");
            String token=loginResult.get("token").toString();
            String dsid=loginResult.get("dsid").toString();
            String searchCookies=loginResult.get("searchCookies").toString();
            account.setArea(loginResult.get("countryName").toString());
            List<String> jsonStrList=new ArrayList<>();
            jsonStrList.clear();
            PurchaseBillUtil.search(jsonStrList,dsid,"",token,searchCookies);
            //整合数据
            integratedData(new HashMap<>(), accountPurchasesLast90Count,account,jsonStrList);
            if(jsonStrList.size()==0){
                account.setNote("所选期间无购买记录");
            }else{
                account.setNote("查询完成");
            }

            accountTableView.refresh();
        }else{
            setAndRefreshNote(account,String.valueOf(res.get("msg")));
        }
    }

    private  void integratedData(Map<String,Object> queryParas,int accountPurchasesLast90Count,ConsumptionBill consumptionBill,List<String> datas) {
        Date nowDate= new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SSSXXX");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        BigDecimal costTotalAmount=BigDecimal.ZERO;
        String appleId=consumptionBill.getAccount();
        try {
            //删除历史记录
            Db.use(DataSourceFactory.getDataSource()).del("purchase_record","apple_id",consumptionBill.getAccount());
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
                        Db.use(DataSourceFactory.getDataSource()).insert(entity);
                    }
                }
                //最近记录
                Entity entityLast=Db.use(DataSourceFactory.getDataSource()).queryOne("SELECT * FROM purchase_record WHERE apple_id='"+appleId+"' ORDER BY purchase_date  desc LIMIT 1;");
                nowDate.setTime(entityLast.getLong("purchase_date"));
                consumptionBill.setLastPurchaseDate(sdf.format(nowDate));
                //最早记录
                Entity entityEarliest=Db.use(DataSourceFactory.getDataSource()).queryOne("SELECT * FROM purchase_record WHERE apple_id='"+appleId+"' ORDER BY purchase_date  ASC LIMIT 1;");
                nowDate.setTime(entityEarliest.getLong("purchase_date"));
                consumptionBill.setEarliestPurchaseDate(sdf.format(nowDate));
                //消费总额
                String total_amount=Db.use(DataSourceFactory.getDataSource()).queryString("SELECT sum(CAST(SUBSTR(estimated_total_amount,2 ) AS REAL)) FROM purchase_record  where apple_id='"+appleId+"';");
                consumptionBill.setTotalConsumption(entityEarliest.getStr("estimated_total_amount").substring(0,1)+total_amount);
                String countSql="select COUNT(purchase_id) as count,strftime('%Y', datetime(purchase_date/1000, 'unixepoch', 'localtime'))  as yyyy FROM purchase_record where apple_id='"+appleId+"' GROUP BY  strftime('%Y', datetime(purchase_date/1000, 'unixepoch', 'localtime')) ;";
                List<Entity> countInfo=Db.use(DataSourceFactory.getDataSource()).query(countSql);
                List<Map<String,String>> purchaseRecord=new ArrayList<>(countInfo.size());
                List<String> sList=new ArrayList<>(countInfo.size());
                for(Entity entity:countInfo){
                    String s=String.format("%s[%s]",entity.getStr("yyyy"),entity.getStr("count"));
                    purchaseRecord.add(new HashMap<>(){{
                        put("yyyy",entity.getStr("yyyy"));
                        put("count",entity.getStr("count"));
                    }});
                }
                Collections.sort(purchaseRecord, (o1, o2) -> (Long.valueOf(o2.get("yyyy")).compareTo(Long.valueOf(o1.get("yyyy")))));
                for(Map<String,String> map:purchaseRecord){
                    String s=String.format("%s[%s]",map.get("yyyy"),map.get("count"));
                    sList.add(s);
                }

                String accountPurchasesLast90CountStr=String.format("%s[%s]","90天内",accountPurchasesLast90Count);
                consumptionBill.setPurchaseRecord(accountPurchasesLast90CountStr+"|"+String.join("|",sList));
            }
        }catch (Exception e){
        }
    }
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> items=new ArrayList<>(super.menuItem) ;
//        items.add(Constant.RightContextMenu.WEB_TWO_FACTOR_CODE.getCode());
        super.onContentMenuClick(contextMenuEvent,accountTableView,items);

    }
    @Override
    protected void twoFactorCodeExecute(ConsumptionBill a, String authCode){

    }
}

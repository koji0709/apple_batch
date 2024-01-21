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
import com.sgswit.fx.enums.FunctionListEnum;
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
public class QueryAccountInfoController extends CustomTableView<ConsumptionBill>{
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.QUERY_ACCOUNT_INFO.getCode())));
        super.initialize(url,resourceBundle);
    }

    @FXML
    protected void onAccountInputBtnClick() throws IOException {
        super.openImportAccountView(List.of("account----pwd"));
    }

    @FXML
    protected void stopTaskButtonAction(ActionEvent actionEvent) {

    }
    @FXML
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> items=new ArrayList<>(super.menuItem) ;
        items.add(Constant.RightContextMenu.TWO_FACTOR_CODE.getCode());
        super.onContentMenuClick(contextMenuEvent,accountTableView,items);
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
                accountHandlerExpand(account);
            }else{
                alert("未下发双重验证码");
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void accountHandler(ConsumptionBill account){
        account.setHasFinished(false);
        setAndRefreshNote(account,"正在登录...");
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
            account.setDataStatus("0");
            account.setNote(String.valueOf(accountInfoMap.get("msg")));
            accountTableView.refresh();
        }else {
            boolean hasInspectionFlag= (boolean) accountInfoMap.get("hasInspectionFlag");
            if(!hasInspectionFlag){
                account.setDataStatus("0");
                tableRefreshAndInsertLocal(account, "此 Apple ID 尚未用于 App Store。");
                return;
            }

            setAndRefreshNote(account,"登录成功，读取用户信息...");
            Map<String,Object> accountSummaryMap=PurchaseBillUtil.accountSummary(accountInfoMap);
            account.setArea(accountInfoMap.get("countryName").toString());
            account.setAccountBalance(accountInfoMap.get("creditDisplay").toString());
            account.setShippingAddress(accountSummaryMap.get("address").toString());
            account.setPaymentInformation(accountSummaryMap.get("paymentMethod").toString());
            account.setName(accountInfoMap.get("name").toString());
            int purchasesLast90Count=PurchaseBillUtil.accountPurchasesLast90Count(accountInfoMap);
            account.setPurchaseRecord(String.valueOf(purchasesLast90Count));
            setAndRefreshNote(account,"查询成功，获取家庭共享信息...");
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
                account.setDataStatus("1");
            }else {
                account.setFamilyDetails("-");
                account.setDataStatus("0");
            }
            tableRefreshAndInsertLocal(account, "查询完成");
            accountTableView.refresh();
        }
        account.setHasFinished(true);
        accountTableView.refresh();
    }
}

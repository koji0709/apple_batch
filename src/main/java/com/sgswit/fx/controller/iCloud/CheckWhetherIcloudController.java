package com.sgswit.fx.controller.iCloud;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dd.plist.NSObject;
import com.dd.plist.XMLPropertyListParser;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CommRightContextMenuView;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.iTunes.AccountInputPopupController;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.*;

/**
 * @author DeZh
 * @title: CheckWhetherIcloudController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/2714:40
 */
public class CheckWhetherIcloudController extends CustomTableView<Account>{

    @FXML
    public TableColumn seq;
    @FXML
    public TableColumn account;
    @FXML
    public TableColumn area;
    @FXML
    public TableColumn dsid;
    @FXML
    public TableColumn support;
    @FXML
    public TableColumn note;
    @FXML
    public TableColumn pwd;
    @FXML
    public Button accountQueryBtn;

    @FXML
    private TableView accountTableView;

    private ObservableList<Account> list = FXCollections.observableArrayList();
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }
    @FXML
    protected void onAccountInputBtnClick() throws IOException {
        super.openImportAccountView(List.of("account----pwd"));
    }
    @FXML
    protected void onAreaQueryLogBtnClick() throws Exception{
        super.localHistoryButtonAction();
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
    protected void onAccountQueryBtnClick() throws Exception{
        list=accountTableView.getItems();
        if(list.size() < 1){
            return;
        }
        for(Account account:list){
            //判断是否已执行或执行中,避免重复执行
            if(!StrUtil.isEmptyIfStr(account.getNote())){
                continue;
            }else{
                executeFun(account);
            }

        }
    }
    protected void checkCloudAcc(Account account) {
        tableRefresh(account,"正在登录...");
        HttpResponse response;
        String clientId=DataUtil.getClientIdByAppleId(account.getAccount());
        if(!StringUtils.isEmpty(account.getStep()) && account.getStep().equals("00")){
            response= ICloudUtil.checkCloudAccount(clientId,account.getAccount(),account.getPwd()+ account.getAuthCode());
        }else{
            response= ICloudUtil.checkCloudAccount(clientId,account.getAccount(),account.getPwd() );
        }

        if(response.getStatus()==200){
            try {
                String rb = response.charset("UTF-8").body();
                JSONObject rspJSON = PListUtil.parse(rb);
                if("0".equals(rspJSON.getStr("status"))){
                    String message="查询成功";
                    JSONObject delegates= rspJSON.getJSONObject("delegates");
                    JSON comAppleMobileme =JSONUtil.parse(delegates.get("com.apple.mobileme"));
                    String status= comAppleMobileme.getByPath("status",String.class);
                    if("0".equals(status)){
                        account.setSupport("支持");
                        account.setDsid(rspJSON.getStr("dsid"));
                    }else{
                        if(Constant.ACCOUNT_INVALID_HSA_TOKEN.equals(comAppleMobileme.getByPath("status-error",String.class))){
                            message=comAppleMobileme.getByPath("status-message",String.class);
                            account.getAuthData().put("code",Constant.TWO_FACTOR_AUTHENTICATION);
                        }else{
                            account.setSupport("不支持");
                        }
                    }
                    JSONObject ids= delegates.getJSONObject("com.apple.private.ids");
                    if("0".equals(ids.getStr("status"))){
                        String regionId=JSONUtil.parse(ids.get("service-data")).getByPath("invitation-context.region-id",String.class);
                        regionId= DataUtil.getNameByCountryCode(regionId.split(":")[1]);
                        account.setArea(regionId);
                        account.setDsid(rspJSON.getStr("dsid"));
                    }
                    account.setDataStatus("1");
                    tableRefreshAndInsertLocal(account,message);
                }else{
                    account.setDataStatus("0");
                    String message="";
                    for (Map.Entry<String, String> entry : Constant.errorMap.entrySet()) {
                        if (StringUtils.containsIgnoreCase(rspJSON.getStr("status-message"),entry.getKey())){
                            message=entry.getValue();
                            break;
                        }
                    }
                    if(!StringUtils.isEmpty(message)){
                        tableRefreshAndInsertLocal(account,message);
                    }else{
                        tableRefreshAndInsertLocal(account,rspJSON.getStr("status-message"));
                    }
                }
                account.setHasFinished(true);
            }catch (Exception e){
                account.setHasFinished(true);
                account.setDataStatus("0");
                tableRefreshAndInsertLocal(account,"Apple ID或密码错误。");
            }
        }else {
            account.setHasFinished(true);
            tableRefresh(account,response.body());
        }
    }
    private void tableRefresh(Account account,String message){
        account.setNote(message);
        accountTableView.refresh();
    }
    private void tableRefreshAndInsertLocal(Account account, String message){
        account.setNote(message);
        accountTableView.refresh();
        insertLocalHistory(List.of(account));
    }
    @FXML
    public void onStopBtnClick(ActionEvent actionEvent) {
    }
    @FXML
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> items=new ArrayList<>(super.menuItem) ;
        items.add(Constant.RightContextMenu.TWO_FACTOR_CODE.getCode());
        super.onContentMenuClick(contextMenuEvent,accountTableView,items);
    }
    protected  void executeFun(Account account){
        try {
            //非双重认证
            accountQueryBtn.setText("正在查询");
            accountQueryBtn.setTextFill(Paint.valueOf("#FF0000"));
            accountQueryBtn.setDisable(true);

            new Thread(new Runnable() {
                @Override
                public void run(){
                    try {
                        try {
                            account.setHasFinished(false);
                            account.setNote("正在登录...");
                            accountTableView.refresh();
                            checkCloudAcc(account);
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
                                setAccountNumLabel();
                                accountQueryBtn.setDisable(false);
                                accountQueryBtn.setText("开始执行");
                                accountQueryBtn.setTextFill(Paint.valueOf("#238142"));
                                return 1;
                            }
                        });
                    }
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**重新执行**/
    @Override
    protected void reExecute(Account account){
        executeFun(account);
    }
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
}

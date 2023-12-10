package com.sgswit.fx.controller.iCloud;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.iTunes.AccountInputPopupController;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.DataUtil;
import com.sgswit.fx.utils.ICloudUtil;
import com.sgswit.fx.utils.PListUtil;
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
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

/**
 * @author DeZh
 * @title: FamilyDetailsController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/10/2714:40
 */
public class FamilyDetailsController {

    @FXML
    public TableColumn seq;
    @FXML
    public TableColumn account;
    @FXML
    public TableColumn area;
    @FXML
    public TableColumn dsid;
    @FXML
    public TableColumn familyDetails;
    @FXML
    public TableColumn note;
    @FXML
    public TableColumn pwd;
    @FXML
    public Button accountQueryBtn;

    @FXML
    private TableView accountTableView;

    private ObservableList<Account> list = FXCollections.observableArrayList();

    @FXML
    protected void onAccountInputBtnClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/iTunes/account-input-popup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 450);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        Stage popupStage = new Stage();

        popupStage.setTitle("账户导入");

        popupStage.initModality(Modality.WINDOW_MODAL);
        popupStage.setScene(scene);
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
            account.setPwd(its[1]);
            list.add(account);
        }
        initAccountTableView();
        accountTableView.setEditable(true);
        accountTableView.setItems(list);
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
        for(Account account:list){
            //判断是否已执行或执行中,避免重复执行
            if(!StrUtil.isEmptyIfStr(account.getNote())){
                continue;
            }
            //非双重认证
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
    protected void checkCloudAcc(Account account) {
        tableRefresh(account,"正在登录...");
        HttpResponse response= ICloudUtil.checkCloudAccount(DataUtil.getClientIdByAppleId(account.getAccount()),account.getAccount(),account.getPwd() );
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
                        //获取家庭共享
                        Map<String,Object> res=ICloudUtil.getFamilyDetails(ICloudUtil.getAuthByHttResponse(response),account.getAccount());
                        if("200".equals(res.get("code"))){
                            account.setFamilyDetails(res.get("familyDetails").toString());
                        }
                    }else{
                        if(Constant.ACCOUNT_INVALID_HSA_TOKEN.equals(comAppleMobileme.getByPath("status-error",String.class))){
                            tableRefresh(account,comAppleMobileme.getByPath("status-message",String.class));
                            message=comAppleMobileme.getByPath("status-message",String.class);
                        }else{
                            message="未激活iCloud账户";
                        }
                    }
                    JSONObject ids= delegates.getJSONObject("com.apple.private.ids");
                    if("0".equals(ids.getStr("status"))){
                        String regionId=JSONUtil.parse(ids.get("service-data")).getByPath("invitation-context.region-id",String.class);
                        regionId= DataUtil.getNameByCountryCode(regionId.split(":")[1]);
                        account.setArea(regionId);
                    }
                    account.setDsid(rspJSON.getStr("dsid"));
                    tableRefresh(account,message);
                }else{
                    tableRefresh(account,rspJSON.getStr("status-message"));
                }

            }catch (Exception e){
                tableRefresh(account,"账号不存在或密码错误");
                e.printStackTrace();
            }
        }else {
            tableRefresh(account,response.body());
        }
    }
    private void initAccountTableView(){
        seq.setCellValueFactory(new PropertyValueFactory<Account,Integer>("seq"));
        account.setCellValueFactory(new PropertyValueFactory<Account,String>("account"));
        pwd.setCellValueFactory(new PropertyValueFactory<Account,String>("pwd"));
        area.setCellValueFactory(new PropertyValueFactory<Account,String>("area"));
        dsid.setCellValueFactory(new PropertyValueFactory<Account,String>("dsid"));
        familyDetails.setCellValueFactory(new PropertyValueFactory<Account,String>("familyDetails"));
        note.setCellValueFactory(new PropertyValueFactory<Account,String>("note"));
    }
    private void tableRefresh(Account account,String message){
        account.setNote(message);
        accountTableView.refresh();
    }
    @FXML
    public void onStopBtnClick(ActionEvent actionEvent) {
    }
}

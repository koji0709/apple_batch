package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CommRightContextMenuView;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.CreditCard;
import com.sgswit.fx.utils.ITunesUtil;
import com.sgswit.fx.utils.MapUtils;
import com.sgswit.fx.utils.PurchaseBillUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;


/**
　* iTunes付款方式管理
  * @param
　* @throws
　* @author DeZh
　* @date 2023/10/27 10:10
 */
public class BindVirtualCardController extends CustomTableView<CreditCard> implements Initializable  {
    @FXML
    public TableColumn creditInfo;
    @FXML
    private TableColumn seq;
    @FXML
    private TableColumn account;
    @FXML
    private TableColumn pwd;
    @FXML
    private TableColumn note;
    @FXML
    private TableView accountTableView;
    @FXML
    private Button accountQueryBtn;

    @FXML
    private Button accountExportBtn;

    private ObservableList<CreditCard> list = FXCollections.observableArrayList();

    public BindVirtualCardController(){


    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }


    @FXML
    protected void onAccountInputBtnClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("views/iTunes/virtualCard-input-popup.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 300);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");

        Stage popupStage = new Stage();

        popupStage.setTitle("信用卡导入");
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(scene);
        popupStage.setResizable(false);
        popupStage.initStyle(StageStyle.UTILITY);
        popupStage.showAndWait();

        VirtualCardInputPopupController c = fxmlLoader.getController();
        if(null == c.getData() || "".equals(c.getData())){
            return;
        }
        String[] lineArray = c.getData().split("\n");
        for(String item : lineArray){
            boolean f=false;
            //判断是否符合正则表达式
            CreditCard creditCard = new CreditCard();
            String[] array=item.split("----");
            if(array.length==3){
                creditCard.setSeq(list.size()+1);
                creditCard.setAccount(array[0]);
                creditCard.setPwd(array[1]);
                creditCard.setCreditInfo(array[2]);
                String cCreditInfoRegex = "\\w{1,40}/\\d{6}/\\w{3}";
                if(array[2].matches(cCreditInfoRegex)){
                    f=true;
                    String[] creditInfoArr=array[2].split("/");
                    creditCard.setCreditCardNumber(creditInfoArr[0]);
                    creditCard.setCreditVerificationNumber(creditInfoArr[2]);
                    String monthAndYear=creditInfoArr[1];
                    creditCard.setCreditCardExpirationMonth(Integer.valueOf(monthAndYear.substring(0,2)).toString());
                    creditCard.setCreditCardExpirationYear(monthAndYear.substring(2));
                }
            }
            if(f){
                list.add(creditCard);
            }
        }
        initAccountTableView();
        accountTableView.setItems(list);
        setAccountNumLabel();
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
        super.accountList=list;
        super.clearAccountListButtonAction();
    }

    @FXML
    protected void onAccountQueryBtnClick() throws Exception{
        for(CreditCard account:list){
            if (!StrUtil.isEmptyIfStr(account.getNote())) {
                continue;
            }else{
                executeFun(account);
            }
        }
    }

    private void executeFun(CreditCard account){
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
                        String step= StringUtils.isEmpty(account.getStep())?"01":account.getStep();
                        Map<String,Object> res=account.getAuthData();
                        if("01".equals(step)){
                            res= PurchaseBillUtil.iTunesAuth(account.getAccount(),account.getPwd());
                        }else if("02".equals(step)){
                            res.put("smsCode",account.getAuthCode());
                        }else{
                            res=new HashMap<>();
                        }

                        res.put("creditCardNumber",account.getCreditCardNumber());
                        res.put("creditCardExpirationMonth",account.getCreditCardExpirationMonth());
                        res.put("creditCardExpirationYear",account.getCreditCardExpirationYear());
                        res.put("creditVerificationNumber",account.getCreditVerificationNumber());
                        if(Constant.TWO_FACTOR_AUTHENTICATION.equals(res.get("code"))) {
                            account.setNote(String.valueOf(res.get("msg")));
                            account.setAuthData(res);
                        }else if(!Constant.SUCCESS.equals(res.get("code"))){
                            account.setDataStatus("0");
                            tableRefreshAndInsertLocal(account, String.valueOf(res.get("msg")));
                        }else{
                            boolean hasInspectionFlag= (boolean) res.get("hasInspectionFlag");
                            if(!hasInspectionFlag){
                                account.setDataStatus("0");
                                tableRefreshAndInsertLocal(account, "此 Apple ID 尚未用于 App Store。");
                                return;
                            }
                            account.setNote("登录成功，正在验证银行卡信息...");
                            accountTableView.refresh();

                            Map<String,Object> addCreditPaymentRes=ITunesUtil.addCreditPayment(res,step);
                            if(Constant.SUCCESS.equals(addCreditPaymentRes.get("code")) && "01".equals(step)){
                                Map<String,Object> data=MapUtil.get(addCreditPaymentRes,"data",Map.class);
                                account.setAuthData(res);
                            }else{
                                if(Constant.SUCCESS.equals(MapUtils.getStr(addCreditPaymentRes,"code"))){
                                    account.setDataStatus("1");
                                }else{
                                    account.setDataStatus("0");
                                }
                                tableRefreshAndInsertLocal(account, MapUtil.getStr(addCreditPaymentRes,"message"));
                            }
                            account.setNote(MapUtil.getStr(addCreditPaymentRes,"message"));
                        }
                        account.setHasFinished(true);
                        accountTableView.refresh();
                    } catch (Exception e) {
                        account.setHasFinished(true);
                        tableRefreshAndInsertLocal(account, "操作失败，接口异常。");
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
    }


   /**第二步操作**/
    @Override
    protected void secondStepHandler(CreditCard account, String code){
        account.setAuthCode(code);
        account.setStep("02");
        if (StringUtils.isEmpty(code)){
            return;
        }
        String step= StringUtils.isEmpty(account.getStep())?"01":account.getStep();
        Map<String,Object> res=new HashMap<>();
        if(step.equals("02")){
            res=account.getAuthData();
            if(null==res){
                return;
            }
            res.put("smsCode",account.getAuthCode());
        }else{
            res= PurchaseBillUtil.iTunesAuth(account.getAccount(),account.getPwd());
        }

        res.put("creditCardNumber",account.getCreditCardNumber());
        res.put("creditCardExpirationMonth",account.getCreditCardExpirationMonth());
        res.put("creditCardExpirationYear",account.getCreditCardExpirationYear());
        res.put("creditVerificationNumber",account.getCreditVerificationNumber());
        Map<String,Object> addCreditPaymentRes=ITunesUtil.addCreditPayment(res,step);
        if(addCreditPaymentRes.get("code").equals(Constant.SUCCESS) && "01".equals(step)){
            addCreditPaymentRes.get("data");
            account.setAuthData((ObservableMap<String, Object>) addCreditPaymentRes.get("data"));
        }else{

        }
        account.setNote(MapUtil.getStr(addCreditPaymentRes,"message"));
        accountTableView.refresh();
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
    protected void twoFactorCodeExecute(CreditCard account, String authCode){
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
    private void tableRefreshAndInsertLocal(CreditCard account, String message){
        account.setNote(message);
        accountTableView.refresh();
        super.insertLocalHistory(List.of(account));
    }

    @FXML
    protected void onAreaQueryLogBtnClick() throws Exception{
       super.localHistoryButtonAction();
    }

    private void initAccountTableView(){
        seq.setCellValueFactory(new PropertyValueFactory<CreditCard,Integer>("seq"));
        account.setCellValueFactory(new PropertyValueFactory<CreditCard,String>("account"));
        pwd.setCellValueFactory(new PropertyValueFactory<CreditCard,String>("pwd"));
        note.setCellValueFactory(new PropertyValueFactory<CreditCard,String>("note"));
        creditInfo.setCellValueFactory(new PropertyValueFactory<CreditCard,String>("creditInfo"));

    }


    public void onStopBtnClick(ActionEvent actionEvent) {

    }
    @FXML
    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        List<String> items=new ArrayList<>(super.menuItem) ;
        items.add(Constant.RightContextMenu.TWO_FACTOR_CODE.getCode());
        items.add(Constant.RightContextMenu.CODE.getCode());
        super.onContentMenuClick(contextMenuEvent,accountTableView,items);
    }

    /**重新执行**/
    @Override
    protected void reExecute(CreditCard account){
        executeFun(account);
    }
}

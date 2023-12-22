package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.sgswit.fx.controller.CommController;
import com.sgswit.fx.controller.common.CommRightContextMenuView;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.CreditCard;
import com.sgswit.fx.utils.ITunesUtil;
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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


/**
　* iTunes付款方式管理
  * @param
　* @throws
　* @author DeZh
　* @date 2023/10/27 10:10
 */
public class BindVirtualCardController extends CommRightContextMenuView<CreditCard> implements Initializable  {
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
        for(CreditCard account:list){
            if (!StrUtil.isEmptyIfStr(account.getNote())) {
                continue;
            }else{
                new Thread(new Runnable() {
                    @Override
                    public void run(){
                        try {
                            try {
                                accountQueryBtn.setText("正在查询");
                                accountQueryBtn.setTextFill(Paint.valueOf("#FF0000"));
                                accountQueryBtn.setDisable(true);
                                account.setNote("正在登录...");
                                String step= StringUtils.isEmpty(account.getStep())?"01":account.getStep();
                                Map<String,Object> res=new HashMap<>();
                                if(step.equals("02")){
                                    res=account.getAuthData();
                                    res.put("smsCode",account.getSmsCode());
                                }else{
                                    res= PurchaseBillUtil.authenticate(account.getAccount(),account.getPwd());
                                }

                                res.put("creditCardNumber",account.getCreditCardNumber());
                                res.put("creditCardExpirationMonth",account.getCreditCardExpirationMonth());
                                res.put("creditCardExpirationYear",account.getCreditCardExpirationYear());
                                res.put("creditVerificationNumber",account.getCreditVerificationNumber());
                                if(!res.get("code").equals("200")){
                                    account.setNote(String.valueOf(res.get("msg")));
                                }else{
                                    boolean hasInspectionFlag= (boolean) res.get("hasInspectionFlag");
                                    if(!hasInspectionFlag){
                                        account.setNote("此 Apple ID 尚未用于 App Store。");
                                        accountTableView.refresh();
                                        return;
                                    }
                                    account.setNote("登录成功，正在验证银行卡信息...");
                                    accountTableView.refresh();

                                    Map<String,Object> addCreditPaymentRes=ITunesUtil.addCreditPayment(res,step);
                                    if(addCreditPaymentRes.get("code").equals("200") && "01".equals(step)){
                                        Map<String,Object> data=MapUtil.get(addCreditPaymentRes,"data",Map.class);
                                        account.setAuthData(mapConvertToObservableMap(data));
                                    }else{

                                    }
                                    account.setNote(MapUtil.getStr(addCreditPaymentRes,"message"));
                                }
                                accountTableView.refresh();
                            } catch (Exception e) {
                                account.setNote("操作失败，接口异常");
                                accountTableView.refresh();
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
    }


    @Override
    protected void secondStepHandler(CreditCard account, String code){
        account.setSmsCode(code);
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
            res.put("smsCode",account.getSmsCode());
        }else{
            res= PurchaseBillUtil.authenticate(account.getAccount(),account.getPwd());
        }

        res.put("creditCardNumber",account.getCreditCardNumber());
        res.put("creditCardExpirationMonth",account.getCreditCardExpirationMonth());
        res.put("creditCardExpirationYear",account.getCreditCardExpirationYear());
        res.put("creditVerificationNumber",account.getCreditVerificationNumber());
        Map<String,Object> addCreditPaymentRes=ITunesUtil.addCreditPayment(res,step);
        if(addCreditPaymentRes.get("code").equals("200") && "01".equals(step)){
            addCreditPaymentRes.get("data");
            account.setAuthData((ObservableMap<String, Object>) addCreditPaymentRes.get("data"));
        }else{

        }
        account.setNote(MapUtil.getStr(addCreditPaymentRes,"message"));
        accountTableView.refresh();
    }


   public static ObservableMap<String,Object> mapConvertToObservableMap(Map<String,Object> data){
       ObservableMap<String, Object> observableMap =FXCollections.observableHashMap();
       for (String key:data.keySet()){
           observableMap.put(key,data.get(key));
       }
       return observableMap;
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
        super.onContentMenuClick(contextMenuEvent,accountTableView,"delete-copy-smsCode");
//        try {
//            super.onContentMenuClick(contextMenuEvent,accountTableView,"delete-copy-smsCode");
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }
}

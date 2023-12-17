package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.controller.common.CommDataInputPopupController;
import com.sgswit.fx.enums.DataImportEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.CreditCard;
import com.sgswit.fx.model.GiftCard;
import com.sgswit.fx.utils.ITunesUtil;
import com.sgswit.fx.utils.PurchaseBillUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;


/**
　* iTunes付款方式管理
  * @param
　* @throws
　* @author DeZh
　* @date 2023/10/27 10:10
 */
public class BindVirtualCardController implements Initializable  {
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
                                Map<String,Object> res= PurchaseBillUtil.authenticate(account.getAccount(),account.getPwd());
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
                                    Map<String,Object> addCreditPaymentRes=ITunesUtil.addCreditPayment(res,"01");
                                    if(!addCreditPaymentRes.get("code").equals("200")){
                                        account.setNote(MapUtil.getStr(addCreditPaymentRes,"message"));
                                    }else{
                                        account.setNote(MapUtil.getStr(addCreditPaymentRes,"message"));
                                    }
                                }
                                accountTableView.refresh();
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
        creditInfo.setCellValueFactory(new PropertyValueFactory<Account,String>("creditInfo"));

    }


    public void onStopBtnClick(ActionEvent actionEvent) {
    }
}

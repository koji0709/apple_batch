package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.util.StrUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.model.Account;
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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;


/**
　* iTunes付款方式管理
  * @param
　* @throws
　* @author DeZh
　* @date 2023/10/27 10:10
 */
public class PaymentMethodController extends CustomTableView<Account> implements Initializable  {
    @FXML
    private TableView accountTableView;
    @FXML
    private TableColumn seq;
    @FXML
    private TableColumn account;
    @FXML
    private TableColumn pwd;
    @FXML
    private TableColumn note;

    @FXML
    private Button accountQueryBtn;

    @FXML
    private Button accountExportBtn;

    private ObservableList<Account> list = FXCollections.observableArrayList();

    public PaymentMethodController(){


    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        super.initialize(url,resourceBundle);
    }


    @FXML
    protected void onAccountInputBtnClick() throws IOException {
        openImportAccountView(List.of("account----pwd"));
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
        list=accountTableView.getItems();
        if(list.size() < 1){
            return;
        }
        for(Account account:list){
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
                                accountTableView.refresh();
                                Map<String,Object> res= PurchaseBillUtil.iTunesAuth(account.getAccount(),account.getPwd());
                                if(!res.get("code").equals(Constant.SUCCESS)){
                                    account.setNote(res.get("msg").toString());
                                }else{
                                    boolean hasInspectionFlag= (boolean) res.get("hasInspectionFlag");
                                    if(!hasInspectionFlag){
                                        account.setNote("此 Apple ID 尚未用于 App Store。");
                                        accountTableView.refresh();
                                        return;
                                    }
                                    account.setNote("登录成功，数据删除中...");
                                    accountTableView.refresh();
                                    res=ITunesUtil.delPaymentInfos(res);
                                    if(res.get("code").equals(Constant.SUCCESS)){
                                        account.setNote("删除成功");
                                    }else{
                                        account.setNote(res.get("msg").toString());
                                    }
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
    }


    public void onStopBtnClick(ActionEvent actionEvent) {
    }
}

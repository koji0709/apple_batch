package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.sgswit.fx.MainApplication;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.CreditCard;
import com.sgswit.fx.utils.ITunesUtil;
import com.sgswit.fx.utils.MapUtils;
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
        super.clearAccountListButtonAction();
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
                                Map<String,Object> res= PurchaseBillUtil.iTunesAuth(account.getAccount(),account.getPwd());
                                if(!res.get("code").equals(Constant.SUCCESS)){
                                    account.setDataStatus("0");
                                    account.setNote(res.get("msg").toString());
                                    tableRefreshAndInsertLocal(account, res.get("msg").toString());
                                }else{
                                    boolean hasInspectionFlag= (boolean) res.get("hasInspectionFlag");
                                    if(!hasInspectionFlag){
                                        account.setDataStatus("0");
                                        tableRefreshAndInsertLocal(account,"此 Apple ID 尚未用于 App Store。");
                                        return;
                                    }
                                    account.setNote("登录成功，数据删除中...");
                                    accountTableView.refresh();
                                    res=ITunesUtil.delPaymentInfos(res);
                                    if(res.get("code").equals(Constant.SUCCESS)){
                                        account.setDataStatus("1");
                                        tableRefreshAndInsertLocal(account, "删除成功");
                                    }else{
                                        account.setDataStatus("0");
                                        tableRefreshAndInsertLocal(account, MapUtils.getStr(res,"msg"));
                                    }

                                }
                                account.setHasFinished(true);
                                accountTableView.refresh();
                            } catch (Exception e) {
                                account.setDataStatus("0");
                                account.setHasFinished(true);
                                tableRefreshAndInsertLocal(account, "操作失败，接口异常");
                                accountQueryBtn.setDisable(false);
                                accountQueryBtn.setText("开始执行");
                                accountQueryBtn.setTextFill(Paint.valueOf("#238142"));
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

        }
    }
    @FXML
    protected void onAreaQueryLogBtnClick() throws Exception{
        super.localHistoryButtonAction();
    }

    private void tableRefreshAndInsertLocal(Account account, String message){
        account.setNote(message);
        accountTableView.refresh();
        super.insertLocalHistory(List.of(account));
    }
    public void onStopBtnClick(ActionEvent actionEvent) {
    }
}

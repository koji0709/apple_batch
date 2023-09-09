package com.sgswit.fx;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.AccountLog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;


public class AccountQuerylogPopupController {

    @FXML
    private TableView accountTableView;
    @FXML
    private TableColumn seqLog;
    @FXML
    private TableColumn accountLog;
    @FXML
    private TableColumn pwdLog;
    @FXML
    private TableColumn nameLog;
    @FXML
    private TableColumn stateLog;
    @FXML
    private TableColumn aeraLog;
    @FXML
    private TableColumn statusLog;
    @FXML
    private TableColumn noteLog;
    @FXML
    private TableColumn logtimeLog;

    @FXML
    private TableColumn answer1Log;

    @FXML
    private TableColumn answer2Log;

    @FXML
    private TableColumn answer3Log;

    @FXML
    private Button queryBtn;

    @FXML
    private Label accountLogNum;


    public AccountQuerylogPopupController() {
    }

    @FXML
    private void onQueryBtnClick() throws Exception{
        ObservableList<Account> list = FXCollections.observableArrayList();

        FileReader reader = new FileReader("qlog.txt");

        List<String> logs = reader.readLines();
        int i = 1;
        for(String log : logs){
            AccountLog al = JSONUtil.toBean(log, AccountLog.class);

            Account account = new Account();
            account.setAccount(al.getAccount());
            account.setPwd(al.getPwd());
            account.setSeq(i++);
            account.setAera(al.getAera());
            account.setLogtime(al.getLogtime());
            account.setName(al.getName());
            account.setNote(al.getNote());
            account.setState(al.getState());
            account.setStatus(al.getStatus());
            account.setAnswer1(al.getAnswer1());
            account.setAnswer2(al.getAnswer2());
            account.setAnswer3(al.getAnswer3());

            list.add(account);
        }

        initAccoutTableView();
        this.accountTableView.setItems(list);
        this.accountTableView.refresh();

        this.accountLogNum.setText(String.valueOf(logs.size()));

    }



    private void initAccoutTableView(){
        seqLog.setCellValueFactory(new PropertyValueFactory<Account,Integer>("seq"));
        accountLog.setCellValueFactory(new PropertyValueFactory<Account,String>("account"));
        pwdLog.setCellValueFactory(new PropertyValueFactory<Account,String>("pwd"));
        stateLog.setCellValueFactory(new PropertyValueFactory<Account,String>("state"));
        aeraLog.setCellValueFactory(new PropertyValueFactory<Account,String>("aera"));
        nameLog.setCellValueFactory(new PropertyValueFactory<Account,String>("name"));
        statusLog.setCellValueFactory(new PropertyValueFactory<Account,String>("status"));
        noteLog.setCellValueFactory(new PropertyValueFactory<Account,String>("note"));
        logtimeLog.setCellValueFactory(new PropertyValueFactory<Account,String>("logtime"));
        answer1Log.setCellValueFactory(new PropertyValueFactory<Account,String>("answer1"));
        answer2Log.setCellValueFactory(new PropertyValueFactory<Account,String>("answer2"));
        answer3Log.setCellValueFactory(new PropertyValueFactory<Account,String>("answer3"));
    }
}
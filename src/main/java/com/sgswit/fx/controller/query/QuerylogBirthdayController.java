package com.sgswit.fx.controller.query;


import cn.hutool.core.io.file.FileReader;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.BirtgdayCountryLog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * 生日国家查询记录
 */
public class QuerylogBirthdayController {

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
    private TableColumn balance;
    @FXML
    private TableColumn birthdayLog;
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


    public QuerylogBirthdayController() {
    }

    @FXML
    private void onQueryBtnClick() throws Exception{
        ObservableList<Account> list = FXCollections.observableArrayList();

        FileReader reader = new FileReader("birthdayQuery.txt");

        List<String> logs = reader.readLines();
        int i = 1;
        for(String log : logs){
            BirtgdayCountryLog al = JSONUtil.toBean(log, BirtgdayCountryLog.class);

            Account account = new Account();
            account.setAccount(al.getAccount());
            account.setPwd(al.getPwd());
            account.setSeq(i++);
            account.setLogtime(al.getLogtime());
            account.setBirthday(al.getBirthday());
            account.setState(al.getState());
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
        birthdayLog.setCellValueFactory(new PropertyValueFactory<Account,String>("birthday"));
        logtimeLog.setCellValueFactory(new PropertyValueFactory<Account,String>("logtime"));
        answer1Log.setCellValueFactory(new PropertyValueFactory<Account,String>("answer1"));
        answer2Log.setCellValueFactory(new PropertyValueFactory<Account,String>("answer2"));
        answer3Log.setCellValueFactory(new PropertyValueFactory<Account,String>("answer3"));
    }
}

package com.sgswit.fx.controller.query;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.model.BirtgdayCountryLog;
import com.sgswit.fx.model.Problem;
import com.sgswit.fx.model.QuestionCountryLog;
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
 * 余额查询记录
 */
public class QuerylogQuestionController {

    @FXML
    private TableView accountTableView;
    @FXML
    private TableColumn seqLog;
    @FXML
    private TableColumn accountLog;
    @FXML
    private TableColumn pwdLog;
    @FXML
    private TableColumn noteLog;
    @FXML
    private TableColumn logtimeLog;

    @FXML
    private TableColumn question1Log;

    @FXML
    private TableColumn question2Log;

    @FXML
    private Label accountLogNum;


    public QuerylogQuestionController() {
    }

    @FXML
    private void onQueryBtnClick() throws Exception{
        ObservableList<Problem> list = FXCollections.observableArrayList();

        FileReader reader = new FileReader("questionQuery.txt");

        List<String> logs = reader.readLines();
        int i = 1;
        for(String log : logs){
            QuestionCountryLog al = JSONUtil.toBean(log, QuestionCountryLog.class);

            Problem problem = new Problem();
            problem.setAccount(al.getAccount());
            problem.setPwd(al.getPwd());
            problem.setSeq(i++);
            problem.setLogtime(al.getLogtime());
            problem.setBirthday(al.getBirthday());
            problem.setState(al.getState());
            problem.setNote(al.getNote());
            problem.setProblem2(al.getProblem2());
            problem.setProblem1(al.getProblem1());

            list.add(problem);
        }

        initAccoutTableView();
        this.accountTableView.setItems(list);
        this.accountTableView.refresh();

        this.accountLogNum.setText(String.valueOf(logs.size()));

    }



    private void initAccoutTableView(){
        seqLog.setCellValueFactory(new PropertyValueFactory<Problem,Integer>("seq"));
        accountLog.setCellValueFactory(new PropertyValueFactory<Problem,String>("account"));
        pwdLog.setCellValueFactory(new PropertyValueFactory<Problem,String>("pwd"));
        logtimeLog.setCellValueFactory(new PropertyValueFactory<Problem,String>("logtime"));
        question1Log.setCellValueFactory(new PropertyValueFactory<Problem,String>("problem1"));
        question2Log.setCellValueFactory(new PropertyValueFactory<Problem,String>("problem2"));
        noteLog.setCellValueFactory(new PropertyValueFactory<Problem,String>("note"));
    }
}

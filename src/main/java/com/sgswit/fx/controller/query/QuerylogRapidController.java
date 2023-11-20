package com.sgswit.fx.controller.query;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.model.Problem;
import com.sgswit.fx.model.QuestionCountryLog;
import com.sgswit.fx.model.RapidCountryLog;
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
 * 急速过滤密正记录
 */
public class QuerylogRapidController {

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
    private Label accountLogNum;


    public QuerylogRapidController() {
    }

    @FXML
    private void onQueryBtnClick() throws Exception{
        ObservableList<Problem> list = FXCollections.observableArrayList();

        FileReader reader = new FileReader("rapidQuery.txt");

        List<String> logs = reader.readLines();
        int i = 1;
        for(String log : logs){
            RapidCountryLog al = JSONUtil.toBean(log, RapidCountryLog.class);

            Problem problem = new Problem();
            problem.setAccount(al.getAccount());
            problem.setSeq(i++);
            problem.setPwd(al.getPwd());
            problem.setNote(al.getNote());
            problem.setLogtime(al.getLogtime());

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
        noteLog.setCellValueFactory(new PropertyValueFactory<Problem,String>("note"));
        logtimeLog.setCellValueFactory(new PropertyValueFactory<Problem,String>("logtime"));
    }
}

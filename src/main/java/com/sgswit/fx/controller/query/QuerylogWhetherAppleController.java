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
 * 余额查询记录
 */
public class QuerylogWhetherAppleController {

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
    private TableColumn question1Log;

    @FXML
    private TableColumn question2Log;

    @FXML
    private TableColumn question3Log;

    @FXML
    private Button queryBtn;

    @FXML
    private Label accountLogNum;


    public QuerylogWhetherAppleController() {
    }

    @FXML
    private void onQueryBtnClick() throws Exception{
        ObservableList<Problem> list = FXCollections.observableArrayList();

        FileReader reader = new FileReader("appleIDVerify.txt");

        List<String> logs = reader.readLines();
        int i = 1;
        for(String log : logs){
            RapidCountryLog al = JSONUtil.toBean(log, RapidCountryLog.class);

            Problem problem = new Problem();
            problem.setAccount(al.getAccount());
            problem.setSeq(i++);
            problem.setStatus(al.getStatus());
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
        statusLog.setCellValueFactory(new PropertyValueFactory<Problem,String>("status"));
        logtimeLog.setCellValueFactory(new PropertyValueFactory<Problem,String>("logtime"));
    }
}

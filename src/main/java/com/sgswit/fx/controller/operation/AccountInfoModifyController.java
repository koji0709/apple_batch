package com.sgswit.fx.controller.operation;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.operation.ele.AccountInfoModifyView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.NbUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.util.*;


/**
 * 官网修改资料controller
 */
public class AccountInfoModifyController extends AccountInfoModifyView {

    private ObservableList<Account> accountList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
        initViewData();
        refreshList();
    }

    /**
     * 初始化视图数据
     */
    public void initViewData(){
        List<List<String>> questionList = NbUtil.getQuestionList();
        question1ChoiceBox.setItems(FXCollections.observableArrayList(questionList.get(0)));
        question2ChoiceBox.setItems(FXCollections.observableArrayList(questionList.get(1)));
        question3ChoiceBox.setItems(FXCollections.observableArrayList(questionList.get(2)));

        List<String> languageList = NbUtil.getLanguageList();
        showLangChoiceBox.setItems(FXCollections.observableArrayList(languageList));
    }

    /***************************************** 业务方法 *************************************/

    /**
     * 刷新页面数据
     */
    public void refreshList(){
        // 清空数据
        accountList.clear();

        // 初始化表格数据
        List<Account> list = getList();

        if (!CollectionUtil.isEmpty(list)){
            accountList.addAll(list);
        }
        if (!CollectionUtil.isEmpty(list)){
            for (int i = 0; i < accountList.size(); i++) {
                Account account = accountList.get(i);
                account.setSeq(i+1);
            }
        }
        tableViewDataList.setItems(accountList);
        accountNumLable.setText(accountList.size()+"");
    }

    /**
     * 获取账号列表
     */
    public List<Account> getList(){
        String accountStr = ResourceUtil.readUtf8Str("json/account.json");
        JSONArray jsonArray = JSONUtil.parseArray(accountStr);
        if (CollectionUtil.isEmpty(jsonArray)){
            return Collections.emptyList();
        }

        List<Account> accountList1 = new ArrayList<>();
        for (Object o : jsonArray) {
            JSONObject json = (JSONObject) o;
            Account account = new Account();
            account.setAccount(json.getStr("account"));
            account.setPwd(json.getStr("pwd"));
            accountList1.add(account);
        }

        return accountList1;
    }

    /**
     * 清空账号列表
     */
    public void clearList(){
        accountList.clear();
    }

    /***************************************** 操作 *************************************/

    /**
     * 开始执行按钮点击
     */
    public void executeButtonAction(){
        alert("开始执行按钮点击");
    }

    /**
     * 导入账号按钮点击
     */
    public void importAccountButtonAction(){
        refreshList();
    }

    /**
     * 清空列表按钮点击
     */
    public void clearAccountListButtonAction(){
        clearList();
    }

    /**
     * 本地记录按钮点击
     */
    public void localHistoryButtonAction(){
        alert("本地记录按钮点击");
    }

    /**
     * 导出Excel按钮点击
     */
    public void exportExcelButtonAction(){
        alert("导出Excel按钮点击");
    }

    /**
     * 停止任务按钮点击
     */
    public void stopTaskButtonAction(){
        alert("停止任务按钮点击");
    }

}

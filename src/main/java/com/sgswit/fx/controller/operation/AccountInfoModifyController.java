package com.sgswit.fx.controller.operation;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.base.BaseTableViewController;
import com.sgswit.fx.model.Account;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.net.URL;
import java.util.*;


/**
 * 官网修改资料controller
 */
public class AccountInfoModifyController extends BaseTableViewController {

    private ObservableList<Account> accountList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
        refreshList();
    }

    /***************************************** 业务方法 *************************************/

    /**
     * 刷新页面数据
     */
    public void refreshList(){
        // 初始化表格数据
        List<Account> list = getList();
        if (!CollectionUtil.isEmpty(list)){
            for (int i = 0; i < list.size(); i++) {
                Account account = list.get(i);
                account.setSeq(i+1);
                accountList.add(account);
            }
        }
        tableViewDataList.setItems(accountList);
    }

    /**
     * 获取用户列表
     */
    public List<Account> getList(){
        String accountStr = ResourceUtil.readUtf8Str("account.json");
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
        accountList.clear();
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

    public void alert(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示信息");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



}

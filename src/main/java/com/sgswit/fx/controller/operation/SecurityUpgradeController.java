package com.sgswit.fx.controller.operation;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.operation.viewData.SecurityUpgradeView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 双重认证controller
 */
public class SecurityUpgradeController extends SecurityUpgradeView {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
    }

    @Override
    public void importAccountButtonAction() {
        super.importAccountButtonAction("account-pwd-answer1-answer2-answer3-phone");
        super.bindActions();
    }

    @Override
    public TableCell<Account, Void> buildTableCell(){
        TableCell<Account,Void> cell = new TableCell<>() {
            private final Button btn1 = new Button("输入短信验证码");
            {
                btn1.setOnAction((ActionEvent event) -> {
                    // 按钮所在行账号
                    // Account account = getTableView().getItems().get(getIndex());
                    // todo
                });
            }
            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn1);
                }
            }
        };
        return cell;
    }

    /**
     * 开始执行按钮点击
     */
    public void executeButtonAction(){
        // 校验
        if (accountList.isEmpty()){
            alert("请先导入账号！");
            return;
        }

        // todo

        accountTableView.refresh();
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

package com.sgswit.fx.controller.operation;

import com.sgswit.fx.controller.base.TableView;
import com.sgswit.fx.controller.operation.viewData.SecurityUpgradeView;

import java.net.URL;
import java.util.ResourceBundle;


/**
 * 生成支持PIN controller
 */
public class SupportPinController extends TableView {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
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
}

package com.sgswit.fx.controller.operation;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.controller.base.AppleIdView;
import com.sgswit.fx.controller.base.TableView;
import com.sgswit.fx.controller.operation.viewData.SecurityUpgradeView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.AppleIDUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;


/**
 * 生成支持PIN controller
 */
public class SupportPinController extends AppleIdView {

    @FXML
    private TableColumn pin;

    @FXML
    private TableColumn pinExpir;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
        pin.setCellValueFactory(new PropertyValueFactory<Account,String>("pin"));
        pinExpir.setCellValueFactory(new PropertyValueFactory<Account,String>("pinExpir"));
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

        for (Account account : accountList) {
            HttpResponse supportPinRsp = AppleIDUtil.supportPin(getTokenScnt(account));
            account.setNote("生成失败！");

            String body = supportPinRsp.body();
            if (!StrUtil.isEmpty(body) && JSONUtil.isTypeJSON(body)){
                JSON parse = JSONUtil.parse(body);
                String pin = parse.getByPath("pin", String.class);
                if (!StrUtil.isEmpty(pin)){
                    account.setPin(pin);
                    account.setPinExpir(DateUtil.format(DateUtil.offsetMinute(new Date(), 30),"yyyy-MM-dd HH:mm"));
                    account.setNote("生成成功");
                }
            }
        }
        this.refreshTableView();
    }

}

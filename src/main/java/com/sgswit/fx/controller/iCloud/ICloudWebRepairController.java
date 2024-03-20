package com.sgswit.fx.controller.iCloud;

import com.sgswit.fx.controller.common.ICloudView;
import com.sgswit.fx.model.Account;
import javafx.event.ActionEvent;

import java.util.List;

public class ICloudWebRepairController extends ICloudView<Account> {

    /**
     * 导入账号
     */
    public void importAccountButtonAction(ActionEvent actionEvent){
        openImportAccountView(List.of("account----pwd"),actionEvent);
    }

    @Override
    public void accountHandler(Account account) {
        iCloudLogin(account);
        setAndRefreshNote(account,"修复成功，并已激活iCloud");
    }
}

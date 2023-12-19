package com.sgswit.fx.controller.iCloud;

import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.model.Account;
import java.util.List;

public class ICloudWebRepairController extends CustomTableView<Account> {

    /**
     * 导入账号
     */
    public void importAccountButtonAction(){
        openImportAccountView(List.of("account----pwd"));
    }

    @Override
    public void accountHandler(Account account) {

    }
}

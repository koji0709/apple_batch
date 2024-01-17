package com.sgswit.fx.controller.iCloud;

import com.sgswit.fx.controller.common.ICloudView;
import com.sgswit.fx.model.Account;

import java.util.List;

public class ICloudWebRepairController extends ICloudView<Account> {

    /**
     * 导入账号
     */
    public void importAccountButtonAction(){
        openImportAccountView(List.of("account----pwd"));
    }

    @Override
    public void accountHandler(Account account) {
        iCloudLogin(account);
        setAndRefreshNote(account,"iCloud网页登陆修复成功");
    }

    @Override
    protected void reExecute(Account account) {
        accountHandlerExpand(account);
    }
}

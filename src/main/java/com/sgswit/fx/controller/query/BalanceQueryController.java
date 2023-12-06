package com.sgswit.fx.controller.query;

import com.sgswit.fx.controller.common.TableView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.PurchaseBillUtil;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  密保查询余额
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class BalanceQueryController extends TableView<Account> {


    public void onAccountInputBtnClick(){
        openImportAccountView("account----pwd");
    }


    public void accountHandler(Account account) {
        Map<String, Object> res = PurchaseBillUtil.authenticate(account.getAccount(), account.getPwd());
        if(res.get("code").equals("200")){
            account.setState(res.get("countryName") != null? res.get("countryName").toString():"无");
            account.setBalance(res.get("creditDisplay")!= null? res.get("creditDisplay").toString():"0");
            account.setNote("查询成功");
            accountTableView.refresh();
            insertLocalHistory(List.of(account));
        }else {
            account.setState("无");
            account.setBalance("0");
            account.setNote("账号或密码错误");
            accountTableView.refresh();
            insertLocalHistory(List.of(account));
        }
    }
}

package com.sgswit.fx.controller.query;

import com.sgswit.fx.controller.common.TableView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.PurchaseBillUtil;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  检测是否过检
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class WhetherInspectionController extends TableView<Account> {

    public void onAccountInputBtnClick(){
        openImportAccountView("account----pwd");
    }

    public void accountHandler(Account account) {
        Map<String, Object> res = PurchaseBillUtil.authenticate(account.getAccount(), account.getPwd());
        if(res.get("code").equals("200")){
            account.setInspection(res.get("inspection") != null? res.get("inspection").toString():"已过检");
            account.setPurchasesLast90Count(res.get("purchasesLast90Count") != null? res.get("purchasesLast90Count").toString():"0");
            account.setNote("查询成功");
            accountTableView.refresh();
            insertLocalHistory(List.of(account));
        }else {
            account.setState("账号或密码错误");
            account.setBalance("账号或密码错误");
            account.setNote("查询成功");
            accountTableView.refresh();
            insertLocalHistory(List.of(account));
        }
    }
}

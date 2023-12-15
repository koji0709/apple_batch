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
        openImportAccountView(List.of("account----pwd"));
    }

    @Override
    public void accountHandler(Account account) {
        Map<String, Object> res = PurchaseBillUtil.authenticate(account.getAccount(), account.getPwd());
        if(res.get("code").equals("200")){
            int purchasesLast90Count=0;
            boolean hasInspectionFlag= (boolean) res.get("hasInspectionFlag");
            if(hasInspectionFlag){
                purchasesLast90Count= PurchaseBillUtil.accountPurchasesLast90Count(res);
            }
            account.setInspection(hasInspectionFlag? "已过检":"未过检");
            account.setPurchasesLast90Count(String.valueOf(purchasesLast90Count));
            account.setNote("查询成功");
        }else {
            account.setNote(res.get("msg").toString());
        }
        accountTableView.refresh();
        insertLocalHistory(List.of(account));
    }
}

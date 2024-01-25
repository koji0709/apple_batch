package com.sgswit.fx.controller.query;

import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.common.ServiceException;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.PointUtil;
import com.sgswit.fx.utils.PurchaseBillUtil;
import javafx.scene.input.ContextMenuEvent;

import java.net.URL;
import java.util.*;

/**
 * <p>
 *  密保查询余额
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class BalanceQueryController extends CustomTableView<Account> {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.BALANCE_QUERY.getCode())));
        super.initialize(url, resourceBundle);
    }
    public List<String> menuItem =new ArrayList<>(){{
        add(Constant.RightContextMenu.DELETE.getCode());
        add(Constant.RightContextMenu.REEXECUTE.getCode());
        add(Constant.RightContextMenu.COPY.getCode());
    }};

    public void onContentMenuClick(ContextMenuEvent contextMenuEvent) {
        super.onContentMenuClick(contextMenuEvent,accountTableView,menuItem,new ArrayList<>());
    }


    public void onAccountInputBtnClick(){
        openImportAccountView(List.of("account----pwd"));
    }


    @Override
    public void accountHandler(Account account) {
        account.setHasFinished(false);
        setAndRefreshNote(account,"查询余额中...");
        Map<String, Object> res = PurchaseBillUtil.iTunesAuth(account.getAccount(), account.getPwd());
        account.setHasFinished(true);
        if(res.get("code").equals(Constant.SUCCESS)){
            account.setDataStatus("1");
            boolean hasInspectionFlag= (boolean) res.get("hasInspectionFlag");
            if(!hasInspectionFlag){
                account.setDataStatus("0");
                throw new ServiceException("此 Apple ID 尚未用于 App Store。");
            }
            res= PurchaseBillUtil.accountSummary(res);
            account.setState(res.get("countryName") != null? res.get("countryName").toString():"无");
            account.setBalance(res.get("creditDisplay")!= null? res.get("creditDisplay").toString():"0");
            account.setNote("查询成功");
        }else {
            account.setDataStatus("0");
            throw new ServiceException(res.get("msg").toString());
        }
        accountTableView.refresh();
        insertLocalHistory(List.of(account));
    }
}

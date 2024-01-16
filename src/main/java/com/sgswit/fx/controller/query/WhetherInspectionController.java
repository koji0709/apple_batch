package com.sgswit.fx.controller.query;

import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.PointUtil;
import com.sgswit.fx.utils.PurchaseBillUtil;
import javafx.scene.control.Alert;
import javafx.scene.input.ContextMenuEvent;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * <p>
 *  检测是否过检
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class WhetherInspectionController extends CustomTableView<Account> {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.DETECTION_WHETHER.getCode())));
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

    @Override
    protected void reExecute(Account account) {
        accountHandler(account);
    }

    public void onAccountInputBtnClick(){
        openImportAccountView(List.of("account----pwd"));
    }

    @Override
    public void accountHandler(Account account) {
        //扣除点数
        Map<String,String> pointCost=PointUtil.pointCost(FunctionListEnum.DETECTION_WHETHER.getCode(),PointUtil.out,account.getAccount());
        if(!Constant.SUCCESS.equals(pointCost.get("code"))){
            alertUI(pointCost.get("msg"), Alert.AlertType.ERROR);
            return;
        }
        try {
            account.setNote("登录中");
            accountTableView.refresh();
            Map<String, Object> res = PurchaseBillUtil.iTunesAuth(account.getAccount(), account.getPwd());
            account.setNote("查询是否过检中");
            accountTableView.refresh();
            if(res.get("code").equals(Constant.SUCCESS)){

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
                //返还点数
                PointUtil.pointCost(FunctionListEnum.DETECTION_WHETHER.getCode(),PointUtil.in,account.getAccount());
            }
            accountTableView.refresh();
            insertLocalHistory(List.of(account));
        }catch (Exception e){
            //返还点数
            PointUtil.pointCost(FunctionListEnum.DETECTION_WHETHER.getCode(),PointUtil.in,account.getAccount());
        }
    }
}

package com.sgswit.fx.controller.query;

import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.controller.exception.ServiceException;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.PointUtil;
import com.sgswit.fx.utils.PurchaseBillUtil;
import javafx.event.ActionEvent;
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


    public void onAccountInputBtnClick(ActionEvent actionEvent){
        openImportAccountView(List.of("account----pwd"),actionEvent);
    }

    @Override
    public void accountHandler(Account account) {
        //扣除点数
        Map<String,String> pointCost=PointUtil.pointCost(FunctionListEnum.DETECTION_WHETHER.getCode(),PointUtil.out,account.getAccount());
        if(!Constant.SUCCESS.equals(pointCost.get("code"))){
            alertUI(pointCost.get("msg"), Alert.AlertType.ERROR);
            return;
        }
        account.setHasFinished(false);
        try {
            setAndRefreshNote(account,"登录中...");
            Map<String, Object> res = PurchaseBillUtil.iTunesAuth(account.getAccount(), account.getPwd());
            setAndRefreshNote(account,"查询是否过检中...");
            account.setHasFinished(true);
            if(res.get("code").equals(Constant.SUCCESS)){
                account.setInspection("已过检");
            } else if(Constant.CustomerMessageNotYetUsediTunesStoreCode.equals(res.get("code"))){
                account.setInspection("未过检");
            }else {
                throw new ServiceException(res.get("msg").toString());
            }
            account.setNote("查询成功");
            accountTableView.refresh();
            insertLocalHistory(List.of(account));
        }catch (Exception e){
            account.setHasFinished(true);
            throw e;
        }
    }
}

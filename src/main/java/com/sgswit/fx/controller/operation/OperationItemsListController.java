package com.sgswit.fx.controller.operation;

import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.StageUtil;
import javafx.event.ActionEvent;

/**
 * 修改操作专区list controller
 */
public class OperationItemsListController {

    public void onAccountInfoModifyBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.ACCOUNT_INFO_MODIFY);
    }

    public void onUnlockChangePasswordBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.UNLOCK_CHANGE_PASSWORD);
    }

    public void onUpdateAppleIdBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.UPDATE_APPLE_ID);
    }

    public void onSecurityUpgradeBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.SECURITY_UPGRADE);
    }

    public void onSecurityDowngradeBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.SECURITY_DOWNGRADE);
    }

    public void onSupportPinBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.SUPPORT_PIN);
    }

}

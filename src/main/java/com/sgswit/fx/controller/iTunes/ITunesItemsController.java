package com.sgswit.fx.controller.iTunes;

import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.StageUtil;
import javafx.event.ActionEvent;

/**
 * @author DeZh
 * @title: ITunesItemsController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/1321:39
 */
public class ITunesItemsController {

    public void onCountryModifyBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.COUNTRY_MODIFY);
    }

    public void onDeletePaymentBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.DELETE_PAYMENT);
    }

    public void onGiftCardBlanceCheck(ActionEvent actionEvent){
        StageUtil.show(StageEnum.GIFTCARD_BLANCE);
    }

    public void onCheckAreaBalanceBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.CHECK_AREA_BALANCE);
    }

    public void onCheckBalanceDisabledStatusBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.CHECK_BALANCE_DISABLEDSTATUS);
    }

}

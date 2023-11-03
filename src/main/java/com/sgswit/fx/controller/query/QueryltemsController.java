package com.sgswit.fx.controller.query;

import com.sgswit.fx.enums.StageEnum;
import com.sgswit.fx.utils.StageUtil;
import javafx.event.ActionEvent;

/**
 * <p>
 *
 * </p>
 *
 * @author yanggang
 * @createTime 2023/09/23
 */
public class QueryltemsController {

    public void onBirthdayCountryQueryBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.BIRTHDAY_COUNTRY_QUERY);
    }

    public void onBalanceQueryBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.BALANCE_QUERY);
    }

    public void onCheckGrayBalanceBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.CHECK_GRAY_BALANCE);
    }

    public void onWhetherAppleIdBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.WHETHER_APPLEID);
    }

    public void onRapidFiltrationBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.RAPID_FILTRATION);
    }

    public void onDetectionWhetherBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.DETECTION_WHETHER);
    }

    public void onSecurityQuestionBtnClick(ActionEvent actionEvent){
        StageUtil.show(StageEnum.BIRTHDAY_COUNTRY_QUERY);
    }

}

package com.sgswit.fx.controller.iCloud;

import com.sgswit.fx.controller.common.CustomTableView;
import com.sgswit.fx.enums.FunctionListEnum;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.PointUtil;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * <p>
 *  管理家庭成员
 * </p>
 *
 * @author yanggang
 * @createTime 2023/12/11
 */
public class FamilyMembersController extends CustomTableView<Account> {


    public ComboBox nameGenerationTypeChoiceBox;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pointLabel.setText(String.valueOf(PointUtil.getPointByCode(FunctionListEnum.FAMILY_MEMBERS.getCode())));
        super.initialize(url,resourceBundle);
        start();
    }


    public void start(){
        nameGenerationTypeChoiceBox.getItems().addAll("添加家庭成员","移除家庭成员");
        nameGenerationTypeChoiceBox.setValue("添加家庭成员");

        nameGenerationTypeChoiceBox.valueProperty().addListener((observableValue, o, t1) -> {
            if("添加家庭成员".equals(t1)){
                for (TableColumn<Account, ?> column : accountTableView.getColumns()) {
                    if("memberPwd".equals(column.getId())){
                        column.setVisible(true);
                    }
                    if("cvv".equals(column.getId())){
                        column.setVisible(true);
                    }
                }
            }else {
                for (TableColumn<Account, ?> column : accountTableView.getColumns()) {
                    if("memberPwd".equals(column.getId())){
                        column.setVisible(false);
                    }
                    if("cvv".equals(column.getId())){
                        column.setVisible(false);
                    }
                }
            }
        });
    }


    public void openImportAccountView(ActionEvent actionEvent){
        openImportAccountView(List.of("account----pwd----memberAccount----memberPwd----cvv","account----pwd----memberAccount"),actionEvent);
    }

    @Override
    public void accountHandler(Account account){
        String message = null;
        Object value = nameGenerationTypeChoiceBox.getValue();
        if("添加家庭成员".equals(value)){
            if(StringUtils.isEmpty(account.getMemberPwd())){
                message = "账号导入格式不正确";
                setAndRefreshNote(account,message);
                return;
            }
            message = "";
        }else {
            if(!StringUtils.isEmpty(account.getMemberPwd())){
                message = "账号导入格式不正确";
                setAndRefreshNote(account,message);
                return;
            }
            message = "";
        }
    }
}

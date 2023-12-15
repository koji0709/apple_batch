package com.sgswit.fx.controller.iCloud;

import com.sgswit.fx.controller.common.TableView;
import com.sgswit.fx.model.Account;
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
public class FamilyMembersController extends TableView<Account> {


    public ComboBox nameGenerationTypeChoiceBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url,resourceBundle);
        start();
    }


    public void start(){
        nameGenerationTypeChoiceBox.getItems().addAll("添加家庭成员","移除家庭成员");
        nameGenerationTypeChoiceBox.setValue("添加家庭成员");

        nameGenerationTypeChoiceBox.valueProperty().addListener((observableValue, o, t1) -> {
            if("添加家庭成员".equals(t1)){
                for (TableColumn<Account, ?> column : accountTableView.getColumns()) {
                    if(column.getId().equals("memberPwd")){
                        column.setVisible(true);
                    }
                    if(column.getId().equals("cvv")){
                        column.setVisible(true);
                    }
                }
            }else {
                for (TableColumn<Account, ?> column : accountTableView.getColumns()) {
                    if(column.getId().equals("memberPwd")){
                        column.setVisible(false);
                    }
                    if(column.getId().equals("cvv")){
                        column.setVisible(false);
                    }
                }
            }
        });
    }


    public void openImportAccountView(){
        openImportAccountView(List.of("account----pwd----memberAccount----memberPwd----cvv","account----pwd----memberAccount"));
    }

    @Override
    public void accountHandler(Account account){
        String message = null;
        Object value = nameGenerationTypeChoiceBox.getValue();
        if("添加家庭成员".equals(value)){
            if(StringUtils.isEmpty(account.getMemberPwd())){
                message = "账号导入格式不正确";
                tableRefresh(account,message);
                return;
            }
            message = "";
        }else {
            if(!StringUtils.isEmpty(account.getMemberPwd())){
                message = "账号导入格式不正确";
                tableRefresh(account,message);
                return;
            }
            message = "";
        }
    }

    private void tableRefresh(Account account,String message){
        account.setNote(message);
        accountTableView.refresh();
        insertLocalHistory(List.of(account));
    }
}

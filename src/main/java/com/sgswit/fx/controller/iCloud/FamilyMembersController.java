package com.sgswit.fx.controller.iCloud;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.controller.common.TableView;
import com.sgswit.fx.model.Account;
import com.sgswit.fx.utils.DataUtil;
import com.sgswit.fx.utils.ICloudUtil;
import com.sgswit.fx.utils.PListUtil;
import com.sgswit.fx.utils.StringUtils;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static com.sgswit.fx.utils.ICloudUtil.checkCloudAccount;

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
        openImportAccountView("account----pwd----memberAccount----memberPwd----cvv","account----pwd----memberAccount");
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

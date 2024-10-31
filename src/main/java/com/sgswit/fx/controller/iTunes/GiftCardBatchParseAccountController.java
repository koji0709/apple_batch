package com.sgswit.fx.controller.iTunes;

import cn.hutool.core.util.StrUtil;
import com.sgswit.fx.controller.common.CommonView;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class GiftCardBatchParseAccountController extends CommonView {
    @FXML
    private TextArea textArea1;
    @FXML
    private TextArea textArea2;
    @FXML
    private TextArea textArea3;
    @FXML
    private CheckBox checkbox2;
    @FXML
    private TextField numTextField;
    @FXML
    private TextField delimiterTextField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        numTextField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            // 检查新输入的文本是否为数字
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        }));
        numTextField.setText("5");//默认数量
        delimiterTextField.setText("----");//默认分隔符
    }

    /**
     * 合并整理
     */
    public void mergeAndOrganizeAction() {
        String textArea1Text = textArea1.getText();
        String textArea2Text = textArea2.getText();
        String numText = numTextField.getText();
        String delimiterText = delimiterTextField.getText();
        if (StrUtil.isEmpty(textArea1Text)){
            alert("文本1不能为空");
            return;
        }
        if (StrUtil.isEmpty(textArea2Text)){
            alert("文本2不能为空");
            return;
        }
        if (StrUtil.isEmpty(numText)){
            alert("数量不能为空");
            return;
        }
        if (StrUtil.isEmpty(delimiterText)){
            alert("分隔符不能为空");
            return;
        }

        String[] accountArr = Arrays.stream(textArea1Text.split("\\R"))
                .filter(line -> !line.isEmpty())
                .toArray(String[]::new);
        String[] giftCardArr = Arrays.stream(textArea2Text.split("\\R"))
                .filter(line -> !line.isEmpty())
                .toArray(String[]::new);
        Integer n = Integer.valueOf(numText);

        String result = "";
        int in = 0;
        for (String accountStr : accountArr) {
            for (int i = 0; i < n && in < giftCardArr.length; i++) {
                result += accountStr.trim() + delimiterText + giftCardArr[in].trim() +"\n";
                in++;
            }
        }
        textArea3.setText(result);

        if (checkbox2.isSelected()){
            textArea1.setText("");
            textArea2.setText("");
        }
    }
}

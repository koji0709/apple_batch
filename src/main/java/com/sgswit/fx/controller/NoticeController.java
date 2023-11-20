package com.sgswit.fx.controller;

import cn.hutool.core.io.FileUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ResourceBundle;

/**
 * @author DeZh
 * @title: NoticeController
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/11/1711:49
 */
public class NoticeController implements Initializable {
    @FXML
    public TextArea news;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        File fFile = new File("news.ini");
        if(!fFile.exists()){
            return;
        }
        String text = FileUtil.readString(fFile, Charset.defaultCharset());
        news.setText(text);
    }
}

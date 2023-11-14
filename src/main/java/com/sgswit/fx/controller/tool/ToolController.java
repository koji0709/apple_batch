package com.sgswit.fx.controller.tool;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.swing.DesktopUtil;
import cn.hutool.core.swing.clipboard.ClipboardUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import com.sgswit.fx.controller.base.CommonView;
import com.sgswit.fx.setting.LoginSetting;
import com.sgswit.fx.utils.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 *
 * </p>
 *
 * @author yanggang
 * @createTime 2023/10/31
 */
public class ToolController extends CommonView {


    @FXML
    private TextArea sourceText;
    @FXML
    private TextArea compareText;
    @FXML
    private RadioButton box;
    @FXML
    private Label segmentation;
    @FXML
    private Label repeat;
    @FXML
    private TextField textField;
    @FXML
    private Label current;
    @FXML
    private Label current1;

    @FXML
    private Label zh;
    @FXML
    private Label qq;
    @FXML
    private Label sj;
    @FXML
    private Label dz;
    @FXML
    private Label kh;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Setting loginSetting = LoginSetting.getLoginSetting();
            String s = loginSetting.get("login.info");
            JSONObject object = JSONUtil.parseObj(s);
            zh.setText(object.get("userName").toString());
            qq.setText(object.get("qq").toString());
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            Date  date = format1.parse(object.get("registerTime").toString());
            SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日  hh时mm分ss秒");
            String registerTime = format.format(date);
            sj.setText(registerTime);
            dz.setText(object.get("lastLoginIp").toString());
            kh.setText(object.get("cardNo").toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //文本去重
    @FXML
    public void distinct() {

        List<String> source = List.of(sourceText.getText().split("\n"));
        ArrayList arrayList = new ArrayList(source);
        if (StringUtils.isEmpty(source.get(0))) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("信息: ");
            alert.setHeaderText("");
            alert.setContentText("请输入要去除的源文本！");

            alert.showAndWait();
            return;
        }
        List<String> compare = List.of(compareText.getText().split("\n"));
        ArrayList arrayList1 = new ArrayList(compare);
        if (StringUtils.isEmpty(compare.get(0))) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("信息: ");
            alert.setHeaderText("");
            alert.setContentText("请输入要去除的文本！");

            alert.showAndWait();
            return;
        }
        Integer num = 0;
        for (int i = 0; i < arrayList.size(); i++) {
            for (int j = 0; j < arrayList1.size(); j++) {
                if (arrayList.get(i).equals(arrayList1.get(j))) {
                    arrayList.remove(i);
                    arrayList1.remove(j);
                    num++;
                }
            }
        }
        String join = String.join("\n", arrayList);
        sourceText.setText(join);
        compareText.setText("");
        String s = "";
        if (box.isSelected()) {
            s = "已复制到剪辑板";
            ClipboardUtil.setStr(join);
        } else {
            s = "否（未勾选）";
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("操作详细");
        alert.setHeaderText("");

        alert.setContentText("\n源文本: " + source.size() +
                "\n\n\n重复数: " + num +
                "\n\n\n剩余数: " + arrayList.size() +
                "\n\n\n是否复制到剪切板: " + s +
                "\n\n\n剩余数据在源文本编辑框中，可在右边继续添加要去除的文本!");


        alert.showAndWait();

    }


    //超大文件分割
    @FXML
    public void splitDataToSaveFile() {
        Integer rows = Integer.valueOf(textField.getText());
        String str = segmentation.getText();
        File sourceFile = new File(str);
        if (!"txt".equals(FileUtil.getSuffix(sourceFile))) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("信息: ");
            alert.setHeaderText("");
            alert.setContentText("请拖拽需要分割的txt文件到指定区域后再执行此操作！");

            alert.showAndWait();
            return;
        }
        String targetDirectoryPath = sourceFile.getParent();
        long startTime = System.currentTimeMillis();
        List<File> fileList = new ArrayList<>();
        File targetFile = new File(targetDirectoryPath);
        if (!sourceFile.exists() || rows <= 0 || sourceFile.isDirectory()) {
            return;
        }
        if (targetFile.exists()) {
            if (!targetFile.isDirectory()) {
                return;
            }
        } else {
            targetFile.mkdirs();
        }

        try (FileInputStream fileInputStream = new FileInputStream(sourceFile);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            StringBuilder stringBuilder = new StringBuilder();
            String lineStr;
            int lineNo = 1, fileNum = 1;
            while ((lineStr = bufferedReader.readLine()) != null) {
                stringBuilder.append(lineStr).append("\r\n");
                if (lineNo % rows == 0) {
                    File file = new File(fileUrl() + "分割文本" + "-" + fileNum + "-" + sourceFile.getName());
                    writeFile(stringBuilder.toString(), file);
                    //清空文本
                    stringBuilder.delete(0, stringBuilder.length());
                    fileNum++;
                    fileList.add(file);
                }
                lineNo++;
            }
            if ((lineNo - 1) % rows != 0) {
                File file = new File(fileUrl() + "分割文本" + "-" + fileNum + "-" + sourceFile.getName());
                writeFile(stringBuilder.toString(), file);
                fileList.add(file);
            }
            current.setText("分割完成，共计分割" + fileNum + "份，文件保存在软件同目录的【文件处理】文件夹中！");
            long endTime = System.currentTimeMillis();

        } catch (Exception e) {
            throw new SecurityException("分割文件异常", e);
        }

    }

    private void writeFile(String text, File file) {
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter, 1024)
        ) {
            bufferedWriter.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //文本文件行去重
    @FXML
    public void current() {
        String filePath = repeat.getText();
        Set<String> lines = new HashSet<>();
        File file = new File(filePath);
        if (!"txt".equals(FileUtil.getSuffix(file))) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("信息: ");
            alert.setHeaderText("");
            alert.setContentText("请拖拽需要分割的txt文件到指定区域后再执行此操作！");

            alert.showAndWait();
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line.trim()); // 去除行首尾的空白字符
            }


            FileWriter fileWriter = new FileWriter(fileUrl() + "/去重文件" + file.getName());
            for (String string : lines) {
                if (!string.trim().equals("")) {
                    fileWriter.write(string.trim() + "\r\n");
                }
            }
            current1.setText("去重复完成，文件保存在软件同目录的【文件处理】文件夹中！");
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @FXML
    void getFile(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        //获得文件
        File file = files.get(0);
        //之后的相关操作，获得文件路径等..
        if (!"txt".equals(FileUtil.getSuffix(file))) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("信息: ");
            alert.setHeaderText("");
            alert.setContentText("本工具仅支持txt文件！");

            alert.showAndWait();
        }
        //之后的相关操作，获得文件路径等..
        repeat.setText(file.getPath());
    }

    @FXML
    void getFiles(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        //获得文件
        File file = files.get(0);
        if (!"txt".equals(FileUtil.getSuffix(file))) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("信息: ");
            alert.setHeaderText("");
            alert.setContentText("本工具仅支持txt文件！");

            alert.showAndWait();
        }
        //之后的相关操作，获得文件路径等..
        segmentation.setText(file.getPath());
    }

    @FXML
    void a() throws IOException {
        String s = fileUrl();
        File file1 = new File(s);
        DesktopUtil.open(file1);

    }

    public String fileUrl() throws IOException {
        File file = new File("");
        String s = file.getCanonicalPath() + "\\文本处理\\";
        File file1 = new File(s);
        if(!file1.exists()) {
            file1.mkdir();
        }
        return s;
    }


}

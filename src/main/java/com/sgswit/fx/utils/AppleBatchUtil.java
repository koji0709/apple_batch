package com.sgswit.fx.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class AppleBatchUtil {

    public static final String PATH =  System.getProperty("user.home") + "/.apple_batch/";
    public static final String LOCAL_FILE_STORAGE_PATH =  PATH + "fileSystem";

    public static final String DEFAULT_PRODUCT_URL = "https://www.apple.com/shop/product/MYJ83AM/A/40mm-blue-cloud-sport-loop?fnode=d9f9dcf34fe32a43bcc08f076756af853df81b9fa2d6af31d908c34221bcc80fcdae8d11d989734ea267a66a2dc49f30eac14d3cce9f8851fa5b4fb473eacf5ef101da26704509668ce602386fbccc73";

    public static List<String> PRODUCT_URLS = new ArrayList<>();

    static {
        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File dir2 = new File(LOCAL_FILE_STORAGE_PATH);
        if (!dir2.exists()) {
            dir2.mkdir();
        }
    }

    public static File getNewsFile(){
        return new File(PATH, "news.ini");
    }

    public static void writeNews(String title, String content) throws IOException {
        File newsFile = getNewsFile();
        if(newsFile.exists()){
            newsFile.delete();
        }
        String path = PATH + "news.ini";
        BufferedWriter writer = new BufferedWriter(new FileWriter(path, true));
        try{
            writer.write(title+":");
            writer.write("\n");
            writer.write("\n");
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            writer.flush();
            writer.close();
        }
    }

    public static FileLock getLock() throws IOException {
        FileLock lock = FileChannel.open(
                        Paths.get(PATH, "single_instance.lock"),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE)
                .tryLock();
        return lock;
    }

    public static String randomProductUrl(){
        String productUrl = PropertiesUtil.getConfig("productUrl");
        if (StrUtil.isEmpty(productUrl)) {
            return DEFAULT_PRODUCT_URL;
        }

        if (CollUtil.isEmpty(PRODUCT_URLS)) {
            String responseStr = HttpUtil.get(productUrl);
            JSONObject response = JSONUtil.parseObj(responseStr);
            if (response.getStr("code").equals("200")){
                JSONArray dataList = response.getJSONArray("data");
                if (!CollUtil.isEmpty(dataList)) {
                    dataList.forEach(item->{
                        if (item != null){
                            PRODUCT_URLS.add(((JSONObject)item).getStr("configValue"));
                        }
                    });
                }
            }
        }

        int i = RandomUtil.randomInt(0, PRODUCT_URLS.size());
        String configValue = PRODUCT_URLS.get(i);
        return StrUtil.isEmpty(configValue) ? DEFAULT_PRODUCT_URL : configValue;
    }
}

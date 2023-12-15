package com.sgswit.fx;

import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author DeZh
 * @title: StageManager
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/9/1317:06
 */
public class StageManager {
    /**
     * 场景集合
     */
    private static Map<String, Stage> stageMap = new ConcurrentHashMap<>();

    /**
     * 根据key存放Scene
     * @param key
     * @param stage
     */
    public static void put(String key, Stage stage) {
        if(StringUtils.isEmpty(key)) {
            throw new RuntimeException("key不为空!");
        }
        if(Objects.isNull(stage)) {
            throw new RuntimeException("scene不为空!");
        }
        stageMap.put(key, stage);
    }

    /**
     * 根据key获取Scene
     * @param key
     * @return
     */
    public static Stage getStage(String key) {
        if(StringUtils.isEmpty(key)) {
            throw new RuntimeException("key不为空!");
        }
        return stageMap.get(key);
    }
}

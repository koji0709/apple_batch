package com.sgswit.fx.utils;
import cn.hutool.core.map.MapUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.Map;

/**
 * @author DeZh
 * @title: MapHelper
 * @projectName appleBatch
 * @description: TODO
 * @date 2023/12/2318:05
 */
public class MapUtils extends MapUtil{
    public static ObservableMap<String,Object> mapConvertToObservableMap(Map<String,Object> data){
        ObservableMap<String, Object> observableMap = FXCollections.observableHashMap();
        for (String key:data.keySet()){
            observableMap.put(key,data.get(key));
        }
        return observableMap;
    }
}

package com.sgswit.fx.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SQLiteUtil {

    public static void init() throws Exception {
        File file = new File("xg.sqlite");
        if (!file.exists()){
            List<String> lines = FileUtil.readUtf8Lines("sql/xg.sql");
            StringBuffer sqlText=new StringBuffer();
            for (String line : lines) {
                sqlText.append(line);
            }
            Db.use().executeBatch(sqlText.toString().split(";"));
        }
    }

    /**
     * 查询本地历史记录
     * @return
     */
    public static List selectLocalHistoryList(HashMap params,Class clz){
        List<Entity> localHistoryList;
        String sql = "SELECT * FROM local_history WHERE 1 = 1";
        Object clz_name = params.get("clz_name");
        Object row_json = params.get("row_json");
        Object limit = params.get("limit");

        sql = clz_name != null ? sql + " AND clz_name = '"+clz_name+"'" : sql;
        sql = row_json != null ? sql + " AND row_json LIKE '%"+row_json+"%'" : sql;
        sql = sql + " ORDER BY create_time DESC ";
        sql = limit    != null ? sql + limit : sql;

        try {
            localHistoryList = Db.use().query(sql,params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        List resultList =  new ArrayList<>();
        if (!localHistoryList.isEmpty()){
            for (Entity entity : localHistoryList) {
                String rowJson = entity.getStr("row_json");
                JSONObject rowObj = JSONUtil.parseObj(rowJson);
                Object clzInstance = null;
                try {
                    clzInstance = clz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                for (String colName : rowObj.keySet()) {
                    Object colValue = rowObj.get(colName);
                    if (colValue != null){
                        boolean hasField = ReflectUtil.hasField(clz, colName);
                        if (hasField){
                            ReflectUtil.invoke(
                                    clzInstance
                                    , "set" + colName.substring(0, 1).toUpperCase() + colName.substring(1)
                                    , colValue);
                        }
                    }
                }
                boolean hasCreateTime = ReflectUtil.hasField(clz, "createTime");
                if (hasCreateTime){
                    Long createTime = entity.getLong("create_time");
                    DateTime date = DateUtil.date(createTime);

                    ReflectUtil.invoke(clzInstance, "setCreateTime", DateUtil.format(date,"yyyy-MM-dd HH:mm"));
                }
                resultList.add(clzInstance);
            }
        }
        return resultList;
    }

    /**
     * 清空本地某页面历史记录
     */
    public static int clearLocalHistoryByClzName(String clzName){
        if (StrUtil.isEmpty(clzName)){
            return 0;
        }
        try {
            int count = Db.use().del("local_history", "clz_name", clzName);
            return count;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

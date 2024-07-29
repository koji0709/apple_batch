package com.sgswit.fx.utils.db;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.utils.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class SQLiteUtil {

    public static void init() throws Exception {
        boolean isWindows = SystemUtils.isWindows();
        File file = new File(isWindows ? Constant.WIN_DB_URL : Constant.MAC_DB_URL);
        if (!file.exists()){
            FileUtil.touch(file);
            if (!isWindows){
                //windows可以忽略权限
                // 设置文件权限为 rw-rw-rw-
                Path path = file.toPath();
                Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-rw-rw-");
                Files.setPosixFilePermissions(path, perms);
            }
            // 获取当前类加载器对象
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            // 通过类加载器获取指定路径下的输入流
            InputStream inputStream = loader.getResourceAsStream("sql/xg.sql");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuffer sqlText=new StringBuffer();
            while ((line = reader.readLine()) != null) {
                sqlText.append(line);
            }
            reader.close();
            Db.use(DataSourceFactory.getDataSource()).executeBatch(sqlText.toString().split(";"));
        }else{
           String sql= "SELECT name FROM sqlite_master WHERE type='table' AND name='proxy_ip_info';";
            Entity entity= Db.use(DataSourceFactory.getDataSource()).queryOne(sql);
            if(null==entity){
                Db.use(DataSourceFactory.getDataSource()).execute("CREATE TABLE \"proxy_ip_info\" (\n" +
                        "  \"id\" text NOT NULL,\n" +
                        "  \"ip\" text NOT NULL,\n" +
                        "  \"port\" integer NOT NULL,\n" +
                        "  \"input_time\" integer NOT NULL,\n" +
                        "  \"last_update_time\" integer NOT NULL,\n" +
                        "  \"expiration_time\" integer NOT NULL,\n" +
                        "  \"username\" TEXT,\n" +
                        "  \"pwd\" TEXT,\n" +
                        "  \"protocol_type\" TEXT,\n" +
                        "  PRIMARY KEY (\"id\")\n" +
                        ");");
            }
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
            localHistoryList = Db.use(DataSourceFactory.getDataSource()).query(sql,params);
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
            int count = Db.use(DataSourceFactory.getDataSource()).del("local_history", "clz_name", clzName);
            return count;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.sgswit.fx.utils.db;

import cn.hutool.db.ds.simple.SimpleDataSource;
import com.sgswit.fx.constant.Constant;
import com.sgswit.fx.utils.SystemUtils;

public class DataSourceFactory {
    private static SimpleDataSource dataSource;

    static {
        String prefix = "jdbc:sqlite:";
        boolean isWindows = SystemUtils.isWindows();
        String url = isWindows ? prefix + Constant.WIN_DB_URL : prefix + Constant.MAC_DB_URL;
        // 初始化数据源
        dataSource = new SimpleDataSource(url,"","");
    }

    public static SimpleDataSource getDataSource() {
        return dataSource;
    }
}
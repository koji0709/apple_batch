package com.sgswit.fx.utils;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class DbTest {

    public static void main(String[] args) throws SQLException {
        List<Entity> entityList = Db.use().query("SELECT * FROM apple_id_base_info");
        System.err.println(entityList);
    }

}

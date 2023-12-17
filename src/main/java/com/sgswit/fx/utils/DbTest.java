package com.sgswit.fx.utils;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;

import java.sql.SQLException;
import java.util.List;

/**
 *
 */
public class DbTest {

    public static void main(String[] args) throws SQLException {
        List<Entity> entityList = Db.use().query("SELECT * FROM apple_id_base_info");
        System.err.println(entityList);
    }

}

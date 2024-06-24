package com.sgswit.fx.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import com.sgswit.fx.utils.db.DataSourceFactory;

import java.util.*;

public class LocalhistoryTask {

    public static LinkedList<Entity> entityList = new LinkedList<>();

    static {
        CronUtil.schedule("*/5 * * * * *", (Task) () -> {
            while (!CollUtil.isEmpty(entityList)){
                try {
                    Entity entity = entityList.getFirst();
                    Db.use(DataSourceFactory.getDataSource()).insert(entity);
                    entityList.remove(entity);
                    Thread.sleep(1000L);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

}

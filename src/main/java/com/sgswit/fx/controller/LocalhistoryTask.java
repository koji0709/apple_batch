package com.sgswit.fx.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.utils.LoggerManger;
import com.sgswit.fx.utils.db.DataSourceFactory;

import java.util.LinkedList;

public class LocalhistoryTask {

    public static LinkedList<Entity> entityList = new LinkedList<>();

    static {
        CronUtil.schedule("*/5 * * * * *", (Task) () -> {
            while (!CollUtil.isEmpty(entityList)){
                Entity entity = entityList.getFirst();
                try {
                    LoggerManger.info("【日志定时任务】 entity:" + JSONUtil.toJsonStr(entity));
                    Db.use(DataSourceFactory.getDataSource()).insert(entity);
                    ThreadUtil.sleep(1000);
                    entityList.remove(entity);
                } catch (Exception e) {
                    e.printStackTrace();
                    LoggerManger.info("【日志定时任务】 插入失败 data:" + JSONUtil.toJsonStr(entity),e);
                }
            }
        });
        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

}

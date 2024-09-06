package com.sgswit.fx.utils.proxy;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.sgswit.fx.utils.PropertiesUtil;
import com.sgswit.fx.utils.db.DataSourceFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author DeZh
 * @title: ApiProxy
 * @projectName appleBatchService
 * @description: TODO
 * @date 2024/5/711:28
 */
public class ApiProxyUtil {
    private static final ReentrantLock lock = new ReentrantLock();
    /**
    　* 通过白名单的形式访问获取
      * @param
    　* @return void
    　* @throws
    　* @author DeZh
    　* @date 2024/5/7 14:10
    */
    private static Entity getIps() {
        if(lock.isLocked()){
            return null;
        }
        lock.lock();
        try {
            String proxyUrl= PropertiesUtil.getConfig("proxyUrl");
            HashMap<String, List<String>> headers = new HashMap<>(10);
            HttpResponse result = HttpRequest.get(proxyUrl)
                    .timeout(10000)
                    .header(headers)
                    .execute();
            JSON jsonObject=JSONUtil.parse(result.body());
            String code= jsonObject.getByPath("code",String.class);
            delete();
            long nowDate=System.currentTimeMillis();
            List<Entity> insertList = new ArrayList<>();
            if("0".equals(code)){
                List<Map<String,Object>> dataList=jsonObject.getByPath("data",List.class);
                for(Map<String,Object> map:dataList){
                    Entity entity = new Entity();
                    entity.setTableName("proxy_ip_info");
                    entity.set("id", map.get("id"));
                    entity.set("ip", map.get("ip"));
                    entity.set("port", map.get("port"));
                    entity.set("username", map.get("username"));
                    entity.set("pwd", map.get("pwd"));
                    entity.set("last_update_time", 0);
                    entity.set("input_time", nowDate);
                    entity.set("expiration_time", map.get("expiration_time"));
                    entity.set("protocol_type", map.get("protocol_type"));
                    insertList.add(entity);
                }
                Db.use(DataSourceFactory.getDataSource()).insert(insertList);
            }
            if(insertList.size()>0){
                return insertList.get(0);
            }else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return null;
    }

    protected static void delete(){
        try{
            Db.use(DataSourceFactory.getDataSource()).execute("DELETE from proxy_ip_info  WHERE expiration_time<?", System.currentTimeMillis());
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    /**
    　* 随机获取一条未过期的数据,并修改使用日期
      * @param
    　* @return cn.hutool.db.Entity
    　* @throws
    　* @author DeZh
    　* @date 2024/5/8 19:57
    */
    private static Entity getRandomOne(){
        AtomicReference<Entity> one=new AtomicReference<>();
        try{
            Db.use(DataSourceFactory.getDataSource()).tx(db->{
                delete();
                long currentTimeMillis=System.currentTimeMillis();
                Entity queryOne= Db.use(DataSourceFactory.getDataSource()).queryOne("SELECT * FROM proxy_ip_info where (expiration_time-3000)>? and (last_update_time+5*1000<?) ORDER BY last_update_time LIMIT 1",currentTimeMillis,currentTimeMillis);
                if(null!=queryOne){
                    one.set(queryOne);
                    queryOne.set("last_update_time", System.currentTimeMillis());
                    Entity where=new Entity();
                    where.set("id",queryOne.getStr("id"));
                    Db.use(DataSourceFactory.getDataSource()).update(queryOne,where);
                }else{
                    return;
                }
            });
        }catch (SQLException e){
            e.printStackTrace();
        }
        return one.get();
    }
    /**
     　* 随机获取一个IP
     * @param
    　* @return cn.hutool.db.Entity
    　* @throws
    　* @author DeZh
    　* @date 2024/5/8 19:57
     */
    protected static Entity getRandomIp(){
        Entity randomOne=getRandomOne();
        if(null==randomOne){
            randomOne= getIps();
        }
        return randomOne;
    }

}


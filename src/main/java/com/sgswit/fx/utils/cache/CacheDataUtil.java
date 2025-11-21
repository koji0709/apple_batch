package com.sgswit.fx.utils.cache;

import com.sgswit.fx.controller.iTunes.vo.giftCard.SessionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author DeZh
 * @date 2025/11/19 12:14
 * 数据缓存
 */
public class CacheDataUtil {
    private static final Queue<SessionManager.SessionInfo> queryCardSessions = new ConcurrentLinkedQueue<>();

    public static Queue<SessionManager.SessionInfo> getQueryCardSessions() {
        return queryCardSessions;
    }
    public static void saveSessions(Queue<SessionManager.SessionInfo> sessionInfos) {
        queryCardSessions.clear();
        if (sessionInfos != null) {
            queryCardSessions.addAll(sessionInfos);
        }
    }
}

package com.sgswit.fx.controller.iTunes.vo.giftCard;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Session管理器
 */
public  class SessionManager {
    // 常量配置
    private static final Queue<SessionInfo> sessions = new ConcurrentLinkedQueue<>();
    /**
     * session 使用间隔
     */
    private static final long SESSION_COOLDOWN_MS = 500;
    /**
     * 会话时长 30分钟
     */
    private static final long SESSION_MM = 30;
    public void add(SessionInfo session) {
        sessions.offer(session);
        System.out.println("[Session管理] ✅ 新增Session: " + session.getTxtAccount());
    }

    public static Queue<SessionInfo> getSessions() {
        return sessions;
    }
    public static void setSessions(Queue<SessionInfo> sessionsCache ) {
        sessions.addAll(sessionsCache);
    }

    public SessionInfo acquireAvailableSession(String countryCode) {
        if (sessions.isEmpty()) {
            return null;
        }
        long currentTime = System.currentTimeMillis();

        if (sessions.isEmpty()) {
            return null;
        }
        SessionInfo selected = null;
        int attempts = 0;
        // 最多尝试队列大小次数
        int maxAttempts = sessions.size();
        // 轮询查找符合条件的会话
        while (attempts < maxAttempts && selected == null) {
            SessionInfo candidate = sessions.poll();
            if (candidate != null) {
                if ((currentTime - candidate.lastUsedTime) >= SESSION_COOLDOWN_MS
                        && candidate.getCountryCode().equals(countryCode)) {
                    candidate.setLastUsedTime(currentTime);
                    selected = candidate;
                } else {
                    attempts++;
                }
                sessions.offer(candidate);
            }
        }
        if (selected == null) {
            System.out.println("[轮询策略] ⚠️ 未找到符合条件的会话，尝试次数: " + attempts);
        }else{
            System.out.println("[轮询策略] 找到符合条件的会话 " + selected.txtAccount);
        }
        return selected;
    }

    public int getSizeByCountry(String countryCode) {
        if (sessions.isEmpty()) {
            return 0;
        }
        return (int) sessions.stream().filter(s -> countryCode.equals(s.getCountryCode())).count();
    }

    /**
     * 更新session
     * @param sessionId
     */
    public void updateSession(String sessionId) {
        updateSession(sessions,sessionId);
    }
    public void updateSession(Queue<SessionInfo> sessionsQueue) {
        updateSession(sessions,null);
    }
    public void updateSession(Queue<SessionInfo> sessionsQueue,String sessionId) {
        // 最多尝试队列大小次数
        int maxAttempts = sessionsQueue.size();
        int attempts = 0;
        long currentTime = System.currentTimeMillis();
        // 轮询查找符合条件的会话
        while (attempts < maxAttempts) {
            SessionInfo candidate = sessionsQueue.poll();
            if (candidate != null) {
                Long lastUsed = candidate.lastUsedTime;
                if(StrUtil.isNotEmpty(sessionId)){
                    if ((currentTime - lastUsed) >= SESSION_MM*60*1000 && candidate.getId().equals(sessionId)) {
                        break;
                    } else {
                        attempts++;
                    }
                }else {
                    if ((currentTime - lastUsed) >= SESSION_MM*60*1000) {

                    } else {
                        attempts++;
                    }
                }

                sessionsQueue.offer(candidate);
            }
        }

    }


    public static class SessionInfo {
        private final String id;
        private final String countryCode;
        private final Map<String, Object> cookies;
        private final long createTime;
        private long lastUsedTime;
        private final String txtAccount;
        public SessionInfo(String countryCode, Map<String, Object> cookies, String txtAccount) {
            this.txtAccount = txtAccount;
            this.id = IdUtil.simpleUUID();
            this.countryCode = countryCode;
            this.cookies = new ConcurrentHashMap<>(cookies);
            this.createTime = System.currentTimeMillis();
            this.lastUsedTime = System.currentTimeMillis();
        }

        public long getLastUsedTime() {
            return lastUsedTime;
        }

        public void setLastUsedTime(long lastUsedTime) {
            this.lastUsedTime = lastUsedTime;
        }

        public String getTxtAccount() {
            return txtAccount;
        }
        public String getId() { return id; }
        public String getCountryCode() { return countryCode; }
        public Map<String, Object> getCookies() { return cookies; }
        public long getCreateTime() { return createTime; }
    }
}
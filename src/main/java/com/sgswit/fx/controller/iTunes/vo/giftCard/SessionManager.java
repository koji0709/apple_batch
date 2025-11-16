package com.sgswit.fx.controller.iTunes.vo.giftCard;

import cn.hutool.core.util.IdUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session管理器
 */
public  class SessionManager {
    // 常量配置
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> lastUsedTime = new ConcurrentHashMap<>();
    private static final long SESSION_COOLDOWN_MS = 500;
    public void add(SessionInfo session) {
        sessions.put(session.getId(), session);
        lastUsedTime.put(session.getId(), System.currentTimeMillis());
        System.out.println("[Session管理] ✅ 新增Session: " + session.getId());
    }

    public SessionInfo acquireAvailableSession(String countryCode) {
        long currentTime = System.currentTimeMillis();

        for (SessionInfo session : sessions.values()) {
            Long lastUsed = lastUsedTime.get(session.getId());
            if (lastUsed != null && (currentTime - lastUsed) >= SESSION_COOLDOWN_MS && session.getCountryCode().equals(countryCode)) {
                lastUsedTime.put(session.getId(), currentTime);
                return session;
            }
        }
        return null;
    }

    public int size() {
        return sessions.size();
    }

    public boolean hasAvailableSession() {
        return sessions.values().stream()
                .anyMatch(session -> {
                    Long lastUsed = lastUsedTime.get(session.getId());
                    return lastUsed != null &&
                            (System.currentTimeMillis() - lastUsed) >= SESSION_COOLDOWN_MS;
                });
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    public static class SessionInfo {
        private final String id;
        private final String countryCode;
        private final Map<String, Object> cookies;
        private final long createTime;

        public SessionInfo(String countryCode, Map<String, Object> cookies) {
            this.id = IdUtil.simpleUUID();
            this.countryCode = countryCode;
            this.cookies = new ConcurrentHashMap<>(cookies);
            this.createTime = System.currentTimeMillis();
        }

        public String getId() { return id; }
        public String getCountryCode() { return countryCode; }
        public Map<String, Object> getCookies() { return cookies; }
        public long getCreateTime() { return createTime; }
    }
}
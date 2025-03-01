package com.swyp.saratang.session;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.stereotype.Component;

@Component
public class SessionListener implements HttpSessionListener {
    // 세션 ID와 생성 시간을 저장하는 맵
    private static final Map<String, LocalDateTime> activeSessions = new ConcurrentHashMap<>();

    // 세션 생성될 때 기록 추가
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        String sessionId = event.getSession().getId();
        LocalDateTime createdTime = LocalDateTime.now(); // 현재 시간 기록
        activeSessions.put(sessionId, createdTime);

        System.out.println("New session created: " + sessionId + " at " + createdTime);
    }

    // 세션이 소멸될 때 목록에서 제거
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        String sessionId = event.getSession().getId();
        activeSessions.remove(sessionId);
        System.out.println("Session destroyed: " + sessionId);
    }

    // 세션 정보를 조회하는 메서드 (필요시 활용)
    public static Map<String, LocalDateTime> getActiveSessions() {
        return activeSessions;
    }
}
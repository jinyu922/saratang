package com.swyp.saratang.session;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.swyp.saratang.model.UserDTO;

@Component
public class SessionManager {
    
    private final Map<String, UserDTO> sessionStore = new ConcurrentHashMap<>();

    /**
     * ✅ 세션 저장
     */
    public void setSession(String sessionId, UserDTO user) {
        sessionStore.put(sessionId, user);
    }

    /**
     * ✅ 세션 조회
     */
    public UserDTO getSession(String sessionId) {
        return sessionStore.get(sessionId);
    }

    /**
     * ✅ 세션 삭제 (로그아웃)
     */
    public void removeSession(String sessionId) {
        sessionStore.remove(sessionId);
    }
}
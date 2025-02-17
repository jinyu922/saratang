package com.swyp.saratang.session;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.swyp.saratang.model.UserDTO;

@Component
public class SessionManager {

    private final Map<String, UserDTO> sessionStore = new HashMap<>();

    /**
     * 세션 저장
     * 
     * @param sessionId 세션 ID
     * @param user 저장할 사용자 정보
     */
    public void setSession(String sessionId, UserDTO user) {
        sessionStore.put(sessionId, user);
    }

    /**
     * 세션 조회
     * 
     * @param sessionId 세션 ID
     * @return 저장된 사용자 정보
     */
    public UserDTO getSession(String sessionId) {
        return sessionStore.get(sessionId);
    }

    /**
     * 세션 삭제
     * 
     * @param sessionId 세션 ID
     */
    public void removeSession(String sessionId) {
        sessionStore.remove(sessionId);
    }
}

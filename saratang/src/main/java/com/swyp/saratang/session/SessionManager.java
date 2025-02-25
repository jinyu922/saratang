package com.swyp.saratang.session;

import java.util.HashMap;
import java.util.Map;

import java.util.zip.ZipEntry;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import org.springframework.aop.ThrowsAdvice;
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
    
    //현재 세션에서 유저 고유 아이디 가져오기, 
    //중요! 유저 아이디가 매개변수로 입력되어도 세션에 유저 정보가 있다면 세션정보를 우선으로 합니다
    //보안상 중요한 부분, 테스트 끝난 시점에선 더 이상 유저id를 직접 입력받으면 안됨
    public Integer getUserIdFromSession(HttpSession session,Integer requestUserId) throws SessionNotFoundException{
    	String sessionId = session.getId();  // 현재 세션 ID 가져오기
    	System.out.println(sessionId);
    	
        UserDTO sessionUser = getSession(sessionId); // 세션 ID로 유저 정보 조회
    	
        if (sessionUser != null) {
            return sessionUser.getId();  // 유저 정보가 있으면 해당 ID 반환
        }

        if (requestUserId != null) {
            System.out.println("SessionManager.java getuserIdFromSession() : 세션 정보가 없어 입력받은 유저 아이디로 매핑");
            return requestUserId; 
        }
        throw new SessionNotFoundException("해당 API 요청엔 유저 고유id가 필요함 : 로그인된 세션 정보가 없거나 세션이 만료되었는지 확인하세요. 또는 요청 파라미터에 유저 ID를 직접 입력하세요");
    }
    
    public class SessionNotFoundException extends Exception {
        public SessionNotFoundException(String string) {
            super(string);
        }
    }
}


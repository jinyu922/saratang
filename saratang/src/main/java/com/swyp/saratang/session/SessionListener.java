package com.swyp.saratang.session;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.stereotype.Component;

@Component
public class SessionListener implements HttpSessionListener {
    private static final Set<String> activeSessions = 
        Collections.newSetFromMap(new ConcurrentHashMap<>());

    //세션 생성될때 리스트에 기록 추가
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        activeSessions.add(event.getSession().getId());
        System.out.println("New session created: " + event.getSession().getId());
    }

    //세션 삭제될때 리스트에 기록 추가
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        activeSessions.remove(event.getSession().getId());
        System.out.println("Session destroyed: " + event.getSession().getId());
    }

    //현재 서버의 세션 리스트 가져오기
    public static Set<String> getActiveSessions() {
        return activeSessions;
    }
}
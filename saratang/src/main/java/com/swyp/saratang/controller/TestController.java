package com.swyp.saratang.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.session.SessionManager;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class TestController {
	@Autowired
    private SessionManager sessionManager;
	
    public ApiResponseDTO<Integer> getUserIdFromSession(HttpSession session){
        Integer userId = null;
        try {
            String sessionId = session.getId();  // 현재 세션 ID 가져오기
            UserDTO sessionUser = sessionManager.getSession(sessionId); // SessionManager에서 유저 정보 조회
            
            if (sessionUser != null) {
                userId = sessionUser.getId();  // 유저 정보가 있으면 id 추출
            }
        } catch (Exception e) {
            return new ApiResponseDTO<>(500, "세션 조회 중 오류발생: " + e.getMessage(), null );
        }
        
        // userId가 null인 경우 처리
        if (userId == null) {
            return new ApiResponseDTO<>(500, "세션에서 사용자 ID를 조회할 수 없습니다.", null );
        }
        
        return new ApiResponseDTO<>(200, "유저정보를 성공적으로 조회", userId );
    }

}

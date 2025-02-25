package com.swyp.saratang.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.session.SessionManager;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class TestController {
	@Autowired
    private SessionManager sessionManager;
	
	@Operation(summary = "현재 세션에서 사용자의 user 테이블 고유 id를 찾습니다")
	@GetMapping("/test")
    public ApiResponseDTO<Integer> getUserIdFromSession(HttpSession session,
    		@Parameter(description = "요청유저 고유id, 로그인 세션 있으면 입력하지 않아도 됩니다")@RequestParam(required = false) Integer requestUserId
    		){
		
        Integer userId = null;
        try {
            userId=sessionManager.getUserIdFromSession(session, requestUserId);
        } catch (Exception e) {
            return new ApiResponseDTO<>(500,e.getMessage(), null );
        }
        
        return new ApiResponseDTO<>(200, "유저정보를 성공적으로 조회", userId );
    }

}

package com.swyp.saratang.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.config.JwtAuthUtil;
import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.service.JudgeService;
import com.swyp.saratang.session.SessionManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
public class JudgeController {
	@Autowired
	private JudgeService judgeService;
	
	@Autowired
	private SessionManager sessionManager;
	
	@Autowired
    private JwtAuthUtil jwtAuthUtil; // JWT 유틸리티 주입
	
	@Operation(summary = "게시글에 대한 판단을 내립니다", description = "judgementType은 positive 또는 negative 입니다")
    @PostMapping("/judge")
    public ApiResponseDTO<?> judge(
    	@Parameter(description = "요청유저 고유id, 로그인 세션 있으면 입력하지 않아도 됩니다")@RequestParam(required = false) Integer requestUserId,
        @RequestParam int postId,
    	@Parameter(name="judgementType" ,schema=@Schema(allowableValues = { "positive", "negative" },defaultValue = "positive"))
        @RequestParam String judgementType,
        @RequestHeader(value = "Authorization", required = false) String token,
        HttpServletRequest request
    ) {
        
        // 기본적인 파라미터 검증
        if (!"positive".equals(judgementType) && !"negative".equals(judgementType)) {
            return new ApiResponseDTO<>(400, "judgementType은 positive 혹은 negative 중 하나입니다.", null);
        }
        
        // JWT 토큰에서 userId 추출
        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }
        
        try {
            judgeService.addJudgement(Integer.parseInt(userId), postId, judgementType);
        } catch (IllegalStateException e) {
            return new ApiResponseDTO<>(400, "유저는 이미 이 게시글에 대해 판단을 내렸습니다."+e.getMessage(), null);
        } catch (Exception e) {
        	return new ApiResponseDTO<>(500, "DB 데이터 요청 중 예기치 못한 오류가 발생했습니다."+e.getMessage(), null);
        }
        return new ApiResponseDTO<>(200, "성공적으로 해당 게시물에 대한 판단을 내렸습니다.", null);
    }
}
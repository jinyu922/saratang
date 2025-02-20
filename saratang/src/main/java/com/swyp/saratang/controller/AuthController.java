package com.swyp.saratang.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.service.AuthService;
import com.swyp.saratang.session.SessionManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth API", description = "SNS 로그인 및 로그아웃 API")
public class AuthController {
	
	private static final Logger logger = LogManager.getLogger(AuthController.class); 
	
    @Autowired
    private AuthService authService;
    
    @Autowired
    private SessionManager sessionManager;
    
    

    @PostMapping("/{provider}/login")
    @Operation(summary = "SNS 로그인", description = "네이버 또는 카카오로 로그인 provider 값은 naver or kakao")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiResponse(responseCode = "201", description = "신규가입, 프로필 입력필요")
    @ApiResponse(responseCode = "400", description = "이미 존재하는 이메일")
    @ApiResponse(responseCode = "410", description = "토큰 요청 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    public ApiResponseDTO<UserDTO> snsLogin(
            @Parameter(description = "로그인 제공자 (naver 또는 kakao)") @PathVariable String provider,
            @RequestBody Map<String, Object> userInfo,
            HttpSession session) {

        logger.info("SNS 로그인 요청 - Provider: {}, Session ID: {}", provider, session.getId());

        try {
            UserDTO user = authService.snsLogin(provider, userInfo, session.getId());

            if (!user.getProfileYn()) {
                logger.info("프로필 입력 필요 - Social ID: {}", user.getSocialId());
                return new ApiResponseDTO<>(201, "신규가입, 프로필 입력필요", user);
            }

            logger.info("SNS 로그인 성공 - User: {}", user.getEmail());
            return new ApiResponseDTO<>(200, "로그인 성공", user);

        } catch (IllegalArgumentException e) {
            logger.warn("이미 존재하는 이메일: {}", e.getMessage());
            return new ApiResponseDTO<>(400, "이미 존재하는 이메일입니다.", null);

        } catch (RuntimeException e) {
            logger.error("SNS 로그인 중 오류 발생: {}", e.getMessage(), e);
            return new ApiResponseDTO<>(410, "토큰 요청 중 오류", null);

        } catch (Exception e) {
            logger.error("서버 오류 발생: {}", e.getMessage(), e);
            return new ApiResponseDTO<>(500, "서버 오류 발생", null);
        }
    }


    /**
     * 로그아웃 API
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃")
    @ApiResponse(responseCode = "200", description = "로그아웃 완료")
    @ApiResponse(responseCode = "401", description = "이미 로그아웃된 상태")
    public ApiResponseDTO<String> logout(HttpSession session) {
        UserDTO sessionUser = sessionManager.getSession(session.getId());

        if (sessionUser == null) {
            return new ApiResponseDTO<>(401, "이미 로그아웃된 상태입니다.", null);
        }

        sessionManager.removeSession(session.getId());
        return new ApiResponseDTO<>(200, "로그아웃 완료", "success");
    }
}

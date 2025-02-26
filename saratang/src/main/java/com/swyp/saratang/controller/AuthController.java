package com.swyp.saratang.controller;

import io.swagger.v3.oas.annotations.Operation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.service.AuthService;
import com.swyp.saratang.service.UserService;
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
    

    @Value("${oauth.success.redirect.url}")
    private String oauthSuccessRedirectUrl;
    
    @Autowired
    private UserService userService;
    /**
     * 프론트엔드에서 API 하나만 호출하면, 백엔드에서 로그인 페이지로 자동 이동
     */
    @GetMapping("/login")
    @Operation(summary = "OAuth 로그인 요청", description = "네이버 또는 카카오 로그인 페이지로 리디렉트")
    @ApiResponse(responseCode = "200", description = "로그인성공")
    @ApiResponse(responseCode = "201", description = "프로필 입력필요")
    @ApiResponse(responseCode = "402", description = "다른 provider에 존재하는 이메일")
    @ApiResponse(responseCode = "410", description = "탈퇴 계정이 있는 회원")
    @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    public ResponseEntity<ApiResponseDTO<String>> redirectToOAuthProvider(
            @RequestParam("provider") String provider,
            HttpServletResponse response) {

        if (!"naver".equals(provider) && !"kakao".equals(provider)) {
            logger.warn("잘못된 provider 요청: {}", provider);
            return ResponseEntity.status(400)
                    .body(new ApiResponseDTO<>(400, "지원되지 않는 provider 값입니다. (naver 또는 kakao만 허용)", null));
        }

        try {
            String authUrl = authService.getAuthUrl(provider);
            logger.info("OAuth 로그인 요청 - Provider: {}, Redirecting to: {}", provider, authUrl);

            response.sendRedirect(authUrl);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "로그인 페이지로 리디렉트 완료", authUrl));

        } catch (Exception e) {
            logger.error("OAuth 로그인 URL 생성 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "OAuth 로그인 URL 생성 중 오류 발생", null));
        }
    }

    @GetMapping("/callback/kakao")
    public void kakaoLoginCallback(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            @RequestParam(value = "code", required = false) String code,
            HttpSession session,
            HttpServletResponse response) throws IOException {
        handleLoginCallback("kakao", error, errorDescription, code, session, response);
    }

    @GetMapping("/callback/naver")
    public void naverLoginCallback(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            @RequestParam(value = "code", required = false) String code,
            HttpSession session,
            HttpServletResponse response) throws IOException {
        handleLoginCallback("naver", error, errorDescription, code, session, response);
    }

    private ResponseEntity<Void> handleLoginCallback(
            String provider,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            @RequestParam(value = "code", required = false) String code,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        if ("access_denied".equals(error)) {
            logger.warn("사용자가 {} 로그인 취소 - 이유: {}", provider, errorDescription);
            response.sendRedirect(oauthSuccessRedirectUrl + "?error=access_denied&status=400");
            return ResponseEntity.status(400).build();
        }

        try {
            UserDTO user = authService.processOAuthLogin(provider, code, session.getId());
            
            
            // 세션 유지
            sessionManager.setSession(session.getId(), user);
            
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            
            // ✅ 개발 환경 감지 (localhost 또는 127.0.0.1)
            boolean isLocalEnv = request.getServerName().equals("localhost") || request.getServerName().startsWith("127.0.0.1");

            // ✅ 쿠키 설정 (SameSite=None 유지, 개발 환경에서는 Secure 제거)
            String cookie = "JSESSIONID=" + session.getId() + "; Path=/; HttpOnly; SameSite=None";
            
            if (!isLocalEnv) { 
                cookie += "; Secure"; // 운영 환경(HTTPS)에서는 Secure 속성 추가
            }

            // ✅ 쿠키 설정 적용
            response.addHeader("Set-Cookie", cookie);
            
           
            
            // 프로필이 미완성된 경우 (201 응답)
            if (!user.getProfileYn()) {
                response.sendRedirect(oauthSuccessRedirectUrl + "?status=201");
                return ResponseEntity.status(201).build();
            }

            // 로그인 성공 후 OAuth 성공 리디렉트 URL 사용
            response.sendRedirect(oauthSuccessRedirectUrl + "?status=200");
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("이미 존재하는 이메일")) {
                response.sendRedirect(oauthSuccessRedirectUrl + "?error=email_exists&status=402");
                return ResponseEntity.status(402).build();
            }
            if (e.getMessage().contains("탈퇴한 회원입니다.")) {
                response.sendRedirect(oauthSuccessRedirectUrl + "?error=account_disabled&status=410");
                return ResponseEntity.status(410).build();
            }
            response.sendRedirect(oauthSuccessRedirectUrl + "?error=bad_request&status=400");
            return ResponseEntity.status(400).build();
        } catch (Exception e) {
            response.sendRedirect(oauthSuccessRedirectUrl + "?error=server_error&status=500");
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 로그아웃 API
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃")
    @ApiResponse(responseCode = "200", description = "로그아웃 완료")
    @ApiResponse(responseCode = "401", description = "이미 로그아웃된 상태")
    public ResponseEntity<ApiResponseDTO<String>> logout(HttpSession session,HttpServletRequest request,  HttpServletResponse response) {
        UserDTO sessionUser = sessionManager.getSession(session.getId());

        if (sessionUser == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponseDTO<>(401, "이미 로그아웃된 상태입니다.", null));
        }

        // 세션 삭제
        sessionManager.removeSession(session.getId());

        // 개발 환경 여부 감지
        boolean isLocalEnv = request.getServerName().equals("localhost") || request.getServerName().startsWith("127.0.0.1");

        // 기본 쿠키 설정
        String cookie = "JSESSIONID=; Path=/; HttpOnly; SameSite=None; Max-Age=0";
        
        // 운영 환경에서는 Secure 속성 추가
        if (!isLocalEnv) {
            cookie += "; Secure";
        }

        // 쿠키 삭제
        response.addHeader("Set-Cookie", cookie);

        return ResponseEntity.ok(new ApiResponseDTO<>(200, "로그아웃 완료", "success"));
    }
    
    @PostMapping("/delete")
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴")
    @ApiResponse(responseCode = "200", description = "회원 탈퇴 완료")
    @ApiResponse(responseCode = "401", description = "세션이 만료됨")
    public ApiResponseDTO<String> deleteProfile(HttpSession session) {
        UserDTO sessionUser = sessionManager.getSession(session.getId());
        if (sessionUser == null) {
            return new ApiResponseDTO<>(401, "세션이 만료되었습니다. 다시 로그인해주세요.", null);
        }

        userService.deleteUser(sessionUser.getSocialId(), sessionUser.getAuthProvider(), sessionUser.getEmail());
        sessionManager.removeSession(session.getId());

        return new ApiResponseDTO<>(200, "회원 탈퇴 완료", "success");
    }
}

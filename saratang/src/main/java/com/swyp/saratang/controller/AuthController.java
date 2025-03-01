package com.swyp.saratang.controller;

import io.swagger.v3.oas.annotations.Operation;

import javax.servlet.http.Cookie;
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

import com.swyp.saratang.config.JwtAuthUtil;
import com.swyp.saratang.config.JwtUtil;
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
    private JwtUtil jwtUtil;
    
    @Autowired
    private JwtAuthUtil jwtAuthUtil; // ✅ JWT 유틸리티 주입
    
    @Value("${oauth.success.redirect.url}")
    private String oauthSuccessRedirectUrl;
    
    @Autowired
    private UserService userService;
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

        // 로그인 제공자 확인
        if (!"naver".equals(provider) && !"kakao".equals(provider)) {
            logger.warn("잘못된 provider 요청: {}", provider);
            return ResponseEntity.status(400)
                    .body(new ApiResponseDTO<>(400, "지원되지 않는 provider 값입니다. (naver 또는 kakao만 허용)", null));
        }

        try {
            // 로그인 URL 생성 (OAuth 서비스로 리디렉션)
            String authUrl = authService.getAuthUrl(provider);
            logger.info("OAuth 로그인 요청 - Provider: {}, Redirecting to: {}", provider, authUrl);

            // 리디렉션
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
            HttpServletResponse response) throws IOException {

        // 로그인 취소 처리
        if ("access_denied".equals(error)) {
            logger.warn("사용자가 Kakao 로그인 취소 - 이유: {}", errorDescription);
            response.sendRedirect(oauthSuccessRedirectUrl + "?error=access_denied&status=400");
            return;
        }

        // OAuth 로그인 처리 및 JWT 발급
        handleLoginCallback("kakao", error, errorDescription, code, response);
    }

    @GetMapping("/callback/naver")
    public void naverLoginCallback(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            @RequestParam(value = "code", required = false) String code,
            HttpServletResponse response) throws IOException {

        // 로그인 취소 처리
        if ("access_denied".equals(error)) {
            logger.warn("사용자가 Naver 로그인 취소 - 이유: {}", errorDescription);
            response.sendRedirect(oauthSuccessRedirectUrl + "?error=access_denied&status=400");
            return;
        }

        // OAuth 로그인 처리 및 JWT 발급
        handleLoginCallback("naver", error, errorDescription, code, response);
    }

    private void handleLoginCallback(
            String provider,
            String error,
            String errorDescription,
            String code,
            HttpServletResponse response) throws IOException {

        try {
            // OAuth 로그인 처리
            UserDTO user = authService.processOAuthLogin(provider, code);

            // JWT 생성
            String jwtToken = jwtUtil.generateToken(user.getId().toString(), user.getEmail(), user.getAuthProvider());
            logger.info("JWT 생성 완료 - Token: {}", jwtToken);

            //  JWT를 쿠키에 포함 (HttpOnly, Secure, SameSite 설정)
            Cookie jwtCookie = new Cookie("jwt", jwtToken);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false);   // HTTPS가 아니라면 false 설정
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 60 * 24); // 1일 동안 유지
            response.addCookie(jwtCookie);

            // SameSite=None; 설정을 위해 `Set-Cookie` 헤더 직접 추가
            response.setHeader("Set-Cookie",
                "jwt=" + jwtToken + "; Path=/; HttpOnly; Secure; SameSite=None");

            // `profileYn` 확인 후 리디렉트 URL 결정
            String redirectUrl;
            if (Boolean.FALSE.equals(user.getProfileYn())) {
                // 프로필 미등록 (201 상태 코드)
                redirectUrl = oauthSuccessRedirectUrl + "?status=201&token=" + jwtToken;
                logger.info("프로필 미등록 사용자 - 201 리디렉트");
            } else {
                // 기존 정상 로그인 처리 (200 상태 코드)
                redirectUrl = oauthSuccessRedirectUrl + "?status=200&token=" + jwtToken;
                logger.info("로그인 성공 - 200 리디렉트");
            }

            // 리디렉트 수행
            response.sendRedirect(redirectUrl);

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("이미 존재하는 이메일")) {
                response.sendRedirect(oauthSuccessRedirectUrl + "?error=email_exists&status=402");
                return;
            }
            if (e.getMessage().contains("탈퇴한 회원입니다.")) {
                response.sendRedirect(oauthSuccessRedirectUrl + "?error=account_disabled&status=410");
                return;
            }
            response.sendRedirect(oauthSuccessRedirectUrl + "?error=bad_request&status=400");
        } catch (Exception e) {
            response.sendRedirect(oauthSuccessRedirectUrl + "?error=server_error&status=500");
        }
    }


    /**
     * ✅ 로그아웃 API (JWT 기반)
     */
    @Operation(summary = "로그아웃", description = "JWT 인증 후 현재 로그인된 사용자를 로그아웃합니다.")
    @ApiResponse(responseCode = "200", description = "로그아웃 완료")
    @ApiResponse(responseCode = "401", description = "이미 로그아웃된 상태")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO<String>> logout(
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request,
            HttpServletResponse response) {

        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponseDTO<>(401, "이미 로그아웃된 상태입니다.", null));
        }

        // ✅ JWT 쿠키 삭제 (만료 처리)
        invalidateJwtCookie(response);

        logger.info("✅ 로그아웃 완료 - 사용자 ID: {}", userId);
        return ResponseEntity.ok(new ApiResponseDTO<>(200, "로그아웃 완료", "success"));
    }

    /**
     * ✅ 회원 탈퇴 API (JWT 기반)
     */
    @Operation(summary = "회원 탈퇴", description = "JWT 인증 후 현재 로그인된 사용자를 탈퇴 처리합니다.")
    @ApiResponse(responseCode = "200", description = "회원 탈퇴 완료")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "404", description = "사용자 없음")
    @PostMapping("/delete")
    public ResponseEntity<ApiResponseDTO<String>> deleteProfile(
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request,
            HttpServletResponse response) {

        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponseDTO<>(401, "세션이 만료되었습니다. 다시 로그인해주세요.", null));
        }

        // ✅ 사용자 정보 삭제
        UserDTO user = userService.getUserById(Integer.parseInt(userId));
        if (user == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponseDTO<>(404, "해당 사용자가 존재하지 않습니다.", null));
        }

        userService.deleteUser(user.getSocialId(), user.getAuthProvider(), user.getEmail());

        // ✅ JWT 쿠키 삭제 (만료 처리)
        invalidateJwtCookie(response);

        logger.info("✅ 회원 탈퇴 완료 - 사용자 ID: {}", userId);
        return ResponseEntity.ok(new ApiResponseDTO<>(200, "회원 탈퇴 완료", "success"));
    }

    /**
     * ✅ JWT 쿠키 삭제 (만료 처리)
     */
    private void invalidateJwtCookie(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // 배포 시에는 true로 설정해야 함
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // 즉시 만료
        response.addCookie(jwtCookie);
    }
}



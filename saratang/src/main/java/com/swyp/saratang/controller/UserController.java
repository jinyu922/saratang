package com.swyp.saratang.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * SNS 회원가입 (임시 저장)
     */
    @Operation(summary = "SNS 회원가입 검증", description = "SNS 로그인 검증 후 프로필 입력 진행")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "SNS 검증 완료"),
        @ApiResponse(responseCode = "409", description = "이미 가입된 사용자"),
        @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @PostMapping("/signup/temp")
    public ResponseEntity<ApiResponseDTO<Void>> registerUserWithToken(
            @RequestParam String accessToken,
            @RequestParam String provider) {

        ResponseEntity<ApiResponseDTO<Void>> result = userService.registerUserWithToken(accessToken, provider);
        return result;
    }

    /**
     * 추가 프로필 입력 후 최종 가입 완료
     */
    @Operation(summary = "추가 프로필 입력 후 최종 가입", description = "사용자가 추가 정보를 입력한 후 최종 회원가입 완료")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "가입 완료"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @PostMapping("/signup/complete")
    public ResponseEntity<ApiResponseDTO<Void>> completeRegistration(@RequestBody UserDTO userDTO) {
        ResponseEntity<ApiResponseDTO<Void>> result = userService.completeRegistration(userDTO);
        return result;
    }

    /**
     * 네이버 & 카카오 로그인 처리
     */
    @Operation(summary = "SNS 로그인", description = "네이버 또는 카카오 계정으로 로그인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
        @ApiResponse(responseCode = "404", description = "회원가입 필요(계정없음)"),
        @ApiResponse(responseCode = "500", description = "로그인 처리 중 오류 발생")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<UserDTO>> loginWithSNS(
            @RequestParam String accessToken,
            @RequestParam String provider,
            HttpSession session) {

        try {
            ResponseEntity<ApiResponseDTO<UserDTO>> response = userService.loginWithSNS(accessToken, provider);

            // 로그인 성공 시 세션 저장
            if (response.getStatusCode() == HttpStatus.OK) {
                session.setAttribute("user", response.getBody().getData());
            }

            return response;

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "로그인 처리 중 오류 발생: " + e.getMessage(), null));
        }
    }

    /**
     * 현재 로그인한 사용자 세션 조회
     */
    @Operation(summary = "현재 로그인한 사용자 조회", description = "현재 로그인된 사용자의 정보를 세션에서 가져옴")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "세션 조회 성공"),
        @ApiResponse(responseCode = "401", description = "로그인 필요"),
        @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping("/session")
    public ResponseEntity<ApiResponseDTO<UserDTO>> getSessionUser(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponseDTO<>(401, "로그인 필요", null));
        }

        return ResponseEntity.ok(new ApiResponseDTO<>(200, "세션 조회 성공", user));
    }

    /**
     * 로그아웃 (세션 삭제)
     */
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자의 세션을 삭제하고 로그아웃")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO<String>> logout(HttpSession session) {
        session.invalidate(); // 세션 삭제

        return ResponseEntity.ok(new ApiResponseDTO<>(200, "로그아웃 성공", "세션이 정상적으로 삭제"));
    }

}

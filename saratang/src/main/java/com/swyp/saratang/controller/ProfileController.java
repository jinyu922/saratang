package com.swyp.saratang.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.swyp.saratang.config.JwtAuthUtil;
import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.model.SafeUserDTO;
import com.swyp.saratang.model.PointDTO;
import com.swyp.saratang.service.UserService;
import com.swyp.saratang.config.JwtAuthUtil;

@RestController
@RequestMapping("/profile")
@Tag(name = "Profile API", description = "회원 프로필 관련 API")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtAuthUtil jwtAuthUtil; // ✅ JWT 유틸리티 주입

    /**
     *  신규 회원 프로필 입력 API
     */
    @Operation(summary = "신규 회원 프로필 등록", description = "신규 회원의 프로필을 입력합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 입력 완료")
    @ApiResponse(responseCode = "400", description = "이미 프로필이 등록된 사용자")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @PostMapping("/new")
    public ApiResponseDTO<UserDTO> createNewProfile(
            @RequestBody UserDTO user,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {

        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }

        UserDTO existingUser = userService.getUserById(Integer.parseInt(userId));
        if (existingUser == null) {
            return new ApiResponseDTO<>(401, "유효하지 않은 사용자", null);
        }

        if (existingUser.getUsername() != null) {
            return new ApiResponseDTO<>(400, "이미 프로필이 등록된 사용자입니다.", null);
        }

        user.setSocialId(existingUser.getSocialId());
        user.setAuthProvider(existingUser.getAuthProvider());

        userService.newProfile(user);
        return new ApiResponseDTO<>(200, "프로필 입력 완료(회원가입완료)", user);
    }

    /**
     *  프로필 조회 API
     */
    @Operation(summary = "회원 프로필 조회", description = "JWT 인증 후 자신의 프로필을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 조회 성공")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @GetMapping("/me")
    public ApiResponseDTO<SafeUserDTO> getProfile(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(value = "token", required = false) String queryToken,
            HttpServletRequest request) {

        String jwtToken = jwtAuthUtil.extractToken(request, token, queryToken);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패: 토큰이 없음", null);
        }

        SafeUserDTO user = userService.getSafeUserById(Integer.parseInt(userId));
        return new ApiResponseDTO<>(200, "프로필 조회 성공", user);
    }

    /**
     *  프로필 수정 API
     */
    @Operation(summary = "회원 프로필 수정", description = "JWT 인증 후 자신의 프로필 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 수정 완료")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @PostMapping("/edit")
    public ApiResponseDTO<String> editProfile(
            @RequestBody UserDTO user,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {

        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }

        user.setId(Integer.parseInt(userId));
        user.setIsActive(true);

        userService.editProfile(user);
        return new ApiResponseDTO<>(200, "프로필 수정 완료", "success");
    }
}

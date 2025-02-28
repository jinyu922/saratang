package com.swyp.saratang.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    private JwtAuthUtil jwtAuthUtil; //  JWT 유틸리티 주입

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
    
    /**
     * 사용자 크레딧 조회 API (JWT 인증)
     */
    @GetMapping("/credits")
    @Operation(summary = "사용자 크레딧 조회", description = "JWT 인증을 이용하여 현재 로그인된 사용자의 총 크레딧을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "크레딧 조회 성공")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getUserCredits(
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {

        try {
            String jwtToken = jwtAuthUtil.extractToken(request, token, null);
            String userId = jwtAuthUtil.extractUserId(jwtToken);

            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponseDTO<>(401, "JWT 인증 실패", null));
            }

            Integer totalCredits = userService.getTotalCreditsByUserId(Integer.parseInt(userId));

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("totalCredits", totalCredits);

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "크레딧 조회 성공", responseData));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "크레딧 조회 중 오류 발생", null));
        }
    }

    /**
     * 사용자 크레딧 내역 조회 API (JWT 인증)
     */
    @GetMapping("/credits/history")
    @Operation(summary = "사용자 크레딧 내역 조회", description = "JWT 인증을 이용하여 현재 로그인된 사용자의 크레딧 사용 내역을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "크레딧 내역 조회 성공")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    public ResponseEntity<ApiResponseDTO<List<PointDTO>>> getUserCreditHistory(
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {

        try {
            String jwtToken = jwtAuthUtil.extractToken(request, token, null);
            String userId = jwtAuthUtil.extractUserId(jwtToken);

            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponseDTO<>(401, "JWT 인증 실패", null));
            }

            List<PointDTO> creditHistory = userService.getCreditHistoryByUserId(Integer.parseInt(userId));

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "크레딧 내역 조회 성공", creditHistory));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "크레딧 내역 조회 중 오류 발생", null));
        }
    }
    
    
    /**
     * 아이콘 변경 API (JWT 인증)
     */
    @PostMapping("/changeusericon")
    @Operation(summary = "아이콘 변경", description = "JWT 인증을 이용하여 아이콘을 변경 (3포인트 차감 후 기록)")
    @ApiResponse(responseCode = "200", description = "아이콘 변경 완료")
    @ApiResponse(responseCode = "400", description = "변경할 아이콘 값이 없음")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @ApiResponse(responseCode = "402", description = "포인트 부족")
    @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> changeUserIcon(
            @RequestBody Map<String, Integer> requestData,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {

        try {
            String jwtToken = jwtAuthUtil.extractToken(request, token, null);
            String userId = jwtAuthUtil.extractUserId(jwtToken);

            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponseDTO<>(401, "JWT 인증 실패", null));
            }

            Integer newIconId = requestData.get("iconId");
            if (newIconId == null || newIconId <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDTO<>(400, "변경할 아이콘 값이 없습니다.", null));
            }

            Integer currentCredits = userService.getTotalCreditsByUserId(Integer.parseInt(userId));
            int changeCost = 3;

            if (currentCredits < changeCost) {
                return ResponseEntity.status(402)
                        .body(new ApiResponseDTO<>(402, "포인트가 부족합니다.", null));
            }

            userService.changeUserIcon(Integer.parseInt(userId), newIconId);
            userService.insertCreditHistory(Integer.parseInt(userId), "spend", -3, "아이콘 변경");

            Integer updatedCredits = userService.getTotalCreditsByUserId(Integer.parseInt(userId));

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("updatedCredits", updatedCredits);
            responseData.put("iconId", newIconId);

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "아이콘 변경 완료 (포인트 3 차감)", responseData));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "아이콘 변경 중 오류 발생", null));
        }
    }

    /**
     * 닉네임 색상 변경 API (JWT 인증)
     */
    @PostMapping("/changeusercolor")
    @Operation(summary = "닉네임 색상 변경", description = "JWT 인증을 이용하여 닉네임 색상을 변경 (3포인트 차감 후 기록)")
    @ApiResponse(responseCode = "200", description = "닉네임 색상 변경 완료")
    @ApiResponse(responseCode = "400", description = "변경할 값이 없음")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @ApiResponse(responseCode = "402", description = "포인트 부족")
    @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> changeUserColor(
            @RequestBody Map<String, String> requestData,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {

        try {
            String jwtToken = jwtAuthUtil.extractToken(request, token, null);
            String userId = jwtAuthUtil.extractUserId(jwtToken);

            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponseDTO<>(401, "JWT 인증 실패", null));
            }

            String newNicknameColor = requestData.get("nicknameColor");
            if (newNicknameColor == null || newNicknameColor.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDTO<>(400, "변경할 닉네임 색상 값이 없습니다.", null));
            }

            Integer currentCredits = userService.getTotalCreditsByUserId(Integer.parseInt(userId));
            int changeCost = 3;

            if (currentCredits < changeCost) {
                return ResponseEntity.status(402)
                        .body(new ApiResponseDTO<>(402, "포인트가 부족합니다.", null));
            }

            userService.changeUserColor(Integer.parseInt(userId), newNicknameColor);
            userService.insertCreditHistory(Integer.parseInt(userId), "spend", -3, "닉네임 색상 변경");

            Integer updatedCredits = userService.getTotalCreditsByUserId(Integer.parseInt(userId));

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("updatedCredits", updatedCredits);
            responseData.put("nicknameColor", newNicknameColor);

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "닉네임 색상 변경 완료 (포인트 3 차감)", responseData));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "닉네임 색상 변경 중 오류 발생", null));
        }
    }
}

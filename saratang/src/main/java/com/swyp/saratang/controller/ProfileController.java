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
import lombok.RequiredArgsConstructor;

import com.swyp.saratang.config.JwtAuthUtil;
import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.CategoryDTO;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.model.SafeUserDTO;
import com.swyp.saratang.model.UserColorDTO;
import com.swyp.saratang.model.PointDTO;
import com.swyp.saratang.service.CategoryService;
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
    
    @Autowired
    private CategoryService categoryService;

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
        //새로운 유저 카테고리 등록 (기본값 : 전부 선호)
        CategoryDTO categoryDTO=new CategoryDTO();
        categoryDTO.setAll();
        categoryDTO.setUserId(Integer.parseInt(userId));
        categoryService.saveCategory(categoryDTO);
        
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
    
    
    
    
    
    
    
    @GetMapping("/usernamecolors")
    @Operation(summary = "사용자 닉네임 색상 목록 조회", description = "JWT 인증을 이용하여 사용자가 보유한 닉네임 색상 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "닉네임 색상 목록 조회 완료")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    public ResponseEntity<ApiResponseDTO<List<UserColorDTO>>> getUserColors(
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {

        try {
            String jwtToken = jwtAuthUtil.extractToken(request, token, null);
            String userId = jwtAuthUtil.extractUserId(jwtToken);

            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponseDTO<>(401, "JWT 인증 실패", null));
            }

            List<UserColorDTO> userColors = userService.getUserColorsByUserId(Integer.parseInt(userId));

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "닉네임 색상 목록 조회 완료", userColors));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "닉네임 색상 목록 조회 중 오류 발생", null));
        }
    }

    @PostMapping("/buyusercolor")
    @Operation(summary = "닉네임 색상 구매", description = "JWT 인증을 이용하여 닉네임 색상을 구매 (3포인트 차감 후 기록)")
    @ApiResponse(responseCode = "200", description = "닉네임 색상 구매 완료")
    @ApiResponse(responseCode = "400", description = "구매할 값이 없음")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @ApiResponse(responseCode = "402", description = "포인트 부족")
    @ApiResponse(responseCode = "409", description = "이미 보유한 색상")
    @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> purchaseUserColor(
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

            Integer colorId = requestData.get("colorId");
            if (colorId == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDTO<>(400, "구매할 닉네임 색상이 없습니다.", null));
            }

            // 이미 보유한 색상인지 확인
            boolean alreadyOwned = userService.isUserOwnsColor(Integer.parseInt(userId), colorId);
            if (alreadyOwned) {
                return ResponseEntity.status(409)
                        .body(new ApiResponseDTO<>(409, "이미 보유한 색상입니다.", null));
            }

            Integer currentCredits = userService.getTotalCreditsByUserId(Integer.parseInt(userId));
            int purchaseCost = 3;

            if (currentCredits < purchaseCost) {
                return ResponseEntity.status(402)
                        .body(new ApiResponseDTO<>(402, "포인트가 부족합니다.", null));
            }

            // 색상 구매 등록
            userService.purchaseUserColor(Integer.parseInt(userId), colorId);
            userService.insertCreditHistory(Integer.parseInt(userId), "spend", -3, "닉네임 색상 구매");

            Integer updatedCredits = userService.getTotalCreditsByUserId(Integer.parseInt(userId));

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("updatedCredits", updatedCredits);
            responseData.put("colorId", colorId);

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "닉네임 색상 구매 완료 (포인트 3 차감)", responseData));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "닉네임 색상 구매 중 오류 발생", null));
        }
    }

    /**
     * 닉네임 색상 변경 
     */
    @PostMapping("/changeusercolor")
    @Operation(summary = "닉네임 색상 변경", description = "JWT 인증을 이용하여 닉네임 색상을 변경")
    @ApiResponse(responseCode = "200", description = "닉네임 색상 변경 완료")
    @ApiResponse(responseCode = "400", description = "변경할 값이 없음")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @ApiResponse(responseCode = "409", description = "이미 현재 사용 중인 색상")
    @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> changeUserColor(
            @RequestBody Map<String, Integer> requestData,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {

        try {
            // JWT에서 사용자 ID 추출
            String jwtToken = jwtAuthUtil.extractToken(request, token, null);
            String userId = jwtAuthUtil.extractUserId(jwtToken);

            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponseDTO<>(401, "JWT 인증 실패", null));
            }

            Integer newColorId = requestData.get("colorId");


            // 색상 변경
            userService.changeUserColor(Integer.parseInt(userId), newColorId);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("colorId", newColorId);

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "닉네임 색상 변경 완료", responseData));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "닉네임 색상 변경 중 오류 발생", null));
        }
    }
    
    
    /**
     * 현재 사용자의 닉네임 색상 조회 (JWT 인증)
     */
    @GetMapping("/currentcolor")
    @Operation(summary = "현재 사용자의 닉네임 색상 조회", description = "JWT 인증을 이용하여 현재 사용자의 색상을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "현재 색상 조회 성공")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    public ResponseEntity<ApiResponseDTO<UserColorDTO>> getCurrentUserColor(
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {

        try {
            String jwtToken = jwtAuthUtil.extractToken(request, token, null);
            String userId = jwtAuthUtil.extractUserId(jwtToken);

            if (userId == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponseDTO<>(401, "JWT 인증 실패", null));
            }

            UserColorDTO currentColor = userService.getCurrentColorByUserId(Integer.parseInt(userId));

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "현재 색상 조회 성공", currentColor));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "현재 색상 조회 중 오류 발생", null));
        }
    }
    
    /**
     * 전체 색상 리스트 조회
     */
    @GetMapping("/allcolors")
    @Operation(summary = "전체 색상 리스트 조회", description = "전체 색상 리스트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "전체 색상 조회 성공")
    @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    public ResponseEntity<ApiResponseDTO<List<UserColorDTO>>> getAllColors() {
        try {
            List<UserColorDTO> allColors = userService.getAllColors();

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "전체 색상 조회 성공", allColors));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "전체 색상 조회 중 오류 발생", null));
        }
    }
    
   /////////////////////////
    
    @GetMapping("/test/usernamecolors")
    @Operation(summary = "사용자 닉네임 색상 목록 조회 (테스트용)", description = "JWT 없이 userId를 직접 전달하여 사용자의 닉네임 색상 목록을 조회합니다.")
    public ResponseEntity<ApiResponseDTO<List<UserColorDTO>>> testGetUserColors(@RequestParam("userId") Integer userId) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDTO<>(400, "userId가 없습니다.", null));
            }

            List<UserColorDTO> userColors = userService.getUserColorsByUserId(userId);

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "닉네임 색상 목록 조회 완료", userColors));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "닉네임 색상 목록 조회 중 오류 발생", null));
        }
    }

    /**
     * 닉네임 색상 구매 (테스트용)
     */
    @PostMapping("/test/buyusercolor")
    @Operation(summary = "닉네임 색상 구매 (테스트용)", description = "JWT 없이 userId를 직접 전달하여 닉네임 색상을 구매합니다.")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> testPurchaseUserColor(@RequestBody Map<String, Integer> requestData) {
        try {
            Integer userId = requestData.get("userId");
            Integer colorId = requestData.get("colorId");

            if (userId == null || colorId == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDTO<>(400, "userId 또는 colorId가 없습니다.", null));
            }

            boolean alreadyOwned = userService.isUserOwnsColor(userId, colorId);
            if (alreadyOwned) {
                return ResponseEntity.status(409)
                        .body(new ApiResponseDTO<>(409, "이미 보유한 색상입니다.", null));
            }

            Integer currentCredits = userService.getTotalCreditsByUserId(userId);
            int purchaseCost = 3;

            if (currentCredits < purchaseCost) {
                return ResponseEntity.status(402)
                        .body(new ApiResponseDTO<>(402, "포인트가 부족합니다.", null));
            }

            userService.purchaseUserColor(userId, colorId);
            userService.insertCreditHistory(userId, "spend", -3, "닉네임 색상 구매");

            Integer updatedCredits = userService.getTotalCreditsByUserId(userId);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("updatedCredits", updatedCredits);
            responseData.put("colorId", colorId);

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "닉네임 색상 구매 완료", responseData));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "닉네임 색상 구매 중 오류 발생", null));
        }
    }

    /**
     * 닉네임 색상 변경 (테스트용)
     */
    @PostMapping("/test/changeusercolor")
    @Operation(summary = "닉네임 색상 변경 (테스트용)", description = "JWT 없이 userId를 직접 전달하여 닉네임 색상을 변경합니다.")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> testChangeUserColor(@RequestBody Map<String, Integer> requestData) {
        try {
            Integer userId = requestData.get("userId");
            Integer newColorId = requestData.get("colorId");

            if (userId == null || newColorId == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDTO<>(400, "userId 또는 colorId가 없습니다.", null));
            }


            userService.changeUserColor(userId, newColorId);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("colorId", newColorId);

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "닉네임 색상 변경 완료", responseData));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "닉네임 색상 변경 중 오류 발생", null));
        }
    }

    /**
     * 현재 사용자의 닉네임 색상 조회 (테스트용)
     */
    @GetMapping("/test/currentcolor")
    @Operation(summary = "현재 사용자의 닉네임 색상 조회 (테스트용)", description = "JWT 없이 userId를 직접 전달하여 현재 사용자의 닉네임 색상을 조회합니다.")
    public ResponseEntity<ApiResponseDTO<UserColorDTO>> testGetCurrentUserColor(@RequestParam("userId") Integer userId) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDTO<>(400, "userId가 없습니다.", null));
            }

            UserColorDTO currentColor = userService.getCurrentColorByUserId(userId);

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "현재 색상 조회 성공", currentColor));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "현재 색상 조회 중 오류 발생", null));
        }
    }
    
    
   
    
}

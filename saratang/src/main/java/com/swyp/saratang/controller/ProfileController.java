package com.swyp.saratang.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.BoardDTO;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.model.SafeUserDTO;
import com.swyp.saratang.model.PointDTO;
import com.swyp.saratang.service.AuthServiceImpl;
import com.swyp.saratang.service.BoardService;
import com.swyp.saratang.service.UserService;
import com.swyp.saratang.session.SessionManager;

@RestController
@RequestMapping("/profile")
@Tag(name = "Profile API", description = "회원 프로필 관련 API")
public class ProfileController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private BoardService boardService;
    
    @Autowired
    private SessionManager sessionManager;

    private static final Logger logger = LogManager.getLogger(ProfileController.class);

    /**
     * 신규 회원 프로필 입력 API
     */
    @PostMapping("/new")
    @Operation(summary = "신규 프로필 등록", description = "프로필까지 정식회원 등록")
    @ApiResponse(responseCode = "200", description = "회원가입 완료")
    @ApiResponse(responseCode = "400", description = "이미 프로필이 등록된 사용자")
    @ApiResponse(responseCode = "401", description = "세션 만료")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    public ApiResponseDTO<UserDTO> createNewProfile(@RequestBody UserDTO user, HttpSession session) {
        UserDTO sessionUser = sessionManager.getSession(session.getId());

        if (sessionUser == null) {
            return new ApiResponseDTO<>(401, "세션이 만료되었습니다1. 다시 로그인해주세요.", null);
        }

        if (sessionUser.getUsername() != null) {
            return new ApiResponseDTO<>(400, "이미 프로필이 등록된 사용자입니다.", null);
        }
        
        user.setSocialId(sessionUser.getSocialId());
        user.setAuthProvider(sessionUser.getAuthProvider());

        userService.newProfile(user);

        UserDTO updatedUser = userService.getUserBySocialId(user.getSocialId(), user.getAuthProvider());
        sessionManager.setSession(session.getId(), updatedUser);

        return new ApiResponseDTO<>(200, "프로필 입력 완료(회원가입완료)", updatedUser);
    }


    /**
     * 프로필 조회 API
     */
    @GetMapping("/me")
    @Operation(summary = "프로필 조회", description = "현재 로그인한 사용자의 프로필 정보 확인")
    @ApiResponse(responseCode = "200", description = "프로필 조회 성공")
    @ApiResponse(responseCode = "401", description = "세션이 만료됨")
    public ApiResponseDTO<SafeUserDTO> getProfile(HttpSession session,HttpServletRequest request) {
    	System.out.println("📌 프로필 조회 요청 - 세션 ID: " + session.getId());
        UserDTO sessionUser = sessionManager.getSession(session.getId());
        if (sessionUser == null) {
            return new ApiResponseDTO<>(401, "세션이 만료되었습니다2. 다시 로그인해주세요.", null);
        }

        SafeUserDTO user = userService.getSafeUserById(sessionUser.getId());
        return new ApiResponseDTO<>(200, "프로필 조회 성공", user);
    }
    
   

    /**
     * 프로필 수정 API
     */
    @PostMapping("/edit")
    @Operation(summary = "프로필 수정", description = "회원의 프로필 정보를 수정")
    @ApiResponse(responseCode = "200", description = "프로필 수정 완료")
    @ApiResponse(responseCode = "401", description = "세션이 만료됨")
    public ApiResponseDTO<String> editProfile(@RequestBody UserDTO user, HttpSession session) {
        UserDTO sessionUser = sessionManager.getSession(session.getId());
        if (sessionUser == null) {
            return new ApiResponseDTO<>(401, "세션이 만료되었습니다3. 다시 로그인해주세요.", null);
        }

        user.setSocialId(sessionUser.getSocialId());
        user.setAuthProvider(sessionUser.getAuthProvider());
        user.setIsActive(true);

        userService.editProfile(user);
        sessionManager.setSession(session.getId(), user);

        return new ApiResponseDTO<>(200, "프로필 수정 완료", "success");
    }
    
    @GetMapping("/credits")
    @Operation(summary = "사용자 크레딧 조회", description = "현재 로그인한 사용자의 총 크레딧 반환")
    @ApiResponse(responseCode = "200", description = "크레딧 조회 성공")
    @ApiResponse(responseCode = "401", description = "세션이 만료됨")
    public ApiResponseDTO<Map<String, Object>> getUserCredits(HttpSession session) {
        //  세션에서 사용자 정보 가져오기
        UserDTO sessionUser = sessionManager.getSession(session.getId());

        if (sessionUser == null) {
            return new ApiResponseDTO<>(401, "세션이 만료되었습니다. 다시 로그인해주세요.", null);
        }

        Integer userId = sessionUser.getId();

        //  사용자 크레딧 내역 가져오기
        List<PointDTO> creditHistory = userService.getCreditHistoryByUserId(userId);

        //  사용자 총 크레딧 합계 조회
        Integer totalCredits = userService.getTotalCreditsByUserId(userId);
        
        totalCredits = (totalCredits == null) ? 0 : totalCredits;

        //  응답 데이터 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("totalCredits", totalCredits);

        return new ApiResponseDTO<>(200, "크레딧 조회 성공", responseData);
    }
    /**
     * 사용자 크레딧 내역 조회
     */
    @GetMapping("/credits/history")
    @Operation(summary = "사용자 크레딧 내역 조회", description = "현재 로그인한 사용자의 모든 크레딧 내역 반환")
    @ApiResponse(responseCode = "200", description = "크레딧 내역 조회 성공")
    @ApiResponse(responseCode = "401", description = "세션이 만료됨")
    public ApiResponseDTO<List<PointDTO>> getUserCreditHistory(HttpSession session) {
        UserDTO sessionUser = sessionManager.getSession(session.getId());

        if (sessionUser == null) {
            return new ApiResponseDTO<>(401, "세션이 만료되었습니다. 다시 로그인해주세요.", null);
        }

        List<PointDTO> creditHistory = userService.getCreditHistoryByUserId(sessionUser.getId());
        return new ApiResponseDTO<>(200, "크레딧 내역 조회 성공", creditHistory);
    }

    
    /**
     * [TEST] 사용자 크레딧 내역 조회
     */
    @GetMapping("/credits/test/history")
    @Operation(summary = "[TEST] 사용자 크레딧 내역 조회", description = "특정 사용자의 모든 크레딧 내역 반환 (세션 없이 userId 직접 입력)")
    @ApiResponse(responseCode = "200", description = "크레딧 내역 조회 성공")
    @ApiResponse(responseCode = "400", description = "유효하지 않은 사용자 ID")
    public ApiResponseDTO<List<PointDTO>> getUserCreditHistoryForTest(@RequestParam("userId") Integer userId) {
        if (userId == null) {
            return new ApiResponseDTO<>(400, "유효하지 않은 요청: userId가 필요합니다.", null);
        }

        List<PointDTO> creditHistory = userService.getCreditHistoryByUserId(userId);
        return new ApiResponseDTO<>(200, "크레딧 내역 조회 성공 (테스트 모드)", creditHistory);
    }

    /**
     * [TEST] 사용자 총 크레딧 조회
     */
    @GetMapping("/credit/test/total")
    @Operation(summary = "[TEST] 사용자 총 크레딧 조회", description = "특정 사용자의 총 크레딧 반환 (세션 없이 userId 직접 입력)")
    @ApiResponse(responseCode = "200", description = "총 크레딧 조회 성공")
    @ApiResponse(responseCode = "400", description = "유효하지 않은 사용자 ID")
    public ApiResponseDTO<Integer> getUserTotalCreditsForTest(@RequestParam("userId") Integer userId) {
        if (userId == null) {
            return new ApiResponseDTO<>(400, "유효하지 않은 요청: userId가 필요합니다.", null);
        }

        Integer totalCredits = userService.getTotalCreditsByUserId(userId);
        return new ApiResponseDTO<>(200, "총 크레딧 조회 성공 (테스트 모드)", totalCredits);
    }
    
    
    @PostMapping("/changeusericon")
    @Operation(summary = "아이콘 변경", description = "아이콘을 변경 (3포인트 차감 후 기록)")
    @ApiResponse(responseCode = "200", description = "변경 완료")
    @ApiResponse(responseCode = "401", description = "세션이 만료됨")
    @ApiResponse(responseCode = "402", description = "포인트 부족")
    @ApiResponse(responseCode = "400", description = "변경할 값이 없음")
    public ApiResponseDTO<Map<String, Object>> changeUserIcon(@RequestBody Map<String, Integer> requestData, HttpSession session) {
        UserDTO sessionUser = sessionManager.getSession(session.getId());

        if (sessionUser == null) {
            return new ApiResponseDTO<>(401, "세션이 만료되었습니다. 다시 로그인해주세요.", null);
        }

        Integer newIconId = requestData.get("iconId");
        boolean isIconChange = newIconId != null && newIconId > 0;

        //  변경할 값이 없는 경우 예외 반환
        if (!isIconChange) {
            return new ApiResponseDTO<>(400, "변경할 아이콘 값이 없습니다.", null);
        }

        Integer userId = sessionUser.getId();

        //  현재 크레딧 조회 (credits 테이블에서 총합)
        Integer currentCredits = userService.getTotalCreditsByUserId(userId);

        //  크레딧 차감 금액
        int changeCost = 3;

        //  포인트 부족 시 예외 반환
        if (currentCredits < changeCost) {
            return new ApiResponseDTO<>(402, "포인트가 부족합니다.", null);
        }

        //  아이콘 변경 적용
        sessionUser.setIcon(newIconId);

        //  DB 업데이트: 아이콘 변경
        userService.changeUserIcon(userId, newIconId);

        //  DB 업데이트: 크레딧 내역 추가 (-3포인트)
        userService.insertCreditHistory(userId, "spend", -3, "아이콘 변경");

        //  변경 후 새로운 크레딧 총합 조회
        Integer updatedCredits = userService.getTotalCreditsByUserId(userId);

        // 응답 데이터 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("updatedCredits", updatedCredits);
        responseData.put("iconId", newIconId);

        //  세션 업데이트 (변경된 크레딧 포함)
        sessionUser.setCredits(updatedCredits);
        sessionManager.setSession(session.getId(), sessionUser);

        return new ApiResponseDTO<>(200, "아이콘 변경 완료 (포인트 3 차감)", responseData);
    }
    
    @PostMapping("/changeusericon/test")
    @Operation(summary = "아이콘 변경 (테스트용)", description = "아이콘을 변경 (3포인트 차감 후 기록) - 테스트 버전")
    @ApiResponse(responseCode = "200", description = "변경 완료")
    @ApiResponse(responseCode = "401", description = "유효하지 않은 사용자 ID")
    @ApiResponse(responseCode = "402", description = "포인트 부족")
    @ApiResponse(responseCode = "400", description = "변경할 값이 없음")
    public ApiResponseDTO<Map<String, Object>> changeUserIconTest(@RequestBody Map<String, Integer> requestData) {
        Integer userId = requestData.get("userId");
        Integer newIconId = requestData.get("iconId");

        if (userId == null || userId <= 0) {
            return new ApiResponseDTO<>(401, "유효하지 않은 사용자 ID입니다.", null);
        }

        boolean isIconChange = newIconId != null && newIconId > 0;

        //  변경할 값이 없는 경우 예외 반환
        if (!isIconChange) {
            return new ApiResponseDTO<>(400, "변경할 아이콘 값이 없습니다.", null);
        }

        //  현재 사용자 정보 조회
        UserDTO user = userService.getUserById(userId);
        if (user == null) {
            return new ApiResponseDTO<>(401, "유효하지 않은 사용자 ID입니다.", null);
        }

        //  현재 크레딧 조회 (credits 테이블에서 총합)
        Integer currentCredits = userService.getTotalCreditsByUserId(userId);

        //  크레딧 차감 금액
        int changeCost = 3;

        // 포인트 부족 시 예외 반환
        if (currentCredits < changeCost) {
            return new ApiResponseDTO<>(402, "포인트가 부족합니다.", null);
        }

        //  아이콘 변경 적용
        user.setIcon(newIconId);

        //  DB 업데이트: 아이콘 변경
        userService.changeUserIcon(userId, newIconId);

        //  DB 업데이트: 크레딧 내역 추가 (-3포인트)
        userService.insertCreditHistory(userId, "spend", -3, "아이콘 변경 (테스트)");

        //  변경 후 새로운 크레딧 총합 조회
        Integer updatedCredits = userService.getTotalCreditsByUserId(userId);

        // 응답 데이터 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("updatedCredits", updatedCredits);
        responseData.put("iconId", newIconId);

        return new ApiResponseDTO<>(200, "아이콘 변경 완료 (포인트 3 차감) - 테스트 모드", responseData);
    }

    @PostMapping("/changeusercolor")
    @Operation(summary = "닉네임 색상 변경", description = "닉네임 색상을 변경 (3포인트 차감 후 기록)")
    @ApiResponse(responseCode = "200", description = "변경 완료")
    @ApiResponse(responseCode = "401", description = "세션이 만료됨")
    @ApiResponse(responseCode = "402", description = "포인트 부족")
    @ApiResponse(responseCode = "400", description = "변경할 값이 없음")
    public ApiResponseDTO<Map<String, Object>> changeUserColor(@RequestBody Map<String, String> requestData, HttpSession session) {
        UserDTO sessionUser = sessionManager.getSession(session.getId());

        if (sessionUser == null) {
            return new ApiResponseDTO<>(401, "세션이 만료되었습니다. 다시 로그인해주세요.", null);
        }

        String newNicknameColor = requestData.get("nicknameColor");
        boolean isColorChange = newNicknameColor != null && !newNicknameColor.trim().isEmpty();

        //  변경할 값이 없는 경우 예외 반환
        if (!isColorChange) {
            return new ApiResponseDTO<>(400, "변경사항 없음", null);
        }

        Integer userId = sessionUser.getId();

        //  현재 크레딧 조회 (credits 테이블에서 총합)
        Integer currentCredits = userService.getTotalCreditsByUserId(userId);

        //  크레딧 차감 금액
        int changeCost = 3;

        //  포인트 부족 시 예외 반환
        if (currentCredits < changeCost) {
            return new ApiResponseDTO<>(402, "포인트가 부족합니다.", null);
        }

        //  닉네임 색상 변경 적용
        sessionUser.setColor(newNicknameColor);

        //  DB 업데이트: 닉네임 색상 변경
        userService.changeUserColor(userId, newNicknameColor);

        //  DB 업데이트: 크레딧 내역 추가 (-3포인트)
        userService.insertCreditHistory(userId, "spend", -3, "닉네임 색상 변경");

        //  변경 후 새로운 크레딧 총합 조회
        Integer updatedCredits = userService.getTotalCreditsByUserId(userId);

        // 응답 데이터 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("updatedCredits", updatedCredits);
        responseData.put("nicknameColor", newNicknameColor);

        //  세션 업데이트 (변경된 크레딧 포함)
        sessionUser.setCredits(updatedCredits);
        sessionManager.setSession(session.getId(), sessionUser);

        return new ApiResponseDTO<>(200, "변경 완료 (포인트 3 차감)", responseData);
    }

    @PostMapping("/changeusercolor/test")
    @Operation(summary = "[TEST] 닉네임 색상 변경", description = "특정 사용자의 닉네임 색상을 변경 (3포인트 차감 후 기록) - 세션 없이 userId 직접 입력")
    @ApiResponse(responseCode = "200", description = "변경 완료")
    @ApiResponse(responseCode = "400", description = "유효하지 않은 사용자 ID")
    @ApiResponse(responseCode = "402", description = "포인트 부족")
    public ApiResponseDTO<Map<String, Object>> changeUserColorForTest(
            @RequestBody Map<String, String> requestData,
            @RequestParam("userId") Integer userId) {

        if (userId == null) {
            return new ApiResponseDTO<>(400, "유효하지 않은 요청: userId가 필요합니다.", null);
        }

        String newNicknameColor = requestData.get("nicknameColor");
        boolean isColorChange = newNicknameColor != null && !newNicknameColor.trim().isEmpty();

        //  변경할 값이 없는 경우 예외 반환
        if (!isColorChange) {
            return new ApiResponseDTO<>(400, "변경사항 없음", null);
        }

        //  현재 크레딧 조회 (credits 테이블에서 총합)
        Integer currentCredits = userService.getTotalCreditsByUserId(userId);

        //  크레딧 차감 금액
        int changeCost = 3;

        //  포인트 부족 시 예외 반환
        if (currentCredits < changeCost) {
            return new ApiResponseDTO<>(402, "포인트가 부족합니다.", null);
        }

        // DB 업데이트: 닉네임 색상 변경
        userService.changeUserColor(userId, newNicknameColor);

        // DB 업데이트: 크레딧 내역 추가 (-3포인트)
        userService.insertCreditHistory(userId, "spend", -3, "닉네임 색상 변경");

        // 변경 후 새로운 크레딧 총합 조회
        Integer updatedCredits = userService.getTotalCreditsByUserId(userId);

        // 응답 데이터 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("updatedCredits", updatedCredits);
        responseData.put("nicknameColor", newNicknameColor);

        return new ApiResponseDTO<>(200, "변경 완료 (포인트 3 차감)", responseData);
    }
    
    
    
}
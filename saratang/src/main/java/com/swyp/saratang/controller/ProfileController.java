package com.swyp.saratang.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.service.UserService;
import com.swyp.saratang.session.SessionManager;

@RestController
@RequestMapping("/profile")
@Tag(name = "Profile API", description = "회원 프로필 관련 API")
public class ProfileController {

    @Autowired
    private UserService userService;
    

    @Autowired
    private SessionManager sessionManager;

    

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
            return new ApiResponseDTO<>(401, "세션이 만료되었습니다. 다시 로그인해주세요.", null);
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
    public ApiResponseDTO<UserDTO> getProfile(HttpSession session) {
        UserDTO sessionUser = sessionManager.getSession(session.getId());
        if (sessionUser == null) {
            return new ApiResponseDTO<>(401, "세션이 만료되었습니다. 다시 로그인해주세요.", null);
        }

        UserDTO user = userService.getUserBySocialId(sessionUser.getSocialId(), sessionUser.getAuthProvider());
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
            return new ApiResponseDTO<>(401, "세션이 만료되었습니다. 다시 로그인해주세요.", null);
        }

        user.setSocialId(sessionUser.getSocialId());
        user.setAuthProvider(sessionUser.getAuthProvider());
        user.setIsActive(true);

        userService.editProfile(user);
        sessionManager.setSession(session.getId(), user);

        return new ApiResponseDTO<>(200, "프로필 수정 완료", "success");
    }

    /**
     * 회원 탈퇴 API
     */
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
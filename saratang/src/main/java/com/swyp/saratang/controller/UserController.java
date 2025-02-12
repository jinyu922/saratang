package com.swyp.saratang.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.service.UserService;


@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 네이버 로그인 후, 가입 여부 체크
     */
    @GetMapping("/naver/check")
    public ResponseEntity<Boolean> checkNaverUser(@RequestParam String socialId) {
        boolean exists = userService.existsBySocialId(socialId);
        return ResponseEntity.ok(exists);
    }

    /**
     * 네이버 로그인 후 임시 저장 
     */
    @PostMapping("/naversignup/temp")
    public ResponseEntity<String> registerNaverUserWithToken(@RequestParam String accessToken) {
        String result = userService.registerNaverUserWithToken(accessToken);

        if ("이미 가입된 사용자".equals(result)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        } else if ("네이버 accessToken이 유효하지 않습니다.".equals(result)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        } else if ("임시 가입 대기 중인 사용자입니다.".equals(result)) {
            return ResponseEntity.status(HttpStatus.OK).body(result);
        } else if (result.startsWith("네이버 회원가입 처리 중 오류 발생")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
        //오류같은경우 다시 로그인 화면으로 이동

        return ResponseEntity.ok(result); //네이버 인증받으면 프로필작성 페이지로 이동
    }

    /**
     * 추가 프로필 입력 후 최종 가입 완료
     */
    @PostMapping("/naversignup/complete")
    public ResponseEntity<String> completeRegistration(@RequestBody UserDTO userDTO) {
        String result = userService.completeRegistration(userDTO);

        if ("가입완료".equals(result)) {
            return ResponseEntity.ok(result);
        } else if (result.startsWith("회원가입 처리 중 오류 발생")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
    @PostMapping("/login")
    public ResponseEntity<String> loginWithSNS(@RequestParam String accessToken, HttpSession session) {
        try {
            UserDTO user = userService.loginWithSNS(accessToken);

            // 세션에 사용자 정보 저장
            session.setAttribute("user", user);
            return ResponseEntity.ok("로그인 성공");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage()); // 401 Unauthorized
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found (회원가입 필요)
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그인 처리 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 로그인 상태 확인 (세션 정보 조회)
     */
    @GetMapping("/session")
    public ResponseEntity<?> getSessionUser(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body("로그인 필요");
        }
        return ResponseEntity.ok(user);
    }

    /**
     * 로그아웃 (세션 삭제)
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate(); // 세션 삭제
        return ResponseEntity.ok("로그아웃 성공");
    }
    
    
}

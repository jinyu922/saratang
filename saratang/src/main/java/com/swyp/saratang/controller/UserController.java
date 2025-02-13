package com.swyp.saratang.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.service.UserService;


@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

     
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
   @PostMapping("/signup/complete")
   public ResponseEntity<ApiResponseDTO<Void>> completeRegistration(@RequestBody UserDTO userDTO) {
       ResponseEntity<ApiResponseDTO<Void>> result = userService.completeRegistration(userDTO);
       return result; 
   }
   
   /**
    * 네이버 & 카카오 로그인 처리
    */
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
   @PostMapping("/logout")
   public ResponseEntity<ApiResponseDTO<String>> logout(HttpSession session) {
       session.invalidate(); // 세션 삭제
       
       return ResponseEntity.ok(new ApiResponseDTO<>(200, "로그아웃 성공", "세션이 정상적으로 삭제되었습니다."));
   }

    
}

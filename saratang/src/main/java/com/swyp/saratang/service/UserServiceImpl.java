package com.swyp.saratang.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.swyp.saratang.mapper.UserMapper;
import com.swyp.saratang.mapper.TemporaryUserMapper;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.TemporaryUserDTO;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TemporaryUserMapper temporaryUserMapper; 

    @Autowired
    private NaverAuthService naverAuthService;

    @Autowired
    private KakaoAuthService kakaoAuthService;

    /**
     * 소셜 회원가입 처리 (네이버, 카카오)
     */
    @Override
    public ResponseEntity<ApiResponseDTO<Void>> registerUser(UserDTO userDTO) {
        try {
            if (existsBySocialId(userDTO.getSocialId())) {
                return ResponseEntity.status(409).body(new ApiResponseDTO<>(409, "이미 가입된 사용자", null));
            }

            userMapper.registerNaverUser(userDTO); // 네이버와 카카오 같은 테이블 사용

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "회원가입 성공", null));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "회원가입 처리 중 오류 발생: " + e.getMessage(), null));
        }
    }

    @Override
    public boolean existsBySocialId(String socialId) {
        return userMapper.countBySocialId(socialId) > 0;
    }

    /**
     * 네이버 또는 카카오 회원가입 요청 (accessToken 이용)
     */
    @Override
    public ResponseEntity<ApiResponseDTO<Void>> registerUserWithToken(String accessToken, String provider) {
        try {
            TemporaryUserDTO tuserDTO;

            if ("naver".equalsIgnoreCase(provider)) {
                tuserDTO = naverAuthService.getNaverUserInfo(accessToken).block();
            } else if ("kakao".equalsIgnoreCase(provider)) {
                tuserDTO = kakaoAuthService.getKakaoUserInfo(accessToken).block();
            } else {
                return ResponseEntity.status(400).body(new ApiResponseDTO<>(400, "지원하지 않는 OAuth 제공자입니다.", null));
            }

            if (tuserDTO == null) {
                return ResponseEntity.status(400).body(new ApiResponseDTO<>(400, "연동 실패", null));
            }

            if (existsBySocialId(tuserDTO.getSocialId())) {
                return ResponseEntity.status(409).body(new ApiResponseDTO<>(409, "이미 가입된 사용자", null));
            }

            temporaryUserMapper.saveTemporaryUser(tuserDTO);

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "추가 프로필 입력 필요", null));

        } catch (WebClientResponseException.Unauthorized e) {
            return ResponseEntity.status(401).body(new ApiResponseDTO<>(401, "OAuth accessToken이 유효하지 않습니다.", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "회원가입 처리 중 오류 발생: " + e.getMessage(), null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDTO<Void>> completeRegistration(UserDTO userDTO) {
        try {
            int count = temporaryUserMapper.countByTempSocialId(userDTO.getSocialId());

            if (count == 0) {
                return ResponseEntity.status(400).body(new ApiResponseDTO<>(400, "인증 후 진행 필요", null));
            }

            registerUser(userDTO);

            temporaryUserMapper.deleteBySocialId(userDTO.getSocialId());

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "가입 완료", null));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "회원가입 처리 중 오류 발생: " + e.getMessage(), null));
        }
    }

    /**
     * 소셜 로그인 (네이버, 카카오)
     */
    @Override
    public ResponseEntity<ApiResponseDTO<UserDTO>> loginWithSNS(String accessToken, String provider) {
        try {
            UserDTO snsUser;

            if ("naver".equalsIgnoreCase(provider)) {
                snsUser = naverAuthService.getNaverUser(accessToken).block();
            } else if ("kakao".equalsIgnoreCase(provider)) {
                snsUser = kakaoAuthService.getKakaoUser(accessToken).block();
            } else {
                return ResponseEntity.status(400).body(new ApiResponseDTO<>(400, "지원하지 않는 OAuth 제공자입니다.", null));
            }

            if (snsUser == null) {
                return ResponseEntity.status(401).body(new ApiResponseDTO<>(401, "유효하지 않은 accessToken 입니다.", null));
            }

            UserDTO existingUser = userMapper.findBySocialId(snsUser.getSocialId());

            if (existingUser == null) {
                return ResponseEntity.status(404).body(new ApiResponseDTO<>(404, "회원가입 필요", null));
            }

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "로그인 성공", existingUser));

        } catch (WebClientResponseException.Unauthorized e) {
            return ResponseEntity.status(401).body(new ApiResponseDTO<>(401, "유효하지 않은 accessToken 입니다.", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponseDTO<>(500, "로그인 처리 중 오류 발생: " + e.getMessage(), null));
        }
    }
}

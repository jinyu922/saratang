package com.swyp.saratang.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.swyp.saratang.mapper.UserMapper;
import com.swyp.saratang.mapper.TemporaryUserMapper;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.model.TemporaryUserDTO;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TemporaryUserMapper temporaryUserMapper; // 임시 유저 저장을 위한 매퍼

    @Autowired
    private NaverAuthService naverAuthService;
    
    @Override
    public void registerNaverUser(UserDTO userDTO) {
    	System.out.println("[registerNaverUser] 신규 회원가입 진행: " + userDTO);
        if (existsBySocialId(userDTO.getSocialId())) {
            throw new IllegalArgumentException("이미 가입된 네이버 사용자입니다.");
        }
        
        userMapper.registerNaverUser(userDTO);
    }

    @Override
    public boolean existsBySocialId(String socialId) {
        return userMapper.countBySocialId(socialId) > 0;
    }

    @Override
    public String registerNaverUserWithToken(String accessToken) {
        try {
            // 네이버 API에서 사용자 정보 가져오기
            TemporaryUserDTO tuserDTO = naverAuthService.getNaverUserInfo(accessToken).block();

            if (tuserDTO == null) {
                return "연동 실패";
            }

            // ② **정식 가입된 사용자 먼저 확인 (Users 테이블 조회)**
            if (existsBySocialId(tuserDTO.getSocialId())) {
                return "이미 가입된 사용자";
            }

            // ③ 신규 사용자 임시 저장
            temporaryUserMapper.saveTemporaryUser(tuserDTO);

            // ④ 프론트에서 추가 프로필 입력 페이지로 이동
            return "추가 프로필 입력 필요";

        } catch (WebClientResponseException.Unauthorized e) {
            return "네이버 accessToken이 유효하지 않습니다.";
        } catch (Exception e) {
            return "네이버 회원가입 처리 중 오류 발생: " + e.getMessage();
        }
    }

    @Override
    public String completeRegistration(UserDTO userDTO) {
        try {
        	 // ① 임시 가입 정보 개수 확인
            int count = temporaryUserMapper.countByTempSocialId(userDTO.getSocialId());

            // ② 1개 이상이면 통과, 0개면 예외 발생
            if (count == 0) {
                throw new RuntimeException("인증후 진행바랍니다.");
            }
         
            // ② UserDTO로 변환하여 최종 가입 진행
            registerNaverUser(userDTO);

            // ③ 임시 데이터 삭제
            temporaryUserMapper.deleteBySocialId(userDTO.getSocialId());

            return "가입완료";

        } catch (Exception e) {
            return "회원가입 처리 중 오류 발생: " + e.getMessage();
        }
    }
    
    
    @Override
    public UserDTO loginWithSNS(String accessToken) {
        try {
            // 네이버 API에서 사용자 정보 가져오기
            UserDTO snsUser = naverAuthService.getNaverUser(accessToken).block();

            if (snsUser == null) {
                throw new IllegalArgumentException("유효하지 않은 네이버 accessToken 입니다.");
            }

            // DB에서 해당 socialId가 있는지 확인
            UserDTO existingUser = userMapper.findBySocialId(snsUser.getSocialId());

            if (existingUser == null) {
                throw new IllegalStateException("회원가입이 되어 있지 않습니다.");
            }

            return existingUser; // 로그인 성공

        } catch (WebClientResponseException.Unauthorized e) {
            throw new IllegalArgumentException("유효하지 않은 네이버 accessToken 입니다."); // 401 Unauthorized
        } catch (Exception e) {
            throw new RuntimeException("로그인 처리 중 오류 발생: " + e.getMessage());
        }
    }
}
package com.swyp.saratang.service;
import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.UserDTO;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * AuthService는 인증 관련 기능을 제공하는 서비스 인터페이스입니다.
 */
public interface AuthService {

    /**
     * 프론트에서 로그인 버튼을 눌렀을 때 네이버/카카오 로그인 URL을 반환하는 메서드
     */
    String getAuthUrl(String provider);

    /**
     * SNS 로그인 - OAuth 인증 코드로 Access Token을 발급받고 로그인 처리
     */
    public UserDTO processOAuthLogin(String provider, String code);

    /**
     * 인증 코드로 Access Token 요청
     */
    String getAccessToken(String provider, String code);

    /**
     * Access Token을 이용해 사용자 프로필 조회
     */
    UserDTO getUserProfile(String provider, String accessToken);
    
    //토큰으로 userId 가져오기
    public Integer validateJwtAndGetUserId(HttpServletRequest request, String token);
}
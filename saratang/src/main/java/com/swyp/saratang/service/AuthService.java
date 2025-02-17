package com.swyp.saratang.service;
import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.UserDTO;

import java.util.Map;

/**
 * AuthService는 인증 관련 기능을 제공하는 서비스 인터페이스입니다.
 */
public interface AuthService {
	public UserDTO snsLogin(String provider, Map<String, Object> userInfo, String sessionId);
    public String getAccessToken(String provider, String code);
    public UserDTO getUserProfile(String provider, String accessToken, String sessionId);
}

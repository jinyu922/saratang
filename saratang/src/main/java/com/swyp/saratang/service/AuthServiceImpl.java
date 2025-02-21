package com.swyp.saratang.service;

import java.util.Date;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.swyp.saratang.controller.AuthController;
import com.swyp.saratang.mapper.UserMapper;
import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.session.SessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
@Service
public class AuthServiceImpl implements AuthService {
	
	private static final Logger logger = LogManager.getLogger(AuthServiceImpl.class); 
	
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SessionManager sessionManager;

    
    private final RestTemplate restTemplate = new RestTemplate();


    @Value("${naver.client.id}")
    private String NAVER_CLIENT_ID;

    @Value("${naver.client.secret}")
    private String NAVER_CLIENT_SECRET;

    @Value("${naver.token.url}")
    private String NAVER_TOKEN_URL;

    @Value("${naver.profile.url}")
    private String NAVER_PROFILE_URL;

    @Value("${kakao.client.id}")
    private String KAKAO_CLIENT_ID;

    @Value("${kakao.client.secret}")
    private String KAKAO_CLIENT_SECRET;

    @Value("${kakao.token.url}")
    private String KAKAO_TOKEN_URL;

    @Value("${kakao.profile.url}")
    private String KAKAO_PROFILE_URL;

    @Value("${oauth.naver.redirect.uri}")
    private String NAVER_REDIRECT_URI;

    @Value("${oauth.kakao.redirect.uri}")
    private String KAKAO_REDIRECT_URI;

    @Override
    public UserDTO snsLogin(String provider, Map<String, Object> userInfo, String sessionId) {
        try {
            String authCode = (String) userInfo.get("code");
            String accessToken = getAccessToken(provider, authCode);

            UserDTO user = getUserProfile(provider, accessToken, sessionId);

            // ✅ 1. provider + socialId 기준으로 기존 회원 조회
            UserDTO existingUser = userMapper.findBySocialId(user.getSocialId(), provider);

            if (existingUser != null) {
                // ✅ 1-1. 프로필까지 입력 완료된 회원이면 로그인 성공 (200)
                if (existingUser.getProfileYn()) { 
                    sessionManager.setSession(sessionId, existingUser);
                    return existingUser;
                } else {
                    // ✅ 1-2. 가입은 되었지만 프로필 미작성 상태 (201)
                    sessionManager.setSession(sessionId, existingUser);
                    return existingUser;
                }
            }

            // ✅ 2. 같은 이메일을 가진 다른 provider의 계정이 존재하는지 확인
            UserDTO existingEmailUser = userMapper.findByEmail(user.getEmail());
            if (existingEmailUser != null) {
                throw new IllegalArgumentException("이미 가입된 이메일입니다.");
            }

            // ✅ 3. 신규 사용자 - 프로필 미작성 상태로 DB 저장 (201)
            user.setProfileYn(false);
            userMapper.insertUser(user);

            sessionManager.setSession(sessionId, user);
            return user;

        } catch (IllegalArgumentException e) {
            throw e; // "이미 가입된 이메일입니다." 예외 발생
        } catch (RuntimeException e) {
            logger.error("SNS 로그인 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("토큰 요청 중 오류", e);
        }
    }


    /**
     * 인증 코드로 Access Token 요청
     */
    @Override
    public String getAccessToken(String provider, String code) {
        String tokenUrl = provider.equals("naver") ? NAVER_TOKEN_URL : KAKAO_TOKEN_URL;
        String clientId = provider.equals("naver") ? NAVER_CLIENT_ID : KAKAO_CLIENT_ID;
        String clientSecret = provider.equals("naver") ? NAVER_CLIENT_SECRET : KAKAO_CLIENT_SECRET;
        String redirectUri = provider.equals("naver") ? NAVER_REDIRECT_URI : KAKAO_REDIRECT_URI; 

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("code", code);
        params.add("redirect_uri", redirectUri); // ✅ 네이버/카카오의 `redirect_uri` 적용

        if (provider.equals("naver") || (KAKAO_CLIENT_SECRET != null && !KAKAO_CLIENT_SECRET.isEmpty())) {
            params.add("client_secret", clientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = responseEntity.getBody();

            if (response == null || response.get("access_token") == null) {
                throw new RuntimeException(provider + " Access Token 요청 실패: 응답이 null 또는 access_token 없음");
            }

            return (String) response.get("access_token");

        } catch (Exception e) {
            System.err.println(provider + " Access Token 요청 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("Access Token 요청 실패", e);
        }
    }

    /**
     * Access Token을 이용해 사용자 프로필 조회
     */
    @Override
    public UserDTO getUserProfile(String provider, String accessToken, String sessionId) {
        String profileUrl = provider.equals("naver") ? NAVER_PROFILE_URL : KAKAO_PROFILE_URL;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        logger.info("Sending request to: {}", profileUrl);
        ResponseEntity<Map> responseEntity = restTemplate.exchange(profileUrl, HttpMethod.GET, entity, Map.class);
        logger.info("Response received: {}", responseEntity.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> response = (Map<String, Object>) responseEntity.getBody();

        String socialId, email, nickname;

        if (provider.equals("naver")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = (Map<String, Object>) response.get("response");
            socialId = (String) responseData.get("id");
            email = (String) responseData.get("email");
            nickname = (String) responseData.get("nickname");
        } else { // Kakao
            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) response.get("properties");
            socialId = String.valueOf(response.get("id"));
            email = (String) kakaoAccount.get("email");
            nickname = (String) properties.get("nickname");
        }

        // DB 저장 없이 UserDTO만 생성하여 반환
        UserDTO newUser = new UserDTO();
        newUser.setSocialId(socialId);
        newUser.setAuthProvider(provider);
        newUser.setEmail(email);
        newUser.setNickname(nickname);
        newUser.setEmailVerified(true);
        newUser.setRole("regular");
        newUser.setIsActive(false);
        newUser.setRegDate(new Date());

        return newUser;
    }
}
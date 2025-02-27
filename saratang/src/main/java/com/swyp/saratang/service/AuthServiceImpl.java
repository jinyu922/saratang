package com.swyp.saratang.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.swyp.saratang.mapper.UserMapper;
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

    // 네이버 API 설정
    @Value("${naver.client.id}") private String NAVER_CLIENT_ID;
    @Value("${naver.client.secret}") private String NAVER_CLIENT_SECRET;
    @Value("${naver.token.url}") private String NAVER_TOKEN_URL;
    @Value("${naver.profile.url}") private String NAVER_PROFILE_URL;
    @Value("${naver.redirect.uri}") private String NAVER_REDIRECT_URI;

    // 카카오 API 설정
    @Value("${kakao.client.id}") private String KAKAO_CLIENT_ID;
    @Value("${kakao.client.secret}") private String KAKAO_CLIENT_SECRET;
    @Value("${kakao.token.url}") private String KAKAO_TOKEN_URL;
    @Value("${kakao.profile.url}") private String KAKAO_PROFILE_URL;
    @Value("${kakao.redirect.uri}") private String KAKAO_REDIRECT_URI;

    @Override
    public String getAuthUrl(String provider) {
        if ("naver".equals(provider)) {
            return "https://nid.naver.com/oauth2.0/authorize?response_type=code"
                    + "&client_id=" + NAVER_CLIENT_ID
                    + "&redirect_uri=" + NAVER_REDIRECT_URI  // provider 추가 X
                    + "&state=" + UUID.randomUUID();
        } else if ("kakao".equals(provider)) {
            return "https://kauth.kakao.com/oauth/authorize?response_type=code"
                    + "&client_id=" + KAKAO_CLIENT_ID
                    + "&redirect_uri=" + KAKAO_REDIRECT_URI; //  provider 추가 X
        } else {
            throw new IllegalArgumentException("지원되지 않는 provider: " + provider);
        }
    }


    /**
     * ✅ OAuth 로그인 처리
     */
    @Override
    public UserDTO processOAuthLogin(String provider, String code, String sessionId) {
        String accessToken = getAccessToken(provider, code);
        return getUserProfile(provider, accessToken);
    }
    
    /**
     * ✅ 인증 코드로 액세스 토큰 요청
     */
    @Override
    public String getAccessToken(String provider, String code) {
        String tokenUrl = provider.equals("naver") ? NAVER_TOKEN_URL : KAKAO_TOKEN_URL;
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", provider.equals("naver") ? NAVER_CLIENT_ID : KAKAO_CLIENT_ID);
        params.add("client_secret", provider.equals("naver") ? NAVER_CLIENT_SECRET : KAKAO_CLIENT_SECRET);
        params.add("code", code);
        params.add("redirect_uri", provider.equals("naver") ? NAVER_REDIRECT_URI : KAKAO_REDIRECT_URI);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, new HttpHeaders());

        try {
            logger.info("OAuth 토큰 요청 시작 - URL: {}, Params: {}", tokenUrl, params);
            ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);
            
            if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
                logger.error("Access Token 요청 실패 - 응답: {}", response.getBody());
                throw new RuntimeException("Access Token 요청 실패");
            }

            logger.info("Access Token 요청 성공 - Token: {}", response.getBody().get("access_token"));
            return (String) response.getBody().get("access_token");

        } catch (Exception e) {
            logger.error("OAuth Access Token 요청 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("OAuth Access Token 요청 실패", e);
        }
    }

    /**
     * ✅ Access Token을 이용해 사용자 프로필 조회
     */
    @Override
    public UserDTO getUserProfile(String provider, String accessToken) {
        String profileUrl = provider.equals("naver") ? NAVER_PROFILE_URL : KAKAO_PROFILE_URL;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        //debug 김준혁
        ResponseEntity<Map> responseEntity = restTemplate.exchange(profileUrl, HttpMethod.GET, entity, Map.class);
        System.out.println("getuserprofile메소드1:"+responseEntity);
        Map<String, Object> response=responseEntity.getBody();
        System.out.println("getuserprofile메소드2:"+response);


        if (response == null) {
            throw new RuntimeException("사용자 정보 요청 실패: 응답이 null");
        }

        String socialId, email, nickname;

        if ("naver".equals(provider)) {
            Map<String, Object> responseData = (Map<String, Object>) response.get("response");
            socialId = (String) responseData.get("id");
            email = (String) responseData.get("email");
            nickname = (String) responseData.get("nickname");
        } else {
            Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
            Map<String, Object> properties = (Map<String, Object>) response.get("properties");
            socialId = String.valueOf(response.get("id"));
            email = (String) kakaoAccount.get("email");
            nickname = (String) properties.get("nickname");
        }

        //  1. 기존 사용자 조회
        UserDTO existingUser = userMapper.findBySocialId(socialId, provider);
        if (existingUser != null) {
        	if (!existingUser.getIsActive()) {
                throw new IllegalArgumentException("탈퇴한 회원입니다.");
            }
            return existingUser;  // ✅ 로그인 성공 시 그대로 반환
        }

        UserDTO existingEmailUser = userMapper.findByEmail(email);
        if (existingEmailUser != null) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");  // ✅ 예외 발생 -> `handleLoginRedirect()`에서 402로 변환
        }


        //  3. 신규 회원 가입 (프로필 미작성 상태)
        UserDTO newUser = new UserDTO();
        newUser.setSocialId(socialId);
        newUser.setAuthProvider(provider);
        newUser.setEmail(email);
        newUser.setNickname(nickname);
        newUser.setRegDate(new Date());
        newUser.setProfileYn(false);  // 프로필 입력 필요 상태
        newUser.setRole("regular");

        userMapper.insertUser(newUser);
        return newUser;  //  컨트롤러에서 201 응답 처리
    }
}

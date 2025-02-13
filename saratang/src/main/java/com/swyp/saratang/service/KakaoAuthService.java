package com.swyp.saratang.service;

import com.swyp.saratang.model.TemporaryUserDTO;
import com.swyp.saratang.model.UserDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 카카오 OAuth를 통해 사용자 정보를 가져오는 서비스
 */
@Service
public class KakaoAuthService {
    private final WebClient webClient;


    public KakaoAuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://kapi.kakao.com").build();
    }

    public Mono<TemporaryUserDTO> getKakaoUserInfo(String accessToken) {
        return webClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserResponse.class) 
                .map(response -> {
                    String email = response.getKakaoAccount() != null ? response.getKakaoAccount().getEmail() : null;
                    boolean emailVerified = response.getKakaoAccount() != null && response.getKakaoAccount().isEmailVerified();

                    return new TemporaryUserDTO(
                            response.getId(),  // 카카오 사용자 고유 ID
                            email,             // 이메일 (null 가능)
                            response.getId(),  // socialId는 카카오 ID 사용
                            "kakao",           // authProvider를 "kakao"로 설정
                            emailVerified      // 이메일 인증 여부
                    );
                });
    }


  
    public Mono<UserDTO> getKakaoUser(String accessToken) {
        return webClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserResponse.class) 
                .map(response -> {
                    String email = response.getKakaoAccount() != null ? response.getKakaoAccount().getEmail() : null;
                    boolean emailVerified = response.getKakaoAccount() != null && response.getKakaoAccount().isEmailVerified();

                    return new UserDTO(
                            response.getId(),  // 카카오 사용자 고유 ID
                            email,             // 이메일
                            response.getId(),  // socialId는 카카오 ID 사용
                            "kakao",           // authProvider를 "kakao"로 설정
                            emailVerified      // 이메일 인증 여부
                    );
                });
    }

    /**
     * 카카오 API 응답을 매핑하기 위한 내부 클래스
     */
    static class KakaoUserResponse {
        private String id;
        private KakaoAccount kakao_account;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public KakaoAccount getKakaoAccount() { return kakao_account; }
        public void setKakaoAccount(KakaoAccount kakao_account) { this.kakao_account = kakao_account; }

        static class KakaoAccount {
            private String email;
            private boolean is_email_verified; 

            public String getEmail() { return email; }
            public void setEmail(String email) { this.email = email; }

            public boolean isEmailVerified() { return is_email_verified; }
            public void setEmailVerified(boolean is_email_verified) { this.is_email_verified = is_email_verified; }
        }
    }
}

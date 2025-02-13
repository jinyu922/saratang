package com.swyp.saratang.service;

import com.swyp.saratang.model.TemporaryUserDTO;
import com.swyp.saratang.model.UserDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 네이버 OAuth를 통해 사용자 정보를 가져오는 서비스
 */
@Service
public class NaverAuthService {
	
	
	
    private final WebClient webClient;

 
    public NaverAuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://openapi.naver.com").build();
    }

  
    public Mono<TemporaryUserDTO> getNaverUserInfo(String accessToken) {
        return webClient.get()
                .uri("/v1/nid/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(NaverUserResponse.class) // 네이버 응답을 객체로 변환
                .map(response -> new TemporaryUserDTO(
                        response.getResponse().getNickname(),  // 사용자 닉네임
                        response.getResponse().getEmail(),     // 사용자 이메일
                        response.getResponse().getId(),        // 네이버 고유 ID
                        "naver",                               // authProvider를 "naver"로 고정
                        true                                   // 이메일 인증 여부 (네이버는 기본적으로 인증됨)
                ));
    }
    
    public Mono<UserDTO> getNaverUser(String accessToken) {
        return webClient.get()
                .uri("/v1/nid/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(NaverUserResponse.class) // 네이버 응답을 객체로 변환
                .map(response -> new UserDTO(
                        response.getResponse().getNickname(),  
                        response.getResponse().getEmail(),     
                        response.getResponse().getId(),        
                        "naver", 
                        true                                   
                ));
    }

   
    static class NaverUserResponse {
        private NaverResponse response;

        public NaverResponse getResponse() { return response; }
        public void setResponse(NaverResponse response) { this.response = response; }

       
        static class NaverResponse {
            private String id;      // 네이버 사용자 고유 ID
            private String email;   // 이메일
            private String nickname;// 닉네임

            public String getId() { return id; }
            public void setId(String id) { this.id = id; }

            public String getEmail() { return email; }
            public void setEmail(String email) { this.email = email; }

            public String getNickname() { return nickname; }
            public void setNickname(String nickname) { this.nickname = nickname; }
        }
    }
}
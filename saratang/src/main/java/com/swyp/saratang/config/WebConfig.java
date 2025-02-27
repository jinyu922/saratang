package com.swyp.saratang.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration  // 이 클래스는 Spring의 설정 클래스로 인식
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // ✅ 모든 엔드포인트 허용
                .allowedOrigins(
                        "http://localhost:3000", 
                        "http://localhost:8080", 
                        "http://223.130.162.183",
                        "https://saratangmaratang.vercel.app"
                )  // ✅ 정확한 출처(origin)만 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // ✅ OPTIONS 추가
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept")  // ✅ 명확한 헤더 지정
                .exposedHeaders("Authorization", "Set-Cookie")  // ✅ 클라이언트가 응답 헤더 읽을 수 있도록 설정
                .allowCredentials(true);  // ✅ 쿠키 포함한 요청 허용
    }
}
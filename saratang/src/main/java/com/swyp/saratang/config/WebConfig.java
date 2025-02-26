package com.swyp.saratang.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration  // 이 클래스는 Spring의 설정 클래스로 인식
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 모든 경로에 대해 CORS 설정을 추가
        registry.addMapping("/*")  // 모든 경로에 대해 CORS 허용
                .allowedOrigins("http://localhost:3000/", "http://localhost:8080","https://saratangmaratang.vercel.app/")  // 프론트엔드 도메인 추가 (localhost:3000과 localhost:8080)
                .allowedMethods("GET", "POST", "PUT", "DELETE")  // 허용할 HTTP 메소드
                .allowedHeaders("")  // 모든 헤더 허용
                .allowCredentials(true);  // 쿠키를 포함한 요청 허용
    }
}
package com.swyp.saratang.model;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TemporaryUserDTO {
	private Long id;
    private String username;
    private String email;
    private String socialId;
    private String authProvider;
    private Boolean emailVerified;
    private LocalDateTime createdAt = LocalDateTime.now(); // 현재 시간 자동 설정


    public TemporaryUserDTO(String username, String email, String socialId, Boolean emailVerified) {
        this.username = username;
        this.email = email;
        this.socialId = socialId;
        this.authProvider = "naver"; // 네이버로 고정
        this.emailVerified = emailVerified;
    }

}


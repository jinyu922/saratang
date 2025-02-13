package com.swyp.saratang.model;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TemporaryUserDTO {
	private Long id;
    private String nickname;
    private String email;
    private String socialId;
    private String authProvider;
    private Boolean emailVerified;
    private LocalDateTime regDate = LocalDateTime.now();


    public TemporaryUserDTO(String nickname, String email, String socialId, String authProvider, Boolean emailVerified) {
    		this.nickname = nickname;
	        this.email = email;
	        this.socialId = socialId;
	        this.authProvider = authProvider; 
	        this.emailVerified = emailVerified;
	    }

}


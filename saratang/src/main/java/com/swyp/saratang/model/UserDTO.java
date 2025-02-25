package com.swyp.saratang.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
public class UserDTO {
		private Integer id;
	    private String username;
	    private String nickname;
	    private String email;
	    private String password;
	    private String socialId; 
	    private String authProvider;
	    private Boolean emailVerified;
	    private String role; 
	    private String bio;
	    private Integer weight;
	    private Integer height;
	    private Date birthDate;
	    private String gender; 
	    private Boolean isActive;
	    private Date regDate;
	    private String topSize;
	    private String bottomSize;
	    private Integer footSize;
	    private Boolean profileYn;
	    private String color;
	    private Integer credits; // 포인트 양
	    private Integer Icon;
	    
	    public UserDTO() {}
	    
	 // ✅ 모든 필드를 포함한 생성자
	    public UserDTO(String socialId, String authProvider, String email, String nickname, 
	                   Date regDate, boolean profileYn, String role, boolean emailVerified) {
	        this.socialId = socialId;
	        this.authProvider = authProvider;
	        this.email = email;
	        this.nickname = nickname;
	        this.regDate = regDate;
	        this.profileYn = profileYn;
	        this.role = role;
	        this.emailVerified = emailVerified;
	    }
 
}



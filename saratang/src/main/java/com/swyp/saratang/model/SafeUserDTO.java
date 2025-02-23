package com.swyp.saratang.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
public class SafeUserDTO {//응답 시, UserDTO에서 패스워드같은 민감한 유저 정보 제외
		private Integer id;
	    private String username;
	    private String nickname;
	    private String email;
	    private String socialId; 
	    private String authProvider;
	    private String role; 
	    private String bio;
	    private Integer weight;
	    private Integer height;
	    private Date birthDate;
	    private String gender; 
	    private Date regDate;
	    private Integer topSize;
	    private Integer bottomSize;
	    private Integer footSize;
	    
	    public SafeUserDTO(UserDTO user) {
	        this.id = user.getId();
	        this.username = user.getUsername();
	        this.nickname = user.getNickname();
	        this.email = user.getEmail();
	        this.authProvider = user.getAuthProvider();
	        this.role = user.getRole();
	        this.bio = user.getBio();
	        this.weight = user.getWeight();
	        this.height = user.getHeight();
	        this.birthDate = user.getBirthDate();
	        this.gender = user.getGender();
	        this.regDate = user.getRegDate();
	        this.topSize = user.getTopSize();
	        this.bottomSize = user.getBottomSize();
	        this.footSize = user.getFootSize();
	    }
}

package com.swyp.saratang.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDTO {
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
	    private Integer topSize;
	    private Integer bottomSize;
	    private Integer footSize;
	    
	    public UserDTO(String username, String email, String socialId, String authProvider, Boolean emailVerified) {
	        this.username = username;
	        this.email = email;
	        this.socialId = socialId;
	        this.authProvider = authProvider; 
	        this.emailVerified = emailVerified;
	    }
}

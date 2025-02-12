package com.swyp.saratang.model;

import java.util.Date;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;



@Data
public class UserDTO {
	    private String username;
	    private String email;
	    private String socialId; 
	    private String authProvider;
	    private Boolean emailVerified;
	    private String role; 
	    private String bio;
	    private Double weight;
	    private Double height;
	    private Integer age;
	    private String gender; 
	    private Boolean isActive;
	    private Date regdate;
	    private Integer currentIconId;
	    private Integer currentNameColorId;
	    
	    public UserDTO(String username, String email, String socialId, Boolean emailVerified) {
	        this.username = username;
	        this.email = email;
	        this.socialId = socialId;
	        this.authProvider = "naver"; // 네이버로 고정
	        this.emailVerified = emailVerified;
	    }
}

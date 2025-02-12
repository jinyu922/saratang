package com.swyp.saratang.service;
import java.util.List;
import com.swyp.saratang.model.UserDTO;

public interface UserService {
	void registerNaverUser(UserDTO userDTO);
	boolean existsBySocialId(String socialId); 
	public String registerNaverUserWithToken(String accessToken);
	public String completeRegistration(UserDTO userDTO);
	public UserDTO loginWithSNS(String accessToken);
}


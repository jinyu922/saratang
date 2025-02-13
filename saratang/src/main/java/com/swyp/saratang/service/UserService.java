package com.swyp.saratang.service;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.UserDTO;

public interface UserService {
    public ResponseEntity<ApiResponseDTO<Void>> registerUser(UserDTO userDTO);
	boolean existsBySocialId(String socialId); 
	public ResponseEntity<ApiResponseDTO<Void>> registerUserWithToken(String accessToken, String provider);
	public ResponseEntity<ApiResponseDTO<Void>> completeRegistration(UserDTO userDTO);
	public ResponseEntity<ApiResponseDTO<UserDTO>> loginWithSNS(String accessToken, String provider);
}


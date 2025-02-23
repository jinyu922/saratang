package com.swyp.saratang.service;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.PointDTO;
import com.swyp.saratang.model.UserDTO;

public interface UserService {

    void insertUser(UserDTO user);

    UserDTO getUserBySocialId(String socialId, String provider);
    
    UserDTO getUserById(Integer id);
    
    void newProfile(UserDTO user);
    
    void editProfile(UserDTO user); 
    
    void deleteUser(String socialId, String authProvider, String email);
    
    void changeUserColor(int Id, String newNicknameColor, int updatedCredits);
    
    List<PointDTO> getCreditHistoryByUserId(Integer userId);
    
    Integer getTotalCreditsByUserId(Integer userId);
    
    void changeUserColor(Integer userId, String newNicknameColor);
    
    void insertCreditHistory(Integer userId, String type, Integer credits, String description);

}


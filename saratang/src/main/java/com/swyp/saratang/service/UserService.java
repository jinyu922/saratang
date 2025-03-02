package com.swyp.saratang.service;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.PointDTO;
import com.swyp.saratang.model.SafeUserDTO;
import com.swyp.saratang.model.UserColorDTO;
import com.swyp.saratang.model.UserDTO;

public interface UserService {

    void insertUser(UserDTO user);

    UserDTO getUserBySocialId(String socialId, String provider);
    
    UserDTO getUserById(Integer id);
    
    SafeUserDTO getSafeUserById(Integer id);
    
    void newProfile(UserDTO user);
    
    void editProfile(UserDTO user); 
    
    void deleteUser(String socialId, String authProvider, String email);
    
    
    List<PointDTO> getCreditHistoryByUserId(Integer userId);
    
    Integer getTotalCreditsByUserId(Integer userId);
   
    
    void changeUserIcon(Integer userId, Integer newIconId);
    
    void insertCreditHistory(Integer userId, String type, Integer credits, String description);

    
    // 사용자가 보유한 닉네임 색상 목록 조회
    List<UserColorDTO> getUserColorsByUserId(int userId);

    // 닉네임 색상 변경 
    void changeUserColor(int userId, int colorId);

    // 닉네임 색상 구매 (포인트 3 차감)
    void purchaseUserColor(int userId, int colorId);

    // 사용자가 특정 색상을 보유하고 있는지 확인
    boolean isUserOwnsColor(int userId, int colorId);
    
    public UserColorDTO getCurrentColorByUserId(int userId);
    
    public List<UserColorDTO> getAllColors();

}


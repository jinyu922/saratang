package com.swyp.saratang.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.swyp.saratang.model.PointDTO;
import com.swyp.saratang.model.UserDTO;

import io.swagger.v3.oas.annotations.Parameter;

@Mapper
public interface UserMapper {
	

    UserDTO findBySocialId(@Param("socialId") String socialId, @Param("authProvider") String authProvider);
   
    UserDTO findByEmail(@Param("email") String email);
    
    UserDTO findById(@Param("id") Integer id);
 
    void insertUser(UserDTO user);
    
    void newUserProfile(UserDTO user);
    
    void editUserProfile(UserDTO user);

    void changeUserColor(@Param("id") Integer id, @Param("newNicknameColor") String newNicknameColor, @Param("updatedCredits") Integer updatedCredits);
    
    void deleteUser(@Param("socialId") String socialId, @Param("authProvider") String authProvider, @Param("email") String email);
    
    List<PointDTO> getCreditHistoryByUserId(@Param("userId") Integer userId);
    
    Integer getTotalCreditsByUserId(@Param("userId") Integer userId);
    
    void insertCreditHistory(@Param("userId") Integer userId, @Param("type") String type, 
            @Param("credits") Integer credits, @Param("description") String description);
    
    void changeUserColor(@Param("userId") Integer userId, @Param("newNicknameColor") String newNicknameColor);
    
    
}

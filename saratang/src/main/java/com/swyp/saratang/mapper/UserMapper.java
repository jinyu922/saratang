package com.swyp.saratang.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    
    void deleteUser(@Param("socialId") String socialId, @Param("authProvider") String authProvider, @Param("email") String email);
    
    
}

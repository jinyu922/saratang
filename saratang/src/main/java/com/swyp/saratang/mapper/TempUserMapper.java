package com.swyp.saratang.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.swyp.saratang.model.UserDTO;

@Mapper
public interface TempUserMapper {
	
    void insertTempUser(UserDTO user); 
    UserDTO findTempUserBySocialId(@Param("socialId") String socialId, @Param("authProvider") String authProvider);
    void deleteTempUser(@Param("socialId") String socialId, @Param("authProvider") String authProvider);
}

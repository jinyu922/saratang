package com.swyp.saratang.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.swyp.saratang.model.UserDTO;

@Mapper
public interface UserMapper {
	
	// 네이버 사용자 등록
    void registerNaverUser(UserDTO user);
    
    // 소셜 ID로 가입 여부 확인
    int countBySocialId(@Param("socialId") String socialId);
    
    UserDTO findBySocialId(@Param("socialId") String socialId);
}

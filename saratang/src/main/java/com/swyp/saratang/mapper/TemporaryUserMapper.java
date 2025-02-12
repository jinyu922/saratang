package com.swyp.saratang.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.swyp.saratang.model.TemporaryUserDTO;

@Mapper
public interface TemporaryUserMapper {

	 // 임시 사용자 저장
    void saveTemporaryUser(TemporaryUserDTO user);

    // 소셜 ID로 임시 사용자 찾기
    int countByTempSocialId(@Param("socialId") String socialId);

    // 임시 사용자 삭제 (가입 완료 후)
    void deleteBySocialId(@Param("socialId") String socialId);

}

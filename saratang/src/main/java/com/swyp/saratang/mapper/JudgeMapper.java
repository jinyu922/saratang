package com.swyp.saratang.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JudgeMapper {
	public void addJudgement(@Param("userId") int userId,@Param("postId") int postId,@Param("judgementType") String judgementType);
	
	public int existJudgeByUserIdAndPostId(@Param("userId") int userId, @Param("postId") int postId);

	public Map<String, Integer> countJudgementsByPostId(@Param("postId") int postId);
	
	public int countJudgementsByUserId(@Param("userId") int userId);
}

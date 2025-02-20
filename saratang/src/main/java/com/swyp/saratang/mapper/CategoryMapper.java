package com.swyp.saratang.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CategoryMapper {
	
    // 유저의 기존 카테고리 삭제
    void deleteCategoryByUserId(@Param("userId") int userId);
	
	public void saveCategory(@Param("userId") int userId,@Param("categoryId") int categoryId);
	
	public List<Integer> getCategoryList(@Param("userId") int userId);

}

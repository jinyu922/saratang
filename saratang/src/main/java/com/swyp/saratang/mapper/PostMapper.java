package com.swyp.saratang.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.swyp.saratang.data.RequestList;
import com.swyp.saratang.model.PostDTO;

@Mapper
public interface PostMapper {
	
	public List<Map<String, Object>> getFashionList(RequestList<?> requestList); //패션정보 조회
	
	public int getFashionListCount(); //패션정보 페이징
	
	public void createFashionPost(PostDTO postDTO); //패션정보 게시
}

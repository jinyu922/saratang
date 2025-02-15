package com.swyp.saratang.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.swyp.saratang.data.RequestList;

@Mapper
public interface PostMapper {
	List<Map<String, Object>> getFashionList(RequestList<?> requestList);
	int getFashionListCount();
}

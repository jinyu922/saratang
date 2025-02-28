package com.swyp.saratang.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.swyp.saratang.model.LinkAccessDTO;
import com.swyp.saratang.model.PointDTO;

@Mapper
public interface PointMapper {
	void addPoint(PointDTO pointDTO);
	void addLinkAccess(LinkAccessDTO linkAccessDTO);
}

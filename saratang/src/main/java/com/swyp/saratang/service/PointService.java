package com.swyp.saratang.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swyp.saratang.mapper.PointMapper;
import com.swyp.saratang.model.LinkAccessDTO;
import com.swyp.saratang.model.PointDTO;


@Service
public class PointService {
	
	@Autowired
	private PointMapper pointMapper;
	
	public void addPoint(PointDTO pointDTO) {
		pointMapper.addPoint(pointDTO);
	}
	
	public void addLinkAccess(LinkAccessDTO accessDTO) {
		pointMapper.addLinkAccess(accessDTO);
	}

}

package com.swyp.saratang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.PointDTO;
import com.swyp.saratang.service.PointService;

@RestController
public class PointController {

	@Autowired
	private PointService pointService;
	
	//todo 인증된 권한 사용자만 해당 api 사용가능하게 수정해야함
	@PostMapping("/point")
	public ApiResponseDTO<?> addPoint(PointDTO pointDTO) {
		pointService.addPoint(pointDTO);
		return new ApiResponseDTO<>(200, "포인트 추가 성공", null);
	}
}

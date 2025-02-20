package com.swyp.saratang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.CategoryDTO;
import com.swyp.saratang.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class CategoryController {
	
	@Autowired
	private CategoryService categoryService;
	
	@Operation(summary = "유저별 선호 카테고리 조회", description = "유저 id를 통해 선호 카테고리를 조회합니다")
	@GetMapping("/category")
	public ApiResponseDTO<CategoryDTO> getCategoryList(@RequestParam("userId") int userId) {
		CategoryDTO categoryDTO=categoryService.getCategoryList(userId);
		return new ApiResponseDTO<>(200, "입력 userId를 통해 해당 유저 선호 카테고리 데이터를 받아왔습니다.", categoryDTO);
	}
	
	@Operation(summary = "유저별 선호 카테고리 저장", description = "유저 id를 통해 선호 카테고리를 저장합니다<br>전체 지우고 다시 등록하는 방식이라 카테고리 수정도 해당 api를 이용하면 됩니다")
	@PostMapping("/category")
	public ApiResponseDTO<?> saveCategory(CategoryDTO categoryDTO){
		categoryService.saveCategory(categoryDTO);
		return new ApiResponseDTO<>(200, "입력 categoryDTO를 통해 해당 유저 선호 카테고리 데이터를 등록했습니다.", null);
	}
	

	
}

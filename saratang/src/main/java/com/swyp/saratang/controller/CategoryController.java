package com.swyp.saratang.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.config.JwtAuthUtil;
import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.CategoryDTO;
import com.swyp.saratang.service.CategoryService;
import com.swyp.saratang.session.SessionManager;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class CategoryController {
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private SessionManager sessionManager;
	
	@Autowired
    private JwtAuthUtil jwtAuthUtil; // JWT 유틸리티 주입
	
	@Operation(summary = "유저별 선호 카테고리 조회", description = "유저 id를 통해 선호 카테고리를 조회합니다")
	@GetMapping("/category")
	public ApiResponseDTO<CategoryDTO> getCategoryList(
			@Parameter(description = "요청유저 고유id, 로그인 세션 있으면 입력하지 않아도 됩니다")@RequestParam(required = false) Integer requestUserId,
			@RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        
		String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }
        
		CategoryDTO categoryDTO = categoryService.getCategoryList(Integer.parseInt(userId));
		return new ApiResponseDTO<>(200, "입력 userId를 통해 해당 유저 선호 카테고리 데이터를 받아왔습니다.", categoryDTO);
	}
	
	@Operation(summary = "유저별 선호 카테고리 저장", description = "유저 id를 통해 선호 카테고리를 저장합니다<br>전체 지우고 다시 등록하는 방식이라 카테고리 수정도 해당 api를 이용하면 됩니다")
	@PostMapping("/category")
	public ApiResponseDTO<?> saveCategory(
			@RequestBody CategoryDTO categoryDTO,
			@Parameter(description = "요청유저 고유id, 로그인 세션 있으면 입력하지 않아도 됩니다")@RequestParam(required = false) Integer requestUserId,
			@RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request){
        
		String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }
        
        categoryDTO.setUserId(Integer.parseInt(userId));
		categoryService.saveCategory(categoryDTO);
		return new ApiResponseDTO<>(200, "입력 categoryDTO를 통해 해당 유저 선호 카테고리 데이터를 등록했습니다.", null);
	}
}
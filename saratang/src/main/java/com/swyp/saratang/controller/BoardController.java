package com.swyp.saratang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.model.BoardDTO;
import com.swyp.saratang.service.BoardService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class BoardController {
	
	@Autowired
	private BoardService boardService;
	
	@Operation(summary = "패션정보 조회", description = "패션정보 리스트 반환, 페이징 지원")
	@GetMapping("/fashion")
	public ResponseEntity<?> getFashionList(        
			@RequestParam(defaultValue = "5") int size,
	        @RequestParam(defaultValue = "0") int page){
		Pageable pageable = PageRequest.of(page, size);
		return ResponseEntity.ok(boardService.getFashionList(pageable));
	}
	
	@Operation(summary = "할인정보 조회", description = "할인정보 리스트 반환, 페이징 지원")
	@GetMapping("/discount")
	public ResponseEntity<?> getDiscountList(        
			@RequestParam(defaultValue = "5") int size,
	        @RequestParam(defaultValue = "0") int page){
		Pageable pageable = PageRequest.of(page, size);
		return ResponseEntity.ok(boardService.getDiscountList(pageable));
	}
	
	@Operation(summary = "패션/할인정보 저장", description = "패션정보 저장<br>- 로그인 구현 전 까진 userId 도 추가로 입력<br>- 복수의 url은 콤마로 구분하여 입력 \"url1\",\"url2\"<br>- 각 필드 별 세부 정보는 하단 Schemas 의 BoardDTO 참고하세요 ")
	@PostMapping("/fashion")
	public ResponseEntity<String> createPost(@RequestBody BoardDTO boardDTO){
		boardService.createPost(boardDTO, boardDTO.getImageUrls());
		return ResponseEntity.ok("FashionPost create successfully!");
	}

}

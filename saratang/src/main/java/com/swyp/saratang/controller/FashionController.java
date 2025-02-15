package com.swyp.saratang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.model.PostDTO;
import com.swyp.saratang.service.FashionService;

@RestController
public class FashionController {
	
	@Autowired
	FashionService fashionService;
	
	@GetMapping("/fashion")
	public ResponseEntity<?> getFashionList(@PageableDefault(size = 5,page = 0) Pageable pageable){
		return ResponseEntity.ok(fashionService.getFashionList(pageable));
	}
	@PostMapping("/fashion")
	public ResponseEntity<String> createFashionPost(@RequestBody PostDTO postDTO){
	    if (postDTO.getUserId() == null) {
	        return ResponseEntity.badRequest().body("user_id cannot be null");
	    }
		fashionService.createFashionPost(postDTO);
		return ResponseEntity.ok("FashionPost create successfully!");
	}

}

package com.swyp.saratang.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.service.NCPStorageService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class NCPStorageController {
	
	@Autowired
	private NCPStorageService ncpStorageService;
	
	@Operation(summary = "이미지 파일 저장", description = "form-data 형식으로 file을 요구함, 여러개의 파일 동시에 가능, 반환값으로 NCP object storage URL 이 반환됩니다")
	@PostMapping("/upload")
	public ApiResponseDTO<?> uploadFile(@RequestParam("file") List<MultipartFile> files){
        try {
            List<String> fileUrls = ncpStorageService.uploadFiles(files);
            return new ApiResponseDTO<>(200, "파일 업로드 성공", fileUrls);
        } catch (Exception e) {
        	return new ApiResponseDTO<>(500, "서버측 오류"+e.getMessage(), null);
        }
		
	}

}

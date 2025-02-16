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

import com.swyp.saratang.service.NCPStorageService;

@RestController
public class NCPStorageController {
	
	@Autowired
	private NCPStorageService ncpStorageService;
	
	@PostMapping("/upload")
	public ResponseEntity<List<String>> uploadFile(@RequestParam("file") List<MultipartFile> files){
        try {
            List<String> fileUrls = ncpStorageService.uploadFiles(files);
            return ResponseEntity.ok(fileUrls);
        } catch (IOException e) {
        	return ResponseEntity.badRequest().body(Collections.singletonList("File upload failed: " + e.getMessage()));
        }
		
	}

}

package com.swyp.saratang.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Service
public class NCPStorageService {
	
	@Autowired
	private AmazonS3 s3Client;
	
	@Value("${ncp.storage.bucket}")
	private String bucketName;
	
    @Value("${ncp.storage.endpoint}")
    private String endPoint;
    
    //파일업로드
    public String uploadFile(MultipartFile file) throws IOException {
        // 파일명 = 랜덤시드 + 원본파일이름
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // 파일 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        // S3에 파일 업로드
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead)); // 공개 URL 사용

        // 업로드된 파일의 URL 반환
        return endPoint + "/" + bucketName + "/" + fileName;
    }
    
    //복수파일업로드
    public List<String> uploadFiles(List<MultipartFile> files) throws IOException {
    	List<String> fileUrls = new ArrayList<>();
    	
    	for (MultipartFile file : files) {
    		String fileUrl = uploadFile(file);
    		fileUrls.add(fileUrl);
    	}
    	
    	return fileUrls;
    }
    
    // 단일 파일 삭제 메서드
    public void deleteFile(String fileUrl) {
        try {
            // URL에서 파일 이름 추출
            String fileName = extractFileNameFromUrl(fileUrl);
            
            // S3에서 객체 삭제 (메타데이터 포함)
            s3Client.deleteObject(bucketName, fileName);
        } catch (Exception e) {
            // 로깅 또는 예외 처리
            throw new RuntimeException("파일 삭제 중 오류 발생: " + e.getMessage(), e);
        }
    }

    // 여러 파일 삭제 메서드
    public void deleteFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {//아무것도 없으면 실행하지 않음
            return;
        }
        for (String fileUrl : fileUrls) {
            deleteFile(fileUrl);
        }
    }

    // URL에서 파일 이름 추출 유틸리티 메서드
    private String extractFileNameFromUrl(String fileUrl) {
        // 엔드포인트와 버킷 이름을 제외한 파일 이름 추출
        String endpoint = endPoint + "/" + bucketName + "/";
        return fileUrl.replace(endpoint, "");
    }
}

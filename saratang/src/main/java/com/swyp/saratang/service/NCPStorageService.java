package com.swyp.saratang.service;

import java.io.IOException;
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
}

package com.swyp.saratang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.swyp.saratang.model.IconDTO;
import com.swyp.saratang.service.IconService;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/icons")
public class IconController {

	private static final Logger logger = LogManager.getLogger(IconController.class);

	//  환경 변수에서 아이콘 저장 디렉토리 가져오기
    @Value("${icon.directory}")
    private String iconDirectory;


    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getIcon(@PathVariable String filename) {
        try {
            // Windows 호환 경로 처리 (iconDirectory + filename)
            Path filePath = Paths.get(iconDirectory, filename).normalize();
            logger.info("📌 요청된 파일: {}", filePath.toAbsolutePath());

            // 파일이 존재하는지 확인
            if (!Files.exists(filePath)) {
                logger.error("❌ 파일이 존재하지 않음: {}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            // 파일 리소스 로드
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                logger.error("❌ 파일이 존재하지만 읽을 수 없음: {}", filePath.toAbsolutePath());
                return ResponseEntity.badRequest().build();
            }

            logger.info("✅ 파일 제공: {}", filePath.toAbsolutePath());

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            logger.error("❌ 파일 경로가 잘못됨: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("❌ 알 수 없는 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }


    @Autowired
    private IconService iconService;

    // 서버 URL을 환경 변수에서 가져오기
    @Value("${server.base.url}")
    private String serverBaseUrl;

    // 모든 아이콘 정보 가져오기 (파일 URL 포함)
    @GetMapping("/all")
    public List<IconDTO> getAllIcons() {
        List<IconDTO> icons = iconService.getAllIcons();

        // 환경 변수 기반으로 파일 URL 생성
        return icons.stream()
                .peek(icon -> icon.setFileUrl(serverBaseUrl + "/icons/" + icon.getFilename()))
                .collect(Collectors.toList());
    }
}

package com.swyp.saratang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.IconDTO;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.service.IconService;
import com.swyp.saratang.session.SessionManager;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/icons")
public class IconController {
	
	
	@Autowired
    private SessionManager sessionManager;
	
	private static final Logger logger = LogManager.getLogger(IconController.class);

	//  환경 변수에서 아이콘 저장 디렉토리 가져오기
    @Value("${icon.directory}")
    private String iconDirectory;

    @GetMapping("/me")
    @Operation(
            summary = "현재 로그인한 사용자의 아이콘 조회",
            description = "세션 정보를 이용하여 현재 로그인한 사용자의 아이콘 정보를 반환합니다."
        )
        @ApiResponse(responseCode = "200", description = "아이콘 정보 조회 성공")
        @ApiResponse(responseCode = "401", description = "세션 만료로 인한 인증 오류")
        @ApiResponse(responseCode = "404", description = "사용자의 아이콘이 설정되지 않음")
    public ResponseEntity<ApiResponseDTO<IconDTO>> getUserIcon(HttpSession session) {
        UserDTO sessionUser = sessionManager.getSession(session.getId());

        // 세션이 없으면 401 Unauthorized 반환
        if (sessionUser == null) {
            logger.warn("세션이 만료됨: 아이콘 정보 제공 불가");
            return ResponseEntity.status(401)
                    .body(new ApiResponseDTO<>(401, "세션이 만료되었습니다.", null));
        }

        Integer userId = sessionUser.getId();
        logger.info("현재 로그인한 사용자의 아이콘 조회 요청: userId={}", userId);

        // 사용자의 icon_id 조회
        Integer iconId = iconService.getUserIconId(userId);

        if (iconId == null) {
            logger.warn("❌ 사용자의 아이콘이 설정되지 않음: userId={}", userId);
            return ResponseEntity.status(404)
                    .body(new ApiResponseDTO<>(404, "사용자의 아이콘이 설정되지 않았습니다.", null));
        }

        // 해당 아이콘 ID로 아이콘 정보 조회
        IconDTO icon = iconService.getIconById(iconId);

        if (icon == null) {
            logger.warn("❌ 아이콘 정보가 존재하지 않음: iconId={}", iconId);
            return ResponseEntity.status(404)
                    .body(new ApiResponseDTO<>(404, "해당 아이콘 정보가 존재하지 않습니다.", null));
        }

        // 파일 URL 설정
        icon.setFileUrl(serverBaseUrl + "/icons/" + icon.getFilename());

        logger.info("✅ 현재 로그인한 사용자의 아이콘 정보 반환: {}", icon);
        return ResponseEntity.ok(new ApiResponseDTO<>(200, "아이콘 정보 조회 성공", icon));
    }

    
    @GetMapping("/test/{userId}")
    public ResponseEntity<ApiResponseDTO<IconDTO>> getUserIconTest(@PathVariable Integer userId) {
        logger.info("📌 특정 사용자 아이콘 조회 요청 (테스트용): userId={}", userId);

        // 유효하지 않은 userId 입력 시 400 Bad Request 반환
        if (userId == null || userId <= 0) {
            logger.warn("❌ 잘못된 사용자 ID 입력: userId={}", userId);
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDTO<>(400, "잘못된 사용자 ID입니다.", null));
        }

        // 사용자의 icon_id 조회
        Integer iconId = iconService.getUserIconId(userId);

        if (iconId == null) {
            logger.warn("❌ 사용자의 아이콘이 설정되지 않음: userId={}", userId);
            return ResponseEntity.status(404)
                    .body(new ApiResponseDTO<>(404, "사용자의 아이콘이 설정되지 않았습니다.", null));
        }

        // 해당 아이콘 ID로 아이콘 정보 조회
        IconDTO icon = iconService.getIconById(iconId);

        if (icon == null) {
            logger.warn("❌ 아이콘 정보가 존재하지 않음: iconId={}", iconId);
            return ResponseEntity.status(404)
                    .body(new ApiResponseDTO<>(404, "해당 아이콘 정보가 존재하지 않습니다.", null));
        }

        // 파일 URL 설정
        icon.setFileUrl(serverBaseUrl + "/icons/" + icon.getFilename());

        logger.info("✅ 특정 사용자의 아이콘 정보 반환 (테스트용): {}", icon);
        return ResponseEntity.ok(new ApiResponseDTO<>(200, "아이콘 정보 조회 성공", icon));
    }


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
    @Operation(
            summary = "모든 아이콘 조회",
            description = "서버에 저장된 모든 아이콘 정보를 반환합니다."
        )
    @ApiResponse(responseCode = "200", description = "아이콘 목록 조회 성공")
    public List<IconDTO> getAllIcons() {
        List<IconDTO> icons = iconService.getAllIcons();

        // 환경 변수 기반으로 파일 URL 생성
        return icons.stream()
                .peek(icon -> icon.setFileUrl(serverBaseUrl + "/icons/" + icon.getFilename()))
                .collect(Collectors.toList());
    }
}

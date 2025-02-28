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
import com.swyp.saratang.service.IconService;
import com.swyp.saratang.config.JwtAuthUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/icons")
public class IconController {

    private static final Logger logger = LogManager.getLogger(IconController.class);

    @Autowired
    private IconService iconService;

    @Autowired
    private JwtAuthUtil jwtAuthUtil; // JWT 유틸리티 주입

    @Value("${icon.directory}")
    private String iconDirectory;

    @Value("${server.base.url}")
    private String serverBaseUrl;

    /**
     * 현재 로그인한 사용자의 아이콘 조회 (JWT 인증)
     */
    @GetMapping("/me")
    @Operation(summary = "현재 로그인한 사용자의 아이콘 조회", description = "JWT 인증을 이용하여 현재 로그인한 사용자의 아이콘 정보를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "아이콘 정보 조회 성공")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @ApiResponse(responseCode = "404", description = "사용자의 아이콘이 설정되지 않음")
    public ResponseEntity<ApiResponseDTO<IconDTO>> getUserIcon(
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {

        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponseDTO<>(401, "JWT 인증 실패", null));
        }

        logger.info("현재 로그인한 사용자의 아이콘 조회 요청: userId={}", userId);

        Integer iconId = iconService.getUserIconId(Integer.parseInt(userId));
        if (iconId == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponseDTO<>(404, "사용자의 아이콘이 설정되지 않았습니다.", null));
        }

        IconDTO icon = iconService.getIconById(iconId);
        if (icon == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponseDTO<>(404, "해당 아이콘 정보가 존재하지 않습니다.", null));
        }

        icon.setFileUrl(serverBaseUrl + "/icons/" + icon.getFilename());

        return ResponseEntity.ok(new ApiResponseDTO<>(200, "아이콘 정보 조회 성공", icon));
    }

    /**
     * 특정 사용자의 아이콘 조회 (테스트용)
     */
    @GetMapping("/test/{userId}")
    @Operation(summary = "특정 사용자 아이콘 조회 (테스트용)", description = "특정 사용자 ID를 이용하여 아이콘 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "아이콘 정보 조회 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 사용자 ID")
    @ApiResponse(responseCode = "404", description = "아이콘 정보 없음")
    public ResponseEntity<ApiResponseDTO<IconDTO>> getUserIconTest(@PathVariable Integer userId) {

        if (userId == null || userId <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDTO<>(400, "잘못된 사용자 ID입니다.", null));
        }

        Integer iconId = iconService.getUserIconId(userId);
        if (iconId == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponseDTO<>(404, "사용자의 아이콘이 설정되지 않았습니다.", null));
        }

        IconDTO icon = iconService.getIconById(iconId);
        if (icon == null) {
            return ResponseEntity.status(404)
                    .body(new ApiResponseDTO<>(404, "해당 아이콘 정보가 존재하지 않습니다.", null));
        }

        icon.setFileUrl(serverBaseUrl + "/icons/" + icon.getFilename());

        return ResponseEntity.ok(new ApiResponseDTO<>(200, "아이콘 정보 조회 성공", icon));
    }

    /**
     *  아이콘 파일 조회 API 
     */
    @GetMapping("/{filename}")
    @Operation(summary = "아이콘 이미지 조회", description = "아이콘 파일을 제공하는 API입니다.")
    @ApiResponse(responseCode = "200", description = "파일 제공 성공")
    @ApiResponse(responseCode = "404", description = "파일 없음")
    public ResponseEntity<Resource> getIcon(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(iconDirectory, filename).normalize();

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * ✅ 모든 아이콘 조회 API
     */
    @GetMapping("/all")
    @Operation(summary = "모든 아이콘 조회", description = "서버에 저장된 모든 아이콘 정보를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "아이콘 목록 조회 성공")
    @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    public ResponseEntity<ApiResponseDTO<List<IconDTO>>> getAllIcons() {
        try {
            List<IconDTO> icons = iconService.getAllIcons();

            // 각 아이콘의 파일 URL 설정
            icons.forEach(icon -> icon.setFileUrl(serverBaseUrl + "/icons/" + icon.getFilename()));

            return ResponseEntity.ok(new ApiResponseDTO<>(200, "아이콘 목록 조회 성공", icons));
        } catch (Exception e) {
            logger.error("❌ 모든 아이콘 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(500, "아이콘 목록 조회 중 오류 발생", null));
        }
    }
}

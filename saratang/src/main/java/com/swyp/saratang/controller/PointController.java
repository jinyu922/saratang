package com.swyp.saratang.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.config.JwtAuthUtil;
import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.BoardDTO;
import com.swyp.saratang.model.LinkAccessDTO;
import com.swyp.saratang.model.PointDTO;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.service.BoardService;
import com.swyp.saratang.service.PointService;
import com.swyp.saratang.service.UserService;
import com.swyp.saratang.session.SessionManager;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
public class PointController {

	@Autowired
	private PointService pointService;
	
    @Autowired
    private UserService userService;
    
    @Autowired
    private BoardService boardService;
    
	@Autowired
	private SessionManager sessionManager;
	
    @Autowired
    private JwtAuthUtil jwtAuthUtil; // JWT 유틸리티 주입
	
	@PostMapping("/point")
	public ApiResponseDTO<?> addPoint(
	        PointDTO pointDTO, 
	        @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
	    
        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }
        
        // 인증된 사용자만 포인트 추가 가능하도록 수정
        pointDTO.setUserId(Integer.parseInt(userId));
		pointService.addPoint(pointDTO);
		return new ApiResponseDTO<>(200, "포인트추가 성공", null);
	}
	
	
	@PostMapping("/viewurl")
	@Operation(summary = "포인트 사용하여 상품 링크 조회",
		       description = "포인트 3점을 사용하여 상품 링크를 조회합니다. 현재 사용자의 크레딧 정보를 업데이트합니다.")
		    @ApiResponse(responseCode = "200", description = "상품 링크 조회 완료 (포인트 3 차감)")
		    @ApiResponse(responseCode = "400", description = "유효하지 않은 boardId 값")
		    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
		    @ApiResponse(responseCode = "402", description = "포인트 부족")
		    @ApiResponse(responseCode = "404", description = "해당 게시글을 찾을 수 없음")
    public ApiResponseDTO<Map<String, Object>> usePointsForProductLink(
            @RequestBody Map<String, Integer> requestData,
            @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        
        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userIdStr = jwtAuthUtil.extractUserId(jwtToken);

        if (userIdStr == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }
        
        Integer userId = Integer.parseInt(userIdStr);
        Integer boardId = requestData.get("boardId");
        LinkAccessDTO accessDTO=new LinkAccessDTO();
        accessDTO.setPostId(boardId);
        accessDTO.setUserId(userId);
        Map<String, Object> responseData = new HashMap<>();

        // boardId가 없는 경우 예외 처리
        if (boardId == null || boardId <= 0) {
            return new ApiResponseDTO<>(400, "유효하지 않은 boardId 값입니다.", null);
        }
        
        // 링크 엑세스 권한이 없다면 포인트 소모해 구매
        if(!pointService.existsLinkAccess(accessDTO)) {
            // 현재 포인트 조회
            Integer currentCredits = userService.getTotalCreditsByUserId(userId);
            int requiredPoints = 3;
            if (currentCredits < requiredPoints) {
                return new ApiResponseDTO<>(402, "포인트가 부족합니다.", null);
            }
            // 포인트 차감 및 기록
            userService.insertCreditHistory(userId, "spend", -3, "URL 조회");
            // 포인트 엑세스 권한 영구 획득
            pointService.addLinkAccess(accessDTO);
            // 변경된 포인트 반영
            Integer updatedCredits = userService.getTotalCreditsByUserId(userId);
            
            // Board 정보 가져오기
            BoardDTO boardDTO = boardService.getBoardById(boardId);
            if (boardDTO == null) {
                return new ApiResponseDTO<>(404, "해당 게시글을 찾을 수 없습니다.", null);
            }
            String productLink = boardDTO.getProductLink();
            // 응답 데이터 반환
            responseData.put("productLink", productLink);
            responseData.put("updatedCredits", updatedCredits);
            return new ApiResponseDTO<>(200, "상품 링크 조회 완료 (포인트 3 차감)", responseData);
        }
        else {
        // 권한 있으면 포인트 소모하지 않고 조회
            // Board 정보 가져오기
            BoardDTO boardDTO = boardService.getBoardById(boardId);
            if (boardDTO == null) {
                return new ApiResponseDTO<>(404, "해당 게시글을 찾을 수 없습니다.", null);
            }

            String productLink = boardDTO.getProductLink();
            // 응답 데이터 반환
            responseData.put("productLink", productLink);

            return new ApiResponseDTO<>(200, "상품 링크 조회 완료", responseData);
        }
    }
}
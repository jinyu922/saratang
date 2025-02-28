package com.swyp.saratang.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.BoardDTO;
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
	
	
	//todo 인증된 권한 사용자만 해당 api 사용가능하게 수정해야함
	@PostMapping("/point")
	public ApiResponseDTO<?> addPoint(PointDTO pointDTO) {
		pointService.addPoint(pointDTO);
		return new ApiResponseDTO<>(200, "포인트추가 성공", null);
	}
	
	
	@PostMapping("/viewurl")
	@Operation(summary = "포인트 사용하여 상품 링크 조회",
		       description = "포인트 3점을 사용하여 상품 링크를 조회합니다. 현재 사용자의 크레딧 정보를 업데이트합니다.")
		    @ApiResponse(responseCode = "200", description = "상품 링크 조회 완료 (포인트 3 차감)")
		    @ApiResponse(responseCode = "400", description = "유효하지 않은 boardId 값")
		    @ApiResponse(responseCode = "401", description = "세션 만료")
		    @ApiResponse(responseCode = "402", description = "포인트 부족")
		    @ApiResponse(responseCode = "404", description = "해당 게시글을 찾을 수 없음")
    public ApiResponseDTO<Map<String, Object>> usePointsForProductLink(@RequestBody Map<String, Integer> requestData, HttpSession session) {
        UserDTO sessionUser = sessionManager.getSession(session.getId());
        
        // 세션 검증
        if (sessionUser == null) {
            return new ApiResponseDTO<>(401, "세션이 만료되었습니다. 다시 로그인해주세요.", null);
        }

        Integer boardId = requestData.get("boardId");

        // boardId가 없는 경우 예외 처리
        if (boardId == null || boardId <= 0) {
            return new ApiResponseDTO<>(400, "유효하지 않은 boardId 값입니다.", null);
        }

        Integer userId = sessionUser.getId();

        // 3현재 포인트 조회
        Integer currentCredits = userService.getTotalCreditsByUserId(userId);
        int requiredPoints = 3;

        if (currentCredits < requiredPoints) {
            return new ApiResponseDTO<>(402, "포인트가 부족합니다.", null);
        }

        // 4️⃣ Board 정보 가져오기
        BoardDTO boardDTO = boardService.getBoardById(boardId);
        if (boardDTO == null) {
            return new ApiResponseDTO<>(404, "해당 게시글을 찾을 수 없습니다.", null);
        }

        String productLink = boardDTO.getProductLink();

   
        userService.insertCreditHistory(userId, "spend", -3, "URL 조회");

        // 변경된 포인트 반영
        Integer updatedCredits = userService.getTotalCreditsByUserId(userId);
        sessionUser.setCredits(updatedCredits);
        sessionManager.setSession(session.getId(), sessionUser);
        
        // 포인트 엑세스 권한 부여
        

        // 응답 데이터 반환
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("updatedCredits", updatedCredits);
        responseData.put("productLink", productLink);

        return new ApiResponseDTO<>(200, "상품 링크 조회 완료 (포인트 3 차감)", responseData);
    }
	
	@PostMapping("/viewurl/test")
    public ApiResponseDTO<Map<String, Object>> usePointsForProductLink(@RequestBody Map<String, Integer> requestData) {
        Integer userId = requestData.get("userId");
        Integer boardId = requestData.get("boardId");

        // 1️⃣ 입력값 검증
        if (userId == null || userId <= 0) {
            return new ApiResponseDTO<>(400, "유효하지 않은 userId 값입니다.", null);
        }
        if (boardId == null || boardId <= 0) {
            return new ApiResponseDTO<>(400, "유효하지 않은 boardId 값입니다.", null);
        }

        // 2️⃣ 현재 사용자 포인트 조회
        Integer currentCredits = userService.getTotalCreditsByUserId(userId);
        int requiredPoints = 3;

        if (currentCredits < requiredPoints) {
            return new ApiResponseDTO<>(402, "포인트가 부족합니다.", null);
        }

        // 3️⃣ Board 정보 가져오기
        BoardDTO boardDTO = boardService.getBoardById(boardId);
        if (boardDTO == null) {
            return new ApiResponseDTO<>(404, "해당 게시글을 찾을 수 없습니다.", null);
        }

        String productLink = boardDTO.getProductLink();

        // 4️⃣ 포인트 차감 및 기록
        userService.insertCreditHistory(userId, "spend", -3, "URL 조회");

        // 5️⃣ 변경된 포인트 반영
        Integer updatedCredits = userService.getTotalCreditsByUserId(userId);

        // 6️⃣ 응답 데이터 반환
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("updatedCredits", updatedCredits);
        responseData.put("productLink", productLink);

        return new ApiResponseDTO<>(200, "상품 링크 조회 완료 (포인트 3 차감)", responseData);
    }
}


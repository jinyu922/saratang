package com.swyp.saratang.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.BoardDTO;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.service.BoardService;
import com.swyp.saratang.session.SessionManager;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class BoardController {
	
	@Autowired
	private BoardService boardService;
	
	@Autowired
	private SessionManager sessionManager;
	
	@Operation(summary = "패션/할인정보 조회", description = "패션/할인정보 리스트 반환, 페이징 지원,postType은 fashion 혹은 discount 중 하나<br>  **요청값으로 어떤 사용자가 조회하는지 requestUserId 를 받습니다, 로그인 상태에서 요청할 경우 세션 사용자의 Id로 자동 맵핑됩니다")
	@GetMapping("/fashion")
	public ApiResponseDTO<?> getFashionList(
			@RequestParam(defaultValue = "5") int size,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "fashion" ) String postType,
	        @RequestParam int requestUserId,
	        HttpSession session){
        if (!"fashion".equals(postType) && !"discount".equals(postType)) {
            return new ApiResponseDTO<>(400, "postType은 fashion 혹은 discount 중 하나입니다.", null);
        }
        int userId=requestUserId;
//		String sessionId = session.getId();  // 현재 세션 ID 가져오기
//	    UserDTO sessionuser = sessionManager.getSession(sessionId); // SessionManager에서 유저 정보 조회
//	    userId=sessionuser.getId();        
        
		Pageable pageable = PageRequest.of(page, size);
		//투두 포스트타입 이상한거 넣으면 오류뱉도록! 상세조회도 동일
		return new ApiResponseDTO<>(200, "성공적으로 패션정보를 조회했습니다", boardService.getFashionList(userId,pageable,postType));
	}
	
	@Operation(summary = "패션/할인정보 상세 조회", description = "id로 상세조회 사라,마라 카운트가 추가되어 나타납니다")
	@GetMapping("/fashion/{id}")
	public ApiResponseDTO<Map<String, Object>> getFashionPostById(@PathVariable int id,@RequestParam(defaultValue = "fashion" ) String postType){
        if (!"fashion".equals(postType) && !"discount".equals(postType)) {
            return new ApiResponseDTO<>(400, "postType은 fashion 혹은 discount 중 하나입니다.", null);
        }
		Map<String, Object> response=new HashMap<>();
		try {
			response =boardService.getFashionPostById(id,postType);
		} catch (NotFoundException e) {
        	return new ApiResponseDTO<>(400, "정보를 찾을 수 없습니다, "+e.getMessage(), null);
        } catch (Exception e) {
        	return new ApiResponseDTO<>(400, "예기치 않은 오류가 발생했습니다, "+e.getMessage(), null);
        }
	    return new ApiResponseDTO<>(200, "성공적으로 패션정보를 상세 조회했습니다", response);
	}
	
	@Operation(summary = "패션/할인정보 저장", description = "패션정보 저장<br>- 로그인 구현 전 까진 userId 도 추가로 입력<br>- 복수의 url은 콤마로 구분하여 입력 \"url1\",\"url2\"<br>- 각 필드 별 세부 정보는 하단 Schemas 의 BoardDTO 참고하세요 ")
	@PostMapping("/fashion")
	public ApiResponseDTO<?> createPost(@RequestBody BoardDTO boardDTO,HttpSession session){
//		String sessionId = session.getId();  // 현재 세션 ID 가져오기
//	    UserDTO sessionuser = sessionManager.getSession(sessionId); // SessionManager에서 유저 정보 조회
//	    boardDTO.setUserId(sessionuser.getId());
	    if (boardDTO.getUserId() == null) {//로그인 했는지 안했는지만 알고싶으면 변수명에 sessionId 넣기
	    	return new ApiResponseDTO<>(400, "글쓴이가 누군지 id 정보가 없습니다 로그인 하거나(세션으로부터 id정보를 받아옴). 혹은 id를 직접 입력하세요", null);
	    }
		if (!("fashion".equals(boardDTO.getPostType()) || "discount".equals(boardDTO.getPostType()))) {
			return new ApiResponseDTO<>(400, "postType은 fashion 혹은 discount 중 하나입니다.", null);
		}
		boardService.createPost(boardDTO, boardDTO.getImageUrls());
		return new ApiResponseDTO<>(200, "성공적으로 정보를 저장하였습니다.", null);
	}

}

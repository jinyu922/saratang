package com.swyp.saratang.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import com.swyp.saratang.data.PeriodType;
import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.BoardDTO;
import com.swyp.saratang.model.CommentDTO;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.service.BoardService;
import com.swyp.saratang.session.SessionManager;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
public class BoardController {
	
	@Autowired
	private BoardService boardService;
	
	@Autowired
	private SessionManager sessionManager;
	
	
	@Operation(summary = "패션/할인정보 조회", description = "패션/할인정보 리스트 페이징 정보 포함하여 반환,<br>**요청값으로 어떤 사용자가 조회하는지 requestUserId 를 받습니다, 로그인 상태에서 요청할 경우 세션 사용자의 Id로 자동 맵핑됩니다")
	@GetMapping("/fashion")
	public ApiResponseDTO<?> getFashionList(
			@Parameter(name="postType" ,schema=@Schema(allowableValues = { "fashion", "discount" },defaultValue = "fashion"))
	        @RequestParam(defaultValue = "fashion" ) String postType,
	        @RequestParam int requestUserId,
			@RequestParam(defaultValue = "5") int size ,
	        @RequestParam(defaultValue = "0") int page,
	        HttpSession session){
        if (!"fashion".equals(postType) && !"discount".equals(postType)) {
            return new ApiResponseDTO<>(400, "postType은 fashion 혹은 discount 중 하나입니다.", null);
        }
        int userId=requestUserId;
//		String sessionId = session.getId();  // 현재 세션 ID 가져오기
//	    UserDTO sessionuser = sessionManager.getSession(sessionId); // SessionManager에서 유저 정보 조회
//	    userId=sessionuser.getId();      
		Pageable pageable = PageRequest.of(page, size);
		return new ApiResponseDTO<>(200, "성공적으로 패션정보를 조회했습니다", boardService.getFashionList(userId,pageable,postType));
	}
	
	@Operation(summary = "히스토리 조회", description = "히스토리 페이징 조회, **요청값으로 어떤 사용자가 조회하는지 requestUserId 를 받습니다, 로그인 상태에서 요청할 경우 세션 사용자의 Id로 자동 맵핑됩니다")
	@GetMapping("/fashion/history")
	public ApiResponseDTO<?> getHistory(
			@Parameter(name="postType" ,schema=@Schema(allowableValues = { "fashion", "discount" },defaultValue = "fashion"))
	        @RequestParam(defaultValue = "fashion" ) String postType,
	    	@Parameter(name="judgementType" ,schema=@Schema(allowableValues = { "positive", "negative" },defaultValue = "positive"))
			@RequestParam String judgementType,
	    	@Parameter(name="sortType" ,schema=@Schema(allowableValues = { "ASC", "DESC" },defaultValue = "DESC"))
			@RequestParam String sortType,
	        @RequestParam int requestUserId,
			@RequestParam(defaultValue = "5") int size,
	        @RequestParam(defaultValue = "0") int page,
	        HttpSession session){
        if (!"fashion".equals(postType) && !"discount".equals(postType)) {
            return new ApiResponseDTO<>(400, "postType은 fashion 혹은 discount 중 하나입니다.", null);
        }
        if (!"positive".equals(judgementType) && !"negative".equals(judgementType)) {
            return new ApiResponseDTO<>(400, "judgementType은 positive 혹은 negative 중 하나입니다.", null);
        }
        if (!"desc".equals(sortType) && !"asc".equals(sortType)&& !"ASC".equals(sortType)&& !"DESC".equals(sortType)) {
            return new ApiResponseDTO<>(400, "sort은 desc,asc,ASC,DESC 중 하나입니다.", null);
        }
        
        int userId=requestUserId;
//		String sessionId = session.getId();  // 현재 세션 ID 가져오기
//	    UserDTO sessionuser = sessionManager.getSession(sessionId); // SessionManager에서 유저 정보 조회
//	    userId=sessionuser.getId();      
		Pageable pageable = PageRequest.of(page, size);
		return new ApiResponseDTO<>(200, "성공적으로 히스토리를 조회했습니다", boardService.getHistory(userId,pageable,postType,judgementType,sortType));
	}
	
	@Operation(summary = "랜덤 알고리즘 적용된 패션/할인정보 조회", description = "패션/할인정보 리스트 반환 페이징은 지원하지 않습니다,"
			+ "<br>  요청값으로 어떤 사용자가 조회하는지 requestUserId 를 받습니다, 로그인 상태에서 요청할 경우 세션 사용자의 Id로 자동 맵핑됩니다"
			+ "<br><br>  *limitSize,finalLimitSize 설명"
			+ "<br>  데이터에서 1.최신순/2.인기순/3.랜덤 다른 방식으로 총 3번 limitSize 만큼 뽑아 하나로 합친 뒤 다시 랜덤으로 섞은것을 finalLimitSize 만큼 출력하는 방식입니다"
			+ "<br>  **쉽게 말해 limitSize가 커질수록 무작위성이 증가하며 finalLimitSize 는 최종 출력 게시글 개수를 결정합니다. "
			+ "<br>  ***필터링 된 게시글 개수가 요청 게시글 개수보다 작을경우 요청 개수 이하의 데이터가 반환될 수 있습니다, 따라서 알고리즘 특성상 limitSize>=finalLimitSize 을 권장합니다 ")
	@GetMapping("/fashion/random")
	public ApiResponseDTO<?> getFashionListAlgorithm(
			@Parameter(name="postType" ,schema=@Schema(allowableValues = { "fashion", "discount" },defaultValue = "fashion"))
			@RequestParam(defaultValue = "fashion" ) String postType,
	        @RequestParam int requestUserId,
	        @RequestParam(defaultValue = "5" ) int limitSize,
	        @RequestParam(defaultValue = "5" ) int finalLimitSize,
	        HttpSession session){
        if (!"fashion".equals(postType) && !"discount".equals(postType)) {
            return new ApiResponseDTO<>(400, "postType은 fashion 혹은 discount 중 하나입니다.", null);
        }
        int userId=requestUserId;
//		String sessionId = session.getId();  // 현재 세션 ID 가져오기
//	    UserDTO sessionuser = sessionManager.getSession(sessionId); // SessionManager에서 유저 정보 조회
//	    userId=sessionuser.getId();
        List<BoardDTO> result=new ArrayList<>();
        try {
        	result=boardService.getFashionListAlgorithm(userId,postType,limitSize,finalLimitSize);
		} catch (Exception e) {
			return new ApiResponseDTO<>(400, "쿼리 조회중 오류 발생: "+e.getMessage(), null);
		}
		return new ApiResponseDTO<>(200, "성공적으로 랜덤 알고리즘 적용된 패션정보를 조회했습니다", result);
	}
	
	@Operation(summary = "패션/할인정보 상세 조회", description = "id로 상세조회 사라,마라 카운트가 추가되어 나타납니다")
	@GetMapping("/fashion/{id}")
	public ApiResponseDTO<Map<String, Object>> getFashionPostById(	@Parameter(name="postType" ,schema=@Schema(allowableValues = { "fashion", "discount" },defaultValue = "fashion"))
	@RequestParam(defaultValue = "fashion" ) String postType,@PathVariable int id){
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
	
	@Operation(summary = "기간별 베스트", description = "패션/할인정보 기간별 베스트, 선택한 기간부터 지금까지 가장 인기있는 게시글 조회")
	@GetMapping("/fashion/best")
	public ApiResponseDTO<?> getBest(
		@Parameter(name="postType" ,schema=@Schema(allowableValues = { "fashion", "discount" },defaultValue = "fashion"))
		@RequestParam(defaultValue = "fashion" ) String postType,
		@Parameter(name="periodType" ,schema=@Schema(allowableValues = { "yesterday", "week", "month", "year" },defaultValue = "yesterday"))
        @RequestParam String periodType,
		@RequestParam int requestUserId,
		@RequestParam(defaultValue = "5") int size,
        @RequestParam(defaultValue = "0") int page,
        HttpSession session){
        if (!"fashion".equals(postType) && !"discount".equals(postType)) {
            return new ApiResponseDTO<>(400, "postType 값은 'fashion', 'discount' 중 하나여야 함.", null);
        }
        if (!PeriodType.isValid(periodType)) {
            return new ApiResponseDTO<>(400, "period 값은 'yesterday', 'week', 'month', 'year' 중 하나여야 함.", null);
        }
        int period=PeriodType.fromString(periodType).getDays();
        int userId=requestUserId;
//		String sessionId = session.getId();  // 현재 세션 ID 가져오기
//	    UserDTO sessionuser = sessionManager.getSession(sessionId); // SessionManager에서 유저 정보 조회
//	    userId=sessionuser.getId();
		Pageable pageable = PageRequest.of(page, size);
		return new ApiResponseDTO<>(200, "성공적으로 베스트정보를 상세 조회했습니다", boardService.getBest(userId, pageable, postType, period));
	}
	
	@PostMapping("/comment")
	public ApiResponseDTO<?> insertComment(@RequestBody CommentDTO commentDTO,HttpSession session){
		boardService.insertComment(commentDTO);
		return new ApiResponseDTO<>(200, "성공적으로 댓글 정보를 저장하였습니다.", null);
	}
	
	@GetMapping("/comment/{postId}")
	public ApiResponseDTO<?> getCommentList(
			@PathVariable int postId,
			@RequestParam(defaultValue = "5") int size,
	        @RequestParam(defaultValue = "0") int page){
		Pageable pageable = PageRequest.of(page, size);
		return new ApiResponseDTO<>(200, "성공적으로 댓글 정보를 조회하였습니다.", boardService.getCommentList(postId,pageable));
	}

}

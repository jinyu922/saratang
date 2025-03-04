package com.swyp.saratang.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.config.JwtAuthUtil;
import com.swyp.saratang.data.PeriodType;
import com.swyp.saratang.exception.UnauthorizedAccessException;
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
	
    @Autowired
    private JwtAuthUtil jwtAuthUtil; // JWT 유틸리티 주입
	
	
	@Operation(summary = "패션/할인정보 조회", description = "패션/할인정보 리스트 페이징 조회")
	@GetMapping("/fashion")
	public ApiResponseDTO<?> getFashionList(
			@Parameter(name="postType" ,schema=@Schema(allowableValues = { "fashion", "discount" },defaultValue = "fashion"))
	        @RequestParam(defaultValue = "fashion" ) String postType,
	        @Parameter(description = "요청유저 고유id, 로그인 세션 있으면 입력하지 않아도 됩니다")@RequestParam(required = false) Integer requestUserId,
			@RequestParam(defaultValue = "5") int size ,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request){
        if (!"fashion".equals(postType) && !"discount".equals(postType)) {
            return new ApiResponseDTO<>(400, "postType은 fashion 혹은 discount 중 하나입니다.", null);
        }
        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);
        System.out.println("userId = jwtAuthUtil.extractUserId(jwtToken) == "+userId);
        System.out.println(Integer.parseInt(userId));
        

        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }     
        
		Pageable pageable = PageRequest.of(page, size);
		return new ApiResponseDTO<>(200, "성공적으로 패션정보를 조회했습니다", boardService.getFashionList(Integer.parseInt(userId),pageable,postType));
	}
	
	@Operation(summary = "히스토리 조회", description = "히스토리 리스트 페이징 조회")
	@GetMapping("/fashion/history")
	public ApiResponseDTO<?> getHistory(
			@Parameter(name="postType" ,schema=@Schema(allowableValues = { "fashion", "discount" },defaultValue = "fashion"))
	        @RequestParam(defaultValue = "fashion" ) String postType,
	    	@Parameter(name="judgementType" ,schema=@Schema(allowableValues = { "positive", "negative" },defaultValue = "positive"))
			@RequestParam String judgementType,
	    	@Parameter(name="sortType" ,schema=@Schema(allowableValues = { "ASC", "DESC" },defaultValue = "DESC"))
			@RequestParam String sortType,
			@Parameter(description = "요청유저 고유id, 로그인 세션 있으면 입력하지 않아도 됩니다")@RequestParam(required = false) Integer requestUserId,
			@RequestParam(defaultValue = "5") int size,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request){
        if (!"fashion".equals(postType) && !"discount".equals(postType)) {
            return new ApiResponseDTO<>(400, "postType은 fashion 혹은 discount 중 하나입니다.", null);
        }
        if (!"positive".equals(judgementType) && !"negative".equals(judgementType)) {
            return new ApiResponseDTO<>(400, "judgementType은 positive 혹은 negative 중 하나입니다.", null);
        }
        if (!"desc".equals(sortType) && !"asc".equals(sortType)&& !"ASC".equals(sortType)&& !"DESC".equals(sortType)) {
            return new ApiResponseDTO<>(400, "sort은 desc,asc,ASC,DESC 중 하나입니다.", null);
        }
        
        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }
        
		Pageable pageable = PageRequest.of(page, size);
		return new ApiResponseDTO<>(200, "성공적으로 히스토리를 조회했습니다", boardService.getHistory(Integer.parseInt(userId),pageable,postType,judgementType,sortType));
	}
	
	@Operation(summary = "링크 히스토리 조회", description = "링크 히스토리 리스트 페이징 조회")
	@GetMapping("/fashion/linkHistory")
	public ApiResponseDTO<?> getLinkHistory(
			@Parameter(name="postType" ,schema=@Schema(allowableValues = { "fashion", "discount" },defaultValue = "fashion"))
	        @RequestParam(defaultValue = "fashion" ) String postType,
	    	@Parameter(name="sortType" ,schema=@Schema(allowableValues = { "ASC", "DESC" },defaultValue = "DESC"))
			@RequestParam String sortType,
			@Parameter(description = "요청유저 고유id, 로그인 세션 있으면 입력하지 않아도 됩니다")@RequestParam(required = false) Integer requestUserId,
			@RequestParam(defaultValue = "5") int size,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request){
        if (!"fashion".equals(postType) && !"discount".equals(postType)) {
            return new ApiResponseDTO<>(400, "postType은 fashion 혹은 discount 중 하나입니다.", null);
        }
        if (!"desc".equals(sortType) && !"asc".equals(sortType)&& !"ASC".equals(sortType)&& !"DESC".equals(sortType)) {
            return new ApiResponseDTO<>(400, "sort은 desc,asc,ASC,DESC 중 하나입니다.", null);
        }
        
        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }
        
		Pageable pageable = PageRequest.of(page, size);
		return new ApiResponseDTO<>(200, "성공적으로 링크 히스토리를 조회했습니다", boardService.getLinkHistory(Integer.parseInt(userId),pageable,postType,sortType));
	}
	
	@Operation(summary = "랜덤 알고리즘 적용된 패션/할인정보 조회", description = "패션/할인정보 리스트 페이징은 지원하지 않습니다,"
			+ "<br><br>  *limitSize,finalLimitSize 설명"
			+ "<br>  데이터에서 1.최신순/2.인기순/3.랜덤 다른 방식으로 총 3번 limitSize 만큼 뽑아 하나로 합친 뒤 다시 랜덤으로 섞은것을 finalLimitSize 만큼 출력하는 방식입니다"
			+ "<br>  **쉽게 말해 limitSize가 커질수록 무작위성이 증가하며 finalLimitSize 는 최종 출력 게시글 개수를 결정합니다. "
			+ "<br>  ***필터링 된 게시글 개수가 요청 게시글 개수보다 작을경우 요청 개수 이하의 데이터가 반환될 수 있습니다, 따라서 알고리즘 특성상 limitSize>=finalLimitSize 을 권장합니다 ")
	@GetMapping("/fashion/random")
	public ApiResponseDTO<?> getFashionListAlgorithm(
			@Parameter(name="postType" ,schema=@Schema(allowableValues = { "fashion", "discount" },defaultValue = "fashion"))
			@RequestParam(defaultValue = "fashion" ) String postType,
			@Parameter(description = "요청유저 고유id, 로그인 세션 있으면 입력하지 않아도 됩니다")@RequestParam(required = false) Integer requestUserId,
	        @RequestParam(defaultValue = "5" ) int limitSize,
	        @RequestParam(defaultValue = "5" ) int finalLimitSize,
	        @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request){
        if (!"fashion".equals(postType) && !"discount".equals(postType)) {
            return new ApiResponseDTO<>(400, "postType은 fashion 혹은 discount 중 하나입니다.", null);
        }
        
        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }
        
        List<BoardDTO> result=new ArrayList<>();
        try {
        	result=boardService.getFashionListAlgorithm(Integer.parseInt(userId),postType,limitSize,finalLimitSize);
		} catch (Exception e) {
			return new ApiResponseDTO<>(400, "쿼리 조회중 오류 발생: "+e.getMessage(), null);
		}
		return new ApiResponseDTO<>(200, "성공적으로 랜덤 알고리즘 적용된 패션정보를 조회했습니다", result);
	}
	
	@Operation(summary = "패션/할인정보 상세 조회", description = "id로 상세조회 사라,마라 카운트가 추가되어 나타납니다")
	@GetMapping("/fashion/{id}")
	public ApiResponseDTO<Map<String, Object>> getFashionPostById(	
	        @Parameter(name="postType" ,schema=@Schema(allowableValues = { "fashion", "discount" },defaultValue = "fashion"))
	        @RequestParam(defaultValue = "fashion" ) String postType,
	        @PathVariable int id,
	        @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request){
        if (!"fashion".equals(postType) && !"discount".equals(postType)) {
            return new ApiResponseDTO<>(400, "postType은 fashion 혹은 discount 중 하나입니다.", null);
        }
        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }
		Map<String, Object> response=new HashMap<>();
		try {
			response =boardService.getFashionPostById(id,Integer.parseInt(userId),postType);
		} catch (NotFoundException e) {
        	return new ApiResponseDTO<>(400, "정보를 찾을 수 없습니다, "+e.getMessage(), null);
        } catch (Exception e) {
        	return new ApiResponseDTO<>(400, "예기치 않은 오류가 발생했습니다, "+e.getMessage(), null);
        }
	    return new ApiResponseDTO<>(200, "성공적으로 패션정보를 상세 조회했습니다", response);
	}
	
	@Operation(summary = "패션/할인정보 저장", description = "패션정보 저장<br>- 복수의 url은 콤마로 구분하여 입력 \"url1\",\"url2\"<br>- 각 필드 별 세부 정보는 하단 Schemas 의 BoardDTO 참고하세요 ")
	@PostMapping("/fashion")
	public ApiResponseDTO<Integer> createPost(
	        @RequestBody BoardDTO boardDTO,
			@Parameter(description = "요청유저 고유id, 로그인 세션 있으면 입력하지 않아도 됩니다")@RequestParam(required = false) Integer requestUserId,
			@RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request){
        
        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }
        
		if (!("fashion".equals(boardDTO.getPostType()) || "discount".equals(boardDTO.getPostType()))) {
			return new ApiResponseDTO<>(400, "postType은 fashion 혹은 discount 중 하나입니다.", null);
		}
		boardDTO.setUserId(Integer.parseInt(userId));
		Integer createdPostId=boardService.createPost(boardDTO, boardDTO.getImageUrls());
		return new ApiResponseDTO<>(200, "성공적으로 정보를 저장하였습니다.", createdPostId);
	}
	
	@Operation(summary = "기간별 베스트", description = "패션/할인정보 기간별 베스트, 선택한 기간부터 지금까지 가장 인기있는 게시글 조회")
	@GetMapping("/fashion/best")
	public ApiResponseDTO<?> getBest(
		@Parameter(name="postType" ,schema=@Schema(allowableValues = { "fashion", "discount" },defaultValue = "fashion"))
		@RequestParam(defaultValue = "fashion" ) String postType,
		@Parameter(name="periodType" ,schema=@Schema(allowableValues = { "yesterday", "week", "month", "year" },defaultValue = "yesterday"))
        @RequestParam String periodType,
        @Parameter(description = "요청유저 고유id, 로그인 세션 있으면 입력하지 않아도 됩니다")@RequestParam(required = false) Integer requestUserId,
		@RequestParam(defaultValue = "5") int size,
        @RequestParam(defaultValue = "0") int page,
        @RequestHeader(value = "Authorization", required = false) String token,
        HttpServletRequest request){
        if (!"fashion".equals(postType) && !"discount".equals(postType)) {
            return new ApiResponseDTO<>(400, "postType 값은 'fashion', 'discount' 중 하나여야 함.", null);
        }
        if (!PeriodType.isValid(periodType)) {
            return new ApiResponseDTO<>(400, "period 값은 'yesterday', 'week', 'month', 'year' 중 하나여야 함.", null);
        }
        int period=PeriodType.fromString(periodType).getDays();
        
        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }
        
		Pageable pageable = PageRequest.of(page, size);
		return new ApiResponseDTO<>(200, "성공적으로 베스트정보를 상세 조회했습니다", boardService.getBest(Integer.parseInt(userId), pageable, postType, period));
	}
	
	@Operation(summary = "댓글저장", description = "어떤 유저가 어떤 post에 저장하는지 입력받습니다. id는 입력안해도됩니다")
	@PostMapping("/comment")
	public ApiResponseDTO<?> insertComment(
	        @RequestBody CommentDTO commentDTO,
	        @Parameter(description = "요청유저 고유id, 로그인 세션 있으면 입력하지 않아도 됩니다")@RequestParam(required = false) Integer requestUserId,
	        @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request){
        
        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }
        
        commentDTO.setUserId(Integer.parseInt(userId));
		boardService.insertComment(commentDTO);
		return new ApiResponseDTO<>(200, "성공적으로 댓글 정보를 저장하였습니다.", null);
	}
	
	@Operation(summary = "댓글조회", description = "게시글의 댓글 목록 최신순 조회, 페이징 지원합니다")
	@GetMapping("/comment/{postId}")
	public ApiResponseDTO<?> getCommentList(
			@PathVariable int postId,
			@RequestParam(defaultValue = "5") int size,
	        @RequestParam(defaultValue = "0") int page){
		Pageable pageable = PageRequest.of(page, size);
		return new ApiResponseDTO<>(200, "성공적으로 댓글 정보를 조회하였습니다.", boardService.getCommentList(postId,pageable));
	}
	
	@Operation(summary = "작성한 게시글들 조회", description = "작성한 게시글들 리스트 페이징 조회")
	@GetMapping("/my_post")
	public ApiResponseDTO<?> getMyPosts(
	        @Parameter(description = "요청유저 고유id, 로그인 세션 있으면 입력하지 않아도 됩니다")@RequestParam(required = false) Integer requestUserId,
			@RequestParam(defaultValue = "5") int size,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request){
        
        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);

        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }
        
		Pageable pageable = PageRequest.of(page, size);
		return new ApiResponseDTO<>(200, "성공적으로 작성한 게시글들을 조회했습니다", boardService.getMyPosts(Integer.parseInt(userId), pageable));
	}
	
	@Operation(summary = "게시글 수정", description = "1.글 작성자만 수정가능함(로그인 토큰 여부로 판별)<br>2.속성이 널값이면 해당 속성은 기존 값으로 유지(이미지url도 동일)<br>3.ImageUrl을 받으면 받은 이미지로 교체<br>4.body의 userId 속성값엔 영향이 없습니다(입력해도 작성자는 변경되지 않음).")
	@PutMapping("/fashion/{postId}")
	public ApiResponseDTO<?> updatePost(
			@PathVariable Integer postId,
			@Parameter(description = "수정할 게시글의 고유 id")@RequestBody BoardDTO boardDTO,
			@Parameter(description = "jwt 토큰 없는 테스트용 파라미터")@RequestParam(required = false) Integer requestUserId,
			@RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request){
		
        String jwtToken = jwtAuthUtil.extractToken(request, token, null);
        String userId = jwtAuthUtil.extractUserId(jwtToken);
        Integer updatedPostId=0;
        if (userId == null) {
            return new ApiResponseDTO<>(401, "JWT 인증 실패", null);
        }
        try {
        	updatedPostId=boardService.updatePost(boardDTO, boardDTO.getImageUrls(),Integer.parseInt(userId),postId);
        } catch (UnauthorizedAccessException e) {
            return new ApiResponseDTO<>(403, "게시글 수정 권한이 없습니다.", null);
        } catch (NoSuchElementException e) {
            return new ApiResponseDTO<>(404, "수정할 게시글을 찾을 수 없습니다.", null);
        } catch (Exception e) {
            return new ApiResponseDTO<>(500, "게시글 수정 중 오류가 발생했습니다."+e.toString(), null);
        }
        
		return new ApiResponseDTO<>(200, "성공적으로 정보를 저장하였습니다.", updatedPostId);
	}
}
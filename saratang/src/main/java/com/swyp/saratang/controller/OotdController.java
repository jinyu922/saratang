package com.swyp.saratang.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.exception.NotFoundException;
import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.model.OotdDTO;
import com.swyp.saratang.service.AuthService;
import com.swyp.saratang.service.OotdService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/Ootd")
public class OotdController {
	
	@Autowired
	private OotdService ootdService;
	
	@Autowired
	private AuthService authService;
	
    // OOTD 저장
    @ApiResponse(responseCode = "200", description = "저장성공")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
	@Operation(summary = "OOTD 저장", description = "userId는 요청자 로그인 정보로 자동 맵핑됩니다 (입력필요x)")
    @PostMapping("")
    public ApiResponseDTO<?> createOotd(@RequestBody OotdDTO ootdDTO,@RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        Integer userId = authService.validateJwtAndGetUserId(request, token);
        if(userId==null) {
        	return new ApiResponseDTO<>(401, "JWT 인증 실패 : UserId is null", null);
        }
        ootdDTO.setUserId(userId);
        return new ApiResponseDTO<>(200, "성공적으로 정보를 저장하였습니다.", ootdService.createOotd(ootdDTO));
    }

    // OOTD 삭제
    @ApiResponse(responseCode = "200", description = "삭제성공")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @ApiResponse(responseCode = "403", description = "삭제 권한 없음")
	@Operation(summary = "OOTD 삭제", description = "삭제할 ootd의 id를 입력받음, 작성자 본인만 삭제 가능")
    @DeleteMapping("/{id}")
    public ApiResponseDTO<?> deleteOotd(@PathVariable Integer id,@RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        Integer userId = authService.validateJwtAndGetUserId(request, token);
        if(userId==null) {
        	return new ApiResponseDTO<>(401, "JWT 인증 실패 : UserId is null", null);
        }
        try {
        	ootdService.deleteOotd(userId,id);
		} catch (NotFoundException e) {
			return new ApiResponseDTO<>(400, "삭제할 데이터가 없습니다", null);
		} 
        catch (Exception e) {
			return new ApiResponseDTO<>(403, "게시글 삭제 권한이 없습니다 : 작성자 본인만 삭제가능.", null);
		}
        
        return new ApiResponseDTO<>(200, "성공적으로 정보를 삭제하였습니다.", null);
    }

    // OOTD 좋아요
    @ApiResponse(responseCode = "200", description = "좋아요성공")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @ApiResponse(responseCode = "404", description = "이미 좋아요 누른 ootd")
	@Operation(summary = "OOTD 좋아요", description = "특정 ootd에 좋아요 판단을 남깁니다")
    @PostMapping("/{id}/likes")
    public ApiResponseDTO<?> likeOotd(@PathVariable Integer id,@RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        Integer userId = authService.validateJwtAndGetUserId(request, token);
        if(userId==null) {
        	return new ApiResponseDTO<>(401, "JWT 인증 실패 : UserId is null", null);
        }
        try {
			ootdService.likeOotd(userId, id);
		} catch (Exception e) {
			return new ApiResponseDTO<>(404, "좋아요 판단 실패 :"+e.getMessage(),null);
		}
        return new ApiResponseDTO<>(200, "성공적으로 좋아요 판단을 내렸습니다.", null);
    }

    // OOTD 좋아요 취소
    @ApiResponse(responseCode = "200", description = "좋아요취소성공")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @ApiResponse(responseCode = "404", description = "좋아요 누른적 없는 ootd")
	@Operation(summary = "OOTD 좋아요 철회", description = "특정 ootd에 좋아요 판단을 철회합니다")
    @DeleteMapping("/{id}/likes")
    public ApiResponseDTO<?> unlikeOotd(@PathVariable Integer id,@RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        Integer userId = authService.validateJwtAndGetUserId(request, token);
        if(userId==null) {
        	return new ApiResponseDTO<>(401, "JWT 인증 실패 : UserId is null", null);
        }
        try {
			ootdService.unlikeOotd(userId, id);
		} catch (Exception e) {
			return new ApiResponseDTO<>(404, "좋아요 취소 실패 :"+e.getMessage(),null);
		}
        return new ApiResponseDTO<>(200, "성공적으로 좋아요 취소 했습니다.", null);
    }

    // OOTD 스크랩
    @ApiResponse(responseCode = "200", description = "스크랩성공")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @ApiResponse(responseCode = "404", description = "이미 스크랩한 ootd")
	@Operation(summary = "OOTD 스크랩", description = "특정 ootd를 스크랩합니다")
    @PostMapping("/{id}/scraps")
    public ApiResponseDTO<?> scrapOotd(@PathVariable Integer id,@RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        Integer userId = authService.validateJwtAndGetUserId(request, token);
        if(userId==null) {
        	return new ApiResponseDTO<>(401, "JWT 인증 실패 : UserId is null", null);
        }
        try {
			ootdService.scrapOotd(userId, id);
		} catch (Exception e) {
			return new ApiResponseDTO<>(404, "스크랩 실패 :"+e.getMessage(),null);
		}
        return new ApiResponseDTO<>(200, "성공적으로 스크랩 했습니다.", null);
    }

    // OOTD 스크랩 취소
    @ApiResponse(responseCode = "200", description = "스크랩취소성공")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @ApiResponse(responseCode = "404", description = "스크랩 한 적 없는 ootd")
	@Operation(summary = "OOTD 스크랩 철회", description = "특정 ootd에 스크랩을 철회합니다")
    @DeleteMapping("/{id}/scraps")
    public ApiResponseDTO<?> unscrapOotd(@PathVariable Integer id,@RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        Integer userId = authService.validateJwtAndGetUserId(request, token);
        if(userId==null) {
        	return new ApiResponseDTO<>(401, "JWT 인증 실패 : UserId is null", null);
        }
        try {
			ootdService.unscrapOotd(userId, id);
		} catch (Exception e) {
			return new ApiResponseDTO<>(404, "스크랩 취소 실패 :"+e.getMessage(),null);
		}
        return new ApiResponseDTO<>(200, "성공적으로 스크랩 취소 했습니다.", null);
    }
    
    // OOTD 상세조회
    @ApiResponse(responseCode = "200", description = "조회성공")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @ApiResponse(responseCode = "404", description = "해당 ootd 가 존재하지 않음")
    @Operation(summary = "OOTD 상세조회", description = "조회할 OOTD 고유 id 입력받음")
    @GetMapping("{id}")
    public ApiResponseDTO<?> getOotd(@PathVariable Integer id,@RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request){
        Integer userId = authService.validateJwtAndGetUserId(request, token);
        if(userId==null) {
        	return new ApiResponseDTO<>(401, "JWT 인증 실패 : UserId is null", null);
        }
        Map<String, Object> response=new HashMap<>();
        try {
			response=ootdService.getOotd(userId, id);
		} catch (Exception e) {
			return new ApiResponseDTO<>(404, "상세조회 실패", null);
		}
        return new ApiResponseDTO<>(200, "성공적으로 조회했습니다", response);
    }

    // OOTD 조회 (인기순, 최신순)
    @ApiResponse(responseCode = "200", description = "조회성공")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
    @ApiResponse(responseCode = "400", description = "잘못된 sort 형식")
	@Operation(summary = "전체 OOTD 조회", description = "sort 는 recent/like (최신순/인기순) 입니다. 페이징을 지원합니다")
    @GetMapping("")
    public ApiResponseDTO<?> getOotds(@RequestParam String sort, @RequestParam int page, @RequestParam int size,@RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        Integer userId = authService.validateJwtAndGetUserId(request, token);
        if(userId==null) {
        	return new ApiResponseDTO<>(401, "JWT 인증 실패 : UserId is null", null);
        }
        // sort 형식 검증
        if(!sort.equals("recent")&&!sort.equals("like")) {
        	return new ApiResponseDTO<>(400, "잘못된 sort 형식", null);
        }
        // 페이징 객체 보내기
    	Pageable pageable = PageRequest.of(page, size);
    	// 인기순인지 최신순인지 판단
        Page<Map<String, Object>> response=ootdService.getOotds(userId, sort, pageable);
    	return new ApiResponseDTO<>(200, "성공적으로 조회했습니다", response);
    }

    // OOTD 좋아요한 글 조회
    @ApiResponse(responseCode = "200", description = "조회성공")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
	@Operation(summary = "전체 OOTD 조회", description = "로그인 사용자가 좋아요한 OOTD 조회. 페이징을 지원합니다")
    @GetMapping("/liked")
    public ApiResponseDTO<?> getLikedOotds(@RequestParam int page, @RequestParam int size,@RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        Integer userId = authService.validateJwtAndGetUserId(request, token);
        if(userId==null) {
        	return new ApiResponseDTO<>(401, "JWT 인증 실패 : UserId is null", null);
        }

    	Pageable pageable = PageRequest.of(page, size);
        return new ApiResponseDTO<>(200, "성공적으로 조회했습니다", ootdService.getLikedOotds(userId, pageable));
    }

    // OOTD 스크랩한 글 조회
    @ApiResponse(responseCode = "200", description = "조회성공")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
	@Operation(summary = "전체 OOTD 조회", description = "로그인 사용자가 스크랩한 OOTD 조회. 페이징을 지원합니다")
    @GetMapping("/scrapped")
    public ApiResponseDTO<?> getScrappedOotds(@RequestParam int page, @RequestParam int size, 
    		@RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        Integer userId = authService.validateJwtAndGetUserId(request, token);
        if(userId==null) {
        	return new ApiResponseDTO<>(401, "JWT 인증 실패 : UserId is null", null);
        }
        
        Pageable pageable = PageRequest.of(page, size);
        return new ApiResponseDTO<>(200, "성공적으로 조회했습니다", ootdService.getScrappedOotds(userId, pageable));
    }

    // OOTD 내가 쓴 글 조회
    @ApiResponse(responseCode = "200", description = "조회성공")
    @ApiResponse(responseCode = "401", description = "JWT 인증 실패")
	@Operation(summary = "전체 OOTD 조회", description = "로그인 사용자가 작성한 OOTD 조회. 페이징을 지원합니다")
    @GetMapping("/my")
    public ApiResponseDTO<?> getMyOotds(@RequestParam int page, @RequestParam int size,
    		@RequestHeader(value = "Authorization", required = false) String token,
            HttpServletRequest request) {
        Integer userId = authService.validateJwtAndGetUserId(request, token);
        if(userId==null) {
        	return new ApiResponseDTO<>(401, "JWT 인증 실패 : UserId is null", null);
        }
        Pageable pageable = PageRequest.of(page, size);
        return new ApiResponseDTO<>(200, "성공적으로 조회했습니다", ootdService.getMyOotds(userId, pageable));
    }
}

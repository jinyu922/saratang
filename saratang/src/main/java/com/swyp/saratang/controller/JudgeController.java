package com.swyp.saratang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.model.ApiResponseDTO;
import com.swyp.saratang.service.JudgeService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class JudgeController {
	@Autowired
	private JudgeService judgeService;
	
	@Operation(summary = "게시글에 대한 판단을 내립니다", description = "judgementType은 positive 또는 negative 입니다")
    @PostMapping("/judge")
    public ApiResponseDTO<?> judge(
        @RequestParam int userId,
        @RequestParam int postId,
        @RequestParam String judgementType
    ) {
        
        // 기본적인 파라미터 검증
        if (!"positive".equals(judgementType) && !"negative".equals(judgementType)) {
            return new ApiResponseDTO<>(400, "judgementType은 positive 혹은 negative 중 하나입니다.", null);
        }

        try {
            judgeService.addJudgement(userId, postId, judgementType);
        } catch (IllegalStateException e) {
            return new ApiResponseDTO<>(400, "유저는 이미 이 게시글에 대해 판단을 내렸습니다."+e.getMessage(), null);
        } catch (Exception e) {
        	return new ApiResponseDTO<>(500, "DB 데이터 요청 중 예기치 못한 오류가 발생했습니다."+e.getMessage(), null);
        }
        return new ApiResponseDTO<>(200, "성공적으로 해당 게시물에 대한 판단을 내렸습니다.", null);
    }
	
}

package com.swyp.saratang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.service.JudgeService;

@RestController
public class JudgeController {
	@Autowired
	private JudgeService judgeService;
	
    @PostMapping("/judge")
    public ResponseEntity<?> judge(
        @RequestParam int userId,
        @RequestParam int postId,
        @RequestParam String judgementType
    ) {
        
        // 기본적인 파라미터 검증
        if (!"positive".equals(judgementType) && !"negative".equals(judgementType)) {
            return ResponseEntity.badRequest().body("judgementType은 positive 혹은 negative 중 하나입니다");
        }

        try {
            judgeService.addJudgement(userId, postId, judgementType);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오류 발생: " + e.getMessage());
        }
        return ResponseEntity.ok("Judgment recorded successfully");
    }
	
}

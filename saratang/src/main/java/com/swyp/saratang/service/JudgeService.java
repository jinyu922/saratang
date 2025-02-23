package com.swyp.saratang.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.swyp.saratang.mapper.JudgeMapper;
import com.swyp.saratang.model.PointDTO;

@Service
public class JudgeService {
	
	@Autowired
	private JudgeMapper judgeMapper;
	
	@Autowired
	private PointService pointService;
	
	public void addJudgement(int userId,int postId,String judgementType) {
		//todo 널값 오류 처리 추가해야함
		
        if (judgeMapper.existJudgeByUserIdAndPostId(userId, postId) > 0) {//이미 판단내린건 다시 판단 불가
            throw new IllegalStateException();
        }
        //DB에 굉장히 부하를 많이 주는 방식임, 고도화때 Redis등을 사용하는등 다른 방식으로 변경필요
        //유저 판단횟수 카운트 
        int judgeCount = judgeMapper.countJudgementsByUserId(userId);
        //카운트되면 point service의 포인트 부여 메소드 작동
        if(judgeCount%5==0) {
            PointDTO pointDTO=PointDTO.builder()
            		.userId(userId)
            		.postId(postId)
            		.type("earn")
            		.credits(10)
            		.description("판단 횟수 5회누적 적립")
            		.build();
        	
            pointService.addPoint(pointDTO);
        }
        
		judgeMapper.addJudgement(userId, postId, judgementType);
	}
	
}

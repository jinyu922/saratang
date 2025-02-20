package com.swyp.saratang.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.swyp.saratang.mapper.JudgeMapper;

@Service
public class JudgeService {
	
	@Autowired
	private JudgeMapper judgeMapper;
	
	public void addJudgement(int userId,int postId,String judgementType) {
		//todo 널값 오류 처리 추가해야함
		
        if (judgeMapper.existJudgeByUserIdAndPostId(userId, postId) > 0) {
            throw new IllegalStateException();
        }
		judgeMapper.addJudgement(userId, postId, judgementType);
	}
	
}

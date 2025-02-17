package com.swyp.saratang.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.amazonaws.services.kms.model.NotFoundException;
import com.swyp.saratang.data.RequestList;
import com.swyp.saratang.mapper.BoardMapper;
import com.swyp.saratang.mapper.JudgeMapper;
import com.swyp.saratang.model.BoardDTO;
import com.swyp.saratang.model.PostImageDTO;

@Service
public class BoardService {
	
	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	JudgeMapper judgeMapper;
	
	public Page<BoardDTO> getFashionList(Pageable pageable){
		RequestList<?> requestList=RequestList.builder()
				.pageable(pageable)
				.build();
		
		List<BoardDTO> boardDTOs = boardMapper.getFashionList(requestList);
		
		//요청 시 전달받은 정보 이외의 정보 추가
        for (BoardDTO boardDTO : boardDTOs) {
            List<String> imageUrls = boardMapper.getImagesByPostId(boardDTO.getId());
            boardDTO.setImageUrls(imageUrls);  // imageUrls 필드에 이미지 URL 리스트 추가
        }
		
		int total = boardMapper.getBoardListCount();
		
		return new PageImpl<>(boardDTOs, pageable, total);
	}
	
	public Map<String, Object> getFashionPostById(int id){
		BoardDTO boardDTO = boardMapper.getFashionPostById(id);
		if (boardDTO == null) {
			throw new NotFoundException("데이터가 없습니다");
		}
		Map<String, Integer> judgementCounts = judgeMapper.countJudgementsByPostId(id);
		if (judgementCounts == null) {
		    judgementCounts = new HashMap<>(); // 빈 맵으로 초기화
		}
		
        Map<String, Object> response = new HashMap<>();
        response.put("post", boardDTO);
        response.put("positiveCount", judgementCounts.getOrDefault("positiveCount", 0));
        response.put("negativeCount", judgementCounts.getOrDefault("negativeCount", 0));
		
		
        return response;
	}
	
	public Page<BoardDTO> getDiscountList(Pageable pageable){
		RequestList<?> requestList=RequestList.builder()
				.pageable(pageable)
				.build();
		
		List<BoardDTO> boardDTOs = boardMapper.getDiscountList(requestList);
		
		//요청 시 전달받은 정보 이외의 정보 추가
        for (BoardDTO boardDTO : boardDTOs) {
            List<String> imageUrls = boardMapper.getImagesByPostId(boardDTO.getId());
            boardDTO.setImageUrls(imageUrls);  // imageUrls 필드에 이미지 URL 리스트 추가
        }
		
		int total = boardMapper.getBoardListCount();
		
		return new PageImpl<>(boardDTOs, pageable, total);
	}
	
	public Map<String, Object> getDiscountPostById(int id){
		BoardDTO boardDTO = boardMapper.getDiscountPostById(id);
		if (boardDTO == null) {
			throw new NotFoundException("데이터가 없습니다");
		}
		Map<String, Integer> judgementCounts = judgeMapper.countJudgementsByPostId(id);
		if (judgementCounts == null) {
		    judgementCounts = new HashMap<>(); // 빈 맵으로 초기화
		}
		
        Map<String, Object> response = new HashMap<>();
        response.put("post", boardDTO);
        response.put("positiveCount", judgementCounts.getOrDefault("positiveCount", 0));
        response.put("negativeCount", judgementCounts.getOrDefault("negativeCount", 0));
		
        return response;
	}
	
	public void createPost(BoardDTO boardDTO, List<String> imageUrls) {
		/*
		 * -----------------------------------------------
		 * todo: boardDTO.setUserId(현재 로그인한 사용자의 pk id);
		 * -----------------------------------------------
		 */
		boardMapper.createPost(boardDTO);
		//그림자료 저장
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
            	PostImageDTO postImageDTO=new PostImageDTO(boardDTO.getId(), imageUrl);
            	boardMapper.insertPostImage(postImageDTO);
            }
        }
	}
}

package com.swyp.saratang.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.swyp.saratang.data.RequestList;
import com.swyp.saratang.mapper.BoardMapper;
import com.swyp.saratang.mapper.JudgeMapper;
import com.swyp.saratang.model.BoardDTO;
import com.swyp.saratang.model.CategoryDTO;
import com.swyp.saratang.model.PostImageDTO;
import com.swyp.saratang.session.SessionManager;

@Service
public class BoardService {
	
	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	JudgeMapper judgeMapper;
	
	@Autowired
	SessionManager sessionManager;
	
	@Autowired
	CategoryService categoryService;
	
	
	public Page<BoardDTO> getFashionList(int userId,Pageable pageable,String postType){
	    // 사용자의 선호 카테고리 가져오기
	    CategoryDTO categoryDTO = categoryService.getCategoryList(userId);

	    // CategoryDTO를 List<Integer> categoryIds 형태로 변환
	    List<Integer> categoryIds = new ArrayList<>();
	    if (categoryDTO.isOuterwear()) categoryIds.add(1);
	    if (categoryDTO.isTops()) categoryIds.add(2);
	    if (categoryDTO.isBottoms()) categoryIds.add(3);
	    if (categoryDTO.isUnderwearHomewear()) categoryIds.add(4);
	    if (categoryDTO.isShoes()) categoryIds.add(5);
	    if (categoryDTO.isBags()) categoryIds.add(6);
	    if (categoryDTO.isFashionAccessories()) categoryIds.add(7);
	    if (categoryDTO.isKids()) categoryIds.add(8);
	    if (categoryDTO.isSportsLeisure()) categoryIds.add(9);
	    if (categoryDTO.isDigitalLife()) categoryIds.add(10);
	    if (categoryDTO.isBeauty()) categoryIds.add(11);
	    if (categoryDTO.isFood()) categoryIds.add(12);

	    // 사용자의 선호 카테고리가 없는 경우 빈 결과 반환
	    if (categoryIds.isEmpty()) {
	        return new PageImpl<>(Collections.emptyList(), pageable, 0);
	    }
	    
		RequestList<?> requestList=RequestList.builder()
				.pageable(pageable)
				.postType(postType)
				.categoryIds(categoryIds)  // 변환된 카테고리 리스트 사용
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
	
	public Map<String, Object> getFashionPostById(int id,String postType) throws NotFoundException {
	    try {
	        // 패션 포스트 상세 조회
	        BoardDTO boardDTO = boardMapper.getFashionPostById(id,postType);
	        
	        if (boardDTO == null) {
	            throw new NotFoundException("데이터가 없습니다");
	        }
	        
	        // 판별 결과 조회
	        Map<String, Integer> judgementCounts = judgeMapper.countJudgementsByPostId(id);
	        if (judgementCounts == null) {
	            judgementCounts = new HashMap<>(); // 빈 맵으로 초기화
	        }
	        
	        // 응답 맵 구성
	        Map<String, Object> response = new HashMap<>();
	        response.put("post", boardDTO);
	        response.put("positiveCount", judgementCounts.getOrDefault("positiveCount", 0));
	        response.put("negativeCount", judgementCounts.getOrDefault("negativeCount", 0));
	        
	        return response;
	    } catch (NotFoundException e) {
	        throw e;  // NotFoundException은 그대로 던진다
	    } catch (Exception e) {
	        // 일반 예외 처리
	        throw new RuntimeException("패션정보 상세조회 실패", e);
	    }
	}

//통합예정	
	
//	public Page<BoardDTO> getDiscountList(Pageable pageable){
//		RequestList<?> requestList=RequestList.builder()
//				.pageable(pageable)
//				.build();
//		
//		List<BoardDTO> boardDTOs = boardMapper.getDiscountList(requestList);
//		
//		//요청 시 전달받은 정보 이외의 정보 추가
//        for (BoardDTO boardDTO : boardDTOs) {
//            List<String> imageUrls = boardMapper.getImagesByPostId(boardDTO.getId());
//            boardDTO.setImageUrls(imageUrls);  // imageUrls 필드에 이미지 URL 리스트 추가
//        }
//		
//		int total = boardMapper.getBoardListCount();
//		
//		return new PageImpl<>(boardDTOs, pageable, total);
//	}
//	
//	public Map<String, Object> getDiscountPostById(int id){
//		BoardDTO boardDTO = boardMapper.getDiscountPostById(id);
//		if (boardDTO == null) {
//			throw new NotFoundException("데이터가 없습니다");
//		}
//		Map<String, Integer> judgementCounts = judgeMapper.countJudgementsByPostId(id);
//		if (judgementCounts == null) {
//		    judgementCounts = new HashMap<>(); // 빈 맵으로 초기화
//		}
//		
//        Map<String, Object> response = new HashMap<>();
//        response.put("post", boardDTO);
//        response.put("positiveCount", judgementCounts.getOrDefault("positiveCount", 0));
//        response.put("negativeCount", judgementCounts.getOrDefault("negativeCount", 0));
//		
//        return response;
//	}
	
	public void createPost(BoardDTO boardDTO, List<String> imageUrls) {
		/*
		 * -----------------------------------------------
		 * todo: 세션 정보를 통해 현재 유저 id 알아서 boardDTO에 맵핑
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

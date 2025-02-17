package com.swyp.saratang.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.swyp.saratang.data.RequestList;
import com.swyp.saratang.mapper.BoardMapper;
import com.swyp.saratang.model.BoardDTO;
import com.swyp.saratang.model.PostImageDTO;

@Service
public class BoardService {
	
	@Autowired
	BoardMapper boardMapper;
	
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
	
	public BoardDTO getFashionPostById(int id){
		return boardMapper.getFashionPostById(id);
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
	
	public BoardDTO getDiscountPostById(int id){
		return boardMapper.getDiscountPostById(id);
	}
	
	public void createPost(BoardDTO boardDTO, List<String> imageUrls) {
		if (!("fashion".equals(boardDTO.getPostType()) || "discount".equals(boardDTO.getPostType()))) {
		    throw new IllegalArgumentException("postType 값은 fashion 또는 discount 이여야 합니다");
		}
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

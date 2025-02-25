package com.swyp.saratang.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.swyp.saratang.model.CommentDTO;
import com.swyp.saratang.model.PostImageDTO;
import com.swyp.saratang.model.SafeUserDTO;
import com.swyp.saratang.model.UserDTO;
import com.swyp.saratang.session.SessionManager;

import io.netty.handler.codec.AsciiHeadersEncoder.NewlineType;

@Service
public class BoardService {
	
	@Autowired
	BoardMapper boardMapper;
	@Autowired
	JudgeMapper judgeMapper;
	@Autowired
	SessionManager sessionManager;
	@Autowired
	UserService userService;
	@Autowired
	CategoryService categoryService;
	
	//최신순 페이징 리스트 출력
	public Page<BoardDTO> getFashionList(int userId,Pageable pageable,String postType){
		//Mapper 쿼리에 필요한 내용 정의
		RequestList<?> requestList=RequestList.builder()
				.requestUserId(userId)
				.pageable(pageable)
				.postType(postType)
				.build();
		
		List<BoardDTO> boardDTOs = boardMapper.getFashionListPaging(requestList);
		
		//반환할 게시글 DTO마다 url 정보 추가
        for (BoardDTO boardDTO : boardDTOs) {
            List<String> imageUrls = boardMapper.getImagesByPostId(boardDTO.getId());
            boardDTO.setImageUrls(imageUrls);  // imageUrls 필드에 이미지 URL 리스트 추가
        }
		
		int total = boardMapper.getBoardListCount();
		
		return new PageImpl<>(boardDTOs, pageable, total);
	}
	
	//최신+인기+랜덤 복합 리스트 출력 (페이징x limitSize만큼의 결과 출력)
	public List<BoardDTO> getFashionListAlgorithm(int userId,String postType,int limitSize,int finalLimitSize)throws IllegalArgumentException{
		//Mapper 쿼리에 필요한 내용 정의
		RequestList<?> requestList=RequestList.builder()
				.requestUserId(userId)
				.postType(postType)
				.limitSize(limitSize)
				.build();
		
		//최신,인기,랜덤으로 뽑아서 합친다음 무작위로 섞고 finalLimitSize 만큼 반환
		List<BoardDTO> boardDTOsLatest = boardMapper.getFashionListLatest(requestList);
		List<BoardDTO> boardDTOsPopular = boardMapper.getFashionListPopular(requestList);
		List<BoardDTO> boardDTOsRandom = boardMapper.getFashionListRandom(requestList);
		List<BoardDTO> resultDTOs = new ArrayList<>();
		resultDTOs.addAll(boardDTOsLatest);
		resultDTOs.addAll(boardDTOsPopular);
		resultDTOs.addAll(boardDTOsRandom);
		// 중복 제거후 리스트 변환 (Set을 사용하여 중복 제거 후 List로 변환)
		Set<BoardDTO> resultSet = new HashSet<>(resultDTOs);
		resultDTOs = new ArrayList<>(resultSet);
		
		Collections.shuffle(resultDTOs); // 무작위 섞기
		
		// 만약 섞은 게시물 총 개수가 요청한 개수보다 많을경우 자른다
		if(resultDTOs.size()>finalLimitSize) {
			try {
				resultDTOs = resultDTOs.subList(0, finalLimitSize);//
			} catch (Exception e) {
				throw e;
			}
		}

		// 반환할 게시글 DTO마다 url 정보 추가
        for (BoardDTO boardDTO : resultDTOs) {
            List<String> imageUrls = boardMapper.getImagesByPostId(boardDTO.getId());
            boardDTO.setImageUrls(imageUrls);  // imageUrls 필드에 이미지 URL 리스트 추가
        }
		
		return resultDTOs;
	}
	
	public Map<String, Object> getFashionPostById(int id,String postType) throws NotFoundException {
	    try {
	        // 패션 포스트 상세 조회
	        BoardDTO boardDTO = boardMapper.getFashionPostById(id,postType);
	        if (boardDTO == null) {
	            throw new NotFoundException("데이터가 없습니다");
	        }
	        // 작성자 프로필 조회(프론트요청)
	        UserDTO userDTO = userService.getUserById(boardDTO.getUserId());
	        SafeUserDTO safeUserDTO = new SafeUserDTO(userDTO); // 민감한 데이터 제외
	        
	        // 판별 결과 조회
	        Map<String, BigDecimal> judgementCounts = judgeMapper.countJudgementsByPostId(id);
	        if (judgementCounts == null) {
	            judgementCounts = new HashMap<>(); // 빈 맵으로 초기화
	        }
	        
	        
	        //남여 통계
	        int positiveCount=judgementCounts.get("positiveCount").intValue();
	        int negativeCount=judgementCounts.get("negativeCount").intValue();
	        int malePositiveCount=judgementCounts.get("malePositiveCount").intValue();
	        int maleNegativeCount=judgementCounts.get("maleNegativeCount").intValue();
	        int femalePositiveCount=judgementCounts.get("femalePositiveCount").intValue();
	        int femaleNegativeCount=judgementCounts.get("femaleNegativeCount").intValue();
	        
	        // 남자/여자별 총 투표 수
	        int maleTotal = malePositiveCount + maleNegativeCount;
	        int femaleTotal = femalePositiveCount + femaleNegativeCount;

	        // 퍼센트 계산 (0으로 나누는 경우 방지)
	        int positiveRate=(positiveCount==0)?0:(positiveCount*100/(positiveCount+negativeCount));
	        int negativeRate =(negativeCount==0)?0:(negativeCount*100/(positiveCount+negativeCount));
	        int malePositiveRate =(maleTotal == 0) ? 0 : (malePositiveCount * 100 / maleTotal);
	        int maleNegativeRate =(maleTotal == 0) ? 0 : (maleNegativeCount * 100 / maleTotal);
	        int femalePositiveRate =(femaleTotal == 0) ? 0 : (femalePositiveCount * 100 / femaleTotal);
	        int femaleNegativeRate =(femaleTotal == 0) ? 0 :(femaleNegativeCount * 100 / femaleTotal);
	        


	        // 응답 맵 구성
	        Map<String, Object> response = new LinkedHashMap<>();
	        response.put("content", boardDTO);
	        response.put("writerProfile", safeUserDTO);
	        response.put("positiveCount", positiveCount);
	        response.put("negativeCount", negativeCount);
	        response.put("positiveRate", positiveRate);
	        response.put("negativeRate", negativeRate);
	        response.put("malePositiveRate",malePositiveRate );
	        response.put("maleNegativeRate", maleNegativeRate);
	        response.put("femalePositiveRate", femalePositiveRate);
	        response.put("femaleNegativeRate", femaleNegativeRate);
	        
	        return response;
	    } catch (NotFoundException e) {
	        throw e;  // NotFoundException은 그대로 던진다
	    } catch (Exception e) {
	        // 일반 예외 처리
	        throw new RuntimeException("패션정보 상세조회 실패"+e.getMessage());
	    }
	}
	
	//히스토리 조회
	public Page<BoardDTO> getHistory(int userId,Pageable pageable,String postType,String judgementType,String sortType){
		//Mapper 쿼리에 필요한 내용 정의
		RequestList<?> requestList=RequestList.builder()
				.requestUserId(userId)
				.pageable(pageable)
				.postType(postType)
				.judgementType(judgementType)
				.sortType(sortType)
				.build();
		
		List<BoardDTO> boardDTOs = boardMapper.getHistory(requestList);
		
		//반환할 게시글 DTO마다 url 정보 추가
        for (BoardDTO boardDTO : boardDTOs) {
            List<String> imageUrls = boardMapper.getImagesByPostId(boardDTO.getId());
            boardDTO.setImageUrls(imageUrls);  // imageUrls 필드에 이미지 URL 리스트 추가
        }
		
		int total = boardMapper.getBoardListCount();
		
		return new PageImpl<>(boardDTOs, pageable, total);
	}
	
	public Integer createPost(BoardDTO boardDTO, List<String> imageUrls) {
	
		boardMapper.createPost(boardDTO);
		//그림자료 저장
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
            	PostImageDTO postImageDTO=new PostImageDTO(boardDTO.getId(), imageUrl);
            	boardMapper.insertPostImage(postImageDTO);
            }
        }
        return boardDTO.getId();
	}
	
	public Page<BoardDTO> getBest(int userId,Pageable pageable,String postType,int period) {
		//Mapper 쿼리에 필요한 내용 정의
		RequestList<?> requestList=RequestList.builder()
				.requestUserId(userId)
				.pageable(pageable)
				.period(period)
				.build();
		List<BoardDTO> boardDTOs= new ArrayList<>();
		
		if("fashion".equals(postType)) {
			boardDTOs = boardMapper.getFashionBest(requestList);
		}
		else if("discount".equals(postType)) {
			boardDTOs = boardMapper.getDiscountBest(requestList);
		}
		
		
		//반환할 게시글 DTO마다 url 정보 추가
        for (BoardDTO boardDTO : boardDTOs) {
            List<String> imageUrls = boardMapper.getImagesByPostId(boardDTO.getId());
            boardDTO.setImageUrls(imageUrls);  // imageUrls 필드에 이미지 URL 리스트 추가
        }
		
		int total = boardMapper.getBoardListCount();
		
		return new PageImpl<>(boardDTOs, pageable, total);
		
	}
	
	public void insertComment(CommentDTO commentDTO) {
		boardMapper.insertComment(commentDTO);
	}
	
	public Page<CommentDTO> getCommentList(int postId,Pageable pageable) {
		
		List<CommentDTO> commentDTOs=new ArrayList<>();
		commentDTOs = boardMapper.getCommentList(postId, pageable);
		
		int total = boardMapper.getCommentListCount();
		
		return new PageImpl<>(commentDTOs, pageable, total);
	}
	
	public BoardDTO getBoardById(Integer boardId) {
	        return boardMapper.getBoardById(boardId);
	    }
	
}

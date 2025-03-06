package com.swyp.saratang.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.swyp.saratang.data.RequestList;
import com.swyp.saratang.exception.UnauthorizedAccessException;
import com.swyp.saratang.mapper.BoardMapper;
import com.swyp.saratang.mapper.JudgeMapper;
import com.swyp.saratang.model.BoardDTO;
import com.swyp.saratang.model.CommentDTO;
import com.swyp.saratang.model.PostImageDTO;
import com.swyp.saratang.model.SafeUserDTO;
import com.swyp.saratang.model.UserDTO;
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
	UserService userService;
	@Autowired
	CategoryService categoryService;
	@Autowired
	JudgeService judgeService;
	@Autowired
	NCPStorageService ncpStorageService;
	
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
	
	public Map<String, Object> getFashionPostById(int postId,int userId,String postType) throws NotFoundException {
	    try {
	        // 패션 포스트 상세 조회
	        BoardDTO boardDTO = boardMapper.getFashionPostById(postId,postType);
	        if (boardDTO == null) {
	            throw new NotFoundException("데이터가 없습니다");
	        }
	        List<String> imageUrls = boardMapper.getImagesByPostId(boardDTO.getId());
	        boardDTO.setImageUrls(imageUrls);  // imageUrls 필드에 이미지 URL 리스트 추가
	        // 작성자 프로필 조회(프론트요청)
	        UserDTO userDTO = userService.getUserById(boardDTO.getUserId());
	        SafeUserDTO safeUserDTO = new SafeUserDTO(userDTO); // 민감한 데이터 제외
	        
	        // 판별 결과 조회
	        Map<String, BigDecimal> judgementCounts = judgeMapper.countJudgementsByPostId(postId);
	        if (judgementCounts == null) {
	            judgementCounts = new HashMap<>(); // 빈 맵으로 초기화
	        }
	        
	        // 작성자의 사라 마라 판단 여부 조회
	        String judge=judgeService.getJudegeByUserIdAndPostId(userId, postId);
	        
	        
	        //남여 통계
	        int positiveCount = judgementCounts.getOrDefault("positiveCount", BigDecimal.ZERO).intValue();
	        int negativeCount = judgementCounts.getOrDefault("negativeCount", BigDecimal.ZERO).intValue();
	        int malePositiveCount = judgementCounts.getOrDefault("malePositiveCount", BigDecimal.ZERO).intValue();
	        int maleNegativeCount = judgementCounts.getOrDefault("maleNegativeCount", BigDecimal.ZERO).intValue();
	        int femalePositiveCount = judgementCounts.getOrDefault("femalePositiveCount", BigDecimal.ZERO).intValue();
	        int femaleNegativeCount = judgementCounts.getOrDefault("femaleNegativeCount", BigDecimal.ZERO).intValue();
	        
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
	        response.put("userJudgement", judge);
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
		
		int total = boardMapper.getHistoryCount(requestList);
		
		return new PageImpl<>(boardDTOs, pageable, total);
	}
	
	//링크 히스토리 조회
	public Page<BoardDTO> getLinkHistory(int userId,Pageable pageable,String postType,String sortType){
		//Mapper 쿼리에 필요한 내용 정의
		RequestList<?> requestList=RequestList.builder()
				.requestUserId(userId)
				.pageable(pageable)
				.sortType(sortType)
				.postType(postType)
				.build();
		
		List<BoardDTO> boardDTOs = boardMapper.getLinkHistory(requestList);
		
		//반환할 게시글 DTO마다 url 정보 추가
        for (BoardDTO boardDTO : boardDTOs) {
            List<String> imageUrls = boardMapper.getImagesByPostId(boardDTO.getId());
            boardDTO.setImageUrls(imageUrls);  // imageUrls 필드에 이미지 URL 리스트 추가
        }
		
		int total = boardMapper.getLinkHistoryCount(requestList);
		
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
		int total = boardMapper.getBoardListCount();
		if("fashion".equals(postType)) {
			boardDTOs = boardMapper.getFashionBest(requestList);
			total=boardMapper.getFashionBestCount(requestList);
		}
		else if("discount".equals(postType)) {
			boardDTOs = boardMapper.getDiscountBest(requestList);
			total=boardMapper.getDiscountBestCount(requestList);
		}
		
		
		//반환할 게시글 DTO마다 url 정보 추가
        for (BoardDTO boardDTO : boardDTOs) {
            List<String> imageUrls = boardMapper.getImagesByPostId(boardDTO.getId());
            boardDTO.setImageUrls(imageUrls);  // imageUrls 필드에 이미지 URL 리스트 추가
        }
		
		
		
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
	
	public BoardDTO getUrlById(Integer boardId) {
	        return boardMapper.getUrlById(boardId);
	}
	
	//작성한 게시글 출력
	public Page<BoardDTO> getMyPosts(int userId,Pageable pageable){
		//Mapper 쿼리에 필요한 내용 정의
		RequestList<?> requestList=RequestList.builder()
				.requestUserId(userId)
				.pageable(pageable)
				.build();
		
		List<BoardDTO> boardDTOs = boardMapper.getMyPosts(requestList);
		
		//반환할 게시글 DTO마다 url 정보 추가
        for (BoardDTO boardDTO : boardDTOs) {
            List<String> imageUrls = boardMapper.getImagesByPostId(boardDTO.getId());
            boardDTO.setImageUrls(imageUrls);  // imageUrls 필드에 이미지 URL 리스트 추가
        }
		
		int total = boardMapper.getMyPostsCount(requestList);
		
		return new PageImpl<>(boardDTOs, pageable, total);
	}

	//게시글 수정
	public Integer updatePost(BoardDTO boardDTO, List<String> imageUrls,int RequestUserId,int postId) throws RuntimeException{
		
		//클라이언트에서 보낸 boardDTO엔 id,userId 정보가 없음
		
		//변경 요청 게시글 찾기
		BoardDTO targetPost=boardMapper.getPostById(postId);
		
		//변경 요청 게시글이 실제로 존재하는지
		if(targetPost==null) {
			throw new NoSuchElementException();
		}
		
		//요청자가 게시글 쓴 본인인지
		Integer postWriterUserId=targetPost.getUserId();
		if(postWriterUserId!=RequestUserId) {
			throw new UnauthorizedAccessException(null);
		}
		
		//게시글 수정 쿼리
		boardDTO.setId(targetPost.getId());
		boardMapper.updatePost(boardDTO);
		
		//새로운 이미지 url 정보가 있다면
		if(imageUrls != null && !imageUrls.isEmpty()) {
			System.out.println("새로운 이미지 url정보 있음");
			//기존 그림자료 삭제를 위해 url 가져옴
			List<String> oldImageUrls = boardMapper.getImagesByPostId(boardDTO.getId());
			System.out.println("oldImageUrls :"+oldImageUrls);
			//기존 그림자료 S3에서 삭제
			ncpStorageService.deleteFiles(oldImageUrls);
			//기존 그림자료 url 데이터베이스에서 삭제
			boardMapper.deletePostImage(postId);
			
			//새 그림자료 저장
	        if (imageUrls != null && !imageUrls.isEmpty()) {
	            for (String imageUrl : imageUrls) {
	            	PostImageDTO postImageDTO=new PostImageDTO(boardDTO.getId(), imageUrl);
	            	boardMapper.insertPostImage(postImageDTO);
	            }
	        }
		}
		
        //수정한 게시글 id 반환
        return boardDTO.getId();
	}
}

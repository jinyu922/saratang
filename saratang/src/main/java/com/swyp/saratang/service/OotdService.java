package com.swyp.saratang.service;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.swyp.saratang.exception.AlreadyExistException;
import com.swyp.saratang.exception.NotFoundException;
import com.swyp.saratang.mapper.OotdMapper;
import com.swyp.saratang.model.OotdDTO;

@Service
public class OotdService {
	
	@Autowired
	private OotdMapper ootdMapper;
	@Autowired
	private NCPStorageService ncpStorageService;
	
	public int createOotd(OotdDTO ootdDTO) {
		ootdMapper.insertOotd(ootdDTO);
		List<String> imageUrls = ootdDTO.getOotdImageUrls();
		//그림자료 url 저장
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
            	ootdMapper.insertOotdImage(ootdDTO.getId(), imageUrl);
            }
        }
        return ootdDTO.getId();
	}
	
	public void deleteOotd(int UserId,int postId) throws RuntimeException{
		// 삭제하려는 OOTD가 있는지 확인
		OotdDTO ootdDTO=ootdMapper.getOotdById(postId);
		if(ootdDTO==null) {
			throw new NotFoundException("삭제할 데이터가 없습니다");
		}
    	// 삭제하려는 OOTD가 로그인한 본인인지 확인
        if(UserId!=ootdDTO.getUserId()) {
        	throw new RuntimeException("권한부족");
        }
		ootdMapper.deleteOotdById(postId);
		//기존 그림자료 삭제를 위해 url 가져옴
		List<String> oldImageUrls = ootdMapper.getImagesByOotdPostId(postId);
		//기존 그림자료 S3에서 삭제
		ncpStorageService.deleteFiles(oldImageUrls);
	}
	
	public void likeOotd(int UserId,int postId) throws RuntimeException{
		if(ootdMapper.existOotdLike(UserId, postId)==1) {
			throw new AlreadyExistException("유저는 이미 좋아요 판단을 내렸습니다");
		}
		ootdMapper.insertOotdLike(UserId, postId);
		ootdMapper.incrementOotdLikeCount(postId);
	}
	
	public void unlikeOotd(int UserId,int postId) throws RuntimeException{
		if(ootdMapper.existOotdLike(UserId, postId)!=1) {
			throw new NotFoundException("철회할 좋아요 판단이 없습니다");
		}
		ootdMapper.deleteOotdLike(UserId, postId);
		ootdMapper.decrementOotdLikeCount(postId);
	}
	
	public void scrapOotd(int UserId,int postId) throws RuntimeException{
		if(ootdMapper.existOotdScrap(UserId, postId)==1) {
			throw new AlreadyExistException("이미 스크랩한 게시글");
		}
		ootdMapper.insertOotdScrap(UserId, postId);
		ootdMapper.incrementOotdScrapCount(postId);
	}
	
	public void unscrapOotd(int UserId,int postId) throws RuntimeException{
		if(ootdMapper.existOotdLike(UserId, postId)!=1) {
			throw new NotFoundException("철회할 스크랩 기록이 없습니다");
		}
		ootdMapper.deleteOotdScrap(UserId, postId);
		ootdMapper.decrementOotdScrapCount(postId);
	}
	
	public Page<Map<String, Object>> getOotds(int userId,String sort,Pageable pageable) throws RuntimeException{
		List<Map<String, Object>> responses = new ArrayList<>();
		
		List<OotdDTO> ootdDTOs=new ArrayList<>();
		if(sort.equals("recent")) {
			ootdDTOs=ootdMapper.selectOotds(pageable);
		}else if (sort.equals("like")) {
			ootdDTOs=ootdMapper.selectOotdsByLikes(pageable);
		}
		
		// 응답 맵 구성
		for(OotdDTO ootdDTO:ootdDTOs) {
			Map<String,Object> response=new HashMap<>();
			Integer postId=ootdDTO.getId();
			//반환할 게시글 DTO마다 url 정보 추가
			List<String> imageUrls = ootdMapper.getImagesByOotdPostId(postId);
			ootdDTO.setOotdImageUrls(imageUrls);
		
			response.put("content", ootdDTO);
			response.put("requestUserLike", ootdMapper.existOotdLike(userId, postId));
			response.put("requestUserScrap", ootdMapper.existOotdScrap(userId, postId));
			responses.add(response);
		}
		
		int total=ootdMapper.selectOotdsCount();
		return new PageImpl<>(responses, pageable, total);
				
	}
	
	public Page<OotdDTO> getLikedOotds(int userId,Pageable pageable){
		List<OotdDTO> ootdDTOs=new ArrayList<>();
		ootdDTOs=ootdMapper.selectLikedOotdsByUserId(userId, pageable);
		for(OotdDTO ootdDTO:ootdDTOs) {
			Integer postId=ootdDTO.getId();
			List<String> imageUrls = ootdMapper.getImagesByOotdPostId(postId);
			ootdDTO.setOotdImageUrls(imageUrls);
		}
		
		int total=ootdMapper.selectLikedOotdsByUserIdCount(userId);
		return new PageImpl<>(ootdDTOs, pageable, total);
	}
	
	public Page<OotdDTO> getScrappedOotds(int userId,Pageable pageable){
		List<OotdDTO> ootdDTOs=new ArrayList<>();
		ootdDTOs=ootdMapper.selectScrapedOotdsByUserId(userId, pageable);
		for(OotdDTO ootdDTO:ootdDTOs) {
			Integer postId=ootdDTO.getId();
			List<String> imageUrls = ootdMapper.getImagesByOotdPostId(postId);
			ootdDTO.setOotdImageUrls(imageUrls);
		}
		
		int total=ootdMapper.selectScrapedOotdsByUserIdCount(userId);
		return new PageImpl<>(ootdDTOs, pageable, total);
	}
	
	public Page<OotdDTO> getMyOotds(int userId,Pageable pageable){
		List<OotdDTO> ootdDTOs=new ArrayList<>();
		ootdDTOs=ootdMapper.selectOotdsByUserId(userId, pageable);
		for(OotdDTO ootdDTO:ootdDTOs) {
			Integer postId=ootdDTO.getId();
			List<String> imageUrls = ootdMapper.getImagesByOotdPostId(postId);
			ootdDTO.setOotdImageUrls(imageUrls);
		}
		
		int total=ootdMapper.selectOotdsByUserIdCount(userId);
		return new PageImpl<>(ootdDTOs, pageable, total);
	}
}

package com.swyp.saratang.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.swyp.saratang.data.RequestList;
import com.swyp.saratang.mapper.PostMapper;
import com.swyp.saratang.model.PostDTO;
import com.swyp.saratang.model.PostImageDTO;

@Service
public class FashionService {
	
	@Autowired
	PostMapper postMapper;
	
	public Page<PostDTO> getFashionList(Pageable pageable){
		RequestList<?> requestList=RequestList.builder()
				.pageable(pageable)
				.build();
		
		List<PostDTO> postDTOs = postMapper.getFashionList(requestList);
        for (PostDTO postDTO : postDTOs) {
            List<String> imageUrls = postMapper.getImagesByPostId(postDTO.getId());
            postDTO.setImageUrls(imageUrls);  // imageUrls 필드에 이미지 URL 리스트 추가
        }
		
		int total = postMapper.getFashionListCount();
		
		return new PageImpl<>(postDTOs, pageable, total);
	}
	
	public void createFashionPost(PostDTO postDTO, List<String> imageUrls) {
		postMapper.createFashionPost(postDTO);
		//그림자료 저장
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
            	PostImageDTO postImageDTO=new PostImageDTO(postDTO.getId(), imageUrl);
                postMapper.insertPostImage(postImageDTO);
            }
        }
	}
}

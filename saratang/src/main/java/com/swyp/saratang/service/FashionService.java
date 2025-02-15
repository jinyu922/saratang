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

@Service
public class FashionService {
	
	@Autowired
	PostMapper postMapper;
	
	public Page<Map<String, Object>> getFashionList(Pageable pageable){
		RequestList<?> requestList=RequestList.builder()
				.pageable(pageable)
				.build();
		
		List<Map<String, Object>> content = postMapper.getFashionList(requestList);
		int total = postMapper.getFashionListCount();
		

		
		return new PageImpl<>(content, pageable, total);
	}
	
	public void createFashionPost(PostDTO postDTO) {
		//게시물 post랑
		postMapper.createFashionPost(postDTO);
		//그림자료 저장하는거까지
	}
}

package com.swyp.saratang.data;

import java.util.List;

import org.springframework.data.domain.Pageable;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RequestList<T> { //패션/할인정보 요청시 필요한 요소들, 빌더 패턴으로 페이징 구현하기 위해 추가
	private T data; //리턴 데이터 값
	private String postType; //패션 정보를 요청하는지 할인 정보를 요청하는지
	private Pageable pageable; //요청 페이지 정보
	private List<Integer> categoryIds; // 선호 카테고리 리스트 추가
}
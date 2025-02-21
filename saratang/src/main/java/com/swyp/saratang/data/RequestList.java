package com.swyp.saratang.data;


import org.springframework.data.domain.Pageable;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RequestList<T> { //패션/할인정보 요청시 필요한 요소들, 빌더 패턴으로 페이징 구현하기 위해 추가
	private T data; //요청시 필요한 데이터값을 보내는데 사용, 주로 게시글 저장 시 게시글 정보를 담는 DTO를 보낸다
	private int requestUserId;//요청한 유저 아이디, 유저 선호 카테고리 별 필터링 등의 용도로 사용
	private String postType; //패션 정보를 요청하는지 할인 정보를 요청하는지
	private int limitSize; //알고리즘 쿼리를 보낼때 랜덤성을 조절하는 변수
	private int finalLimitSize; //페이징을 지원하지 않는 쿼리를 보낼때 응답 게시물 개수를 조절하는 변수
	private Pageable pageable; //페이징 지원 쿼리를 보낼때 필요한 변수
}
package com.swyp.saratang.data;

import org.springframework.data.domain.Pageable;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RequestList<T> { //빌더 패턴으로 페이징 구현하기 위해 추가
	private T data;
	private Pageable pageable;
}

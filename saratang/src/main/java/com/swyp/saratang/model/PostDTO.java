package com.swyp.saratang.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Integer id; // 게시글 고유 ID *
    private Integer userId; // 게시글 작성자 ID *
    private Integer categoryId; // 게시글의 카테고리 ID
    private Integer concernKeywordId; // 고민 키워드 ID
    private String postType; // 게시글 타입 ('fashion', 'discount') *

    private String brand; // 브랜드 이름
    private Integer originalPrice; // 출시가
    private Integer discountPrice; // 할인 가격
    private String productLink; // 제품 링크 (외부 사이트 URL)
    private String title; // 제목

    private Integer currentPrice; // 현재 가격
    private Boolean isSpecPublic; // 스펙 공개 여부
    private String memo; // 메모 또는 구매 고민 이유
    private String detailMemo; // 상세 이유
    private LocalDateTime regdate; // 게시글 작성일 *
    
    private List<String> imageUrls; // 이미지 URL 리스트
}
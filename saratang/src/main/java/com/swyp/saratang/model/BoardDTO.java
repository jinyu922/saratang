package com.swyp.saratang.model;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {
    @Schema(description = "게시글 고유 ID (자동 생성)", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id; // 게시글 고유 ID *

    @Schema(description = "게시글 작성자 고유 ID", example = "1")
    private Integer userId; // 게시글 작성자 ID *

    @Schema(description = "게시글의 카테고리 ID 1.아우터 2.상의 3.하의 4.속옷/홈웨어 5.신발 6.가방 7.패션소품 8.키즈 9.스포츠/레저 10.디지털/라이프 11.뷰티 12.식품", example = "5")
    private Integer categoryId; // 게시글의 카테고리 ID

    @Schema(description = "고민 키워드 ID 1.핏 2.사이즈 3.브랜드 4.가격 (패션정보only)", example = "1")
    private Integer concernKeywordId; // 고민 키워드 ID

    @Schema(description = "게시글 타입 문자열로 (fashion 또는 discount)", example = "fashion")
    private String postType; // 게시글 타입 ('fashion', 'discount') *

    @Schema(description = "브랜드 이름", example = "Nike")
    private String brand; // 브랜드 이름

    @Schema(description = "출시가 (할인정보only)", example = "0")
    private Integer originalPrice; // 출시가

    @Schema(description = "할인 가격 (할인정보only)", example = "0")
    private Integer discountPrice; // 할인 가격

    @Schema(description = "제품 링크 (외부 사이트 URL)", example = "https://www.nike.com/shoes/air-force-1")
    private String productLink; // 제품 링크 (외부 사이트 URL)

    @Schema(description = "게시글 제목", example = "나이키 에어포스 1")
    private String title; // 제목

    @Schema(description = "현재 가격", example = "90000")
    private Integer currentPrice; // 현재 가격

    @Schema(description = "스펙 공개 여부 true/false (패션정보only)", example = "true")
    private Boolean isSpecPublic; // 스펙 공개 여부

    @Schema(description = "메모 또는 구매 고민 이유 (할인정보only)", example = "에어포스 1을 이용한 코디")
    private String memo; // 메모 또는 구매 고민 이유

    @Schema(description = "상세 이유 (패션정보only)", example = "할인을 기다렸는데 이제 적당한 가격이 된 것 같아요.")
    private String detailMemo; // 상세 이유

    @Schema(description = "게시글 작성일 (자동 생성)", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime regdate; // 게시글 작성일 *

    @Schema(description = "이미지 URL 리스트", example = "[\"https://example.com/image1.jpg\"]")
    private List<String> imageUrls; // 이미지 URL 리스트
}
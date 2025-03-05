package com.swyp.saratang.model;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OotdDTO {
	
    @Schema(description = "OOTD 고유 ID (자동 생성)", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;
    @Schema(description = "OOTD 작성자 고유 ID", example = "1")
    private Integer userId;
    @Schema(description = "OOTD 제목", example = "셔츠를 활용한 코디")
    private String title;
    @Schema(description = "OOTD 내용", example = "계절감에 맞춰서 셔츠를 레이어링한 코디에요!")
    private String content;    
    @Schema(description = "좋아요 개수")
    private Integer likeCount;    
    @Schema(description = "스크랩 개수")
    private Integer scrapCount; //미사용    
    @Schema(description = "OOTD 작성일 (자동 생성)", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;    
    @Schema(description = "OOTD 수정일 (자동 생성)", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt; //미사용
    @Schema(description = "OOTD 이미지 URL 리스트", example = "[\"https://example.com/image1.jpg\"]")
    private List<String> ootdImageUrls; // 이미지 URL 리스트
    
    public OotdDTO() {}
   
    public OotdDTO(Integer id, Integer userId, String title, String content, 
    		Integer likeCount, Integer scrapCount, LocalDateTime createdAt, 
    		LocalDateTime updatedAt,List<String> ootdImageUrls) {
        this.id = id;
        this.userId = userId;//*
        this.title = title;//*
        this.content = content;//*
        this.likeCount = likeCount;
        this.scrapCount = scrapCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.ootdImageUrls = ootdImageUrls;
    }

}

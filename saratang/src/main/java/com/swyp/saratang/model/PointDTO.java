package com.swyp.saratang.model;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointDTO {
    private Integer id; // 포인트 거래 고유 ID
    private Integer userId; // 사용자 ID
    private Integer postId; // 관련 게시글 ID (없을 수도 있음)

    @Schema(description = "포인트 유형 ('earn' 적립, 'spend' 사용)", allowableValues = { "earn", "spend" })
    private String type; // 포인트 유형 (적립: earn, 사용: spend)
    private Integer credits; // 포인트 양
    private String description; // 설명
    private LocalDateTime regdate; // 거래 발생 시간
}

package com.swyp.saratang.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LinkAccessDTO {
    private int id;
    private int userId;
    private int postId;
    private LocalDateTime regdate; // 링크 접근권한 획득일

}

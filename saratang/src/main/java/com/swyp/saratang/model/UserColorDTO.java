package com.swyp.saratang.model;

import lombok.Data;

@Data
public class UserColorDTO {
    private int colorId;      // 색상의 고유 ID
    private String colorName; // 색상 코드 또는 이름
    private int userId;       // 사용자 ID
}
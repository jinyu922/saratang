package com.swyp.saratang.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponseDTO<T> {
    private int status;   // 상태 코드 (200, 400, 500 등)
    private String message;  // 응답 메시지
    private T data;  // 실제 응답 데이터 (UserDTO 등)
}
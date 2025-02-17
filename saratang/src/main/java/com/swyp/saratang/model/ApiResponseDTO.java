package com.swyp.saratang.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponseDTO<T> {
    private int status;   
    private String message;  
    private T data;  // 실제 응답 데이터 (UserDTO 등)
}
package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;

    // 성공 응답 생성
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    // 성공 응답 생성 (데이터 없음)
    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }

    // 에러 응답 생성
    public static <T> ApiResponse<T> error(String message) {
        return error(message, null);
    }

    // 에러 응답 생성 (에러 코드 포함)
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrorCode(errorCode);
        return response;
    }
}
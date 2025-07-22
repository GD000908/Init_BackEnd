package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    
    public static <T> SpecApiResponse<T> success(T data) {
        return SpecApiResponse.<T>builder()
                .success(true)
                .message("성공")
                .data(data)
                .build();
    }
    
    public static <T> SpecApiResponse<T> success(String message, T data) {
        return SpecApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
    
    public static <T> SpecApiResponse<T> error(String message) {
        return SpecApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}

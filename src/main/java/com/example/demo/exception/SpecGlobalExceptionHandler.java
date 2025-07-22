package com.example.demo.exception;

import com.example.demo.dto.SpecApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.demo.controller")
@Slf4j
public class SpecGlobalExceptionHandler {

    @ExceptionHandler(SpecNotFoundException.class)
    public ResponseEntity<SpecApiResponse<Object>> handleSpecNotFoundException(SpecNotFoundException e) {
        log.error("Spec Not Found Exception: ", e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(SpecApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(SpecValidationException.class)
    public ResponseEntity<SpecApiResponse<Object>> handleSpecValidationException(SpecValidationException e) {
        log.error("Spec Validation Exception: ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(SpecApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<SpecApiResponse<Object>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime Exception in Spec API: ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(SpecApiResponse.error("요청을 처리할 수 없습니다: " + e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SpecApiResponse<Object>> handleException(Exception e) {
        log.error("Unexpected Exception in Spec API: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SpecApiResponse.error("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<SpecApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal Argument Exception in Spec API: ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(SpecApiResponse.error("잘못된 요청입니다: " + e.getMessage()));
    }
}

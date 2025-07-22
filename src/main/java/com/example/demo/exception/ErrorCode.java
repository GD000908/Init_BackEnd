package com.example.demo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // User 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자입니다."),

    // Resume 관련 에러
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "이력서를 찾을 수 없습니다."),
    RESUME_ACCESS_DENIED(HttpStatus.FORBIDDEN, "R002", "이력서에 접근할 권한이 없습니다."),

    // 일반적인 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),

    // 🔥 로그인 관련 에러 추가
    LOGIN_FAILED(HttpStatus.BAD_REQUEST, "A001", "아이디 또는 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A002", "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A003", "접근 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
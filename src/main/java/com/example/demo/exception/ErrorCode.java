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

    // 로그인 관련 에러
    LOGIN_FAILED(HttpStatus.BAD_REQUEST, "A001", "아이디 또는 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A002", "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A003", "접근 권한이 없습니다."),
    
    // 🔥 누락된 에러 코드들 추가
    COMMUNITY_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "CP001", "커뮤니티 프로필을 찾을 수 없습니다."),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "P001", "게시글에 접근할 권한이 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P002", "게시글을 찾을 수 없습니다."),
    
    // 기타 에러 코드들
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "댓글을 찾을 수 없습니다."),
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "팔로우 관계를 찾을 수 없습니다."),
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "북마크를 찾을 수 없습니다."),
    FILE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, "FU001", "파일 업로드 중 오류가 발생했습니다."),
    
    // 채용공고 관련
    JOB_POSTING_NOT_FOUND(HttpStatus.NOT_FOUND, "J001", "채용공고를 찾을 수 없습니다."),
    JOB_BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "JB001", "채용공고 북마크를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
package com.example.demo.exception;

public class SpecValidationException extends RuntimeException {
    
    public SpecValidationException(String message) {
        super(message);
    }
    
    public SpecValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static SpecValidationException invalidEmail(String email) {
        return new SpecValidationException("잘못된 이메일 형식입니다: " + email);
    }
    
    public static SpecValidationException invalidPhone(String phone) {
        return new SpecValidationException("잘못된 전화번호 형식입니다: " + phone);
    }
    
    public static SpecValidationException invalidDateRange(String startDate, String endDate) {
        return new SpecValidationException("시작일이 종료일보다 늦을 수 없습니다. 시작일: " + startDate + ", 종료일: " + endDate);
    }
    
    public static SpecValidationException invalidSkillLevel(Integer level) {
        return new SpecValidationException("스킬 레벨은 1-5 사이여야 합니다. 입력값: " + level);
    }
    
    public static SpecValidationException emptyRequiredField(String fieldName) {
        return new SpecValidationException("필수 입력 항목이 비어있습니다: " + fieldName);
    }
    
    public static SpecValidationException invalidUrl(String url) {
        return new SpecValidationException("잘못된 URL 형식입니다: " + url);
    }
}

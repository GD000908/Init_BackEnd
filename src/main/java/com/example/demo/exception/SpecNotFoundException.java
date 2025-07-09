package com.example.demo.exception;

public class SpecNotFoundException extends RuntimeException {
    
    public SpecNotFoundException(String message) {
        super(message);
    }
    
    public SpecNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static SpecNotFoundException userNotFound(Long userId) {
        return new SpecNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId);
    }
    
    public static SpecNotFoundException userSpecNotFound(Long userId) {
        return new SpecNotFoundException("사용자 스펙 정보를 찾을 수 없습니다. 사용자 ID: " + userId);
    }
    
    public static SpecNotFoundException careerNotFound(Long careerId) {
        return new SpecNotFoundException("경력 정보를 찾을 수 없습니다. ID: " + careerId);
    }
    
    public static SpecNotFoundException educationNotFound(Long educationId) {
        return new SpecNotFoundException("학력 정보를 찾을 수 없습니다. ID: " + educationId);
    }
    
    public static SpecNotFoundException skillNotFound(Long skillId) {
        return new SpecNotFoundException("스킬 정보를 찾을 수 없습니다. ID: " + skillId);
    }
}

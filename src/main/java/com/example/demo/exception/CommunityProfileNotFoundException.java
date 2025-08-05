package com.example.demo.exception;

/**
 * 커뮤니티 프로필을 찾을 수 없을 때 발생하는 예외
 */
public class CommunityProfileNotFoundException extends CustomException {

    public CommunityProfileNotFoundException(Long userId) {
        super(ErrorCode.COMMUNITY_PROFILE_NOT_FOUND);
    }

    public CommunityProfileNotFoundException(String message) {
        super(ErrorCode.COMMUNITY_PROFILE_NOT_FOUND);
    }

    public CommunityProfileNotFoundException() {
        super(ErrorCode.COMMUNITY_PROFILE_NOT_FOUND);
    }
}

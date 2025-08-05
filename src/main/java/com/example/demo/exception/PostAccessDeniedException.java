package com.example.demo.exception;

/**
 * 게시글 접근 권한이 없을 때 발생하는 예외
 */
public class PostAccessDeniedException extends CustomException {
    
    public PostAccessDeniedException(Long postId) {
        super(ErrorCode.POST_ACCESS_DENIED);
    }
    
    public PostAccessDeniedException() {
        super(ErrorCode.POST_ACCESS_DENIED);
    }
}

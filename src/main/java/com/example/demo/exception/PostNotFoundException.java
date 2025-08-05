package com.example.demo.exception;

/**
 * 게시글을 찾을 수 없을 때 발생하는 예외
 */
public class PostNotFoundException extends CustomException {
    
    public PostNotFoundException(Long postId) {
        super(ErrorCode.POST_NOT_FOUND);
    }
    
    public PostNotFoundException() {
        super(ErrorCode.POST_NOT_FOUND);
    }
}

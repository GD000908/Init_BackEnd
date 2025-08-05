package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CommentDto;
import com.example.demo.dto.CreateCommentRequest;
import com.example.demo.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 생성
     * POST /api/posts/{postId}/comments
     * 
     * 프론트엔드 요청 형식: { userId: number, content: string }
     * 기존 요청 형식: ?authorId=1 + { content: string }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentDto>> createComment(
            @PathVariable Long postId,
            @RequestParam(required = false) Long authorId,
            @RequestBody CreateCommentRequest request) {
        
        Long userId = request.getUserId() != null ? request.getUserId() : authorId;
        
        log.info("🎯 댓글 생성 요청: postId={}, userId={}, content='{}'", 
                postId, userId, request.getContent());
        
        if (userId == null) {
            log.error("❌ 사용자 ID가 누락됨: postId={}", postId);
            return ResponseEntity.badRequest().body(ApiResponse.<CommentDto>builder()
                .success(false)
                .message("사용자 ID가 필요합니다")
                .build());
        }
        
        try {
            CommentDto comment = commentService.createComment(postId, userId, request);
            
            log.info("✅ 댓글 생성 성공: commentId={}", comment.getId());
            
            return ResponseEntity.ok(ApiResponse.<CommentDto>builder()
                .success(true)
                .data(comment)
                .message("댓글이 작성되었습니다")
                .build());
        } catch (Exception e) {
            log.error("❌ 댓글 생성 실패: postId={}, userId={}", postId, userId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<CommentDto>builder()
                .success(false)
                .message("댓글 작성 실패: " + e.getMessage())
                .build());
        }
    }

    /**
     * 게시글의 댓글 목록 조회
     * GET /api/posts/{postId}/comments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CommentDto>>> getCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(required = false) Long currentUserId,
            Pageable pageable) {
        
        log.info("🔍 게시글 댓글 조회 요청: postId={}", postId);
        
        try {
            Page<CommentDto> comments = commentService.getCommentsByPost(postId, currentUserId, pageable);
            
            log.info("✅ 댓글 조회 성공: postId={}, count={}", postId, comments.getTotalElements());
            
            return ResponseEntity.ok(ApiResponse.<Page<CommentDto>>builder()
                .success(true)
                .data(comments)
                .build());
        } catch (Exception e) {
            log.error("❌ 게시글 댓글 조회 실패: postId={}", postId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Page<CommentDto>>builder()
                .success(false)
                .message("댓글 조회 실패: " + e.getMessage())
                .build());
        }
    }

    /**
     * 댓글 삭제
     * DELETE /api/posts/{postId}/comments/{commentId}
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam Long authorId) {
        
        log.info("🗑️ 댓글 삭제 요청: postId={}, commentId={}, authorId={}", postId, commentId, authorId);
        
        try {
            commentService.deleteComment(commentId, authorId);
            
            log.info("✅ 댓글 삭제 성공: commentId={}", commentId);
            
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("댓글이 삭제되었습니다")
                .build());
        } catch (Exception e) {
            log.error("❌ 댓글 삭제 실패: commentId={}, authorId={}", commentId, authorId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                .success(false)
                .message("댓글 삭제 실패: " + e.getMessage())
                .build());
        }
    }
}

/**
 * 🔥 프론트엔드 호환 댓글 API
 * /api/comments (프론트엔드에서 사용하는 경로)
 */
@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
class FrontendCommentController {

    private final CommentService commentService;

    /**
     * 댓글 생성 - 프론트엔드 호환
     * POST /api/comments
     * Request body: { postId: number, authorId: number, content: string }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentDto>> createCommentFrontend(
            @RequestBody Map<String, Object> requestBody) {
        
        Long postId = Long.valueOf(requestBody.get("postId").toString());
        Long authorId = Long.valueOf(requestBody.get("authorId").toString());
        String content = requestBody.get("content").toString();
        
        log.info("🎯 [프론트엔드] 댓글 생성 요청: postId={}, authorId={}, content='{}'", 
                postId, authorId, content);
        
        try {
            CreateCommentRequest request = new CreateCommentRequest();
            request.setUserId(authorId);
            request.setContent(content);
            
            CommentDto comment = commentService.createComment(postId, authorId, request);
            
            log.info("✅ [프론트엔드] 댓글 생성 성공: commentId={}", comment.getId());
            
            return ResponseEntity.ok(ApiResponse.<CommentDto>builder()
                .success(true)
                .data(comment)
                .message("댓글이 작성되었습니다")
                .build());
        } catch (Exception e) {
            log.error("❌ [프론트엔드] 댓글 생성 실패: postId={}, authorId={}", postId, authorId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<CommentDto>builder()
                .success(false)
                .message("댓글 작성 실패: " + e.getMessage())
                .build());
        }
    }

    /**
     * 댓글 조회 - 프론트엔드 호환
     * GET /api/comments?postId={postId}
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CommentDto>>> getCommentsFrontend(
            @RequestParam Long postId,
            @RequestParam(required = false) Long currentUserId,
            Pageable pageable) {
        
        log.info("🔍 [프론트엔드] 게시글 댓글 조회 요청: postId={}", postId);
        
        try {
            Page<CommentDto> comments = commentService.getCommentsByPost(postId, currentUserId, pageable);
            
            log.info("✅ [프론트엔드] 댓글 조회 성공: postId={}, count={}", postId, comments.getTotalElements());
            
            return ResponseEntity.ok(ApiResponse.<Page<CommentDto>>builder()
                .success(true)
                .data(comments)
                .build());
        } catch (Exception e) {
            log.error("❌ [프론트엔드] 게시글 댓글 조회 실패: postId={}", postId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Page<CommentDto>>builder()
                .success(false)
                .message("댓글 조회 실패: " + e.getMessage())
                .build());
        }
    }
}

/**
 * 사용자의 댓글 관련 API
 * /api/users/{userId}/comments
 */
@Slf4j
@RestController
@RequestMapping("/api/users/{userId}/comments")
@RequiredArgsConstructor
class UserCommentController {

    private final CommentService commentService;

    /**
     * 사용자가 작성한 댓글 목록 조회
     * GET /api/users/{userId}/comments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CommentDto>>> getCommentsByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) Long currentUserId,
            Pageable pageable) {
        
        log.info("🔍 사용자 댓글 조회 요청: userId={}", userId);
        
        try {
            Page<CommentDto> comments = commentService.getCommentsByUser(userId, currentUserId, pageable);
            
            log.info("✅ 사용자 댓글 조회 성공: userId={}, count={}", userId, comments.getTotalElements());
            
            return ResponseEntity.ok(ApiResponse.<Page<CommentDto>>builder()
                .success(true)
                .data(comments)
                .build());
        } catch (Exception e) {
            log.error("❌ 사용자 댓글 조회 실패: userId={}", userId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Page<CommentDto>>builder()
                .success(false)
                .message("사용자 댓글 조회 실패: " + e.getMessage())
                .build());
        }
    }
}

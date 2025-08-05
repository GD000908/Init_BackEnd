package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.PostDto;
import com.example.demo.service.PostService;
import com.example.demo.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;

    /**
     * 게시글 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostDto>> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @RequestParam Long authorId) {

        log.info("=== 게시글 생성 요청 디버깅 시작 ===");
        log.info("authorId: {}", authorId);
        log.info("request 객체: {}", request);

        // request 필드별 상세 로그
        if (request != null) {
            log.info("request.content: '{}'", request.getContent());
            log.info("request.imageUrl: '{}'", request.getImageUrl());
            log.info("request.jobCategory: '{}'", request.getJobCategory());
            log.info("request.topicCategory: '{}'", request.getTopicCategory());
            log.info("request.status: '{}'", request.getStatus());
            log.info("request.hashtags: {}", request.getHashtags());
        } else {
            log.error("❌ request 객체가 null입니다!");
        }

        try {
            log.info("🔄 PostService.createPost 호출 시작");
            PostDto post = postService.createPost(authorId, request);
            log.info("✅ PostService.createPost 호출 성공");

            return ResponseEntity.ok(ApiResponse.<PostDto>builder()
                    .success(true)
                    .data(post)
                    .message("게시글이 생성되었습니다")
                    .build());
        } catch (Exception e) {
            log.error("❌ 게시글 생성 실패: authorId={}", authorId);
            log.error("❌ 예외 타입: {}", e.getClass().getSimpleName());
            log.error("❌ 에러 메시지: '{}'", e.getMessage());
            log.error("❌ 스택 트레이스:", e);

            return ResponseEntity.badRequest().body(ApiResponse.<PostDto>builder()
                    .success(false)
                    .message("게시글 생성 실패: " + e.getMessage())
                    .build());
        }
    }

    /**
     * 전체 게시글 조회 (피드)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostDto>>> getAllPosts(
            @RequestParam(required = false) Long currentUserId,
            Pageable pageable) {
        
        log.info("🔍 [FEED] 전체 게시글 조회 요청: currentUserId={}", currentUserId);
        
        Page<PostDto> posts = postService.getAllPosts(currentUserId, pageable);
        
        log.info("✅ [FEED] 게시글 조회 완료: 총 {}개, currentUserId={}", 
                posts.getTotalElements(), currentUserId);
        
        return ResponseEntity.ok(ApiResponse.<Page<PostDto>>builder()
            .success(true)
            .data(posts)
            .build());
    }

    /**
     * 팔로잉 사용자들의 게시글 조회
     */
    @GetMapping("/following/{userId}")
    public ResponseEntity<ApiResponse<Page<PostDto>>> getFollowingPosts(
            @PathVariable Long userId,
            Pageable pageable) {
        
        log.info("팔로잉 게시글 조회 요청: userId={}", userId);
        
        Page<PostDto> posts = postService.getFollowingPosts(userId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<PostDto>>builder()
            .success(true)
            .data(posts)
            .build());
    }

    /**
     * 카테고리별 게시글 조회
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<PostDto>>> getPostsByCategory(
            @PathVariable String category,
            @RequestParam(required = false) Long currentUserId,
            Pageable pageable) {
        
        log.info("카테고리별 게시글 조회 요청: category={}", category);
        
        Page<PostDto> posts = postService.getPostsByCategory(category, currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<PostDto>>builder()
            .success(true)
            .data(posts)
            .build());
    }

    /**
     * 게시글 검색
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PostDto>>> searchPosts(
            @RequestParam String q,
            @RequestParam(required = false) Long currentUserId,
            Pageable pageable) {
        
        log.info("게시글 검색 요청: keyword={}", q);
        
        Page<PostDto> posts = postService.searchPosts(q, currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<PostDto>>builder()
            .success(true)
            .data(posts)
            .build());
    }

    /**
     * 특정 사용자의 게시글 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<PostDto>>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long currentUserId,
            Pageable pageable) {
        
        log.info("사용자 게시글 조회 요청: userId={}, status={}", userId, status);
        
        Page<PostDto> posts = postService.getUserPosts(userId, status, currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<PostDto>>builder()
            .success(true)
            .data(posts)
            .build());
    }

    /**
     * 특정 사용자의 발행된 게시글만 조회
     */
    @GetMapping("/user/{userId}/published")
    public ResponseEntity<ApiResponse<Page<PostDto>>> getUserPublishedPosts(
            @PathVariable Long userId,
            @RequestParam(required = false) Long currentUserId,
            Pageable pageable) {
        
        log.info("사용자 발행 게시글 조회 요청: userId={}", userId);
        
        Page<PostDto> posts = postService.getUserPosts(userId, "PUBLISHED", currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<PostDto>>builder()
            .success(true)
            .data(posts)
            .build());
    }

    /**
     * 특정 사용자의 임시저장 게시글만 조회
     */
    @GetMapping("/user/{userId}/drafts")
    public ResponseEntity<ApiResponse<Page<PostDto>>> getUserDraftPosts(
            @PathVariable Long userId,
            @RequestParam(required = false) Long currentUserId,
            Pageable pageable) {
        
        log.info("사용자 임시저장 게시글 조회 요청: userId={}", userId);
        
        Page<PostDto> posts = postService.getUserPosts(userId, "DRAFT", currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<PostDto>>builder()
            .success(true)
            .data(posts)
            .build());
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPostById(
            @PathVariable Long postId,
            @RequestParam(required = false) Long currentUserId) {
        
        log.info("게시글 상세 조회 요청: postId={}", postId);
        
        Optional<PostDto> post = postService.getPostById(postId, currentUserId);
        
        if (post.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(post.get());
    }

    /**
     * 게시글 업데이트
     */
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDto>> updatePost(
            @PathVariable Long postId,
            @RequestParam Long authorId,
            @Valid @RequestBody CreatePostRequest request) {
        
        log.info("게시글 업데이트 요청: postId={}, authorId={}", postId, authorId);
        
        try {
            PostDto post = postService.updatePost(postId, authorId, request);
            return ResponseEntity.ok(ApiResponse.<PostDto>builder()
                .success(true)
                .data(post)
                .message("게시글이 수정되었습니다")
                .build());
        } catch (Exception e) {
            log.error("게시글 업데이트 실패: postId={}, authorId={}", postId, authorId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<PostDto>builder()
                .success(false)
                .message(e.getMessage())
                .build());
        }
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @RequestParam Long authorId) {
        
        log.info("게시글 삭제 요청: postId={}, authorId={}", postId, authorId);
        
        try {
            postService.deletePost(postId, authorId);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("게시글이 삭제되었습니다")
                .build());
        } catch (Exception e) {
            log.error("게시글 삭제 실패: postId={}, authorId={}", postId, authorId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                .success(false)
                .message(e.getMessage())
                .build());
        }
    }

    /**
     * 게시글 좋아요 토글 (프론트엔드 API 호환)
     * POST /api/posts/{postId}/like
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleLike(
            @PathVariable Long postId,
            @RequestBody Map<String, Long> requestBody) {
        
        Long userId = requestBody.get("userId");
        log.info("🎯 [LIKE_TOGGLE] 요청 시작: postId={}, userId={}", postId, userId);
        
        // 입력값 검증
        if (userId == null) {
            log.error("❌ [LIKE_TOGGLE] userId가 null입니다");
            return ResponseEntity.badRequest().body(ApiResponse.<Map<String, Object>>builder()
                .success(false)
                .message("사용자 ID가 필요합니다")
                .build());
        }
        
        try {
            // 토글 전 상태 로깅
            boolean beforeState = postLikeService.isLikedByUser(postId, userId);
            long beforeCount = postLikeService.getLikesCount(postId);
            log.info("🔍 [LIKE_TOGGLE] 토글 전: postId={}, beforeLiked={}, beforeCount={}", 
                    postId, beforeState, beforeCount);
            
            // 좋아요 토글 실행
            boolean isLiked = postLikeService.toggleLike(postId, userId);
            long likesCount = postLikeService.getLikesCount(postId);
            
            // 토글 후 상태 재확인 (검증용)
            boolean afterStateCheck = postLikeService.isLikedByUser(postId, userId);
            
            // 토글 후 상태 로깅
            log.info("🔍 [LIKE_TOGGLE] 토글 후: postId={}, afterLiked={}, afterCount={}, stateCheck={}", 
                    postId, isLiked, likesCount, afterStateCheck);
            
            // 상태 변화 검증
            if (beforeState == isLiked) {
                log.warn("⚠️ [LIKE_TOGGLE] 상태가 변경되지 않았습니다! before={}, after={}", 
                        beforeState, isLiked);
            }
            
            // 이중 검증 - 반환값과 재조회 결과가 일치하는지 확인
            if (isLiked != afterStateCheck) {
                log.error("🚨 [LIKE_TOGGLE] 심각한 불일치! toggle결과={}, 재조회결과={}", 
                        isLiked, afterStateCheck);
                // 재조회 결과를 우선시
                isLiked = afterStateCheck;
            }
            
            Map<String, Object> responseData = Map.of(
                "isLiked", isLiked,
                "likesCount", likesCount
            );
            
            log.info("✅ [LIKE_TOGGLE] 응답 데이터: {}", responseData);
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .data(responseData)
                .message(isLiked ? "좋아요를 눌렀습니다" : "좋아요를 취소했습니다")
                .build());
                
        } catch (Exception e) {
            log.error("❌ [LIKE_TOGGLE] 실패: postId={}, userId={}", postId, userId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Map<String, Object>>builder()
                .success(false)
                .message("좋아요 처리 실패: " + e.getMessage())
                .build());
        }
    }

    /**
     * 🔥 프론트엔드 호환: 북마크한 게시글 조회
     * GET /api/posts/bookmarked/{userId}
     */
    @GetMapping("/bookmarked/{userId}")
    public ResponseEntity<ApiResponse<Page<PostDto>>> getBookmarkedPosts(
            @PathVariable Long userId,
            Pageable pageable) {
        
        log.info("🔖 [프론트엔드] 북마크한 게시글 조회 요청: userId={}", userId);
        
        try {
            Page<PostDto> posts = postService.getBookmarkedPosts(userId, pageable);
            
            log.info("✅ [프론트엔드] 북마크한 게시글 조회 성공: userId={}, count={}", 
                    userId, posts.getTotalElements());
            
            return ResponseEntity.ok(ApiResponse.<Page<PostDto>>builder()
                .success(true)
                .data(posts)
                .build());
        } catch (Exception e) {
            log.error("❌ [프론트엔드] 북마크한 게시글 조회 실패: userId={}", userId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Page<PostDto>>builder()
                .success(false)
                .message("북마크한 게시글 조회 실패: " + e.getMessage())
                .build());
        }
    }

    /**
     * 게시글 좋아요 상태 확인
     * GET /api/posts/{postId}/like/status
     */
    @GetMapping("/{postId}/like/status")
    public ResponseEntity<ApiResponse<Boolean>> getLikeStatus(
            @PathVariable Long postId,
            @RequestParam Long userId) {
        
        log.info("🔍 게시글 좋아요 상태 확인: postId={}, userId={}", postId, userId);
        
        try {
            boolean isLiked = postLikeService.isLikedByUser(postId, userId);
            return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(true)
                .data(isLiked)
                .build());
        } catch (Exception e) {
            log.error("❌ 좋아요 상태 확인 실패: postId={}, userId={}", postId, userId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Boolean>builder()
                .success(false)
                .message("좋아요 상태 확인 실패: " + e.getMessage())
                .build());
        }
    }
}

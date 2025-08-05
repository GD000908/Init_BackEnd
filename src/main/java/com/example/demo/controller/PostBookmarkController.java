package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PostDto;
import com.example.demo.service.PostBookmarkService;
import com.example.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class PostBookmarkController {

    private final PostBookmarkService postBookmarkService;
    private final PostService postService;

    // =================== RESTful 방식 북마크 API ===================

    /**
     * 북마크 토글 (추가/제거) - RESTful 방식
     * POST /api/posts/{postId}/bookmarks
     */
    @PostMapping("/api/posts/{postId}/bookmarks")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleBookmark(
            @PathVariable Long postId,
            @RequestBody Map<String, Long> requestBody) {

        Long userId = requestBody.get("userId");
        log.info("🔖 [RESTful] 북마크 토글 요청: postId={}, userId={}", postId, userId);

        if (userId == null) {
            log.error("❌ [RESTful] userId가 null입니다");
            return ResponseEntity.badRequest().body(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("사용자 ID가 필요합니다")
                    .build());
        }

        try {
            boolean beforeState = postBookmarkService.isBookmarked(postId, userId);
            log.info("🔍 [RESTful] 토글 전: postId={}, beforeBookmarked={}", postId, beforeState);

            boolean isBookmarked = postBookmarkService.toggleBookmark(postId, userId);
            boolean afterStateCheck = postBookmarkService.isBookmarked(postId, userId);

            log.info("🔍 [RESTful] 토글 후: postId={}, afterBookmarked={}, stateCheck={}", 
                    postId, isBookmarked, afterStateCheck);

            if (beforeState == isBookmarked) {
                log.warn("⚠️ [RESTful] 상태가 변경되지 않았습니다! before={}, after={}", 
                        beforeState, isBookmarked);
            }

            if (isBookmarked != afterStateCheck) {
                log.error("🚨 [RESTful] 심각한 불일치! toggle결과={}, 재조회결과={}", 
                        isBookmarked, afterStateCheck);
                isBookmarked = afterStateCheck;
            }

            Map<String, Object> responseData = Map.of("isBookmarked", isBookmarked);
            log.info("✅ [RESTful] 응답 데이터: {}", responseData);

            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .data(responseData)
                    .message(isBookmarked ? "북마크에 저장되었습니다" : "북마크에서 제거되었습니다")
                    .build());

        } catch (Exception e) {
            log.error("❌ [RESTful] 북마크 토글 실패: postId={}, userId={}", postId, userId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("북마크 처리 실패: " + e.getMessage())
                    .build());
        }
    }

    /**
     * 북마크 상태 확인 - RESTful 방식
     * GET /api/posts/{postId}/bookmarks/status
     */
    @GetMapping("/api/posts/{postId}/bookmarks/status")
    public ResponseEntity<ApiResponse<Boolean>> getBookmarkStatus(
            @PathVariable Long postId,
            @RequestParam Long userId) {
        try {
            log.info("🔍 [RESTful] 북마크 상태 확인 요청: postId={}, userId={}", postId, userId);

            boolean isBookmarked = postBookmarkService.isBookmarked(postId, userId);

            log.info("✅ [RESTful] 북마크 상태 확인 완료: postId={}, userId={}, isBookmarked={}",
                    postId, userId, isBookmarked);

            return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                    .success(true)
                    .data(isBookmarked)
                    .message("북마크 상태 조회 성공")
                    .build());
        } catch (Exception e) {
            log.error("❌ [RESTful] 북마크 상태 확인 실패: postId={}, userId={}", postId, userId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Boolean>builder()
                    .success(false)
                    .message("북마크 상태 확인 실패: " + e.getMessage())
                    .build());
        }
    }

    /**
     * 사용자의 북마크 목록 조회 - RESTful 방식
     * GET /api/users/{userId}/bookmarks
     */
    @GetMapping("/api/users/{userId}/bookmarks")
    public ResponseEntity<ApiResponse<Page<PostDto>>> getUserBookmarks(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            log.info("🔍 [RESTful] 사용자 북마크 목록 조회 요청: userId={}", userId);

            Pageable pageable = PageRequest.of(page, size);
            Page<PostDto> bookmarkedPosts = postService.getBookmarkedPosts(userId, pageable);

            log.info("✅ [RESTful] 사용자 북마크 목록 조회 성공: userId={}, count={}",
                    userId, bookmarkedPosts.getTotalElements());

            return ResponseEntity.ok(ApiResponse.<Page<PostDto>>builder()
                    .success(true)
                    .data(bookmarkedPosts)
                    .build());

        } catch (Exception e) {
            log.error("❌ [RESTful] 사용자 북마크 목록 조회 실패: userId={}", userId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Page<PostDto>>builder()
                    .success(false)
                    .message("북마크 목록 조회 실패: " + e.getMessage())
                    .build());
        }
    }

    // =================== 프론트엔드 호환 API ===================

    /**
     * 북마크 토글 - 프론트엔드 호환
     * POST /api/bookmarks/toggle
     */
    @PostMapping("/api/bookmarks/toggle")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleBookmarkFrontend(
            @RequestBody Map<String, Long> requestBody) {

        Long postId = requestBody.get("postId");
        Long userId = requestBody.get("userId");

        log.info("🔖 [프론트엔드] 북마크 토글 요청: postId={}, userId={}", postId, userId);

        if (postId == null || userId == null) {
            log.error("❌ [프론트엔드] postId 또는 userId가 null입니다");
            return ResponseEntity.badRequest().body(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("게시글 ID와 사용자 ID가 필요합니다")
                    .build());
        }

        try {
            boolean beforeState = postBookmarkService.isBookmarked(postId, userId);
            log.info("🔍 [프론트엔드] 토글 전: postId={}, beforeBookmarked={}", postId, beforeState);

            boolean isBookmarked = postBookmarkService.toggleBookmark(postId, userId);
            boolean afterStateCheck = postBookmarkService.isBookmarked(postId, userId);

            log.info("🔍 [프론트엔드] 토글 후: postId={}, afterBookmarked={}, stateCheck={}", 
                    postId, isBookmarked, afterStateCheck);

            if (beforeState == isBookmarked) {
                log.warn("⚠️ [프론트엔드] 상태가 변경되지 않았습니다! before={}, after={}", 
                        beforeState, isBookmarked);
            }

            if (isBookmarked != afterStateCheck) {
                log.error("🚨 [프론트엔드] 심각한 불일치! toggle결과={}, 재조회결과={}", 
                        isBookmarked, afterStateCheck);
                isBookmarked = afterStateCheck;
            }

            Map<String, Object> responseData = Map.of("isBookmarked", isBookmarked);
            log.info("✅ [프론트엔드] 응답 데이터: {}", responseData);

            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .data(responseData)
                    .message(isBookmarked ? "북마크에 저장되었습니다" : "북마크에서 제거되었습니다")
                    .build());

        } catch (Exception e) {
            log.error("❌ [프론트엔드] 북마크 토글 실패: postId={}, userId={}", postId, userId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("북마크 처리 실패: " + e.getMessage())
                    .build());
        }
    }

    /**
     * 북마크 상태 확인 - 프론트엔드 호환
     * GET /api/bookmarks/status?postId={postId}&userId={userId}
     */
    @GetMapping("/api/bookmarks/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBookmarkStatusFrontend(
            @RequestParam Long postId,
            @RequestParam Long userId) {

        log.info("🔍 [프론트엔드] 북마크 상태 확인 요청: postId={}, userId={}", postId, userId);

        try {
            boolean isBookmarked = postBookmarkService.isBookmarked(postId, userId);

            Map<String, Object> responseData = Map.of("isBookmarked", isBookmarked);

            log.info("✅ [프론트엔드] 북마크 상태 확인 완료: {}", responseData);

            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .data(responseData)
                    .build());

        } catch (Exception e) {
            log.error("❌ [프론트엔드] 북마크 상태 확인 실패: postId={}, userId={}", postId, userId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("북마크 상태 확인 실패: " + e.getMessage())
                    .build());
        }
    }
}

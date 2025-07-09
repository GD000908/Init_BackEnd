package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.JobBookmarkDto;
import com.example.demo.dto.CreateJobBookmarkRequest;
import com.example.demo.service.JobBookmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/job-calendar/bookmarks")
@RequiredArgsConstructor
@Slf4j
public class JobBookmarkController {

    private final JobBookmarkService jobBookmarkService;
    private final AuthHelper authHelper;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<JobBookmarkDto>>> getBookmarksByUserId(
            @PathVariable Long userId,
            @RequestHeader(value = "x-user-id", required = false) String requestUserIdHeader) {
        try {
            Long requestUserId = parseUserIdFromHeader(requestUserIdHeader);
            authHelper.validateUser(requestUserId);
            authHelper.validateUserAccess(requestUserId, userId);

            List<JobBookmarkDto> bookmarks = jobBookmarkService.getBookmarksByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success("북마크 목록 조회 성공", bookmarks));
        } catch (Exception e) {
            log.error("북마크 목록 조회 실패: 사용자 ID {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("북마크 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/ending-soon")
    public ResponseEntity<ApiResponse<List<JobBookmarkDto>>> getBookmarksEndingSoon(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "7") int days,
            @RequestHeader(value = "x-user-id", required = false) String requestUserIdHeader) {
        try {
            Long requestUserId = parseUserIdFromHeader(requestUserIdHeader);
            authHelper.validateUser(requestUserId);
            authHelper.validateUserAccess(requestUserId, userId);

            List<JobBookmarkDto> bookmarks = jobBookmarkService.getBookmarksEndingSoon(userId, days);
            return ResponseEntity.ok(ApiResponse.success("마감 임박 북마크 조회 성공", bookmarks));
        } catch (Exception e) {
            log.error("마감 임박 북마크 조회 실패: 사용자 ID {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("마감 임박 북마크 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<ApiResponse<List<JobBookmarkDto>>> getActiveJobBookmarks(
            @PathVariable Long userId,
            @RequestHeader(value = "x-user-id", required = false) String requestUserIdHeader) {
        try {
            Long requestUserId = parseUserIdFromHeader(requestUserIdHeader);
            authHelper.validateUser(requestUserId);
            authHelper.validateUserAccess(requestUserId, userId);

            List<JobBookmarkDto> bookmarks = jobBookmarkService.getActiveJobBookmarks(userId);
            return ResponseEntity.ok(ApiResponse.success("진행중인 북마크 조회 성공", bookmarks));
        } catch (Exception e) {
            log.error("진행중인 북마크 조회 실패: 사용자 ID {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("진행중인 북마크 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/expired")
    public ResponseEntity<ApiResponse<List<JobBookmarkDto>>> getExpiredJobBookmarks(
            @PathVariable Long userId,
            @RequestHeader(value = "x-user-id", required = false) String requestUserIdHeader) {
        try {
            Long requestUserId = parseUserIdFromHeader(requestUserIdHeader);
            authHelper.validateUser(requestUserId);
            authHelper.validateUserAccess(requestUserId, userId);

            List<JobBookmarkDto> bookmarks = jobBookmarkService.getExpiredJobBookmarks(userId);
            return ResponseEntity.ok(ApiResponse.success("마감된 북마크 조회 성공", bookmarks));
        } catch (Exception e) {
            log.error("마감된 북마크 조회 실패: 사용자 ID {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("마감된 북마크 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<ApiResponse<Long>> getBookmarkCount(
            @PathVariable Long userId,
            @RequestHeader(value = "x-user-id", required = false) String requestUserIdHeader) {
        try {
            Long requestUserId = parseUserIdFromHeader(requestUserIdHeader);
            authHelper.validateUser(requestUserId);
            authHelper.validateUserAccess(requestUserId, userId);

            Long count = jobBookmarkService.getBookmarkCount(userId);
            return ResponseEntity.ok(ApiResponse.success("북마크 수 조회 성공", count));
        } catch (Exception e) {
            log.error("북마크 수 조회 실패: 사용자 ID {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("북마크 수 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> isBookmarked(
            @RequestParam Long userId,
            @RequestParam Long jobPostingId,
            @RequestHeader(value = "x-user-id", required = false) String requestUserIdHeader) {
        try {
            Long requestUserId = parseUserIdFromHeader(requestUserIdHeader);
            authHelper.validateUser(requestUserId);
            authHelper.validateUserAccess(requestUserId, userId);

            boolean isBookmarked = jobBookmarkService.isBookmarked(userId, jobPostingId);
            return ResponseEntity.ok(ApiResponse.success("북마크 상태 확인 성공", isBookmarked));
        } catch (Exception e) {
            log.error("북마크 상태 확인 실패: 사용자 ID {}, 공고 ID {}", userId, jobPostingId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("북마크 상태 확인에 실패했습니다: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobBookmarkDto>> createBookmark(
            @Valid @RequestBody CreateJobBookmarkRequest request,
            @RequestHeader(value = "x-user-id", required = false) String requestUserIdHeader) {
        try {
            Long requestUserId = parseUserIdFromHeader(requestUserIdHeader);
            authHelper.validateUser(requestUserId);
            authHelper.validateUserAccess(requestUserId, request.getUserId());

            JobBookmarkDto bookmark = jobBookmarkService.createBookmark(request);
            return ResponseEntity.ok(ApiResponse.success("북마크 생성 성공", bookmark));
        } catch (Exception e) {
            log.error("북마크 생성 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("북마크 생성에 실패했습니다: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/memo")
    public ResponseEntity<ApiResponse<JobBookmarkDto>> updateBookmarkMemo(
            @PathVariable Long id,
            @RequestParam String memo,
            @RequestHeader(value = "x-user-id", required = false) String requestUserIdHeader) {
        try {
            Long requestUserId = parseUserIdFromHeader(requestUserIdHeader);
            authHelper.validateUser(requestUserId);

            // 북마크 소유자 확인을 위해 서비스에서 검증하도록 위임
            JobBookmarkDto bookmark = jobBookmarkService.updateBookmarkMemo(id, memo, requestUserId);
            return ResponseEntity.ok(ApiResponse.success("북마크 메모 수정 성공", bookmark));
        } catch (Exception e) {
            log.error("북마크 메모 수정 실패: ID {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("북마크 메모 수정에 실패했습니다: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBookmark(
            @PathVariable Long id,
            @RequestHeader(value = "x-user-id", required = false) String requestUserIdHeader) {
        try {
            Long requestUserId = parseUserIdFromHeader(requestUserIdHeader);
            authHelper.validateUser(requestUserId);

            // 북마크 소유자 확인을 위해 서비스에서 검증하도록 위임
            jobBookmarkService.deleteBookmark(id, requestUserId);
            return ResponseEntity.ok(ApiResponse.success("북마크 삭제 성공", null));
        } catch (Exception e) {
            log.error("북마크 삭제 실패: ID {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("북마크 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    @DeleteMapping("/user/{userId}/job/{jobPostingId}")
    public ResponseEntity<ApiResponse<Void>> deleteBookmarkByUserAndJob(
            @PathVariable Long userId,
            @PathVariable Long jobPostingId,
            @RequestHeader(value = "x-user-id", required = false) String requestUserIdHeader) {
        try {
            Long requestUserId = parseUserIdFromHeader(requestUserIdHeader);
            authHelper.validateUser(requestUserId);
            authHelper.validateUserAccess(requestUserId, userId);

            jobBookmarkService.deleteBookmarkByUserAndJob(userId, jobPostingId);
            return ResponseEntity.ok(ApiResponse.success("북마크 삭제 성공", null));
        } catch (Exception e) {
            log.error("북마크 삭제 실패: 사용자 ID {}, 공고 ID {}", userId, jobPostingId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("북마크 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 헤더에서 사용자 ID를 파싱합니다.
     */
    private Long parseUserIdFromHeader(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            throw new RuntimeException("인증 정보가 없습니다. 로그인이 필요합니다.");
        }

        try {
            return Long.parseLong(userIdHeader.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("잘못된 사용자 ID 형식입니다.");
        }
    }
}
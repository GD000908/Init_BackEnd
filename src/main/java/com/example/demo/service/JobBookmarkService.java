package com.example.demo.service;

import com.example.demo.dto.JobBookmarkDto;
import com.example.demo.dto.CreateJobBookmarkRequest;
import com.example.demo.entity.JobBookmark;
import com.example.demo.entity.JobPosting;
import com.example.demo.entity.User;
import com.example.demo.repository.JobBookmarkRepository;
import com.example.demo.repository.JobPostingRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class JobBookmarkService {

    private final JobBookmarkRepository jobBookmarkRepository;
    private final UserRepository userRepository;
    private final JobPostingRepository jobPostingRepository;

    /**
     * 사용자별 북마크 목록 조회
     * @param userId 사용자 ID
     * @return 북마크 목록
     */
    public List<JobBookmarkDto> getBookmarksByUserId(Long userId) {
        validateUser(userId);
        return jobBookmarkRepository.findActiveBookmarksByUserId(userId).stream()
                .map(JobBookmarkDto::fromSimple)
                .collect(Collectors.toList());
    }

    /**
     * 마감 임박 북마크 조회
     * @param userId 사용자 ID
     * @param days 마감까지 남은 일수
     * @return 마감 임박 북마크 목록
     */
    public List<JobBookmarkDto> getBookmarksEndingSoon(Long userId, int days) {
        validateUser(userId);
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(days);

        return jobBookmarkRepository.findBookmarksEndingSoon(userId, today, deadline).stream()
                .map(JobBookmarkDto::fromSimple)
                .collect(Collectors.toList());
    }

    /**
     * 진행중인 북마크 조회
     * @param userId 사용자 ID
     * @return 진행중인 북마크 목록
     */
    public List<JobBookmarkDto> getActiveJobBookmarks(Long userId) {
        validateUser(userId);
        return jobBookmarkRepository.findActiveJobBookmarks(userId, LocalDate.now()).stream()
                .map(JobBookmarkDto::fromSimple)
                .collect(Collectors.toList());
    }

    /**
     * 마감된 북마크 조회
     * @param userId 사용자 ID
     * @return 마감된 북마크 목록
     */
    public List<JobBookmarkDto> getExpiredJobBookmarks(Long userId) {
        validateUser(userId);
        return jobBookmarkRepository.findExpiredJobBookmarks(userId, LocalDate.now()).stream()
                .map(JobBookmarkDto::fromSimple)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 북마크 수 조회
     * @param userId 사용자 ID
     * @return 북마크 수
     */
    public Long getBookmarkCount(Long userId) {
        validateUser(userId);
        return jobBookmarkRepository.countActiveBookmarksByUserId(userId);
    }

    /**
     * 북마크 생성
     * @param request 북마크 생성 요청
     * @return 생성된 북마크
     */
    @Transactional
    public JobBookmarkDto createBookmark(CreateJobBookmarkRequest request) {
        // 중복 체크
        if (jobBookmarkRepository.existsByUserIdAndJobPostingIdAndStatus(
                request.getUserId(), request.getJobPostingId(), JobBookmark.BookmarkStatus.ACTIVE)) {
            throw new RuntimeException("이미 북마크된 공고입니다.");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + request.getUserId()));

        if (!user.getIsActive()) {
            throw new RuntimeException("비활성화된 사용자입니다.");
        }

        JobPosting jobPosting = jobPostingRepository.findById(request.getJobPostingId())
                .orElseThrow(() -> new RuntimeException("채용공고를 찾을 수 없습니다. ID: " + request.getJobPostingId()));

        JobBookmark bookmark = JobBookmark.builder()
                .user(user)
                .jobPosting(jobPosting)
                .memo(request.getMemo())
                .status(JobBookmark.BookmarkStatus.ACTIVE)
                .build();

        JobBookmark savedBookmark = jobBookmarkRepository.save(bookmark);
        log.info("새 북마크 생성: 사용자 ID {} - 공고 ID {}", user.getId(), jobPosting.getId());

        return JobBookmarkDto.fromSimple(savedBookmark);
    }

    /**
     * 북마크 메모 수정 (권한 확인 포함)
     * @param bookmarkId 북마크 ID
     * @param memo 수정할 메모
     * @param requestUserId 요청한 사용자 ID
     * @return 수정된 북마크
     */
    @Transactional
    public JobBookmarkDto updateBookmarkMemo(Long bookmarkId, String memo, Long requestUserId) {
        JobBookmark bookmark = jobBookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new RuntimeException("북마크를 찾을 수 없습니다. ID: " + bookmarkId));

        // 북마크 소유자 확인
        if (!bookmark.getUser().getId().equals(requestUserId)) {
            throw new RuntimeException("해당 북마크에 접근할 권한이 없습니다.");
        }

        bookmark.setMemo(memo);
        JobBookmark updatedBookmark = jobBookmarkRepository.save(bookmark);
        log.info("북마크 메모 수정: ID {} - 사용자 ID {}", bookmarkId, requestUserId);

        return JobBookmarkDto.fromSimple(updatedBookmark);
    }

    /**
     * 북마크 메모 수정 (권한 확인 없음 - 기존 호환성)
     * @param bookmarkId 북마크 ID
     * @param memo 수정할 메모
     * @return 수정된 북마크
     */
    @Transactional
    public JobBookmarkDto updateBookmarkMemo(Long bookmarkId, String memo) {
        JobBookmark bookmark = jobBookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new RuntimeException("북마크를 찾을 수 없습니다. ID: " + bookmarkId));

        bookmark.setMemo(memo);
        JobBookmark updatedBookmark = jobBookmarkRepository.save(bookmark);
        log.info("북마크 메모 수정: ID {}", bookmarkId);

        return JobBookmarkDto.fromSimple(updatedBookmark);
    }

    /**
     * 북마크 삭제 (권한 확인 포함)
     * @param bookmarkId 북마크 ID
     * @param requestUserId 요청한 사용자 ID
     */
    @Transactional
    public void deleteBookmark(Long bookmarkId, Long requestUserId) {
        JobBookmark bookmark = jobBookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new RuntimeException("북마크를 찾을 수 없습니다. ID: " + bookmarkId));

        // 북마크 소유자 확인
        if (!bookmark.getUser().getId().equals(requestUserId)) {
            throw new RuntimeException("해당 북마크에 접근할 권한이 없습니다.");
        }

        bookmark.setStatus(JobBookmark.BookmarkStatus.DELETED);
        jobBookmarkRepository.save(bookmark);
        log.info("북마크 삭제: ID {} - 사용자 ID {}", bookmarkId, requestUserId);
    }

    /**
     * 북마크 삭제 (권한 확인 없음 - 기존 호환성)
     * @param bookmarkId 북마크 ID
     */
    @Transactional
    public void deleteBookmark(Long bookmarkId) {
        JobBookmark bookmark = jobBookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new RuntimeException("북마크를 찾을 수 없습니다. ID: " + bookmarkId));

        bookmark.setStatus(JobBookmark.BookmarkStatus.DELETED);
        jobBookmarkRepository.save(bookmark);
        log.info("북마크 삭제: ID {}", bookmarkId);
    }

    /**
     * 사용자와 공고로 북마크 삭제
     * @param userId 사용자 ID
     * @param jobPostingId 공고 ID
     */
    @Transactional
    public void deleteBookmarkByUserAndJob(Long userId, Long jobPostingId) {
        validateUser(userId);

        JobBookmark bookmark = jobBookmarkRepository.findByUserIdAndJobPostingIdAndStatus(
                        userId, jobPostingId, JobBookmark.BookmarkStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("북마크를 찾을 수 없습니다."));

        bookmark.setStatus(JobBookmark.BookmarkStatus.DELETED);
        jobBookmarkRepository.save(bookmark);
        log.info("북마크 삭제: 사용자 ID {} - 공고 ID {}", userId, jobPostingId);
    }

    /**
     * 북마크 여부 확인
     * @param userId 사용자 ID
     * @param jobPostingId 공고 ID
     * @return 북마크 여부
     */
    public boolean isBookmarked(Long userId, Long jobPostingId) {
        validateUser(userId);
        return jobBookmarkRepository.existsByUserIdAndJobPostingIdAndStatus(
                userId, jobPostingId, JobBookmark.BookmarkStatus.ACTIVE);
    }

    /**
     * 사용자별 북마크를 생성일 순으로 조회
     * @param userId 사용자 ID
     * @return 북마크 목록 (생성일 순)
     */
    public List<JobBookmarkDto> getBookmarksByUserIdOrderByCreatedAt(Long userId) {
        validateUser(userId);
        return jobBookmarkRepository.findBookmarksByUserIdOrderByCreatedAt(userId).stream()
                .map(JobBookmarkDto::fromSimple)
                .collect(Collectors.toList());
    }

    /**
     * 인기 공고 조회 (북마크 많이 된 순)
     * @return 인기 공고 목록과 북마크 수
     */
    public List<Object[]> getPopularJobs() {
        return jobBookmarkRepository.findPopularJobs();
    }

    /**
     * 사용자 검증
     * @param userId 검증할 사용자 ID
     */
    private void validateUser(Long userId) {
        if (userId == null) {
            throw new RuntimeException("사용자 ID가 필요합니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));

        if (!user.getIsActive()) {
            throw new RuntimeException("비활성화된 사용자입니다.");
        }
    }

    /**
     * 북마크 소유자 검증
     * @param bookmarkId 북마크 ID
     * @param userId 검증할 사용자 ID
     * @return 검증된 북마크
     */
    private JobBookmark validateBookmarkOwnership(Long bookmarkId, Long userId) {
        JobBookmark bookmark = jobBookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new RuntimeException("북마크를 찾을 수 없습니다. ID: " + bookmarkId));

        if (!bookmark.getUser().getId().equals(userId)) {
            throw new RuntimeException("해당 북마크에 접근할 권한이 없습니다.");
        }

        return bookmark;
    }

    /**
     * 북마크 상태별 조회
     * @param userId 사용자 ID
     * @param status 북마크 상태
     * @return 해당 상태의 북마크 목록
     */
    public List<JobBookmarkDto> getBookmarksByStatus(Long userId, JobBookmark.BookmarkStatus status) {
        validateUser(userId);
        return jobBookmarkRepository.findByUserIdAndStatus(userId, status).stream()
                .map(JobBookmarkDto::fromSimple)
                .collect(Collectors.toList());
    }

    /**
     * 북마크 상태 변경
     * @param bookmarkId 북마크 ID
     * @param status 변경할 상태
     * @param requestUserId 요청한 사용자 ID
     * @return 수정된 북마크
     */
    @Transactional
    public JobBookmarkDto updateBookmarkStatus(Long bookmarkId, JobBookmark.BookmarkStatus status, Long requestUserId) {
        JobBookmark bookmark = validateBookmarkOwnership(bookmarkId, requestUserId);

        bookmark.setStatus(status);
        JobBookmark updatedBookmark = jobBookmarkRepository.save(bookmark);
        log.info("북마크 상태 변경: ID {} - 상태 {} - 사용자 ID {}", bookmarkId, status, requestUserId);

        return JobBookmarkDto.fromSimple(updatedBookmark);
    }

    /**
     * 사용자의 모든 북마크 삭제 (계정 삭제 시 사용)
     * @param userId 사용자 ID
     */
    @Transactional
    public void deleteAllBookmarksByUser(Long userId) {
        validateUser(userId);

        List<JobBookmark> bookmarks = jobBookmarkRepository.findByUserId(userId);
        bookmarks.forEach(bookmark -> bookmark.setStatus(JobBookmark.BookmarkStatus.DELETED));
        jobBookmarkRepository.saveAll(bookmarks);

        log.info("사용자의 모든 북마크 삭제: 사용자 ID {} - 북마크 수 {}", userId, bookmarks.size());
    }
}
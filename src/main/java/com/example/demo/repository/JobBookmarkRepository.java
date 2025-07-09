package com.example.demo.repository;

import com.example.demo.entity.JobBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobBookmarkRepository extends JpaRepository<JobBookmark, Long> {

    // 사용자별 북마크 조회
    List<JobBookmark> findByUserIdAndStatus(Long userId, JobBookmark.BookmarkStatus status);

    // 사용자의 모든 북마크 조회 (활성 상태만)
    @Query("SELECT b FROM JobBookmark b WHERE b.user.id = :userId AND b.status = 'ACTIVE'")
    List<JobBookmark> findActiveBookmarksByUserId(@Param("userId") Long userId);

    // 사용자의 북마크 중 마감 임박 공고 조회
    @Query("SELECT b FROM JobBookmark b WHERE b.user.id = :userId AND b.status = 'ACTIVE' " +
            "AND b.jobPosting.endDate BETWEEN :today AND :deadline ORDER BY b.jobPosting.endDate ASC")
    List<JobBookmark> findBookmarksEndingSoon(@Param("userId") Long userId,
                                              @Param("today") LocalDate today,
                                              @Param("deadline") LocalDate deadline);

    // 사용자의 북마크 중 진행중인 공고
    @Query("SELECT b FROM JobBookmark b WHERE b.user.id = :userId AND b.status = 'ACTIVE' " +
            "AND b.jobPosting.startDate <= :today AND b.jobPosting.endDate >= :today")
    List<JobBookmark> findActiveJobBookmarks(@Param("userId") Long userId, @Param("today") LocalDate today);

    // 사용자의 북마크 중 마감된 공고
    @Query("SELECT b FROM JobBookmark b WHERE b.user.id = :userId AND b.status = 'ACTIVE' " +
            "AND b.jobPosting.endDate < :today ORDER BY b.jobPosting.endDate DESC")
    List<JobBookmark> findExpiredJobBookmarks(@Param("userId") Long userId, @Param("today") LocalDate today);

    // 중복 북마크 체크
    boolean existsByUserIdAndJobPostingIdAndStatus(Long userId, Long jobPostingId, JobBookmark.BookmarkStatus status);

    // 특정 북마크 찾기
    Optional<JobBookmark> findByUserIdAndJobPostingIdAndStatus(Long userId, Long jobPostingId, JobBookmark.BookmarkStatus status);

    // 사용자의 북마크 수 카운트
    @Query("SELECT COUNT(b) FROM JobBookmark b WHERE b.user.id = :userId AND b.status = 'ACTIVE'")
    Long countActiveBookmarksByUserId(@Param("userId") Long userId);

    // 인기 공고 (북마크 많이 된 순)
    @Query("SELECT b.jobPosting, COUNT(b) as bookmarkCount FROM JobBookmark b " +
            "WHERE b.status = 'ACTIVE' GROUP BY b.jobPosting ORDER BY bookmarkCount DESC")
    List<Object[]> findPopularJobs();

    // 사용자별 북마크를 생성일 순으로 정렬
    @Query("SELECT b FROM JobBookmark b WHERE b.user.id = :userId AND b.status = 'ACTIVE' " +
            "ORDER BY b.createdAt DESC")
    List<JobBookmark> findBookmarksByUserIdOrderByCreatedAt(@Param("userId") Long userId);
    
    // 사용자의 모든 북마크 조회 (상태 상관없이)
    List<JobBookmark> findByUserId(Long userId);
    
    // 사용자의 북마크 수 카운트 (활성 상태만)
    int countByUserId(Long userId);
}
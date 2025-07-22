package com.example.demo.repository;

import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ApplicationStatusRepository extends JpaRepository<ApplicationStatus, Long> {

    // 🔥 기존 메서드들 유지
    List<ApplicationStatus> findByUser(User user);
    List<ApplicationStatus> findByUserId(Long userId);
    int countByUserIdAndStatus(Long userId, ApplicationStatus.Status status);

    // 🔥 추가된 메서드들
    /**
     * 특정 사용자의 모든 지원현황을 최신순으로 조회
     */
    List<ApplicationStatus> findByUserOrderByIdDesc(User user);

    /**
     * 특정 사용자 ID로 최신순 조회 (기존 메서드와 호환)
     */
    List<ApplicationStatus> findByUserIdOrderByIdDesc(Long userId);

    /**
     * 특정 사용자의 지원현황을 상태별로 조회
     */
    List<ApplicationStatus> findByUserAndStatusOrderByIdDesc(User user, ApplicationStatus.Status status);

    /**
     * 특정 사용자의 지원현황 개수 조회
     */
    long countByUser(User user);

    /**
     * 특정 사용자의 전체 지원현황 개수 조회 (기존 호환)
     */
    long countByUserId(Long userId);

    /**
     * 특정 사용자의 상태별 지원현황 개수 조회 (User 엔티티 기반)
     */
    long countByUserAndStatus(User user, ApplicationStatus.Status status);

    /**
     * 특정 사용자의 마감일이 임박한 지원현황 조회 (7일 이내)
     */
    @Query("SELECT a FROM ApplicationStatus a WHERE a.user.id = :userId AND a.deadline BETWEEN :startDate AND :endDate ORDER BY a.deadline ASC")
    List<ApplicationStatus> findUpcomingDeadlinesByUserId(@Param("userId") Long userId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    /**
     * 특정 사용자의 회사명으로 검색
     */
    List<ApplicationStatus> findByUserIdAndCompanyContainingIgnoreCaseOrderByIdDesc(Long userId, String company);

    /**
     * 특정 사용자의 카테고리별 지원현황 조회
     */
    List<ApplicationStatus> findByUserIdAndCategoryOrderByIdDesc(Long userId, String category);

    /**
     * 특정 사용자의 마감일이 지나지 않은 지원현황 조회
     */
    @Query("SELECT a FROM ApplicationStatus a WHERE a.user.id = :userId AND (a.deadline IS NULL OR a.deadline >= :currentDate) ORDER BY a.deadline ASC")
    List<ApplicationStatus> findActiveApplicationsByUserId(@Param("userId") Long userId, @Param("currentDate") LocalDate currentDate);
}
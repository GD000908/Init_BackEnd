package com.example.demo.repository;

import com.example.demo.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    // 제목으로 검색
    List<JobPosting> findByTitleContainingIgnoreCase(String title);

    // 회사명으로 검색
    List<JobPosting> findByCompanyContainingIgnoreCase(String company);

    // 포지션으로 검색
    List<JobPosting> findByPositionContainingIgnoreCase(String position);

    // 지역으로 검색
    List<JobPosting> findByLocationContainingIgnoreCase(String location);

    // 상태로 검색
    List<JobPosting> findByStatus(JobPosting.JobStatus status);

    // 날짜 범위로 검색 (마감일 기준)
    List<JobPosting> findByEndDateBetween(LocalDate startDate, LocalDate endDate);

    // 현재 진행중인 공고 (오늘 날짜 기준)
    @Query("SELECT j FROM JobPosting j WHERE j.startDate <= :today AND j.endDate >= :today")
    List<JobPosting> findActiveJobs(@Param("today") LocalDate today);

    // 마감 임박 공고 (N일 이내)
    @Query("SELECT j FROM JobPosting j WHERE j.endDate BETWEEN :today AND :deadline ORDER BY j.endDate ASC")
    List<JobPosting> findJobsEndingSoon(@Param("today") LocalDate today, @Param("deadline") LocalDate deadline);

    // 마감된 공고
    @Query("SELECT j FROM JobPosting j WHERE j.endDate < :today")
    List<JobPosting> findExpiredJobs(@Param("today") LocalDate today);

    // 예정된 공고
    @Query("SELECT j FROM JobPosting j WHERE j.startDate > :today")
    List<JobPosting> findUpcomingJobs(@Param("today") LocalDate today);

    // 특정 기간의 공고 수 카운트
    @Query("SELECT COUNT(j) FROM JobPosting j WHERE j.createdAt BETWEEN :startDate AND :endDate")
    Long countJobsCreatedBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

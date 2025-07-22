package com.example.demo.repository;

import com.example.demo.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    /**
     * 특정 사용자의 이력서 목록을 생성일 기준 내림차순으로 조회
     */
    List<Resume> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 특정 사용자의 특정 이력서 조회 (권한 체크용)
     */
    Optional<Resume> findByIdAndUserId(Long id, Long userId);

    /**
     * 특정 사용자의 대표 이력서 조회
     */
    Optional<Resume> findByUserIdAndIsPrimaryTrue(Long userId);

    /**
     * 특정 사용자의 공개 이력서 목록 조회
     */
    List<Resume> findByUserIdAndIsPublicTrue(Long userId);

    /**
     * 특정 사용자의 이력서 개수 조회
     */
    long countByUserId(Long userId);

    /**
     * 특정 사용자의 대표 이력서를 모두 일반 이력서로 변경
     */
    @Modifying
    @Query("UPDATE Resume r SET r.isPrimary = false WHERE r.user.id = :userId")
    void resetPrimaryResumes(@Param("userId") Long userId);

    /**
     * 이력서 제목으로 검색 (특정 사용자)
     */
    List<Resume> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title);

    /**
     * 특정 사용자의 이력서 존재 여부 확인
     */
    boolean existsByIdAndUserId(Long id, Long userId);
}
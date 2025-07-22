package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 로그인 시 사용자 아이디(String)로 사용자를 찾습니다.
     */
    Optional<User> findByUserId(String userId);

    /**
     * 회원가입 시 사용자 아이디의 중복 여부를 확인합니다.
     */
    boolean existsByUserId(String userId);

    /**
     * 회원가입 시 이메일의 중복 여부를 확인합니다.
     */
    boolean existsByEmail(String email);

    /**
     * 이메일로 사용자를 찾습니다.
     */
    Optional<User> findByEmail(String email);

    // 🔥 관리자용 추가 메서드들

    /**
     * 사용자 검색 (아이디, 이메일, 이름으로 검색)
     */
    Page<User> findByUserIdContainingOrEmailContainingOrNameContaining(
            String userId, String email, String name, Pageable pageable);

    /**
     * 활성/비활성 상태별 사용자 수 조회
     */
    long countByIsActive(boolean isActive);

    /**
     * 특정 기간 사이에 가입한 사용자 수 조회
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 특정 날짜 이전에 가입한 전체 사용자 수 조회
     */
    long countByCreatedAtBefore(LocalDateTime date);

    /**
     * 활성 상태별 사용자 목록 조회
     */
    Page<User> findByIsActive(boolean isActive, Pageable pageable);
}
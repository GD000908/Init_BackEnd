package com.example.demo.repository;

import com.example.demo.entity.CommunityProfile;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityProfileRepository extends JpaRepository<CommunityProfile, Long> {
    
    // 사용자 ID로 커뮤니티 프로필 조회
    Optional<CommunityProfile> findByUser(User user);
    
    @Query("SELECT cp FROM CommunityProfile cp WHERE cp.user.id = :userId")
    Optional<CommunityProfile> findByUserId(@Param("userId") Long userId);
    

    // 표시 이름으로 검색 (LIKE 검색)
    @Query("SELECT cp FROM CommunityProfile cp WHERE cp.displayName LIKE %:name% AND cp.isPublic = true")
    Page<CommunityProfile> findByDisplayNameContaining(@Param("name") String name, Pageable pageable);
    
    // 공개 프로필만 조회
    @Query("SELECT cp FROM CommunityProfile cp WHERE cp.isPublic = true")
    Page<CommunityProfile> findPublicProfiles(Pageable pageable);
    
    // 팔로워 수 기준 상위 프로필 조회
    @Query("SELECT cp FROM CommunityProfile cp WHERE cp.isPublic = true ORDER BY cp.followersCount DESC")
    Page<CommunityProfile> findTopProfilesByFollowers(Pageable pageable);
    
    // 활성 사용자 프로필 조회 (게시글이 있는 사용자들)
    @Query("SELECT cp FROM CommunityProfile cp WHERE cp.postsCount > 0 AND cp.isPublic = true ORDER BY cp.postsCount DESC")
    Page<CommunityProfile> findActiveProfiles(Pageable pageable);
    
    // 최근 가입한 프로필 조회
    @Query("SELECT cp FROM CommunityProfile cp WHERE cp.isPublic = true ORDER BY cp.createdAt DESC")
    Page<CommunityProfile> findRecentProfiles(Pageable pageable);
    
    // 특정 회사의 프로필들 조회
    @Query("SELECT cp FROM CommunityProfile cp WHERE cp.company LIKE %:company% AND cp.isPublic = true")
    List<CommunityProfile> findByCompanyContaining(@Param("company") String company);
    
    // 특정 직책의 프로필들 조회
    @Query("SELECT cp FROM CommunityProfile cp WHERE cp.jobTitle LIKE %:jobTitle% AND cp.isPublic = true")
    List<CommunityProfile> findByJobTitleContaining(@Param("jobTitle") String jobTitle);
    
    // 특정 지역의 프로필들 조회
    @Query("SELECT cp FROM CommunityProfile cp WHERE cp.location LIKE %:location% AND cp.isPublic = true")
    List<CommunityProfile> findByLocationContaining(@Param("location") String location);
    
    // 프로필 통계 조회
    @Query("SELECT COUNT(cp) FROM CommunityProfile cp WHERE cp.isPublic = true")
    long countPublicProfiles();
    
    @Query("SELECT AVG(cp.followersCount) FROM CommunityProfile cp WHERE cp.isPublic = true")
    Double getAverageFollowersCount();
    
    @Query("SELECT AVG(cp.postsCount) FROM CommunityProfile cp WHERE cp.isPublic = true")
    Double getAveragePostsCount();
}

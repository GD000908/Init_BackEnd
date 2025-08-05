package com.example.demo.repository;

import com.example.demo.entity.CommunityProfile;
import com.example.demo.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    // 팔로우 관계 존재 여부 확인
    boolean existsByFollowerAndFollowing(CommunityProfile follower, CommunityProfile following);
    
    // 팔로우 관계 조회
    Optional<Follow> findByFollowerAndFollowing(CommunityProfile follower, CommunityProfile following);
    
    // 특정 사용자가 팔로잉하는 사람들 조회
    Page<Follow> findByFollowerOrderByCreatedAtDesc(CommunityProfile follower, Pageable pageable);
    
    // 특정 사용자를 팔로우하는 사람들 조회 (팔로워)
    Page<Follow> findByFollowingOrderByCreatedAtDesc(CommunityProfile following, Pageable pageable);
    
    // 팔로잉 수 조회
    long countByFollower(CommunityProfile follower);
    
    // 팔로워 수 조회
    long countByFollowing(CommunityProfile following);
    
    // 특정 사용자가 팔로잉하는 사람들의 ID 목록
    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId")
    List<Long> findFollowingIdsByFollowerId(@Param("followerId") Long followerId);
    
    // 상호 팔로우 관계인지 확인
    @Query("SELECT COUNT(f) = 2 FROM Follow f " +
           "WHERE (f.follower.id = :user1Id AND f.following.id = :user2Id) " +
           "OR (f.follower.id = :user2Id AND f.following.id = :user1Id)")
    boolean isMutualFollow(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
    
    // 팔로우 추천 (내가 팔로우하지 않은 사람 중 팔로워가 많은 사람)
    @Query("SELECT cp FROM CommunityProfile cp " +
           "WHERE cp.id NOT IN (" +
           "    SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId" +
           ") " +
           "AND cp.id != :userId " +
           "AND cp.allowFollow = true " +
           "AND cp.isPublic = true " +
           "ORDER BY cp.followersCount DESC")
    List<CommunityProfile> findFollowRecommendations(@Param("userId") Long userId, Pageable pageable);
}

package com.example.demo.repository;

import com.example.demo.entity.CommunityProfile;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    
    // 좋아요 여부 확인
    boolean existsByPostAndUser(Post post, CommunityProfile user);
    
    // 좋아요 관계 조회
    Optional<PostLike> findByPostAndUser(Post post, CommunityProfile user);
    
    // 특정 게시글의 좋아요 수
    long countByPost(Post post);
    
    // 특정 사용자가 좋아요한 게시글들
    Page<PostLike> findByUserOrderByCreatedAtDesc(CommunityProfile user, Pageable pageable);
    
    // 특정 게시글의 모든 좋아요 삭제 (게시글 삭제 시)
    void deleteByPost(Post post);
    
    // 특정 게시글의 좋아요한 사용자들
    Page<PostLike> findByPostOrderByCreatedAtDesc(Post post, Pageable pageable);
    
    // 특정 사용자가 좋아요한 게시글 ID 목록 (IN 쿼리용)
    @Query("SELECT pl.post.id FROM PostLike pl WHERE pl.user.id = :userId")
    List<Long> findLikedPostIdsByUserId(@Param("userId") Long userId);
    
    // 내가 좋아요한 게시글들과 좋아요 정보
    @Query("SELECT pl FROM PostLike pl " +
           "JOIN FETCH pl.post p " +
           "WHERE pl.user.id = :userId " +
           "AND p.status = 'PUBLISHED' " +
           "ORDER BY pl.createdAt DESC")
    Page<PostLike> findMyLikedPosts(@Param("userId") Long userId, Pageable pageable);
}

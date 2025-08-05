package com.example.demo.repository;

import com.example.demo.entity.CommunityProfile;
import com.example.demo.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 발행된 게시글만 조회 (최신순)
    Page<Post> findByStatusOrderByCreatedAtDesc(Post.PostStatus status, Pageable pageable);
    
    // 특정 사용자의 게시글 조회
    Page<Post> findByAuthorAndStatusOrderByCreatedAtDesc(CommunityProfile author, Post.PostStatus status, Pageable pageable);
    
    // 특정 사용자의 모든 게시글 조회 (상태 무관)
    Page<Post> findByAuthorOrderByCreatedAtDesc(CommunityProfile author, Pageable pageable);
    
    // 직무 카테고리별 게시글 조회
    Page<Post> findByJobCategoryAndStatusOrderByCreatedAtDesc(String jobCategory, Post.PostStatus status, Pageable pageable);
    
    // 주제 카테고리별 게시글 조회
    Page<Post> findByTopicCategoryAndStatusOrderByCreatedAtDesc(String topicCategory, Post.PostStatus status, Pageable pageable);
    
    // 팔로잉한 사용자들의 게시글 조회
    @Query("SELECT p FROM Post p " +
           "WHERE p.author.id IN :followingIds " +
           "AND p.status = :status " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findFollowingPosts(@Param("followingIds") List<Long> followingIds, 
                                  @Param("status") Post.PostStatus status, 
                                  Pageable pageable);
    
    // 🔥 개선된 통합 검색 - 게시글 내용, 작성자 이름, 해시태그, 직책 모두 검색
    @Query("SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN p.hashtags h " +
           "LEFT JOIN p.author a " +
           "WHERE (p.content LIKE %:keyword% " +
           "OR a.displayName LIKE %:keyword% " +
           "OR a.jobTitle LIKE %:keyword% " +
           "OR h.hashtag LIKE %:keyword%) " +
           "AND p.status = :status " +
           "ORDER BY p.createdAt DESC")
    Page<Post> searchPosts(@Param("keyword") String keyword, 
                          @Param("status") Post.PostStatus status, 
                          Pageable pageable);
    
    // 해시태그로 검색
    @Query("SELECT DISTINCT p FROM Post p " +
           "JOIN p.hashtags h " +
           "WHERE h.hashtag = :hashtag " +
           "AND p.status = :status " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findByHashtag(@Param("hashtag") String hashtag, 
                            @Param("status") Post.PostStatus status, 
                            Pageable pageable);
    
    // 인기 게시글 조회 (좋아요 수 기준)
    @Query("SELECT p FROM Post p " +
           "WHERE p.status = :status " +
           "ORDER BY p.likesCount DESC, p.createdAt DESC")
    Page<Post> findPopularPosts(@Param("status") Post.PostStatus status, Pageable pageable);
    
    // 특정 사용자의 게시글 수 조회
    long countByAuthorAndStatus(CommunityProfile author, Post.PostStatus status);
}

package com.example.demo.repository;

import com.example.demo.entity.Comment;
import com.example.demo.entity.CommunityProfile;
import com.example.demo.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // 댓글 조회 시 연관 엔티티 함께 fetch (Lazy Loading 문제 해결)
    @Query("SELECT c FROM Comment c " +
           "JOIN FETCH c.author a " +
           "JOIN FETCH a.user " +
           "WHERE c.id = :commentId")
    Optional<Comment> findByIdWithAuthorAndUser(@Param("commentId") Long commentId);
    
    // 🔥 특정 게시글의 댓글 조회 (최신순) - N+1 문제 해결
    @Query("SELECT c FROM Comment c " +
           "JOIN FETCH c.author a " +
           "JOIN FETCH a.user " +
           "WHERE c.post = :post " +
           "ORDER BY c.createdAt DESC")
    Page<Comment> findByPostWithAuthor(@Param("post") Post post, Pageable pageable);
    
    // 기존 메서드 유지 (호환성을 위해)
    Page<Comment> findByPostOrderByCreatedAtDesc(Post post, Pageable pageable);
    
    // 특정 게시글의 댓글 조회 (좋아요 순)
    Page<Comment> findByPostOrderByLikesCountDescCreatedAtDesc(Post post, Pageable pageable);
    
    // 🔥 특정 사용자가 작성한 댓글 조회 - N+1 문제 해결
    @Query("SELECT c FROM Comment c " +
           "JOIN FETCH c.author a " +
           "JOIN FETCH a.user " +
           "JOIN FETCH c.post p " +
           "JOIN FETCH p.author pa " +
           "WHERE c.author = :author " +
           "ORDER BY c.createdAt DESC")
    Page<Comment> findByAuthorWithPost(@Param("author") CommunityProfile author, Pageable pageable);
    
    // 기존 메서드 유지 (호환성을 위해)
    Page<Comment> findByAuthorOrderByCreatedAtDesc(CommunityProfile author, Pageable pageable);
    
    // 특정 게시글의 댓글 수 조회
    long countByPost(Post post);
    
    // 특정 사용자의 댓글 수 조회
    long countByAuthor(CommunityProfile author);
    
    // 특정 사용자가 댓글을 단 게시글들 조회
    @Query("SELECT DISTINCT c.post FROM Comment c " +
           "WHERE c.author = :author " +
           "ORDER BY c.createdAt DESC")
    List<Post> findPostsCommentedByUser(@Param("author") CommunityProfile author, Pageable pageable);
    
    // 내가 댓글 단 게시글들과 해당 댓글 정보 조회
    @Query("SELECT c FROM Comment c " +
           "WHERE c.author = :author " +
           "ORDER BY c.createdAt DESC")
    Page<Comment> findMyComments(@Param("author") CommunityProfile author, Pageable pageable);
}

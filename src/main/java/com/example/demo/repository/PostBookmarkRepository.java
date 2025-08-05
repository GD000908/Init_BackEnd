package com.example.demo.repository;

import com.example.demo.entity.CommunityProfile;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {

    /**
     * 특정 사용자와 게시글의 북마크 존재 여부 확인
     */
    boolean existsByUserAndPost(CommunityProfile user, Post post);

    /**
     * 특정 사용자와 게시글의 북마크 조회
     */
    Optional<PostBookmark> findByUserAndPost(CommunityProfile user, Post post);

    /**
     * 특정 사용자가 북마크한 게시글 목록 조회 (생성일 기준 내림차순)
     */
    @Query("SELECT pb FROM PostBookmark pb " +
           "JOIN FETCH pb.post p " +
           "JOIN FETCH p.author " +
           "WHERE pb.user = :user " +
           "ORDER BY pb.createdAt DESC")
    Page<PostBookmark> findByUserOrderByCreatedAtDesc(@Param("user") CommunityProfile user, Pageable pageable);

    /**
     * 특정 게시글의 북마크 수 조회
     */
    long countByPost(Post post);

    /**
     * 특정 사용자의 전체 북마크 수 조회
     */
    long countByUser(CommunityProfile user);

    /**
     * 특정 사용자의 북마크 모두 삭제
     */
    void deleteByUser(CommunityProfile user);

    /**
     * 특정 게시글의 북마크 모두 삭제
     */
    void deleteByPost(Post post);

    Boolean existsByPostAndUser(Post post, CommunityProfile communityProfile);
}

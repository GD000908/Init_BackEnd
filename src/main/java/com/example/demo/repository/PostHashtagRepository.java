package com.example.demo.repository;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostHashtagRepository extends JpaRepository<PostHashtag, Long> {
    
    // 특정 게시글의 해시태그들
    List<PostHashtag> findByPost(Post post);
    
    // 특정 해시태그가 사용된 게시글들
    List<PostHashtag> findByHashtag(String hashtag);
    
    // 인기 해시태그 조회 (사용 빈도순)
    @Query("SELECT ph.hashtag, COUNT(ph) as count FROM PostHashtag ph " +
           "GROUP BY ph.hashtag " +
           "ORDER BY count DESC")
    List<Object[]> findPopularHashtags();
    
    // 특정 게시글의 해시태그 삭제
    void deleteByPost(Post post);
    
    // 해시태그 자동완성을 위한 검색
    @Query("SELECT DISTINCT ph.hashtag FROM PostHashtag ph " +
           "WHERE ph.hashtag LIKE %:prefix% " +
           "ORDER BY ph.hashtag")
    List<String> findHashtagsStartingWith(@Param("prefix") String prefix);
    
    // 최근 인기 해시태그 (최근 7일)
    @Query("SELECT ph.hashtag, COUNT(ph) as count FROM PostHashtag ph " +
           "JOIN ph.post p " +
           "WHERE p.createdAt >= :weekAgo " +
           "AND p.status = 'PUBLISHED' " +
           "GROUP BY ph.hashtag " +
           "ORDER BY count DESC")
    List<Object[]> findRecentPopularHashtags(@Param("weekAgo") java.time.LocalDateTime weekAgo);
}

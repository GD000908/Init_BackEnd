package com.example.demo.service;

import com.example.demo.entity.CommunityProfile;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostBookmark;
import com.example.demo.repository.CommunityProfileRepository;
import com.example.demo.repository.PostBookmarkRepository;
import com.example.demo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostBookmarkService {

    private final PostBookmarkRepository postBookmarkRepository;
    private final PostRepository postRepository;
    private final CommunityProfileRepository communityProfileRepository;

    /**
     * 북마크 토글 (추가/제거)
     */
    @Transactional
    public boolean toggleBookmark(Long postId, Long userId) {
        log.info("북마크 토글 시도: postId={}, userId={}", postId, userId);

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다: " + postId));

        CommunityProfile user = communityProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("커뮤니티 프로필을 찾을 수 없습니다: " + userId));

        // 기존 북마크 확인
        Optional<PostBookmark> existingBookmark = postBookmarkRepository.findByUserAndPost(user, post);

        if (existingBookmark.isPresent()) {
            // 북마크 제거
            postBookmarkRepository.delete(existingBookmark.get());
            
            // 게시글의 북마크 수 감소
            post.decrementBookmarksCount();
            postRepository.save(post);
            
            log.info("북마크 제거 완료: postId={}, userId={}", postId, userId);
            return false;
        } else {
            // 북마크 추가
            PostBookmark bookmark = PostBookmark.builder()
                .user(user)
                .post(post)
                .build();
            
            postBookmarkRepository.save(bookmark);
            
            // 게시글의 북마크 수 증가
            post.incrementBookmarksCount();
            postRepository.save(post);
            
            log.info("북마크 추가 완료: postId={}, userId={}", postId, userId);
            return true;
        }
    }

    /**
     * 북마크 상태 확인
     */
    public boolean isBookmarked(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다: " + postId));

        CommunityProfile user = communityProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("커뮤니티 프로필을 찾을 수 없습니다: " + userId));

        return postBookmarkRepository.existsByUserAndPost(user, post);
    }

    /**
     * 사용자의 북마크한 게시글 목록 조회
     */
    public Page<PostBookmark> getUserBookmarks(Long userId, Pageable pageable) {
        log.info("사용자 북마크 목록 조회: userId={}", userId);

        CommunityProfile user = communityProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("커뮤니티 프로필을 찾을 수 없습니다: " + userId));

        Page<PostBookmark> bookmarks = postBookmarkRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        
        log.info("사용자 북마크 조회 완료: userId={}, count={}", userId, bookmarks.getTotalElements());
        
        return bookmarks;
    }

    /**
     * 게시글의 북마크 수 조회
     */
    public long getBookmarkCount(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다: " + postId));

        return postBookmarkRepository.countByPost(post);
    }

    /**
     * 사용자의 전체 북마크 수 조회
     */
    public long getUserBookmarkCount(Long userId) {
        CommunityProfile user = communityProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("커뮤니티 프로필을 찾을 수 없습니다: " + userId));

        return postBookmarkRepository.countByUser(user);
    }
}

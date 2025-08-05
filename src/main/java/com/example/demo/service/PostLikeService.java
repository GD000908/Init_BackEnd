package com.example.demo.service;

import com.example.demo.entity.CommunityProfile;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostLike;
import com.example.demo.exception.CommunityProfileNotFoundException;
import com.example.demo.exception.PostNotFoundException;
import com.example.demo.repository.CommunityProfileRepository;
import com.example.demo.repository.PostLikeRepository;
import com.example.demo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final CommunityProfileRepository communityProfileRepository;

    /**
     * 게시글 좋아요 토글 (동시성 처리 개선)
     */
    @Transactional
    public synchronized boolean toggleLike(Long postId, Long userId) {
        log.info("🎯 게시글 좋아요 토글 시도: postId={}, userId={}", postId, userId);

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));

        CommunityProfile user = communityProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new CommunityProfileNotFoundException(userId));

        // 현재 좋아요 상태를 다시 확인
        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);
        
        boolean isNowLiked;
        try {
            if (existingLike.isPresent()) {
                // 좋아요 취소
                log.info("📤 좋아요 취소 시작: postId={}, userId={}", postId, userId);
                postLikeRepository.delete(existingLike.get());
                postLikeRepository.flush(); // 즉시 DB 반영
                
                post.decrementLikesCount();
                isNowLiked = false;
                log.info("✅ 게시글 좋아요 취소 완료: postId={}, userId={}, newCount={}", 
                        postId, userId, post.getLikesCount());
            } else {
                // 좋아요 추가
                log.info("📥 좋아요 추가 시작: postId={}, userId={}", postId, userId);
                PostLike newLike = new PostLike(post, user);
                postLikeRepository.save(newLike);
                postLikeRepository.flush(); // 즉시 DB 반영
                
                post.incrementLikesCount();
                isNowLiked = true;
                log.info("✅ 게시글 좋아요 추가 완료: postId={}, userId={}, newCount={}", 
                        postId, userId, post.getLikesCount());
            }

            // Post 저장 및 즉시 반영
            postRepository.save(post);
            postRepository.flush();
            
            // 검증: 실제 DB 상태와 비교
            long actualCount = postLikeRepository.countByPost(post);
            if (actualCount != post.getLikesCount()) {
                log.warn("⚠️ 좋아요 카운트 불일치 감지! 수정중... expected={}, actual={}", 
                        post.getLikesCount(), actualCount);
                post.setLikesCount((int) actualCount);
                postRepository.save(post);
            }
            
            log.info("🔍 최종 검증 완료: postId={}, isLiked={}, dbCount={}, entityCount={}", 
                    postId, isNowLiked, actualCount, post.getLikesCount());
            
            return isNowLiked;
            
        } catch (Exception e) {
            log.error("❌ 좋아요 토글 중 오류 발생: postId={}, userId={}", postId, userId, e);
            throw e;
        }
    }

    /**
     * 게시글 좋아요 상태 확인
     */
    public boolean isLikedByUser(Long postId, Long userId) {
        log.info("🔍 게시글 좋아요 상태 확인: postId={}, userId={}", postId, userId);
        
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));

        CommunityProfile user = communityProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new CommunityProfileNotFoundException(userId));

        boolean isLiked = postLikeRepository.existsByPostAndUser(post, user);
        log.info("✅ 좋아요 상태: {}", isLiked);
        
        return isLiked;
    }

    /**
     * 게시글의 좋아요 수 조회
     */
    public long getLikesCount(Long postId) {
        log.info("🔍 게시글 좋아요 수 조회: postId={}", postId);
        
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));

        long count = postLikeRepository.countByPost(post);
        log.info("✅ 좋아요 수: {}", count);
        
        return count;
    }
}

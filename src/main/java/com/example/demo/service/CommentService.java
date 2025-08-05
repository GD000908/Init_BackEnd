package com.example.demo.service;

import com.example.demo.dto.CommentDto;
import com.example.demo.dto.CreateCommentRequest;
import com.example.demo.dto.PostSummaryDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.CommunityProfile;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.CommunityProfileRepository;
import com.example.demo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommunityProfileRepository communityProfileRepository;

    /**
     * 댓글 생성
     */
    @Transactional
    public CommentDto createComment(Long postId, Long authorId, CreateCommentRequest request) {
        log.info("🎯 댓글 생성 시도: postId={}, authorId={}", postId, authorId);

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다: " + postId));

        CommunityProfile author = communityProfileRepository.findByUserId(authorId)
            .orElseThrow(() -> new RuntimeException("커뮤니티 프로필을 찾을 수 없습니다: " + authorId));

        Comment comment = Comment.builder()
            .post(post)
            .author(author)
            .content(request.getContent())
            .build();

        Comment savedComment = commentRepository.save(comment);

        // 게시글의 댓글 수 증가
        post.incrementCommentsCount();
        postRepository.save(post);

        log.info("✅ 댓글 생성 완료: commentId={}", savedComment.getId());
        return convertToDto(savedComment, authorId);
    }

    /**
     * 게시글의 댓글 목록 조회
     * 🔥 수정: JOIN FETCH를 사용하는 메서드로 변경하여 N+1 문제 해결
     */
    public Page<CommentDto> getCommentsByPost(Long postId, Long currentUserId, Pageable pageable) {
        log.info("🔍 게시글 댓글 조회: postId={}", postId);

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다: " + postId));

        // 🔥 N+1 문제 해결: JOIN FETCH를 사용하는 메서드 사용
        Page<Comment> comments = commentRepository.findByPostWithAuthor(post, pageable);
        
        log.info("✅ 게시글 댓글 조회 완료: postId={}, totalComments={}", postId, comments.getTotalElements());
        
        return comments.map(comment -> convertToDto(comment, currentUserId));
    }

    /**
     * 사용자가 작성한 댓글 목록 조회
     * 🔥 이미 JOIN FETCH를 사용하고 있어서 N+1 문제 없음
     */
    public Page<CommentDto> getCommentsByUser(Long userId, Long currentUserId, Pageable pageable) {
        log.info("🔍 사용자 댓글 조회: userId={}", userId);

        CommunityProfile author = communityProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        // 🔥 연관 엔티티와 함께 조회하여 N+1 문제 해결
        Page<Comment> comments = commentRepository.findByAuthorWithPost(author, pageable);
        
        log.info("✅ 사용자 댓글 조회 완료: userId={}, totalComments={}", userId, comments.getTotalElements());
        
        return comments.map(comment -> convertToDto(comment, currentUserId));
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long commentId, Long authorId) {
        log.info("🗑️ 댓글 삭제 시도: commentId={}, authorId={}", commentId, authorId);

        // 🔥 연관 엔티티와 함께 조회하여 Lazy Loading 문제 해결 (이미 적용됨)
        Comment comment = commentRepository.findByIdWithAuthorAndUser(commentId)
            .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다: " + commentId));

        // 작성자 확인 - null 체크와 Lazy Loading 문제 해결
        CommunityProfile author = comment.getAuthor();
        if (author == null) {
            log.error("❌ 댓글의 작성자 정보가 없습니다: commentId={}", commentId);
            throw new RuntimeException("댓글의 작성자 정보를 찾을 수 없습니다");
        }

        User user = author.getUser();
        if (user == null) {
            log.error("❌ CommunityProfile에 연결된 사용자 정보가 없습니다: authorId={}", author.getId());
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다");
        }

        Long commentAuthorUserId = user.getId();
        log.info("🔍 권한 체크: 댓글 작성자 User ID={}, 요청자 User ID={}", commentAuthorUserId, authorId);

        if (!commentAuthorUserId.equals(authorId)) {
            log.warn("⚠️ 댓글 삭제 권한 없음: commentId={}, 댓글작성자UserId={}, 요청자UserId={}", 
                    commentId, commentAuthorUserId, authorId);
            throw new RuntimeException("댓글 삭제 권한이 없습니다");
        }

        // 게시글의 댓글 수 감소
        Post post = comment.getPost();
        post.decrementCommentsCount();
        postRepository.save(post);

        commentRepository.delete(comment);
        log.info("✅ 댓글 삭제 완료: commentId={}", commentId);
    }

    /**
     * 댓글을 DTO로 변환
     * 🔥 수정: 더 안전한 null 체크와 로깅 추가
     */
    private CommentDto convertToDto(Comment comment, Long currentUserId) {
        log.debug("🔄 CommentDto 변환 시작: commentId={}, currentUserId={}", comment.getId(), currentUserId);
        
        // 작성자 정보 안전하게 가져오기
        CommunityProfile author = comment.getAuthor();
        if (author == null) {
            log.error("❌ 댓글의 작성자 정보가 없습니다: commentId={}", comment.getId());
            throw new RuntimeException("댓글의 작성자 정보를 찾을 수 없습니다");
        }
        
        User user = author.getUser();
        if (user == null) {
            log.error("❌ 작성자의 사용자 정보가 없습니다: authorId={}", author.getId());
            throw new RuntimeException("작성자의 사용자 정보를 찾을 수 없습니다");
        }
        
        Post post = comment.getPost();
        if (post == null) {
            log.error("❌ 댓글의 게시글 정보가 없습니다: commentId={}", comment.getId());
            throw new RuntimeException("댓글의 게시글 정보를 찾을 수 없습니다");
        }
        
        // 🔥 수정: displayName이 null이거나 비어있는 경우 User의 name 사용
        String authorName = author.getDisplayName();
        if (authorName == null || authorName.trim().isEmpty()) {
            authorName = user.getName();
            if (authorName == null || authorName.trim().isEmpty()) {
                authorName = "익명";
            }
        }
        
        // 🔥 프로필 이미지가 없으면 기본 이미지 사용
        String avatarUrl = author.getProfileImageUrl();
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            avatarUrl = "/placeholder_person.svg";
        }
        
        CommentDto.AuthorDto authorDto = CommentDto.AuthorDto.builder()
            .id(author.getId())
            .userId(user.getId())
            .name(authorName)
            .avatar(avatarUrl)  // 🔥 기본 이미지 처리된 URL 사용
            .title(author.getJobTitle())
            .build();

        // 게시글 작성자 정보
        CommunityProfile postAuthor = post.getAuthor();
        String postAuthorName = "익명";
        String postAuthorAvatarUrl = "/placeholder_person.svg";  // 🔥 기본 이미지
        
        if (postAuthor != null) {
            postAuthorName = postAuthor.getDisplayName();
            if (postAuthorName == null || postAuthorName.trim().isEmpty()) {
                User postUser = postAuthor.getUser();
                if (postUser != null && postUser.getName() != null) {
                    postAuthorName = postUser.getName();
                }
            }
            
            // 🔥 프로필 이미지가 없으면 기본 이미지 사용
            postAuthorAvatarUrl = postAuthor.getProfileImageUrl();
            if (postAuthorAvatarUrl == null || postAuthorAvatarUrl.trim().isEmpty()) {
                postAuthorAvatarUrl = "/placeholder_person.svg";
            }
        }

        PostSummaryDto.AuthorDto postAuthorDto = PostSummaryDto.AuthorDto.builder()
                .name(postAuthorName)
                .avatar(postAuthorAvatarUrl)  // 🔥 기본 이미지 처리된 URL 사용
                .build();

        // 게시글 해시태그 (안전하게 처리)
        List<String> hashtags = List.of(); // 기본값
        try {
            if (post.getHashtags() != null) {
                hashtags = post.getHashtags().stream()
                        .map(h -> h.getHashtag())
                        .toList();
            }
        } catch (Exception e) {
            log.warn("⚠️ 해시태그 조회 중 오류 발생: postId={}", post.getId(), e);
        }

        PostSummaryDto postSummaryDto = PostSummaryDto.builder()
                .id(post.getId())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())  // 🔥 이미지 URL 추가
                .hashtags(hashtags)
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .timeAgo(calculateTimeAgo(post.getCreatedAt()))
                .jobCategory(post.getJobCategory())
                .topicCategory(post.getTopicCategory())
                .author(postAuthorDto)
                .build();

        CommentDto dto = CommentDto.builder()
                .id(comment.getId())
                .postId(post.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .author(authorDto)
                .timeAgo(calculateTimeAgo(comment.getCreatedAt()))
                .post(postSummaryDto)
                .build();
                
        log.debug("✅ CommentDto 변환 완료: commentId={}", comment.getId());
        return dto;
    }

    private String calculateTimeAgo(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "알 수 없음";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(createdAt, now);
        
        if (minutes < 1) return "방금 전";
        if (minutes < 60) return minutes + "분 전";
        
        long hours = ChronoUnit.HOURS.between(createdAt, now);
        if (hours < 24) return hours + "시간 전";
        
        long days = ChronoUnit.DAYS.between(createdAt, now);
        if (days < 7) return days + "일 전";
        
        long weeks = days / 7;
        if (weeks < 4) return weeks + "주 전";
        
        long months = days / 30;
        if (months < 12) return months + "개월 전";
        
        long years = days / 365;
        return years + "년 전";
    }
}

package com.example.demo.service;

import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.PostDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CommunityProfileNotFoundException;
import com.example.demo.exception.PostAccessDeniedException;
import com.example.demo.exception.PostNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CommunityProfileRepository communityProfileRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostBookmarkRepository postBookmarkRepository;
    private final FollowRepository followRepository;

    /**
     * 게시글 생성
     */
    @Transactional
    public PostDto createPost(Long authorId, CreatePostRequest request) {
        log.info("=== PostService.createPost 시작 ===");
        log.info("입력 매개변수: authorId={} (User ID)", authorId);

        try {
            // 🔥 CommunityProfile 조회 (User 정보 포함)
            log.info("🔄 CommunityProfile 조회: userId={}", authorId);
            CommunityProfile communityProfile = communityProfileRepository.findByUserId(authorId)
                .orElseThrow(() -> new CommunityProfileNotFoundException(authorId));
            log.info("✅ CommunityProfile 조회 성공: {}", communityProfile.getDisplayName());

            // 🔥 CommunityProfile에서 User 가져오기
            User user = communityProfile.getUser();
            if (user == null) {
                throw new CommunityProfileNotFoundException("사용자 정보가 연결되어 있지 않습니다: " + authorId);
            }
            log.info("✅ User 정보 확인: {} ({})", user.getName(), user.getUserId());

            // 🔥 Post 엔티티 생성 - 세 개 필드 모두 명확하게 설정
            log.info("🔄 Post 엔티티 생성 중...");
            Post post = Post.builder()
                    .author(communityProfile)           // author_id (기존 호환성)
                    .communityProfile(communityProfile) // community_profile_id (명확한 참조)
                    .user(user)                        // user_id (직접 참조)
                    .content(request.getContent())
                    .imageUrl(request.getImageUrl())
                    .jobCategory(request.getJobCategory())
                    .topicCategory(request.getTopicCategory())
                    .status(Post.PostStatus.valueOf(request.getStatus() != null ? request.getStatus() : "PUBLISHED"))
                    .build();

            log.info("🔄 Post 저장 중...");
            Post savedPost = postRepository.save(post);
            log.info("✅ Post 저장 완료: postId={}", savedPost.getId());

            // 해시태그 처리
            if (request.getHashtags() != null && !request.getHashtags().isEmpty()) {
                log.info("🔄 해시태그 저장 중: {}", request.getHashtags());
                saveHashtags(savedPost, request.getHashtags());
            }

            // 발행된 게시글인 경우 작성자의 게시글 수 증가
            if (savedPost.getStatus() == Post.PostStatus.PUBLISHED) {
                log.info("🔄 발행된 게시글이므로 포스트 수 증가");
                communityProfile.incrementPostsCount();
                communityProfileRepository.save(communityProfile);
            }

            log.info("✅ 게시글 생성 완료: postId={}", savedPost.getId());
            return convertToDto(savedPost, authorId);

        } catch (CommunityProfileNotFoundException e) {
            log.error("❌ PostService.createPost - 커뮤니티 프로필 오류:", e);
            throw e;
        } catch (Exception e) {
            log.error("❌ PostService.createPost - 예상치 못한 오류:", e);
            throw new RuntimeException("게시글 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 전체 게시글 조회 (피드)
     */
    public Page<PostDto> getAllPosts(Long currentUserId, Pageable pageable) {
        log.info("전체 게시글 조회: currentUserId={}", currentUserId);

        Page<Post> posts = postRepository.findByStatusOrderByCreatedAtDesc(Post.PostStatus.PUBLISHED, pageable);
        return posts.map(post -> convertToDto(post, currentUserId));
    }

    /**
     * 팔로잉 사용자들의 게시글 조회
     */
    public Page<PostDto> getFollowingPosts(Long currentUserId, Pageable pageable) {
        log.info("🔍 팔로잉 게시글 조회: currentUserId={}", currentUserId);

        // 🔥 수정: currentUserId로 CommunityProfile 조회 후 해당 ID 사용
        CommunityProfile currentUserProfile = communityProfileRepository.findByUserId(currentUserId)
            .orElseThrow(() -> new CommunityProfileNotFoundException(currentUserId));

        // 🔥 수정: CommunityProfile ID로 팔로잉 목록 조회
        List<Long> followingIds = followRepository.findFollowingIdsByFollowerId(currentUserProfile.getId());
        
        log.info("✅ 팔로잉 목록: userId={}, profileId={}, followingCount={}", 
                currentUserId, currentUserProfile.getId(), followingIds.size());
        
        if (followingIds.isEmpty()) {
            log.info("📭 팔로잉한 사용자가 없습니다.");
            return Page.empty();
        }

        Page<Post> posts = postRepository.findFollowingPosts(followingIds, Post.PostStatus.PUBLISHED, pageable);
        log.info("✅ 팔로잉 게시글 조회 결과: {}개", posts.getTotalElements());
        
        return posts.map(post -> convertToDto(post, currentUserId));
    }

    /**
     * 카테고리별 게시글 조회
     */
    public Page<PostDto> getPostsByCategory(String category, Long currentUserId, Pageable pageable) {
        log.info("카테고리별 게시글 조회: category={}", category);

        Page<Post> posts;
        
        // 직무 카테고리인지 주제 카테고리인지 확인
        if (isJobCategory(category)) {
            posts = postRepository.findByJobCategoryAndStatusOrderByCreatedAtDesc(category, Post.PostStatus.PUBLISHED, pageable);
        } else {
            posts = postRepository.findByTopicCategoryAndStatusOrderByCreatedAtDesc(category, Post.PostStatus.PUBLISHED, pageable);
        }

        return posts.map(post -> convertToDto(post, currentUserId));
    }

    /**
     * 게시글 검색
     */
    public Page<PostDto> searchPosts(String keyword, Long currentUserId, Pageable pageable) {
        log.info("게시글 검색: keyword={}", keyword);

        Page<Post> posts = postRepository.searchPosts(keyword, Post.PostStatus.PUBLISHED, pageable);
        return posts.map(post -> convertToDto(post, currentUserId));
    }

    private void saveHashtags(Post post, List<String> hashtags) {
        List<PostHashtag> postHashtags = hashtags.stream()
            .map(hashtag -> {
                String cleanedHashtag = hashtag.startsWith("#") ? hashtag : "#" + hashtag;
                return new PostHashtag(post, cleanedHashtag);
            })
            .collect(Collectors.toList());
        
        postHashtagRepository.saveAll(postHashtags);
    }

    private boolean isJobCategory(String category) {
        // 직무 카테고리 목록
        List<String> jobCategories = List.of(
            "management", "design", "dev", "marketing", "sales",
            "education", "operations", "logistics", "public", "special"
        );
        return jobCategories.contains(category);
    }

    public PostDto convertToDto(Post post, Long currentUserId) {
        log.debug("🔄 PostDto 변환 시작: postId={}, currentUserId={}", post.getId(), currentUserId);
        
        // 해시태그 목록
        List<String> hashtags = postHashtagRepository.findByPost(post)
            .stream()
            .map(PostHashtag::getHashtag)
            .collect(Collectors.toList());

        // 🔥 작성자 정보 - 세 개 필드 모두 활용
        CommunityProfile author = post.getAuthor(); // 또는 post.getCommunityProfile()
        User user = post.getUser();
        
        // 🔥 프로필 이미지가 없으면 기본 이미지 사용
        String avatarUrl = author.getProfileImageUrl();
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            avatarUrl = "/placeholder_person.svg";
        }
        
        PostDto.AuthorDto authorDto = PostDto.AuthorDto.builder()
            .id(user.getId())  // 🔥 User ID 사용
            .name(author.getDisplayName())
            .avatar(avatarUrl)  // 🔥 기본 이미지 처리된 URL 사용
            .jobTitle(author.getJobTitle())
            .build();

        // 현재 사용자의 좋아요/북마크 상태 - 더 안전한 확인
        Boolean likedByMe = false;
        Boolean bookmarkedByMe = false;
        
        if (currentUserId != null) {
            try {
                Optional<CommunityProfile> currentUserProfile = communityProfileRepository.findByUserId(currentUserId);
                if (currentUserProfile.isPresent()) {
                    CommunityProfile profile = currentUserProfile.get();
                    
                    // 🔥 좋아요 상태 - 더 정확한 체크
                    likedByMe = postLikeRepository.existsByPostAndUser(post, profile);
                    log.info("🔍 [DTO] 좋아요 상태 확인: postId={}, userId={}, isLiked={}", 
                            post.getId(), currentUserId, likedByMe);
                    
                    bookmarkedByMe = postBookmarkRepository.existsByPostAndUser(post, profile);
                    
                    // 작성자 팔로우 상태
                    if (!currentUserId.equals(user.getId())) {  // 🔥 User ID로 비교
                        boolean isFollowing = followRepository.existsByFollowerAndFollowing(
                            profile, author);
                        authorDto.setIsFollowing(isFollowing);
                    }
                }
            } catch (Exception e) {
                log.warn("⚠️ 사용자 상태 확인 중 오류 발생: userId={}, postId={}", currentUserId, post.getId(), e);
                // 오류 발생 시 기본값 유지
            }
        }

        // 🔥 실제 DB 카운트와 비교 검증 (개발 환경에서만)
        long actualLikesCount = postLikeRepository.countByPost(post);
        if (actualLikesCount != post.getLikesCount()) {
            log.warn("⚠️ 좋아요 카운트 불일치 감지: postId={}, entity={}, db={}", 
                    post.getId(), post.getLikesCount(), actualLikesCount);
            // 실제 DB 값으로 응답
            post.setLikesCount((int) actualLikesCount);
        }

        PostDto dto = PostDto.builder()
            .id(post.getId())
            .content(post.getContent())
            .imageUrl(post.getImageUrl())
            .jobCategory(post.getJobCategory())
            .topicCategory(post.getTopicCategory())
            .status(post.getStatus().name())
            .likesCount(post.getLikesCount())
            .commentsCount(post.getCommentsCount())
            .bookmarksCount(post.getBookmarksCount())
            .createdAt(post.getCreatedAt())
            .updatedAt(post.getUpdatedAt())
            .author(authorDto)
            .hashtags(hashtags)
            .likedByMe(likedByMe)
            .bookmarkedByMe(bookmarkedByMe)
            .timeAgo(calculateTimeAgo(post.getCreatedAt()))
            .build();
            
        log.info("✅ [DTO] PostDto 변환 완료: postId={}, likedByMe={}, likesCount={}", 
                post.getId(), likedByMe, post.getLikesCount());
        
        return dto;
    }

    /**
     * 사용자별 게시글 조회
     */
    public Page<PostDto> getUserPosts(Long userId, String status, Long currentUserId, Pageable pageable) {
        log.info("🔍 사용자별 게시글 조회: userId={}, status={}", userId, status);

        CommunityProfile userProfile = communityProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new CommunityProfileNotFoundException(userId));

        Page<Post> posts;
        if ("PUBLISHED".equals(status)) {
            posts = postRepository.findByAuthorAndStatusOrderByCreatedAtDesc(userProfile, Post.PostStatus.PUBLISHED, pageable);
        } else if ("DRAFT".equals(status)) {
            posts = postRepository.findByAuthorAndStatusOrderByCreatedAtDesc(userProfile, Post.PostStatus.DRAFT, pageable);
        } else {
            // 전체 조회
            posts = postRepository.findByAuthorOrderByCreatedAtDesc(userProfile, pageable);
        }

        log.info("✅ 사용자 게시글 조회 완료: userId={}, totalPosts={}", userId, posts.getTotalElements());

        return posts.map(post -> convertToDto(post, currentUserId));
    }

    /**
     * 특정 게시글 조회
     */
    public Optional<PostDto> getPostById(Long postId, Long currentUserId) {
        log.info("🔍 게시글 조회: postId={}", postId);

        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            log.warn("⚠️ 게시글을 찾을 수 없음: postId={}", postId);
        } else {
            log.info("✅ 게시글 조회 성공: postId={}", postId);
        }
        
        return post.map(p -> convertToDto(p, currentUserId));
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public PostDto updatePost(Long postId, Long authorId, CreatePostRequest request) {
        log.info("🔄 게시글 수정: postId={}, authorId={} (User ID)", postId, authorId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // 🔥 작성자 확인 - User ID로 비교
        if (!post.getUser().getId().equals(authorId)) {
            log.error("❌ 게시글 수정 권한 없음: postId={}, requestUserId={}, actualUserId={}",
                    postId, authorId, post.getUser().getId());
            throw new PostAccessDeniedException(postId);
        }

        // 🔥 기존 상태 저장
        Post.PostStatus oldStatus = post.getStatus();

        // 🔥 부분 업데이트 - null이 아닌 값만 업데이트
        if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            post.setContent(request.getContent().trim());
        }

        if (request.getImageUrl() != null) {
            post.setImageUrl(request.getImageUrl());
        }

        if (request.getJobCategory() != null) {
            post.setJobCategory(request.getJobCategory());
            // jobCategory가 설정되면 topicCategory는 null로
            post.setTopicCategory(null);
        }

        if (request.getTopicCategory() != null) {
            post.setTopicCategory(request.getTopicCategory());
            // topicCategory가 설정되면 jobCategory는 null로
            post.setJobCategory(null);
        }

        // 🔥 상태 변경 처리
        if (request.getStatus() != null) {
            try {
                Post.PostStatus newStatus = Post.PostStatus.valueOf(request.getStatus());
                post.setStatus(newStatus);

                // 상태 변경에 따른 작성자의 게시글 수 업데이트
                CommunityProfile author = post.getAuthor();
                if (oldStatus == Post.PostStatus.DRAFT && newStatus == Post.PostStatus.PUBLISHED) {
                    author.incrementPostsCount();
                    log.info("📈 게시글 발행으로 포스트 수 증가: {}", author.getPostsCount());
                } else if (oldStatus == Post.PostStatus.PUBLISHED && newStatus == Post.PostStatus.DRAFT) {
                    author.decrementPostsCount();
                    log.info("📉 게시글 임시저장으로 포스트 수 감소: {}", author.getPostsCount());
                }
                communityProfileRepository.save(author);
            } catch (IllegalArgumentException e) {
                log.error("❌ 잘못된 게시글 상태: {}", request.getStatus());
                throw new RuntimeException("잘못된 게시글 상태입니다: " + request.getStatus());
            }
        }

        Post savedPost = postRepository.save(post);

        // 🔥 해시태그 업데이트 - 해시태그가 제공된 경우에만
        if (request.getHashtags() != null) {
            // 기존 해시태그 삭제
            postHashtagRepository.deleteByPost(post);
            // 새 해시태그 저장
            if (!request.getHashtags().isEmpty()) {
                saveHashtags(savedPost, request.getHashtags());
            }
        }

        log.info("✅ 게시글 수정 완료: postId={}", savedPost.getId());
        return convertToDto(savedPost, authorId);
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void deletePost(Long postId, Long authorId) {
        log.info("🗑️ 게시글 삭제: postId={}, authorId={} (User ID)", postId, authorId);

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));

        // 🔥 작성자 확인 - User ID로 비교
        if (!post.getUser().getId().equals(authorId)) {
            log.error("❌ 게시글 삭제 권한 없음: postId={}, requestUserId={}, actualUserId={}", 
                    postId, authorId, post.getUser().getId());
            throw new PostAccessDeniedException(postId);
        }

        // 발행된 게시글인 경우 작성자의 게시글 수 감소
        CommunityProfile author = post.getAuthor();
        if (post.getStatus() == Post.PostStatus.PUBLISHED) {
            author.decrementPostsCount();
            communityProfileRepository.save(author);
            log.info("📉 게시글 삭제로 포스트 수 감소: {}", author.getPostsCount());
        }

        // 관련 데이터 삭제 (ON DELETE CASCADE로 처리되지만 명시적으로 삭제)
        postHashtagRepository.deleteByPost(post);
        postLikeRepository.deleteByPost(post);
        postBookmarkRepository.deleteByPost(post);
        
        // 게시글 삭제
        postRepository.delete(post);

        log.info("✅ 게시글 삭제 완료: postId={}", postId);
    }

    private String calculateTimeAgo(LocalDateTime createdAt) {
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

    public Page<PostDto> getBookmarkedPosts(Long userId, Pageable pageable) {
        CommunityProfile profile = communityProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new CommunityProfileNotFoundException(userId));

        Page<PostBookmark> bookmarks = postBookmarkRepository.findByUserOrderByCreatedAtDesc(profile, pageable);

        return bookmarks.map(bookmark -> convertToDto(bookmark.getPost(), userId));
    }

}

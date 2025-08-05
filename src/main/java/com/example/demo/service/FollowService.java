package com.example.demo.service;

import com.example.demo.dto.FollowDto;
import com.example.demo.dto.FollowStatusWithStatsDto;
import com.example.demo.dto.FollowToggleRequest;
import com.example.demo.dto.FollowToggleResponse;
import com.example.demo.entity.CommunityProfile;
import com.example.demo.entity.Follow;
import com.example.demo.entity.User;
import com.example.demo.repository.CommunityProfileRepository;
import com.example.demo.repository.FollowRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final CommunityProfileRepository communityProfileRepository;
    private final UserRepository userRepository;

    /**
     * 팔로우 토글 (팔로우/언팔로우)
     */
    @Transactional
    public FollowToggleResponse toggleFollow(Long followerId, Long followingId) {
        log.info("=== 팔로우 토글 시작 (User ID 기준) ===");
        log.info("Follower User ID: {}, Following User ID: {}", followerId, followingId);

        // 입력값 검증
        if (followerId == null || followingId == null) {
            throw new RuntimeException("팔로워 ID와 팔로잉 ID는 필수입니다");
        }

        if (followerId.equals(followingId)) {
            throw new RuntimeException("자기 자신을 팔로우할 수 없습니다");
        }

        // 🔥 수정: User ID로 CommunityProfile 조회
        log.info("📊 CommunityProfile 조회 시도 (User ID 기준)...");

        Optional<CommunityProfile> followerOpt = communityProfileRepository.findByUserId(followerId);
        Optional<CommunityProfile> followingOpt = communityProfileRepository.findByUserId(followingId);

        log.info("조회 결과: follower={}, following={}",
                followerOpt.isPresent() ? "FOUND" : "NOT_FOUND",
                followingOpt.isPresent() ? "FOUND" : "NOT_FOUND");

        // 🔥 프로필이 없으면 오류 발생 (커뮤니티 프로필은 유저가 직접 생성해야 함)
        if (followerOpt.isEmpty()) {
            log.error("❌ 팔로워의 커뮤니티 프로필이 존재하지 않습니다: User ID={}", followerId);
            throw new RuntimeException("커뮤니티 프로필을 먼저 생성해주세요.");
        }

        if (followingOpt.isEmpty()) {
            log.error("❌ 팔로우 대상의 커뮤니티 프로필이 존재하지 않습니다: User ID={}", followingId);
            throw new RuntimeException("해당 사용자의 커뮤니티 프로필이 존재하지 않습니다.");
        }

        CommunityProfile follower = followerOpt.get();
        CommunityProfile following = followingOpt.get();

        log.info("✅ 프로필 조회 성공:");
        log.info("  팔로워: {} (Profile ID: {}, User ID: {})",
                follower.getDisplayName(), follower.getId(), follower.getUser().getId());
        log.info("  팔로잉 대상: {} (Profile ID: {}, User ID: {})",
                following.getDisplayName(), following.getId(), following.getUser().getId());

        // 팔로우 허용 여부 확인
        if (!following.getAllowFollow()) {
            throw new RuntimeException("해당 사용자는 팔로우를 허용하지 않습니다");
        }

        // 기존 팔로우 관계 확인
        Optional<Follow> existingFollow = followRepository.findByFollowerAndFollowing(follower, following);
        log.info("기존 팔로우 관계: {}", existingFollow.isPresent() ? "EXISTS" : "NOT_EXISTS");

        boolean isNowFollowing;
        if (existingFollow.isPresent()) {
            // 언팔로우
            log.info("🔄 언팔로우 처리 중...");
            followRepository.delete(existingFollow.get());
            follower.decrementFollowingCount();
            following.decrementFollowersCount();
            isNowFollowing = false;
            log.info("✅ 언팔로우 완료");
        } else {
            // 팔로우
            log.info("🔄 팔로우 처리 중...");
            Follow newFollow = new Follow(follower, following);
            followRepository.save(newFollow);
            follower.incrementFollowingCount();
            following.incrementFollowersCount();
            isNowFollowing = true;
            log.info("✅ 팔로우 완료");
        }

        // 프로필 업데이트
        communityProfileRepository.save(follower);
        communityProfileRepository.save(following);

        FollowToggleResponse response = FollowToggleResponse.builder()
                .success(true)
                .following(isNowFollowing)
                .message(isNowFollowing ? "팔로우했습니다" : "언팔로우했습니다")
                .followersCount(following.getFollowersCount())
                .followingCount(follower.getFollowingCount())
                .build();

        log.info("=== 팔로우 토글 완료 ===");
        log.info("응답: success={}, following={}", response.getSuccess(), response.getFollowing());

        return response;
    }



    /**
     * 팔로우 상태 확인
     */
    public boolean checkFollowStatus(Long followerId, Long followingId) {
        log.info("팔로우 상태 확인: followerId={}, followingId={}", followerId, followingId);

        if (followerId.equals(followingId)) {
            return false; // 자기 자신은 팔로우 상태가 아님
        }

        Optional<CommunityProfile> follower = communityProfileRepository.findByUserId(followerId);
        Optional<CommunityProfile> following = communityProfileRepository.findByUserId(followingId);

        if (follower.isEmpty() || following.isEmpty()) {
            return false;
        }

        return followRepository.existsByFollowerAndFollowing(follower.get(), following.get());
    }

    /**
     * 팔로우 상태와 통계 정보 조회 (모달용)
     */
    public FollowStatusWithStatsDto getFollowStatusWithStats(Long followerId, Long followingId) {
        log.info("🔍 팔로우 상태 및 통계 조회: followerId={}, followingId={}", followerId, followingId);

        // 자기 자신인 경우
        if (followerId.equals(followingId)) {
            throw new RuntimeException("자기 자신의 팔로우 상태는 조회할 수 없습니다");
        }

        // 프로필 조회
        Optional<CommunityProfile> followerOpt = communityProfileRepository.findByUserId(followerId);
        Optional<CommunityProfile> followingOpt = communityProfileRepository.findByUserId(followingId);

        if (followerOpt.isEmpty()) {
            throw new RuntimeException("팔로워 프로필을 찾을 수 없습니다: " + followerId);
        }

        if (followingOpt.isEmpty()) {
            throw new RuntimeException("대상 프로필을 찾을 수 없습니다: " + followingId);
        }

        CommunityProfile follower = followerOpt.get();
        CommunityProfile following = followingOpt.get();

        // 팔로우 상태 확인
        boolean isFollowing = followRepository.existsByFollowerAndFollowing(follower, following);
        
        // 상호 팔로우 확인
        boolean isMutualFollow = false;
        if (isFollowing) {
            isMutualFollow = followRepository.existsByFollowerAndFollowing(following, follower);
        }

        // 팔로우 가능 여부 확인
        boolean canFollow = following.getAllowFollow() && following.getIsPublic();

        FollowStatusWithStatsDto result = FollowStatusWithStatsDto.builder()
            .isFollowing(isFollowing)
            .isMutualFollow(isMutualFollow)
            .canFollow(canFollow)
            .followersCount(following.getFollowersCount())
            .followingCount(following.getFollowingCount())
            .postsCount(following.getPostsCount())
            .build();

        log.info("✅ 팔로우 상태 조회 완료: isFollowing={}, isMutualFollow={}, canFollow={}", 
            isFollowing, isMutualFollow, canFollow);

        return result;
    }

    /**
     * 팔로잉 목록 조회
     */
    public Page<FollowDto> getFollowingList(Long userId, Long currentUserId, Pageable pageable) {
        log.info("팔로잉 목록 조회: userId={}", userId);

        CommunityProfile user = communityProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        Page<Follow> follows = followRepository.findByFollowerOrderByCreatedAtDesc(user, pageable);
        
        return follows.map(follow -> convertToFollowDto(follow, currentUserId, "following"));
    }

    /**
     * 팔로워 목록 조회
     */
    public Page<FollowDto> getFollowersList(Long userId, Long currentUserId, Pageable pageable) {
        log.info("팔로워 목록 조회: userId={}", userId);

        CommunityProfile user = communityProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        Page<Follow> follows = followRepository.findByFollowingOrderByCreatedAtDesc(user, pageable);
        
        return follows.map(follow -> convertToFollowDto(follow, currentUserId, "follower"));
    }

    /**
     * 팔로우 추천 사용자 조회
     */
    public Page<FollowDto.UserInfoDto> getFollowRecommendations(Long userId, Pageable pageable) {
        log.info("팔로우 추천 조회: userId={}", userId);

        // 실제로는 팔로우하지 않은 사용자 중 인기 사용자들을 추천하는 로직
        // 간단하게 인기 프로필들을 반환하도록 구현
        Page<CommunityProfile> recommendations = communityProfileRepository.findTopProfilesByFollowers(pageable);
        
        return recommendations.map(profile -> {
            FollowDto.UserInfoDto userInfo = FollowDto.UserInfoDto.builder()
                .id(profile.getId())
                .displayName(profile.getDisplayName())
                .profileImageUrl(profile.getProfileImageUrl())
                .jobTitle(profile.getJobTitle())
                .company(profile.getCompany())
                .followersCount(profile.getFollowersCount())
                .followingCount(profile.getFollowingCount())
                .postsCount(profile.getPostsCount())
                .build();

            // 현재 사용자의 팔로우 상태 설정
            if (userId != null && !userId.equals(profile.getUser().getId())) {
                Optional<CommunityProfile> currentUserProfile = communityProfileRepository.findByUserId(userId);
                if (currentUserProfile.isPresent()) {
                    boolean isFollowing = followRepository.existsByFollowerAndFollowing(
                        currentUserProfile.get(), profile);
                    userInfo.setIsFollowing(isFollowing);
                }
            }

            return userInfo;
        });
    }

    private FollowDto convertToFollowDto(Follow follow, Long currentUserId, String type) {
        CommunityProfile targetProfile = "following".equals(type) ? follow.getFollowing() : follow.getFollower();

        FollowDto.UserInfoDto userInfo = FollowDto.UserInfoDto.builder()
                .id(targetProfile.getId())                    // Profile ID
                .userId(targetProfile.getUser().getId())      // 🔥 User ID 추가
                .displayName(targetProfile.getDisplayName())
                .profileImageUrl(targetProfile.getProfileImageUrl())
                .jobTitle(targetProfile.getJobTitle())
                .company(targetProfile.getCompany())
                .followersCount(targetProfile.getFollowersCount())
                .followingCount(targetProfile.getFollowingCount())
                .postsCount(targetProfile.getPostsCount())
                .build();

        // 현재 사용자의 팔로우 상태 설정
        if (currentUserId != null && !currentUserId.equals(targetProfile.getUser().getId())) {
            Optional<CommunityProfile> currentUserProfile = communityProfileRepository.findByUserId(currentUserId);
            if (currentUserProfile.isPresent()) {
                boolean isFollowing = followRepository.existsByFollowerAndFollowing(
                    currentUserProfile.get(), targetProfile);
                userInfo.setIsFollowing(isFollowing);
            }
        }

        FollowDto followDto = FollowDto.builder()
            .id(follow.getId())
            .createdAt(follow.getCreatedAt())
            .build();

        if ("following".equals(type)) {
            followDto.setFollowing(userInfo);
        } else {
            followDto.setFollower(userInfo);
        }

        return followDto;
    }
}

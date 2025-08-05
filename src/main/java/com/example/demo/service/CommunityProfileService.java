package com.example.demo.service;

import com.example.demo.dto.CommunityProfileDto;
import com.example.demo.entity.CommunityProfile;
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

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityProfileService {

    private final CommunityProfileRepository communityProfileRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    /**
     * 커뮤니티 프로필 조회 (사용자 ID로)
     */
    public Optional<CommunityProfileDto> getProfileByUserId(Long userId, Long currentUserId) {
        log.info("커뮤니티 프로필 조회 시도: userId={}", userId);
        
        Optional<CommunityProfile> profileOpt = communityProfileRepository.findByUserId(userId);
        
        if (profileOpt.isEmpty()) {
            log.warn("커뮤니티 프로필을 찾을 수 없습니다: userId={}", userId);
            return Optional.empty();
        }
        
        CommunityProfile profile = profileOpt.get();
        CommunityProfileDto dto = convertToDto(profile);
        
        // 추가 정보 설정
        if (currentUserId != null) {
            dto.setIsOwner(currentUserId.equals(userId));
            
            if (!dto.getIsOwner()) {
                // 팔로우 상태 확인
                Optional<CommunityProfile> currentUserProfile = communityProfileRepository.findByUserId(currentUserId);
                if (currentUserProfile.isPresent()) {
                    boolean isFollowing = followRepository.existsByFollowerAndFollowing(
                        currentUserProfile.get(), profile);
                    dto.setIsFollowing(isFollowing);
                    
                    // 상호 팔로우 확인
                    boolean isMutualFollow = followRepository.isMutualFollow(currentUserId, userId);
                    dto.setIsMutualFollow(isMutualFollow);
                }
            }
        }
        
        return Optional.of(dto);
    }

    /**
     * 커뮤니티 프로필 생성 또는 업데이트
     */
    @Transactional
    public CommunityProfileDto createOrUpdateProfile(Long userId, CommunityProfileDto profileDto) {
        log.info("커뮤니티 프로필 생성/업데이트 시도: userId={}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        Optional<CommunityProfile> existingProfileOpt = communityProfileRepository.findByUser(user);
        
        CommunityProfile profile;
        if (existingProfileOpt.isPresent()) {
            // 기존 프로필 업데이트
            profile = existingProfileOpt.get();
            updateProfileFields(profile, profileDto);
            log.info("커뮤니티 프로필 업데이트 완료: userId={}", userId);
        } else {
            // 새 프로필 생성
            profile = createNewProfile(user, profileDto);
            log.info("커뮤니티 프로필 생성 완료: userId={}", userId);
        }
        
        CommunityProfile savedProfile = communityProfileRepository.save(profile);
        return convertToDto(savedProfile);
    }

    /**
     * 프로필 검색
     */
    public Page<CommunityProfileDto> searchProfiles(String keyword, Long currentUserId, Pageable pageable) {
        log.info("프로필 검색: keyword={}", keyword);
        
        Page<CommunityProfile> profiles = communityProfileRepository.findByDisplayNameContaining(keyword, pageable);
        
        return profiles.map(this::convertToDto);
    }

    /**
     * 인기 프로필 조회
     */
    public Page<CommunityProfileDto> getPopularProfiles(Long currentUserId, Pageable pageable) {
        log.info("인기 프로필 조회");
        
        Page<CommunityProfile> profiles = communityProfileRepository.findTopProfilesByFollowers(pageable);
        
        return profiles.map(this::convertToDto);
    }

    private CommunityProfile createNewProfile(User user, CommunityProfileDto dto) {
        return CommunityProfile.builder()
            .user(user)
            .displayName(dto.getDisplayName())
            .bio(dto.getBio())
            .jobTitle(dto.getJobTitle())
            .company(dto.getCompany())
            .location(dto.getLocation())
            .profileImageUrl(dto.getProfileImageUrl())
            .coverImageUrl(dto.getCoverImageUrl())
            .isPublic(dto.getIsPublic() != null ? dto.getIsPublic() : true)
            .allowFollow(dto.getAllowFollow() != null ? dto.getAllowFollow() : true)
            .build();
    }

    private void updateProfileFields(CommunityProfile profile, CommunityProfileDto dto) {
        if (dto.getDisplayName() != null) {
            profile.setDisplayName(dto.getDisplayName());
        }
        if (dto.getBio() != null) {
            profile.setBio(dto.getBio());
        }
        if (dto.getJobTitle() != null) {
            profile.setJobTitle(dto.getJobTitle());
        }
        if (dto.getCompany() != null) {
            profile.setCompany(dto.getCompany());
        }
        if (dto.getLocation() != null) {
            profile.setLocation(dto.getLocation());
        }
        if (dto.getProfileImageUrl() != null) {
            profile.setProfileImageUrl(dto.getProfileImageUrl());
        }
        if (dto.getCoverImageUrl() != null) {
            profile.setCoverImageUrl(dto.getCoverImageUrl());
        }
        if (dto.getIsPublic() != null) {
            profile.setIsPublic(dto.getIsPublic());
        }
        if (dto.getAllowFollow() != null) {
            profile.setAllowFollow(dto.getAllowFollow());
        }
    }

    private CommunityProfileDto convertToDto(CommunityProfile profile) {
        // 🔥 프로필 이미지가 없으면 기본 이미지 사용
        String profileImageUrl = profile.getProfileImageUrl();
        if (profileImageUrl == null || profileImageUrl.trim().isEmpty()) {
            profileImageUrl = "/placeholder_person.svg";
        }

        return CommunityProfileDto.builder()
            .id(profile.getId())
            .userId(profile.getUser().getId())
            .displayName(profile.getDisplayName())
            .bio(profile.getBio())
            .jobTitle(profile.getJobTitle())
            .company(profile.getCompany())
            .location(profile.getLocation())
            .profileImageUrl(profileImageUrl)  // 🔥 기본 이미지 처리된 URL 사용
            .coverImageUrl(profile.getCoverImageUrl())
            .isPublic(profile.getIsPublic())
            .allowFollow(profile.getAllowFollow())
            .postsCount(profile.getPostsCount())
            .followersCount(profile.getFollowersCount())
            .followingCount(profile.getFollowingCount())
            .createdAt(profile.getCreatedAt())
            .updatedAt(profile.getUpdatedAt())
            .build();
    }
}

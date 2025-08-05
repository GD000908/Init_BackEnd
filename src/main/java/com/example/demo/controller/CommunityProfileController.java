package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CommunityProfileDto;
import com.example.demo.service.CommunityProfileService;
import com.example.demo.repository.UserRepository;
import com.example.demo.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/community/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class CommunityProfileController {

    private final CommunityProfileService communityProfileService;
    private final UserRepository userRepository;

    /**
     * 🔥 프로필 이미지 업로드 전용 엔드포인트
     */
    @PostMapping("/{userId}/upload-avatar")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam("image") org.springframework.web.multipart.MultipartFile file) {
        
        log.info("🖼️ 프로필 이미지 업로드 요청: userId={}, fileName={}", userId, file.getOriginalFilename());
        
        try {
            // 파일 유효성 검사
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.<Map<String, String>>builder()
                    .success(false)
                    .message("업로드할 파일이 없습니다")
                    .build());
            }

            // 파일 크기 체크 (2MB - 프로필 이미지는 더 작게)
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(ApiResponse.<Map<String, String>>builder()
                    .success(false)
                    .message("프로필 이미지는 2MB 이하여야 합니다")
                    .build());
            }

            // 파일 형식 체크
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(ApiResponse.<Map<String, String>>builder()
                    .success(false)
                    .message("이미지 파일만 업로드 가능합니다")
                    .build());
            }

            // 사용자 존재 여부 확인
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.badRequest().body(ApiResponse.<Map<String, String>>builder()
                    .success(false)
                    .message("존재하지 않는 사용자입니다")
                    .build());
            }

            // 업로드 디렉토리 생성
            String uploadPath = "uploads";
            String datePath = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            java.nio.file.Path uploadDir = java.nio.file.Paths.get(uploadPath, "profiles", datePath);
            
            if (!java.nio.file.Files.exists(uploadDir)) {
                java.nio.file.Files.createDirectories(uploadDir);
                log.info("📁 프로필 이미지 디렉토리 생성: {}", uploadDir.toAbsolutePath());
            }

            // 파일명 생성 (사용자ID_UUID + 확장자)
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String newFilename = "profile_" + userId + "_" + java.util.UUID.randomUUID().toString() + extension;
            
            // 파일 저장
            java.nio.file.Path filePath = uploadDir.resolve(newFilename);
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // URL 생성
            String imageUrl = "/uploads/profiles/" + datePath + "/" + newFilename;
            
            log.info("📸 프로필 이미지 저장 완료: {}", imageUrl);

            Map<String, String> response = new java.util.HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("message", "프로필 이미지 업로드 완료");

            return ResponseEntity.ok(ApiResponse.<Map<String, String>>builder()
                .success(true)
                .data(response)
                .message("프로필 이미지 업로드가 완료되었습니다")
                .build());

        } catch (Exception e) {
            log.error("❌ 프로필 이미지 업로드 실패: userId={}", userId, e);
            return ResponseEntity.internalServerError().body(ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("프로필 이미지 업로드 중 오류가 발생했습니다")
                .build());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return ".jpg";  // 기본 확장자
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return ".jpg";  // 기본 확장자
        }
        return filename.substring(lastDotIndex);
    }

    /**
     * 커뮤니티 프로필 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<CommunityProfileDto>> getProfile(
            @PathVariable Long userId,
            @RequestParam(required = false) Long currentUserId) {
        
        log.info("커뮤니티 프로필 조회 요청: userId={}", userId);
        
        try {
            Optional<CommunityProfileDto> profile = communityProfileService.getProfileByUserId(userId, currentUserId);
            
            if (profile.isEmpty()) {
                log.warn("⚠️ 프로필을 찾을 수 없습니다: userId={}", userId);
                return ResponseEntity.ok(ApiResponse.<CommunityProfileDto>builder()
                    .success(false)
                    .message("프로필을 찾을 수 없습니다")
                    .build());
            }
            
            log.info("✅ 프로필 조회 성공: userId={}, displayName={}", userId, profile.get().getDisplayName());
            return ResponseEntity.ok(ApiResponse.<CommunityProfileDto>builder()
                .success(true)
                .data(profile.get())
                .message("프로필 조회 성공")
                .build());
                
        } catch (Exception e) {
            log.error("❌ 프로필 조회 실패: userId={}", userId, e);
            return ResponseEntity.ok(ApiResponse.<CommunityProfileDto>builder()
                .success(false)
                .message("프로필 조회 중 오류가 발생했습니다: " + e.getMessage())
                .build());
        }
    }

    /**
     * 🔥 프로필 모달용 상세 정보 조회 - 기존 메서드 활용
     * GET /api/community/profile/{userId}/modal
     */
    @GetMapping("/{userId}/modal")
    public ResponseEntity<CommunityProfileDto> getProfileModal(
            @PathVariable Long userId,
            @RequestParam(required = false) Long currentUserId) {
        
        log.info("🔍 프로필 모달 조회 요청: userId={}, currentUserId={}", userId, currentUserId);
        
        try {
            Optional<CommunityProfileDto> profile = communityProfileService.getProfileByUserId(userId, currentUserId);
            
            if (profile.isEmpty()) {
                log.warn("⚠️ 프로필을 찾을 수 없습니다: userId={}", userId);
                return ResponseEntity.notFound().build();
            }
            
            log.info("✅ 프로필 모달 조회 성공: displayName={}", profile.get().getDisplayName());
            
            return ResponseEntity.ok(profile.get());
        } catch (RuntimeException e) {
            log.error("❌ 프로필 모달 조회 실패: userId={}", userId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 커뮤니티 프로필 생성/업데이트
     */
    @PostMapping("/{userId}")
    public ResponseEntity<CommunityProfileDto> createOrUpdateProfile(
            @PathVariable Long userId,
            @RequestBody CommunityProfileDto profileDto) {

        log.info("🔄 커뮤니티 프로필 생성/업데이트 요청: userId={}, displayName={}", userId, profileDto.getDisplayName());
        log.info("📋 요청 데이터: {}", profileDto);

        try {
            CommunityProfileDto savedProfile = communityProfileService.createOrUpdateProfile(userId, profileDto);
            log.info("✅ 커뮤니티 프로필 생성/업데이트 성공: profileId={}", savedProfile.getId());
            log.info("📤 응답 데이터: {}", savedProfile);
            return ResponseEntity.ok(savedProfile);
        } catch (Exception e) {
            log.error("❌ 커뮤니티 프로필 생성/업데이트 실패: userId={}", userId, e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 커뮤니티 프로필 업데이트 (PUT)
     */
    @PutMapping("/{userId}")
    public ResponseEntity<CommunityProfileDto> updateProfile(
            @PathVariable Long userId,
            @RequestBody CommunityProfileDto profileDto) {

        log.info("🔄 커뮤니티 프로필 업데이트 요청: userId={}, displayName={}", userId, profileDto.getDisplayName());
        log.info("📋 요청 데이터: {}", profileDto);

        try {
            CommunityProfileDto updatedProfile = communityProfileService.createOrUpdateProfile(userId, profileDto);
            log.info("✅ 커뮤니티 프로필 업데이트 성공: profileId={}", updatedProfile.getId());
            log.info("📤 응답 데이터: {}", updatedProfile);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            log.error("❌ 커뮤니티 프로필 업데이트 실패: userId={}", userId, e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 프로필 검색
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CommunityProfileDto>> searchProfiles(
            @RequestParam String keyword,
            @RequestParam(required = false) Long currentUserId,
            Pageable pageable) {

        log.info("프로필 검색 요청: keyword={}", keyword);

        Page<CommunityProfileDto> profiles = communityProfileService.searchProfiles(keyword, currentUserId, pageable);
        return ResponseEntity.ok(profiles);
    }

    /**
     * 인기 프로필 조회
     */
    @GetMapping("/popular")
    public ResponseEntity<Page<CommunityProfileDto>> getPopularProfiles(
            @RequestParam(required = false) Long currentUserId,
            Pageable pageable) {

        log.info("인기 프로필 조회 요청");

        Page<CommunityProfileDto> profiles = communityProfileService.getPopularProfiles(currentUserId, pageable);
        return ResponseEntity.ok(profiles);
    }

    /**
     * 디버그: 모든 사용자 조회 (임시용)
     */
    @GetMapping("/debug/users")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("디버그: 모든 사용자 조회");
        List<User> users = userRepository.findAll();
        log.info("총 {} 명의 사용자가 있습니다", users.size());
        users.forEach(user -> log.info("User ID: {}, Email: {}, Name: {}", user.getId(), user.getEmail(), user.getName()));
        return ResponseEntity.ok(users);
    }
}
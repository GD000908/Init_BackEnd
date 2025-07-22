package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.HomePageService;
import com.example.demo.service.JobRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://init-front.vercel.app",
        "https://init-front-git-main-parks-projects-52059b12.vercel.app",
        "https://init-front-8vae2fth4-parks-projects-52059b12.vercel.app"
})

@Slf4j
public class HomePageController {

    private final HomePageService homePageService;
    private final JobRecommendationService jobRecommendationService;

    // 🔥 개선된 공공데이터포털 채용정보 API 호출 엔드포인트
    @PostMapping("/job-recommendations/{userId}")
    public ResponseEntity<List<JobRecommendationDto>> getJobRecommendations(
            @PathVariable Long userId,
            @RequestBody JobRecommendationRequestDto requestDto) {

        try {
            log.info("🔍 공고 추천 요청 - userId: {}, keywords: {}, locations: {}",
                    userId, requestDto.getKeywords(), requestDto.getLocations());

            List<String> jobKeywords = requestDto.getKeywords();
            List<String> locations = requestDto.getLocations();

            List<JobRecommendationDto> recommendations = jobRecommendationService
                    .getPublicJobRecommendations(userId, jobKeywords, locations);

            log.info("✅ 공고 추천 완료 - userId: {}, 추천 개수: {}", userId, recommendations.size());
            return ResponseEntity.ok(recommendations);

        } catch (Exception e) {
            log.error("❌ 공고 추천 실패 - userId: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.ok(List.of()); // 에러 시 빈 리스트 반환
        }
    }

    // Profile endpoints
    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable Long userId) {
        try {
            log.info("🔍 프로필 조회 - userId: {}", userId);
            UserProfileDto profile = homePageService.getUserProfile(userId);
            log.info("✅ 프로필 조회 완료 - userId: {}", userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("❌ 프로필 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDto> updateUserProfile(
            @PathVariable Long userId,
            @RequestBody UserProfileDto dto) {
        try {
            log.info("🔄 프로필 업데이트 - userId: {}", userId);
            UserProfileDto updated = homePageService.createOrUpdateUserProfile(userId, dto);
            log.info("✅ 프로필 업데이트 완료 - userId: {}", userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("❌ 프로필 업데이트 실패 - userId: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Desired conditions endpoints
    @GetMapping("/conditions/{userId}")
    public ResponseEntity<DesiredConditionsDto> getDesiredConditions(@PathVariable Long userId) {
        try {
            log.info("🔍 희망조건 조회 - userId: {}", userId);
            DesiredConditionsDto conditions = homePageService.getDesiredConditions(userId);
            log.info("✅ 희망조건 조회 완료 - userId: {}", userId);
            return ResponseEntity.ok(conditions);
        } catch (Exception e) {
            log.error("❌ 희망조건 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/conditions/{userId}")
    public ResponseEntity<DesiredConditionsDto> updateDesiredConditions(
            @PathVariable Long userId,
            @RequestBody DesiredConditionsDto dto) {
        try {
            log.info("🔄 희망조건 업데이트 - userId: {}", userId);
            DesiredConditionsDto updated = homePageService.createOrUpdateDesiredConditions(userId, dto);
            log.info("✅ 희망조건 업데이트 완료 - userId: {}", userId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("❌ 희망조건 업데이트 실패 - userId: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 🔥 개선된 Application status endpoints
    @GetMapping("/applications/{userId}")
    public ResponseEntity<List<ApplicationStatusDto>> getApplicationStatuses(@PathVariable Long userId) {
        try {
            log.info("🔍 지원현황 조회 - userId: {}", userId);
            List<ApplicationStatusDto> applications = homePageService.getApplicationStatuses(userId);
            log.info("✅ 지원현황 조회 완료 - userId: {}, 개수: {}", userId, applications.size());
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            log.error("❌ 지원현황 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.ok(List.of()); // 에러 시 빈 리스트 반환
        }
    }

    // 🔥 완전히 수정된 일괄 업데이트 엔드포인트 - URL에 userId 포함
    @PutMapping("/applications/batch/{userId}")
    public ResponseEntity<List<ApplicationStatusDto>> updateApplicationStatusesBatch(
            @PathVariable Long userId,  // 🔥 URL에서 userId 받음
            @RequestBody List<ApplicationStatusDto> applications) {

        try {
            log.info("🔄 지원현황 일괄 업데이트 시작 - userId: {}, 요청 개수: {}",
                    userId, applications != null ? applications.size() : 0);

            // 🔥 null 체크
            if (applications == null) {
                log.warn("⚠️ 요청 데이터가 null입니다. - userId: {}", userId);
                return ResponseEntity.badRequest().build();
            }

            // 🔥 모든 DTO에 userId 설정 (빈 리스트라도 userId는 알고 있음)
            applications.forEach(app -> {
                if (app.getUserId() == null) {
                    app.setUserId(userId);
                }
            });

            // 🔥 0개인 경우 로그 추가
            if (applications.isEmpty()) {
                log.info("🗑️ 빈 리스트 요청 감지 - userId: {}, 모든 데이터 삭제 예정", userId);
            }

            // 🔥 서비스 호출 (userId도 함께 전달)
            List<ApplicationStatusDto> updated = homePageService.updateApplicationStatusesBatch(applications, userId);

            log.info("✅ 지원현황 일괄 업데이트 완료 - userId: {}, 요청: {}개, 결과: {}개",
                    userId, applications.size(), updated.size());

            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            log.error("❌ 지원현황 일괄 업데이트 실패 (잘못된 요청) - userId: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("❌ 지원현황 일괄 업데이트 실패 - userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🔥 개선된 Stats endpoint
    @GetMapping("/stats/{userId}")
    public ResponseEntity<HomePageStatsDto> getHomePageStats(@PathVariable Long userId) {
        try {
            log.info("📊 통계 조회 - userId: {}", userId);
            HomePageStatsDto stats = homePageService.getHomePageStats(userId);
            log.info("✅ 통계 조회 완료 - userId: {}, 지원현황: {}개", userId, stats.getTotalApplications());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ 통계 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            // 에러 시 기본 통계 반환
            return ResponseEntity.ok(new HomePageStatsDto(0, 0, 0, 0, 0, 0, 0, 0, new ProfileCompletionDto()));
        }
    }

    // 🔥 개선된 All data endpoint - 홈페이지에 필요한 모든 데이터를 한번에 가져오기
    @GetMapping("/all/{userId}")
    public ResponseEntity<HomePageAllDataDto> getAllHomePageData(@PathVariable Long userId) {
        try {
            log.info("🔍 전체 데이터 조회 - userId: {}", userId);

            HomePageAllDataDto allData = new HomePageAllDataDto();

            // 각 데이터 조회 (에러가 나도 다른 데이터는 계속 로드)
            try {
                allData.setProfile(homePageService.getUserProfile(userId));
            } catch (Exception e) {
                log.warn("⚠️ 프로필 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
                allData.setProfile(null);
            }

            try {
                allData.setConditions(homePageService.getDesiredConditions(userId));
            } catch (Exception e) {
                log.warn("⚠️ 희망조건 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
                allData.setConditions(null);
            }

            // 지원현황은 항상 빈 리스트라도 반환
            try {
                allData.setApplications(homePageService.getApplicationStatuses(userId));
            } catch (Exception e) {
                log.warn("⚠️ 지원현황 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
                allData.setApplications(List.of());
            }

            // 통계는 항상 기본값이라도 반환
            try {
                allData.setStats(homePageService.getHomePageStats(userId));
            } catch (Exception e) {
                log.warn("⚠️ 통계 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
                allData.setStats(new HomePageStatsDto(0, 0, 0, 0, 0, 0, 0, 0, new ProfileCompletionDto()));
            }

            log.info("✅ 전체 데이터 조회 완료 - userId: {}", userId);
            return ResponseEntity.ok(allData);

        } catch (Exception e) {
            log.error("❌ 전체 데이터 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🔥 추가된 헬스체크 엔드포인트
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Home API 서버가 정상적으로 실행 중입니다.");
    }
}
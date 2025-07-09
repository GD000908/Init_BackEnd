package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.HomePageService;
import com.example.demo.service.JobRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class HomePageController {

    private final HomePageService homePageService;
    private final JobRecommendationService jobRecommendationService; // 새로운 서비스로 변경

    // [UPDATED] 공공데이터포털 채용정보 API 호출을 위한 엔드포인트
    @PostMapping("/job-recommendations/{userId}")
    public ResponseEntity<List<JobRecommendationDto>> getJobRecommendations(
            @PathVariable Long userId,
            @RequestBody JobRecommendationRequestDto requestDto) {

        List<String> jobKeywords = requestDto.getKeywords();
        List<String> locations = requestDto.getLocations();

        // 공공데이터포털 API를 사용한 추천 서비스로 변경
        List<JobRecommendationDto> recommendations = jobRecommendationService
                .getPublicJobRecommendations(userId, jobKeywords, locations);

        return ResponseEntity.ok(recommendations);
    }

    // Profile endpoints
    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(homePageService.getUserProfile(userId));
    }

    @PostMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDto> updateUserProfile(
            @PathVariable Long userId,
            @RequestBody UserProfileDto dto) {
        return ResponseEntity.ok(homePageService.createOrUpdateUserProfile(userId, dto));
    }

    // Desired conditions endpoints
    @GetMapping("/conditions/{userId}")
    public ResponseEntity<DesiredConditionsDto> getDesiredConditions(@PathVariable Long userId) {
        return ResponseEntity.ok(homePageService.getDesiredConditions(userId));
    }

    @PostMapping("/conditions/{userId}")
    public ResponseEntity<DesiredConditionsDto> updateDesiredConditions(
            @PathVariable Long userId,
            @RequestBody DesiredConditionsDto dto) {
        return ResponseEntity.ok(homePageService.createOrUpdateDesiredConditions(userId, dto));
    }

    // Application status endpoints
    @GetMapping("/applications/{userId}")
    public ResponseEntity<List<ApplicationStatusDto>> getApplicationStatuses(@PathVariable Long userId) {
        return ResponseEntity.ok(homePageService.getApplicationStatuses(userId));
    }

    @PutMapping("/applications/batch")
    public ResponseEntity<List<ApplicationStatusDto>> updateApplicationStatusesBatch(
            @RequestBody List<ApplicationStatusDto> applications) {
        return ResponseEntity.ok(homePageService.updateApplicationStatusesBatch(applications));
    }

    // Stats endpoint
    @GetMapping("/stats/{userId}")
    public ResponseEntity<HomePageStatsDto> getHomePageStats(@PathVariable Long userId) {
        return ResponseEntity.ok(homePageService.getHomePageStats(userId));
    }

    // All data endpoint - 홈페이지에 필요한 모든 데이터를 한번에 가져오기
    @GetMapping("/all/{userId}")
    public ResponseEntity<HomePageAllDataDto> getAllHomePageData(@PathVariable Long userId) {
        HomePageAllDataDto allData = new HomePageAllDataDto();

        try {
            allData.setProfile(homePageService.getUserProfile(userId));
        } catch (Exception e) {
            allData.setProfile(null);
        }

        try {
            allData.setConditions(homePageService.getDesiredConditions(userId));
        } catch (Exception e) {
            allData.setConditions(null);
        }

        allData.setApplications(homePageService.getApplicationStatuses(userId));
        allData.setStats(homePageService.getHomePageStats(userId));

        return ResponseEntity.ok(allData);
    }
}
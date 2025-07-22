package com.example.demo.controller;

import com.example.demo.dto.PublicJobSearchRequest;
import com.example.demo.dto.PublicRecruitmentResponse;
import com.example.demo.service.PublicJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public-jobs")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://init-front.vercel.app")

@Slf4j
public class PublicJobController {

    private final PublicJobService publicJobService;

    @PostMapping("/search")
    public ResponseEntity<PublicRecruitmentResponse> searchJobs(
            @RequestBody PublicJobSearchRequest request) {

        log.info("🔍 공공 채용정보 검색 요청: {}", request);

        try {
            PublicRecruitmentResponse response = publicJobService.searchJobs(request);
            log.info("✅ 검색 완료 - 총 {}건", response.getTotalCount());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 공공 채용정보 검색 실패", e);
            return ResponseEntity.internalServerError()
                    .body(PublicRecruitmentResponse.builder()
                            .resultCode(500)
                            .resultMsg("검색 중 오류가 발생했습니다: " + e.getMessage())
                            .totalCount(0)
                            .build());
        }
    }
}
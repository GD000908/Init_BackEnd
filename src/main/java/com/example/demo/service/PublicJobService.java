package com.example.demo.service;

import com.example.demo.client.PublicRecruitmentApiClient;
import com.example.demo.dto.PublicJobSearchRequest;
import com.example.demo.dto.PublicRecruitmentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicJobService {

    private final PublicRecruitmentApiClient publicRecruitmentApiClient;

    public PublicRecruitmentResponse searchJobs(PublicJobSearchRequest request) {
        log.info("📡 공공 채용정보 API 호출 시작");

        try {
            PublicRecruitmentResponse response = publicRecruitmentApiClient.searchJobs(request);
            log.info("✅ API 호출 성공 - 결과: {}건", response.getTotalCount());
            return response;

        } catch (Exception e) {
            log.error("❌ 공공 채용정보 API 호출 실패", e);
            throw new RuntimeException("채용정보 검색 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
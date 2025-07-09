package com.example.demo.service;

import com.example.demo.client.PublicRecruitmentApiClient;
import com.example.demo.dto.JobRecommendationDto;
import com.example.demo.dto.PublicJobSearchRequest;
import com.example.demo.dto.PublicRecruitmentResponse;
import com.example.demo.repository.DesiredConditionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobRecommendationService {

    private final PublicRecruitmentApiClient publicApiClient;
    private final JobCodeMappingService codeMapping;
    private final DesiredConditionsRepository desiredConditionsRepository;

    public List<JobRecommendationDto> getPublicJobRecommendations(Long userId, List<String> keywords, List<String> locations) {
        try {
            // 1. 지역명을 코드로 변환
            List<String> regionCodes = codeMapping.convertRegionNamesToCodes(locations);

            // 2. 직무명을 NCS코드로 변환
            List<String> ncsCodes = codeMapping.convertJobNamesToCodes(keywords);

            log.info("Search params - NCS codes: {}, Region codes: {}", ncsCodes, regionCodes);

            // 3. API 검색 요청 구성
            PublicJobSearchRequest searchRequest = PublicJobSearchRequest.builder()
                    .ncsCdLst(ncsCodes)
                    .workRgnLst(regionCodes)
                    .numOfRows(20)
                    .pageNo(1)
                    .build();

            // 4. 공공데이터 API 호출
            PublicRecruitmentResponse response = publicApiClient.searchJobs(searchRequest);

            // 5. 응답 데이터 변환
            return convertToJobRecommendations(response, keywords);

        } catch (Exception e) {
            log.error("Error getting public job recommendations for user {}", userId, e);
            return Collections.emptyList();
        }
    }

    private List<JobRecommendationDto> convertToJobRecommendations(
            PublicRecruitmentResponse response, List<String> originalKeywords) {

        // 🔥 새로운 응답 구조에 맞게 수정
        if (response == null || response.getResult() == null) {
            return Collections.emptyList();
        }

        return response.getResult().stream()
                .map(item -> convertToJobRecommendationDto(item, originalKeywords))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private JobRecommendationDto convertToJobRecommendationDto(
            PublicRecruitmentResponse.PublicJobItem item, List<String> originalKeywords) {

        try {
            return JobRecommendationDto.builder()
                    .id(item.getRecrutPblntSn() != null ? item.getRecrutPblntSn().toString() : UUID.randomUUID().toString())
                    .title(item.getRecrutPbancTtl())
                    .company(item.getInstNm())
                    .location(item.getWorkRgnNmLst() != null ? item.getWorkRgnNmLst() : "전국")
                    .experience("공고 참조")
                    .education(extractEducationFromContent(item.getAplyQlfcCn()))
                    .employmentType(item.getHireTypeNmLst() != null ? item.getHireTypeNmLst() : "정규직")
                    .salary("공고 참조")
                    .deadline(parseDate(item.getPbancEndYmd()))
                    .url(item.getSrcUrl())
                    .keywords(extractKeywords(item, originalKeywords))
                    .postedDate(parseDate(item.getPbancBgngYmd()))
                    .matchScore(calculateMatchScore(item, originalKeywords))
                    .description(buildDescription(item))
                    .requirements(item.getAplyQlfcCn())
                    .benefits(item.getPrefCn())
                    .recruitCount(item.getRecrutNope() != null ? item.getRecrutNope().toString() : "정보 없음")
                    .build();
        } catch (Exception e) {
            log.error("Error converting job item to DTO", e);
            return null;
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            // YYYYMMDD 형식 처리
            if (dateStr.length() == 8) {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr);
        }
        return null;
    }

    private String extractEducationFromContent(String content) {
        if (content == null) return "학력무관";

        if (content.contains("대졸") || content.contains("대학교")) return "대졸 이상";
        if (content.contains("고졸") || content.contains("고등학교")) return "고졸 이상";
        if (content.contains("석사")) return "석사 이상";
        if (content.contains("박사")) return "박사 이상";

        return "학력무관";
    }

    private List<String> extractKeywords(PublicRecruitmentResponse.PublicJobItem item, List<String> originalKeywords) {
        List<String> keywords = new ArrayList<>();

        // 원본 키워드 추가
        if (originalKeywords != null) {
            keywords.addAll(originalKeywords);
        }

        // NCS 분류명에서 키워드 추출
        if (item.getNcsCdNmLst() != null) {
            String[] ncsNames = item.getNcsCdNmLst().split(",");
            for (String name : ncsNames) {
                keywords.add(name.trim());
            }
        }

        return keywords.stream().distinct().limit(5).collect(Collectors.toList());
    }

    private Integer calculateMatchScore(PublicRecruitmentResponse.PublicJobItem item, List<String> keywords) {
        int score = 50; // 기본 점수

        // 키워드 매칭 점수
        if (keywords != null && item.getRecrutPbancTtl() != null) {
            for (String keyword : keywords) {
                if (item.getRecrutPbancTtl().contains(keyword)) {
                    score += 20;
                }
            }
        }

        // NCS 분류 매칭 점수
        if (keywords != null && item.getNcsCdNmLst() != null) {
            for (String keyword : keywords) {
                if (item.getNcsCdNmLst().contains(keyword)) {
                    score += 15;
                }
            }
        }

        // 마감일 임박도
        LocalDate deadline = parseDate(item.getPbancEndYmd());
        if (deadline != null) {
            long daysUntilDeadline = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
            if (daysUntilDeadline <= 7) {
                score += 10;
            }
        }

        return Math.min(score, 100);
    }

    private String buildDescription(PublicRecruitmentResponse.PublicJobItem item) {
        StringBuilder desc = new StringBuilder();

        if (item.getInstNm() != null) {
            desc.append("기관: ").append(item.getInstNm()).append("\n");
        }

        if (item.getRecrutNope() != null) {
            desc.append("채용인원: ").append(item.getRecrutNope()).append("명\n");
        }

        if (item.getNcsCdNmLst() != null) {
            desc.append("직무분야: ").append(item.getNcsCdNmLst()).append("\n");
        }

        return desc.toString();
    }
}
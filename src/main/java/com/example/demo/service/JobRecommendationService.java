package com.example.demo.service;

import com.example.demo.dto.JobRecommendationDto;
import com.example.demo.dto.PublicJobSearchRequest;
import com.example.demo.dto.PublicRecruitmentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobRecommendationService {

    private final PublicJobService publicJobService;

    // 🔥 개선된 NCS 직무 코드 매핑 (더 많은 IT 키워드 포함)
    private static final Map<String, String> NCS_CODE_MAP = Map.ofEntries(
            Map.entry("사업관리", "01"),
            Map.entry("경영", "02"), Map.entry("회계", "02"), Map.entry("사무", "02"),
            Map.entry("금융", "03"), Map.entry("보험", "03"),
            Map.entry("교육", "04"), Map.entry("자연과학", "04"), Map.entry("사회과학", "04"),
            Map.entry("법률", "05"), Map.entry("경찰", "05"), Map.entry("소방", "05"), Map.entry("교도", "05"), Map.entry("국방", "05"),
            Map.entry("보건", "06"), Map.entry("의료", "06"),
            Map.entry("사회복지", "07"), Map.entry("종교", "07"),
            Map.entry("문화", "08"), Map.entry("예술", "08"), Map.entry("디자인", "08"), Map.entry("방송", "08"),
            Map.entry("운전", "09"), Map.entry("운송", "09"),
            Map.entry("영업", "10"), Map.entry("판매", "10"),
            Map.entry("경비", "11"), Map.entry("청소", "11"),
            Map.entry("이용", "12"), Map.entry("숙박", "12"), Map.entry("여행", "12"), Map.entry("오락", "12"), Map.entry("스포츠", "12"),
            Map.entry("음식", "13"),
            Map.entry("건설", "14"),
            Map.entry("기계", "15"),
            Map.entry("재료", "16"),
            Map.entry("화학", "17"),
            Map.entry("섬유", "18"), Map.entry("의복", "18"),
            Map.entry("전기", "19"), Map.entry("전자", "19"),
            // 🔥 IT 관련 키워드 대폭 확장
            Map.entry("정보통신", "20"), Map.entry("it", "20"), Map.entry("개발", "20"),
            Map.entry("프로그래머", "20"), Map.entry("소프트웨어", "20"), Map.entry("웹개발", "20"),
            Map.entry("앱개발", "20"), Map.entry("프론트엔드", "20"), Map.entry("백엔드", "20"),
            Map.entry("데이터", "20"), Map.entry("ai", "20"), Map.entry("머신러닝", "20"),
            Map.entry("빅데이터", "20"), Map.entry("클라우드", "20"), Map.entry("서버", "20"),
            Map.entry("데이터베이스", "20"), Map.entry("네트워크", "20"), Map.entry("보안", "20"),
            Map.entry("시스템", "20"), Map.entry("인프라", "20"), Map.entry("devops", "20"),
            Map.entry("식품", "21"), Map.entry("가공", "21"),
            Map.entry("인쇄", "22"), Map.entry("목재", "22"), Map.entry("가구", "22"), Map.entry("공예", "22"),
            Map.entry("환경", "23"), Map.entry("에너지", "23"), Map.entry("안전", "23"),
            Map.entry("농림어업", "24")
    );

    /**
     * 사용자의 희망 직무 키워드를 NCS 코드로 변환하는 헬퍼 메서드입니다.
     */
    private List<String> mapKeywordsToNcsCodes(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return List.of();
        }
        return keywords.stream()
                .map(String::toLowerCase)
                .flatMap(keyword -> NCS_CODE_MAP.entrySet().stream()
                        .filter(entry -> keyword.toLowerCase().contains(entry.getKey().toLowerCase()))
                        .map(Map.Entry::getValue))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 🔥 개선된 공공데이터포털 기반 채용공고 추천
     * 우선순위: 직무 > 지역 > 전체 검색 / 하나라도 있으면 추천
     */
    public List<JobRecommendationDto> getPublicJobRecommendations(
            Long userId,
            List<String> jobKeywords,
            List<String> locations) {

        log.info("📊 사용자 {}의 공고 추천 요청 - 키워드: {}, 지역: {}", userId, jobKeywords, locations);

        // 🔥 입력값 유효성 검사
        boolean hasJobKeywords = jobKeywords != null && !jobKeywords.isEmpty();
        boolean hasLocations = locations != null && !locations.isEmpty();

        if (!hasJobKeywords && !hasLocations) {
            log.info("❌ 직무와 지역 조건이 모두 없어서 추천을 중단합니다.");
            return List.of();
        }

        try {
            List<JobRecommendationDto> allRecommendations = new ArrayList<>();

            // 🔥 우선순위 1: 직무 기반 검색 (지역 포함 가능)
            if (hasJobKeywords) {
                log.info("🎯 1단계: 직무 기반 추천 검색 시작");
                try {
                    List<JobRecommendationDto> jobBasedResults = searchByJobKeywords(jobKeywords, locations);
                    allRecommendations.addAll(jobBasedResults);
                    log.info("✅ 직무 기반 검색 완료: {}건", jobBasedResults.size());
                } catch (Exception e) {
                    log.warn("⚠️ 직무 기반 검색 실패, 다음 단계로 진행: {}", e.getMessage());
                }
            }

            // 🔥 우선순위 2: 지역만 검색 (직무가 없거나 결과가 부족한 경우)
            if (hasLocations && allRecommendations.size() < 10) {
                log.info("🌍 2단계: 지역 기반 추천 검색 시작");
                try {
                    List<JobRecommendationDto> locationBasedResults = searchByLocationOnly(locations);

                    // 중복 제거하면서 추가
                    for (JobRecommendationDto newItem : locationBasedResults) {
                        if (!isDuplicateRecommendation(allRecommendations, newItem)) {
                            allRecommendations.add(newItem);
                        }
                    }
                    log.info("✅ 지역 기반 검색 완료: 추가 {}건", locationBasedResults.size());
                } catch (Exception e) {
                    log.warn("⚠️ 지역 기반 검색 실패, 다음 단계로 진행: {}", e.getMessage());
                }
            }

            // 🔥 우선순위 3: 전체 검색 (여전히 결과가 부족한 경우)
            if (allRecommendations.size() < 5) {
                log.info("📢 3단계: 전체 추천 검색 시작 (결과 부족: {}건)", allRecommendations.size());
                try {
                    List<JobRecommendationDto> generalResults = searchGeneral();

                    // 중복 제거하면서 최대 10개만 추가
                    int addedCount = 0;
                    for (JobRecommendationDto newItem : generalResults) {
                        if (!isDuplicateRecommendation(allRecommendations, newItem) && addedCount < 10) {
                            allRecommendations.add(newItem);
                            addedCount++;
                        }
                    }
                    log.info("✅ 전체 검색 완료: 추가 {}건", addedCount);
                } catch (Exception e) {
                    log.warn("⚠️ 전체 검색 실패: {}", e.getMessage());
                }
            }

            // 최종 결과 처리 및 정렬
            List<JobRecommendationDto> finalRecommendations = allRecommendations.stream()
                    .map(this::enhanceRecommendation)
                    .filter(this::isValidRecommendation)
                    .sorted((a, b) -> Integer.compare(b.getMatchScore(), a.getMatchScore())) // 매칭 점수 내림차순
                    .limit(20) // 최대 20개만 추천
                    .collect(Collectors.toList());

            log.info("✅ 최종 {}개의 공고 추천 완료 (요청조건: 직무={}, 지역={})",
                    finalRecommendations.size(), hasJobKeywords, hasLocations);
            return finalRecommendations;

        } catch (Exception e) {
            log.error("❌ 공고 추천 전체 프로세스 실패", e);
            return List.of(); // 에러 시 빈 리스트 반환
        }
    }

    /**
     * 🔥 직무 키워드 기반 검색 (지역 조건도 함께 적용 가능)
     */
    private List<JobRecommendationDto> searchByJobKeywords(List<String> jobKeywords, List<String> locations) {
        try {
            List<String> ncsCodes = mapKeywordsToNcsCodes(jobKeywords);

            PublicJobSearchRequest searchRequest = new PublicJobSearchRequest();
            searchRequest.setNumOfRows(25); // 직무 기반은 더 많이 가져오기
            searchRequest.setPageNo(1);

            // 🔥 지역 포함 여부에 따른 로깅 및 점수 차별화
            boolean hasLocation = locations != null && !locations.isEmpty();
            if (hasLocation) {
                searchRequest.setWorkRgnLst(locations);
                log.info("🔍 직무+지역 검색: 키워드={}, 지역={}", jobKeywords, locations);
            } else {
                log.info("🔍 직무만 검색: {}", jobKeywords);
            }

            // NCS 코드가 있으면 우선 사용, 없으면 키워드로 제목 검색
            if (!ncsCodes.isEmpty()) {
                searchRequest.setNcsCdLst(ncsCodes);
                log.info("   └ NCS 코드 사용: {}", ncsCodes);
            } else {
                searchRequest.setKeywords(jobKeywords);
                log.info("   └ 키워드 제목 검색 사용: {}", jobKeywords);
            }

            PublicRecruitmentResponse response = publicJobService.searchJobs(searchRequest);

            if (response.getResult() != null) {
                return response.getResult().stream()
                        .map(item -> {
                            JobRecommendationDto dto = JobRecommendationDto.fromPublicJobPosting(item);
                            // 🔥 지역까지 매칭되면 더 높은 점수
                            dto.setMatchScore(hasLocation ? 95 : 85);
                            return dto;
                        })
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            log.error("❌ 직무 기반 검색 실패", e);
        }

        return List.of();
    }

    /**
     * 🔥 지역만으로 검색 (직무 조건 없이)
     */
    private List<JobRecommendationDto> searchByLocationOnly(List<String> locations) {
        try {
            PublicJobSearchRequest searchRequest = new PublicJobSearchRequest();
            searchRequest.setWorkRgnLst(locations);
            searchRequest.setNumOfRows(15);
            searchRequest.setPageNo(1);

            log.info("🔍 지역만으로 검색: {}", locations);

            PublicRecruitmentResponse response = publicJobService.searchJobs(searchRequest);

            if (response.getResult() != null) {
                return response.getResult().stream()
                        .map(item -> {
                            JobRecommendationDto dto = JobRecommendationDto.fromPublicJobPosting(item);
                            dto.setMatchScore(75); // 지역 매칭이므로 중간 점수
                            return dto;
                        })
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            log.error("❌ 지역 기반 검색 실패", e);
        }

        return List.of();
    }

    /**
     * 🔥 전체 검색 (조건 없이 최신 공고)
     */
    private List<JobRecommendationDto> searchGeneral() {
        try {
            PublicJobSearchRequest searchRequest = new PublicJobSearchRequest();
            searchRequest.setNumOfRows(10);
            searchRequest.setPageNo(1);
            // 최신 공고 위주로 가져오기 위해 조건 없이 검색

            log.info("🔍 전체 검색 (조건 없음 - 최신 공고)");

            PublicRecruitmentResponse response = publicJobService.searchJobs(searchRequest);

            if (response.getResult() != null) {
                return response.getResult().stream()
                        .map(item -> {
                            JobRecommendationDto dto = JobRecommendationDto.fromPublicJobPosting(item);
                            dto.setMatchScore(60); // 전체 검색이므로 낮은 점수
                            return dto;
                        })
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            log.error("❌ 전체 검색 실패", e);
        }

        return List.of();
    }

    /**
     * 🔥 중복 추천 확인 (회사명 + 공고제목)
     */
    private boolean isDuplicateRecommendation(List<JobRecommendationDto> existingList, JobRecommendationDto newItem) {
        return existingList.stream()
                .anyMatch(existing ->
                        existing.getCompany().equals(newItem.getCompany()) &&
                                existing.getTitle().equals(newItem.getTitle())
                );
    }

    /**
     * 추천 공고 정보 보강
     */
    private JobRecommendationDto enhanceRecommendation(JobRecommendationDto recommendation) {
        // 기존 매칭 점수가 없으면 계산
        if (recommendation.getMatchScore() == null || recommendation.getMatchScore() == 0) {
            int matchScore = calculateMatchScore(recommendation);
            recommendation.setMatchScore(matchScore);
        }

        // 키워드 추출 (제목에서)
        List<String> extractedKeywords = extractKeywords(recommendation.getTitle());
        recommendation.setKeywords(extractedKeywords);

        // URL 검증 및 수정
        if (recommendation.getUrl() != null && !recommendation.getUrl().startsWith("http")) {
            recommendation.setUrl("https://" + recommendation.getUrl());
        }

        // 날짜 포맷 정리 (YYYYMMDD -> YYYY.MM.DD)
        if (recommendation.getDeadline() != null && recommendation.getDeadline().length() == 8) {
            String deadline = recommendation.getDeadline();
            String formattedDeadline = deadline.substring(0, 4) + "." +
                    deadline.substring(4, 6) + "." +
                    deadline.substring(6, 8);
            recommendation.setDeadline(formattedDeadline);
        }

        return recommendation;
    }

    /**
     * 🔥 개선된 매칭 점수 계산
     */
    private int calculateMatchScore(JobRecommendationDto recommendation) {
        int score = 70; // 기본 점수

        // 신입/경력 가산점
        if (recommendation.getExperience() != null) {
            String experience = recommendation.getExperience().toLowerCase();
            if (experience.contains("신입")) {
                score += 10;
            } else if (experience.contains("경력")) {
                score += 8;
            }
        }

        // 정규직 가산점
        if (recommendation.getEmploymentType() != null) {
            String employmentType = recommendation.getEmploymentType().toLowerCase();
            if (employmentType.contains("정규직")) {
                score += 8;
            } else if (employmentType.contains("계약직")) {
                score += 5;
            }
        }

        // 🔥 IT 관련 직무 가산점 (더 세밀하게)
        if (recommendation.getTitle() != null) {
            String title = recommendation.getTitle().toLowerCase();
            if (title.contains("개발") || title.contains("프로그래머") || title.contains("소프트웨어")) {
                score += 15;
            } else if (title.contains("시스템") || title.contains("it") || title.contains("데이터")) {
                score += 12;
            } else if (title.contains("웹") || title.contains("앱") || title.contains("ai")) {
                score += 10;
            }
        }

        // 공공기관 가산점
        if (recommendation.getCompany() != null) {
            String company = recommendation.getCompany().toLowerCase();
            if (company.contains("공단") || company.contains("공사") || company.contains("청") ||
                    company.contains("원") || company.contains("센터")) {
                score += 5;
            }
        }

        return Math.min(score, 100); // 최대 100점
    }

    /**
     * 🔥 개선된 제목에서 키워드 추출
     */
    private List<String> extractKeywords(String title) {
        if (title == null) return List.of();

        // 더 다양한 키워드 추출
        return List.of("공공기관", "정규직", "신입", "경력", "IT", "개발", "시스템",
                        "데이터", "웹", "앱", "AI", "클라우드", "보안", "네트워크")
                .stream()
                .filter(keyword -> title.toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * 유효한 추천인지 검증
     */
    private boolean isValidRecommendation(JobRecommendationDto recommendation) {
        // 필수 정보가 있는지 확인
        return recommendation.getCompany() != null &&
                !recommendation.getCompany().trim().isEmpty() &&
                recommendation.getTitle() != null &&
                !recommendation.getTitle().trim().isEmpty();
    }
}
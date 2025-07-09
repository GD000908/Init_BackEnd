package com.example.demo.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Component
public class JobCodeMappingService {

    // 지역명 -> 지역코드 매핑 (공공데이터포털 공식 코드)
    private static final Map<String, String> REGION_CODE_MAP = Map.ofEntries(
            Map.entry("서울", "R3010"),
            Map.entry("서울특별시", "R3010"),
            Map.entry("인천", "R3011"),
            Map.entry("인천광역시", "R3011"),
            Map.entry("대전", "R3012"),
            Map.entry("대전광역시", "R3012"),
            Map.entry("대구", "R3013"),
            Map.entry("대구광역시", "R3013"),
            Map.entry("부산", "R3014"),
            Map.entry("부산광역시", "R3014"),
            Map.entry("광주", "R3015"),
            Map.entry("광주광역시", "R3015"),
            Map.entry("울산", "R3016"),
            Map.entry("울산광역시", "R3016"),
            Map.entry("경기", "R3017"),
            Map.entry("경기도", "R3017"),
            Map.entry("강원", "R3018"),
            Map.entry("강원도", "R3018"),
            Map.entry("충남", "R3019"),
            Map.entry("충청남도", "R3019"),
            Map.entry("충북", "R3020"),
            Map.entry("충청북도", "R3020"),
            Map.entry("경북", "R3021"),
            Map.entry("경상북도", "R3021"),
            Map.entry("경남", "R3022"),
            Map.entry("경상남도", "R3022"),
            Map.entry("전남", "R3023"),
            Map.entry("전라남도", "R3023"),
            Map.entry("전북", "R3024"),
            Map.entry("전라북도", "R3024"),
            Map.entry("제주", "R3025"),
            Map.entry("제주도", "R3025"),
            Map.entry("제주특별자치도", "R3025"),
            Map.entry("세종", "R3026"),
            Map.entry("세종특별자치시", "R3026")
    );

    // 🔥 직무명 -> NCS코드 매핑 (공공데이터포털 공식 코드)
    private static final Map<String, String> JOB_CODE_MAP = Map.ofEntries(
            Map.entry("음식서비스", "R600013"),
            Map.entry("건설", "R600014"),
            Map.entry("기계", "R600015"),
            Map.entry("재료", "R600016"),
            Map.entry("화학", "R600017"),
            Map.entry("섬유.의복", "R600018"),
            Map.entry("섬유", "R600018"),
            Map.entry("의복", "R600018"),
            Map.entry("전기.전자", "R600019"),
            Map.entry("전기", "R600019"),
            Map.entry("전자", "R600019"),
            Map.entry("정보통신", "R600020"),
            Map.entry("IT", "R600020"),
            Map.entry("개발", "R600020"),
            Map.entry("프로그래밍", "R600020"),
            Map.entry("소프트웨어", "R600020"),
            Map.entry("웹 • SW 개발", "R600020"),
            Map.entry("프론트엔드 개발", "R600020"),
            Map.entry("백엔드 개발", "R600020"),
            Map.entry("풀스택 개발", "R600020"),
            Map.entry("모바일 개발", "R600020"),
            Map.entry("데이터 분석", "R600020"),
            Map.entry("인공지능", "R600020"),
            Map.entry("머신러닝", "R600020"),
            Map.entry("시스템 관리", "R600020"),
            Map.entry("네트워크", "R600020"),
            Map.entry("보안", "R600020"),
            Map.entry("DevOps", "R600020"),
            Map.entry("식품가공", "R600021"),
            Map.entry("인쇄.목재.가구.공예", "R600022"),
            Map.entry("인쇄", "R600022"),
            Map.entry("목재", "R600022"),
            Map.entry("가구", "R600022"),
            Map.entry("공예", "R600022"),
            Map.entry("환경.에너지.안전", "R600023"),
            Map.entry("환경", "R600023"),
            Map.entry("에너지", "R600023"),
            Map.entry("안전", "R600023"),
            Map.entry("농림어업", "R600024"),
            Map.entry("농업", "R600024"),
            Map.entry("임업", "R600024"),
            Map.entry("어업", "R600024"),
            Map.entry("연구", "R600025"),
            Map.entry("연구개발", "R600025"),
            Map.entry("R&D", "R600025")
    );

    public List<String> convertRegionNamesToCodes(List<String> regionNames) {
        if (regionNames == null || regionNames.isEmpty()) {
            return Collections.emptyList();
        }

        return regionNames.stream()
                .map(name -> {
                    String trimmedName = name.trim();
                    // 정확한 매칭 먼저 시도
                    String code = REGION_CODE_MAP.get(trimmedName);
                    if (code != null) {
                        return code;
                    }

                    // 부분 매칭 시도 (예: "서울" → "서울특별시")
                    return REGION_CODE_MAP.entrySet().stream()
                            .filter(entry -> entry.getKey().contains(trimmedName) || trimmedName.contains(entry.getKey()))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> convertJobNamesToCodes(List<String> jobNames) {
        if (jobNames == null || jobNames.isEmpty()) {
            return Collections.emptyList();
        }

        return jobNames.stream()
                .flatMap(jobName -> {
                    String trimmedJob = jobName.trim();

                    // 정확한 매칭 먼저 시도
                    String exactCode = JOB_CODE_MAP.get(trimmedJob);
                    if (exactCode != null) {
                        return Stream.of(exactCode);
                    }

                    // 부분 매칭 시도
                    return JOB_CODE_MAP.entrySet().stream()
                            .filter(entry -> {
                                String key = entry.getKey();
                                return trimmedJob.contains(key) ||
                                        key.contains(trimmedJob) ||
                                        isJobRelated(trimmedJob, key);
                            })
                            .map(Map.Entry::getValue);
                })
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean isJobRelated(String jobName, String codeKey) {
        // IT 관련 직무들을 정보통신 코드로 매핑
        if (codeKey.equals("정보통신")) {
            return jobName.toLowerCase().contains("it") ||
                    jobName.contains("개발") ||
                    jobName.contains("프로그래머") ||
                    jobName.contains("소프트웨어") ||
                    jobName.contains("웹") ||
                    jobName.contains("앱") ||
                    jobName.contains("시스템") ||
                    jobName.contains("데이터") ||
                    jobName.contains("AI") ||
                    jobName.contains("인공지능");
        }
        return false;
    }

    public String getJobNameByCode(String code) {
        return JOB_CODE_MAP.entrySet().stream()
                .filter(entry -> entry.getValue().equals(code))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("기타");
    }

    public String getRegionNameByCode(String code) {
        return REGION_CODE_MAP.entrySet().stream()
                .filter(entry -> entry.getValue().equals(code))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("전국");
    }
}
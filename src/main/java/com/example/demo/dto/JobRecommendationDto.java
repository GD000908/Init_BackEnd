package com.example.demo.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRecommendationDto {

    private String id;
    private String company;
    private String title;
    private String location;
    private String experience;
    private String education;
    private String employmentType;
    private String salary;
    private String deadline;
    private String url;

    // 추가 필드
    private List<String> keywords;
    private String postedDate;
    private Integer matchScore;
    private String description;
    private String requirements;
    private String benefits;
    private String recruitCount;

    /**
     * PublicJobPosting을 JobRecommendationDto로 변환하는 정적 메서드
     */
    public static JobRecommendationDto fromPublicJobPosting(PublicJobPosting posting) {
        if (posting == null) {
            return null;
        }

        return JobRecommendationDto.builder()
                .id(posting.getRecrutPblntSn() != null ? posting.getRecrutPblntSn().toString() :
                        "job_" + Math.abs((posting.getInstNm() + posting.getRecrutPbancTtl()).hashCode()))
                .company(posting.getInstNm())
                .title(posting.getRecrutPbancTtl())
                .location(posting.getWorkRgnNmLst())
                .experience(posting.getRecrutSeNm())
                .education(posting.getAcbgCondNmLst())
                .employmentType(posting.getHireTypeNmLst())
                .salary("공무원 보수규정에 따름") // 기본값
                .deadline(posting.getPbancEndYmd())
                .url(posting.getSrcUrl())
                .recruitCount(posting.getRecrutNope() != null ? posting.getRecrutNope().toString() : "미정")
                .description(posting.getAplyQlfcCn())
                .requirements(posting.getAplyQlfcCn())
                .benefits("4대보험, 퇴직금, 공무원 복리후생")
                .postedDate(posting.getPbancBgngYmd())
                .matchScore(80) // 기본 매칭 점수
                .build();
    }
}
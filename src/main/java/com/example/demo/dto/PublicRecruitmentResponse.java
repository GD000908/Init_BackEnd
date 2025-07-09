package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicRecruitmentResponse {

    // 🔥 실제 응답 구조에 맞게 수정
    private String resultCode;
    private String resultMsg;
    private Integer totalCount;
    private List<PublicJobItem> result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublicJobItem {
        private Long recrutPblntSn;         // 채용공고일련번호
        private String pblntInstCd;         // 공고기관코드
        private String instNm;              // 기관명
        private String ncsCdLst;            // NCS코드목록
        private String ncsCdNmLst;          // NCS코드명목록
        private String hireTypeLst;         // 고용유형목록
        private String hireTypeNmLst;       // 고용유형명목록
        private String workRgnLst;          // 근무지역목록
        private String workRgnNmLst;        // 근무지역명목록
        private String recrutSe;            // 채용구분
        private String recrutSeNm;          // 채용구분명
        private String prefCondCn;          // 우대조건내용
        private Integer recrutNope;         // 채용인원
        private String pbancBgngYmd;        // 공고시작일
        private String pbancEndYmd;         // 공고종료일
        private String recrutPbancTtl;      // 채용공고제목
        private String srcUrl;              // 원본URL
        private String replmprYn;           // 대체인력여부
        private String aplyQlfcCn;          // 지원자격내용
        private String disqlfcRsn;          // 결격사유
        private String scrnprcdrMthdExpln;  // 전형절차방법설명
        private String prefCn;              // 우대내용
        private String acbgCondLst;         // 학력조건목록
        private String acbgCondNmLst;       // 학력조건명목록
        private String ongoingYn;           // 진행여부
        private Integer decimalDay;         // 남은일수
    }
}
package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicJobSearchRequest {
    private List<String> ncsCdLst;      // NCS코드 목록 (직무)
    private List<String> workRgnLst;    // 근무지역 목록
    private String recrutPbancTtl;      // 공시제목 검색어
    private Integer numOfRows;          // 한 페이지 결과 수
    private Integer pageNo;             // 페이지 번호
}
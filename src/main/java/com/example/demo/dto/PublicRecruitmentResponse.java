package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicRecruitmentResponse {

    private Integer resultCode;
    private String resultMsg;
    private Integer totalCount;
    private List<PublicJobPosting> result;
}

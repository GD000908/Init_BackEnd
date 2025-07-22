package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicJobPosting {
    private Long recrutPblntSn;
    private String instNm;
    private String recrutPbancTtl;
    private String recrutSeNm;
    private String hireTypeNmLst;
    private String workRgnNmLst;
    private String acbgCondNmLst;
    private String pbancBgngYmd;
    private String pbancEndYmd;
    private String srcUrl;
    private Integer recrutNope;
    private String aplyQlfcCn;
    private Integer decimalDay;
    private List<Object> files;
    private List<Object> steps;

    // 프론트엔드 호환성을 위한 getter 메서드들
    public String getCompany() { return this.instNm; }
    public String getTitle() { return this.recrutPbancTtl; }
    public String getLocation() { return this.workRgnNmLst; }
    public String getExperience() { return this.recrutSeNm; }
    public String getEducation() { return this.acbgCondNmLst; }
    public String getEmploymentType() { return this.hireTypeNmLst; }
    public String getDeadline() { return this.pbancEndYmd; }
    public String getUrl() { return this.srcUrl; }
}

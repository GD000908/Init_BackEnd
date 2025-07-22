package com.example.demo.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PublicJobSearchRequest {
    private Integer pageNo = 1;
    private Integer numOfRows = 20;
    private List<String> keywords;
    private List<String> workRgnLst;
    private List<String> hireTypeLst;
    private String recrutSe;
    private List<String> acbgCondLst;
    private List<String> ncsCdLst;
    private List<String> locations;

    public void setLocations(List<String> locations) {
        this.locations = locations;
        this.workRgnLst = locations;
    }
}
package com.example.demo.client;

import com.example.demo.dto.PublicJobSearchRequest;
import com.example.demo.dto.PublicRecruitmentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Component
@Slf4j
public class PublicRecruitmentApiClient {

    // ✅ @Value 어노테이션을 통해 application.properties 파일의 키 값을 주입받습니다.
    @Value("${api.public-data.service-key}")
    private String serviceKey;

    private final int defaultRows = 20;
    private static final String BASE_URL = "https://apis.data.go.kr/1051000/recruitment/list";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PublicRecruitmentResponse searchJobs(PublicJobSearchRequest request) {
        try {
            URI uri = buildSearchUri(request);
            log.info("🌐 API 호출: {}", uri);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            String responseBody = response.getBody();

            if (responseBody != null && responseBody.trim().startsWith("<")) {
                throw new RuntimeException("API가 JSON 대신 HTML/XML을 반환했습니다.");
            }

            return objectMapper.readValue(responseBody, PublicRecruitmentResponse.class);

        } catch (Exception e) {
            log.error("❌ API 호출 실패", e);
            throw new RuntimeException("공공 채용 API 호출 실패: " + e.getMessage(), e);
        }
    }

    private URI buildSearchUri(PublicJobSearchRequest request) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("serviceKey", this.serviceKey)
                    .queryParam("resultType", "json")
                    .queryParam("numOfRows", request.getNumOfRows() != null ? request.getNumOfRows() : defaultRows)
                    .queryParam("pageNo", request.getPageNo() != null ? request.getPageNo() : 1)
                    .queryParam("ongoingYn", "Y");

            // 선택적 파라미터들
            if (request.getNcsCdLst() != null && !request.getNcsCdLst().isEmpty()) {
                builder.queryParam("ncsCdLst", String.join(",", request.getNcsCdLst()));
            }
            if (request.getWorkRgnLst() != null && !request.getWorkRgnLst().isEmpty()) {
                builder.queryParam("workRgnLst", String.join(",", request.getWorkRgnLst()));
            }
            if (request.getHireTypeLst() != null && !request.getHireTypeLst().isEmpty()) {
                builder.queryParam("hireTypeLst", String.join(",", request.getHireTypeLst()));
            }
            if (request.getRecrutSe() != null && !request.getRecrutSe().trim().isEmpty()) {
                builder.queryParam("recrutSe", request.getRecrutSe());
            }
            if (request.getAcbgCondLst() != null && !request.getAcbgCondLst().isEmpty()) {
                builder.queryParam("acbgCondLst", String.join(",", request.getAcbgCondLst()));
            }

            // ✅ 여러 키워드를 공백으로 연결하여 공고 제목을 검색하도록 개선했습니다.
            if (request.getKeywords() != null && !request.getKeywords().isEmpty()) {
                String titleKeywords = String.join(" ", request.getKeywords());
                if (!titleKeywords.trim().isEmpty()) {
                    builder.queryParam("recrutPbancTtl", titleKeywords);
                }
            }

            return builder.build(true).toUri();
        } catch (Exception e) {
            throw new RuntimeException("URL 구성 실패", e);
        }
    }
}
package com.example.demo.client;

import com.example.demo.dto.PublicJobSearchRequest;
import com.example.demo.dto.PublicRecruitmentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI; // java.net.URI 임포트
import java.util.List;

@Component
@Slf4j
public class PublicRecruitmentApiClient {

    // 테스트가 끝나면 이 부분은 원래대로 돌려놓으시는 것을 잊지 마세요!
    // @Value("${recruitment.api.service-key}")
    private String serviceKey = "ZNCVPk4K5AlBcu2prllK/ynRrMIKKv2PuTKoQv7FoxbzV87O5z5C0AQGnnZVZGsObqtxyzRig+jrTVsMFij8Hw==";

//    @Value("${recruitment.api.default-rows}")
    private int defaultRows=20;

    private static final String BASE_URL = "https://apis.data.go.kr/1051000/recruitment/list";

    private final RestTemplate restTemplate;

    public PublicRecruitmentApiClient() {
        this.restTemplate = new RestTemplate();
    }

    public PublicRecruitmentResponse searchJobs(PublicJobSearchRequest request) {
        try {
            URI uri = buildSearchUri(request);
            log.info("🌐 Calling public recruitment API with URI: {}", uri);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    uri, HttpMethod.GET, entity, String.class);

            log.info("✅ API Response Status: {}", response.getStatusCode());

            String responseBody = response.getBody();
            if (responseBody != null) {
                if (responseBody.trim().startsWith("<")) {
                    log.error("❌ API returned HTML/XML instead of JSON!");
                    log.error("Full response: {}", responseBody);
                    throw new RuntimeException("API가 JSON 대신 HTML/XML을 반환했습니다. 인증키나 URL을 확인해주세요.");
                }
            }

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseBody, PublicRecruitmentResponse.class);

        } catch (HttpClientErrorException e) {
            log.error("❌ HTTP Error Code: {}", e.getStatusCode());
            log.error("❌ HTTP Error Body: {}", e.getResponseBodyAsString());
            throw new RuntimeException("공공 채용 API HTTP 오류: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("❌ Error calling public recruitment API", e);
            throw new RuntimeException("공공 채용 API 호출 실패: " + e.getMessage(), e);
        }
    }

    private URI buildSearchUri(PublicJobSearchRequest request) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("resultType", "json")
                    .queryParam("numOfRows", request.getNumOfRows() != null ? request.getNumOfRows() : defaultRows)
                    // 💡 [오타 수정] .query를 제거했습니다.
                    .queryParam("pageNo", request.getPageNo() != null ? request.getPageNo() : 1)
                    .queryParam("ongoingYn", "Y");

            if (request.getNcsCdLst() != null && !request.getNcsCdLst().isEmpty()) {
                builder.queryParam("ncsCdLst", String.join(",", request.getNcsCdLst()));
            }

            if (request.getWorkRgnLst() != null && !request.getWorkRgnLst().isEmpty()) {
                builder.queryParam("workRgnLst", String.join(",", request.getWorkRgnLst()));
            }

            if (request.getRecrutPbancTtl() != null && !request.getRecrutPbancTtl().trim().isEmpty()) {
                builder.queryParam("recrutPbancTtl", request.getRecrutPbancTtl());
            }

            String urlWithoutServiceKey = builder.build(true).toUriString();

            String finalUrlString = urlWithoutServiceKey + "&serviceKey=" + this.serviceKey;

            log.info("🔗 Final URL String for URI creation: {}", finalUrlString);

            return new URI(finalUrlString);

        } catch (Exception e) {
            log.error("Error building search URI", e);
            throw new RuntimeException("URL(URI) 구성 실패", e);
        }
    }
}
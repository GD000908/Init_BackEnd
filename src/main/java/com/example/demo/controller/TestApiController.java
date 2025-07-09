package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestApiController {

    @Value("${recruitment.api.service-key}")
    private String serviceKey;

    @GetMapping("/recruitment-api")
    public ResponseEntity<String> testRecruitmentApi() {
        try {
            // 간단한 테스트 URL
            String testUrl = String.format(
                    "https://apis.data.go.kr/1051000/recruitment/list?serviceKey=%s&resultType=json&numOfRows=5&pageNo=1&ongoingYn=Y",
                    serviceKey
            );

            log.info("🧪 Testing API URL: {}", testUrl);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(testUrl, String.class);

            log.info("✅ Test Response Status: {}", response.getStatusCode());
            log.info("📄 Test Response Body: {}", response.getBody());

            return ResponseEntity.ok("API Test Success: " + response.getStatusCode() + "\n\nResponse: " + response.getBody());

        } catch (Exception e) {
            log.error("❌ API Test Failed", e);
            return ResponseEntity.badRequest().body("API Test Failed: " + e.getMessage());
        }
    }
}
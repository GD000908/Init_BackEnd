package com.example.demo.config;

import com.example.demo.entity.Interest;
import com.example.demo.entity.User;
import com.example.demo.repository.InterestRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final InterestRepository interestRepository; // InterestRepository 주입 추가

    @Override
    public void run(String... args) throws Exception {
        // 1. 관심 분야 데이터 생성
        createInterests();

        // 2. 테스트 사용자 생성
        createTestUser();
    }

    private void createInterests() {
        if (interestRepository.count() == 0) {
            log.info("관심 분야 데이터가 없어 초기 데이터를 생성합니다.");
            List<String> interestNames = Arrays.asList(
                    "경영/기획/전략", "디자인/콘텐츠", "개발/IT", "마케팅/브랜딩",
                    "영업/고객관리", "교육/강의/연구", "운영/사무관리", "생산/물류/품질관리",
                    "사회/공공기관", "특수직"
            );

            for (String name : interestNames) {
                interestRepository.save(Interest.builder().name(name).build());
            }
            log.info("관심 분야 데이터 생성이 완료되었습니다.");
        } else {
            log.info("관심 분야 데이터가 이미 존재합니다.");
        }
    }

    private void createTestUser() {
        if (userRepository.count() == 0) {
            log.info("테스트 사용자를 생성합니다.");
            try {
                User testUser = User.builder()
                        .userId("test@example.com")
                        .email("test@example.com") // email 필드 추가
                        .password("password123") // 실제 운영에서는 암호화 필요
                        .name("테스트 사용자")
                        .isActive(true)
                        .build();

                userRepository.save(testUser);
                log.info("테스트 사용자가 생성되었습니다. ID: {}", testUser.getUserId());

            } catch (Exception e) {
                log.error("테스트 사용자 생성 중 오류 발생: ", e);
            }
        } else {
            log.info("사용자 데이터가 이미 존재합니다.");
        }
    }
}
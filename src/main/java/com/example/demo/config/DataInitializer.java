package com.example.demo.config;

import com.example.demo.entity.Interest;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.InterestRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final InterestRepository interestRepository;
    private final BCryptPasswordEncoder passwordEncoder; // 🔥 추가

    @Override
    public void run(String... args) throws Exception {
        // 1. 관심 분야 데이터 생성
        createInterests();

        // 2. 관리자 계정 생성
        createAdminUser();
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

    // 🔥 관리자 계정 생성
    private void createAdminUser() {
        if (!userRepository.existsByUserId("admin")) {
            log.info("관리자 계정을 생성합니다.");
            try {
                User adminUser = User.builder()
                        .userId("admin")
                        .email("admin@init.com")
                        .password(passwordEncoder.encode("admin123!")) // 🔥 암호화된 비밀번호
                        .name("관리자")
                        .role(UserRole.ADMIN) // 🔥 관리자 역할 설정
                        .isActive(true)
                        .build();

                userRepository.save(adminUser);
                log.info("관리자 계정이 생성되었습니다. ID: admin, PW: admin123!");

            } catch (Exception e) {
                log.error("관리자 계정 생성 중 오류 발생: ", e);
            }
        } else {
            log.info("관리자 계정이 이미 존재합니다.");
        }
    }


}
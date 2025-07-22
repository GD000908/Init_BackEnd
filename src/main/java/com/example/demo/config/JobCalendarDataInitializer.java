package com.example.demo.config;

import com.example.demo.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // 기존 DataInitializer 이후에 실행
public class JobCalendarDataInitializer implements CommandLineRunner {

    private final JobPostingRepository jobPostingRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("🎯 Job Calendar 모듈 초기화 중...");

        // 샘플 채용공고가 없으면 생성
        if (jobPostingRepository.count() == 0) {
            log.info("샘플 채용공고를 생성합니다.");

        } else {
            log.info("채용공고 데이터가 이미 존재합니다. ({}개)", jobPostingRepository.count());
        }

        log.info("✅ Job Calendar 모듈 초기화 완료!");
    }


}
package com.example.demo.config;

import com.example.demo.entity.JobPosting;
import com.example.demo.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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
            createSampleJobPostings();
        } else {
            log.info("채용공고 데이터가 이미 존재합니다. ({}개)", jobPostingRepository.count());
        }

        log.info("✅ Job Calendar 모듈 초기화 완료!");
    }

    private void createSampleJobPostings() {
        try {
            JobPosting[] sampleJobs = {
                    JobPosting.builder()
                            .title("네이버")
                            .company("NAVER Corp")
                            .position("프론트엔드 개발자")
                            .startDate(LocalDate.now().minusDays(5))
                            .endDate(LocalDate.now().plusDays(15))
                            .location("경기 성남시")
                            .salary("3000~5000만원")
                            .color("#03C75A")
                            .description("React, TypeScript 기반 웹 서비스 개발")
                            .experienceLevel("3년 이상")
                            .employmentType("정규직")
                            .status(JobPosting.JobStatus.ACTIVE)
                            .build(),

                    JobPosting.builder()
                            .title("카카오")
                            .company("Kakao Corp")
                            .position("백엔드 개발자")
                            .startDate(LocalDate.now().minusDays(3))
                            .endDate(LocalDate.now().plusDays(25))
                            .location("제주도")
                            .salary("4000~6000만원")
                            .color("#FEE500")
                            .description("Spring Boot 기반 대용량 서비스 개발")
                            .experienceLevel("5년 이상")
                            .employmentType("정규직")
                            .status(JobPosting.JobStatus.ACTIVE)
                            .build(),

                    JobPosting.builder()
                            .title("토스")
                            .company("Toss")
                            .position("풀스택 개발자")
                            .startDate(LocalDate.now())
                            .endDate(LocalDate.now().plusDays(7))
                            .location("서울 강남구")
                            .salary("5000~8000만원")
                            .color("#0064FF")
                            .description("핀테크 서비스 개발 및 운영")
                            .experienceLevel("3년 이상")
                            .employmentType("정규직")
                            .status(JobPosting.JobStatus.ACTIVE)
                            .build()
            };

            for (JobPosting job : sampleJobs) {
                jobPostingRepository.save(job);
                log.info("샘플 채용공고 생성: {}", job.getTitle());
            }

        } catch (Exception e) {
            log.error("샘플 채용공고 생성 중 오류 발생: ", e);
        }
    }
}
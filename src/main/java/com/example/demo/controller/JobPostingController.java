package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CreateJobPostingRequest;
import com.example.demo.dto.JobPostingDto;
import com.example.demo.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/job-calendar/job-postings")
@RequiredArgsConstructor
@Slf4j
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobPostingDto>>> getAllJobPostings() {
        try {
            List<JobPostingDto> jobPostings = jobPostingService.getAllJobPostings();
            return ResponseEntity.ok(ApiResponse.success("채용공고 목록 조회 성공", jobPostings));
        } catch (Exception e) {
            log.error("채용공고 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("채용공고 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobPostingDto>> getJobPostingById(@PathVariable Long id) {
        try {
            JobPostingDto jobPosting = jobPostingService.getJobPostingById(id);
            return ResponseEntity.ok(ApiResponse.success("채용공고 조회 성공", jobPosting));
        } catch (Exception e) {
            log.error("채용공고 조회 실패: ID {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("채용공고 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<JobPostingDto>>> searchJobPostings(@RequestParam String keyword) {
        try {
            List<JobPostingDto> jobPostings = jobPostingService.searchJobPostings(keyword);
            return ResponseEntity.ok(ApiResponse.success("채용공고 검색 성공", jobPostings));
        } catch (Exception e) {
            log.error("채용공고 검색 실패: keyword {}", keyword, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("채용공고 검색에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<JobPostingDto>>> getActiveJobs() {
        try {
            List<JobPostingDto> jobPostings = jobPostingService.getActiveJobs();
            return ResponseEntity.ok(ApiResponse.success("진행중인 채용공고 조회 성공", jobPostings));
        } catch (Exception e) {
            log.error("진행중인 채용공고 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("진행중인 채용공고 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/ending-soon")
    public ResponseEntity<ApiResponse<List<JobPostingDto>>> getJobsEndingSoon(@RequestParam(defaultValue = "7") int days) {
        try {
            List<JobPostingDto> jobPostings = jobPostingService.getJobsEndingSoon(days);
            return ResponseEntity.ok(ApiResponse.success("마감 임박 채용공고 조회 성공", jobPostings));
        } catch (Exception e) {
            log.error("마감 임박 채용공고 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("마감 임박 채용공고 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/expired")
    public ResponseEntity<ApiResponse<List<JobPostingDto>>> getExpiredJobs() {
        try {
            List<JobPostingDto> jobPostings = jobPostingService.getExpiredJobs();
            return ResponseEntity.ok(ApiResponse.success("마감된 채용공고 조회 성공", jobPostings));
        } catch (Exception e) {
            log.error("마감된 채용공고 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("마감된 채용공고 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<JobPostingDto>>> getUpcomingJobs() {
        try {
            List<JobPostingDto> jobPostings = jobPostingService.getUpcomingJobs();
            return ResponseEntity.ok(ApiResponse.success("예정된 채용공고 조회 성공", jobPostings));
        } catch (Exception e) {
            log.error("예정된 채용공고 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("예정된 채용공고 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobPostingDto>> createJobPosting(@Valid @RequestBody CreateJobPostingRequest request) {
        try {
            JobPostingDto jobPosting = jobPostingService.createJobPosting(request);
            return ResponseEntity.ok(ApiResponse.success("채용공고 생성 성공", jobPosting));
        } catch (Exception e) {
            log.error("채용공고 생성 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("채용공고 생성에 실패했습니다: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobPostingDto>> updateJobPosting(
            @PathVariable Long id,
            @Valid @RequestBody CreateJobPostingRequest request) {
        try {
            JobPostingDto jobPosting = jobPostingService.updateJobPosting(id, request);
            return ResponseEntity.ok(ApiResponse.success("채용공고 수정 성공", jobPosting));
        } catch (Exception e) {
            log.error("채용공고 수정 실패: ID {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("채용공고 수정에 실패했습니다: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJobPosting(@PathVariable Long id) {
        try {
            jobPostingService.deleteJobPosting(id);
            return ResponseEntity.ok(ApiResponse.success("채용공고 삭제 성공", null));
        } catch (Exception e) {
            log.error("채용공고 삭제 실패: ID {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("채용공고 삭제에 실패했습니다: " + e.getMessage()));
        }
    }
}
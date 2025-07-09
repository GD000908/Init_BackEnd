package com.example.demo.service;

import com.example.demo.dto.CreateJobPostingRequest;
import com.example.demo.dto.JobPostingDto;
import com.example.demo.entity.JobPosting;
import com.example.demo.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;

    public List<JobPostingDto> getAllJobPostings() {
        return jobPostingRepository.findAll().stream()
                .map(JobPostingDto::from)
                .collect(Collectors.toList());
    }

    public JobPostingDto getJobPostingById(Long id) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("채용공고를 찾을 수 없습니다. ID: " + id));
        return JobPostingDto.from(jobPosting);
    }

    public List<JobPostingDto> searchJobPostings(String keyword) {
        List<JobPosting> results = jobPostingRepository.findByTitleContainingIgnoreCase(keyword);
        results.addAll(jobPostingRepository.findByCompanyContainingIgnoreCase(keyword));
        results.addAll(jobPostingRepository.findByPositionContainingIgnoreCase(keyword));

        return results.stream()
                .distinct()
                .map(JobPostingDto::from)
                .collect(Collectors.toList());
    }

    public List<JobPostingDto> getActiveJobs() {
        return jobPostingRepository.findActiveJobs(LocalDate.now()).stream()
                .map(JobPostingDto::from)
                .collect(Collectors.toList());
    }

    public List<JobPostingDto> getJobsEndingSoon(int days) {
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(days);

        return jobPostingRepository.findJobsEndingSoon(today, deadline).stream()
                .map(JobPostingDto::from)
                .collect(Collectors.toList());
    }

    public List<JobPostingDto> getExpiredJobs() {
        return jobPostingRepository.findExpiredJobs(LocalDate.now()).stream()
                .map(JobPostingDto::from)
                .collect(Collectors.toList());
    }

    public List<JobPostingDto> getUpcomingJobs() {
        return jobPostingRepository.findUpcomingJobs(LocalDate.now()).stream()
                .map(JobPostingDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public JobPostingDto createJobPosting(CreateJobPostingRequest request) {
        // 날짜 유효성 검사
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("시작일은 마감일보다 이전이어야 합니다.");
        }

        String[] colors = {"#4f46e5", "#f59e0b", "#10b981", "#ef4444", "#8b5cf6", "#ec4899"};
        String randomColor = request.getColor() != null ? request.getColor() :
                colors[(int) (Math.random() * colors.length)];

        JobPosting jobPosting = JobPosting.builder()
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .location(request.getLocation())
                .position(request.getPosition())
                .salary(request.getSalary())
                .color(randomColor)
                .description(request.getDescription())
                .company(request.getCompany())
                .department(request.getDepartment())
                .experienceLevel(request.getExperienceLevel())
                .employmentType(request.getEmploymentType())
                .status(JobPosting.JobStatus.ACTIVE)
                .build();

        JobPosting savedJobPosting = jobPostingRepository.save(jobPosting);
        log.info("새 채용공고 생성: {}", savedJobPosting.getTitle());

        return JobPostingDto.from(savedJobPosting);
    }

    @Transactional
    public JobPostingDto updateJobPosting(Long id, CreateJobPostingRequest request) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("채용공고를 찾을 수 없습니다. ID: " + id));

        // 날짜 유효성 검사
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("시작일은 마감일보다 이전이어야 합니다.");
        }

        jobPosting.setTitle(request.getTitle());
        jobPosting.setStartDate(request.getStartDate());
        jobPosting.setEndDate(request.getEndDate());
        jobPosting.setLocation(request.getLocation());
        jobPosting.setPosition(request.getPosition());
        jobPosting.setSalary(request.getSalary());
        jobPosting.setDescription(request.getDescription());
        jobPosting.setCompany(request.getCompany());
        jobPosting.setDepartment(request.getDepartment());
        jobPosting.setExperienceLevel(request.getExperienceLevel());
        jobPosting.setEmploymentType(request.getEmploymentType());

        if (request.getColor() != null) {
            jobPosting.setColor(request.getColor());
        }

        JobPosting updatedJobPosting = jobPostingRepository.save(jobPosting);
        log.info("채용공고 수정: {}", updatedJobPosting.getTitle()); // 오타 수정: u   pdated -> updated

        return JobPostingDto.from(updatedJobPosting);
    }

    @Transactional
    public void deleteJobPosting(Long id) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("채용공고를 찾을 수 없습니다. ID: " + id));

        jobPostingRepository.delete(jobPosting);
        log.info("채용공고 삭제: {}", jobPosting.getTitle());
    }
}

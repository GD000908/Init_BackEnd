package com.example.demo.service;

import com.example.demo.dto.ResumeDto;
import com.example.demo.entity.*;
import com.example.demo.repository.ResumeRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    /**
     * 이력서 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ResumeDto.ListResponse> getResumeList(Long userId) {
        log.info("Getting resume list for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<Resume> resumes = resumeRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return resumes.stream()
                .map(ResumeDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 이력서 상세 조회
     */
    @Transactional(readOnly = true)
    public ResumeDto.Response getResume(Long resumeId, Long userId) {
        log.info("Getting resume {} for user: {}", resumeId, userId);
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));
        return ResumeDto.Response.from(resume);
    }

    /**
     * 이력서 생성
     */
    @Transactional
    public ResumeDto.Response createResume(Long userId, ResumeDto.CreateRequest request) {
        log.info("Creating resume for user: {}, title: {}", userId, request.getTitle());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            resumeRepository.resetPrimaryResumes(userId);
        }

        Resume resume = Resume.builder()
                .title(request.getTitle())
                .summary(request.getSummary())
                .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .profileImageUrl(request.getProfileImageUrl())
                .githubUrl(request.getGithubUrl())
                .blogUrl(request.getBlogUrl())
                .portfolioUrl(request.getPortfolioUrl())
                .linkedinUrl(request.getLinkedinUrl())
                .jobCategory(request.getJobCategory())
                .companyType(request.getCompanyType())
                .preferredLocation(request.getPreferredLocation())
                .jobPosition(request.getJobPosition())
                .user(user)
                .build();

        // 하위 엔티티 리스트를 resume 객체에 추가하는 로직
        updateResumeSubEntities(resume, request);

        Resume savedResume = resumeRepository.save(resume);

        log.info("Resume created successfully with id: {}", savedResume.getId());
        return ResumeDto.Response.from(savedResume);
    }

    /**
     * 이력서 수정
     */
    @Transactional
    public ResumeDto.Response updateResume(Long resumeId, Long userId, ResumeDto.UpdateRequest request) {
        log.info("Updating resume {} for user: {}", resumeId, userId);

        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));

        if (Boolean.TRUE.equals(request.getIsPrimary()) && !resume.getIsPrimary()) {
            resumeRepository.resetPrimaryResumes(userId);
        }

        // 기본 정보 업데이트
        resume.setTitle(request.getTitle());
        resume.setSummary(request.getSummary());
        resume.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
        resume.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        resume.setFullName(request.getFullName());
        resume.setEmail(request.getEmail());
        resume.setPhone(request.getPhone());
        resume.setAddress(request.getAddress());
        resume.setBirthDate(request.getBirthDate());
        resume.setGender(request.getGender());
        resume.setProfileImageUrl(request.getProfileImageUrl());
        resume.setGithubUrl(request.getGithubUrl());
        resume.setBlogUrl(request.getBlogUrl());
        resume.setPortfolioUrl(request.getPortfolioUrl());
        resume.setLinkedinUrl(request.getLinkedinUrl());
        resume.setJobCategory(request.getJobCategory());
        resume.setCompanyType(request.getCompanyType());
        resume.setPreferredLocation(request.getPreferredLocation());
        resume.setJobPosition(request.getJobPosition());

        // 하위 엔티티 리스트를 업데이트하는 로직
        updateResumeSubEntities(resume, request);

        log.info("Resume updated successfully: {}", resume.getId());
        return ResumeDto.Response.from(resume);
    }

    // 이력서 하위 엔티티들을 업데이트하는 공통 메서드 (수정용)
    private void updateResumeSubEntities(Resume resume, ResumeDto.UpdateRequest request) {
        // 기존 자식 엔티티들을 모두 삭제 (orphanRemoval=true 옵션 덕분에 가능)
        resume.getWorkExperiences().clear();
        resume.getEducations().clear();
        resume.getSkills().clear();
        resume.getCertificates().clear();
        resume.getLanguages().clear();

        // 새로운 자식 엔티티들을 추가
        if (request.getWorkExperiences() != null) {
            request.getWorkExperiences().forEach(dto -> resume.addWorkExperience(WorkExperience.builder()
                    .company(dto.getCompany())
                    .position(dto.getPosition())
                    .department(dto.getDepartment())
                    .startDate(dto.getStartDate())
                    .endDate(dto.getEndDate())
                    .isCurrent(dto.getIsCurrent())
                    .description(dto.getDescription())
                    .build()));
        }

        if (request.getEducations() != null) {
            request.getEducations().forEach(dto -> resume.addEducation(Education.builder()
                    .school(dto.getSchool())
                    .major(dto.getMajor())
                    .degree(dto.getDegree())
                    .startDate(dto.getStartDate())
                    .endDate(dto.getEndDate())
                    .isCurrent(dto.getIsCurrent())
                    .gpa(dto.getGpa())
                    .maxGpa(dto.getMaxGpa())
                    .description(dto.getDescription())
                    .build()));
        }

        if (request.getSkills() != null) {
            request.getSkills().forEach(dto -> resume.addSkill(Skill.builder()
                    .name(dto.getName())
                    .category(dto.getCategory())
                    .proficiencyLevel(dto.getProficiencyLevel())
                    .build()));
        }

        if (request.getCertificates() != null) {
            request.getCertificates().forEach(dto -> resume.addCertificate(Certificate.builder()
                    .name(dto.getName())
                    .organization(dto.getOrganization())
                    .acquisitionDate(dto.getAcquisitionDate())
                    .expirationDate(dto.getExpirationDate())
                    .certificateNumber(dto.getCertificateNumber())
                    .displayOrder(dto.getDisplayOrder())
                    .build()));
        }

        if (request.getLanguages() != null) {
            request.getLanguages().forEach(dto -> resume.addLanguage(Language.builder()
                    .name(dto.getName())
                    .proficiencyLevel(dto.getProficiencyLevel())
                    .testName(dto.getTestName())
                    .testScore(dto.getTestScore())
                    .build()));
        }
    }

    // 이력서 하위 엔티티들을 업데이트하는 공통 메서드 (생성용)
    private void updateResumeSubEntities(Resume resume, ResumeDto.CreateRequest request) {
        if (request.getWorkExperiences() != null) {
            request.getWorkExperiences().forEach(dto -> resume.addWorkExperience(WorkExperience.builder()
                    .company(dto.getCompany())
                    .position(dto.getPosition())
                    .department(dto.getDepartment())
                    .startDate(dto.getStartDate())
                    .endDate(dto.getEndDate())
                    .isCurrent(dto.getIsCurrent())
                    .description(dto.getDescription())
                    .build()));
        }
        if (request.getEducations() != null) {
            request.getEducations().forEach(dto -> resume.addEducation(Education.builder()
                    .school(dto.getSchool())
                    .major(dto.getMajor())
                    .degree(dto.getDegree())
                    .startDate(dto.getStartDate())
                    .endDate(dto.getEndDate())
                    .isCurrent(dto.getIsCurrent())
                    .gpa(dto.getGpa())
                    .maxGpa(dto.getMaxGpa())
                    .description(dto.getDescription())
                    .build()));
        }
        if (request.getSkills() != null) {
            request.getSkills().forEach(dto -> resume.addSkill(Skill.builder()
                    .name(dto.getName())
                    .category(dto.getCategory())
                    .proficiencyLevel(dto.getProficiencyLevel())
                    .build()));
        }
        if (request.getCertificates() != null) {
            request.getCertificates().forEach(dto -> resume.addCertificate(Certificate.builder()
                    .name(dto.getName())
                    .organization(dto.getOrganization())
                    .acquisitionDate(dto.getAcquisitionDate())
                    .expirationDate(dto.getExpirationDate())
                    .certificateNumber(dto.getCertificateNumber())
                    .displayOrder(dto.getDisplayOrder())
                    .build()));
        }
        if (request.getLanguages() != null) {
            request.getLanguages().forEach(dto -> resume.addLanguage(Language.builder()
                    .name(dto.getName())
                    .proficiencyLevel(dto.getProficiencyLevel())
                    .testName(dto.getTestName())
                    .testScore(dto.getTestScore())
                    .build()));
        }
    }

    /**
     * 이력서 삭제
     */
    @Transactional
    public void deleteResume(Long resumeId, Long userId) {
        log.info("Deleting resume {} for user: {}", resumeId, userId);
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));
        resumeRepository.delete(resume);
        log.info("Resume deleted successfully: {}", resumeId);
    }

    /**
     * 대표 이력서 설정
     */
    @Transactional
    public ResumeDto.Response setPrimaryResume(Long resumeId, Long userId) {
        log.info("Setting primary resume {} for user: {}", resumeId, userId);

        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));

        // 기존 대표 이력서 해제
        resumeRepository.resetPrimaryResumes(userId);

        // 새로운 대표 이력서 설정
        resume.setPrimary(true);
        Resume savedResume = resumeRepository.save(resume);

        log.info("Primary resume set successfully: {}", savedResume.getId());
        return ResumeDto.Response.from(savedResume);
    }

    /**
     * 공개 상태 토글
     */
    @Transactional
    public ResumeDto.Response togglePublicStatus(Long resumeId, Long userId) {
        log.info("Toggling public status for resume {} of user: {}", resumeId, userId);

        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));

        resume.togglePublicStatus();
        Resume savedResume = resumeRepository.save(resume);

        log.info("Public status toggled for resume: {}, isPublic: {}", savedResume.getId(), savedResume.getIsPublic());
        return ResumeDto.Response.from(savedResume);
    }

    /**
     * 이력서 복사
     */
    @Transactional
    public ResumeDto.Response copyResume(Long resumeId, Long userId) {
        log.info("Copying resume {} for user: {}", resumeId, userId);

        Resume originalResume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Resume copiedResume = Resume.builder()
                .title(originalResume.getTitle() + " (복사본)")
                .summary(originalResume.getSummary())
                .isPrimary(false) // 복사본은 대표 이력서가 아님
                .isPublic(false) // 복사본은 비공개로 설정
                .fullName(originalResume.getFullName())
                .email(originalResume.getEmail())
                .phone(originalResume.getPhone())
                .address(originalResume.getAddress())
                .birthDate(originalResume.getBirthDate())
                .gender(originalResume.getGender())
                .profileImageUrl(originalResume.getProfileImageUrl())
                .githubUrl(originalResume.getGithubUrl())
                .blogUrl(originalResume.getBlogUrl())
                .portfolioUrl(originalResume.getPortfolioUrl())
                .linkedinUrl(originalResume.getLinkedinUrl())
                .jobCategory(originalResume.getJobCategory())
                .companyType(originalResume.getCompanyType())
                .preferredLocation(originalResume.getPreferredLocation())
                .jobPosition(originalResume.getJobPosition())
                .user(user)
                .build();

        // 복사 시 하위 엔티티들도 복사
        if (originalResume.getWorkExperiences() != null) {
            originalResume.getWorkExperiences().forEach(dto -> copiedResume.addWorkExperience(WorkExperience.builder()
                    .company(dto.getCompany())
                    .position(dto.getPosition())
                    .department(dto.getDepartment())
                    .startDate(dto.getStartDate())
                    .endDate(dto.getEndDate())
                    .isCurrent(dto.getIsCurrent())
                    .description(dto.getDescription())
                    .build()));
        }
        if (originalResume.getEducations() != null) {
            originalResume.getEducations().forEach(dto -> copiedResume.addEducation(Education.builder()
                    .school(dto.getSchool())
                    .major(dto.getMajor())
                    .degree(dto.getDegree())
                    .startDate(dto.getStartDate())
                    .endDate(dto.getEndDate())
                    .isCurrent(dto.getIsCurrent())
                    .gpa(dto.getGpa())
                    .maxGpa(dto.getMaxGpa())
                    .description(dto.getDescription())
                    .build()));
        }
        if (originalResume.getSkills() != null) {
            originalResume.getSkills().forEach(dto -> copiedResume.addSkill(Skill.builder()
                    .name(dto.getName())
                    .category(dto.getCategory())
                    .proficiencyLevel(dto.getProficiencyLevel())
                    .build()));
        }
        if (originalResume.getCertificates() != null) {
            originalResume.getCertificates().forEach(dto -> copiedResume.addCertificate(Certificate.builder()
                    .name(dto.getName())
                    .organization(dto.getOrganization())
                    .acquisitionDate(dto.getAcquisitionDate())
                    .expirationDate(dto.getExpirationDate())
                    .certificateNumber(dto.getCertificateNumber())
                    .displayOrder(dto.getDisplayOrder())
                    .build()));
        }
        if (originalResume.getLanguages() != null) {
            originalResume.getLanguages().forEach(dto -> copiedResume.addLanguage(Language.builder()
                    .name(dto.getName())
                    .proficiencyLevel(dto.getProficiencyLevel())
                    .testName(dto.getTestName())
                    .testScore(dto.getTestScore())
                    .build()));
        }

        Resume savedResume = resumeRepository.save(copiedResume);

        log.info("Resume copied successfully: {} -> {}", resumeId, savedResume.getId());
        return ResumeDto.Response.from(savedResume);
    }
}

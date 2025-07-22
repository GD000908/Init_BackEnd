package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class HomePageService {

    private final UserProfileRepository userProfileRepository;
    private final DesiredConditionsRepository desiredConditionsRepository;
    private final ApplicationStatusRepository applicationStatusRepository;
    private final TodoItemRepository todoItemRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final CoverLetterRepository coverLetterRepository;
    private final JobBookmarkRepository jobBookmarkRepository;
    private final JobPostingRepository jobPostingRepository;
    private final SpecCareerRepository specCareerRepository;
    private final SpecEducationRepository specEducationRepository;
    private final SpecCertificateRepository specCertificateRepository;
    private final SpecLanguageRepository specLanguageRepository;
    private final SpecSkillRepository specSkillRepository;
    private final SpecLinkRepository specLinkRepository;
    private final SpecProjectRepository specProjectRepository;
    private final SpecActivityRepository specActivityRepository;
    private final SpecMilitaryRepository specMilitaryRepository;

    // UserProfile CRUD
    public UserProfileDto getUserProfile(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        return convertToDto(profile);
    }

    public UserProfileDto createOrUpdateUserProfile(Long userId, UserProfileDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(new UserProfile());

        profile.setUser(user);
        profile.setName(dto.getName());
        profile.setEmail(dto.getEmail());
        profile.setCareerType(dto.getCareerType());
        profile.setJobTitle(dto.getJobTitle());
        profile.setMatching(dto.isMatching());

        UserProfile saved = userProfileRepository.save(profile);
        return convertToDto(saved);
    }

    // DesiredConditions CRUD
    public DesiredConditionsDto getDesiredConditions(Long userId) {
        DesiredConditions conditions = desiredConditionsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Desired conditions not found"));
        return convertToDto(conditions);
    }

    public DesiredConditionsDto createOrUpdateDesiredConditions(Long userId, DesiredConditionsDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DesiredConditions conditions = desiredConditionsRepository.findByUserId(userId)
                .orElse(new DesiredConditions());

        conditions.setUser(user);

        // 🔥 jobs가 비어있거나 null일 때 UserProfile의 jobTitle을 기본값으로 설정
        if (dto.getJobs() == null || dto.getJobs().isEmpty()) {
            UserProfile userProfile = userProfileRepository.findByUserId(userId).orElse(null);
            if (userProfile != null && userProfile.getJobTitle() != null && !userProfile.getJobTitle().trim().isEmpty()) {
                conditions.setJobs(Arrays.asList(userProfile.getJobTitle()));
            } else {
                conditions.setJobs(dto.getJobs());
            }
        } else {
            conditions.setJobs(dto.getJobs());
        }

        conditions.setLocations(dto.getLocations());
        conditions.setSalary(dto.getSalary());
        conditions.setOthers(dto.getOthers());

        DesiredConditions saved = desiredConditionsRepository.save(conditions);
        return convertToDto(saved);
    }

    // 🔥 ApplicationStatus CRUD - 완전히 개선된 버전
    public List<ApplicationStatusDto> getApplicationStatuses(Long userId) {
        try {
            List<ApplicationStatus> applications = applicationStatusRepository.findByUserIdOrderByIdDesc(userId);
            log.info("🔍 지원현황 조회 - userId: {}, 개수: {}", userId, applications.size());

            return applications.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ 지원현황 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            return List.of(); // 에러 시 빈 리스트 반환
        }
    }

    // 🔥 완전히 새로운 배치 업데이트 메서드 - userId를 별도 파라미터로 받음
    public List<ApplicationStatusDto> updateApplicationStatusesBatch(List<ApplicationStatusDto> applicationsFromFE, Long userId) {
        // 🔥 1. 입력값 검증
        if (applicationsFromFE == null) {
            log.warn("⚠️ applicationsFromFE가 null입니다. - userId: {}", userId);
            throw new IllegalArgumentException("입력 데이터가 null입니다.");
        }

        if (userId == null) {
            log.error("❌ userId가 null입니다.");
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }

        log.info("🔄 지원현황 일괄 수정 시작 - userId: {}, 요청 개수: {}", userId, applicationsFromFE.size());

        try {
            // 🔥 2. 기존 데이터 조회
            List<ApplicationStatus> existingEntities = applicationStatusRepository.findByUserIdOrderByIdDesc(userId);
            log.info("📊 기존 DB 데이터 개수: {}", existingEntities.size());

            // 🔥 3. 빈 리스트인 경우 모든 기존 데이터 삭제 (이제 userId가 확실히 있음)
            if (applicationsFromFE.isEmpty()) {
                log.info("🗑️ 빈 리스트 요청 - 모든 기존 데이터 삭제 시작 - userId: {}, 삭제 대상: {}개",
                        userId, existingEntities.size());

                if (!existingEntities.isEmpty()) {
                    applicationStatusRepository.deleteAll(existingEntities);
                    log.info("✅ 모든 지원현황 삭제 완료 - userId: {}, 실제 삭제된 개수: {}",
                            userId, existingEntities.size());
                } else {
                    log.info("ℹ️ 삭제할 기존 데이터가 없음 - userId: {}", userId);
                }

                return List.of(); // 빈 리스트 반환
            }

            // 🔥 4. 기존 데이터를 Map으로 변환 (빠른 조회를 위해)
            Map<Long, ApplicationStatus> dbMap = existingEntities.stream()
                    .collect(Collectors.toMap(ApplicationStatus::getId, entity -> entity));

            List<ApplicationStatusDto> resultDtos = new ArrayList<>();

            // 🔥 5. 프론트엔드 데이터 처리 (생성/수정)
            for (ApplicationStatusDto dto : applicationsFromFE) {
                ApplicationStatus entity;

                // userId 보장
                if (dto.getUserId() == null) {
                    dto.setUserId(userId);
                }

                if (dto.getId() != null && dto.getId() > 0) {
                    // 🔥 기존 데이터 수정
                    entity = dbMap.get(dto.getId());
                    if (entity != null) {
                        // 기존 엔티티 업데이트
                        entity.setCompany(dto.getCompany());
                        entity.setCategory(dto.getCategory());
                        entity.setDeadline(dto.getDeadline());
                        entity.setStatus(convertStringToStatusEnum(dto.getStatus()));

                        // 처리된 것으로 표시 (삭제 대상에서 제외)
                        dbMap.remove(dto.getId());
                        log.debug("✏️ 기존 지원현황 수정 - id: {}, company: {}", dto.getId(), dto.getCompany());
                    } else {
                        // ID가 있지만 DB에 없는 경우 새로 생성
                        entity = createNewApplicationStatus(userId, dto);
                        log.debug("🆕 ID는 있지만 DB에 없어서 새로 생성 - company: {}", dto.getCompany());
                    }
                } else {
                    // 🔥 새 데이터 생성
                    entity = createNewApplicationStatus(userId, dto);
                    log.debug("🆕 새 지원현황 생성 - company: {}", dto.getCompany());
                }

                // 저장
                ApplicationStatus savedEntity = applicationStatusRepository.save(entity);
                resultDtos.add(convertToDto(savedEntity));
            }

            // 🔥 6. 프론트엔드에서 제거된 데이터 삭제
            if (!dbMap.isEmpty()) {
                log.info("🗑️ 프론트엔드에서 제거된 지원현황 삭제 - 개수: {}", dbMap.size());
                for (ApplicationStatus toDelete : dbMap.values()) {
                    log.debug("🗑️ 삭제할 지원현황 - id: {}, company: {}", toDelete.getId(), toDelete.getCompany());
                }
                applicationStatusRepository.deleteAll(dbMap.values());
            }

            log.info("✅ 지원현황 일괄 수정 완료 - userId: {}, 최종 개수: {}", userId, resultDtos.size());
            return resultDtos;

        } catch (Exception e) {
            log.error("❌ 지원현황 일괄 수정 실패 - userId: {}, error: {}", userId, e.getMessage(), e);
            throw new RuntimeException("지원현황 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 🔥 개선된 통계 조회 메서드 - 타입 오류 수정
    // HomePageService.java의 getHomePageStats 메서드 - 올바른 수정 버전

    public HomePageStatsDto getHomePageStats(Long userId) {
        try {
            log.info("📊 통계 데이터 조회 시작 - userId: {}", userId);

            // ✅ 지원현황 통계 (ApplicationStatus 기준 - 대시보드 지원현황용)
            List<ApplicationStatus> applications = applicationStatusRepository.findByUserIdOrderByIdDesc(userId);

            int totalApplications = applications.size();
            int documentPassed = (int) applications.stream().filter(a -> a.getStatus() == ApplicationStatus.Status.서류_합격).count();
            int finalPassed = (int) applications.stream().filter(a -> a.getStatus() == ApplicationStatus.Status.최종_합격).count();
            int rejected = (int) applications.stream().filter(a -> a.getStatus() == ApplicationStatus.Status.불합격).count();

            // ✅ 기타 통계
            int resumeCount = (int) resumeRepository.countByUserId(userId);
            int coverLetterCount = (int) coverLetterRepository.countByUserId(userId);

            // ✅ 북마크된 공고 개수 (JobBookmark 기준)
            int bookmarkedCompanies = (int) jobBookmarkRepository.countByUserId(userId);

            // 🔥 올바른 마감 임박 계산 - JobBookmark의 JobPosting 마감일 기준!
            LocalDate now = LocalDate.now();
            LocalDate threeDaysLater = now.plusDays(3);

            // 🔥 공고 캘린더에서 북마크한 공고들의 마감일 확인
            List<JobBookmark> activeBookmarks = jobBookmarkRepository.findActiveBookmarksByUserId(userId);

            int deadlinesApproaching = (int) activeBookmarks.stream()
                    .filter(bookmark -> bookmark.getJobPosting() != null) // JobPosting이 있는 것만
                    .filter(bookmark -> bookmark.getJobPosting().getEndDate() != null) // 마감일이 있는 것만
                    .filter(bookmark -> {
                        LocalDate endDate = bookmark.getJobPosting().getEndDate();
                        boolean isApproaching = !endDate.isBefore(now) && !endDate.isAfter(threeDaysLater);
                        if (isApproaching) {
                            log.info("🚨 공고 마감 임박: company={}, endDate={}",
                                    bookmark.getJobPosting().getCompany(), endDate);
                        }
                        return isApproaching;
                    })
                    .count();

            log.info("✅ 마감 임박 계산 완료 - 북마크된 공고 중 3일 이내 마감: {}개", deadlinesApproaching);

            // 프로필 완성도
            ProfileCompletionDto profileCompletion = getProfileCompletion(userId);

            HomePageStatsDto stats = new HomePageStatsDto(
                    totalApplications, documentPassed, finalPassed, rejected,
                    resumeCount, coverLetterCount, bookmarkedCompanies, deadlinesApproaching,
                    profileCompletion
            );

            log.info("✅ 최종 통계 - 지원현황: {}개, 북마크: {}개, 마감임박: {}개",
                    totalApplications, bookmarkedCompanies, deadlinesApproaching);

            return stats;

        } catch (Exception e) {
            log.error("❌ 통계 데이터 조회 실패 - userId: {}, error: {}", userId, e.getMessage(), e);
            return new HomePageStatsDto(0, 0, 0, 0, 0, 0, 0, 0, new ProfileCompletionDto());
        }
    }

    // 🔥 헬퍼 메서드들
    private ApplicationStatus createNewApplicationStatus(Long userId, ApplicationStatusDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ApplicationStatus entity = new ApplicationStatus();
        entity.setUser(user);
        entity.setCompany(dto.getCompany());
        entity.setCategory(dto.getCategory());
        entity.setDeadline(dto.getDeadline());
        entity.setStatus(convertStringToStatusEnum(dto.getStatus()));

        return entity;
    }

    private ProfileCompletionDto getProfileCompletion(Long userId) {
        ProfileCompletionDto completion = new ProfileCompletionDto();

        // Check each section
        completion.setBasicInfo(userProfileRepository.findByUserId(userId).isPresent());
        completion.setDesiredConditions(desiredConditionsRepository.findByUserId(userId).isPresent());
        completion.setWorkExperience(specCareerRepository.existsByUserId(userId));
        completion.setEducation(specEducationRepository.existsByUserId(userId));
        completion.setCertificate(specCertificateRepository.existsByUserId(userId));
        completion.setLanguage(specLanguageRepository.existsByUserId(userId));
        completion.setSkill(specSkillRepository.existsByUserId(userId));
        completion.setLink(specLinkRepository.existsByUserId(userId));
        completion.setMilitary(specMilitaryRepository.countByUserId(userId) > 0);
        completion.setPortfolio(specProjectRepository.existsByUserId(userId));

        // Calculate percentage
        int completed = 0;
        if (completion.isBasicInfo()) completed++;
        if (completion.isDesiredConditions()) completed++;
        if (completion.isWorkExperience()) completed++;
        if (completion.isEducation()) completed++;
        if (completion.isCertificate()) completed++;
        if (completion.isLanguage()) completed++;
        if (completion.isSkill()) completed++;
        if (completion.isLink()) completed++;
        if (completion.isMilitary()) completed++;
        if (completion.isPortfolio()) completed++;

        completion.setCompletionPercentage((completed * 100) / 10);

        return completion;
    }

    // Converter methods
    private UserProfileDto convertToDto(UserProfile entity) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setCareerType(entity.getCareerType());
        dto.setJobTitle(entity.getJobTitle());
        dto.setMatching(entity.isMatching());
        return dto;
    }

    private DesiredConditionsDto convertToDto(DesiredConditions entity) {
        DesiredConditionsDto dto = new DesiredConditionsDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId());
        dto.setJobs(entity.getJobs());
        dto.setLocations(entity.getLocations());
        dto.setSalary(entity.getSalary());
        dto.setOthers(entity.getOthers());
        return dto;
    }

    private ApplicationStatusDto convertToDto(ApplicationStatus entity) {
        ApplicationStatusDto dto = new ApplicationStatusDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId());
        dto.setCompany(entity.getCompany());
        dto.setCategory(entity.getCategory());
        dto.setStatus(entity.getStatus().getDisplayName());
        dto.setDeadline(entity.getDeadline());
        return dto;
    }

    private ApplicationStatus.Status convertStringToStatusEnum(String statusString) {
        switch (statusString) {
            case "지원 완료":
                return ApplicationStatus.Status.지원_완료;
            case "서류 합격":
                return ApplicationStatus.Status.서류_합격;
            case "최종 합격":
                return ApplicationStatus.Status.최종_합격;
            case "불합격":
                return ApplicationStatus.Status.불합격;
            default:
                return ApplicationStatus.Status.지원_완료; // Default value
        }
    }
}
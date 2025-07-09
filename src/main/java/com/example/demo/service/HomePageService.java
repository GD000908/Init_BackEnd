package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        conditions.setJobs(dto.getJobs());
        conditions.setLocations(dto.getLocations());
        conditions.setSalary(dto.getSalary());
        conditions.setOthers(dto.getOthers());

        DesiredConditions saved = desiredConditionsRepository.save(conditions);
        return convertToDto(saved);
    }

    // ApplicationStatus CRUD
    public List<ApplicationStatusDto> getApplicationStatuses(Long userId) {
        return applicationStatusRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ApplicationStatusDto createApplicationStatus(Long userId, ApplicationStatusDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ApplicationStatus status = new ApplicationStatus();
        status.setUser(user);
        status.setCompany(dto.getCompany());
        status.setCategory(dto.getCategory());
        status.setDeadline(dto.getDeadline());

        ApplicationStatus.Status enumStatus = convertStringToStatusEnum(dto.getStatus());
        status.setStatus(enumStatus);

        ApplicationStatus saved = applicationStatusRepository.save(status);
        return convertToDto(saved);
    }

    public ApplicationStatusDto updateApplicationStatus(Long id, ApplicationStatusDto dto) {
        ApplicationStatus status = applicationStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application status not found"));

        status.setCompany(dto.getCompany());
        status.setCategory(dto.getCategory());
        status.setDeadline(dto.getDeadline());

        ApplicationStatus.Status enumStatus = convertStringToStatusEnum(dto.getStatus());
        status.setStatus(enumStatus);

        ApplicationStatus saved = applicationStatusRepository.save(status);
        return convertToDto(saved);
    }

    public void deleteApplicationStatus(Long id) {
        applicationStatusRepository.deleteById(id);
    }

    // Batch update for application statuses
    public List<ApplicationStatusDto> updateApplicationStatusesBatch(List<ApplicationStatusDto> applicationsFromFE) {
        // Get userId from the first DTO, assuming all are for the same user.
        Long userId = applicationsFromFE.stream()
                .map(ApplicationStatusDto::getUserId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User ID is required for batch update."));

        // Fetch existing entities from DB and map them by ID.
        Map<Long, ApplicationStatus> dbMap = applicationStatusRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(ApplicationStatus::getId, entity -> entity));

        List<ApplicationStatusDto> resultDtos = new ArrayList<>();

        // Process frontend list for creates and updates.
        for (ApplicationStatusDto dto : applicationsFromFE) {
            ApplicationStatus entity;
            if (dto.getId() != null && dto.getId() > 0) {
                // This is an existing item, process as an UPDATE.
                entity = dbMap.get(dto.getId());
                if (entity != null) {
                    entity.setCompany(dto.getCompany());
                    entity.setCategory(dto.getCategory());
                    entity.setDeadline(dto.getDeadline());
                    entity.setStatus(convertStringToStatusEnum(dto.getStatus()));
                    // Remove from map so it's not deleted later.
                    dbMap.remove(dto.getId());
                } else {
                    // This case should not happen if FE state is consistent, but handle it.
                    entity = new ApplicationStatus();
                    User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
                    entity.setUser(user);
                    entity.setCompany(dto.getCompany());
                    entity.setCategory(dto.getCategory());
                    entity.setDeadline(dto.getDeadline());
                    entity.setStatus(convertStringToStatusEnum(dto.getStatus()));
                }
            } else {
                // This is a new item (ID is negative or null), process as a CREATE.
                entity = new ApplicationStatus();
                User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
                entity.setUser(user);
                entity.setCompany(dto.getCompany());
                entity.setCategory(dto.getCategory());
                entity.setDeadline(dto.getDeadline());
                entity.setStatus(convertStringToStatusEnum(dto.getStatus()));
            }

            ApplicationStatus savedEntity = applicationStatusRepository.save(entity);
            resultDtos.add(convertToDto(savedEntity));
        }

        // Any entities left in dbMap were deleted on the frontend.
        if (!dbMap.isEmpty()) {
            applicationStatusRepository.deleteAll(dbMap.values());
        }

        return resultDtos;
    }

    // TodoItem CRUD
    public List<TodoItemDto> getTodoItems(Long userId) {
        return todoItemRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TodoItemDto createTodoItem(Long userId, TodoItemDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TodoItem item = new TodoItem();
        item.setUser(user);
        item.setText(dto.getText());
        item.setCompleted(dto.isCompleted());

        TodoItem saved = todoItemRepository.save(item);
        return convertToDto(saved);
    }

    public TodoItemDto updateTodoItem(Long id, TodoItemDto dto) {
        TodoItem item = todoItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo item not found"));

        item.setText(dto.getText());
        item.setCompleted(dto.isCompleted());

        TodoItem saved = todoItemRepository.save(item);
        return convertToDto(saved);
    }

    public void deleteTodoItem(Long id) {
        todoItemRepository.deleteById(id);
    }

    // Stats for HomePage
    public HomePageStatsDto getHomePageStats(Long userId) {
        HomePageStatsDto stats = new HomePageStatsDto();

        // Application Status Stats
        List<ApplicationStatus> applications = applicationStatusRepository.findByUserId(userId);
        stats.setTotalApplications(applications.size());
        stats.setDocumentPassed((int) applications.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.Status.서류_합격)
                .count());
        stats.setFinalPassed((int) applications.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.Status.최종_합격)
                .count());
        stats.setRejected((int) applications.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.Status.불합격)
                .count());

        // Activity Stats
        stats.setResumeCount((int) resumeRepository.countByUserId(userId));
        stats.setCoverLetterCount(coverLetterRepository.countByUserId(userId));
        stats.setBookmarkedCompanies(jobBookmarkRepository.countByUserId(userId));

        // Deadlines approaching (3 days)
        LocalDate threeDaysLater = LocalDate.now().plusDays(3);
        List<JobBookmark> bookmarks = jobBookmarkRepository.findByUserId(userId);
        int deadlinesCount = 0;
        for (JobBookmark bookmark : bookmarks) {
            JobPosting posting = bookmark.getJobPosting();
            if (posting != null && posting.getEndDate() != null &&
                    posting.getEndDate().isBefore(threeDaysLater) &&
                    posting.getEndDate().isAfter(LocalDate.now())) {
                deadlinesCount++;
            }
        }
        stats.setDeadlinesApproaching(deadlinesCount);

        // Profile Completion
        stats.setProfileCompletion(getProfileCompletion(userId));

        return stats;
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
        completion.setMilitary(false); // Need to implement
        completion.setPortfolio(specProjectRepository.existsByUserId(userId));
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

    private TodoItemDto convertToDto(TodoItem entity) {
        TodoItemDto dto = new TodoItemDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId());
        dto.setText(entity.getText());
        dto.setCompleted(entity.isCompleted());
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

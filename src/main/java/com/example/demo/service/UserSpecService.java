package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// 클래스 레벨의 @Transactional 어노테이션을 수정하여 readOnly 설정을 제거합니다.
// 이렇게 하면 각 메소드가 필요에 따라 읽기 또는 쓰기 트랜잭션을 사용할 수 있게 됩니다.
@Transactional
public class UserSpecService {

    private static final Logger log = LoggerFactory.getLogger(UserSpecService.class);

    private final UserRepository userRepository;
    private final UserSpecRepository userSpecRepository;
    private final SpecCareerRepository specCareerRepository;
    private final SpecEducationRepository specEducationRepository;
    private final SpecSkillRepository specSkillRepository;
    private final SpecCertificateRepository specCertificateRepository;
    private final SpecLanguageRepository specLanguageRepository;
    private final SpecProjectRepository specProjectRepository;
    private final SpecActivityRepository specActivityRepository;
    private final SpecLinkRepository specLinkRepository;
    private final SpecMilitaryRepository specMilitaryRepository;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 사용자의 전체 스펙 데이터 조회
     */
    // 이 메서드는 여러 조회 메서드를 호출하므로, 클래스 레벨의 트랜잭션을 따릅니다.
    public UserSpecDto getUserSpec(Long userId) {
        UserSpec userSpec = getOrCreateUserSpec(userId);

        return UserSpecDto.builder()
                .profile(getSpecProfile(userId))
                .careerStats(getSpecCareerStats(userId))
                .workExperiences(getSpecCareers(userId))
                .educations(getSpecEducations(userId))
                .skills(getSpecSkills(userId))
                .certificates(getSpecCertificates(userId))
                .languages(getSpecLanguages(userId))
                .projects(getSpecProjects(userId))
                .activities(getSpecActivities(userId))
                .links(getSpecLinks(userId))
                .militaries(getSpecMilitaries(userId))
                .build();
    }

    /**
     * 스펙 프로필 조회
     */
    // 순수 조회 기능이므로 readOnly = true를 명시하여 성능을 최적화합니다.
    @Transactional(readOnly = true)
    public SpecProfileDto getSpecProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // UserSpec이 없을 경우 생성 로직이 호출될 수 있으나,
        // 이 메서드는 주로 조회에 사용되므로 readOnly를 유지합니다.
        // getOrCreateUserSpec은 별도의 트랜잭션 설정을 가집니다.
        UserSpec userSpec = getOrCreateUserSpec(userId);
        List<String> skillNames = specSkillRepository.findSkillNamesByUserId(userId);

        return SpecProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .phone(userSpec.getPhone())
                .location(userSpec.getAddress())
                .careerLevel(userSpec.getCareerLevel())
                .jobTitle(userSpec.getJobTitle())
                .introduction(userSpec.getIntroduction())
                .birthDate(userSpec.getBirthDate())
                .skills(skillNames)
                .build();
    }

    /**
     * 경력 통계 조회
     */
    @Transactional(readOnly = true)
    public SpecCareerStatsDto getSpecCareerStats(Long userId) {
        UserSpec userSpec = getOrCreateUserSpec(userId);
        List<SpecCareer> careers = specCareerRepository.findByUserSpecUserIdOrderByStartDateDesc(userId);

        String experience = calculateTotalExperience(careers);
        String workRecords = String.valueOf(careers.size()) + "개";
        String careerGoal = userSpec.getCareerGoal() != null ? userSpec.getCareerGoal() : "커리어 목표를 입력해 주세요";

        return SpecCareerStatsDto.builder()
                .experience(experience)
                .workRecords(workRecords)
                .careerGoal(careerGoal)
                .build();
    }

    /**
     * 업무 경력 조회
     */
    @Transactional(readOnly = true)
    public List<SpecCareerDto> getSpecCareers(Long userId) {
        return specCareerRepository.findByUserSpecUserIdOrderByDisplayOrderAsc(userId)
                .stream()
                .map(this::convertToSpecCareerDto)
                .collect(Collectors.toList());
    }

    /**
     * 학력 조회
     */
    @Transactional(readOnly = true)
    public List<SpecEducationDto> getSpecEducations(Long userId) {
        return specEducationRepository.findByUserSpecUserIdOrderByDisplayOrderAsc(userId)
                .stream()
                .map(this::convertToSpecEducationDto)
                .collect(Collectors.toList());
    }

    /**
     * 스킬 조회 (이름만)
     */
    @Transactional(readOnly = true)
    public List<String> getSpecSkillNames(Long userId) {
        return specSkillRepository.findSkillNamesByUserId(userId);
    }

    /**
     * 스킬 조회 (전체 정보)
     */
    @Transactional(readOnly = true)
    public List<SpecSkillDto> getSpecSkills(Long userId) {
        return specSkillRepository.findByUserSpecUserIdOrderByDisplayOrderAsc(userId)
                .stream()
                .map(this::convertToSpecSkillDto)
                .collect(Collectors.toList());
    }

    /**
     * 자격증 조회
     */
    @Transactional(readOnly = true)
    public List<SpecCertificateDto> getSpecCertificates(Long userId) {
        return specCertificateRepository.findByUserSpecUserIdOrderByDisplayOrderAsc(userId)
                .stream()
                .map(this::convertToSpecCertificateDto)
                .collect(Collectors.toList());
    }

    /**
     * 어학 조회
     */
    @Transactional(readOnly = true)
    public List<SpecLanguageDto> getSpecLanguages(Long userId) {
        return specLanguageRepository.findByUserSpecUserIdOrderByDisplayOrderAsc(userId)
                .stream()
                .map(this::convertToSpecLanguageDto)
                .collect(Collectors.toList());
    }

    /**
     * 프로젝트 조회
     */
    @Transactional(readOnly = true)
    public List<SpecProjectDto> getSpecProjects(Long userId) {
        return specProjectRepository.findByUserSpecUserIdOrderByDisplayOrderAsc(userId)
                .stream()
                .map(this::convertToSpecProjectDto)
                .collect(Collectors.toList());
    }

    /**
     * 활동 조회
     */
    @Transactional(readOnly = true)
    public List<SpecActivityDto> getSpecActivities(Long userId) {
        return specActivityRepository.findByUserSpecUserIdOrderByDisplayOrderAsc(userId)
                .stream()
                .map(this::convertToSpecActivityDto)
                .collect(Collectors.toList());
    }

    /**
     * 링크 조회
     */
    @Transactional(readOnly = true)
    public List<SpecLinkDto> getSpecLinks(Long userId) {
        return specLinkRepository.findByUserSpecUserIdOrderByDisplayOrderAsc(userId)
                .stream()
                .map(this::convertToSpecLinkDto)
                .collect(Collectors.toList());
    }

    /**
     * 병역 조회
     */
    @Transactional(readOnly = true)
    public List<SpecMilitaryDto> getSpecMilitaries(Long userId) {
        return specMilitaryRepository.findByUserSpecUserIdOrderByStartDateDesc(userId)
                .stream()
                .map(this::convertToSpecMilitaryDto)
                .collect(Collectors.toList());
    }

    // === 업데이트 메서드들 (쓰기 작업이므로 @Transactional 그대로 유지) ===

    /**
     * 프로필 업데이트
     */
    @Transactional
    public SpecProfileDto updateSpecProfile(Long userId, UpdateSpecProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        UserSpec userSpec = getOrCreateUserSpec(userId);
        userSpec.setPhone(request.getPhone());
        userSpec.setAddress(request.getLocation());
        userSpec.setCareerLevel(request.getCareerLevel());
        userSpec.setJobTitle(request.getJobTitle());
        userSpec.setIntroduction(request.getIntroduction());
        userSpec.setBirthDate(request.getBirthDate());

        if (request.getSkills() != null) {
            updateSpecSkills(userId, request.getSkills());
        }

        return getSpecProfile(userId);
    }

    /**
     * 경력 통계 업데이트
     */
    @Transactional
    public SpecCareerStatsDto updateSpecCareerStats(Long userId, UpdateSpecCareerStatsRequest request) {
        UserSpec userSpec = getOrCreateUserSpec(userId);
        userSpec.setCareerGoal(request.getCareerGoal());
        return getSpecCareerStats(userId);
    }

    // ... 이하 모든 update... 및 convert... 메소드는 생략 (변경 없음)

    @Transactional
    public List<SpecCareerDto> updateSpecCareers(Long userId, List<SpecCareerDto> careerDtos) {
        UserSpec userSpec = getOrCreateUserSpec(userId);
        specCareerRepository.deleteByUserSpecUserId(userId);
        List<SpecCareer> newCareers = careerDtos.stream()
                .map(dto -> convertToSpecCareerEntity(dto, userSpec))
                .collect(Collectors.toList());
        specCareerRepository.saveAll(newCareers);
        return getSpecCareers(userId);
    }

    @Transactional
    public List<SpecEducationDto> updateSpecEducations(Long userId, List<SpecEducationDto> educationDtos) {
        UserSpec userSpec = getOrCreateUserSpec(userId);
        specEducationRepository.deleteByUserSpecUserId(userId);
        List<SpecEducation> newEducations = educationDtos.stream()
                .map(dto -> convertToSpecEducationEntity(dto, userSpec))
                .collect(Collectors.toList());
        specEducationRepository.saveAll(newEducations);
        return getSpecEducations(userId);
    }

    @Transactional
    public List<String> updateSpecSkills(Long userId, List<String> skillNames) {
        UserSpec userSpec = getOrCreateUserSpec(userId);
        specSkillRepository.deleteByUserSpecUserId(userId);
        List<SpecSkill> newSkills = skillNames.stream()
                .map(skillName -> SpecSkill.builder()
                        .userSpec(userSpec)
                        .name(skillName)
                        .category("기술")
                        .level(3)
                        .displayOrder(skillNames.indexOf(skillName))
                        .build())
                .collect(Collectors.toList());
        specSkillRepository.saveAll(newSkills);
        return specSkillRepository.findSkillNamesByUserId(userId);
    }

    @Transactional
    public List<SpecCertificateDto> updateSpecCertificates(Long userId, List<SpecCertificateDto> certificateDtos) {
        UserSpec userSpec = getOrCreateUserSpec(userId);
        specCertificateRepository.deleteByUserSpecUserId(userId);
        List<SpecCertificate> newCertificates = certificateDtos.stream()
                .map(dto -> convertToSpecCertificateEntity(dto, userSpec))
                .collect(Collectors.toList());
        specCertificateRepository.saveAll(newCertificates);
        return getSpecCertificates(userId);
    }

    @Transactional
    public List<SpecLanguageDto> updateSpecLanguages(Long userId, List<SpecLanguageDto> languageDtos) {
        UserSpec userSpec = getOrCreateUserSpec(userId);
        specLanguageRepository.deleteByUserSpecUserId(userId);
        List<SpecLanguage> newLanguages = languageDtos.stream()
                .map(dto -> convertToSpecLanguageEntity(dto, userSpec))
                .collect(Collectors.toList());
        specLanguageRepository.saveAll(newLanguages);
        return getSpecLanguages(userId);
    }

    @Transactional
    public List<SpecProjectDto> updateSpecProjects(Long userId, List<SpecProjectDto> projectDtos) {
        UserSpec userSpec = getOrCreateUserSpec(userId);
        specProjectRepository.deleteByUserSpecUserId(userId);
        List<SpecProject> newProjects = projectDtos.stream()
                .map(dto -> convertToSpecProjectEntity(dto, userSpec))
                .collect(Collectors.toList());
        specProjectRepository.saveAll(newProjects);
        return getSpecProjects(userId);
    }

    @Transactional
    public List<SpecActivityDto> updateSpecActivities(Long userId, List<SpecActivityDto> activityDtos) {
        UserSpec userSpec = getOrCreateUserSpec(userId);
        specActivityRepository.deleteByUserSpecUserId(userId);
        List<SpecActivity> newActivities = activityDtos.stream()
                .map(dto -> convertToSpecActivityEntity(dto, userSpec))
                .collect(Collectors.toList());
        specActivityRepository.saveAll(newActivities);
        return getSpecActivities(userId);
    }

    @Transactional
    public List<SpecLinkDto> updateSpecLinks(Long userId, List<SpecLinkDto> linkDtos) {
        UserSpec userSpec = getOrCreateUserSpec(userId);
        specLinkRepository.deleteByUserSpecUserId(userId);
        List<SpecLink> newLinks = linkDtos.stream()
                .map(dto -> convertToSpecLinkEntity(dto, userSpec))
                .collect(Collectors.toList());
        specLinkRepository.saveAll(newLinks);
        return getSpecLinks(userId);
    }

    @Transactional
    public List<SpecMilitaryDto> updateSpecMilitaries(Long userId, List<SpecMilitaryDto> militaryDtos) {
        UserSpec userSpec = getOrCreateUserSpec(userId);
        specMilitaryRepository.deleteByUserSpecUserId(userId);
        List<SpecMilitary> newMilitaries = militaryDtos.stream()
                .map(dto -> convertToSpecMilitaryEntity(dto, userSpec))
                .collect(Collectors.toList());
        specMilitaryRepository.saveAll(newMilitaries);
        return getSpecMilitaries(userId);
    }

    // === 헬퍼 메서드들 ===

    // 이 메서드는 쓰기(save)가 발생할 수 있으므로, 별도의 @Transactional을 유지합니다.
    @Transactional
    private UserSpec getOrCreateUserSpec(Long userId) {
        return userSpecRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

                    UserSpec newUserSpec = UserSpec.builder()
                            .user(user)
                            .careerLevel("신입")
                            .jobTitle("개발자")
                            .introduction("")
                            .careerGoal("")
                            .build();

                    return userSpecRepository.save(newUserSpec);
                });
    }

    private String calculateTotalExperience(List<SpecCareer> careers) {
        if (careers.isEmpty()) {
            return "0개월";
        }

        int totalMonths = careers.size() * 12;
        if (totalMonths < 12) {
            return totalMonths + "개월";
        } else {
            int years = totalMonths / 12;
            int months = totalMonths % 12;
            return years + "년" + (months > 0 ? " " + months + "개월" : "");
        }
    }

    // === Entity ↔ DTO 변환 메서드들 (변경 없음, 생략) ===
    private SpecCareerDto convertToSpecCareerDto(SpecCareer entity){return SpecCareerDto.builder().id(entity.getId()).company(entity.getCompany()).department(entity.getDepartment()).position(entity.getPosition()).startDate(entity.getStartDate()!=null?entity.getStartDate().format(dateFormatter):null).endDate(entity.getEndDate()!=null?entity.getEndDate().format(dateFormatter):null).isCurrent(entity.getIsCurrent()).description(entity.getDescription()).displayOrder(entity.getDisplayOrder()).build();}
    private SpecCareer convertToSpecCareerEntity(SpecCareerDto dto,UserSpec userSpec){return SpecCareer.builder().userSpec(userSpec).company(dto.getCompany()).department(dto.getDepartment()).position(dto.getPosition()).startDate(dto.getStartDate()!=null?LocalDate.parse(dto.getStartDate(),dateFormatter):null).endDate(dto.getEndDate()!=null?LocalDate.parse(dto.getEndDate(),dateFormatter):null).isCurrent(dto.getIsCurrent()!=null?dto.getIsCurrent():false).description(dto.getDescription()).displayOrder(dto.getDisplayOrder()!=null?dto.getDisplayOrder():0).build();}
    private SpecEducationDto convertToSpecEducationDto(SpecEducation entity){return SpecEducationDto.builder().id(entity.getId()).school(entity.getSchool()).major(entity.getMajor()).degree(entity.getDegree()).startDate(entity.getStartDate()!=null?entity.getStartDate().format(dateFormatter):null).endDate(entity.getEndDate()!=null?entity.getEndDate().format(dateFormatter):null).isCurrent(entity.getIsCurrent()).gpa(entity.getGpa()).maxGpa(entity.getMaxGpa()).description(entity.getDescription()).displayOrder(entity.getDisplayOrder()).build();}
    private SpecEducation convertToSpecEducationEntity(SpecEducationDto dto,UserSpec userSpec){return SpecEducation.builder().userSpec(userSpec).school(dto.getSchool()).major(dto.getMajor()).degree(dto.getDegree()).startDate(dto.getStartDate()!=null?LocalDate.parse(dto.getStartDate(),dateFormatter):null).endDate(dto.getEndDate()!=null?LocalDate.parse(dto.getEndDate(),dateFormatter):null).isCurrent(dto.getIsCurrent()!=null?dto.getIsCurrent():false).gpa(dto.getGpa()).maxGpa(dto.getMaxGpa()).description(dto.getDescription()).displayOrder(dto.getDisplayOrder()!=null?dto.getDisplayOrder():0).build();}
    private SpecSkillDto convertToSpecSkillDto(SpecSkill entity){return SpecSkillDto.builder().id(entity.getId()).name(entity.getName()).category(entity.getCategory()).level(entity.getLevel()).displayOrder(entity.getDisplayOrder()).build();}
    private SpecCertificateDto convertToSpecCertificateDto(SpecCertificate entity){return SpecCertificateDto.builder().id(entity.getId()).name(entity.getName()).organization1(entity.getOrganization1()).acquisitionDate(entity.getAcquisitionDate()!=null?entity.getAcquisitionDate().format(dateFormatter):null).expirationDate(entity.getExpirationDate()!=null?entity.getExpirationDate().format(dateFormatter):null).certificateNumber(entity.getCertificateNumber()).displayOrder(entity.getDisplayOrder()).build();}
    private SpecCertificate convertToSpecCertificateEntity(SpecCertificateDto dto,UserSpec userSpec){
        System.out.println("=== DTO를 엔티티로 변환 중 ===");
        System.out.println("DTO: " + dto.toString());
        System.out.println("organization 값: " + dto.getOrganization1());
        
        SpecCertificate entity = SpecCertificate.builder()
                .userSpec(userSpec)
                .name(dto.getName())
                .organization1(dto.getOrganization1())
                .acquisitionDate(dto.getAcquisitionDate()!=null?LocalDate.parse(dto.getAcquisitionDate(),dateFormatter):null)
                .expirationDate(dto.getExpirationDate()!=null?LocalDate.parse(dto.getExpirationDate(),dateFormatter):null)
                .certificateNumber(dto.getCertificateNumber())
                .displayOrder(dto.getDisplayOrder()!=null?dto.getDisplayOrder():0)
                .build();
                
        System.out.println("생성된 엔티티 organization: " + entity.getOrganization1());
        return entity;
    }
    private SpecLanguageDto convertToSpecLanguageDto(SpecLanguage entity){return SpecLanguageDto.builder().id(entity.getId()).language(entity.getName()).level(entity.getLevel()).testName(entity.getTestName()).score(entity.getScore()).acquisitionDate(entity.getAcquisitionDate()!=null?entity.getAcquisitionDate().format(dateFormatter):null).displayOrder(entity.getDisplayOrder()).build();}
    private SpecLanguage convertToSpecLanguageEntity(SpecLanguageDto dto,UserSpec userSpec){return SpecLanguage.builder().userSpec(userSpec).name(dto.getLanguage()).level(dto.getLevel()).testName(dto.getTestName()).score(dto.getScore()).acquisitionDate(dto.getAcquisitionDate()!=null?LocalDate.parse(dto.getAcquisitionDate(),dateFormatter):null).displayOrder(dto.getDisplayOrder()!=null?dto.getDisplayOrder():0).build();}
    private SpecProjectDto convertToSpecProjectDto(SpecProject entity){return SpecProjectDto.builder().id(entity.getId()).name(entity.getName()).description(entity.getDescription()).startDate(entity.getStartDate()!=null?entity.getStartDate().format(dateFormatter):null).endDate(entity.getEndDate()!=null?entity.getEndDate().format(dateFormatter):null).technologies(entity.getTechnologies()).url(entity.getUrl()).displayOrder(entity.getDisplayOrder()).build();}
    private SpecProject convertToSpecProjectEntity(SpecProjectDto dto,UserSpec userSpec){return SpecProject.builder().userSpec(userSpec).name(dto.getName()).description(dto.getDescription()).startDate(dto.getStartDate()!=null?LocalDate.parse(dto.getStartDate(),dateFormatter):null).endDate(dto.getEndDate()!=null?LocalDate.parse(dto.getEndDate(),dateFormatter):null).technologies(dto.getTechnologies()).url(dto.getUrl()).displayOrder(dto.getDisplayOrder()!=null?dto.getDisplayOrder():0).build();}
    private SpecActivityDto convertToSpecActivityDto(SpecActivity entity){return SpecActivityDto.builder().id(entity.getId()).name(entity.getName()).organization(entity.getOrganization()).startDate(entity.getStartDate()!=null?entity.getStartDate().format(dateFormatter):null).endDate(entity.getEndDate()!=null?entity.getEndDate().format(dateFormatter):null).description(entity.getDescription()).displayOrder(entity.getDisplayOrder()).build();}
    private SpecActivity convertToSpecActivityEntity(SpecActivityDto dto,UserSpec userSpec){return SpecActivity.builder().userSpec(userSpec).name(dto.getName()).organization(dto.getOrganization()).startDate(dto.getStartDate()!=null?LocalDate.parse(dto.getStartDate(),dateFormatter):null).endDate(dto.getEndDate()!=null?LocalDate.parse(dto.getEndDate(),dateFormatter):null).description(dto.getDescription()).displayOrder(dto.getDisplayOrder()!=null?dto.getDisplayOrder():0).build();}
    private SpecLinkDto convertToSpecLinkDto(SpecLink entity){return SpecLinkDto.builder().id(entity.getId()).title(entity.getTitle()).url(entity.getUrl()).type(entity.getType()).displayOrder(entity.getDisplayOrder()).build();}
    private SpecLink convertToSpecLinkEntity(SpecLinkDto dto,UserSpec userSpec){return SpecLink.builder().userSpec(userSpec).title(dto.getTitle()).url(dto.getUrl()).type(dto.getType()!=null?dto.getType():"기타").displayOrder(dto.getDisplayOrder()!=null?dto.getDisplayOrder():0).build();}
    private SpecMilitaryDto convertToSpecMilitaryDto(SpecMilitary entity){return SpecMilitaryDto.builder().id(entity.getId()).serviceType(entity.getServiceType()).militaryBranch(entity.getMilitaryBranch()).rank(entity.getRank()).startDate(entity.getStartDate()!=null?entity.getStartDate().format(dateFormatter):null).endDate(entity.getEndDate()!=null?entity.getEndDate().format(dateFormatter):null).exemptionReason(entity.getExemptionReason()).build();}
    private SpecMilitary convertToSpecMilitaryEntity(SpecMilitaryDto dto,UserSpec userSpec){return SpecMilitary.builder().userSpec(userSpec).serviceType(dto.getServiceType()).militaryBranch(dto.getMilitaryBranch()).rank(dto.getRank()).startDate(dto.getStartDate()!=null?LocalDate.parse(dto.getStartDate(),dateFormatter):null).endDate(dto.getEndDate()!=null?LocalDate.parse(dto.getEndDate(),dateFormatter):null).exemptionReason(dto.getExemptionReason()).build();}

}

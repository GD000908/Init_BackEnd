package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.UserSpecService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spec")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}) // 수정된 부분: "*" 대신 프론트엔드 주소를 명시적으로 지정
public class SpecController {

    private final UserSpecService userSpecService;

    /**
     * 전체 스펙 데이터 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<SpecApiResponse<UserSpecDto>> getUserSpec(@PathVariable Long userId) {
        try {
            UserSpecDto userSpec = userSpecService.getUserSpec(userId);
            return ResponseEntity.ok(SpecApiResponse.success(userSpec));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("스펙 데이터를 조회할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 프로필 조회
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<SpecApiResponse<SpecProfileDto>> getSpecProfile(@PathVariable Long userId) {
        try {
            SpecProfileDto profile = userSpecService.getSpecProfile(userId);
            return ResponseEntity.ok(SpecApiResponse.success(profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("프로필을 조회할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 프로필 업데이트
     */
    @PutMapping("/{userId}/profile")
    public ResponseEntity<SpecApiResponse<SpecProfileDto>> updateSpecProfile(
            @PathVariable Long userId,
            @RequestBody UpdateSpecProfileRequest request) {
        try {
            SpecProfileDto updatedProfile = userSpecService.updateSpecProfile(userId, request);
            return ResponseEntity.ok(SpecApiResponse.success("프로필이 성공적으로 업데이트되었습니다.", updatedProfile));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("프로필을 업데이트할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 경력 통계 조회
     */
    @GetMapping("/{userId}/career-stats")
    public ResponseEntity<SpecApiResponse<SpecCareerStatsDto>> getSpecCareerStats(@PathVariable Long userId) {
        try {
            SpecCareerStatsDto careerStats = userSpecService.getSpecCareerStats(userId);
            return ResponseEntity.ok(SpecApiResponse.success(careerStats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("경력 통계를 조회할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 경력 통계 업데이트
     */
    @PutMapping("/{userId}/career-stats")
    public ResponseEntity<SpecApiResponse<SpecCareerStatsDto>> updateSpecCareerStats(
            @PathVariable Long userId,
            @RequestBody UpdateSpecCareerStatsRequest request) {
        try {
            SpecCareerStatsDto updatedStats = userSpecService.updateSpecCareerStats(userId, request);
            return ResponseEntity.ok(SpecApiResponse.success("경력 통계가 성공적으로 업데이트되었습니다.", updatedStats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("경력 통계를 업데이트할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 업무 경력 조회
     */
    @GetMapping("/{userId}/careers")
    public ResponseEntity<SpecApiResponse<List<SpecCareerDto>>> getSpecCareers(@PathVariable Long userId) {
        try {
            List<SpecCareerDto> careers = userSpecService.getSpecCareers(userId);
            return ResponseEntity.ok(SpecApiResponse.success(careers));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("업무 경력을 조회할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 업무 경력 업데이트
     */
    @PutMapping("/{userId}/careers")
    public ResponseEntity<SpecApiResponse<List<SpecCareerDto>>> updateSpecCareers(
            @PathVariable Long userId,
            @RequestBody List<SpecCareerDto> careers) {
        try {
            List<SpecCareerDto> updatedCareers = userSpecService.updateSpecCareers(userId, careers);
            return ResponseEntity.ok(SpecApiResponse.success("업무 경력이 성공적으로 업데이트되었습니다.", updatedCareers));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("업무 경력을 업데이트할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 학력 조회
     */
    @GetMapping("/{userId}/educations")
    public ResponseEntity<SpecApiResponse<List<SpecEducationDto>>> getSpecEducations(@PathVariable Long userId) {
        try {
            List<SpecEducationDto> educations = userSpecService.getSpecEducations(userId);
            return ResponseEntity.ok(SpecApiResponse.success(educations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("학력을 조회할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 학력 업데이트
     */
    @PutMapping("/{userId}/educations")
    public ResponseEntity<SpecApiResponse<List<SpecEducationDto>>> updateSpecEducations(
            @PathVariable Long userId,
            @RequestBody List<SpecEducationDto> educations) {
        try {
            List<SpecEducationDto> updatedEducations = userSpecService.updateSpecEducations(userId, educations);
            return ResponseEntity.ok(SpecApiResponse.success("학력이 성공적으로 업데이트되었습니다.", updatedEducations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("학력을 업데이트할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 스킬 조회 - 수정됨
     */
    @GetMapping("/{userId}/skills")
    public ResponseEntity<SpecApiResponse<List<String>>> getSpecSkills(@PathVariable Long userId) {
        try {
            List<String> skills = userSpecService.getSpecSkillNames(userId); // 조회 전용 메서드 사용
            return ResponseEntity.ok(SpecApiResponse.success(skills));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("스킬을 조회할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 스킬 업데이트
     */
    @PutMapping("/{userId}/skills")
    public ResponseEntity<SpecApiResponse<List<String>>> updateSpecSkills(
            @PathVariable Long userId,
            @RequestBody List<String> skills) {
        try {
            List<String> updatedSkills = userSpecService.updateSpecSkills(userId, skills);
            return ResponseEntity.ok(SpecApiResponse.success("스킬이 성공적으로 업데이트되었습니다.", updatedSkills));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("스킬을 업데이트할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 자격증 조회
     */
    @GetMapping("/{userId}/certificates")
    public ResponseEntity<SpecApiResponse<List<SpecCertificateDto>>> getSpecCertificates(@PathVariable Long userId) {
        try {
            List<SpecCertificateDto> certificates = userSpecService.getSpecCertificates(userId);
            return ResponseEntity.ok(SpecApiResponse.success(certificates));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("자격증을 조회할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 자격증 업데이트
     */
    @PutMapping("/{userId}/certificates")
    public ResponseEntity<SpecApiResponse<List<SpecCertificateDto>>> updateSpecCertificates(
            @PathVariable Long userId,
            @RequestBody List<SpecCertificateDto> certificates) {
        try {
            // 디버깅을 위한 로그 추가
            System.out.println("=== 자격증 업데이트 요청 받음 ===");
            System.out.println("User ID: " + userId);
            System.out.println("자격증 개수: " + certificates.size());
            for (SpecCertificateDto cert : certificates) {
                System.out.println("자격증 정보: " + cert.toString());
                System.out.println("- 이름: " + cert.getName());
                System.out.println("- 발급기관: " + cert.getOrganization1());
                System.out.println("- 취득일: " + cert.getAcquisitionDate());
            }
            
            List<SpecCertificateDto> updatedCertificates = userSpecService.updateSpecCertificates(userId, certificates);
            return ResponseEntity.ok(SpecApiResponse.success("자격증이 성공적으로 업데이트되었습니다.", updatedCertificates));
        } catch (Exception e) {
            e.printStackTrace(); // 예외 스택 트레이스 출력
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("자격증을 업데이트할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 어학 조회
     */
    @GetMapping("/{userId}/languages")
    public ResponseEntity<SpecApiResponse<List<SpecLanguageDto>>> getSpecLanguages(@PathVariable Long userId) {
        try {
            List<SpecLanguageDto> languages = userSpecService.getSpecLanguages(userId);
            return ResponseEntity.ok(SpecApiResponse.success(languages));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("어학 정보를 조회할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 어학 업데이트
     */
    @PutMapping("/{userId}/languages")
    public ResponseEntity<SpecApiResponse<List<SpecLanguageDto>>> updateSpecLanguages(
            @PathVariable Long userId,
            @RequestBody List<SpecLanguageDto> languages) {
        try {
            List<SpecLanguageDto> updatedLanguages = userSpecService.updateSpecLanguages(userId, languages);
            return ResponseEntity.ok(SpecApiResponse.success("어학 정보가 성공적으로 업데이트되었습니다.", updatedLanguages));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("어학 정보를 업데이트할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 프로젝트 조회
     */
    @GetMapping("/{userId}/projects")
    public ResponseEntity<SpecApiResponse<List<SpecProjectDto>>> getSpecProjects(@PathVariable Long userId) {
        try {
            List<SpecProjectDto> projects = userSpecService.getSpecProjects(userId);
            return ResponseEntity.ok(SpecApiResponse.success(projects));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("프로젝트를 조회할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 프로젝트 업데이트
     */
    @PutMapping("/{userId}/projects")
    public ResponseEntity<SpecApiResponse<List<SpecProjectDto>>> updateSpecProjects(
            @PathVariable Long userId,
            @RequestBody List<SpecProjectDto> projects) {
        try {
            List<SpecProjectDto> updatedProjects = userSpecService.updateSpecProjects(userId, projects);
            return ResponseEntity.ok(SpecApiResponse.success("프로젝트가 성공적으로 업데이트되었습니다.", updatedProjects));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("프로젝트를 업데이트할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 활동 조회
     */
    @GetMapping("/{userId}/activities")
    public ResponseEntity<SpecApiResponse<List<SpecActivityDto>>> getSpecActivities(@PathVariable Long userId) {
        try {
            List<SpecActivityDto> activities = userSpecService.getSpecActivities(userId);
            return ResponseEntity.ok(SpecApiResponse.success(activities));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("활동 정보를 조회할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 활동 업데이트
     */
    @PutMapping("/{userId}/activities")
    public ResponseEntity<SpecApiResponse<List<SpecActivityDto>>> updateSpecActivities(
            @PathVariable Long userId,
            @RequestBody List<SpecActivityDto> activities) {
        try {
            List<SpecActivityDto> updatedActivities = userSpecService.updateSpecActivities(userId, activities);
            return ResponseEntity.ok(SpecApiResponse.success("활동 정보가 성공적으로 업데이트되었습니다.", updatedActivities));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("활동 정보를 업데이트할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 링크 조회
     */
    @GetMapping("/{userId}/links")
    public ResponseEntity<SpecApiResponse<List<SpecLinkDto>>> getSpecLinks(@PathVariable Long userId) {
        try {
            List<SpecLinkDto> links = userSpecService.getSpecLinks(userId);
            return ResponseEntity.ok(SpecApiResponse.success(links));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("링크를 조회할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 링크 업데이트
     */
    @PutMapping("/{userId}/links")
    public ResponseEntity<SpecApiResponse<List<SpecLinkDto>>> updateSpecLinks(
            @PathVariable Long userId,
            @RequestBody List<SpecLinkDto> links) {
        try {
            List<SpecLinkDto> updatedLinks = userSpecService.updateSpecLinks(userId, links);
            return ResponseEntity.ok(SpecApiResponse.success("링크가 성공적으로 업데이트되었습니다.", updatedLinks));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("링크를 업데이트할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 병역 조회
     */
    @GetMapping("/{userId}/militaries")
    public ResponseEntity<SpecApiResponse<List<SpecMilitaryDto>>> getSpecMilitaries(@PathVariable Long userId) {
        try {
            List<SpecMilitaryDto> militaries = userSpecService.getSpecMilitaries(userId);
            return ResponseEntity.ok(SpecApiResponse.success(militaries));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("병역 정보를 조회할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 병역 업데이트
     */
    @PutMapping("/{userId}/militaries")
    public ResponseEntity<SpecApiResponse<List<SpecMilitaryDto>>> updateSpecMilitaries(
            @PathVariable Long userId,
            @RequestBody List<SpecMilitaryDto> militaries) {
        try {
            List<SpecMilitaryDto> updatedMilitaries = userSpecService.updateSpecMilitaries(userId, militaries);
            return ResponseEntity.ok(SpecApiResponse.success("병역 정보가 성공적으로 업데이트되었습니다.", updatedMilitaries));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("병역 정보를 업데이트할 수 없습니다: " + e.getMessage()));
        }
    }

    /**
     * 자기소개 업데이트
     */
    @PutMapping("/{userId}/introduction")
    public ResponseEntity<SpecApiResponse<String>> updateIntroduction(
            @PathVariable Long userId,
            @RequestBody String introduction) {
        try {
            SpecProfileDto profile = userSpecService.getSpecProfile(userId);

            UpdateSpecProfileRequest request = UpdateSpecProfileRequest.builder()
                    .name(profile.getName())
                    .email(profile.getEmail())
                    .phone(profile.getPhone())
                    .location(profile.getLocation())
                    .careerLevel(profile.getCareerLevel())
                    .jobTitle(profile.getJobTitle())
                    .birthDate(profile.getBirthDate())
                    .introduction(introduction)
                    .skills(profile.getSkills())
                    .build();

            userSpecService.updateSpecProfile(userId, request);
            return ResponseEntity.ok(SpecApiResponse.success("자기소개가 성공적으로 업데이트되었습니다.", introduction));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SpecApiResponse.error("자기소개를 업데이트할 수 없습니다: " + e.getMessage()));
        }
    }
}

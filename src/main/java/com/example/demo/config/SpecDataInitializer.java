package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpecDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserSpecRepository userSpecRepository;
    private final SpecSkillRepository specSkillRepository;
    private final SpecCareerRepository specCareerRepository;
    private final SpecEducationRepository specEducationRepository;

    @Override
    public void run(String... args) throws Exception {
        // 개발용 샘플 데이터 생성
        if (userRepository.count() == 0) {
            log.info("Creating sample spec data for development...");
            createSampleSpecData();
            log.info("Sample spec data created successfully!");
        }
    }

    private void createSampleSpecData() {
        // 사용자 생성
        User user = User.builder()
                .userId("testuser")
                .email("spec.user@example.com")
                .password("password123") // 실제로는 암호화 필요
                .name("스펙관리자")
                .profileImage(null)
                .isActive(true)
                .build();
        user = userRepository.save(user);

        // UserSpec 생성
        UserSpec userSpec = UserSpec.builder()
                .user(user)
                .phone("010-1234-5678")
                .address("서울특별시 강남구")
                .jobTitle("풀스택 개발자")
                .careerLevel("주니어")
                .introduction("안녕하세요! 새로운 기술을 배우고 적용하는 것을 좋아하는 개발자입니다. " +
                        "사용자 중심의 서비스를 만들기 위해 항상 고민하고 있습니다.")
                .careerGoal("시니어 개발자로 성장하여 팀을 리딩하고, 기술적 역량을 바탕으로 " +
                        "비즈니스 가치를 창출하는 개발자가 되고 싶습니다.")
                .build();
        userSpec = userSpecRepository.save(userSpec);

        // 스킬 생성
        List<String> skillNames = Arrays.asList(
                "React", "TypeScript", "Next.js", "Node.js", "Express.js",
                "Spring Boot", "Java", "Python", "MySQL", "MongoDB",
                "Docker", "AWS", "Git", "Tailwind CSS", "Figma"
        );
        
        for (int i = 0; i < skillNames.size(); i++) {
            SpecSkill skill = SpecSkill.builder()
                    .userSpec(userSpec)
                    .name(skillNames.get(i))
                    .category(getSkillCategory(skillNames.get(i)))
                    .level(3 + (i % 3)) // 3, 4, 5 레벨 순환
                    .displayOrder(i)
                    .build();
            specSkillRepository.save(skill);
        }

        // 경력 생성
        SpecCareer career1 = SpecCareer.builder()
                .userSpec(userSpec)
                .company("테크 스타트업")
                .department("개발팀")
                .position("프론트엔드 개발자")
                .startDate(LocalDate.of(2022, 3, 1))
                .endDate(LocalDate.of(2024, 2, 29))
                .isCurrent(false)
                .description("React와 TypeScript를 활용한 웹 애플리케이션 개발\n" +
                        "- 사용자 대시보드 및 관리자 페이지 개발\n" +
                        "- REST API 연동 및 상태 관리\n" +
                        "- 반응형 웹 디자인 구현\n" +
                        "- 코드 리뷰 및 기술 문서 작성")
                .displayOrder(0)
                .build();
        specCareerRepository.save(career1);

        SpecCareer career2 = SpecCareer.builder()
                .userSpec(userSpec)
                .company("이노베이션 컴퍼니")
                .department("풀스택팀")
                .position("풀스택 개발자")
                .startDate(LocalDate.of(2024, 3, 1))
                .endDate(null)
                .isCurrent(true)
                .description("프론트엔드와 백엔드를 아우르는 풀스택 개발\n" +
                        "- Next.js와 Spring Boot를 활용한 웹 서비스 개발\n" +
                        "- 데이터베이스 설계 및 최적화\n" +
                        "- AWS 인프라 구축 및 CI/CD 파이프라인 구성\n" +
                        "- 팀 내 기술 스택 도입 및 전파")
                .displayOrder(1)
                .build();
        specCareerRepository.save(career2);

        // 학력 생성
        SpecEducation education = SpecEducation.builder()
                .userSpec(userSpec)
                .school("한국대학교")
                .major("컴퓨터공학과")
                .degree("학사")
                .startDate(LocalDate.of(2018, 3, 1))
                .endDate(LocalDate.of(2022, 2, 28))
                .isCurrent(false)
                .gpa(3.8)
                .maxGpa(4.5)
                .description("전공: 컴퓨터공학\n" +
                        "주요 수강과목: 자료구조, 알고리즘, 데이터베이스, 소프트웨어공학\n" +
                        "졸업작품: 웹 기반 프로젝트 관리 시스템")
                .displayOrder(0)
                .build();
        specEducationRepository.save(education);

        log.info("Sample user created with ID: {}", user.getId());
    }

    private String getSkillCategory(String skillName) {
        if (Arrays.asList("React", "TypeScript", "Next.js", "Tailwind CSS").contains(skillName)) {
            return "Frontend";
        } else if (Arrays.asList("Node.js", "Express.js", "Spring Boot", "Java", "Python").contains(skillName)) {
            return "Backend";
        } else if (Arrays.asList("MySQL", "MongoDB").contains(skillName)) {
            return "Database";
        } else if (Arrays.asList("Docker", "AWS").contains(skillName)) {
            return "DevOps";
        } else if (Arrays.asList("Git", "Figma").contains(skillName)) {
            return "Tools";
        } else {
            return "기타";
        }
    }
}

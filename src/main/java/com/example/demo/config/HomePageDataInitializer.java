package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class HomePageDataInitializer {

    @Bean
    @Order(5) // 다른 초기화 후에 실행
    CommandLineRunner initHomePageData(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            DesiredConditionsRepository desiredConditionsRepository,
            ApplicationStatusRepository applicationStatusRepository,
            TodoItemRepository todoItemRepository
    ) {
        return args -> {
            // 사용자 1번이 있는지 확인
            userRepository.findById(1L).ifPresent(user -> {
                // UserProfile 생성
                if (!userProfileRepository.findByUserId(1L).isPresent()) {
                    UserProfile profile = new UserProfile();
                    profile.setUser(user);
                    profile.setName("박건도");
                    profile.setEmail("nagundo@naver.com");
                    profile.setCareerType("신입");
                    profile.setJobTitle("개발자");
                    profile.setMatching(true);
                    userProfileRepository.save(profile);
                    System.out.println("UserProfile created for user 1");
                }

                // DesiredConditions 생성
                if (!desiredConditionsRepository.findByUserId(1L).isPresent()) {
                    DesiredConditions conditions = new DesiredConditions();
                    conditions.setUser(user);
                    conditions.setJobs(Arrays.asList("웹 • SW 개발", "프론트엔드 개발"));
                    conditions.setLocations(Arrays.asList("서울특별시", "경기도"));
                    conditions.setSalary("3500");
                    conditions.setOthers(Arrays.asList("재택근무 가능", "유연근무제"));
                    desiredConditionsRepository.save(conditions);
                    System.out.println("DesiredConditions created for user 1");
                }

                // ApplicationStatus 샘플 데이터 생성
                if (applicationStatusRepository.findByUserId(1L).isEmpty()) {
                    ApplicationStatus app1 = new ApplicationStatus();
                    app1.setUser(user);
                    app1.setCompany("네이버");
                    app1.setCategory("프론트엔드");
                    app1.setStatus(ApplicationStatus.Status.서류_합격);
                    applicationStatusRepository.save(app1);

                    ApplicationStatus app2 = new ApplicationStatus();
                    app2.setUser(user);
                    app2.setCompany("카카오");
                    app2.setCategory("백엔드");
                    app2.setStatus(ApplicationStatus.Status.최종_합격);
                    applicationStatusRepository.save(app2);

                    ApplicationStatus app3 = new ApplicationStatus();
                    app3.setUser(user);
                    app3.setCompany("라인");
                    app3.setCategory("프론트엔드");
                    app3.setStatus(ApplicationStatus.Status.지원_완료);
                    applicationStatusRepository.save(app3);

                    ApplicationStatus app4 = new ApplicationStatus();
                    app4.setUser(user);
                    app4.setCompany("쿠팡");
                    app4.setCategory("백엔드");
                    app4.setStatus(ApplicationStatus.Status.불합격);
                    applicationStatusRepository.save(app4);

                    ApplicationStatus app5 = new ApplicationStatus();
                    app5.setUser(user);
                    app5.setCompany("토스");
                    app5.setCategory("프론트엔드");
                    app5.setStatus(ApplicationStatus.Status.지원_완료);
                    applicationStatusRepository.save(app5);

                    System.out.println("ApplicationStatus sample data created for user 1");
                }

                // TodoItem 샘플 데이터 생성
                if (todoItemRepository.findByUserId(1L).isEmpty()) {
                    TodoItem todo1 = new TodoItem();
                    todo1.setUser(user);
                    todo1.setText("이력서 업데이트하기");
                    todo1.setCompleted(false);
                    todoItemRepository.save(todo1);

                    TodoItem todo2 = new TodoItem();
                    todo2.setUser(user);
                    todo2.setText("자기소개서 작성하기");
                    todo2.setCompleted(true);
                    todoItemRepository.save(todo2);

                    TodoItem todo3 = new TodoItem();
                    todo3.setUser(user);
                    todo3.setText("포트폴리오 정리하기");
                    todo3.setCompleted(false);
                    todoItemRepository.save(todo3);

                    System.out.println("TodoItem sample data created for user 1");
                }
            });
        };
    }
}
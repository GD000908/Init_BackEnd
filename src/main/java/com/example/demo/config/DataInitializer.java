package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // 이미 사용자가 있는지 확인
        if (userRepository.count() == 0) {
            log.info("데이터베이스가 비어있습니다. 테스트 사용자를 생성합니다.");
            createTestUser();
        } else {
            log.info("사용자 데이터가 이미 존재합니다. 건너뜁니다.");
        }
    }

    private void createTestUser() {
        try {
            User testUser = User.builder()
                    .userId("test@example.com")
                    .password("password123") // 개발용 - 실제 운영에서는 암호화 필요
                    .name("테스트 사용자")
                    .profileImage(null)
                    .isActive(true)
                    .build();

            User savedUser = userRepository.save(testUser);
            log.info("테스트 사용자가 생성되었습니다. ID: {}, Email: {}",
                    savedUser.getId(), savedUser.getEmail());

        } catch (Exception e) {
            log.error("테스트 사용자 생성 중 오류 발생: ", e);
        }
    }
}
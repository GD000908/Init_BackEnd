package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.example.demo.repository")
public class SpecJpaConfig {
    // JPA 설정을 위한 구성 클래스
    // @CreationTimestamp, @UpdateTimestamp 활성화
}

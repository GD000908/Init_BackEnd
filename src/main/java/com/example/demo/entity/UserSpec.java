package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_specs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 기본 프로필 정보
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(name = "career_level", length = 50)
    private String careerLevel; // 신입, 경력, 시니어 등

    @Column(columnDefinition = "TEXT")
    private String introduction; // 자기소개

    @Column(name = "career_goal", columnDefinition = "TEXT")
    private String careerGoal; // 경력 목표

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계
    @OneToMany(mappedBy = "userSpec", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SpecCareer> specCareers = new ArrayList<>();

    @OneToMany(mappedBy = "userSpec", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SpecEducation> specEducations = new ArrayList<>();

    @OneToMany(mappedBy = "userSpec", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SpecSkill> specSkills = new ArrayList<>();

    @OneToMany(mappedBy = "userSpec", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SpecCertificate> specCertificates = new ArrayList<>();

    @OneToMany(mappedBy = "userSpec", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SpecProject> specProjects = new ArrayList<>();

    @OneToMany(mappedBy = "userSpec", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SpecActivity> specActivities = new ArrayList<>();

    @OneToMany(mappedBy = "userSpec", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SpecLanguage> specLanguages = new ArrayList<>();

    @OneToMany(mappedBy = "userSpec", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SpecLink> specLinks = new ArrayList<>();

    @OneToMany(mappedBy = "userSpec", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SpecMilitary> specMilitaries = new ArrayList<>();
}
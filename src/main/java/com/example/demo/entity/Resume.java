package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "is_primary", columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "is_public", columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean isPublic = false;

    // 개인정보 필드들
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String address;

    @Column(name = "birth_date", length = 10)
    private String birthDate;

    @Column(length = 10)
    private String gender;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    // URL 필드들
    @Column(name = "github_url", length = 500)
    private String githubUrl;

    @Column(name = "blog_url", length = 500)
    private String blogUrl;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    // 희망 직무 정보
    @Column(name = "job_category", length = 100)
    private String jobCategory;

    @Column(name = "company_type", length = 100)
    private String companyType;

    @Column(name = "preferred_location", length = 100)
    private String preferredLocation;

    @Column(name = "job_position", length = 100)
    private String jobPosition;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    // 연관관계 매핑 (Cascade 설정)
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkExperience> workExperiences = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Education> educations = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Certificate> certificates = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Language> languages = new ArrayList<>();

    // 편의 메서드
    public void setUser(User user) {
        this.user = user;
        if (user != null && !user.getResumes().contains(this)) {
            user.getResumes().add(this);
        }
    }

    public void setPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public void togglePublicStatus() {
        this.isPublic = !this.isPublic;
    }

    // 연관관계 편의 메서드들
    public void addWorkExperience(WorkExperience workExperience) {
        workExperiences.add(workExperience);
        workExperience.setResume(this);
    }

    public void addEducation(Education education) {
        educations.add(education);
        education.setResume(this);
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
        skill.setResume(this);
    }

    public void addCertificate(Certificate certificate) {
        certificates.add(certificate);
        certificate.setResume(this);
    }

    public void addLanguage(Language language) {
        languages.add(language);
        language.setResume(this);
    }
}
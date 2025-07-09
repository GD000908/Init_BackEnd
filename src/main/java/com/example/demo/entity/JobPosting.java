package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_postings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class    JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(length = 100)
    private String location;

    @Column(length = 100)
    private String position;

    @Column(length = 50)
    private String salary;

    @Column(length = 7)
    @Builder.Default
    private String color = "#4f46e5";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JobStatus status = JobStatus.ACTIVE;

    @Column(length = 500)
    private String description;

    @Column(length = 200)
    private String company;

    @Column(length = 100)
    private String department;

    @Column(length = 20)
    private String experienceLevel;

    @Column(length = 20)
    private String employmentType;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JobBookmark> bookmarks = new ArrayList<>();

    public enum JobStatus {
        ACTIVE, EXPIRED, UPCOMING, CLOSED
    }

    // 마감일 기준으로 상태 자동 계산
    public JobStatus calculateStatus() {
        LocalDate today = LocalDate.now();
        if (endDate.isBefore(today)) {
            return JobStatus.EXPIRED;
        } else if (startDate.isAfter(today)) {
            return JobStatus.UPCOMING;
        } else {
            return JobStatus.ACTIVE;
        }
    }
}
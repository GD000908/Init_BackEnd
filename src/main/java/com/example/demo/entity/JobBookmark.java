package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_bookmarks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "job_posting_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(length = 100)
    private String memo;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookmarkStatus status = BookmarkStatus.ACTIVE;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum BookmarkStatus {
        ACTIVE, ARCHIVED, DELETED
    }
}
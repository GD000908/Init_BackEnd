package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 완전 수정: 세 개 필드 모두 명확하게 정의
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private CommunityProfile author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_profile_id", nullable = false)
    private CommunityProfile communityProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "job_category", length = 50)
    private String jobCategory;

    @Column(name = "topic_category", length = 50)
    private String topicCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PostStatus status = PostStatus.PUBLISHED;

    @Column(name = "likes_count", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer likesCount = 0;

    @Column(name = "comments_count", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer commentsCount = 0;

    @Column(name = "bookmarks_count", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer bookmarksCount = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostHashtag> hashtags = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostLike> likes = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostBookmark> bookmarks = new ArrayList<>();

    // 편의 메서드
    public void incrementLikesCount() {
        this.likesCount++;
    }

    public void decrementLikesCount() {
        this.likesCount = Math.max(0, this.likesCount - 1);
    }

    public void incrementCommentsCount() {
        this.commentsCount++;
    }

    public void decrementCommentsCount() {
        this.commentsCount = Math.max(0, this.commentsCount - 1);
    }

    public void incrementBookmarksCount() {
        this.bookmarksCount++;
    }

    public void decrementBookmarksCount() {
        this.bookmarksCount = Math.max(0, this.bookmarksCount - 1);
    }

    // 게시글 상태 enum
    public enum PostStatus {
        DRAFT, PUBLISHED
    }
}

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
@Table(name = "community_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;



    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(name = "company", length = 100)
    private String company;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "is_public", columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private Boolean isPublic = true;

    @Column(name = "allow_follow", columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private Boolean allowFollow = true;

    @Column(name = "posts_count", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer postsCount = 0;

    @Column(name = "followers_count", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer followersCount = 0;

    @Column(name = "following_count", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer followingCount = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 연관관계
    @JsonIgnore
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Post> posts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Follow> following = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Follow> followers = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostBookmark> bookmarks = new ArrayList<>();

    // 편의 메서드
    public void incrementPostsCount() {
        this.postsCount++;
    }

    public void decrementPostsCount() {
        this.postsCount = Math.max(0, this.postsCount - 1);
    }

    public void incrementFollowersCount() {
        this.followersCount++;
    }

    public void decrementFollowersCount() {
        this.followersCount = Math.max(0, this.followersCount - 1);
    }

    public void incrementFollowingCount() {
        this.followingCount++;
    }

    public void decrementFollowingCount() {
        this.followingCount = Math.max(0, this.followingCount - 1);
    }
}

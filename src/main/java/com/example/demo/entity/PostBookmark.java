package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_bookmarks", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private CommunityProfile user;

    @Column(name = "folder", length = 100)
    private String folder;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 생성자
    public PostBookmark(Post post, CommunityProfile user) {
        this.post = post;
        this.user = user;
    }

    public PostBookmark(Post post, CommunityProfile user, String folder) {
        this.post = post;
        this.user = user;
        this.folder = folder;
    }
}

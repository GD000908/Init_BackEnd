package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_hashtags", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "hashtag"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostHashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "hashtag", nullable = false, length = 50)
    private String hashtag;

    // 생성자
    public PostHashtag(Post post, String hashtag) {
        this.post = post;
        this.hashtag = hashtag;
    }
}

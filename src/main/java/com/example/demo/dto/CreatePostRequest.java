package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostRequest {
    
    @NotBlank(message = "내용은 필수입니다")
    private String content;
    
    private String imageUrl;
    
    private String jobCategory;
    
    private String topicCategory;
    
    private String status; // DRAFT, PUBLISHED
    
    private List<String> hashtags;
}

package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommentRequest {
    
    @NotBlank(message = "댓글 내용은 필수입니다")
    private String content;
    
    private Long userId; // 프론트엔드에서 전송하는 사용자 ID (검증용)
}

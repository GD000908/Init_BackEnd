package com.example.demo.dto;

import com.example.demo.entity.JobBookmark;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobBookmarkDto {
    private Long id;
    private Long userId;
    private JobPostingDto jobPosting;
    private String memo;
    private JobBookmark.BookmarkStatus status;
    private LocalDateTime createdAt;

    public static JobBookmarkDto from(JobBookmark bookmark) {
        return JobBookmarkDto.builder()
                .id(bookmark.getId())
                .userId(bookmark.getUser().getId())
                .jobPosting(JobPostingDto.from(bookmark.getJobPosting()))
                .memo(bookmark.getMemo())
                .status(bookmark.getStatus())
                .createdAt(bookmark.getCreatedAt())
                .build();
    }

    // 프론트엔드에서 사용할 간단한 형태
    public static JobBookmarkDto fromSimple(JobBookmark bookmark) {
        return JobBookmarkDto.builder()
                .id(bookmark.getId())
                .jobPosting(JobPostingDto.from(bookmark.getJobPosting()))
                .memo(bookmark.getMemo())
                .status(bookmark.getStatus())
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
}
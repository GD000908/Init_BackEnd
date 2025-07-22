package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusDto {
    private Long id;
    private Long userId;
    private String company;
    private String category;
    private String status; // String 타입으로 유지 (기존 프론트엔드와 호환)
    private LocalDate deadline;

    // 🔥 프론트엔드 호환을 위한 String 변환 메서드
    public String getDeadlineAsString() {
        return deadline != null ? deadline.toString() : "";
    }

    public void setDeadlineFromString(String deadlineStr) {
        if (deadlineStr != null && !deadlineStr.trim().isEmpty()) {
            try {
                this.deadline = LocalDate.parse(deadlineStr);
            } catch (Exception e) {
                this.deadline = null;
            }
        } else {
            this.deadline = null;
        }
    }
}
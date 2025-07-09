package com.example.demo.controller;

import com.example.demo.dto.ResumeDto;
import com.example.demo.dto.ApiResponse;
import com.example.demo.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResumeDto.ListResponse>>> getResumeList(
            @RequestHeader("X-User-Id") Long userId) {

        List<ResumeDto.ListResponse> resumes = resumeService.getResumeList(userId);
        return ResponseEntity.ok(ApiResponse.success("이력서 목록을 성공적으로 조회했습니다.", resumes));
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<ResumeDto.Response>> getResume(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long userId) {

        ResumeDto.Response resume = resumeService.getResume(resumeId, userId);
        return ResponseEntity.ok(ApiResponse.success("이력서를 성공적으로 조회했습니다.", resume));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ResumeDto.Response>> createResume(
            @RequestBody ResumeDto.CreateRequest request,
            @RequestHeader("X-User-Id") Long userId) {

        ResumeDto.Response resume = resumeService.createResume(userId, request);
        return ResponseEntity.ok(ApiResponse.success("이력서가 성공적으로 생성되었습니다.", resume));
    }

    @PutMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<ResumeDto.Response>> updateResume(
            @PathVariable Long resumeId,
            @RequestBody ResumeDto.UpdateRequest request,
            @RequestHeader("X-User-Id") Long userId) {

        ResumeDto.Response resume = resumeService.updateResume(resumeId, userId, request);
        return ResponseEntity.ok(ApiResponse.success("이력서가 성공적으로 수정되었습니다.", resume));
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<Void>> deleteResume(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long userId) {

        resumeService.deleteResume(resumeId, userId);
        return ResponseEntity.ok(ApiResponse.success("이력서가 성공적으로 삭제되었습니다.", null));
    }

    @PatchMapping("/{resumeId}/primary")
    public ResponseEntity<ApiResponse<ResumeDto.Response>> setPrimaryResume(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long userId) {

        ResumeDto.Response resume = resumeService.setPrimaryResume(resumeId, userId);
        return ResponseEntity.ok(ApiResponse.success("대표 이력서가 설정되었습니다.", resume));
    }

    @PatchMapping("/{resumeId}/public")
    public ResponseEntity<ApiResponse<ResumeDto.Response>> togglePublicStatus(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long userId) {

        ResumeDto.Response resume = resumeService.togglePublicStatus(resumeId, userId);
        return ResponseEntity.ok(ApiResponse.success("이력서 공개 상태가 변경되었습니다.", resume));
    }

    @PostMapping("/{resumeId}/copy")
    public ResponseEntity<ApiResponse<ResumeDto.Response>> copyResume(
            @PathVariable Long resumeId,
            @RequestHeader("X-User-Id") Long userId) {

        ResumeDto.Response resume = resumeService.copyResume(resumeId, userId);
        return ResponseEntity.ok(ApiResponse.success("이력서가 성공적으로 복사되었습니다.", resume));
    }
}
package com.example.demo.controller;

import com.example.demo.dto.CoverLetterDto;
import com.example.demo.service.CoverLetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cover-letters")
@RequiredArgsConstructor
public class CoverLetterController {

    private final CoverLetterService coverLetterService;

    /**
     * 자기소개서 생성
     * 헤더에서 X-User-ID를 받아서 DTO에 설정
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCoverLetter(
            @RequestBody CoverLetterDto dto,
            @RequestHeader("X-User-ID") Long userId) {

        dto.setUserId(userId);
        Long id = coverLetterService.save(dto);
        return ResponseEntity.ok(Map.of("id", id));
    }

    /**
     * 자기소개서 목록 조회 (사용자 ID 기준)
     */
    @GetMapping
    public ResponseEntity<List<CoverLetterDto>> getCoverLetters(
            @RequestHeader("X-User-ID") Long userId) {

        List<CoverLetterDto> list = coverLetterService.getByUserId(userId);
        return ResponseEntity.ok(list);
    }

    /**
     * 자기소개서 단건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<CoverLetterDto> getCoverLetter(@PathVariable Long id) {
        CoverLetterDto dto = coverLetterService.getById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * 자기소개서 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCoverLetter(
            @PathVariable Long id,
            @RequestBody CoverLetterDto dto,
            @RequestHeader("X-User-ID") Long userId) {

        dto.setUserId(userId);
        coverLetterService.update(id, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * 자기소개서 삭제
     * 권한 체크를 위해 사용자 ID도 함께 받음
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCoverLetter(
            @PathVariable Long id,
            @RequestHeader("X-User-ID") Long userId) {

        coverLetterService.delete(id, userId);
        return ResponseEntity.ok(Map.of("message", "자기소개서가 삭제되었습니다."));
    }
}

package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ImageUploadController {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${server.host:localhost}")
    private String serverHost;

    /**
     * 이미지 업로드
     */
    @PostMapping("/image")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("image") MultipartFile file) {
        
        log.info("이미지 업로드 요청: fileName={}, size={}", file.getOriginalFilename(), file.getSize());
        
        try {
            // 파일 유효성 검사
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.<Map<String, String>>builder()
                    .success(false)
                    .message("업로드할 파일이 없습니다")
                    .build());
            }

            // 파일 크기 체크 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(ApiResponse.<Map<String, String>>builder()
                    .success(false)
                    .message("파일 크기는 5MB 이하여야 합니다")
                    .build());
            }

            // 파일 형식 체크
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(ApiResponse.<Map<String, String>>builder()
                    .success(false)
                    .message("이미지 파일만 업로드 가능합니다")
                    .build());
            }

            // 허용된 이미지 형식 체크
            if (!isAllowedImageType(contentType)) {
                return ResponseEntity.badRequest().body(ApiResponse.<Map<String, String>>builder()
                    .success(false)
                    .message("지원하지 않는 이미지 형식입니다. (JPEG, PNG, GIF, WebP만 지원)")
                    .build());
            }

            // 업로드 디렉토리 생성 (프로젝트 루트 기준)
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadDir = Paths.get(uploadPath, "images", datePath);
            
            // 디렉토리가 없으면 생성
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("📁 업로드 디렉토리 생성: {}", uploadDir.toAbsolutePath());
            }

            // 파일명 생성 (UUID + 원본 확장자)
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + extension;
            
            // 파일 저장
            Path filePath = uploadDir.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // URL 생성 - 환경에 따라 동적으로 생성
            String imageUrl = "/uploads/images/" + datePath + "/" + newFilename;
            String fullImageUrl = "http://" + serverHost + ":" + serverPort + imageUrl;
            
            log.info("📸 이미지 URL 생성: {}", imageUrl);
            log.info("🔗 전체 URL: {}", fullImageUrl);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("fullImageUrl", fullImageUrl);
            response.put("originalName", originalFilename);
            response.put("size", String.valueOf(file.getSize()));

            log.info("이미지 업로드 성공: imageUrl={}", imageUrl);

            return ResponseEntity.ok(ApiResponse.<Map<String, String>>builder()
                .success(true)
                .data(response)
                .message("이미지 업로드가 완료되었습니다")
                .build());

        } catch (IOException e) {
            log.error("이미지 업로드 실패", e);
            return ResponseEntity.internalServerError().body(ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("이미지 업로드 중 오류가 발생했습니다")
                .build());
        }
    }

    /**
     * 여러 이미지 업로드
     */
    @PostMapping("/images")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadImages(
            @RequestParam("images") MultipartFile[] files) {
        
        log.info("다중 이미지 업로드 요청: count={}", files.length);
        
        if (files.length > 10) {
            return ResponseEntity.badRequest().body(ApiResponse.<Map<String, Object>>builder()
                .success(false)
                .message("한 번에 최대 10개의 이미지만 업로드할 수 있습니다")
                .build());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("uploadedImages", new java.util.ArrayList<>());
        response.put("failedImages", new java.util.ArrayList<>());

        for (MultipartFile file : files) {
            try {
                ResponseEntity<ApiResponse<Map<String, String>>> result = uploadImage(file);
                if (result.getBody() != null && result.getBody().isSuccess()) {
                    ((java.util.List<Map<String, String>>) response.get("uploadedImages"))
                        .add(result.getBody().getData());
                } else {
                    Map<String, String> failedFile = new HashMap<>();
                    failedFile.put("fileName", file.getOriginalFilename());
                    failedFile.put("error", result.getBody() != null ? result.getBody().getMessage() : "업로드 실패");
                    ((java.util.List<Map<String, String>>) response.get("failedImages"))
                        .add(failedFile);
                }
            } catch (Exception e) {
                Map<String, String> failedFile = new HashMap<>();
                failedFile.put("fileName", file.getOriginalFilename());
                failedFile.put("error", e.getMessage());
                ((java.util.List<Map<String, String>>) response.get("failedImages"))
                    .add(failedFile);
            }
        }

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
            .success(true)
            .data(response)
            .message("이미지 업로드가 완료되었습니다")
            .build());
    }

    private boolean isAllowedImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp");
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}

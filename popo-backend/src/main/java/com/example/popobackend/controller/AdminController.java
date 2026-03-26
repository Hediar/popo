package com.example.popobackend.controller;

import com.example.popobackend.service.EmbeddingRegenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 관리자 전용 컨트롤러
 * 임베딩 재생성 등의 관리 작업 수행
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private EmbeddingRegenerationService embeddingRegenerationService;

    /**
     * 모든 포트폴리오 데이터의 임베딩 재생성
     * POST /api/admin/regenerate-embeddings
     */
    @PostMapping("/regenerate-embeddings")
    public ResponseEntity<Map<String, Object>> regenerateAllEmbeddings() {
        log.info("[AdminController] 전체 임베딩 재생성 요청");

        try {
            int regeneratedCount = embeddingRegenerationService.regenerateAllEmbeddings();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "임베딩 재생성 완료");
            response.put("regeneratedCount", regeneratedCount);
            response.put("timestamp", LocalDateTime.now());

            log.info("[AdminController] 임베딩 재생성 성공: {}개", regeneratedCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[AdminController] 임베딩 재생성 실패: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "임베딩 재생성 실패: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 임베딩이 없는 항목만 재생성
     * POST /api/admin/regenerate-missing-embeddings
     */
    @PostMapping("/regenerate-missing-embeddings")
    public ResponseEntity<Map<String, Object>> regenerateMissingEmbeddings() {
        log.info("[AdminController] 누락 임베딩 재생성 요청");

        try {
            int regeneratedCount = embeddingRegenerationService.regenerateMissingEmbeddings();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "누락 임베딩 재생성 완료");
            response.put("regeneratedCount", regeneratedCount);
            response.put("timestamp", LocalDateTime.now());

            log.info("[AdminController] 누락 임베딩 재생성 성공: {}개", regeneratedCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[AdminController] 누락 임베딩 재생성 실패: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "누락 임베딩 재생성 실패: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 특정 타입의 임베딩만 재생성
     * POST /api/admin/regenerate-embeddings/{type}
     *
     * @param type 타입 (project, career, education, etc.)
     */
    @PostMapping("/regenerate-embeddings/{type}")
    public ResponseEntity<Map<String, Object>> regenerateEmbeddingsByType(@PathVariable String type) {
        log.info("[AdminController] type='{}' 임베딩 재생성 요청", type);

        try {
            int regeneratedCount = embeddingRegenerationService.regenerateEmbeddingsByType(type);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "type='" + type + "' 임베딩 재생성 완료");
            response.put("type", type);
            response.put("regeneratedCount", regeneratedCount);
            response.put("timestamp", LocalDateTime.now());

            log.info("[AdminController] type='{}' 임베딩 재생성 성공: {}개", type, regeneratedCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[AdminController] type='{}' 임베딩 재생성 실패: {}", type, e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "type='" + type + "' 임베딩 재생성 실패: " + e.getMessage());
            errorResponse.put("type", type);
            errorResponse.put("timestamp", LocalDateTime.now());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}

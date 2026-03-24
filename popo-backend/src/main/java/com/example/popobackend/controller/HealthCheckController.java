package com.example.popobackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * API 정상 작동 확인용 헬스체크 컨트롤러
 */
@RestController
@RequestMapping("/api")
public class HealthCheckController {

    /**
     * 헬스체크 엔드포인트
     * GET /api/health
     *
     * @return 서버 상태 정보
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "POPO Backend API is running");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "popo-backend");
        response.put("version", "1.0.0");

        return ResponseEntity.ok(response);
    }

}

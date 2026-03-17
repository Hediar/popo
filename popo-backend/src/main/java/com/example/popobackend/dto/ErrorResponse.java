package com.example.popobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 에러 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private String errorCode;      // 에러 코드 (예: OPENAI_UNAUTHORIZED)
    private String message;         // 에러 메시지
    private int status;             // HTTP 상태 코드
    private LocalDateTime timestamp; // 에러 발생 시각

    public static ErrorResponse of(String errorCode, String message, int status) {
        return new ErrorResponse(errorCode, message, status, LocalDateTime.now());
    }
}

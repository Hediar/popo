package com.example.popobackend.exception;

import com.example.popobackend.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 핸들러
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * OpenAI API 관련 예외 처리
     */
    @ExceptionHandler(OpenAIException.class)
    public ResponseEntity<ErrorResponse> handleOpenAIException(OpenAIException e) {
        log.error("OpenAI Error: {} - {}", e.getErrorCode(), e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.of(
                e.getErrorCode(),
                e.getMessage(),
                e.getHttpStatus().value()
        );

        return ResponseEntity
                .status(e.getHttpStatus())
                .body(errorResponse);
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("Unexpected error occurred", e);

        ErrorResponse errorResponse = ErrorResponse.of(
                "INTERNAL_SERVER_ERROR",
                "서버에서 오류가 발생했습니다: " + e.getMessage(),
                500
        );

        return ResponseEntity
                .status(500)
                .body(errorResponse);
    }
}

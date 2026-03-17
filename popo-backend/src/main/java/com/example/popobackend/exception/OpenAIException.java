package com.example.popobackend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * OpenAI API 호출 중 발생하는 예외
 */
@Getter
public class OpenAIException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    public OpenAIException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public OpenAIException(String message, HttpStatus httpStatus, String errorCode, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    // 401 Unauthorized - API 키 문제
    public static OpenAIException unauthorized(String message, Throwable cause) {
        return new OpenAIException(message, HttpStatus.UNAUTHORIZED, "OPENAI_UNAUTHORIZED", cause);
    }

    // 429 Too Many Requests - Rate Limit
    public static OpenAIException rateLimitExceeded(String message, Throwable cause) {
        return new OpenAIException(message, HttpStatus.TOO_MANY_REQUESTS, "OPENAI_RATE_LIMIT", cause);
    }

    // 500 Internal Server Error - OpenAI 서버 에러
    public static OpenAIException serverError(String message, Throwable cause) {
        return new OpenAIException(message, HttpStatus.INTERNAL_SERVER_ERROR, "OPENAI_SERVER_ERROR", cause);
    }

    // 503 Service Unavailable - OpenAI 서비스 사용 불가
    public static OpenAIException serviceUnavailable(String message, Throwable cause) {
        return new OpenAIException(message, HttpStatus.SERVICE_UNAVAILABLE, "OPENAI_SERVICE_UNAVAILABLE", cause);
    }

    // 400 Bad Request - 잘못된 요청
    public static OpenAIException badRequest(String message, Throwable cause) {
        return new OpenAIException(message, HttpStatus.BAD_REQUEST, "OPENAI_BAD_REQUEST", cause);
    }

    // 기타 에러
    public static OpenAIException unknown(String message, Throwable cause) {
        return new OpenAIException(message, HttpStatus.INTERNAL_SERVER_ERROR, "OPENAI_UNKNOWN_ERROR", cause);
    }
}

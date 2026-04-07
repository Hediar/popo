package com.example.popobackend.controller;

import com.example.popobackend.dto.ChatRequest;
import com.example.popobackend.dto.ChatResponse;
import com.example.popobackend.service.ChatService;
import com.example.popobackend.util.NetworkUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 기존 동기 방식 채팅 (호환성 유지)
     * POST /api/chat/message
     */
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(
            @RequestBody ChatRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = NetworkUtils.getClientIp(httpRequest);

        log.info("[Chat] 요청 - sessionId: '{}', message: '{}'", request.getSessionId(), request.getMessage());

        String[] result = chatService.processMessage(
                request.getSessionId(),
                request.getMessage(),
                clientIp
        );

        log.info("[Chat] 응답 완료 - sessionId: '{}'", result[0]);

        ChatResponse response = new ChatResponse(result[0], result[1]);
        return ResponseEntity.ok(response);
    }

    /**
     * Streaming 방식 채팅 (SSE)
     * POST /api/chat/stream
     *
     * SSE 이벤트 형식:
     * - event: sessionId, data: {세션ID}     → 첫 번째 이벤트
     * - event: token, data: {토큰}           → 토큰 단위 스트리밍
     * - event: done, data: [DONE]            → 완료 신호
     * - event: error, data: {에러메시지}      → 에러 발생 시
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessage(
            @RequestBody ChatRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = NetworkUtils.getClientIp(httpRequest);
        SseEmitter emitter = new SseEmitter(120_000L); // 2분 타임아웃

        log.info("[ChatStream] 요청 - sessionId: '{}', message: '{}'", request.getSessionId(), request.getMessage());

        executor.execute(() -> {
            StringBuilder fullResponse = new StringBuilder();
            try {
                // 벡터 검색 + 세션 준비 (동기)
                ChatService.StreamContext ctx = chatService.processMessageStream(
                        request.getSessionId(),
                        request.getMessage(),
                        clientIp
                );

                // sessionId 전송
                emitter.send(SseEmitter.event()
                        .name("sessionId")
                        .data(ctx.sessionId()));

                // AI 응답 토큰 단위 streaming
                ctx.responseStream()
                        .doOnNext(token -> {
                            try {
                                fullResponse.append(token);
                                emitter.send(SseEmitter.event()
                                        .name("token")
                                        .data(token));
                            } catch (IOException e) {
                                log.warn("[ChatStream] 토큰 전송 실패 (클라이언트 연결 끊김)");
                                emitter.completeWithError(e);
                            }
                        })
                        .doOnComplete(() -> {
                            try {
                                // 대화 내역 저장
                                chatService.saveStreamedMessage(
                                        ctx.session(),
                                        ctx.existingMessages(),
                                        ctx.userMessage(),
                                        fullResponse.toString()
                                );

                                emitter.send(SseEmitter.event()
                                        .name("done")
                                        .data("[DONE]"));
                                emitter.complete();
                                log.info("[ChatStream] 완료 - sessionId: '{}'", ctx.sessionId());
                            } catch (IOException e) {
                                log.warn("[ChatStream] 완료 이벤트 전송 실패");
                                emitter.completeWithError(e);
                            }
                        })
                        .doOnError(error -> {
                            try {
                                log.error("[ChatStream] AI 응답 에러: {}", error.getMessage());
                                emitter.send(SseEmitter.event()
                                        .name("error")
                                        .data(error.getMessage()));
                                emitter.completeWithError(error);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        })
                        .subscribe();

            } catch (Exception e) {
                try {
                    log.error("[ChatStream] 처리 에러: {}", e.getMessage());
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("AI 응답 생성 중 오류가 발생했습니다."));
                    emitter.completeWithError(e);
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        return emitter;
    }
}
package com.example.popobackend.controller;

import com.example.popobackend.dto.ChatRequest;
import com.example.popobackend.dto.ChatResponse;
import com.example.popobackend.dto.MessageDto;
import com.example.popobackend.service.ChatService;
import com.example.popobackend.util.NetworkUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 채팅 메시지 처리
     * POST /api/chat/message
     *
     * @param request 채팅 요청 (sessionId, message)
     * @param httpRequest HTTP 요청 (IP 추출용)
     * @return 채팅 응답 (sessionId, AI 메시지)
     */
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(
            @RequestBody ChatRequest request,
            HttpServletRequest httpRequest
    ) {
        // 클라이언트 IP 추출
        String clientIp = NetworkUtils.getClientIp(httpRequest);

        // 메시지 처리 (sessionId와 AI 응답 반환)
        String[] result = chatService.processMessage(
                request.getSessionId(),
                request.getMessage(),
                clientIp
        );

        // 응답 생성
        ChatResponse response = new ChatResponse(
                result[0],  // sessionId
                result[1]   // AI 응답
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 세션의 대화 내역 조회
     * GET /api/chat/session/{sessionId}/messages
     *
     * @param sessionId 세션 ID
     * @return 대화 내역 목록
     */
    @GetMapping("/session/{sessionId}/messages")
    public ResponseEntity<List<MessageDto>> getSessionMessages(@PathVariable String sessionId) {
        List<MessageDto> messages = chatService.getSessionMessages(sessionId);
        return ResponseEntity.ok(messages);
    }

    /**
     * 세션 종료
     * POST /api/chat/session/{sessionId}/close
     *
     * @param sessionId 세션 ID
     * @return 성공 응답
     */
    @PostMapping("/session/{sessionId}/close")
    public ResponseEntity<Void> closeSession(@PathVariable String sessionId) {
        chatService.closeSession(sessionId);
        return ResponseEntity.ok().build();
    }
}


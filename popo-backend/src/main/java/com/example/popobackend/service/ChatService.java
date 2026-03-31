package com.example.popobackend.service;

import com.example.popobackend.dto.MessageDto;
import com.example.popobackend.dto.SearchResult;
import com.example.popobackend.entity.ChatSession;
import com.example.popobackend.entity.SessionStatus;
import com.example.popobackend.repository.ChatSessionRepository;
import com.example.popobackend.util.SessionIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private VectorSearchService vectorSearchService;

    @Autowired
    private OpenAIService openAIService;

    /**
     * 사용자 메시지 처리 및 AI 응답 생성
     *
     * @param sessionId 세션 ID (없으면 새로 생성)
     * @param userMessage 사용자 메시지
     * @param clientIp 클라이언트 IP
     * @return [sessionId, AI 응답 메시지]
     */
    @Transactional
    public String[] processMessage(String sessionId, String userMessage, String clientIp) {
        // 1. sessionId가 있으면 DB에서 세션 조회, 없으면 새로 생성
        ChatSession session;
        List<MessageDto> existingMessages;

        if (sessionId != null && !sessionId.isEmpty()) {
            // sessionId로 DB 조회
            session = chatSessionRepository.findBySessionId(sessionId).orElse(null);

            if (session != null) {
                existingMessages = session.getMessages() != null ? session.getMessages() : new ArrayList<>();
                System.out.println("[ChatService] 기존 세션 조회 성공 - sessionId: " + sessionId + ", 저장된 메시지: " + existingMessages.size() + "건");
            } else {
                // sessionId가 왔지만 DB에 없는 경우 → 새 세션 생성 (같은 sessionId 사용)
                System.out.println("[ChatService] sessionId '" + sessionId + "' DB에 없음 → 새 세션 생성");
                session = createNewSession(sessionId, clientIp);
                existingMessages = new ArrayList<>();
            }
        } else {
            // sessionId 없음 → 첫 대화, 새 세션 생성
            String newSessionId = SessionIdGenerator.generate();
            System.out.println("[ChatService] sessionId 없음 → 새 세션 생성: " + newSessionId);
            session = createNewSession(newSessionId, clientIp);
            existingMessages = new ArrayList<>();
        }

        // 2. 이전 대화 내역 추출 (최근 100개만)
        int totalMessages = existingMessages.size();
        int start = Math.max(0, totalMessages - 100);
        List<String> conversationHistory = existingMessages.subList(start, totalMessages).stream()
                .map(msg -> msg.getRole() + ": " + msg.getContent())
                .collect(Collectors.toList());

        System.out.println("[ChatService] 이전 대화 내역: " + totalMessages + "건 중 " + conversationHistory.size() + "건 사용");

        // 3. 벡터 검색 수행
        List<SearchResult> searchResults = vectorSearchService.search(userMessage);
        String context = vectorSearchService.buildContext(searchResults);

        // 4. AI 응답 생성 (이전 대화 내역 + 현재 질문)
        String aiResponse = openAIService.generateResponse(userMessage, context, conversationHistory);

        // 5. 사용자 메시지 + AI 응답 추가
        List<MessageDto> updatedMessages = new ArrayList<>(existingMessages);
        updatedMessages.add(new MessageDto("user", userMessage, LocalDateTime.now()));
        updatedMessages.add(new MessageDto("assistant", aiResponse, LocalDateTime.now()));

        // 6. 저장: 새 세션이면 JPA save, 기존 세션이면 네이티브 쿼리로 직접 UPDATE
        if (session.getId() == null) {
            // 새 세션 → INSERT
            session.setMessages(updatedMessages);
            chatSessionRepository.saveAndFlush(session);
            System.out.println("[ChatService] 새 세션 INSERT 완료 - sessionId: " + session.getSessionId() + ", DB id: " + session.getId() + ", 총 메시지: " + updatedMessages.size() + "건");
        } else {
            // 기존 세션 → 네이티브 쿼리로 JSONB 직접 UPDATE (Hibernate dirty checking 우회)
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                String messagesJson = mapper.writeValueAsString(updatedMessages);
                int updated = chatSessionRepository.updateMessagesBySessionId(session.getSessionId(), messagesJson);
                System.out.println("[ChatService] 기존 세션 UPDATE 완료 - sessionId: " + session.getSessionId() + ", 영향 row: " + updated + ", 총 메시지: " + updatedMessages.size() + "건");
            } catch (JsonProcessingException e) {
                System.out.println("[ChatService] JSON 직렬화 실패: " + e.getMessage());
                throw new RuntimeException("메시지 저장 실패", e);
            }
        }

        // 7. sessionId와 AI 응답 반환
        return new String[]{session.getSessionId(), aiResponse};
    }

    /**
     * 새 세션 생성 (DB 저장 전 객체만 생성)
     */
    private ChatSession createNewSession(String sessionId, String clientIp) {
        ChatSession session = new ChatSession();
        session.setSessionId(sessionId);
        session.setClientIp(clientIp);
        session.setStatus(SessionStatus.ACTIVE);
        session.setMessages(new ArrayList<>());
        return session;
    }

    /**
     * 세션 종료
     *
     * @param sessionId 세션 ID
     */
    @Transactional
    public void closeSession(String sessionId) {
        chatSessionRepository.findBySessionId(sessionId)
                .ifPresent(session -> {
                    session.setStatus(SessionStatus.COMPLETED);
                    chatSessionRepository.save(session);
                });
    }

    /**
     * 세션의 대화 내역 조회
     *
     * @param sessionId 세션 ID
     * @return 대화 내역
     */
    public List<MessageDto> getSessionMessages(String sessionId) {
        return chatSessionRepository.findBySessionId(sessionId)
                .map(ChatSession::getMessages)
                .orElse(new ArrayList<>());
    }
}


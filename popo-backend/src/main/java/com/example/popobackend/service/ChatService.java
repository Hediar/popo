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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

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
        // 1. 세션 가져오기 또는 생성
        ChatSession session = getOrCreateSession(sessionId, clientIp);

        // 2. 사용자 메시지 저장
        MessageDto userMsg = new MessageDto("user", userMessage, LocalDateTime.now());
        session.getMessages().add(userMsg);

        // 3. 벡터 검색 수행
        List<SearchResult> searchResults = vectorSearchService.search(userMessage);
        String context = vectorSearchService.buildContext(searchResults);

        // 4. 대화 내역 추출
        List<String> conversationHistory = session.getMessages().stream()
                .map(msg -> msg.getRole() + ": " + msg.getContent())
                .collect(Collectors.toList());

        // 5. AI 응답 생성
        String aiResponse = openAIService.generateResponse(userMessage, context, conversationHistory);

        // 6. AI 응답 저장
        MessageDto assistantMsg = new MessageDto("assistant", aiResponse, LocalDateTime.now());
        session.getMessages().add(assistantMsg);

        // 7. 세션 저장
        chatSessionRepository.save(session);

        // 8. sessionId와 AI 응답 반환
        return new String[]{session.getSessionId(), assistantMsg.getContent()};
    }

    /**
     * 세션 가져오기 또는 생성
     *
     * @param sessionId 세션 ID
     * @param clientIp 클라이언트 IP
     * @return ChatSession
     */
    private ChatSession getOrCreateSession(String sessionId, String clientIp) {
        if (sessionId != null && !sessionId.isEmpty()) {
            return chatSessionRepository.findBySessionId(sessionId)
                    .orElseGet(() -> createNewSession(sessionId, clientIp));
        } else {
            return createNewSession(SessionIdGenerator.generate(), clientIp);
        }
    }

    /**
     * 새 세션 생성
     *
     * @param sessionId 세션 ID
     * @param clientIp 클라이언트 IP
     * @return 새로운 ChatSession
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


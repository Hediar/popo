package com.example.popobackend.service;

import com.example.popobackend.exception.OpenAIException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private ProfileService profileService;

    private ChatClient chatClient;

    /**
     * ChatClient 초기화
     * 시스템 프롬프트를 기본으로 설정
     */
    private ChatClient getChatClient() {
        if (chatClient == null) {
            chatClient = chatClientBuilder
                    .defaultSystem(buildSystemPrompt())
                    .build();
        }
        return chatClient;
    }

    /**
     * RAG 패턴으로 AI 응답 생성
     * (Retrieval Augmented Generation)
     *
     * @param userMessage 사용자 메시지
     * @param context 벡터 검색으로 찾은 컨텍스트
     * @param conversationHistory 이전 대화 내역
     * @return AI 응답
     */
    public String generateResponse(String userMessage, String context, List<String> conversationHistory) {
        // RAG 프롬프트 구성
        String userPrompt = buildRAGPrompt(userMessage, context, conversationHistory);

        try {
            // OpenAI API 호출
            String response = getChatClient()
                    .prompt()
                    .user(userPrompt)
                    .call()
                    .content();

            return response;
        } catch (Exception e) {
            // 에러 메시지 분석
            String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

            // HTTP 에러 코드 또는 키워드 기반 예외 분류
            if (errorMessage.contains("401") || errorMessage.contains("unauthorized") || errorMessage.contains("invalid api key")) {
                throw OpenAIException.unauthorized("OpenAI API 인증에 실패했습니다. API 키를 확인해주세요.", e);
            } else if (errorMessage.contains("429") || errorMessage.contains("rate limit") || errorMessage.contains("too many requests")) {
                throw OpenAIException.rateLimitExceeded("OpenAI API 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.", e);
            } else if (errorMessage.contains("503") || errorMessage.contains("service unavailable")) {
                throw OpenAIException.serviceUnavailable("OpenAI 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요.", e);
            } else if (errorMessage.contains("500") || errorMessage.contains("internal server error")) {
                throw OpenAIException.serverError("OpenAI 서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
            } else if (errorMessage.contains("400") || errorMessage.contains("bad request") || errorMessage.contains("invalid request")) {
                throw OpenAIException.badRequest("잘못된 요청입니다. 요청 내용을 확인해주세요.", e);
            } else {
                throw OpenAIException.unknown("AI 응답 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        }
    }

    /**
     * RAG 패턴 프롬프트 구성
     * 시스템 역할 + 검색 컨텍스트 + 대화 내역 + 현재 질문
     *
     * @param userMessage 사용자 메시지
     * @param context 검색된 컨텍스트
     * @param conversationHistory 대화 내역
     * @return 완전한 프롬프트
     */
    private String buildRAGPrompt(String userMessage, String context, List<String> conversationHistory) {
        StringBuilder prompt = new StringBuilder();

        // 1. 시스템 역할
        prompt.append(buildSystemPrompt()).append("\n\n");

        // 2. 기본 프로필 정보 (DB에서 조회)
        String profileContext = profileService.buildProfileContext();
        prompt.append(profileContext).append("\n");

        // 3. 검색된 컨텍스트 (있는 경우에만)
        if (context != null && !context.isEmpty() && !context.contains("없습니다")) {
            prompt.append(context).append("\n");
            prompt.append("위 정보를 참고하여 답변해주세요.\n\n");
        }

        // 4. 대화 내역 (최근 5개만)
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            prompt.append("=== 이전 대화 ===\n");
            int start = Math.max(0, conversationHistory.size() - 5);
            for (int i = start; i < conversationHistory.size(); i++) {
                prompt.append(conversationHistory.get(i)).append("\n");
            }
            prompt.append("\n");
        }

        // 5. 현재 질문
        prompt.append("=== 현재 질문 ===\n");
        prompt.append("사용자: ").append(userMessage).append("\n\n");

        // 6. 답변 지침
        prompt.append("""
                
                ═══════════════════════════════════════
                [정체성]
                ═══════════════════════════════════════
                
                
                ═══════════════════════════════════════
                [나의 성격 및 상세 소개]
                ═══════════════════════════════════════
               
                
                ═══════════════════════════════════════
                [말투 스타일]
                ═══════════════════════════════════════
                
                
                ═══════════════════════════════════════
                [응답 규칙]
                ═══════════════════════════════════════
                - 검색된 정보를 최우선으로 활용하여 답변하세요
                - 프로젝트, 경력, 기술에 대해 구체적으로 소개하세요
                - 기술 스택, 역할, 성과를 명확히 언급하세요
                - 1인칭 시점으로 자연스럽게 대화하세요 ("제가", "저는")
                - 대화 맥락을 고려하여 자연스럽게 답변하세요
                - 검색된 정보에 없는 내용은 "해당 정보는 포트폴리오에 없습니다"라고 답하세요
                
                """);

        return prompt.toString();
    }

    /**
     * 시스템 프롬프트 생성
     * AI의 역할과 행동 방식 정의
     *
     * @return 시스템 프롬프트
     */
    private String buildSystemPrompt() {
        return """
                === 시스템 역할 ===
                당신은 포트폴리오 주인을 대신하여 방문자에게 소개하는 AI 어시스턴트입니다.

                당신의 역할:
                - 포트폴리오 주인의 프로젝트, 경력, 기술 스택을 친근하게 소개
                - 방문자의 질문에 맞춰 관련 정보를 자세히 설명
                - 검색된 정보를 바탕으로 정확하고 구체적으로 답변
                - 포트폴리오 주인을 1인칭 시점에서 소개 (예: "제가 진행한 프로젝트는...")
                - 기술적 내용을 이해하기 쉽게 설명

                답변 스타일:
                - 친근하고 자연스러운 대화체와 존댓말
                - 구체적인 프로젝트 내용과 성과 포함
                - 기술 스택과 역할을 명확히 설명
                - 검색된 정보가 없으면 솔직하게 모른다고 답변

                주의사항:
                - 검색된 정보에 없는 내용은 추측하지 말 것
                - 포트폴리오 주인을 3인칭으로 지칭하지 말 것
                - 방문자를 환영하고 추가 질문을 유도
                """;
    }
}


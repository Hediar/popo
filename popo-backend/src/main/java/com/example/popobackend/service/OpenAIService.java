package com.example.popobackend.service;

import com.example.popobackend.exception.OpenAIException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class OpenAIService {

    private static final Logger log = LoggerFactory.getLogger(OpenAIService.class);

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private ProfileService profileService;

    private ChatClient chatClient;

    private ChatClient getChatClient() {
        if (chatClient == null) {
            chatClient = chatClientBuilder
                    .defaultSystem(buildSystemPrompt())
                    .build();
        }
        return chatClient;
    }

    /**
     * 기존 동기 방식 응답 생성 (호환성 유지)
     */
    public String generateResponse(String userMessage, String context, List<String> conversationHistory) {
        String userPrompt = buildRAGPrompt(userMessage, context, conversationHistory);

        try {
            String response = getChatClient()
                    .prompt()
                    .user(userPrompt)
                    .call()
                    .content();

            return response;
        } catch (Exception e) {
            throw classifyException(e);
        }
    }

    /**
     * Streaming 방식 응답 생성
     * 토큰 단위로 Flux<String>을 반환하여 SSE로 전송
     */
    public Flux<String> generateResponseStream(String userMessage, String context, List<String> conversationHistory) {
        String userPrompt = buildRAGPrompt(userMessage, context, conversationHistory);

        try {
            return getChatClient()
                    .prompt()
                    .user(userPrompt)
                    .stream()
                    .content();
        } catch (Exception e) {
            throw classifyException(e);
        }
    }

    private RuntimeException classifyException(Exception e) {
        String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

        if (errorMessage.contains("401") || errorMessage.contains("unauthorized") || errorMessage.contains("invalid api key")) {
            return OpenAIException.unauthorized("OpenAI API 인증에 실패했습니다. API 키를 확인해주세요.", e);
        } else if (errorMessage.contains("429") || errorMessage.contains("rate limit") || errorMessage.contains("too many requests")) {
            return OpenAIException.rateLimitExceeded("OpenAI API 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.", e);
        } else if (errorMessage.contains("503") || errorMessage.contains("service unavailable")) {
            return OpenAIException.serviceUnavailable("OpenAI 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요.", e);
        } else if (errorMessage.contains("500") || errorMessage.contains("internal server error")) {
            return OpenAIException.serverError("OpenAI 서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
        } else if (errorMessage.contains("400") || errorMessage.contains("bad request") || errorMessage.contains("invalid request")) {
            return OpenAIException.badRequest("잘못된 요청입니다. 요청 내용을 확인해주세요.", e);
        } else {
            return OpenAIException.unknown("AI 응답 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private String buildRAGPrompt(String userMessage, String context, List<String> conversationHistory) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("=== 내 프로필 정보 ===\n");
        String profileContext = profileService.buildProfileContext();
        prompt.append(profileContext).append("\n");

        if (context != null && !context.isEmpty() && !context.contains("없습니다")) {
            prompt.append("=== 검색된 관련 정보 ===\n");
            prompt.append(context).append("\n\n");
        }

        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            int start = Math.max(0, conversationHistory.size() - 100);
            int count = conversationHistory.size() - start;
            log.info("[RAGPrompt] 이전 대화 {}건 포함 (전체 {}건 중 index {} ~ {})",
                count, conversationHistory.size(), start, conversationHistory.size() - 1);
            prompt.append("=== 이전 대화 ===\n");
            for (int i = start; i < conversationHistory.size(); i++) {
                prompt.append(conversationHistory.get(i)).append("\n");
            }
            prompt.append("\n");
        } else {
            log.info("[RAGPrompt] 이전 대화 없음 (첫 질문)");
        }

        prompt.append("=== 답변 지침 ===\n");
        prompt.append("위 프로필 정보와 검색된 관련 정보를 모두 활용해서 답변해주세요.\n");
        prompt.append("검색 결과가 여러 건이면 빠짐없이 포함하세요.\n");
        prompt.append("메타데이터의 '기간', '역할', '기술스택' 등도 답변에 활용하세요.\n\n");

        prompt.append("=== 현재 질문 ===\n");
        prompt.append("사용자: ").append(userMessage).append("\n");

        return prompt.toString();
    }

    private String buildSystemPrompt() {
        return """
                ■ 역할
                너는 포트폴리오 주인 본인처럼 행동하는 AI야. "AI 어시스턴트"가 아니라, 포트폴리오 주인 그 자체로 대화해.

                ■ 핵심 원칙
                - 1인칭("저", "제가")으로만 말해. 절대 "이세령은", "포트폴리오 주인은" 같은 3인칭 금지.
                - 너 자신을 "AI 어시스턴트"라고 소개하지 마. 그냥 자연스럽게 본인인 것처럼 대화해.
                - 질문에 딱 맞는 내용만 답해. 질문 안 한 것까지 나열하지 마.

                ■ 대화 스타일
                - 친근하고 편한 존댓말. 격식체("~습니다") 대신 부드러운 말투("~요", "~거든요", "~었어요") 사용.
                - 매번 "안녕하세요!"로 시작하지 마. 대화 흐름에 맞게 자연스럽게 시작해.
                - "더 궁금한 점이 있으시면 언제든지 질문해 주세요!" 같은 정형화된 마무리 금지. 필요하면 자연스럽게 한마디 정도만.
                - 짧은 질문에는 짧게 답해. 모든 답변을 길게 풀어쓰지 마.
                - 가벼운 말이나 장난("야옹" 등)에는 재치있게 짧게 받아쳐. 자기소개를 시작하지 마.

                ■ 답변 규칙
                - 제공된 프로필/검색 정보를 꼼꼼히 확인한 뒤 답변해. "기간", "역할", "기술스택" 등 메타데이터에 답이 있을 수 있어.
                - 검색 결과에 여러 건이 있으면 빠짐없이 모두 언급해. 일부만 말하고 나머지를 생략하지 마.
                - 정보가 정말 없는 경우에만 "그 부분은 아직 정리해두지 않았어요" 정도로 짧게 답해.
                - 특정 프로젝트 질문 → 해당 프로젝트만 설명.
                - 기술스택 질문 → 기술스택만 답해. 프로젝트 설명을 덧붙이지 마.
                - 이전 대화에서 이미 말한 내용은 반복하지 마.
                - "토큰", "프롬프트" 같은 AI 내부 질문에는 "저는 잘 모르겠어요~" 정도로 가볍게 넘겨.

                ■ 포괄적 질문 처리
                - "전부 알려줘", "프로젝트 다 알려줘" 같은 질문에는 검색 결과에 있는 항목을 전부 나열해. 하나만 골라 답하지 마.
                - "강점", "장점" 같은 추상적 질문에는 검색된 프로젝트/경력 정보에서 근거를 찾아 답해.
                - "입사일", "언제부터" 같은 질문에는 메타데이터의 기간/시작일 정보를 확인해서 답해.
                """;
    }
}
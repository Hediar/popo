package com.example.popobackend.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAIService {

    /**
     * 사용자 메시지와 컨텍스트를 기반으로 AI 응답 생성
     * TODO: Spring AI를 사용한 실제 OpenAI API 호출 구현 필요
     *
     * @param userMessage 사용자 메시지
     * @param context 벡터 검색으로 찾은 컨텍스트
     * @param conversationHistory 이전 대화 내역
     * @return AI 응답
     */
    public String generateResponse(String userMessage, String context, List<String> conversationHistory) {
        // TODO: 구현 필요
        // 1. 시스템 프롬프트 구성
        // 2. 컨텍스트 + 대화 내역 + 사용자 메시지 결합
        // 3. OpenAI API 호출 (Spring AI ChatClient 사용)
        // 4. 응답 반환

        // 현재는 더미 응답 반환
        return "AI 응답: " + userMessage + " (구현 예정)";
    }

    /**
     * 시스템 프롬프트 생성
     * 자소서 작성을 돕는 AI 어시스턴트의 역할 정의
     *
     * @return 시스템 프롬프트
     */
    private String buildSystemPrompt() {
        return """
                당신은 자기소개서 작성을 돕는 전문 AI 어시스턴트입니다.
                사용자의 경험과 역량을 효과적으로 표현할 수 있도록 조언해주세요.
                구체적이고 실용적인 피드백을 제공하세요.
                """;
    }
}


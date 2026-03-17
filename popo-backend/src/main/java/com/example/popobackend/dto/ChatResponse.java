package com.example.popobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String sessionId;   // 세션 ID
    private String message;      // AI 응답 메시지
}

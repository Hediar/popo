package com.example.popobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    private String sessionId;  // 세션 ID (선택적, 없으면 새로 생성)
    private String message;     // 사용자 메시지
}

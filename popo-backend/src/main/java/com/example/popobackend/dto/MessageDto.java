package com.example.popobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {

    private String role;  // "user" 또는 "assistant"
    private String content;
    private LocalDateTime timestamp;
}
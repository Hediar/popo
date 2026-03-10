package com.example.popobackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

    private String content;      // 검색된 내용
    private Double similarity;   // 유사도 점수 (0.0 ~ 1.0)
    private String source;       // 출처 (파일명, URL 등)

    @Override
    public String toString() {
        return String.format("%s (유사도: %.2f, 출처: %s)", content, similarity, source);
    }
}

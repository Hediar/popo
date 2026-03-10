package com.example.popobackend.service;

import com.example.popobackend.dto.SearchResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VectorSearchService {

    /**
     * 사용자 질문을 기반으로 벡터 검색 수행
     * TODO: 실제 pgvector를 사용한 벡터 검색 구현 필요
     *
     * @param query 사용자 질문
     * @return 관련 문서/데이터 목록
     */
    public List<SearchResult> search(String query) {
        // TODO: 구현 필요
        // 1. query를 벡터로 변환 (embedding)
        // 2. PostgreSQL pgvector로 유사도 검색
        // 3. 검색 결과 반환

        // 현재는 빈 결과 반환
        return new ArrayList<>();
    }

    /**
     * 벡터 검색 결과를 컨텍스트 문자열로 변환
     *
     * @param searchResults 검색 결과 목록
     * @return AI에게 전달할 컨텍스트 문자열
     */
    public String buildContext(List<SearchResult> searchResults) {
        if (searchResults == null || searchResults.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder("관련 정보:\n");
        for (SearchResult result : searchResults) {
            context.append("- ").append(result.toString()).append("\n");
        }

        return context.toString();
    }
}
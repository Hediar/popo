package com.example.popobackend.service;

import com.example.popobackend.dto.SearchResult;
import com.example.popobackend.entity.PortfolioData;
import com.example.popobackend.repository.PortfolioDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VectorSearchService {

    @Autowired
    private PortfolioDataRepository portfolioDataRepository;

    /**
     * 방문자 질문을 기반으로 포트폴리오 데이터 벡터 검색 (RAG 패턴)
     *
     * TODO: 실제 pgvector를 사용한 벡터 검색 구현 필요
     *
     * 구현 단계:
     * 1. OpenAI Embeddings API로 query를 벡터로 변환
     * 2. PostgreSQL pgvector로 코사인 유사도 검색
     *    SELECT content, similarity, source, metadata
     *    FROM portfolio_data
     *    ORDER BY embedding <=> query_embedding
     *    LIMIT 5
     * 3. 검색 결과 반환
     *
     * 검색 대상 (포트폴리오 데이터):
     * - 프로젝트 정보 (이름, 설명, 기술스택, 역할, 성과, 기간)
     * - 경력 사항 (회사, 직책, 기간, 담당 업무, 성과)
     * - 기술 스택 (언어, 프레임워크, 도구, 숙련도)
     * - 학력 (학교, 전공, 학위, 기간)
     * - 자격증/수상 경력
     * - 블로그 글 또는 기술 문서 (선택)
     *
     * 검색 쿼리 예시:
     * - "어떤 프로젝트 하셨나요?" → 프로젝트 정보 검색
     * - "백엔드 개발 경험은?" → 기술스택/프로젝트에서 백엔드 관련 검색
     * - "Spring 사용해보셨어요?" → Spring 관련 프로젝트/경력 검색
     *
     * @param query 방문자 질문
     * @return 관련 포트폴리오 데이터 (유사도 순)
     */
    public List<SearchResult> search(String query) {
        // 1단계: 키워드 추출
        List<String> keywords = extractKeywords(query);

        if (keywords.isEmpty()) {
            // 키워드가 없으면 전체 데이터 반환 (우선순위순)
            return searchAll();
        }

        // 2단계: 키워드 매칭 필터링
        List<PortfolioData> filteredData = filterByKeywords(keywords);

        // 3단계: 벡터 검색 (TODO: pgvector 구현 후 활성화)
        // filteredData를 대상으로 벡터 유사도 검색
        // List<SearchResult> vectorResults = vectorSearch(query, filteredData);

        // 현재는 키워드 매칭 결과만 반환
        return convertToSearchResults(filteredData);
    }

    /**
     * 키워드 추출
     * 쿼리에서 의미있는 키워드 추출
     */
    private List<String> extractKeywords(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 간단한 키워드 추출 (공백 기준)
        // TODO: 더 정교한 NLP 기반 키워드 추출 가능
        return Arrays.stream(query.split("\\s+"))
                .map(String::trim)
                .filter(keyword -> keyword.length() >= 2)  // 2글자 이상만
                .filter(keyword -> !isStopWord(keyword))   // 불용어 제외
                .collect(Collectors.toList());
    }

    /**
     * 불용어 체크
     */
    private boolean isStopWord(String word) {
        // 한국어 불용어
        List<String> stopWords = Arrays.asList(
            "은", "는", "이", "가", "을", "를", "의", "에", "에서", "으로", "로",
            "과", "와", "하다", "되다", "있다", "없다", "이다",
            "그", "저", "이거", "저거", "뭐", "어떤", "어떻게", "무슨"
        );
        return stopWords.contains(word);
    }

    /**
     * 키워드 기반 필터링
     */
    private List<PortfolioData> filterByKeywords(List<String> keywords) {
        if (keywords.isEmpty()) {
            return new ArrayList<>();
        }

        // 다중 키워드 OR 검색
        String[] keywordArray = keywords.toArray(new String[0]);
        return portfolioDataRepository.findByKeywords(keywordArray);
    }

    /**
     * 전체 데이터 조회 (우선순위순)
     */
    private List<SearchResult> searchAll() {
        List<PortfolioData> allData = portfolioDataRepository.findByIsPublicTrueOrderByPriorityDesc();
        return convertToSearchResults(allData.stream().limit(5).collect(Collectors.toList()));
    }

    /**
     * PortfolioData를 SearchResult로 변환
     */
    private List<SearchResult> convertToSearchResults(List<PortfolioData> portfolioDataList) {
        return portfolioDataList.stream()
                .map(data -> new SearchResult(
                    formatContent(data),
                    1.0,  // 키워드 매칭은 유사도 1.0 (벡터 검색 시 실제 유사도 사용)
                    data.getSource() != null ? data.getSource() : data.getType() + "-" + data.getId()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 포트폴리오 데이터를 포맷팅
     */
    private String formatContent(PortfolioData data) {
        StringBuilder content = new StringBuilder();

        // 제목
        if (data.getTitle() != null && !data.getTitle().isEmpty()) {
            content.append(data.getTitle());
        }

        // 내용
        if (data.getContent() != null && !data.getContent().isEmpty()) {
            if (content.length() > 0) {
                content.append(": ");
            }
            content.append(data.getContent());
        }

        return content.toString();
    }

    /**
     * 벡터 검색 결과를 RAG용 컨텍스트로 변환
     * AI 프롬프트에 포함될 구조화된 컨텍스트 생성
     *
     * @param searchResults 검색 결과 목록
     * @return RAG용 컨텍스트 문자열
     */
    public String buildContext(List<SearchResult> searchResults) {
        if (searchResults == null || searchResults.isEmpty()) {
            return "검색된 관련 정보가 없습니다.";
        }

        StringBuilder context = new StringBuilder();
        context.append("=== 검색된 관련 정보 ===\n\n");

        for (int i = 0; i < searchResults.size(); i++) {
            SearchResult result = searchResults.get(i);
            context.append(String.format("[참고 %d] (유사도: %.1f%%)%n",
                i + 1, result.getSimilarity() * 100));
            context.append(result.getContent()).append("\n");
            context.append(String.format("출처: %s%n", result.getSource()));
            context.append("\n");
        }

        return context.toString();
    }
}
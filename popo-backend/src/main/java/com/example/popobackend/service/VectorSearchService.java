package com.example.popobackend.service;

import com.example.popobackend.dto.SearchResult;
import com.example.popobackend.entity.PortfolioData;
import com.example.popobackend.repository.PortfolioDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VectorSearchService {

    private static final Logger log = LoggerFactory.getLogger(VectorSearchService.class);

    @Autowired
    private PortfolioDataRepository portfolioDataRepository;

    @Autowired
    private EmbeddingService embeddingService;

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
    /**
     * 타입별 키워드 목록
     */
    private static final List<String> CAREER_KEYWORDS = Arrays.asList(
        "회사", "경력", "직장", "재직", "근무", "업무", "그렉터", "연구원", "팀",
        "어디서 일", "어디 다니", "무슨 일", "뭐하고 있", "어디 근무", "입사",
        "경력사항", "이력", "직무", "직책", "소속", "부서"
    );

    private static final List<String> PROJECT_KEYWORDS = Arrays.asList(
        "프로젝트", "포트폴리오", "개발", "구현", "만들", "제작", "설계",
        "POPO", "불도저", "BDZ", "Aliot", "DMS", "FSMS", "Viewtrack",
        "어떤 프로젝트", "무슨 프로젝트", "프로젝트 경험", "만든 것",
        "개발한", "진행한", "참여한", "작업", "시스템", "플랫폼", "서비스"
    );

    private static final List<String> BROAD_QUERY_KEYWORDS = Arrays.asList(
        "전부", "모두", "모든", "다 알려", "전체", "목록", "리스트", "뭐가 있", "뭐 했", "어떤 것들", "몇 개"
    );

    private static final List<String> PROFILE_KEYWORDS = Arrays.asList(
        "자기소개", "소개", "누구", "이름", "프로필", "본인",
        "당신", "너", "어떤 사람", "어떤 개발자", "개발자",
        "연락", "이메일", "깃허브", "GitHub", "블로그", "velog",
        "기술스택", "스택", "기술", "사용", "다룰 수 있", "할 수 있",
        "관심분야", "관심사", "좋아하", "전공", "학교", "학력", "졸업",
        "자격증", "정보처리기사", "SQLD"
    );

    public List<SearchResult> search(String query) {
        log.info("[VectorSearch] 검색 시작 - query: \"{}\"", query);

        boolean broadQuery = isBroadQuery(query);
        if (broadQuery) {
            log.info("[VectorSearch] 포괄적 질문 감지");
        }

        // 회사/경력 관련 키워드 감지
        String typeFilter = detectTypeFilter(query);
        if (typeFilter != null) {
            log.info("[VectorSearch] type 필터 감지: \"{}\"", typeFilter);
        }

        int vectorLimit = broadQuery ? 15 : 5;
        int finalLimit = broadQuery ? 10 : 7;

        try {
            // 0단계: title 키워드 매칭 우선 검색
            List<SearchResult> keywordMatchResults = searchByTitleKeyword(query, typeFilter, broadQuery);
            log.info("[VectorSearch] title 키워드 매칭 결과: {}건", keywordMatchResults.size());

            // 1단계: 쿼리를 벡터로 변환
            float[] queryEmbedding = embeddingService.createEmbedding(query);

            if (queryEmbedding == null) {
                log.warn("[VectorSearch] 임베딩 실패 → 키워드 검색 결과 반환");
                return keywordMatchResults.isEmpty() ? keywordSearch(query) : keywordMatchResults;
            }

            log.info("[VectorSearch] 임베딩 성공 - 벡터 차원: {}", queryEmbedding.length);

            // 2단계: 벡터 유사도 검색 (type 필터 적용)
            List<Object[]> results;
            if (typeFilter != null) {
                results = portfolioDataRepository.findSimilarByType(queryEmbedding, typeFilter, vectorLimit);
                log.info("[VectorSearch] type='{}' 필터 벡터 검색 결과: {}건", typeFilter, results.size());
            } else {
                results = portfolioDataRepository.findSimilar(queryEmbedding, vectorLimit);
                log.info("[VectorSearch] 전체 벡터 검색 결과: {}건", results.size());
            }

            // 3단계: SearchResult로 변환
            List<SearchResult> vectorSearchResults = results.stream()
                .map(this::convertObjectArrayToSearchResult)
                .collect(Collectors.toList());

            // 3.5단계: type 필터가 있고 벡터 검색 결과가 적으면, 해당 타입 전체 보충
            if (typeFilter != null && vectorSearchResults.size() < 3) {
                log.info("[VectorSearch] 타입 보충 - type='{}' 전체 데이터 추가", typeFilter);
                List<PortfolioData> allOfType = portfolioDataRepository.findByTypeAndIsPublicTrue(typeFilter);
                for (PortfolioData data : allOfType) {
                    String source = data.getSource() != null ? data.getSource() : data.getType() + "-" + data.getId();
                    boolean alreadyExists = vectorSearchResults.stream()
                        .anyMatch(sr -> sr.getSource().equals(source));
                    if (!alreadyExists) {
                        vectorSearchResults.add(new SearchResult(formatContent(data), 0.5, source));
                    }
                }
            }

            // 3.6단계: career 필터일 때 experience도 함께 가져오기 (또는 그 반대)
            if ("career".equals(typeFilter)) {
                List<PortfolioData> experienceData = portfolioDataRepository.findByTypeAndIsPublicTrue("experience");
                for (PortfolioData data : experienceData) {
                    String source = data.getSource() != null ? data.getSource() : data.getType() + "-" + data.getId();
                    boolean alreadyExists = vectorSearchResults.stream()
                        .anyMatch(sr -> sr.getSource().equals(source));
                    if (!alreadyExists) {
                        vectorSearchResults.add(new SearchResult(formatContent(data), 0.5, source));
                    }
                }
            }

            // 4단계: title 키워드 매칭 결과와 벡터 검색 결과 합치기 (중복 제거)
            List<SearchResult> combinedResults = new ArrayList<>(keywordMatchResults);
            for (SearchResult vectorResult : vectorSearchResults) {
                boolean isDuplicate = combinedResults.stream()
                    .anyMatch(existing -> existing.getSource().equals(vectorResult.getSource()));
                if (!isDuplicate) {
                    combinedResults.add(vectorResult);
                }
            }

            // 동적 제한
            List<SearchResult> finalResults = combinedResults.stream()
                .limit(finalLimit)
                .collect(Collectors.toList());

            for (int i = 0; i < finalResults.size(); i++) {
                SearchResult sr = finalResults.get(i);
                log.info("[VectorSearch] 최종결과[{}] 유사도: {}% | source: {} | content: {}",
                    i, String.format("%.2f", sr.getSimilarity() * 100), sr.getSource(),
                    sr.getContent().length() > 80 ? sr.getContent().substring(0, 80) + "..." : sr.getContent());
            }

            return finalResults;

        } catch (Exception e) {
            log.error("[VectorSearch] 벡터 검색 실패 → 키워드 검색으로 폴백: {}", e.getMessage());
            return keywordSearch(query);
        }
    }

    /**
     * title에서 키워드 직접 매칭
     * query에서 의미있는 키워드를 추출하여 title에 포함된 항목을 찾음
     */
    private boolean isBroadQuery(String query) {
        if (query == null) return false;
        String lower = query.toLowerCase();
        return BROAD_QUERY_KEYWORDS.stream().anyMatch(lower::contains);
    }

    private List<SearchResult> searchByTitleKeyword(String query, String typeFilter, boolean broadQuery) {
        List<String> keywords = extractKeywords(query);
        if (keywords.isEmpty()) {
            return new ArrayList<>();
        }

        log.info("[TitleKeywordSearch] 추출된 키워드: {}", keywords);

        List<PortfolioData> matchedData = new ArrayList<>();
        for (String keyword : keywords) {
            List<PortfolioData> matches;
            if (typeFilter != null) {
                matches = portfolioDataRepository.findByTypeAndIsPublicTrue(typeFilter).stream()
                    .filter(data -> data.getTitle() != null &&
                            data.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
            } else {
                matches = portfolioDataRepository.findByKeyword(keyword).stream()
                    .filter(data -> data.getTitle() != null &&
                            data.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
            }

            for (PortfolioData data : matches) {
                if (!matchedData.contains(data)) {
                    matchedData.add(data);
                    log.info("[TitleKeywordSearch] 매칭: keyword='{}' → title='{}'", keyword, data.getTitle());
                }
            }
        }

        int titleLimit = broadQuery ? 10 : 3;
        return matchedData.stream()
            .sorted((a, b) -> Integer.compare(
                b.getPriority() != null ? b.getPriority() : 0,
                a.getPriority() != null ? a.getPriority() : 0
            ))
            .limit(titleLimit)
            .map(data -> new SearchResult(
                formatContent(data),
                1.0,
                data.getSource() != null ? data.getSource() : data.getType() + "-" + data.getId()
            ))
            .collect(Collectors.toList());
    }

    /**
     * 질문에서 type 필터를 감지
     * - 회사/경력 관련 키워드 → "career"
     * - 프로젝트 관련 키워드 → "project"
     * - 프로필 관련 키워드 → "profile" (실제로는 Profile 테이블 조회)
     */
    private String detectTypeFilter(String query) {
        if (query == null) return null;
        String lower = query.toLowerCase();

        // 우선순위: career > project > profile
        // (career가 가장 구체적인 질문이므로 먼저 체크)

        for (String keyword : CAREER_KEYWORDS) {
            if (lower.contains(keyword)) {
                return "career";
            }
        }

        for (String keyword : PROJECT_KEYWORDS) {
            if (lower.contains(keyword)) {
                return "project";
            }
        }

        // profile 키워드는 portfolio_data가 아닌 profile 테이블 조회 필요
        // 현재는 null 반환하여 전체 검색 수행
        // TODO: ProfileService와 연동하여 profile 데이터도 포함
        for (String keyword : PROFILE_KEYWORDS) {
            if (lower.contains(keyword)) {
                // profile 타입은 별도 처리 필요 (현재는 전체 검색)
                return null;
            }
        }

        return null;
    }

    /**
     * 키워드 검색 (벡터 검색 실패 시 폴백)
     */
    private List<SearchResult> keywordSearch(String query) {
        List<String> keywords = extractKeywords(query);
        log.info("[KeywordSearch] 추출된 키워드: {}", keywords);

        if (keywords.isEmpty()) {
            log.info("[KeywordSearch] 키워드 없음 → 전체 검색 (searchAll)");
            return searchAll();
        }

        List<PortfolioData> filteredData = filterByKeywords(keywords);
        log.info("[KeywordSearch] 키워드 필터링 결과: {}건", filteredData.size());
        for (PortfolioData data : filteredData) {
            log.info("[KeywordSearch] - [{}] {} (source: {})", data.getType(), data.getTitle(), data.getSource());
        }
        return convertToSearchResults(filteredData);
    }

    /**
     * Object[] (DB 결과)를 SearchResult로 변환
     * Object[]: [id, type, title, content, metadata, source, priority, similarity]
     *
     * title 기반으로 검색하고, content + metadata를 답변 생성에 사용
     */
    private SearchResult convertObjectArrayToSearchResult(Object[] row) {
        String title = row[2] != null ? row[2].toString() : "";
        String content = row[3] != null ? row[3].toString() : "";
        String metadata = row[4] != null ? row[4].toString() : "";
        String source = row[5] != null ? row[5].toString() : row[1] + "-" + row[0];
        Double similarity = row[7] != null ? ((Number) row[7]).doubleValue() : 1.0;

        String type = row[1] != null ? row[1].toString() : "";
        StringBuilder formattedContent = new StringBuilder();

        if (title != null && !title.isEmpty()) {
            formattedContent.append("[").append(type).append("] ").append(title);
        }

        if (content != null && !content.isEmpty()) {
            formattedContent.append("\n").append(content);
        }

        String parsedMeta = formatMetadata(metadata);
        if (!parsedMeta.isEmpty()) {
            formattedContent.append("\n").append(parsedMeta);
        }

        return new SearchResult(formattedContent.toString(), similarity, source);
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
            "은", "는", "이", "가", "을", "를", "의", "에", "에서", "으로", "로", "랑",
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

    private String formatContent(PortfolioData data) {
        StringBuilder content = new StringBuilder();

        if (data.getTitle() != null && !data.getTitle().isEmpty()) {
            content.append("[").append(data.getType()).append("] ").append(data.getTitle());
        }

        if (data.getContent() != null && !data.getContent().isEmpty()) {
            content.append("\n").append(data.getContent());
        }

        String parsedMeta = formatMetadata(data.getMetadata());
        if (!parsedMeta.isEmpty()) {
            content.append("\n").append(parsedMeta);
        }

        return content.toString();
    }

    @SuppressWarnings("unchecked")
    private String formatMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isEmpty() || metadataJson.equals("null")) {
            return "";
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = mapper.readValue(metadataJson, new TypeReference<>() {});
            StringBuilder sb = new StringBuilder();

            Map<String, String> labelMap = Map.ofEntries(
                Map.entry("duration", "기간"),
                Map.entry("startDate", "시작일"),
                Map.entry("endDate", "종료일"),
                Map.entry("role", "역할"),
                Map.entry("techStack", "기술스택"),
                Map.entry("company", "회사"),
                Map.entry("team", "팀"),
                Map.entry("position", "직책"),
                Map.entry("department", "부서"),
                Map.entry("totalPeriod", "총 기간"),
                Map.entry("goals", "목표"),
                Map.entry("achievements", "성과"),
                Map.entry("background", "배경"),
                Map.entry("proficiency", "숙련도"),
                Map.entry("years", "경험"),
                Map.entry("school", "학교"),
                Map.entry("major", "전공"),
                Map.entry("degree", "학위"),
                Map.entry("gpa", "학점")
            );

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String label = labelMap.getOrDefault(entry.getKey(), entry.getKey());
                Object value = entry.getValue();
                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    if (!list.isEmpty() && list.get(0) instanceof String) {
                        sb.append(label).append(": ").append(String.join(", ", (List<String>) value)).append("\n");
                    }
                } else if (value instanceof String && !((String) value).isEmpty()) {
                    sb.append(label).append(": ").append(value).append("\n");
                }
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return "";
        }
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
            log.info("[BuildContext] 검색 결과 없음 → 빈 컨텍스트 반환");
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

        log.info("[BuildContext] 최종 컨텍스트 길이: {}자, 참고자료 {}개", context.length(), searchResults.size());
        return context.toString();
    }
}
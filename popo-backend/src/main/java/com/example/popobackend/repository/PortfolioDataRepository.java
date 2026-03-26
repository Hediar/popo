package com.example.popobackend.repository;

import com.example.popobackend.entity.PortfolioData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioDataRepository extends JpaRepository<PortfolioData, Long> {

    List<PortfolioData> findByTypeAndIsPublicTrue(String type);

    List<PortfolioData> findByIsPublicTrueOrderByPriorityDesc();

    /**
     * 키워드 매칭 검색 (1단계 필터링)
     * title, content, metadata에서 키워드 검색
     *
     * @param keyword 검색 키워드
     * @return 키워드가 포함된 포트폴리오 데이터
     */
    @Query("""
        SELECT p FROM PortfolioData p
        WHERE p.isPublic = true
        AND (
            LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.metadata) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY p.priority DESC, p.createdAt DESC
        """)
    List<PortfolioData> findByKeyword(@Param("keyword") String keyword);

    /**
     * 다중 키워드 OR 검색
     * 여러 키워드 중 하나라도 매칭되면 반환
     *
     * @param keywords 검색 키워드 리스트
     * @return 키워드가 포함된 포트폴리오 데이터
     */
    @Query(value = """
        SELECT DISTINCT p.*
        FROM portfolio_data p
        CROSS JOIN LATERAL unnest(CAST(:keywords AS TEXT[])) AS k(keyword)
        WHERE p.is_public = true
        AND (
            LOWER(p.title) LIKE LOWER(CONCAT('%', k.keyword, '%'))
            OR LOWER(p.content) LIKE LOWER(CONCAT('%', k.keyword, '%'))
            OR LOWER(CAST(p.metadata AS TEXT)) LIKE LOWER(CONCAT('%', k.keyword, '%'))
        )
        ORDER BY p.priority DESC, p.created_at DESC
        """, nativeQuery = true)
    List<PortfolioData> findByKeywords(@Param("keywords") String[] keywords);

    /**
     * pgvector를 사용한 유사도 검색 (2단계 - 의미적 검색)
     * TODO: pgvector extension 설치 후 활성화
     *
     * @param queryEmbedding 검색 쿼리의 임베딩 벡터
     * @param limit 반환할 결과 개수
     * @return 유사도 순으로 정렬된 포트폴리오 데이터
     */
     @Query(value = """
         SELECT id, type, title, content, metadata, source, priority,
                1 - (embedding <=> CAST(:queryEmbedding AS vector)) AS similarity
         FROM portfolio_data
         WHERE is_public = true
         AND 1 - (embedding <=> CAST(:queryEmbedding AS vector)) >= 0.6
         ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
         LIMIT :limit
         """, nativeQuery = true)
     List<Object[]> findSimilar(@Param("queryEmbedding") float[] queryEmbedding,
                               @Param("limit") int limit);

    /**
     * 키워드 필터링 + 벡터 검색 조합
     */
     @Query(value = """
         SELECT id, type, title, content, metadata, source, priority,
                1 - (embedding <=> CAST(:queryEmbedding AS vector)) AS similarity
         FROM portfolio_data
         WHERE is_public = true
         AND id IN :filteredIds
         AND 1 - (embedding <=> CAST(:queryEmbedding AS vector)) >= 0.6
         ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
         LIMIT :limit
         """, nativeQuery = true)
     List<Object[]> findSimilarWithFilter(
         @Param("queryEmbedding") float[] queryEmbedding,
         @Param("filteredIds") List<Long> filteredIds,
         @Param("limit") int limit
     );

    /**
     * type 필터 + 벡터 유사도 검색
     */
    @Query(value = """
        SELECT id, type, title, content, metadata, source, priority,
               1 - (embedding <=> CAST(:queryEmbedding AS vector)) AS similarity
        FROM portfolio_data
        WHERE is_public = true
        AND type = :type
        AND 1 - (embedding <=> CAST(:queryEmbedding AS vector)) >= 0.6
        ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findSimilarByType(@Param("queryEmbedding") float[] queryEmbedding,
                                     @Param("type") String type,
                                     @Param("limit") int limit);

    /**
     * 임베딩이 없는 데이터 조회
     */
    List<PortfolioData> findByEmbeddingIsNull();
}

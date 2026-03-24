package com.example.popobackend.entity;

import com.example.popobackend.converter.VectorConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 포트폴리오 데이터 (벡터 검색용 통합 엔티티)
 *
 * 프로젝트, 경력, 기술, 학력 등 모든 포트폴리오 정보를 담는 엔티티
 * pgvector를 사용한 유사도 검색을 위해 embedding 필드 포함
 */
@Entity
@Table(name = "portfolio_data")
@Getter
@Setter
@NoArgsConstructor
public class PortfolioData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 데이터 타입 (project, experience, skill, education, certificate 등)
     */
    @Column(nullable = false)
    private String type;

    /**
     * 제목 (프로젝트명, 회사명, 기술명 등)
     */
    @Column(nullable = false)
    private String title;

    /**
     * 상세 내용 (텍스트)
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 메타데이터 (JSON 형태)
     * 예: {"techStack": ["Java", "Spring"], "role": "Backend Developer", "duration": "2023.01-2023.12"}
     */
    @Column(columnDefinition = "jsonb")
    private String metadata;

    /**
     * 임베딩 벡터 (pgvector)
     * OpenAI Embeddings API로 생성된 벡터
     * text-embedding-3-small: 1536 dimensions
     */
    @Column(columnDefinition = "vector(1536)")
    @Convert(converter = VectorConverter.class)
    private float[] embedding;

    /**
     * 출처/태그 (검색 결과에 표시)
     */
    private String source;

    /**
     * 중요도/우선순위 (높을수록 우선 표시)
     */
    private Integer priority;

    /**
     * 공개 여부
     */
    @Column(nullable = false)
    private Boolean isPublic = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

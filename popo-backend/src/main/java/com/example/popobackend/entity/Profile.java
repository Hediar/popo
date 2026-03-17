package com.example.popobackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 포트폴리오 주인의 기본 프로필 정보
 * AI 프롬프트에 기본 컨텍스트로 사용
 */
@Entity
@Table(name = "profile")
@Getter
@Setter
@NoArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이름
     */
    @Column(nullable = false)
    private String name;

    /**
     * 직업/직책
     */
    private String occupation;

    /**
     * 경력 (예: "3년", "신입")
     */
    private String experience;

    /**
     * 현재 회사/소속
     */
    private String currentCompany;

    /**
     * 학력 (학교, 전공)
     * 예: "서울대학교 컴퓨터공학과"
     */
    private String education;

    /**
     * 한 줄 소개
     */
    @Column(columnDefinition = "TEXT")
    private String introduction;

    /**
     * 주요 기술 스택 (JSONB - 숙련도별 구분)
     * 예: {
     *   "expert": ["Java", "Spring Boot"],
     *   "proficient": ["PostgreSQL", "Docker"],
     *   "familiar": ["React", "AWS"]
     * }
     */
    @Column(columnDefinition = "jsonb")
    private String techStack;

    /**
     * 관심 분야
     */
    private String interests;

    /**
     * 이메일
     */
    private String email;

    /**
     * GitHub URL
     */
    private String githubUrl;

    /**
     * 블로그 URL
     */
    private String blogUrl;

    /**
     * 자격증 목록 (JSONB)
     * 예: [
     *   {"name": "정보처리기사", "issuer": "한국산업인력공단", "date": "2025.09"},
     *   {"name": "AWS Solutions Architect", "issuer": "Amazon", "date": "2024.03"}
     * ]
     */
    @Column(columnDefinition = "jsonb")
    private String certifications;

    /**
     * 추가 메타데이터 (JSON)
     */
    @Column(columnDefinition = "jsonb")
    private String metadata;

    /**
     * 활성화 여부 (단일 프로필만 활성화)
     */
    @Column(nullable = false)
    private Boolean isActive = true;

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

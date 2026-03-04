# POPO-AI: 이력서 기반 AI 챗봇

> Spring Boot + Next.js 기반 RAG(Retrieval-Augmented Generation) 챗봇 시스템

## 목차
- [프로젝트 개요](#프로젝트-개요)
- [기술 스택](#기술-스택)
- [시스템 아키텍처](#시스템-아키텍처)
- [동작 흐름](#동작-흐름)
- [데이터베이스 설계](#데이터베이스-설계)
- [API 명세](#api-명세)
- [구현 단계](#구현-단계)

---

## 프로젝트 개요

이력서 데이터를 기반으로 사용자의 질문에 자동으로 답변하는 AI 챗봇입니다.
RAG(Retrieval-Augmented Generation) 패턴을 사용하여 정확하고 관련성 높은 답변을 제공합니다.

### 핵심 기능
- 이력서 데이터를 벡터 임베딩으로 저장
- 질문에 대해 관련 컨텍스트 자동 검색
- 실시간 스트리밍 답변 생성
- 하이브리드 검색 (키워드 + 벡터 유사도)

---

## 기술 스택

### Frontend
- **Next.js 16+** (App Router)
- **TypeScript 5**
- **TailwindCSS 4**
- **React 19**
- **EventSource API** (SSE)

### Backend
- **Spring Boot 4.0+**
- **Java 17**
- **Spring AI** (OpenAI 통합)
- **Spring Data JPA** (PostgreSQL 연동)
- **Gradle**

### Database
- **PostgreSQL 13+** + **pgvector**
  - 벡터 유사도 검색 지원
  - HNSW/IVFFlat 인덱싱
  - 직접 쿼리를 통한 유연한 제어
  - Spring Data JPA 통합

### AI/LLM
- **OpenAI API**
  - `text-embedding-3-small` (임베딩 생성, 768차원)
  - `gpt-3.5-turbo` 또는 `gpt-4` (답변 생성)
  - Spring AI 통합

---

## 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                   Frontend (Next.js)                     │
│  ┌──────────────────────────────────────────────────┐   │
│  │            Chat Interface                         │   │
│  │  - 실시간 스트리밍 채팅                             │   │
│  │  - 메시지 히스토리                                  │   │
│  │  - 헤더 네비게이션                                  │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                          │ SSE / REST API
                          ▼
┌─────────────────────────────────────────────────────────┐
│              Backend (Spring Boot)                       │
│  ┌────────────────────────────────────────────────────┐ │
│  │         ChatController (SSE Endpoint)               │ │
│  └────────────────────────────────────────────────────┘ │
│                          │                               │
│  ┌────────────────────────────────────────────────────┐ │
│  │              ChatService (핵심 RAG 로직)            │ │
│  │  1. 질문에서 키워드 추출 (NLP/정규식)               │ │
│  │  2. 질문 임베딩 생성                                │ │
│  │  3. 하이브리드 검색 (키워드 + 벡터)                  │ │
│  │  4. 컨텍스트 구성                                   │ │
│  │  5. LLM 프롬프트 생성 & 스트리밍 응답               │ │
│  └────────────────────────────────────────────────────┘ │
│           │                 │                 │          │
│           │                 │                            │
│  ┌────────▼──────┐  ┌───────▼──────────┐              │
│  │ OpenAIService │  │ ResumeRepository │              │
│  │ (LLM/임베딩)  │  │ (JPA + pgvector) │              │
│  │               │  │ (벡터 검색)       │              │
│  └───────────────┘  └──────────────────┘              │
└─────────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────────┐
│                  External Services                       │
│  ┌──────────────────┐         ┌──────────────────┐     │
│  │   PostgreSQL     │         │   OpenAI API     │     │
│  │   + pgvector     │         │ - Embeddings     │     │
│  │   Extension      │         │ - Chat Completion│     │
│  └──────────────────┘         └──────────────────┘     │
└─────────────────────────────────────────────────────────┘
```

---

## 동작 흐름

### 1. 초기 설정 단계 (이력서 데이터 등록)

```
┌─────────────────────────────────────────────────────────┐
│ 1. 이력서 데이터 입력 (JSON/Form)                        │
│    {                                                     │
│      "profile": { "name": "홍길동", ... },               │
│      "experiences": [...],                               │
│      "projects": [...],                                  │
│      "skills": [...]                                     │
│    }                                                     │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│ 2. Spring Boot: AdminService                            │
│    - 섹션별로 분할 (Chunking)                            │
│    - 각 섹션을 텍스트로 변환                              │
│      예: "A사 백엔드 개발자: Spring Boot와 JPA를 활용..." │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│ 3. OpenAI Embedding API 호출                            │
│    POST /v1/embeddings                                  │
│    {                                                     │
│      "model": "text-embedding-3-small",                 │
│      "input": "A사 백엔드 개발자: ...",                  │
│      "dimensions": 768                                  │
│    }                                                     │
│    → [0.123, -0.456, 0.789, ...] (768차원 벡터)         │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│ 4. PostgreSQL에 저장 (Spring Data JPA)                  │
│    INSERT INTO resume_sections                          │
│    (type, title, content, embedding)                    │
│    VALUES ('experience', 'A사', '...', '[...]')         │
│                                                          │
│    pgvector를 사용하여 embedding 컬럼에 벡터 저장        │
└─────────────────────────────────────────────────────────┘
```

### 2. 사용자 질문 처리 (실시간)

```
사용자 질문: "Java Spring Boot 경험이 있나요?"
                          ↓
┌─────────────────────────────────────────────────────────┐
│ 1. Next.js: EventSource 연결                            │
│    GET /api/chat/stream?question=Java+Spring+Boot...    │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│ 2. Spring Boot ChatController                           │
│    - SseEmitter 생성                                    │
│    - ChatService 호출                                   │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│ 3. ChatService: RAG 프로세스                            │
│                                                          │
│ [3-1] 키워드 추출                                        │
│       "Java Spring Boot 경험이 있나요?"                  │
│       → 키워드 추출: ["Java", "Spring Boot", "경험"]     │
│       → 정규식/형태소 분석을 통한 중요 키워드 추출        │
│                                                          │
│ [3-2] 질문 임베딩 생성                                   │
│       "Java Spring Boot 경험이 있나요?"                  │
│       → OpenAI Embedding API                            │
│       → [0.234, -0.567, ...] (768차원)                  │
│                                                          │
│ [3-3] 하이브리드 검색 (키워드 + 벡터)                    │
│       A. 키워드 필터 (Full-Text Search)                 │
│          WHERE content ILIKE '%Java%'                   │
│          OR content ILIKE '%Spring Boot%'               │
│                                                          │
│       B. 벡터 유사도 검색 (pgvector)                     │
│          SELECT *, 1 - (embedding <=> '[...]') as sim   │
│          FROM resume_sections                           │
│          WHERE sim > 0.7                                │
│          ORDER BY sim DESC LIMIT 5                      │
│                                                          │
│       → 키워드 매칭 + 벡터 유사도로 상위 3-5개 추출       │
│                                                          │
│ [3-4] 컨텍스트 구성                                      │
│       "관련 이력서 정보:                                  │
│        [경력] A사 백엔드 개발자                           │
│        - Spring Boot와 JPA를 활용한 API 개발...          │
│        [프로젝트] 전자상거래 플랫폼                        │
│        - Spring Boot 기반 MSA 아키텍처..."               │
│                                                          │
│ [3-5] 프롬프트 생성                                      │
│       System: "당신은 지원자의 이력서 정보를 바탕으로..." │
│       Context: [위 컨텍스트]                             │
│       Question: "Java Spring Boot 경험이 있나요?"        │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│ 4. OpenAI Chat Completion API (Stream)                  │
│    POST /v1/chat/completions                            │
│    { "model": "gpt-3.5-turbo", "stream": true, ... }    │
│    → 청크 단위로 응답 수신                                │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│ 5. SSE로 실시간 전송                                     │
│    청크1: "네, "                                         │
│    청크2: "Java와 "                                      │
│    청크3: "Spring Boot "                                │
│    청크4: "경험이 "                                      │
│    ...                                                   │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│ 6. Next.js: 실시간 화면 표시                             │
│    EventSource.onmessage로 청크 수신                     │
│    → 타이핑 애니메이션 효과로 표시                        │
└─────────────────────────────────────────────────────────┘
```

### 상세 시퀀스 다이어그램

```
User        Next.js      Spring Boot    PostgreSQL    OpenAI
 │              │              │             │            │
 │ 질문 입력    │              │             │            │
 │─────────────>│              │             │            │
 │              │ SSE 연결     │             │            │
 │              │─────────────>│             │            │
 │              │              │ 키워드 추출 │            │
 │              │              │ 질문 임베딩 요청          │
 │              │              │─────────────────────────>│
 │              │              │ 임베딩 벡터              │
 │              │              │<─────────────────────────│
 │              │              │ 하이브리드  │            │
 │              │              │ 검색 (키워드+벡터)        │
 │              │              │────────────>│            │
 │              │              │ 관련 섹션   │            │
 │              │              │<────────────│            │
 │              │              │ 스트리밍 채팅 요청        │
 │              │              │─────────────────────────>│
 │              │ 청크1        │ 청크1       │            │
 │              │<─────────────│<─────────────────────────│
 │ 표시         │              │             │            │
 │<─────────────│              │             │            │
 │              │ 청크2        │ 청크2       │            │
 │              │<─────────────│<─────────────────────────│
 │ 표시         │              │             │            │
 │<─────────────│              │             │            │
 │              │     ...      │     ...     │            │
 │              │ 완료         │             │            │
 │              │<─────────────│             │            │
```

---

## 데이터베이스 설계

### PostgreSQL + pgvector 설정

#### 1. pgvector 확장 설치

```sql
-- PostgreSQL 13 이상에서 pgvector 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;
```

#### 2. `resume_sections` 테이블

```sql
CREATE TABLE resume_sections (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,           -- 'profile', 'experience', 'project', 'skill'
    title VARCHAR(200),                  -- 회사명, 프로젝트명 등
    content TEXT NOT NULL,               -- 전체 텍스트 내용
    metadata JSONB,                      -- 추가 정보 (기간, 기술스택 등)
    embedding VECTOR(768),               -- pgvector 임베딩 (768차원)
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 벡터 유사도 검색을 위한 HNSW 인덱스 (IVFFlat보다 빠른 검색)
CREATE INDEX resume_sections_embedding_idx
ON resume_sections
USING hnsw (embedding vector_cosine_ops);

-- IVFFlat 인덱스 (대안)
-- CREATE INDEX resume_sections_embedding_idx
-- ON resume_sections
-- USING ivfflat (embedding vector_cosine_ops)
-- WITH (lists = 100);

-- 키워드 검색을 위한 GIN 인덱스
CREATE INDEX resume_sections_content_idx
ON resume_sections
USING gin(to_tsvector('korean', content));

-- Full-Text Search를 위한 컬럼 추가 (선택사항)
ALTER TABLE resume_sections ADD COLUMN content_tsvector tsvector;
CREATE INDEX resume_sections_tsvector_idx ON resume_sections USING gin(content_tsvector);
```

#### 3. 하이브리드 검색 쿼리 예시

```sql
-- 키워드 + 벡터 유사도 검색
SELECT
    id,
    type,
    title,
    content,
    metadata,
    1 - (embedding <=> '[0.234, -0.567, ...]'::vector) AS similarity
FROM resume_sections
WHERE
    -- 키워드 필터
    (content ILIKE '%Java%' OR content ILIKE '%Spring Boot%')
    -- 벡터 유사도 임계값
    AND 1 - (embedding <=> '[0.234, -0.567, ...]'::vector) > 0.7
ORDER BY similarity DESC
LIMIT 5;
```

#### 4. 벡터 검색 함수 (선택사항)

```sql
CREATE OR REPLACE FUNCTION match_resume_sections(
    query_embedding VECTOR(768),
    match_threshold FLOAT DEFAULT 0.7,
    match_count INT DEFAULT 5,
    keywords TEXT[] DEFAULT NULL
)
RETURNS TABLE (
    id BIGINT,
    type VARCHAR(50),
    title VARCHAR(200),
    content TEXT,
    metadata JSONB,
    similarity FLOAT
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        resume_sections.id,
        resume_sections.type,
        resume_sections.title,
        resume_sections.content,
        resume_sections.metadata,
        1 - (resume_sections.embedding <=> query_embedding) AS similarity
    FROM resume_sections
    WHERE
        1 - (resume_sections.embedding <=> query_embedding) > match_threshold
        AND (keywords IS NULL OR resume_sections.content ILIKE ANY(keywords))
    ORDER BY similarity DESC
    LIMIT match_count;
END;
$$;
```

### 데이터 예시

```json
{
  "id": 1,
  "type": "experience",
  "title": "A사 백엔드 개발자",
  "content": "A사에서 백엔드 개발자로 근무하며 Spring Boot와 JPA를 활용한 RESTful API를 설계 및 구현했습니다. MSA 아키텍처 기반의 마이크로서비스를 개발하고, Redis를 활용한 캐싱 전략을 도입하여 응답 속도를 30% 개선했습니다.",
  "metadata": {
    "company": "A사",
    "position": "백엔드 개발자",
    "period": "2022.01 ~ 2023.12",
    "tech_stack": ["Spring Boot", "JPA", "Redis", "PostgreSQL"]
  },
  "embedding": [0.123, -0.456, 0.789, ...]
}
```

---

## API 명세

### 1. 채팅 스트리밍

**Endpoint**: `GET /api/chat/stream`

**Query Parameters**:
- `question` (required): 사용자 질문

**Response**: `text/event-stream` (SSE)

```
data: {"type":"token","content":"네, "}

data: {"type":"token","content":"Java와 "}

data: {"type":"token","content":"Spring Boot "}

data: {"type":"token","content":"경험이 "}

data: {"type":"token","content":"풍부합니다. "}

data: {"type":"done"}
```

---

## 구현 단계

### Phase 1: 프로젝트 기본 구조 (1-2일)
- [ ] 프로젝트 폴더 구조 생성
- [ ] Spring Boot 프로젝트 초기화
- [ ] Next.js 프로젝트 초기화
- [ ] 기본 의존성 설정

### Phase 2: PostgreSQL + pgvector 셋업 (1일)
- [ ] PostgreSQL 13+ 설치 및 실행
- [ ] pgvector 확장 설치 및 활성화
- [ ] `popo_ai` 데이터베이스 생성
- [ ] `resume_sections` 테이블 생성
- [ ] 벡터 검색 인덱스 생성 (HNSW/IVFFlat)
- [ ] 키워드 검색 인덱스 생성 (GIN)
- [ ] 환경 변수 설정 (DB 연결 정보)

### Phase 3: Backend - 기본 API (2-3일)
- [ ] Spring Data JPA Entity 및 Repository 구현
  - ResumeSection Entity (pgvector 타입 지원)
  - ResumeSectionRepository (네이티브 쿼리로 벡터 검색)
- [ ] OpenAI Service 구현 (Spring AI 사용)
  - 임베딩 생성
  - 채팅 완성 (스트리밍)
- [ ] 키워드 추출 유틸리티 구현
  - 정규식 기반 키워드 추출
  - 불용어 제거

### Phase 4: Backend - RAG 구현 (2-3일)
- [ ] ChatService 구현
  - 키워드 추출
  - 질문 임베딩 생성
  - 하이브리드 검색 (키워드 + 벡터)
  - 컨텍스트 구성
  - 프롬프트 생성
- [ ] ChatController 구현
  - SSE 엔드포인트
  - 스트리밍 응답 처리
- [ ] 에러 핸들링 및 로깅

### Phase 5: Frontend - 기본 UI (2일)
- [x] 채팅 인터페이스 컴포넌트
- [x] EventSource 연결
- [x] 메시지 히스토리 관리
- [x] 헤더 네비게이션
- [x] 다크 테마 적용
- [ ] 마크다운 렌더링

### Phase 6: 테스트 & 최적화 (2-3일)
- [ ] 단위 테스트 작성
- [ ] 통합 테스트
- [ ] 프롬프트 엔지니어링
- [ ] 성능 최적화
  - 캐싱 (Redis 선택적)
  - 임베딩 재사용
- [ ] 에러 핸들링 개선

### Phase 7: 배포 (1일)
- [ ] Backend: Railway / Render / AWS
- [ ] Frontend: Vercel
- [ ] 환경 변수 설정
- [ ] CORS 설정

**총 예상 기간**: 2주

---

## 환경 변수 설정

### Backend (application.yml 또는 application.properties)

**application.properties:**
```properties
spring.application.name=popo-backend

# PostgreSQL 설정
spring.datasource.url=jdbc:postgresql://localhost:5432/popo_ai
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:password}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA 설정
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# OpenAI 설정 (Spring AI)
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-3.5-turbo
spring.ai.openai.embedding.options.model=text-embedding-3-small

# 서버 설정
server.port=8080
```

**application.yml (대안):**
```yaml
spring:
  application:
    name: popo-backend

  datasource:
    url: jdbc:postgresql://localhost:5432/popo_ai
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-3.5-turbo
      embedding:
        options:
          model: text-embedding-3-small

server:
  port: 8080
```

### Frontend (.env.local)

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

## 참고 자료

### RAG 패턴
- [LangChain Documentation](https://docs.langchain.com/)
- [OpenAI Embeddings Guide](https://platform.openai.com/docs/guides/embeddings)

### 벡터 검색
- [pgvector GitHub](https://github.com/pgvector/pgvector)
- [Supabase Vector Guide](https://supabase.com/docs/guides/ai/vector-columns)

### 스트리밍
- [OpenAI Streaming](https://platform.openai.com/docs/api-reference/streaming)
- [Server-Sent Events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)

---

## 프로젝트 구조 (예상)

```
popo-ai/
├── popo-backend/               # Spring Boot (Java)
│   ├── src/main/
│   │   ├── java/com/example/popobackend/
│   │   │   ├── PopoBackendApplication.java
│   │   │   ├── controller/
│   │   │   │   └── ChatController.java
│   │   │   ├── service/
│   │   │   │   ├── ChatService.java
│   │   │   │   └── OpenAIService.java (Spring AI 통합)
│   │   │   ├── repository/
│   │   │   │   └── ResumeSectionRepository.java
│   │   │   ├── entity/
│   │   │   │   └── ResumeSection.java (pgvector 지원)
│   │   │   ├── dto/
│   │   │   │   ├── ChatRequest.java
│   │   │   │   ├── ChatResponse.java
│   │   │   │   └── SearchResult.java
│   │   │   ├── util/
│   │   │   │   └── KeywordExtractor.java (키워드 추출)
│   │   │   └── config/
│   │   │       ├── WebConfig.java
│   │   │       └── DatabaseConfig.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── schema.sql (선택사항)
│   ├── build.gradle
│   └── README.md
│
└── frontend/                   # Next.js
    ├── app/
    │   ├── page.tsx            # 메인 페이지 (채팅)
    │   ├── layout.tsx          # 루트 레이아웃
    │   └── globals.css         # 글로벌 스타일
    ├── components/
    │   ├── ChatInterface.tsx   # 채팅 메인 컴포넌트 (헤더 포함)
    │   └── MessageList.tsx     # 메시지 리스트
    ├── lib/
    │   ├── api.ts              # API 클라이언트
    │   └── types.ts            # TypeScript 타입 정의
    ├── public/                 # 정적 파일
    ├── .env.local              # 환경 변수
    ├── biome.json              # Biome 설정
    ├── package.json
    ├── tsconfig.json
    └── README.md
```

---

## 다음 단계

1. PostgreSQL 13+ 설치 및 pgvector 확장 설정
2. OpenAI API 키 발급
3. Spring Boot에 PostgreSQL 및 pgvector 의존성 추가
4. Entity 및 Repository 구현
5. 키워드 추출 로직 구현
6. RAG 파이프라인 구현

## 추가 참고사항

### pgvector와 Spring Boot 통합

**build.gradle 의존성 추가:**
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.postgresql:postgresql'
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
    // pgvector 지원을 위한 추가 의존성
    implementation 'com.pgvector:pgvector:0.1.4'
}
```

### Entity 예시 (pgvector 지원)

```java
@Entity
@Table(name = "resume_sections")
public class ResumeSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    // pgvector 타입
    @Column(columnDefinition = "vector(768)")
    private float[] embedding;

    // getters, setters...
}
```

### Repository 예시 (벡터 검색 쿼리)

```java
public interface ResumeSectionRepository extends JpaRepository<ResumeSection, Long> {

    @Query(value = """
        SELECT *, 1 - (embedding <=> CAST(:embedding AS vector)) AS similarity
        FROM resume_sections
        WHERE 1 - (embedding <=> CAST(:embedding AS vector)) > :threshold
        AND (:keywords IS NULL OR content ILIKE ANY(:keywords))
        ORDER BY similarity DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<ResumeSection> findByHybridSearch(
        @Param("embedding") String embedding,
        @Param("threshold") double threshold,
        @Param("keywords") String[] keywords,
        @Param("limit") int limit
    );
}
```

질문이나 추가 설명이 필요하면 언제든 문의하세요!

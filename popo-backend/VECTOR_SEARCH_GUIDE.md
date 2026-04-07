# pgvector 벡터 검색 완벽 가이드

pgvector를 사용한 벡터 검색의 원리와 문법을 이해하기 위한 학습 문서입니다.

---

## 📋 목차

1. [벡터란 무엇인가?](#1-벡터란-무엇인가)
2. [임베딩(Embedding)이란?](#2-임베딩embedding이란)
3. [pgvector 기본 개념](#3-pgvector-기본-개념)
4. [거리 측정 방법](#4-거리-측정-방법)
5. [pgvector 연산자](#5-pgvector-연산자)
6. [실전 예제](#6-실전-예제)
7. [성능 최적화](#7-성능-최적화)
8. [참고 자료](#8-참고-자료)

---

## 1. 벡터란 무엇인가?

### 벡터의 기본 개념

**벡터(Vector)**는 크기와 방향을 가진 숫자들의 배열입니다.

```
1차원 벡터: [5]
2차원 벡터: [3, 4]
3차원 벡터: [1, 2, 3]
1536차원 벡터: [0.123, -0.456, 0.789, ..., 0.321]  ← OpenAI Embeddings
```

### 텍스트를 벡터로 표현하는 이유

컴퓨터는 숫자만 이해할 수 있습니다. 따라서:

```
텍스트: "Spring Boot 프로젝트"
    ↓ (임베딩)
벡터: [0.12, -0.34, 0.56, ..., 0.78]  (1536개 숫자)

텍스트: "Node.js 프로젝트"
    ↓ (임베딩)
벡터: [0.11, -0.32, 0.54, ..., 0.76]  (1536개 숫자)
```

**의미가 비슷한 텍스트는 비슷한 벡터로 변환됩니다!**

---

## 2. 임베딩(Embedding)이란?

### 임베딩의 정의

**임베딩(Embedding)**은 텍스트를 고차원 벡터 공간의 점으로 변환하는 과정입니다.

### 시각적 이해 (2차원 예시)

```
      Y
      ↑
    2 |     "프로젝트"
      |        ●
    1 |  ●
      |  "개발"
    0 |________________→ X
      0   1   2   3   4

"개발" = [1, 1]
"프로젝트" = [3, 2]
```

실제로는 1536차원 공간에 존재하지만, 원리는 동일합니다.

### OpenAI Embeddings API

```java
// "Spring Boot 개발" → [0.123, -0.456, ..., 0.789] (1536차원)
float[] embedding = embeddingService.createEmbedding("Spring Boot 개발");

System.out.println(embedding.length);  // 1536
System.out.println(embedding[0]);      // 0.123
System.out.println(embedding[1]);      // -0.456
```

**특징**:
- **text-embedding-3-small**: 1536차원 벡터 생성
- **의미적 유사성 보존**: 비슷한 의미 → 비슷한 벡터
- **언어 무관**: 한국어, 영어 모두 동일한 공간에 매핑

---

## 3. pgvector 기본 개념

### pgvector란?

**pgvector**는 PostgreSQL에서 벡터 데이터를 저장하고 검색할 수 있게 해주는 확장(extension)입니다.

### 설치 및 활성화

```sql
-- 1. Extension 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. 확인
\dx vector
```

### 벡터 타입 선언

```sql
-- 테이블 생성 시 벡터 컬럼 선언
CREATE TABLE portfolio_data (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500),
    content TEXT,
    embedding vector(1536)  -- ✅ 1536차원 벡터
);
```

**`vector(1536)`의 의미**:
- `vector`: pgvector 데이터 타입
- `(1536)`: 차원 수 (OpenAI embeddings 차원)

### 벡터 데이터 삽입

```sql
-- 벡터 삽입
INSERT INTO portfolio_data (title, content, embedding) VALUES (
    'POPO 프로젝트',
    'Spring Boot 기반 AI 챗봇',
    '[0.123, -0.456, 0.789, ...]'::vector  -- ✅ 배열을 벡터로 캐스팅
);
```

---

## 4. 거리 측정 방법

벡터 간의 "유사도"를 측정하는 방법입니다.

### (1) 유클리디안 거리 (L2 Distance)

두 점 사이의 직선 거리 (피타고라스 정리)

```
A = [1, 2]
B = [4, 6]

거리 = √((4-1)² + (6-2)²) = √(9 + 16) = √25 = 5
```

**특징**:
- 가장 직관적
- 벡터의 크기(magnitude)에 영향을 받음
- **거리가 작을수록 유사함**

### (2) 코사인 거리 (Cosine Distance)

두 벡터 사이의 각도를 측정

```
       B
      /
     /  θ (각도)
    /
   A
```

**코사인 유사도 (Cosine Similarity)**:
```
cos(θ) = (A · B) / (||A|| × ||B||)

A · B = A[0]×B[0] + A[1]×B[1] + ... (내적, Dot Product)
||A|| = √(A[0]² + A[1]² + ...) (크기, Magnitude)
```

**코사인 거리 (Cosine Distance)**:
```
거리 = 1 - cos(θ)
```

**값의 범위**:
- `cos(θ) = 1.0`: 완전히 같은 방향 (유사도 100%)
- `cos(θ) = 0.0`: 직각 (무관계)
- `cos(θ) = -1.0`: 정반대 방향

**코사인 거리 범위**:
- `0.0`: 완전히 동일 (거리 최소)
- `2.0`: 정반대 (거리 최대)

**특징**:
- 벡터의 방향만 중요
- 크기(magnitude)에 영향 받지 않음
- **텍스트 유사도에 적합** ✅

### (3) 내적 (Inner Product, Dot Product)

```
A · B = A[0]×B[0] + A[1]×B[1] + ...

A = [2, 3]
B = [4, 5]
A · B = 2×4 + 3×5 = 8 + 15 = 23
```

**특징**:
- 크기와 방향 모두 고려
- **값이 클수록 유사함** (주의: 음수 가능)

### 어느 것을 사용해야 하나?

| 거리 측정 방법 | 사용 사례 | pgvector 연산자 |
|---------------|----------|-----------------|
| **코사인 거리** | **텍스트 유사도** (가장 일반적) ✅ | `<=>` |
| 유클리디안 거리 | 이미지 유사도, 추천 시스템 | `<->` |
| 내적 | 특수한 경우 (정규화된 벡터) | `<#>` |

**POPO 프로젝트는 코사인 거리 사용** ✅

---

## 5. pgvector 연산자

### 연산자 목록

| 연산자 | 거리 측정 방법 | 설명 | 정렬 순서 |
|--------|---------------|------|-----------|
| `<->` | L2 (유클리디안) | 직선 거리 | 작을수록 유사 |
| `<=>` | Cosine (코사인) | 방향 유사도 | 작을수록 유사 |
| `<#>` | Inner Product (내적) | 내적 값 | **클수록 유사** (음수) |

### 코사인 거리 연산자 `<=>`

```sql
-- 예시
SELECT
    title,
    embedding <=> '[0.1, 0.2, 0.3, ...]'::vector AS distance
FROM portfolio_data
ORDER BY distance
LIMIT 5;
```

**해석**:
- `embedding`: 데이터베이스의 벡터 (1536차원)
- `<=>`: 코사인 거리 연산자
- `'[0.1, 0.2, ...]'::vector`: 검색 쿼리 벡터
- `AS distance`: 거리를 distance 컬럼으로 표시
- `ORDER BY distance`: 거리가 작은 순서대로 정렬
- `LIMIT 5`: 상위 5개만 반환

**결과 예시**:
```
title                   | distance
------------------------|----------
POPO 프로젝트          | 0.12     ← 가장 유사 (거리 짧음)
DMS Portal             | 0.25
경력 (그렉터)          | 0.48
학력                   | 0.85     ← 가장 다름 (거리 멀음)
```

---

## 6. 실전 예제

### POPO 프로젝트 실제 코드 분석

#### (1) 벡터 검색 쿼리

```sql
-- PortfolioDataRepository.java
SELECT
    id,
    type,
    title,
    content,
    metadata,
    source,
    priority,
    1 - (embedding <=> CAST(:queryEmbedding AS vector)) AS similarity
FROM portfolio_data
WHERE is_public = true
AND 1 - (embedding <=> CAST(:queryEmbedding AS vector)) >= 0.6
ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
LIMIT 5
```

**단계별 분석**:

#### 1단계: 코사인 거리 계산
```sql
embedding <=> CAST(:queryEmbedding AS vector)
```

- `embedding`: DB에 저장된 벡터 (각 row)
- `<=>`: 코사인 거리 연산자
- `CAST(:queryEmbedding AS vector)`: 파라미터를 vector 타입으로 변환
- **결과**: 0.0 ~ 2.0 (작을수록 유사)

#### 2단계: 코사인 유사도 계산
```sql
1 - (embedding <=> CAST(:queryEmbedding AS vector)) AS similarity
```

- `1 - 거리` = **유사도**
- 거리 0.0 → 유사도 1.0 (100%)
- 거리 0.4 → 유사도 0.6 (60%)
- 거리 1.0 → 유사도 0.0 (0%)
- **결과**: 0.0 ~ 1.0 (클수록 유사)

**왜 `1 - 거리`를 하나요?**
- 거리: 작을수록 유사 (직관과 반대)
- 유사도: 클수록 유사 (직관적)
- 사람이 이해하기 쉽게 변환!

#### 3단계: 필터링
```sql
WHERE 1 - (embedding <=> CAST(:queryEmbedding AS vector)) >= 0.6
```

- 유사도 60% 이상만 선택
- 관련 없는 결과 제거

#### 4단계: 정렬
```sql
ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
```

- 거리가 작은 순서대로 정렬
- 가장 유사한 것부터 반환

#### 5단계: 상위 5개 반환
```sql
LIMIT 5
```

### (2) Java에서 벡터 전달

```java
// VectorSearchService.java

// 1. 쿼리를 벡터로 변환
float[] queryEmbedding = embeddingService.createEmbedding("Spring Boot 프로젝트");
// queryEmbedding = [0.123, -0.456, 0.789, ..., 0.321] (1536개)

// 2. Repository 호출
List<Object[]> results = portfolioDataRepository.findSimilar(queryEmbedding, 5);

// 3. 결과 처리
for (Object[] row : results) {
    Long id = (Long) row[0];
    String type = (String) row[1];
    String title = (String) row[2];
    String content = (String) row[3];
    Double similarity = ((Number) row[7]).doubleValue();

    System.out.printf("유사도: %.2f%% | %s%n", similarity * 100, title);
}
```

**출력 예시**:
```
유사도: 92.3% | POPO - AI 기반 포트폴리오 챗봇
유사도: 78.5% | 불도저(BDZ) - 주차 플랫폼
유사도: 65.2% | Aliot DMS Portal
```

### (3) 실제 동작 시뮬레이션

```sql
-- 질문: "React 프로젝트 경험 있나요?"
-- queryEmbedding: [0.11, 0.22, 0.33, ...]

-- DB 데이터:
Row 1: POPO (Spring Boot)     embedding: [0.15, 0.25, 0.35, ...]
Row 2: DMS Portal (Next.js)   embedding: [0.10, 0.21, 0.32, ...]
Row 3: 경력 (그렉터)          embedding: [0.50, 0.60, 0.70, ...]

-- 계산:
Row 1: 거리 0.30 → 유사도 0.70 (70%) ✅
Row 2: 거리 0.05 → 유사도 0.95 (95%) ✅
Row 3: 거리 0.80 → 유사도 0.20 (20%) ❌ (< 60%)

-- 결과 (60% 이상만, 거리 오름차순):
1. DMS Portal (Next.js) - 95%
2. POPO (Spring Boot) - 70%
```

---

## 7. 성능 최적화

### (1) 인덱스 생성

벡터 검색은 매우 느릴 수 있습니다. 인덱스로 가속화하세요.

#### IVFFlat 인덱스

```sql
-- IVFFlat: Inverted File with Flat compression
CREATE INDEX idx_portfolio_embedding
ON portfolio_data
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);
```

**파라미터 설명**:
- `ivfflat`: 근사 최근접 이웃 (Approximate Nearest Neighbor) 알고리즘
- `vector_cosine_ops`: 코사인 거리 연산
- `lists = 100`: 클러스터 개수 (데이터 1000개당 1개 권장)

**주의사항**:
- 데이터가 충분히 있어야 효과적 (최소 100개 이상)
- 정확도와 속도의 트레이드오프

#### HNSW 인덱스 (더 빠름, PostgreSQL 14+)

```sql
CREATE INDEX idx_portfolio_embedding
ON portfolio_data
USING hnsw (embedding vector_cosine_ops);
```

**특징**:
- IVFFlat보다 빠름
- 메모리 사용량이 더 많음

### (2) 연산자별 인덱스

```sql
-- 코사인 거리 (우리가 사용하는 것) ✅
vector_cosine_ops

-- L2 거리
vector_l2_ops

-- 내적
vector_ip_ops
```

### (3) 검색 성능 측정

```sql
-- 실행 계획 확인
EXPLAIN ANALYZE
SELECT id, title,
       1 - (embedding <=> '[0.1, 0.2, ...]'::vector) AS similarity
FROM portfolio_data
ORDER BY embedding <=> '[0.1, 0.2, ...]'::vector
LIMIT 5;
```

---

## 8. 참고 자료

### 공식 문서

1. **pgvector GitHub**
   - https://github.com/pgvector/pgvector
   - Installation, usage, examples

2. **OpenAI Embeddings API**
   - https://platform.openai.com/docs/guides/embeddings
   - Models, pricing, best practices

3. **PostgreSQL Vector Types**
   - https://www.postgresql.org/docs/current/arrays.html

### 학습 자료

1. **벡터 검색 원리**
   - [Pinecone Learning Center](https://www.pinecone.io/learn/vector-search/)
   - [OpenAI Cookbook - Embeddings](https://cookbook.openai.com/examples/get_embeddings)

2. **코사인 유사도**
   - [위키피디아 - Cosine Similarity](https://en.wikipedia.org/wiki/Cosine_similarity)
   - [YouTube - StatQuest](https://www.youtube.com/watch?v=e9U0QAFbfLI)

3. **RAG 패턴**
   - [LangChain Documentation](https://python.langchain.com/docs/use_cases/question_answering/)
   - [Anthropic RAG Guide](https://docs.anthropic.com/claude/docs/retrieval-augmented-generation)

### 실습 예제

```sql
-- 1. 간단한 벡터 테스트 테이블
CREATE TABLE test_vectors (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    vec vector(3)  -- 3차원으로 간단히 테스트
);

-- 2. 데이터 삽입
INSERT INTO test_vectors (name, vec) VALUES
('A', '[1, 0, 0]'),
('B', '[0.9, 0.1, 0]'),
('C', '[0, 1, 0]'),
('D', '[0, 0, 1]');

-- 3. [1, 0, 0]과 유사한 벡터 찾기
SELECT
    name,
    vec,
    vec <=> '[1, 0, 0]'::vector AS distance,
    1 - (vec <=> '[1, 0, 0]'::vector) AS similarity
FROM test_vectors
ORDER BY distance;

-- 결과:
-- A: distance=0.00, similarity=1.00 (완전 동일)
-- B: distance=0.14, similarity=0.86 (매우 유사)
-- C: distance=1.00, similarity=0.00 (직각)
-- D: distance=1.00, similarity=0.00 (직각)
```

---

## 핵심 요약

### 1. **벡터 = 숫자 배열**
```
"Spring Boot" → [0.12, -0.34, ..., 0.78] (1536개)
```

### 2. **코사인 거리 (<=>)**
```sql
embedding <=> query_vector
```
- 0.0 ~ 2.0 범위
- 작을수록 유사

### 3. **코사인 유사도**
```sql
1 - (embedding <=> query_vector)
```
- 0.0 ~ 1.0 범위
- 클수록 유사

### 4. **POPO 프로젝트 쿼리**
```sql
-- 유사도 60% 이상만 선택
WHERE 1 - (embedding <=> query) >= 0.6
-- 거리 오름차순 정렬
ORDER BY embedding <=> query
-- 상위 5개
LIMIT 5
```

### 5. **성능 최적화**
```sql
-- 인덱스 생성 (100개 이상 데이터 있을 때)
CREATE INDEX ON portfolio_data
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);
```

---

## 다음 학습 단계

1. ✅ **기본**: 벡터와 임베딩 개념 이해
2. ✅ **중급**: 코사인 거리와 유사도 계산
3. ✅ **실전**: pgvector SQL 쿼리 작성
4. ⬜ **고급**: 인덱스 최적화 및 튜닝
5. ⬜ **심화**: 하이브리드 검색 (키워드 + 벡터)

---

**🎓 이 문서로 pgvector의 핵심 개념을 이해했다면, 실제 코드를 직접 실행해보며 익히세요!**

**💡 Tip**: PostgreSQL에 직접 접속해서 위의 `test_vectors` 예제를 실행해보면 더 명확히 이해할 수 있습니다!

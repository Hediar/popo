# POPO-AI

> 이력서 데이터 기반 AI 챗봇 프로젝트

## 소개

이력서에 대한 질문에 자동으로 답변하는 AI 챗봇입니다.
RAG(Retrieval-Augmented Generation) 패턴을 사용하여 정확한 답변을 제공합니다.

## 기술 스택

### Frontend
- **Next.js 16+** (App Router)
- **TypeScript 5**
- **TailwindCSS 4**
- **React 19**
- **Biome** (Linter/Formatter)

### Backend
- **Spring Boot 4.0+**
- **Java 17**
- **Spring AI** (OpenAI 통합)
- **Spring Data JPA** (PostgreSQL 연동)
- **Gradle**

### Database & AI
- **PostgreSQL 13+** + **pgvector** 확장
  - 벡터 유사도 검색 (HNSW/IVFFlat 인덱싱)
  - 키워드 기반 Full-Text Search
- **OpenAI API**
  - `gpt-3.5-turbo` / `gpt-4` (답변 생성)
  - `text-embedding-3-small` (임베딩 생성, 768차원)

## 프로젝트 구조

```
popo-ai/
├── popo-backend/     # Spring Boot 백엔드 (Java)
├── frontend/         # Next.js 프론트엔드
├── ARCHITECTURE.md   # 상세 아키텍처 문서
└── README.md         # 프로젝트 개요
```

## 시작하기

### 사전 요구사항

- Java 17+
- Node.js 18+
- PostgreSQL 13+ (pgvector 확장 지원)
- OpenAI API 키

### 설치

```bash
# 저장소 클론
cd popo-ai

# PostgreSQL + pgvector 설정
# 1. PostgreSQL 13+ 설치
# 2. pgvector 확장 설치
# 3. 데이터베이스 생성
psql -U postgres
CREATE DATABASE popo_ai;
\c popo_ai
CREATE EXTENSION vector;

# 백엔드 설정
cd popo-backend
./gradlew build

# 프론트엔드 설정
cd ../frontend
npm install
```

### 환경 변수 설정

**popo-backend/src/main/resources/application.properties**
```properties
spring.application.name=popo-backend

# PostgreSQL 설정
spring.datasource.url=jdbc:postgresql://localhost:5432/popo_ai
spring.datasource.username=postgres
spring.datasource.password=your-password

# JPA 설정
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# OpenAI 설정
spring.ai.openai.api-key=your-openai-api-key
spring.ai.openai.chat.options.model=gpt-3.5-turbo
spring.ai.openai.embedding.options.model=text-embedding-3-small

server.port=8080
```

**frontend/.env.local**
```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### 실행

```bash
# 백엔드 실행
cd popo-backend
./gradlew bootRun

# 프론트엔드 실행 (새 터미널)
cd frontend
npm run dev
```

- Frontend: http://localhost:3000
- Backend: http://localhost:8080

## 문서

자세한 아키텍처 및 구현 가이드는 [ARCHITECTURE.md](./ARCHITECTURE.md)를 참고하세요.

## 주요 기능

- ✅ **키워드 추출 및 검색**: 사용자 질문에서 핵심 키워드 자동 추출
- ✅ **pgvector 기반 유사도 검색**: PostgreSQL + pgvector를 활용한 고속 벡터 검색
- ✅ **하이브리드 검색**: 키워드 필터링 + 벡터 유사도 검색으로 정확한 컨텍스트 추출
- ✅ **실시간 스트리밍 채팅**: SSE를 통한 실시간 답변 생성
- ✅ **타이핑 애니메이션**: 자연스러운 답변 표시
- ✅ **HNSW/IVFFlat 인덱싱**: 빠른 벡터 검색 성능



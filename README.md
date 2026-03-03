# POPO-AI

이력서 데이터 기반 AI 챗봇 프로젝트

## 소개

이력서에 대한 질문에 자동으로 답변하는 AI 챗봇입니다.
RAG(Retrieval-Augmented Generation) 패턴을 사용하여 정확한 답변을 제공합니다.

## 기술 스택

- **Frontend**: Next.js 14 + TypeScript + TailwindCSS
- **Backend**: Spring Boot 3.2 + Java 17
- **Database**: Supabase (PostgreSQL + pgvector)
- **AI**: OpenAI API (GPT-3.5/4, Embeddings)

## 프로젝트 구조

```
popo-ai/
├── backend/          # Spring Boot 백엔드
├── frontend/         # Next.js 프론트엔드
└── docs/            # 문서
```

## 시작하기

### 사전 요구사항

- Java 17+
- Node.js 18+
- Supabase 계정
- OpenAI API 키

### 설치

```bash
# 저장소 클론
cd popo-ai

# 백엔드 설정
cd backend
./gradlew build

# 프론트엔드 설정
cd ../frontend
npm install
```

### 환경 변수 설정

**backend/src/main/resources/application.yml**
```yaml
openai:
  api-key: your-openai-api-key

supabase:
  url: your-supabase-url
  key: your-supabase-key
```

**frontend/.env.local**
```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### 실행

```bash
# 백엔드 실행
cd backend
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

- 실시간 스트리밍 답변/채팅
- 하이브리드 검색 (키워드 + 벡터 유사도)
- 컨텍스트 기반 정확한 답변


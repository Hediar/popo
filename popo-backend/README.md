# POPO Backend - AI 기반 포트폴리오 챗봇

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue.svg)](https://www.postgresql.org/)
[![pgvector](https://img.shields.io/badge/pgvector-0.8.2-purple.svg)](https://github.com/pgvector/pgvector)

이력서, 포트폴리오, 경력기술서를 기반으로 방문자의 질문에 자연스럽게 답변하는 RAG 기반 AI 챗봇 백엔드입니다.

## 📋 목차

- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시스템 아키텍처](#-시스템-아키텍처)
- [빠른 시작](#-빠른-시작)
- [환경 변수 설정](#-환경-변수-설정)
- [API 엔드포인트](#-api-엔드포인트)
- [배포](#-배포)
- [프로젝트 구조](#-프로젝트-구조)
- [주요 기술 구현](#-주요-기술-구현)

---

## ✨ 주요 기능

### 1. **RAG (Retrieval Augmented Generation) 패턴**
- PostgreSQL + pgvector를 활용한 벡터 검색
- OpenAI Embeddings API로 1536차원 벡터 생성
- 코사인 유사도 기반 의미적 검색
- **유사도 60% 이상 결과만 필터링**

### 2. **지능형 타입 필터링**
- **경력(career)**: "회사", "경력", "재직", "그렉터" 등 키워드 감지
- **프로젝트(project)**: "프로젝트", "개발", "POPO", "DMS", "Aliot" 등 키워드 감지
- **프로필(profile)**: "소개", "기술스택", "학력", "자격증" 등 키워드 감지

### 3. **자연스러운 1인칭 답변**
- 시스템 프롬프트 구조화로 일관된 톤앤매너 유지
- 검색된 컨텍스트를 활용한 정확한 답변 생성
- GPT-4o-mini로 비용 효율 확보 (GPT-4 대비 200배 저렴)

### 4. **대화 컨텍스트 관리**
- 세션별 대화 이력 저장 (최근 5개 메시지)
- 검색 결과 상위 5개만 사용하여 토큰 최적화
- 비용과 성능의 균형

### 5. **Rate Limiting**
- Bucket4j를 사용한 트래픽 제어
- 분당 10회, 시간당 30회 요청 제한
- API 남용 방지

---

## 🛠 기술 스택

### Backend
- **Java 17**
- **Spring Boot 4.0.3**
- **Spring AI Framework** - OpenAI 통합
- **Spring Data JPA** - ORM

### Database
- **PostgreSQL 18**
- **pgvector 0.8.2** - 벡터 검색 확장

### AI/ML
- **OpenAI API**
  - GPT-4o-mini (Chat Completion)
  - text-embedding-3-small (Embeddings, 1536 dimensions)

### Others
- **Bucket4j** - Rate Limiting
- **Gradle 8+** - 빌드 도구
- **Docker** - 컨테이너화

---

## 🏗 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                         Frontend                            │
│                    (Next.js, Vercel)                        │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTPS
                     ↓
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Backend                      │
│                       (Render)                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  ChatController                                       │  │
│  │    ↓                                                  │  │
│  │  ChatService (프롬프트 구조화 + 대화 관리)          │  │
│  │    ↓                                                  │  │
│  │  VectorSearchService                                  │  │
│  │    - 키워드 감지 (career/project/profile)           │  │
│  │    - 타입별 필터링                                    │  │
│  │    ↓                                                  │  │
│  │  EmbeddingService                                     │  │
│  │    ↓                                                  │  │
│  │  OpenAI Embeddings API (1536 dimensions)             │  │
│  │    ↓                                                  │  │
│  │  PostgreSQL + pgvector                                │  │
│  │    - 코사인 유사도 검색 (>= 60%)                     │  │
│  │    - 상위 5개 결과 반환                               │  │
│  │    ↓                                                  │  │
│  │  OpenAI Chat API (GPT-4o-mini)                       │  │
│  │    - 검색된 컨텍스트 + 대화 이력                     │  │
│  │    - 1인칭 답변 생성                                  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│              PostgreSQL + pgvector (Render)                 │
│  - profile: 프로필 정보                                     │
│  - portfolio_data: 프로젝트/경력/학력 (벡터 검색용)        │
│  - chat_sessions: 대화 세션 이력                            │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 빠른 시작

### 1. 사전 요구사항

- Java 17 이상
- PostgreSQL 17+ with pgvector
- OpenAI API Key

### 2. 저장소 클론

```bash
git clone https://github.com/Hediar/popo.git
cd popo-backend
```

### 3. pgvector 설치 (로컬)

#### Debian/Ubuntu
```bash
sudo apt install -y postgresql-17-pgvector
```

#### 또는 Docker Compose 사용 (권장)
```bash
docker-compose up -d postgres
```

### 4. 환경 변수 설정

`.env` 파일 생성:

```bash
cp .env.example .env
```

`.env` 파일 편집:

```bash
# OpenAI API 키
OPENAI_API_KEY=sk-proj-your-api-key-here

# PostgreSQL (로컬)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=popo
DB_USERNAME=postgres
DB_PASSWORD=1234

# CORS (배포된 프론트엔드 URL)
CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app
```

### 5. 데이터베이스 초기화

```bash
# PostgreSQL 접속
psql -U postgres -d popo

# pgvector extension 활성화
CREATE EXTENSION IF NOT EXISTS vector;

# 테이블 생성 (application.properties에서 자동 실행됨)
# spring.jpa.hibernate.ddl-auto=create
# spring.sql.init.mode=always
```

### 6. 애플리케이션 실행

```bash
# Gradle로 실행
./gradlew bootRun

# 또는 JAR 빌드 후 실행
./gradlew build -x test
java -jar build/libs/popo-backend-0.0.1-SNAPSHOT.jar
```

### 7. Health Check

```bash
curl http://localhost:8080/health

# 또는
curl http://localhost:8080/api/health
```

예상 응답:
```json
{
  "status": "OK",
  "message": "POPO Backend API is running",
  "timestamp": "2026-03-25T...",
  "service": "popo-backend",
  "version": "1.0.0"
}
```

---

## 🔧 환경 변수 설정

### 필수 환경 변수

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `OPENAI_API_KEY` | OpenAI API 키 | `sk-proj-...` |
| `DB_HOST` | PostgreSQL 호스트 | `localhost` |
| `DB_PORT` | PostgreSQL 포트 | `5432` |
| `DB_NAME` | 데이터베이스 이름 | `popo` |
| `DB_USERNAME` | DB 사용자명 | `postgres` |
| `DB_PASSWORD` | DB 비밀번호 | `your-password` |

### 선택 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `PORT` | 서버 포트 (Render 자동 제공) | `8080` |
| `CORS_ALLOWED_ORIGINS` | CORS 허용 Origin (쉼표 구분) | 빈 문자열 (localhost만) |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | JPA DDL 모드 | `create` |
| `SPRING_JPA_SHOW_SQL` | SQL 로그 출력 | `true` |
| `LOGGING_LEVEL_ROOT` | 로그 레벨 | `INFO` |

---

## 📡 API 엔드포인트

### Health Check

```http
GET /health
GET /api/health
```

**응답**:
```json
{
  "status": "OK",
  "message": "POPO Backend API is running",
  "timestamp": "2026-03-25T15:30:00",
  "service": "popo-backend",
  "version": "1.0.0"
}
```

### 채팅 메시지 전송

```http
POST /api/chat/message
Content-Type: application/json
```

**요청 바디**:
```json
{
  "sessionId": "unique-session-id",
  "message": "Spring Boot 프로젝트 경험이 있나요?"
}
```

**응답**:
```json
{
  "response": "네, Spring Boot를 활용한 프로젝트 경험이 있습니다. 대표적으로 POPO 프로젝트에서 Spring Boot 4.0.3을 사용하여 RESTful API를 개발했습니다...",
  "sessionId": "unique-session-id",
  "timestamp": "2026-03-25T15:30:00"
}
```

### Rate Limiting

- **분당**: 10회
- **시간당**: 30회

초과 시 `429 Too Many Requests` 응답

---

## 🚢 배포

### Render 배포 (권장)

#### 1. GitHub 연동

```bash
# 변경사항 커밋 및 푸시
git add .
git commit -m "feat: Initial deployment"
git push origin main
```

#### 2. Render Blueprint 사용

`render.yaml` 파일이 이미 설정되어 있습니다:
- PostgreSQL 데이터베이스 자동 생성
- Web Service 자동 배포
- 환경 변수 자동 연결

Render Dashboard:
1. **New** → **Blueprint**
2. GitHub 저장소 선택: `popo-backend`
3. **Apply**

#### 3. 환경 변수 설정

Render Dashboard → Web Service → **Environment** 탭:

```bash
OPENAI_API_KEY=sk-proj-your-api-key
```

#### 4. pgvector Extension 활성화

Render Dashboard → Database → **Shell** 탭:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

#### 5. 초기 데이터 입력

`src/main/resources/data.sql` 참조하여 프로필 및 포트폴리오 데이터 입력

자세한 배포 가이드: [DEPLOYMENT.md](./DEPLOYMENT.md)

---

## 📁 프로젝트 구조

```
popo-backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/popobackend/
│   │   │   ├── controller/
│   │   │   │   ├── ChatController.java          # 채팅 API
│   │   │   │   └── HealthCheckController.java   # Health Check
│   │   │   ├── service/
│   │   │   │   ├── ChatService.java             # 대화 관리
│   │   │   │   ├── VectorSearchService.java     # 벡터 검색 (키워드 감지)
│   │   │   │   ├── EmbeddingService.java        # OpenAI Embeddings
│   │   │   │   └── OpenAIService.java           # OpenAI Chat API
│   │   │   ├── repository/
│   │   │   │   ├── PortfolioDataRepository.java # 벡터 검색 쿼리
│   │   │   │   ├── ProfileRepository.java
│   │   │   │   └── ChatSessionRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── PortfolioData.java           # 포트폴리오 (벡터)
│   │   │   │   ├── Profile.java                 # 프로필
│   │   │   │   └── ChatSession.java             # 대화 세션
│   │   │   ├── dto/
│   │   │   │   ├── ChatRequest.java
│   │   │   │   ├── ChatResponse.java
│   │   │   │   └── SearchResult.java
│   │   │   ├── config/
│   │   │   │   ├── WebConfig.java               # CORS 설정
│   │   │   │   ├── RateLimitFilter.java         # Rate Limiting
│   │   │   │   └── OpenAIConfig.java
│   │   │   ├── converter/
│   │   │   │   └── VectorConverter.java         # pgvector 타입 변환
│   │   │   └── exception/
│   │   │       ├── OpenAIException.java         # 커스텀 예외
│   │   │       ├── ErrorResponse.java
│   │   │       └── GlobalExceptionHandler.java  # 전역 예외 처리
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data.sql                         # 초기 데이터
│   └── test/
├── Dockerfile
├── docker-compose.yml
├── render.yaml
├── build.gradle
├── .env.example
├── .gitignore
├── README.md
├── DEPLOYMENT.md
└── TECHNICAL_DOCUMENTATION.md
```

---

## 🔬 주요 기술 구현

### 1. **벡터 검색 (RAG)**

#### 임베딩 생성
```java
// EmbeddingService.java
public float[] createEmbedding(String text) {
    EmbeddingRequest request = new EmbeddingRequest(
        List.of(text),
        EmbeddingOptionsBuilder.builder()
            .withModel("text-embedding-3-small")
            .build()
    );

    EmbeddingResponse response = embeddingModel.call(request);
    return response.getResult().getOutput(); // 1536 차원 벡터
}
```

#### 코사인 유사도 검색 (60% 이상)
```sql
-- PortfolioDataRepository.java
SELECT id, type, title, content, metadata, source, priority,
       1 - (embedding <=> CAST(:queryEmbedding AS vector)) AS similarity
FROM portfolio_data
WHERE is_public = true
AND 1 - (embedding <=> CAST(:queryEmbedding AS vector)) >= 0.6  -- 유사도 60% 이상
ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
LIMIT 5
```

#### 타입별 필터링
```java
// VectorSearchService.java
private static final List<String> CAREER_KEYWORDS = Arrays.asList(
    "회사", "경력", "직장", "재직", "근무", "업무", "그렉터", ...
);

private static final List<String> PROJECT_KEYWORDS = Arrays.asList(
    "프로젝트", "포트폴리오", "개발", "POPO", "DMS", "Aliot", ...
);

private static final List<String> PROFILE_KEYWORDS = Arrays.asList(
    "소개", "기술스택", "학력", "자격증", "GitHub", ...
);

private String detectTypeFilter(String query) {
    String lower = query.toLowerCase();

    // career > project > profile 우선순위
    for (String keyword : CAREER_KEYWORDS) {
        if (lower.contains(keyword)) return "career";
    }
    for (String keyword : PROJECT_KEYWORDS) {
        if (lower.contains(keyword)) return "project";
    }
    return null; // 전체 검색
}
```

### 2. **시스템 프롬프트 구조화**

```java
// ChatService.java
private String buildSystemPrompt(Profile profile, String context) {
    return """
        당신은 이세령의 포트폴리오 챗봇입니다.

        ## 역할
        - 이세령의 프로필, 프로젝트, 경력을 소개
        - 1인칭 시점으로 자연스럽게 답변
        - 검색된 관련 정보를 바탕으로 정확하게 답변

        ## 답변 규칙
        1. 항상 1인칭("저는", "제가")으로 답변
        2. 검색된 컨텍스트에 근거하여 답변
        3. 모르는 내용은 솔직하게 "잘 모르겠습니다" 표현

        ## 검색된 관련 정보
        %s

        ## 이세령 프로필
        - 이름: %s
        - 직업: %s
        - 경력: %s
        - 회사: %s
        - 기술스택: %s
        """.formatted(context, profile.getName(), ...);
}
```

### 3. **대화 컨텍스트 관리**

```java
// 최근 5개 메시지만 유지
List<MessageDto> recentMessages = chatSession.getMessages()
    .stream()
    .skip(Math.max(0, chatSession.getMessages().size() - 5))
    .toList();
```

### 4. **Rate Limiting**

```java
// RateLimitFilter.java
private final Bucket bucket = Bucket.builder()
    .addLimit(Bandwidth.simple(10, Duration.ofMinutes(1)))  // 분당 10회
    .addLimit(Bandwidth.simple(30, Duration.ofHours(1)))    // 시간당 30회
    .build();
```

### 5. **에러 핸들링**

```java
// GlobalExceptionHandler.java
@ExceptionHandler(OpenAIException.class)
public ResponseEntity<ErrorResponse> handleOpenAIException(OpenAIException e) {
    ErrorResponse errorResponse = ErrorResponse.of(
        e.getErrorCode(),
        e.getMessage(),
        e.getHttpStatus().value()
    );
    return ResponseEntity.status(e.getHttpStatus()).body(errorResponse);
}
```

---

## 📝 관련 문서

- [배포 가이드](./DEPLOYMENT.md) - Render 배포 상세 가이드
- [기술 문서](./TECHNICAL_DOCUMENTATION.md) - 시스템 아키텍처 및 면접 준비

---

## 🤝 기여

이슈와 PR은 언제나 환영합니다!

---

## 📄 라이선스

이 프로젝트는 개인 포트폴리오 용도로 제작되었습니다.

---

## 👤 개발자

**이세령**
- Email: srlimvp@gmail.com
- GitHub: [@Hediar](https://github.com/Hediar)
- Blog: [velog.io/@hediar](https://velog.io/@hediar)

---

**🤖 Built with [Claude Code](https://claude.com/claude-code)**

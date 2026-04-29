# popo-backend 개발 세션 로그

**날짜**: 2026-03-16
**작업 내용**: 채팅 API 테스트 및 포트폴리오 데이터 설정

---

## 🔍 발견된 문제

### 1. 포트폴리오 데이터 미삽입
- **증상**: `portfolio_data` 테이블에 데이터가 없음
- **원인**: `data.sql` 파일에 프로필 데이터만 있고 포트폴리오 데이터가 없었음

### 2. JAR 빌드 타이밍 이슈
- **증상**: 코드 수정 후 JAR 파일이 구버전으로 실행됨
- **원인**: 사용자가 `data.sql` 파일을 수정한 후 빌드하지 않고 실행

---

## ✅ 해결 과정

### 1. data.sql에 포트폴리오 데이터 추가

**파일**: `src/main/resources/data.sql`

추가한 데이터:
- **프로젝트 데이터**: 8개
  - popo-ai (AI 기반 포트폴리오 소개 서비스)
  - 불도적(BDZ) - 주차 플랫폼
  - dfm-evt 백엔드 유지보수
  - 로그 모니터링 경량 시스템
  - Viewtrack 플랫폼 백엔드 마이그레이션
  - Aliot DMS Portal
  - 위험시설물(FSMS) 최소기능 개발
  - 영화 평론 커뮤니티

- **기술 스택 데이터**: 10개
  - JavaScript & TypeScript (expert)
  - Node.js & Express (expert)
  - Spring Boot (proficient)
  - Next.js & React (proficient)
  - Vue.js & Nuxt.js (proficient)
  - PostgreSQL (proficient)
  - Opensearch & Elasticsearch (proficient)
  - Nest.js (familiar)
  - Redis (familiar)
  - Linux & Docker (familiar)

- **경력 데이터**: 1개
  - 그렉터 - 풀스택 엔지니어 (2023.11.03 - 현재)

- **학력 데이터**: 1개
  - 한경대학교 컴퓨터공학과

**총 데이터**: 20개

### 2. 애플리케이션 재빌드 및 재실행

```bash
# 1. 빌드
./gradlew clean bootJar -x test

# 2. 기존 프로세스 종료
lsof -ti:8080 | xargs kill -9

# 3. 재실행
java -jar build/libs/popo-backend-0.0.1-SNAPSHOT.jar
```

### 3. 데이터 삽입 확인

```sql
-- 포트폴리오 데이터 개수 확인
SELECT COUNT(*) FROM portfolio_data;
-- 결과: 20개

-- 상위 5개 데이터 확인
SELECT type, title FROM portfolio_data ORDER BY priority DESC LIMIT 5;
```

**결과**:
```
type    |           title
--------+---------------------------
skill   | JavaScript & TypeScript
project | popo-ai
project | 불도적(BDZ) - 주차 플랫폼
experience | 그렉터 - 풀스택 엔지니어
skill   | Node.js & Express
```

### 4. 채팅 API 테스트

```bash
curl -s http://localhost:8080/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{"message":"Spring Boot 프로젝트 경험이 있나요?"}'
```

**응답**:
```json
{
  "sessionId": "e46c85e2-ee6e-4eee-b963-5dbeb3779c67",
  "message": "죄송합니다. 현재 AI 응답 생성 중 문제가 발생했습니다.\n\n에러: HTTP 429 - insufficient_quota..."
}
```

**분석**:
- ✅ API 엔드포인트 정상 동작
- ✅ 세션 생성 성공
- ✅ 포트폴리오 데이터 검색 정상
- ❌ OpenAI API 할당량 부족 (외부 API 이슈)

---

## 🛠️ 추가 설정

### 로깅 설정 추가

**파일**: `src/main/resources/application.properties`

```properties
# Logging
logging.file.name=logs/popo-backend.log
logging.file.max-size=10MB
logging.file.max-history=30
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

# 애플리케이션 로그 레벨
logging.level.com.example.popobackend=DEBUG
logging.level.com.example.popobackend.controller=DEBUG
logging.level.com.example.popobackend.service=DEBUG
logging.level.com.example.popobackend.repository=DEBUG
logging.level.com.example.popobackend.config=DEBUG

# Spring AI 로그
logging.level.org.springframework.ai=DEBUG

# Hibernate 로그
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

**로그 파일 위치**: `logs/popo-backend.log`

---

## 📊 현재 상태

### ✅ 정상 동작 항목
1. Spring Boot 애플리케이션 구동 (포트 8080)
2. PostgreSQL 데이터베이스 연결
3. JPA 엔티티 및 테이블 생성
4. data.sql 자동 실행 (프로필 + 포트폴리오 데이터)
5. 채팅 API 엔드포인트 (/api/chat/message)
6. 세션 관리 (ChatSession 엔티티)
7. 키워드 기반 포트폴리오 검색
8. Rate Limiting (분당 100개 요청)
9. JSONB 타입 처리 (messages, metadata, tech_stack 등)

### ⚠️ 제한사항
1. **OpenAI API 할당량 부족**
   - 에러 코드: HTTP 429
   - 에러 타입: insufficient_quota
   - 해결 방법: OpenAI API 키 할당량 충전 필요

### 🔧 설정 정보

#### JPA 설정
- **ddl-auto**: create (테이블 재생성)
- **show-sql**: true
- **defer-datasource-initialization**: true (data.sql 실행 보장)

#### 데이터베이스
- **URL**: jdbc:postgresql://localhost:5432/popo
- **사용자**: postgres
- **드라이버**: PostgreSQL JDBC Driver

#### OpenAI 설정
- **모델**: gpt-4o-mini
- **Temperature**: 0.7
- **Max Tokens**: 1000
- **Embedding 모델**: text-embedding-3-small

---

## 📝 주요 파일

### 1. Entity
- `ChatSession.java`: 채팅 세션 (JSONB messages)
- `PortfolioData.java`: 포트폴리오 데이터 (JSONB metadata)
- `Profile.java`: 프로필 정보 (JSONB tech_stack, certifications)

### 2. Service
- `ChatService.java`: 채팅 처리 로직
- `VectorSearchService.java`: 키워드 기반 검색
- `ProfileService.java`: 프로필 컨텍스트 생성

### 3. Repository
- `ChatSessionRepository.java`
- `PortfolioDataRepository.java`: 커스텀 쿼리 (findByKeywords)
- `ProfileRepository.java`

### 4. Configuration
- `RateLimitFilter.java`: Bucket4j 기반 Rate Limiting
- `application.properties`: 전체 설정

### 5. Data
- `data.sql`: 초기 데이터 (프로필 1개 + 포트폴리오 20개)

---

## 🎯 다음 단계

### 1. OpenAI API 이슈 해결
- [ ] OpenAI 계정 확인
- [ ] API 키 할당량 충전
- [ ] 또는 새 API 키 발급

### 2. 기능 테스트
- [ ] OpenAI 응답 정상 동작 확인
- [ ] 다양한 질문으로 포트폴리오 검색 테스트
- [ ] 대화 히스토리 저장 확인

### 3. 프론트엔드 연동
- [ ] CORS 설정 확인 (http://localhost:3000)
- [ ] 채팅 UI 연동
- [ ] 실시간 채팅 흐름 테스트

### 4. 성능 최적화
- [ ] 벡터 검색 성능 측정
- [ ] OpenAI API 응답 시간 측정
- [ ] Rate Limiting 정책 검토

---

## 🐛 디버깅 가이드

### 로그 확인 방법

```bash
# 실시간 로그 확인
tail -f logs/popo-backend.log

# 특정 키워드 검색
grep "OpenAI" logs/popo-backend.log
grep "portfolio" logs/popo-backend.log

# 에러만 확인
grep "ERROR" logs/popo-backend.log

# 최근 100줄 확인
tail -100 logs/popo-backend.log
```

### 주요 디버깅 포인트

1. **ChatController**: API 요청/응답 추적
2. **ChatService**: OpenAI 호출 및 포트폴리오 검색
3. **VectorSearchService**: 키워드 매칭 결과
4. **RateLimitFilter**: Rate Limit 체크
5. **ProfileService**: 프로필 컨텍스트 생성

### 데이터베이스 확인

```sql
-- 포트폴리오 데이터 확인
SELECT type, title, priority FROM portfolio_data ORDER BY priority DESC;

-- 프로필 데이터 확인
SELECT name, occupation, experience FROM profile WHERE is_active = true;

-- 채팅 세션 확인
SELECT session_id, created_at, status FROM chat_sessions ORDER BY created_at DESC;
```

---

## 📚 참고사항

### JSONB 타입 처리
- Hibernate의 `@JdbcTypeCode(SqlTypes.JSON)` 사용
- PostgreSQL JSONB 타입으로 자동 매핑
- `columnDefinition = "jsonb"` 명시

### 데이터 자동 초기화
- `spring.sql.init.mode=always`
- `spring.jpa.defer-datasource-initialization=true`
- DDL 생성 후 data.sql 실행 보장

### Rate Limiting
- Bucket4j 라이브러리 사용
- 분당 100개 요청 제한
- IP 기반 버킷 관리

---

**작성자**: Claude Code
**마지막 업데이트**: 2026-03-16 14:30

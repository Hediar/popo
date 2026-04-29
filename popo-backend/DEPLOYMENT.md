# POPO Backend - Render 배포 가이드

## 📋 목차
1. [배포 전 준비](#1-배포-전-준비)
2. [GitHub 저장소 설정](#2-github-저장소-설정)
3. [Render 계정 생성](#3-render-계정-생성)
4. [PostgreSQL 데이터베이스 생성](#4-postgresql-데이터베이스-생성)
5. [pgvector Extension 설치](#5-pgvector-extension-설치)
6. [Web Service 배포](#6-web-service-배포)
7. [환경 변수 설정](#7-환경-변수-설정)
8. [배포 확인](#8-배포-확인)
9. [초기 데이터 입력](#9-초기-데이터-입력)
10. [문제 해결](#10-문제-해결)

---

## 1. 배포 전 준비

### ✅ 체크리스트

- [ ] OpenAI API 키 발급 완료
- [ ] GitHub 계정 생성
- [ ] 로컬에서 애플리케이션 정상 작동 확인
- [ ] .env 파일이 .gitignore에 포함되어 있는지 확인

### 로컬 테스트

```bash
# 1. 빌드 테스트
./gradlew clean build -x test

# 2. Docker 빌드 테스트 (선택)
docker build -t popo-backend .

# 3. Health Check
./gradlew bootRun
curl http://localhost:8080/api/health
```

---

## 2. GitHub 저장소 설정

### 2-1. 저장소 생성

```bash
# GitHub에서 새 저장소 생성
# Repository name: popo-backend
# Private 또는 Public 선택
```

### 2-2. 코드 푸시

```bash
# Git 초기화 (아직 안 했다면)
git init

# 필수: .env 파일이 커밋되지 않도록 확인
echo ".env" >> .gitignore
echo "logs/" >> .gitignore

# 현재 변경사항 커밋
git add .
git commit -m "feat: Initial commit for Render deployment

- Add Dockerfile
- Add render.yaml
- Add deployment documentation"

# 원격 저장소 연결
git remote add origin https://github.com/YOUR_USERNAME/popo-backend.git

# 푸시
git branch -M main
git push -u origin main
```

### ⚠️ 중요: API 키 확인

```bash
# .env 파일이 커밋되지 않았는지 확인
git log --all --full-history -- .env

# 만약 커밋되었다면 즉시 제거
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch .env' \
  --prune-empty --tag-name-filter cat -- --all
```

---

## 3. Render 계정 생성

1. **Render 웹사이트 접속**: https://render.com
2. **Sign Up** 클릭
3. **GitHub 계정으로 연동** (권장)
4. **Repository 접근 권한 허용**
   - All repositories 또는
   - Only select repositories (popo-backend 선택)

---

## 4. PostgreSQL 데이터베이스 생성

### 4-1. Database 생성

1. Render Dashboard → **New +** → **PostgreSQL**
2. 설정:
   ```
   Name: popo-db
   Database: popo
   User: popo_user
   Region: Singapore (가까운 지역 선택)
   PostgreSQL Version: 16 (최신 버전)
   Plan: Free (또는 필요시 유료)
   ```
3. **Create Database** 클릭

### 4-2. 연결 정보 확인

생성 후 Database 페이지에서:
```
Internal Database URL: postgresql://...
External Database URL: postgresql://...
PSQL Command: PGPASSWORD=... psql -h ...
```

**⚠️ 무료 플랜 제한**:
- 90일 후 자동 삭제됨
- 1GB 저장공간
- 정기적으로 백업 권장

---

## 5. pgvector Extension 설치

### 방법 1: Render Shell 사용 (권장)

1. **Database 페이지** → **Shell** 탭
2. 다음 SQL 실행:
   ```sql
   CREATE EXTENSION IF NOT EXISTS vector;
   ```
3. 확인:
   ```sql
   \dx vector
   ```

### 방법 2: 로컬에서 원격 접속

```bash
# External Database URL 사용
PGPASSWORD=your_password psql -h dpg-xxx.singapore-postgres.render.com -U popo_user popo

# pgvector 설치
CREATE EXTENSION IF NOT EXISTS vector;

# 확인
\dx vector
```

### 확인 결과

```
                                List of installed extensions
  Name  | Version |   Schema   |                Description
--------+---------+------------+--------------------------------------------
 vector | 0.7.0   | public     | vector data type and ivfflat and hnsw...
```

---

## 6. Web Service 배포

### 방법 1: Blueprint 사용 (자동, 권장)

1. **Dashboard** → **New +** → **Blueprint**
2. **GitHub Repository 선택**: popo-backend
3. **Blueprint Name**: popo-backend-blueprint
4. Render가 `render.yaml`을 자동으로 감지
5. **Apply** 클릭
6. 자동으로 Database + Web Service 생성

### 방법 2: Manual 설정

1. **Dashboard** → **New +** → **Web Service**
2. **GitHub Repository 선택**: popo-backend
3. 설정:
   ```
   Name: popo-backend
   Region: Singapore
   Branch: main
   Runtime: Docker
   Instance Type: Free
   ```
4. **Create Web Service** 클릭

---

## 7. 환경 변수 설정

### 7-1. Web Service 환경 변수

**Web Service 페이지** → **Environment** 탭

#### 자동 설정 (Blueprint 사용 시)
render.yaml에서 자동으로 설정됨:
- DB_HOST (데이터베이스 호스트)
- DB_PORT (포트, 기본 5432)
- DB_NAME (데이터베이스 이름)
- DB_USERNAME (사용자명)
- DB_PASSWORD (비밀번호)
→ 자동으로 PostgreSQL 연결됨

#### 수동 추가 필요

```bash
# OpenAI API Key (필수)
OPENAI_API_KEY=sk-proj-xxx...

# CORS (프론트엔드 배포 후)
CORS_ALLOWED_ORIGINS=https://your-frontend.onrender.com,https://www.your-frontend.com

# Spring Boot Settings (선택, 기본값 사용 가능)
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
LOGGING_LEVEL_ROOT=INFO
```

### 7-2. 환경 변수 형식 변환

Render의 DATABASE_URL은 다음 형식:
```
postgresql://user:password@host:port/database
```

Spring Boot는 다음 형식 필요:
```
jdbc:postgresql://host:port/database
```

**해결 방법 1**: render.yaml에서 이미 처리됨

**해결 방법 2**: application.properties 수정

```properties
# application.properties
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:popo}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:password}
```

그리고 Render 환경 변수:
```
DB_HOST=dpg-xxx.singapore-postgres.render.com
DB_PORT=5432
DB_NAME=popo
DB_USERNAME=popo_user
DB_PASSWORD=xxx
```

---

## 8. 배포 확인

### 8-1. 빌드 로그 확인

**Web Service** → **Logs** 탭

성공 시:
```
==> Starting service with 'java -XX:+UseContainerSupport...'
Started PopoBackendApplication in 15.234 seconds
```

실패 시:
```
ERROR: ...
==> Build failed
```

### 8-2. Health Check

배포된 URL 확인:
```
https://popo-backend.onrender.com
```

Health Check:
```bash
curl https://popo-backend.onrender.com/api/health
```

예상 응답:
```json
{
  "status": "OK",
  "message": "POPO Backend API is running",
  "timestamp": "2026-03-24T...",
  "service": "popo-backend",
  "version": "1.0.0"
}
```

### 8-3. 실제 채팅 테스트

```bash
curl -X POST https://popo-backend.onrender.com/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "test123",
    "message": "안녕하세요"
  }'
```

---

## 9. 초기 데이터 입력

### 9-1. Database Shell 접속

**Database 페이지** → **Shell** 탭

### 9-2. Profile 데이터 입력

```sql
INSERT INTO profile (
  name, occupation, experience, current_company, education,
  introduction, tech_stack, interests, email, github_url, blog_url,
  is_active, created_at, updated_at
) VALUES (
  '홍길동',
  '백엔드 개발자',
  '3년',
  'ABC 주식회사',
  '컴퓨터공학과 졸업',
  'AI와 백엔드 개발에 관심이 많은 개발자입니다.',
  '{"expert": ["Java", "Spring Boot"], "proficient": ["PostgreSQL", "Docker"], "familiar": ["React", "AWS"]}',
  'AI/ML, 백엔드 아키텍처, 클라우드',
  'your-email@example.com',
  'https://github.com/yourusername',
  'https://yourblog.com',
  true,
  NOW(),
  NOW()
);
```

### 9-3. Portfolio 데이터 입력

```sql
INSERT INTO portfolio_data (
  type, title, content, metadata, source, priority, is_public, created_at, updated_at
) VALUES
(
  'project',
  'POPO Backend',
  'Spring Boot 기반 AI 챗봇 API 개발. RAG 패턴과 pgvector를 활용한 벡터 검색 구현',
  '{"techStack": ["Spring Boot", "PostgreSQL", "pgvector", "OpenAI API"], "period": "2026.03", "role": "Full-stack Developer"}',
  'project-1',
  10,
  true,
  NOW(),
  NOW()
),
(
  'skill',
  'Spring Boot',
  'Spring Boot를 활용한 RESTful API 개발 및 JPA를 통한 데이터베이스 관리',
  '{"level": "proficient", "years": 3}',
  'skill-backend',
  8,
  true,
  NOW(),
  NOW()
);
```

### 9-4. 임베딩 생성 (선택)

데이터 입력 후 애플리케이션이 자동으로 임베딩을 생성하도록 API 호출:

```bash
# 관리자 API를 만들거나, 수동으로 데이터 업데이트
# 또는 애플리케이션 재시작 시 자동 생성 로직 추가
```

---

## 10. 문제 해결

### 문제 1: 빌드 실패

**증상**:
```
ERROR: Gradle build failed
```

**해결**:
```bash
# 로컬에서 빌드 테스트
./gradlew clean build -x test

# Docker 빌드 테스트
docker build -t popo-backend .
```

### 문제 2: Database 연결 실패

**증상**:
```
ERROR: Could not open JDBC Connection
```

**해결**:
1. 환경 변수 확인
   ```
   SPRING_DATASOURCE_URL
   SPRING_DATASOURCE_USERNAME
   SPRING_DATASOURCE_PASSWORD
   ```
2. Database URL 형식 확인 (jdbc:postgresql://... 인지)
3. Database가 생성되었는지 확인
4. IP Whitelist 설정 확인

### 문제 3: pgvector 에러

**증상**:
```
ERROR: type "vector" does not exist
```

**해결**:
```sql
-- Database Shell에서 실행
CREATE EXTENSION IF NOT EXISTS vector;

-- 확인
\dx vector
```

### 문제 4: OpenAI API 에러

**증상**:
```
ERROR: OPENAI_API_KEY not found
```

**해결**:
1. Render Dashboard → Environment
2. OPENAI_API_KEY 추가
3. Web Service 재시작

### 문제 5: CORS 에러

**증상**:
```
Access to fetch ... from origin ... has been blocked by CORS
```

**해결**:
```bash
# Render Environment 변수에 추가
CORS_ALLOWED_ORIGINS=https://your-frontend.onrender.com

# 또는 application.properties 수정
```

### 문제 6: 무료 플랜 Sleep 모드

**증상**:
첫 요청이 매우 느림 (15-30초)

**원인**:
Render 무료 플랜은 15분 비활성 시 sleep 모드

**해결**:
1. 유료 플랜 사용 ($7/month)
2. 또는 주기적으로 Health Check 호출 (Cron Job)
   ```bash
   # UptimeRobot, Pingdom 등 무료 모니터링 서비스 사용
   ```

---

## 📊 배포 후 체크리스트

### 즉시 확인

- [ ] Health Check 응답 확인
- [ ] 채팅 API 테스트
- [ ] 에러 로그 확인
- [ ] Database 연결 확인
- [ ] pgvector extension 작동 확인

### 1주일 내 확인

- [ ] OpenAI API 비용 모니터링
- [ ] 응답 시간 측정
- [ ] 에러 발생률 확인
- [ ] Database 저장 공간 확인

### 정기적으로 확인

- [ ] 무료 Database 90일 만료 전 백업
- [ ] API 키 로테이션
- [ ] 로그 분석
- [ ] 사용자 피드백 수집

---

## 🚀 다음 단계

### 1. 프론트엔드 배포
- Render Static Site 또는 Vercel 사용
- CORS 설정 업데이트

### 2. 도메인 연결
- Render Dashboard → Settings → Custom Domain
- DNS 설정 (CNAME 레코드)

### 3. HTTPS 설정
- Render는 자동으로 Let's Encrypt SSL 제공
- 별도 설정 불필요

### 4. CI/CD 자동화
- GitHub Actions 설정
- main 브랜치 push 시 자동 배포 (render.yaml의 autoDeploy: true)

### 5. 모니터링 설정
- Render 대시보드에서 기본 메트릭 확인
- 필요시 Sentry, Datadog 등 통합

---

## 💰 비용 예상

### Render 무료 플랜
```
Web Service: Free
- 750 시간/월
- 512MB RAM
- Sleep after 15 min inactivity

PostgreSQL: Free
- 1GB 저장공간
- 90일 후 삭제
- 외부 접속 가능
```

### 유료 플랜 (권장)
```
Web Service: $7/month (Starter)
- Always-on
- 512MB RAM
- No sleep

PostgreSQL: $7/month (Starter)
- 1GB 저장공간
- 영구 사용
- 자동 백업
```

### OpenAI API
```
예상 비용: $5-10/month
- GPT-4o-mini: $0.15 / 1M tokens
- Embeddings: $0.02 / 1M tokens
```

**총 예상 비용**: $19-27/month (유료 플랜 사용 시)

---

## 📚 참고 자료

- [Render Documentation](https://render.com/docs)
- [Render PostgreSQL Guide](https://render.com/docs/databases)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker)

---

## 🆘 지원

문제가 발생하면:
1. Render 로그 확인
2. GitHub Issues 검색
3. Render Community Forum
4. Stack Overflow

---

*작성일: 2026-03-24*
*최종 수정: 2026-03-24*

**축하합니다! 이제 프로덕션 환경에서 운영 중입니다! 🎉**

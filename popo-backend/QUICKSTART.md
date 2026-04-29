# 🚀 POPO Backend - Quick Start Guide

## 빠른 시작 (3가지 방법)

### 방법 1: Docker Compose (가장 쉬움, 권장)

```bash
# 1. 환경 변수 설정
cp .env.example .env
# .env 파일에서 OPENAI_API_KEY 설정

# 2. Docker Compose 실행
docker-compose up -d

# 3. 확인
curl http://localhost:8080/api/health
```

**완료!** 🎉

---

### 방법 2: 로컬 PostgreSQL 사용

#### 필수 조건
- PostgreSQL 17+ 설치
- pgvector extension 설치
- Java 17 설치

```bash
# 1. PostgreSQL + pgvector 설치
sudo apt-get install postgresql-18-pgvector

# 2. Database 및 extension 생성
sudo -u postgres psql
CREATE DATABASE popo;
\c popo
CREATE EXTENSION vector;
\q

# 3. 환경 변수 설정
cp .env.example .env
# .env 파일 수정

# 4. 애플리케이션 실행
./gradlew bootRun

# 5. 확인
curl http://localhost:8080/api/health
```

---

### 방법 3: Render 배포 (프로덕션)

자세한 내용은 [DEPLOYMENT.md](DEPLOYMENT.md) 참조

**요약**:
```bash
# 1. GitHub에 푸시
git add .
git commit -m "feat: Ready for deployment"
git push origin main

# 2. Render 대시보드
- New Blueprint
- GitHub 저장소 선택
- render.yaml 자동 감지
- Apply

# 3. 환경 변수 설정
OPENAI_API_KEY=sk-...

# 4. pgvector 설치
Database Shell에서:
CREATE EXTENSION IF NOT EXISTS vector;
```

---

## 📝 초기 설정

### 1. OpenAI API 키 발급

1. https://platform.openai.com/api-keys
2. **Create new secret key**
3. 키 복사 (한 번만 표시됨!)
4. `.env` 파일에 저장

### 2. 환경 변수 설정

```bash
# .env
OPENAI_API_KEY=sk-proj-xxx...
DB_USERNAME=postgres
DB_PASSWORD=1234
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### 3. 초기 데이터 입력

```sql
-- Profile
INSERT INTO profile (name, occupation, is_active, created_at, updated_at)
VALUES ('홍길동', '백엔드 개발자', true, NOW(), NOW());

-- Portfolio Data
INSERT INTO portfolio_data (type, title, content, priority, is_public, created_at, updated_at)
VALUES ('project', 'POPO Backend', 'AI 챗봇 백엔드', 10, true, NOW(), NOW());
```

---

## 🧪 테스트

### Health Check
```bash
curl http://localhost:8080/api/health
```

### 채팅 테스트
```bash
curl -X POST http://localhost:8080/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "test123",
    "message": "안녕하세요"
  }'
```

---

## 🔧 개발 도구

### 로그 확인
```bash
# Docker Compose
docker-compose logs -f backend

# 로컬 실행
tail -f logs/popo-backend.log
```

### Database 접속
```bash
# Docker Compose
docker exec -it popo-postgres psql -U postgres -d popo

# 로컬
psql -U postgres -d popo
```

### 재시작
```bash
# Docker Compose
docker-compose restart backend

# 로컬
./gradlew bootRun
```

---

## 📚 다음 단계

1. ✅ 로컬 실행 완료
2. 📝 [기술 문서](TECHNICAL_DOCUMENTATION.md) 읽기
3. 🚀 [배포 가이드](DEPLOYMENT.md) 따라하기
4. 🎨 프론트엔드 연동

---

## 🆘 문제 해결

### Q: "pgvector extension not found"
```bash
# pgvector 설치
sudo apt-get install postgresql-18-pgvector

# DB에서 extension 생성
CREATE EXTENSION IF NOT EXISTS vector;
```

### Q: "OpenAI API error"
```bash
# API 키 확인
echo $OPENAI_API_KEY

# .env 파일 확인
cat .env
```

### Q: "Port 8080 already in use"
```bash
# 사용 중인 프로세스 종료
lsof -ti:8080 | xargs kill -9

# 또는 포트 변경
export SERVER_PORT=8081
./gradlew bootRun
```

---

**시작 준비 완료! 🎉**

더 자세한 정보는:
- [기술 문서](TECHNICAL_DOCUMENTATION.md)
- [배포 가이드](DEPLOYMENT.md)

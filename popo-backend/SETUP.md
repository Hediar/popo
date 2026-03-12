# OpenAI 설정 가이드

## 1. OpenAI API 키 발급

1. [OpenAI Platform](https://platform.openai.com/) 접속
2. 로그인 후 [API Keys](https://platform.openai.com/api-keys) 페이지로 이동
3. "Create new secret key" 클릭
4. 생성된 키 복사 (한 번만 표시되므로 안전하게 보관!)

## 2. 환경 변수 설정

### 방법 1: .env 파일 사용 (추천)

```bash
# .env.example을 복사하여 .env 파일 생성
cp .env.example .env

# .env 파일을 열어서 실제 값으로 수정
# OPENAI_API_KEY=sk-your-actual-api-key-here
```

### 방법 2: 시스템 환경 변수

**Linux/Mac:**
```bash
export OPENAI_API_KEY="sk-your-api-key-here"
export DB_USERNAME="postgres"
export DB_PASSWORD="your-password"
```

**Windows (PowerShell):**
```powershell
$env:OPENAI_API_KEY="sk-your-api-key-here"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your-password"
```

### 방법 3: IntelliJ IDEA 설정

1. Run → Edit Configurations
2. Environment variables 섹션에서 추가:
   ```
   OPENAI_API_KEY=sk-your-api-key-here
   DB_USERNAME=postgres
   DB_PASSWORD=your-password
   ```

## 3. 설정 확인

현재 application.properties 설정:

```properties
# OpenAI 설정
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4o-mini
spring.ai.openai.chat.options.temperature=0.7
spring.ai.openai.chat.options.max-tokens=1000
```

## 4. 테스트

애플리케이션을 실행하고 다음 API를 호출해보세요:

```bash
curl -X POST http://localhost:8080/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{
    "message": "안녕하세요, 자기소개 부탁드립니다."
  }'
```

## 5. 비용 관리

- gpt-4o-mini는 비용 효율적인 모델입니다
- 1M 토큰당 약 $0.15 (입력) / $0.60 (출력)
- max-tokens을 1000으로 제한하여 비용 관리

## 6. 문제 해결

### API 키 오류
```
Error: Incorrect API key provided
```
→ OPENAI_API_KEY 환경 변수가 올바르게 설정되었는지 확인

### Rate Limit 오류
```
Error: Rate limit exceeded
```
→ OpenAI 계정의 사용량 한도 확인 또는 결제 수단 등록

### Connection 오류
```
Error: Connection timeout
```
→ 인터넷 연결 확인 또는 프록시 설정 확인

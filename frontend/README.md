# POPO-AI Frontend

> Next.js 14+ 기반 AI 챗봇 프론트엔드

## 기술 스택

- **Next.js 14+** (App Router)
- **TypeScript**
- **TailwindCSS**
- **Biome** (Linter/Formatter)
- **Material Symbols** (아이콘)
- **EventSource API** (SSE 스트리밍)

## 시작하기

### 1. 의존성 설치

```bash
npm install
```

### 2. 환경 변수 설정

`.env.local` 파일을 생성하고 다음 내용을 추가하세요:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### 3. 개발 서버 실행

```bash
npm run dev
```

브라우저에서 [http://localhost:3000](http://localhost:3000)을 열어 확인하세요.

## 사용 가능한 스크립트

```bash
# 개발 서버 실행
npm run dev

# 프로덕션 빌드
npm run build

# 프로덕션 서버 실행
npm start

# Biome 린트 검사
npm run lint

# 코드 포맷팅
npm run format

# 린트 + 포맷팅 (자동 수정)
npm run check

# CI용 검사 (수정 없이 검사만)
npm run biome:ci
```

## 프로젝트 구조

```
frontend/
├── app/
│   ├── page.tsx              # 메인 페이지 (채팅)
│   ├── layout.tsx            # 루트 레이아웃
│   └── globals.css           # 글로벌 스타일
├── components/
│   ├── ChatInterface.tsx     # 채팅 메인 컴포넌트 (헤더 포함)
│   └── MessageList.tsx       # 메시지 리스트
├── lib/
│   ├── types.ts              # TypeScript 타입 정의
│   └── api.ts                # API 클라이언트
├── public/                   # 정적 파일
├── .env.local               # 환경 변수
├── biome.json               # Biome 설정
├── package.json
└── tsconfig.json
```

## 주요 기능

### 1. 채팅 인터페이스
- 실시간 SSE 스트리밍
- 메시지 히스토리 관리
- Enter 키로 전송, Shift+Enter로 줄바꿈

### 2. 다크 테마 UI
- 모던한 다크 테마 적용
- Material Symbols 아이콘
- Inter 폰트 사용

### 3. 헤더 네비게이션
- 앱 브랜딩 (POPO)
- 모델 버전 표시
- 알림/히스토리/메뉴 아이콘

## 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `NEXT_PUBLIC_API_URL` | 백엔드 API URL | `http://localhost:8080` |

## 백엔드 연동

백엔드 서버가 실행 중이어야 채팅 기능이 작동합니다:

```bash
# 백엔드 디렉토리에서
./gradlew bootRun
```

## 배포

### Vercel 배포

```bash
# Vercel CLI 설치
npm i -g vercel

# 배포
vercel
```

### 환경 변수 설정
Vercel 대시보드에서 다음 환경 변수를 설정하세요:
- `NEXT_PUBLIC_API_URL`: 프로덕션 백엔드 URL

## 개발 가이드

### 코드 스타일
- Biome을 사용하여 코드 포맷팅 및 린팅
- 커밋 전 `npm run check` 실행 권장

### 컴포넌트 작성
- 모든 클라이언트 컴포넌트는 `'use client'` 지시어 사용
- TypeScript 타입 정의는 `lib/types.ts`에 추가
- API 호출 함수는 `lib/api.ts`에 추가

## 문제 해결

### 포트 충돌
기본 포트(3000)가 사용 중이면 자동으로 다른 포트(3001 등)를 사용합니다.

### CSS 파싱 에러
Tailwind CSS 관련 에러는 `app/globals.css`를 확인하세요.

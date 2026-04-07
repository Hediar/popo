# Streaming 응답 적용 (SSE)

> 작성일: 2026-04-07

## 배경

채팅 응답 생성이 느린 문제가 있었음. 분석 결과 주요 병목:

1. **OpenAI API 호출 2회 순차 실행** (임베딩 + GPT 응답)
2. **대화 내역 최대 100건을 프롬프트에 포함** (토큰 증가 -> 응답 지연)
3. **벡터 검색에서 다단계 DB 쿼리** (최악 4~5회)
4. **ProfileService 매 요청마다 DB 조회** (캐싱 없음)

이 중 가장 체감 효과가 큰 **Streaming(SSE)** 방식을 우선 적용함.

## 변경 내용

### Backend

#### 1. `build.gradle`
- `spring-boot-starter-webflux` 의존성 추가
  - Spring AI의 `.stream()`이 반환하는 `Flux<String>`을 사용하기 위해 필요
  - `Flux`는 **Project Reactor** 라이브러리의 클래스로, 데이터를 0~N개 비동기로 흘려보내는 스트림
  - 예: GPT가 토큰을 하나씩 생성할 때마다 `Flux`가 그 토큰을 즉시 다음 단계로 전달

#### 2. `OpenAIService.java`
- `generateResponseStream()` 메서드 추가
  - `.call()` 대신 `.stream()` 사용, `Flux<String>` 반환
- 에러 분류 로직을 `classifyException()`으로 추출 (중복 제거)
- 기존 `generateResponse()` 동기 메서드는 유지 (호환성)

#### 3. `ChatService.java`
- `processMessageStream()` 추가
  - 세션 조회/생성, 대화 내역 추출, 벡터 검색까지 동기로 처리
  - AI 응답만 `Flux<String>`으로 반환
- `saveStreamedMessage()` 추가
  - streaming 완료 후 사용자 메시지 + AI 응답을 세션에 저장
- `StreamContext` record 추가
  - sessionId, responseStream, session, existingMessages, userMessage를 묶어서 컨트롤러에 전달

#### 4. `ChatController.java`
- `POST /api/chat/stream` 엔드포인트 추가 (SSE)
  - `SseEmitter` 사용 (타임아웃 2분)
  - `ExecutorService`로 비동기 처리
- SSE 이벤트 순서:
  1. `event: sessionId` - 세션 ID 전달
  2. `event: token` - 토큰 단위 스트리밍 (반복)
  3. `event: done` - 완료 신호
  4. `event: error` - 에러 발생 시
- 기존 `POST /api/chat/message` 동기 엔드포인트 유지

### Frontend

#### 1. `lib/api.ts`
- `createChatStream()` 함수 변경
  - `EventSource`(GET) -> `fetch` + `ReadableStream`(POST) 방식으로 변경
  - `sessionId` 파라미터 추가하여 세션 기반 대화 지원
  - `onSessionId` 콜백 추가
  - 반환 타입: `EventSource` -> `AbortController`
  - SSE 라인 단위 파싱 + `flushEvent` 방식으로 변경 (linter 적용됨)

#### 2. `components/ChatInterface.tsx`
- `eventSourceRef` -> `abortRef` (`AbortController` 기반)
- `createChatStream` 호출부에 `sessionId`, `onSessionId` 콜백 추가
- 언마운트 시 `abort()` 호출로 정리

## 사용 방법

프론트엔드 환경변수로 streaming 모드를 활성화:

```
NEXT_PUBLIC_CHAT_USE_STREAM=true
```

- `true`: streaming 엔드포인트 (`/api/chat/stream`) 사용
- 미설정 또는 `false`: 기존 동기 엔드포인트 (`/api/chat/message`) 사용

## 추후 개선 가능 사항

- 대화 내역 제한: 100건 -> 10~15건으로 줄여 토큰 절약
- ProfileService 캐싱: `@Cacheable` 또는 애플리케이션 시작 시 로딩
- 벡터 검색 쿼리 최적화: 불필요한 보충 쿼리 줄이기
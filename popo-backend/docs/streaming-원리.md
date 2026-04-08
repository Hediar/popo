# Streaming 원리 (SSE)

> 이 프로젝트에 적용된 스트리밍 방식을 설명하는 문서

## 일반 호출 vs Streaming

### 일반 호출 (기존 방식)

```
브라우저 → [POST /api/chat/message] → 서버 → OpenAI API 호출
                                                  ↓
                                          GPT가 전체 답변 생성 (5~10초)
                                                  ↓
                                          완성된 답변을 한 번에 반환
                                                  ↓
브라우저 ← ──────── JSON 응답 ──────── ← 서버
```

사용자는 GPT가 답변을 **전부 만들 때까지** 로딩 화면을 봐야 한다.

### Streaming (현재 방식)

```
브라우저 → [POST /api/chat/stream] → 서버 → OpenAI API 호출 (stream=true)
                                                  ↓
                                          GPT가 토큰 생성 시작
                                                  ↓
브라우저 ← "저는" ─────────────────── ← 서버 ← OpenAI (토큰 1)
브라우저 ← "Spring을" ────────────── ← 서버 ← OpenAI (토큰 2)
브라우저 ← "주로" ───────────────── ← 서버 ← OpenAI (토큰 3)
브라우저 ← "사용했고요," ──────────── ← 서버 ← OpenAI (토큰 4)
  ...
브라우저 ← [DONE] ────────────────── ← 서버 ← OpenAI (완료)
```

GPT가 토큰(단어 조각)을 생성하는 **즉시** 사용자에게 전달된다.
총 소요 시간은 비슷하지만, 첫 글자가 1~2초 만에 나타나므로 체감 속도가 훨씬 빠르다.

## 핵심 기술 3가지

### 1. SSE (Server-Sent Events)

HTTP 기반의 단방향 스트리밍 프로토콜이다.
일반 HTTP 응답은 한 번에 끝나지만, SSE는 연결을 유지한 채 서버가 데이터를 계속 보낼 수 있다.

```
일반 HTTP:  요청 → 응답(완료) → 연결 종료
SSE:        요청 → 응답 시작 → 데이터1 → 데이터2 → ... → 연결 종료
```

SSE 메시지 형식:
```
event: token
data: 안녕하세요

event: token
data: 저는

event: done
data: [DONE]
```

- `event:` — 이벤트 종류 (token, sessionId, done, error)
- `data:` — 실제 데이터
- 빈 줄(`\n\n`) — 하나의 이벤트 종료를 의미

### 2. Flux (Project Reactor)

Reactor는 Java의 리액티브 프로그래밍 라이브러리이고, `Flux`는 그 핵심 클래스다.

```java
// 일반 방식 - 전체 결과를 기다림
String result = chatClient.prompt().user(prompt).call().content();

// Flux 방식 - 데이터가 생기는 대로 하나씩 흘려보냄
Flux<String> stream = chatClient.prompt().user(prompt).stream().content();
```

비유하면:
- **일반 방식** = 식당에서 모든 요리가 다 되면 한꺼번에 서빙
- **Flux** = 요리가 하나씩 완성될 때마다 바로 서빙

`Flux<String>`은 "String이 0~N개 비동기로 흘러오는 스트림"이라는 뜻이다.
Spring AI의 `.stream()`이 OpenAI API에서 토큰을 받을 때마다 `Flux`로 하나씩 넘겨준다.

### 3. SseEmitter (Spring MVC)

Spring MVC에서 SSE를 보내기 위한 클래스다.
Flux에서 토큰이 올 때마다 SseEmitter로 브라우저에 전송한다.

```java
SseEmitter emitter = new SseEmitter(120_000L); // 2분 타임아웃

flux.doOnNext(token -> {
    emitter.send(SseEmitter.event()
        .name("token")      // event: token
        .data(token));       // data: {토큰 내용}
}).doOnComplete(() -> {
    emitter.send(SseEmitter.event()
        .name("done")
        .data("[DONE]"));
    emitter.complete();      // 연결 종료
}).subscribe();
```

## 이 프로젝트에서의 데이터 흐름

```
[브라우저]                    [Spring 서버]                    [OpenAI API]
    │                              │                              │
    │── POST /api/chat/stream ──→ │                              │
    │                              │── 벡터 검색 (동기) ──────→ DB │
    │                              │← 검색 결과 ─────────────── DB │
    │                              │                              │
    │                              │── stream 요청 ────────────→ │
    │                              │                              │
    │← event: sessionId           │                              │
    │   data: abc123              │                              │
    │                              │← 토큰 "저는"                │
    │← event: token               │                              │
    │   data: 저는                │                              │
    │                              │← 토큰 "Spring을"            │
    │← event: token               │                              │
    │   data: Spring을            │                              │
    │                              │          ...                 │
    │                              │← [완료]                     │
    │                              │── 대화 내역 DB 저장          │
    │← event: done                │                              │
    │   data: [DONE]              │                              │
    │                              │                              │
    │── 연결 종료 ──────────────── │                              │
```

핵심 포인트:
- 벡터 검색까지는 **동기**로 처리 (결과가 있어야 GPT에게 보낼 수 있으므로)
- GPT 응답만 **비동기 스트리밍**으로 처리 (여기가 가장 오래 걸리므로)
- 스트리밍이 **완료된 후** 전체 응답을 DB에 저장

## 프론트엔드에서 SSE 수신

브라우저의 `fetch` API로 SSE를 수신한다.
POST 요청이므로 `EventSource`(GET 전용) 대신 `ReadableStream`을 사용한다.

```typescript
const response = await fetch('/api/chat/stream', {
    method: 'POST',
    body: JSON.stringify({ message, sessionId }),
});

const reader = response.body.getReader();

while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    // 받은 chunk를 SSE 형식으로 파싱
    // "event: token\ndata: 안녕\n\n" -> 화면에 "안녕" 추가
}
```

`reader.read()`가 데이터가 올 때마다 깨어나서 처리하므로,
토큰이 도착하는 즉시 화면에 표시된다.
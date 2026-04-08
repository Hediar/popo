# Spring 개념 정리 & 면접 대비

> 이 프로젝트(POPO AI)에서 사용된 Spring 개념을 중심으로 정리

---

## 1. 프로젝트 파일별 역할

### Controller (웹 요청 처리)

| 파일 | 역할 |
|------|------|
| `ChatController.java` | 채팅 API 엔드포인트. 동기(`/message`)와 SSE 스트리밍(`/stream`) 두 가지 방식 제공 |
| `HealthCheckController.java` | 서버 상태 확인용 헬스체크 엔드포인트 (`/health`, `/api/health`) |

### Service (비즈니스 로직)

| 파일 | 역할 |
|------|------|
| `ChatService.java` | 채팅의 전체 흐름을 조율. 세션 관리 → 대화 내역 조회 → 벡터 검색 → AI 응답 생성 → DB 저장 |
| `OpenAIService.java` | OpenAI GPT API 호출 담당. 시스템 프롬프트 구성, RAG 프롬프트 조립, 동기/스트리밍 응답 생성 |
| `EmbeddingService.java` | 텍스트를 벡터(숫자 배열)로 변환. OpenAI text-embedding-3-small 모델 사용 (1536차원) |
| `VectorSearchService.java` | RAG 패턴의 검색 담당. 키워드 매칭 + 벡터 유사도 검색으로 관련 포트폴리오 데이터를 찾음 |
| `ProfileService.java` | 프로필 테이블에서 활성 프로필을 조회하여 AI 프롬프트용 텍스트로 변환 |

### Repository (데이터 접근)

| 파일 | 역할 |
|------|------|
| `ChatSessionRepository.java` | 채팅 세션 CRUD. 세션 ID로 조회, JSONB 메시지 직접 UPDATE 등 |
| `PortfolioDataRepository.java` | 포트폴리오 데이터 접근. pgvector 유사도 검색, 키워드 검색, 타입별 필터링 쿼리 |
| `ProfileRepository.java` | 프로필 데이터 조회. 활성화된 프로필 찾기 |
| `ResumeSectionRepository.java` | 이력서 섹션 데이터 접근 |

### Entity (DB 테이블 매핑)

| 파일 | 역할 |
|------|------|
| `ChatSession.java` | 채팅 세션 엔티티. sessionId, clientIp, 메시지 목록(JSONB), 상태값 포함 |
| `PortfolioData.java` | 포트폴리오 데이터 엔티티. title, content, metadata, embedding 벡터(1536차원) 포함 |
| `Profile.java` | 프로필 엔티티. 이름, 직업, 경력, 기술스택(JSON), 자격증(JSON) 등 |
| `ResumeSection.java` | 이력서 섹션 엔티티 |
| `SessionStatus.java` | 세션 상태 enum (ACTIVE, COMPLETED) |

### DTO (데이터 전송 객체)

| 파일 | 역할 |
|------|------|
| `ChatRequest.java` | 채팅 요청 DTO — sessionId, message |
| `ChatResponse.java` | 채팅 응답 DTO — sessionId, AI 응답 message |
| `MessageDto.java` | 개별 메시지 DTO — role, content, timestamp |
| `SearchResult.java` | 벡터 검색 결과 DTO — content, similarity, source |
| `ErrorResponse.java` | 에러 응답 DTO — errorCode, message, status, timestamp |

### Config (설정)

| 파일 | 역할 |
|------|------|
| `WebConfig.java` | CORS 설정. 허용 origin, 메서드, 헤더 등을 정의 |
| `RateLimitFilter.java` | IP 기반 요청 제한 필터. Bucket4j로 분당 100건 제한 |

### Exception (예외 처리)

| 파일 | 역할 |
|------|------|
| `OpenAIException.java` | OpenAI API 관련 커스텀 예외. 401/429/500 등 상황별 팩토리 메서드 제공 |
| `GlobalExceptionHandler.java` | 전역 예외 처리. `@RestControllerAdvice`로 모든 컨트롤러 예외를 잡아서 통일된 에러 응답 반환 |

### Util (유틸리티)

| 파일 | 역할 |
|------|------|
| `NetworkUtils.java` | 클라이언트 IP 추출 (X-Forwarded-For 헤더 처리) |
| `SessionIdGenerator.java` | 세션 ID 생성 |
| `KeywordExtractor.java` | 키워드 추출 유틸 (스텁) |
| `MessagesConverter.java` | JPA 컨버터 — `List<MessageDto>` <-> JSON 문자열 변환 |
| `VectorConverter.java` | JPA 컨버터 — `float[]` <-> PostgreSQL vector 타입 변환 |
| `EmbeddingRegenerationRunner.java` | 애플리케이션 시작 시 임베딩 데이터 재생성 |

---

## 2. Spring 핵심 개념

### IoC (Inversion of Control) / DI (Dependency Injection)

**개념**: 객체 생성과 의존성 관리를 개발자가 아닌 Spring 컨테이너가 담당하는 것.

```java
// 이 프로젝트 예시: ChatService.java
@Service
public class ChatService {
    @Autowired
    private VectorSearchService vectorSearchService;  // Spring이 알아서 주입

    @Autowired
    private OpenAIService openAIService;              // 직접 new 하지 않음
}
```

- `@Autowired`: "이 필드에 맞는 Bean을 찾아서 넣어줘"라는 뜻
- Spring 컨테이너가 시작할 때 `@Service`, `@Component`, `@Repository` 등이 붙은 클래스를 **Bean**으로 등록하고, 필요한 곳에 주입

### Bean과 Component Scan

**Bean**: Spring 컨테이너가 관리하는 객체. 기본적으로 **싱글톤** (애플리케이션에 1개만 존재).

```
@SpringBootApplication    ← 이 패키지 하위를 자동 스캔
    ├── @RestController   ← Bean 등록 (컨트롤러)
    ├── @Service          ← Bean 등록 (서비스)
    ├── @Repository       ← Bean 등록 (데이터 접근)
    ├── @Component        ← Bean 등록 (범용)
    └── @Configuration    ← Bean 등록 (설정)
```

### 계층 구조 (Layered Architecture)

```
[클라이언트] → Controller → Service → Repository → [DB]
                 ↓             ↓           ↓
              요청/응답     비즈니스      데이터
              처리          로직         접근
```

이 프로젝트에서:
```
ChatController → ChatService → VectorSearchService → PortfolioDataRepository → PostgreSQL
                             → OpenAIService        → OpenAI API
                             → ProfileService       → ProfileRepository → PostgreSQL
```

### @Transactional (트랜잭션 관리)

**개념**: 메서드 실행 중 에러가 나면 DB 변경을 모두 취소(롤백)하는 것.

```java
// 이 프로젝트 예시: ChatService.java
@Transactional
public String[] processMessage(String sessionId, String userMessage, String clientIp) {
    // 1. 세션 조회/생성
    // 2. 벡터 검색
    // 3. AI 응답 생성
    // 4. DB 저장 ← 여기서 에러나면 1~3의 DB 변경도 롤백
}
```

### Spring Data JPA

**개념**: 인터페이스만 정의하면 SQL 없이 DB CRUD가 가능한 것.

```java
// 이 프로젝트 예시: ChatSessionRepository.java
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findBySessionId(String sessionId);
    // ↑ 메서드 이름만으로 SELECT * FROM chat_sessions WHERE session_id = ? 생성
}
```

- `JpaRepository<ChatSession, Long>`: ChatSession 엔티티를 Long 타입 PK로 관리
- `save()`, `findById()`, `findAll()`, `delete()` 등 기본 CRUD 자동 제공
- `@Query`로 복잡한 네이티브 쿼리도 작성 가능

### Entity & JPA 어노테이션

```java
// 이 프로젝트 예시: ChatSession.java
@Entity                                    // 이 클래스는 DB 테이블과 매핑
@Table(name = "chat_sessions")             // 테이블 이름 지정
public class ChatSession {
    @Id                                    // PK
    @GeneratedValue(strategy = IDENTITY)   // DB가 자동 증가 (PostgreSQL SERIAL)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sessionId;

    @Enumerated(EnumType.STRING)           // enum을 문자열로 저장
    private SessionStatus status;

    @PrePersist                            // INSERT 직전에 자동 실행
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

### @RestControllerAdvice (전역 예외 처리)

**개념**: 모든 컨트롤러에서 발생하는 예외를 한 곳에서 처리.

```java
// 이 프로젝트 예시: GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OpenAIException.class)    // OpenAI 관련 에러만 잡음
    public ResponseEntity<ErrorResponse> handleOpenAI(OpenAIException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .body(new ErrorResponse(e.getErrorCode(), e.getMessage(), ...));
    }

    @ExceptionHandler(Exception.class)          // 나머지 모든 에러
    public ResponseEntity<ErrorResponse> handleAll(Exception e) { ... }
}
```

### Filter (필터)

**개념**: 컨트롤러에 도달하기 전에 요청을 가로채서 처리.

```
클라이언트 → [RateLimitFilter] → Controller → Service → ...
               ↓ (분당 100건 초과 시)
            429 Too Many Requests 반환
```

```java
// 이 프로젝트 예시: RateLimitFilter.java
@Component
public class RateLimitFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ..., FilterChain chain) {
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);  // 통과
        } else {
            response.setStatus(429);            // 차단
        }
    }
}
```

### Spring AI

**개념**: Spring에서 AI 모델(OpenAI 등)을 쉽게 통합하기 위한 프레임워크.

```java
// 이 프로젝트 예시: OpenAIService.java
ChatClient chatClient = chatClientBuilder
    .defaultSystem("시스템 프롬프트")
    .build();

// 동기 호출
String response = chatClient.prompt().user("질문").call().content();

// 스트리밍 호출
Flux<String> stream = chatClient.prompt().user("질문").stream().content();
```

### CORS (Cross-Origin Resource Sharing)

**개념**: 다른 도메인에서 오는 요청을 허용하는 설정. 프론트엔드와 백엔드가 다른 포트/도메인일 때 필요.

```java
// 이 프로젝트 예시: WebConfig.java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns("http://localhost:*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH");
    }
}
```

### application.properties (외부 설정)

**개념**: 코드 변경 없이 환경별(개발/운영) 설정을 바꿀 수 있게 하는 것.

```properties
# 환경변수로 덮어쓰기 가능 (배포 시 유용)
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/popo}
spring.ai.openai.api-key=${OPENAI_API_KEY}
server.port=${PORT:8080}
```

- `${ENV_VAR:기본값}` 형식: 환경변수가 있으면 사용, 없으면 기본값 사용

---

## 3. 면접 예상 질문 & 답변

### Spring 기본

**Q1. IoC/DI란 무엇이고, 왜 사용하나요?**

> IoC(Inversion of Control)는 객체 생성과 생명주기 관리를 개발자가 아닌 프레임워크(Spring 컨테이너)가 담당하는 것입니다. DI(Dependency Injection)는 IoC를 구현하는 방법으로, 필요한 의존 객체를 외부에서 주입받는 것입니다.
>
> 사용 이유: 클래스 간 결합도를 낮춰서 테스트와 유지보수가 쉬워집니다. 예를 들어 ChatService가 OpenAIService를 직접 `new`로 생성하면 교체가 어렵지만, DI를 쓰면 테스트 시 Mock 객체로 쉽게 교체할 수 있습니다.
>
> **쉬운 비유**: 카페에서 바리스타가 직접 원두를 재배하고, 우유 농장을 운영하고, 컵 공장을 돌리면 커피 한 잔 만들기가 너무 복잡합니다. 대신 원두 납품업체, 우유 납품업체가 알아서 가져다주면(=주입) 바리스타는 커피 만들기에만 집중할 수 있습니다. Spring 컨테이너가 이 "납품업체 관리자" 역할을 합니다.

**Q2. @Component, @Service, @Repository, @Controller의 차이는?**

> 기능적으로는 모두 Bean을 등록하는 동일한 역할이지만, 역할을 명시하는 의미가 다릅니다.
> - `@Component`: 범용 컴포넌트
> - `@Service`: 비즈니스 로직 계층
> - `@Repository`: 데이터 접근 계층 (추가로 DB 예외를 Spring 예외로 변환해줌)
> - `@Controller`/`@RestController`: 웹 요청 처리 계층
>
> 계층별로 구분하면 코드의 의도가 명확해지고, AOP 등에서 계층별 로직 적용이 가능합니다.
>
> **쉬운 비유**: 병원에서 접수처(Controller), 의사(Service), 약국(Repository) 직원이 모두 "직원"(@Component)이지만, 역할 이름표를 달아두면 누가 어떤 일을 하는지 바로 알 수 있는 것과 같습니다.

**Q3. Bean의 스코프(Scope)에 대해 설명해주세요.**

> Spring Bean의 기본 스코프는 **싱글톤**으로, 애플리케이션에 인스턴스가 1개만 존재합니다.
> - `singleton` (기본): 컨테이너에 1개
> - `prototype`: 요청할 때마다 새로 생성
> - `request`: HTTP 요청마다 1개
> - `session`: HTTP 세션마다 1개
>
> 이 프로젝트의 Service, Repository 등은 모두 싱글톤으로 동작합니다.
>
> **쉬운 비유**: 회사에 프린터가 1대(싱글톤)면 모든 직원이 공유합니다. 만약 각 직원에게 개인 메모장을 지급(prototype)한다면, 요청할 때마다 새 메모장을 줍니다. 서비스 객체는 프린터처럼 공유해도 되지만, 사용자별 상태를 가진 객체는 매번 새로 만들어야 합니다.

**Q4. @Autowired의 동작 원리는?**

> Spring 컨테이너가 해당 타입의 Bean을 찾아서 주입합니다. 같은 타입의 Bean이 여러 개면 `@Qualifier`로 지정하거나, `@Primary`가 붙은 Bean이 우선됩니다.
>
> 주입 방식은 3가지가 있는데:
> - **필드 주입** (`@Autowired private Service service`) — 간단하지만 테스트 시 불편
> - **생성자 주입** (권장) — 불변성 보장, 테스트 용이
> - **Setter 주입** — 선택적 의존성에 사용
>
> **쉬운 비유**: 새 집에 입주할 때 "냉장고 필요해요"라고 적어두면(@Autowired) 이사 업체(Spring)가 알아서 냉장고를 가져다 놓는 것과 같습니다. 내가 직접 매장 가서 사올 필요 없이 "이 타입 필요해"라고만 말하면 됩니다.

### JPA & 데이터

**Q5. JPA에서 영속성 컨텍스트(Persistence Context)란?**

> 엔티티를 관리하는 일종의 캐시 영역입니다. `em.find()`로 조회한 엔티티는 영속성 컨텍스트에 보관되고, 같은 트랜잭션 내에서 다시 조회하면 DB 대신 캐시에서 반환합니다.
>
> 트랜잭션이 커밋될 때, 영속성 컨텍스트의 엔티티와 DB 상태를 비교해서 변경된 부분만 UPDATE합니다 (더티 체킹). 이 프로젝트에서는 JSONB 컬럼의 더티 체킹 문제로 네이티브 쿼리(`updateMessagesBySessionId`)를 사용하기도 합니다.
>
> **쉬운 비유**: 도서관 책상 위에 올려놓은 책(=영속성 컨텍스트에 있는 엔티티)과 같습니다. 같은 책이 필요하면 서가(DB)에 다시 가지 않고 책상 위에서 바로 봅니다. 책에 메모를 적으면(값 변경), 도서관을 나갈 때(트랜잭션 커밋) 자동으로 서가에 반영됩니다.

**Q6. N+1 문제가 무엇이고 해결 방법은?**

> 1개의 쿼리로 N개의 엔티티를 조회한 후, 각 엔티티의 연관 관계를 조회하기 위해 N개의 추가 쿼리가 실행되는 문제입니다.
>
> 해결 방법:
> - `@EntityGraph` 또는 `JOIN FETCH`로 한 번에 조회
> - `@BatchSize`로 IN 절 묶어서 조회
> - DTO 프로젝션으로 필요한 데이터만 조회
>
> **쉬운 비유**: 반 학생 30명의 부모님 연락처를 알고 싶다고 합시다. N+1 방식은 학생 명단을 1번 조회한 후, 학생 한 명씩 "이 학생 부모님 번호 뭐야?"를 30번 물어보는 것입니다(총 31번). JOIN FETCH는 처음부터 "학생 명단이랑 부모님 연락처 같이 줘"라고 1번만 요청하는 것입니다.

**Q7. @Transactional이 동작하는 원리는?**

> Spring AOP 기반의 프록시 패턴으로 동작합니다. `@Transactional`이 붙은 메서드를 호출하면 실제 객체가 아닌 프록시 객체가 호출되어, 메서드 시작 시 트랜잭션을 시작하고 정상 완료 시 커밋, 예외 발생 시 롤백합니다.
>
> 주의할 점: 같은 클래스 내에서 `@Transactional` 메서드를 호출하면 프록시를 거치지 않아 트랜잭션이 적용되지 않습니다 (self-invocation 문제).
>
> **쉬운 비유**: 은행 이체와 같습니다. A 계좌에서 출금 → B 계좌에 입금, 이 두 단계를 하나의 거래(트랜잭션)로 묶습니다. 입금 단계에서 오류가 나면 출금도 취소(롤백)됩니다. "A에서 돈은 빠졌는데 B에는 안 들어간" 상황을 방지하는 것입니다.

### Spring MVC & 웹

**Q8. @Controller와 @RestController의 차이는?**

> `@Controller`는 View를 반환하고, `@RestController`는 데이터(JSON 등)를 직접 반환합니다.
> `@RestController` = `@Controller` + `@ResponseBody` 입니다.
>
> 이 프로젝트처럼 API 서버에서는 `@RestController`를 사용합니다.
>
> **쉬운 비유**: `@Controller`는 "3번 창구로 가세요"라고 안내하는 직원(→ 화면 페이지를 돌려줌)이고, `@RestController`는 "여기 서류 있습니다"라고 데이터를 직접 건네주는 직원(→ JSON 데이터를 돌려줌)입니다. 요즘 프론트엔드/백엔드가 분리된 구조에서는 데이터만 주고받으므로 `@RestController`를 주로 씁니다.

**Q9. Filter, Interceptor, AOP의 차이는?**

> 요청 처리 흐름에서 동작하는 위치가 다릅니다:
> ```
> 요청 → [Filter] → DispatcherServlet → [Interceptor] → Controller → [AOP] → Service
> ```
> - **Filter**: 서블릿 레벨. Spring 밖에서 동작. 인코딩, 보안, 로깅 등. (이 프로젝트: RateLimitFilter)
> - **Interceptor**: Spring MVC 레벨. 인증/인가, 로깅 등.
> - **AOP**: 메서드 레벨. 트랜잭션, 로깅 등. `@Transactional`이 대표적인 AOP.
>
> **쉬운 비유**: 건물 출입에 비유하면, **Filter**는 건물 정문 경비(모든 방문자 검사), **Interceptor**는 층별 안내 데스크(해당 층 방문자만 확인), **AOP**는 각 사무실 문 앞의 출입카드 리더기(특정 방에 들어갈 때만 작동)입니다. 범위가 넓은 것부터 좁은 것 순서입니다.

**Q10. SSE(Server-Sent Events)와 WebSocket의 차이는?**

> - **SSE**: 서버 → 클라이언트 단방향 스트리밍. HTTP 기반이라 별도 프로토콜 불필요. 텍스트 데이터 전송에 적합.
> - **WebSocket**: 양방향 통신. 별도 프로토콜(ws://)을 사용. 실시간 채팅, 게임 등에 적합.
>
> 이 프로젝트에서는 AI 응답이 서버에서 클라이언트로의 단방향 전송이므로 SSE가 적합합니다.
>
> **쉬운 비유**: **SSE**는 라디오 방송입니다. 방송국(서버)이 일방적으로 송출하고 청취자(브라우저)는 듣기만 합니다. **WebSocket**은 전화 통화입니다. 양쪽이 동시에 말하고 들을 수 있습니다. AI 챗봇은 "질문 보내고 → 답변 듣기"이므로 라디오(SSE)면 충분합니다.

### 예외 처리 & 설계

**Q11. @RestControllerAdvice를 사용하는 이유는?**

> 각 컨트롤러마다 try-catch로 예외를 처리하면 코드가 중복되고, 에러 응답 형식이 달라질 수 있습니다.
> `@RestControllerAdvice`로 전역 예외 처리를 하면:
> - 에러 응답 형식이 통일됨
> - 컨트롤러 코드가 깔끔해짐
> - 예외 처리 로직을 한 곳에서 관리 가능
>
> 이 프로젝트에서는 OpenAI API 에러(401, 429, 500 등)를 분류하여 사용자에게 적절한 메시지를 반환합니다.
>
> **쉬운 비유**: 회사에서 고객 불만이 생기면 각 부서가 제각각 대응하면 혼란스럽습니다. 대신 "고객센터"라는 전담 부서를 하나 두면 모든 불만을 일관된 방식으로 처리할 수 있습니다. `@RestControllerAdvice`가 이 고객센터 역할입니다.

**Q12. 커스텀 예외를 만드는 이유는?**

> 표준 예외(RuntimeException 등)만 사용하면 어떤 상황의 에러인지 구분하기 어렵습니다.
> 커스텀 예외를 만들면:
> - 예외 종류별로 다른 HTTP 상태 코드 반환 가능
> - 에러 코드로 클라이언트가 에러 유형을 구분 가능
> - `@ExceptionHandler`에서 특정 예외만 잡아서 처리 가능
>
> 이 프로젝트의 `OpenAIException`은 `httpStatus`와 `errorCode`를 포함하여 상황별 대응이 가능합니다.
>
> **쉬운 비유**: 병원에서 "아파요"라고만 하면 어디가 아픈지 모릅니다. "두통이요", "복통이요"처럼 구체적으로 말해야 의사가 올바른 처방을 내릴 수 있습니다. `RuntimeException`은 "아파요"이고, `OpenAIException.rateLimitExceeded()`는 "API 호출 횟수 초과입니다"처럼 정확한 진단명을 주는 것입니다.

### 아키텍처 & 설계 패턴

**Q13. 이 프로젝트의 아키텍처를 설명해주세요.**

> 3계층 레이어드 아키텍처 + RAG 패턴을 사용합니다.
> - **Controller 계층**: HTTP 요청/응답 처리, SSE 스트리밍
> - **Service 계층**: 비즈니스 로직 (세션 관리, 벡터 검색 조율, AI 응답 생성)
> - **Repository 계층**: 데이터 접근 (JPA + 네이티브 쿼리)
>
> RAG(Retrieval-Augmented Generation) 패턴으로 AI 응답의 정확도를 높입니다:
> 1. 사용자 질문을 벡터로 변환 (Embedding)
> 2. 유사한 포트폴리오 데이터를 DB에서 검색 (Vector Search)
> 3. 검색 결과를 GPT 프롬프트에 포함하여 응답 생성 (Generation)
>
> **쉬운 비유**: 오픈북 시험과 같습니다. GPT가 맨 머리로 답하면 부정확할 수 있지만(일반 ChatGPT), 관련 자료를 펼쳐놓고 답하면(RAG) 정확도가 훨씬 높아집니다. 이 프로젝트에서는 "포트폴리오 데이터"라는 책을 펼쳐놓고 GPT에게 답하게 합니다.

**Q14. DTO를 왜 사용하나요? Entity를 직접 반환하면 안 되나요?**

> Entity를 직접 반환하면:
> - DB 스키마가 API 응답에 그대로 노출 (보안 문제)
> - 불필요한 필드까지 전송 (성능 문제)
> - Entity 변경이 API 스펙에 영향 (결합도 문제)
> - 양방향 연관관계에서 순환 참조 발생 가능
>
> DTO로 분리하면 API 스펙과 DB 스키마를 독립적으로 변경할 수 있습니다.
>
> **쉬운 비유**: 이력서 원본(Entity)에는 주민번호, 집 주소 같은 민감한 정보가 있습니다. 면접관에게 원본을 그대로 주면 위험하죠. 대신 필요한 정보만 골라 담은 자기소개서(DTO)를 따로 만들어서 전달하는 것입니다.

### Spring Boot 설정

**Q15. application.properties에서 `${ENV_VAR:default}` 문법의 의미는?**

> 환경변수가 있으면 그 값을 사용하고, 없으면 기본값을 사용합니다.
> 이를 통해 코드 변경 없이 로컬/개발/운영 환경별로 다른 설정을 적용할 수 있습니다.
>
> 예시: `server.port=${PORT:8080}`
> - 로컬: PORT 미설정 → 8080
> - Render 배포: PORT=10000 → 10000
>
> **쉬운 비유**: 택배 주소에 "사무실 주소, 없으면 자택 주소"라고 적는 것과 같습니다. 운영 서버에서는 운영 DB 주소(환경변수)를 넣고, 로컬에서는 아무것도 안 넣으면 자동으로 localhost(기본값)로 연결됩니다.

**Q16. Spring Boot의 자동 설정(Auto-Configuration)이란?**

> 클래스패스에 있는 라이브러리를 감지하여 자동으로 Bean을 설정해주는 기능입니다.
> 예를 들어 `spring-boot-starter-data-jpa`를 추가하면 DataSource, EntityManager, TransactionManager 등이 자동 설정됩니다.
>
> 이 프로젝트에서 `spring-boot-starter-webflux`를 추가하면 WebClient, ReactorResourceFactory 등이 자동으로 등록됩니다. `spring.ai.openai.*` 속성만 설정하면 ChatClient, EmbeddingModel 등이 자동 생성됩니다.
>
> **쉬운 비유**: 호텔에 체크인하면 침대, 수건, 비누가 이미 세팅되어 있는 것과 같습니다. "JPA 라이브러리를 넣었네? 그러면 DB 연결, 트랜잭션 관리자, 엔티티 매니저가 필요하겠군" 하고 Spring Boot가 알아서 준비해줍니다. 직접 하나하나 설정할 필요가 없습니다.
-- 프로필 샘플 데이터
INSERT INTO profile (
    name,
    occupation,
    experience,
    current_company,
    education,
    introduction,
    tech_stack,
    interests,
    email,
    github_url,
    blog_url,
    certifications,
    is_active,
    created_at,
    updated_at
) VALUES (
    '이세령',
    '풀스택 개발자',
    '2년 4개월',
    '그렉터',
    '한경대학교 컴퓨터공학과',
    '자동화와 구조 개선으로 운영 효율과 안정성을 높인 서비스 전주기 경험. 플랫폼 개발팀(60명 중 20명)에서 IoT 수자원/시설물 운영 웹 서비스(Aliot WaterGrid, DMS Portal 등)를 개발 및 운영하며 서비스 전주기를 경험했습니다. 모니터링 시스템을 구축해 수동 타입별 없이 오류를 확인할 수 있는 환경을 마련했고, 데이터 수집 → 가공 → 저장 전 과정을 관리하며 안정적인 데이터 플로우 운영에 기여했습니다.',
    '{
        "expert": ["JavaScript", "TypeScript", "Node.js"],
        "proficient": ["Spring Boot", "Next.js", "React", "Vue", "PostgreSQL", "Opensearch/Elasticsearch", "Linux"],
        "familiar": ["Java", "Redis", "Docker"]
    }'::jsonb,
    '데이터베이스, 인프라, 시스템 아키텍처',
    'srlimvp@gmail.com',
    'https://github.com/Hediar',
    'https://velog.io/@hediar',
    '[
        {"name": "정보처리기사", "issuer": "한국산업인력공단", "date": "2025.09"}
    ]'::jsonb,
    true,
    NOW(),
    NOW()
);

-- 포트폴리오 데이터
-- 프로젝트 데이터
INSERT INTO portfolio_data (type, title, content, metadata, source, priority, is_public, created_at, updated_at) VALUES
(
    'project',
    'popo-ai',
    'AI 기반 포트폴리오 소개 서비스입니다. 방문자와 대화하며 포트폴리오를 소개합니다. Spring Boot, PostgreSQL, pgvector를 사용하여 벡터 검색 기반 RAG(Retrieval Augmented Generation) 패턴을 구현했습니다. 방문자 질문을 기반으로 포트폴리오 데이터를 검색하고, OpenAI API를 활용해 자연스러운 대화로 응답합니다.',
    '{
        "techStack": ["Java", "Spring Boot", "PostgreSQL", "Next.js", "pgvector", "OpenAI API", "Spring AI", "JPA", "Redis", "Bucket4j"],
        "role": "풀스택 개발",
        "duration": "2026.03 - 진행중",
        "background": "기존 정적 포트폴리오는 방문자가 원하는 정보를 찾기 어렵고, 제공자 입장에서도 모든 내용을 효과적으로 전달하기 어려웠습니다. AI를 활용해 방문자의 질문에 맞춰 포트폴리오를 동적으로 소개하는 시스템을 기획했습니다.",
        "goals": ["방문자 질문 기반 포트폴리오 정보 제공", "RAG 패턴으로 정확한 정보 전달", "자연스러운 대화 경험 제공", "확장 가능한 벡터 검색 시스템 구축"],
        "features": ["키워드 매칭 필터링 → 벡터 검색 2단계 검색 전략", "프로필 정보를 DB에 저장하고 AI 프롬프트에 활용", "대화 세션 관리 및 히스토리 저장 (JSONB)", "IP 기반 Rate Limiting (Bucket4j)", "Spring AI ChatClient 활용한 OpenAI 통합"],
        "challenges": ["효율적인 검색을 위한 키워드 필터링 + 벡터 검색 조합", "한국어 불용어 처리 및 키워드 추출", "대화 히스토리 관리 전략 (캐싱 vs DB 저장)", "기술 스택 숙련도별 분류 및 관리"],
        "achievements": ["키워드 매칭 필터링 구현 (성능 최적화)", "RAG 패턴 적용 (Profile + Portfolio Data + Conversation History)", "실시간 대화 시스템 구축", "JSONB 활용한 유연한 데이터 구조 설계", "IP 기반 요청 제한으로 서비스 안정성 확보"]
    }'::jsonb,
    'project-popo-ai',
    10,
    true,
    NOW(),
    NOW()
),
(
    'project',
    '불도적(BDZ) - 주차 플랫폼',
    '공영 + 유료 주차장 정보와 불법주정차 단속구역을 통합한 주차 앱 서비스입니다. Spring Boot 기반 백엔드 API 개발 및 ERD 설계를 담당했습니다. 시민에게는 공영+유료 주차장+단속 정보를 한 번에 보여주고 검색부터 결제·주차·알림까지 원스톱 제공하며, 주차장 관리자에게는 개인 소유 유료 주차장을 등록·홍보·운영·정산할 수 있는 채널을 제공합니다.',
    '{
        "techStack": ["Spring Boot", "PostgreSQL", "Redis", "Spring Security", "Docker", "JWT", "BCrypt"],
        "role": "백엔드 개발 (Auth, 사용자, ERD 초안 구성)",
        "duration": "2025.12 - 진행중",
        "goals": ["시민에게는: 공영 + 유료 주차장 + 단속 정보를 한 번에 보여주고, 검색부터 결제·주차·알림까지 원스톱 제공", "주차장 관리자에게는: 개인 소유 유료 주차장을 등록·홍보·운영·정산할 수 있는 채널 제공", "행정에게는: 신고·단속·주차 데이터가 모이는 정책/단속 참고 데이터 인프라 구축"],
        "authDesign": {
            "background": "모바일 앱/웹 서비스의 회원/인증을 확인할 수 있는 환경을 마련했습니다. 세션 기반 인증은 서버 확장 시 상태 관리가 복잡하고, 다중 클라이언트 환경에서도 일관된 인증 처리가 필요했습니다. 이에 따라 무상태(Stateless) API에 적합하고 모바일 앱과 JWT 기반 인증 구조를 설계했고, 토큰 갱신을 위한 리프레시 토큰은 Redis로 관리해 인증 체계를 설계/구현했습니다",
            "goals": ["보안: BCrypt 해시 기반 비밀번호 저장, 표준 JWT 서명/검증 적용", "성능/확장성: 액세스 토큰은 무상태 검증, 리프레시 토큰은 Redis로 관리해 DB 의존 최소화", "운영 편의: Spring Security 기반 일관된 필터 체인, DB 커넥션/스키마 변경/헬스체크를 표준화"],
            "implementation": ["인증·인가: Spring Security 필터 체인으로 /api/v1/auth/** 공개, 그 외 엔드포인트 보호. BCryptPasswordEncoder, DaoAuthenticationProvider 기반 표준 인증 흐름 구성. JwtAuthenticationFilter + JwtTokenProvider로 액세스/리프레시 토큰 발급/검증", "토큰 저장 전략: 액세스 토큰은 JWT Claims 서명 검증 기반 무상태 처리. 리프레시 토큰은 Redis(refresh:{userId}) + TTL 관리로 로테이션 및 강제 로그아웃 지원", "데이터베이스: PostgreSQL + JPA(Hibernate) 안정적 커넥션 유지"]
        },
        "retrospective": {
            "keep": ["책임 분리: 토큰 발급/검증, Redis 접근, 도메인 로직 분리로 테스트 용이성 확보", "보안 기본기: BCrypt, 토큰 만료/검증, 민감정보 환경변수 분리", "확장성: 무상태 액세스 토큰 + 상태적 리프레시 토큰"],
            "problem": ["OAuth2 연동까지 확장하지 못해 소셜 로그인 시나리오를 다루지 못했습니다", "테스트는 핵심 로직 위주로만 구성했고, 인증 플로우 E2E/보안 시나리오 자동화가 부족했습니다", "TDD로 설계를 이끌진 못해, 다음 프로젝트에서는 시나리오 기반 테스트를 먼저 작성하는 방식으로 개선할 계획입니다"]
        },
        "learnings": "인증 기능을 구현하며 보안/성능/운영 안정성의 균형을 이해했습니다. 모바일 환경에서는 JWT 기반 무상태 인증이 확장성과 운영 측면에 유리하고, Redis를 활용한 리프레시 토큰 관리가 운영 유연성 측면에서 효과적이었습니다. 다음에는 테스트 기반 설계와 OAuth2 확장을 고려해 인증 구조를 설계할 계획입니다",
        "achievements": ["Auth 기능 구현 (JWT + Redis 기반 액세스/리프레시 토큰)", "ERD 설계 (dbdiagram.io)", "RESTful API 설계", "Spring Security 필터 체인 구성", "무상태 인증 구조로 확장성 확보"]
    }'::jsonb,
    'project-bdz',
    10,
    true,
    NOW(),
    NOW()
),
(
    'project',
    'dfm-evt 백엔드 유지보수',
    'Node.js 기반 데이터 플로우 관리 시스템(dfm-evt)의 백엔드 유지보수 및 기능 개선 업무를 담당하고 있습니다.',
    '{
        "techStack": ["Node.js", "Express", "OpenSearch/ElasticSearch","PostgreSQL","MQTT"],
        "role": "백엔드 유지보수",
        "duration": "2026.03 - 현재",
        "achievements": ["안정적인 데이터 플로우 관리", "버그 수정 및 기능 개선"]
    }'::jsonb,
    'project-dfm-evt',
    9,
    true,
    NOW(),
    NOW()
),
(
    'project',
    '로그 모니터링 경량 시스템',
    '15개 이상의 서버를 운영하던 중 로그 확인이 어려워 매번 서버에 SSH 접속해 직접 파일을 열어봐야 했습니다. 원격 서버의 로그를 새벽 수집하여 웹 UI로 검색·조회할 수 있는 경량 로그 모니터링 도구를 개발했습니다. cron과 ingest job을 설계해 SFTP로 로그 수집 → OpenSearch 색인 → UI는 Express API로 조회하는 형태로 구성했습니다.',
    '{
        "techStack": ["React.js", "Express.js", "Cron", "Opensearch", "SFTP"],
        "role": "풀스택 개발",
        "duration": "2025.12 - 2026.01",
        "background": "15개 이상 서버 운영 중 로그 확인이 어려움. 하지만 쉽게 로그가 무엇을 의미하는지 알아야 했고, 매번 서버에 접속해 직접 파일을 열어봐야 했습니다.",
        "goals": ["원격 서버 로그 새벽 수집 → 정보 최신화, 부하 최소화", "검색 가능 → 필요한 내용, 시각별 로그 확인", "웹 UI로 확인 → 비개발자도 대응 가능하도록 모니터링 제공"],
        "challenges": ["초기 rsync/scp 기반 수집 시 의존성 차이로 문제 발생", "로그 포맷이 서버마다 조금씩 달라 파싱 로직을 유연하게 가져가야 했음"],
        "solutions": ["운영 안정성 위해 SFTP 단일 방식으로 정리", "cron과 ingest job을 설계 → ingest job이 SFTP로 로그 수집 → OpenSearch 색인 → UI는 Express API로 조회", "ingest job(쓰기)과 API 서버(읽기) 분리"],
        "retrospective": {
            "keep": ["처음부터 운영 환경에 적용하지 않고 테스트 서버에서 검증", "SFTP 단일 방식으로 단순화", "ingest job(쓰기)과 API 서버(읽기) 분리 구조"],
            "problem": ["rsync/scp 의존성 문제 → SFTP로 정리", "로그 포맷 차이로 파싱 로직 복잡도 증가"]
        },
        "achievements": ["운영팀 수작업 시간 감소", "시간/키워드 기반 검색으로 대응 속도 향상", "SFTP 단일 방식으로 안정성 확보"]
    }'::jsonb,
    'project-log-monitoring',
    9,
    true,
    NOW(),
    NOW()
),
(
    'project',
    'Viewtrack 플랫폼 백엔드 마이그레이션',
    '기존 Nuxt2 + Express에서 운영하던 외부 서비스 연동 구조를 분석하고, 이를 Nest.js 기반 독립 Backend API 서버로 마이그레이션했습니다. Backend와 Frontend를 완전 분리하여 다양한 UI 서비스와 연동 가능하도록 설계했습니다. NestJS 모듈 서비스로 분리하고, 기능별 폴더 분리를 통해 유지보수 확장이 쉽도록 변경했습니다.',
    '{
        "techStack": ["Nest.js", "React.js", "Redis", "TS Redis", "Opensearch", "MQTT"],
        "role": "백엔드 개발",
        "duration": "2025.11",
        "background": "기존 Nuxt2 + Express에서 운영하던 외부 서비스 연동 구조 분석",
        "problems": ["Node.js 구버전으로 인한 보안 취약점 및 패키지 업데이트 제약", "타입 관리 없이 확장된 코드로 인해 타입 오류가 누적", "UI, API 로직이 혼재된 구조로 유지보수가 어렵고 확장성이 부족"],
        "goals": ["Backend와 Frontend 완전 분리", "다양한 UI 서비스와 연동 가능", "설정 및 기능 모듈화"],
        "solutions": ["기존 외부 서비스 사용 분석 (Redis, TS Redis, ILinker, MQTT, Opensearch)", "NestJS 모듈 서비스로 분리", "기능별 폴더 분리를 통해 유지보수 확장이 쉽도록 변경"],
        "achievements": ["NestJS 모듈 서비스로 분리", "기능별 폴더 분리로 유지보수 개선", "외부 서비스 연동 안정화"]
    }'::jsonb,
    'project-viewtrack',
    8,
    true,
    NOW(),
    NOW()
),
(
    'project',
    'Aliot DMS Portal',
    'IoT 센서 단말기(DMS - Device Management System)를 원격 모니터링 및 관리하는 웹 애플리케이션입니다. 약 15대 이상 서버 운영 환경에서 장애/이슈 발생 시 매번 다른 서버로 접속하여 관리하기 힘들었습니다. 운영팀의 수작업(데이터 처리/조회/정리) 절차를 단순화(Excel 입·출력 + 공통 API)하고, ORM 기반 API 및 공통 컴포넌트로 유지보수 안정성을 확보했습니다. 관리팀의 운영을 하나의 시스템으로 통합하면서 운영 비용이 감소했고, 입력 검증과 공통 컴포넌트 기반으로 일관성이 높아져 유지보수 안정성이 개선되었습니다.',
    '{
        "techStack": ["Next.js 15 (App Router)", "React 19", "TypeScript", "Tailwind CSS 4", "shadcn/ui", "Drizzle ORM", "PostgreSQL", "React Hook Form", "Zod", "Zustand", "Biome", "Excel 기반 입출력"],
        "role": "풀스택 개발",
        "duration": "2025.07.08 - 2025.08.18",
        "background": "약 15개 서비스의 전체 단말기 관리를 한번에 하기 위해 해당 프로젝트를 개발하게 되었습니다.",
        "goals": ["단말기 관리팀이 운영 업무를 수행하는 데 필요한 조회·등록·수정·관리(운영 CRUD) 흐름을 일원화", "장애/이슈 대응 시 필요한 정보를 화면에서 빠르게 확인할 수 있도록 운영 동선 단축", "Excel 기반 운영 프로세스를 고려해 입·출력 기능 및 데이터 검증"],
        "challenges": ["운영 규모가 커지면서 단말기 관리팀의 반복 작업과 상태 확인 비용이 증가했습니다", "초기 운영 정책(데이터 품질)을 충분히 표준화하지 못해 변경 비용이나 고객 응대에 문제가 생길 가능성이 커졌습니다", "관리팀의 운영을 하나의 시스템으로 통합하면서 운영 비용이 감소했고, 입력 검증과 공통 컴포넌트 기반으로 일관성이 높아져 유지보수 안정성이 개선되었습니다"],
        "solutions": ["운영팀 업무를 기준으로 CRUD/관리 화면을 정리하고 Excel 연계로 현업 친화성 확보", "입력 검증을 강화해 오입력/재처리 비용 감소", "이후, 정규화/검증 로직 등 기반으로 분리하여 신규 정치 추가 시 코드 변경 없이 확장 가능하도록 개선", "또한, 단말기 관리팀의 운영 흐름을 기준으로 필요한 기능을 우선순위화하여 지속적으로 추가할 예정입니다"],
        "retrospective": {
            "keep": ["운영팀 업무를 기준으로 CRUD/관리 화면을 정리하고 Excel 연계로 현업 친화성 확보", "입력 검증을 강화해 오입력/재처리 비용 감소"],
            "problem": ["현장 단말의 통신 불안정, 시간 동기화, 결측/중복 데이터 등 예외 케이스가 많아, 데이터 품질 관리(검증·정합성) 체계를 더 강하게 잡을 필요가 있었습니다", "운영 영향도가 큰 기능(데이터 수정, 일괄 처리 등)에 대해 E2E 테스트나 롤백 전략을 더 명확히 마련하면 안정성이 더 높아질 여지가 있었습니다"]
        },
        "achievements": ["운영팀 수작업 데이터 처리 시간 감소", "ORM 기반 API로 유지보수 안정성 확보", "Excel 연계로 현업 친화성 확보", "운영 이슈 대응 시 필요한 정보 접근을 개선하여 관리팀의 처리 속도 및 대응 품질 향상에 기여"]
    }'::jsonb,
    'aliot-dms',
    8,
    true,
    NOW(),
    NOW()
),
(
    'project',
    '위험시설물(FSMS) 최소기능 개발',
    '사업점검에서 요구사항 미충족 리스크가 있어, 빠르게 추가하고 수정해야 하는 상황이었습니다. 계획된 일정(약 2~3주) 내 우선순위 기반 핵심 화면 기능을 완료했습니다. CORS 이슈 해결을 위해 UI 서버에 서버사이드 프록시 API를 구성하여 Same-Origin으로 요청을 처리했습니다.',
    '{
        "techStack": ["Nuxt.js (Vue.js)", "Node.js", "Express", "Bootstrap", "JavaScript", "Docker"],
        "role": "풀스택 개발",
        "duration": "2025.12.01 - 2025.12.24",
        "background": "사업점검에서 요구사항 미충족 리스크가 있어, 빠르게 추가하고 수정 + 있는 기능 필요",
        "goals": ["계획된 일정(약 2~3주) 내 우선순위 기반 핵심 화면 기능 완료", "협업팀 요구사항을 빠르게 반영할 수 있도록 개발 진행/현황 공유 체계화"],
        "challenges": ["CORS 이슈: 클라이언트에서 api 서버를 통해 요청 시 발생", "장치 ID 정책 협의: 학습 모델 특성상 ID 변경 시 재작업 비용이 크기 때문"],
        "solutions": [
            "CORS 이슈 해결: UI 서버에 서버사이드 프록시 API 구성 → 클라이언트는 UI 서버의 프록시 API로 요청 전송 → UI 서버(프록시)는 설정된 AI 백엔드로 JSON POST 전달 → 응답 처리: 백엔드의 JSON 응답은 그대로 반환, HTML 등 비정상 응답은 JSON 에러로 변환해 반환 → 반환: 클라이언트는 Same-Origin으로 JSON 응답 수신",
            "장치 ID 정책 협의: 학습 모델 특성상 ID 변경 시 재작업 비용이 크기 때문에 협의를 통해 시설명_장치명으로 임시 표준화",
            "설계 의사결정: (안1) 클라이언트에서 dfm-evt 서버를 통해 api 요청 ⇒ 서버 부하 및 구조 복잡도 증가 / (안2) 클라이언트에서 1개씩 모든 목록을 요청 ⇒ 클라이언트 부하 예상 / (최종) 조건 충족 시 버튼 활성화 → 클릭 시 단건 조회로 트래픽/복잡도 최소화"
        ],
        "retrospective": {
            "keep": ["우선순위 재정렬 및 진행 현황 공유로 팀의 의사결정 속도를 높인 점", "협업을 통해 요구사항을 빠르게 반영하며 완료까지의 리드타임을 단축한 점"],
            "problem": ["일정 압박이 있는 프로젝트 특성상, 기능 고도화(UX 디테일, 예외처리) 영역이 후순위로 밀릴 수밖에 없는 한계"]
        },
        "achievements": ["우선순위 재정렬 및 진행 현황 공유로 의사결정 속도 향상", "CORS 프록시 구축으로 안정적 API 연동"]
    }'::jsonb,
    'project-fsms',
    7,
    true,
    NOW(),
    NOW()
),
(
    'project',
    '영화 평론 커뮤니티',
    'TMDB API를 활용한 영화 정보 조회 및 커뮤니티 서비스입니다. Next.js 13과 Supabase를 사용하여 풀스택 개발을 진행했습니다. 팀 프로젝트 리더로서 Git 협업, 개발 일정 관리, DB 설계 등을 담당하며 프로젝트를 성공적으로 완료했습니다. 5주 4인 팀 프로젝트였습니다.',
    '{
        "techStack": ["Next.js 13", "TypeScript", "Supabase", "TailwindCSS", "Zustand", "Sentry", "Lodash", "TanStack Query", "Vercel"],
        "role": "프로젝트 리더 및 풀스택 개발",
        "duration": "2023.08.16 - 2023.09.15 (5주, 4인)",
        "responsibilities": ["Git을 활용한 소스 코드 버전 관리 및 협업", "개발 일정 관리 및 팀원 업무 배정", "프로젝트 디렉토리 구조 설계 및 초기 환경 세팅", "DB 구조 설계 및 핵심 기능 구현 (메인 페이지, 영화 목록, 좋아요/추천 등)"],
        "implementations": ["NextJS App Route로 각 페이지 CRUD 기능 개발", "내부 공통 컴포넌트·함수 구성 및 협업을 통한 재사용성 확보", "Git Commit Convention 준수로 기록 관리 및 협업 효율성 강화"],
        "achievements": ["API 병렬 처리로 렌더링 성능 20% 개선", "Throttle/Debounce 적용 및 스켈레톤 UI 도입으로 UX 개선", "디자이너와 협업을 통해 기획-개발 간 협업 프로세스 경험", "Git Flow 전략, Git commit convention 코드 품질 개선"],
        "links": {
            "demo": "https://moviebaba.vercel.app/",
            "github": "https://github.com/Hediar/NBC-Project?tab=readme-ov-file",
            "notion": "https://deeply-silence-9a4.notion.site/39145054cc05484a8a2b01a11237d5f9"
        }
    }'::jsonb,
    'project-movie-community',
    6,
    true,
    NOW(),
    NOW()
);

-- 경력 데이터
INSERT INTO portfolio_data (type, title, content, metadata, source, priority, is_public, created_at, updated_at) VALUES
(
    'experience',
    '그렉터 - 풀스택 엔지니어',
    '플랫폼 개발팀(60명 중 20명)에서 IoT 수자원/시설물 운영 웹 서비스(Aliot WaterGrid, DMS Portal 등)를 개발 및 운영하며 서비스 전주기를 경험했습니다. Cron+Slack, ExcelJS, Playwright 기반 자동화로 운영 업무 시간을 50% 이상 절감했고, 서비스 리뉴얼과 구조 개선으로 유지보수 대응 시간을 20% 단축했습니다. 또한 UI/UX 개선을 통해 문의 및 오류를 약 80% 줄였으며, 검색 성능을 14초에서 5초로 개선하고 벌크 속도를 약 10% 향상시키는 등 운영 안정성과 성능을 지속적으로 개선했습니다.',
    '{
        "company": "그렉터",
        "position": "풀스택 엔지니어",
        "duration": "2023.11.03 - 현재",
        "team": "플랫폼 개발팀 (60명 중 20명)",
        "responsibilities": [
            "IoT 플랫폼 개발 및 운영 (Aliot WaterGrid, DMS Portal 등)",
            "운영 자동화 도구 개발 (Cron+Slack, ExcelJS, Playwright)",
            "서비스 리뉴얼 및 구조 개선",
            "로그 모니터링 시스템 구축",
            "데이터 수집 -> 가공 -> 저장 플로우 관리"
        ],
        "achievements": [
            "운영 업무 시간 50% 절감",
            "유지보수 대응 시간 20% 단축",
            "UI/UX 개선으로 문의·오류 80% 감소",
            "검색 성능 14초 → 5초로 개선",
            "빌드 속도 10% 향상"
        ]
    }'::jsonb,
    'experience-gractor',
    10,
    true,
    NOW(),
    NOW()
);

-- 기술 스택 데이터
INSERT INTO portfolio_data (type, title, content, metadata, source, priority, is_public, created_at, updated_at) VALUES
(
    'skill',
    'JavaScript & TypeScript',
    'JavaScript와 TypeScript 개발 능력을 보유하고 있습니다. 모던 JavaScript(ES6+), TypeScript를 활용한 타입 안전한 코드 작성, 비동기 처리',
    '{
        "proficiency": "expert",
        "years": "2년+",
        "related_projects": ["popo-ai", "로그 모니터링", "Aliot DMS Portal", "Viewtrack", "영화 평론 커뮤니티"]
    }'::jsonb,
    'skill-javascript-typescript',
    10,
    true,
    NOW(),
    NOW()
),
(
    'skill',
    'Node.js & Express',
    'Node.js 백엔드 개발 능력을 보유하고 있습니다. Express를 활용한 RESTful API 설계 및 구현, 미들웨어 구성, 비동기 처리',
    '{
        "proficiency": "expert",
        "years": "2년+",
        "related_projects": ["dfm-evt", "로그 모니터링", "위험시설물(FSMS)", "Aliot DMS Portal"]
    }'::jsonb,
    'skill-nodejs',
    10,
    true,
    NOW(),
    NOW()
),
(
    'skill',
    'Spring Boot',
    'Spring Boot 개발 경험을 보유하고 있습니다. RESTful API 설계, JPA를 활용한 데이터베이스 연동, Spring Security를 통한 인증/인가 구현 경험이 있습니다.',
    '{
        "proficiency": "proficient",
        "years": "1년+",
        "related_projects": ["popo-ai", "불도저(BDZ)"]
    }'::jsonb,
    'skill-spring-boot',
    9,
    true,
    NOW(),
    NOW()
),
(
    'skill',
    'Next.js & React',
    'Next.js와 React 개발 능력을 보유하고 있습니다. App Router, Server Components, Client Components 구분, SSR/SSG 최적화 등',
    '{
        "proficiency": "proficient",
        "years": "2년+",
        "related_projects": ["popo-ai", "Aliot DMS Portal", "영화 평론 커뮤니티"]
    }'::jsonb,
    'skill-nextjs-react',
    9,
    true,
    NOW(),
    NOW()
),
(
    'skill',
    'Vue.js & Nuxt.js',
    'Vue.js와 Nuxt.js 개발 능력을 보유하고 있습니다. Composition API, Nuxt 2/3 기반 프로젝트 개발 경험이 있습니다.',
    '{
        "proficiency": "proficient",
        "years": "1년+",
        "related_projects": ["위험시설물(FSMS)", "Viewtrack"]
    }'::jsonb,
    'skill-vue-nuxt',
    8,
    true,
    NOW(),
    NOW()
),
(
    'skill',
    'PostgreSQL',
    'PostgreSQL 데이터베이스 관리 및 최적화 능력을 보유하고 있습니다. 쿼리 최적화, 인덱스 설계, pgvector를 활용한 벡터 검색 구현, ORM(Drizzle, JPA) 사용 경험이 있습니다.',
    '{
        "proficiency": "proficient",
        "years": "2년+",
        "related_projects": ["popo-ai", "불도저(BDZ)", "Aliot DMS Portal", "dfm-evt"]
    }'::jsonb,
    'skill-postgresql',
    8,
    true,
    NOW(),
    NOW()
),
(
    'skill',
    'Opensearch & Elasticsearch',
    'Opensearch와 Elasticsearch 사용 경험이 있습니다. 로그 수집, 검색, 통계 처리 등을 위한 검색 엔진 활용',
    '{
        "proficiency": "proficient",
        "years": "1년+",
        "related_projects": ["로그 모니터링", "Viewtrack"]
    }'::jsonb,
    'skill-opensearch',
    7,
    true,
    NOW(),
    NOW()
),
(
    'skill',
    'Nest.js',
    '경험이 있는 Nest.js 개발 능력을 보유하고 있습니다. 모듈 기반 아키텍처, 의존성 주입, TypeScript 활용 등의 경험이 있습니다.',
    '{
        "proficiency": "familiar",
        "years": "6개월+",
        "related_projects": ["Viewtrack"]
    }'::jsonb,
    'skill-nestjs',
    6,
    true,
    NOW(),
    NOW()
),
(
    'skill',
    'Redis',
    '경험이 있는 Redis 사용 능력을 보유하고 있습니다. 세션 관리, 캐싱, 리프레시 토큰 저장 등의 경험이 있습니다.',
    '{
        "proficiency": "familiar",
        "years": "1년+",
        "related_projects": ["불도저(BDZ)", "Viewtrack"]
    }'::jsonb,
    'skill-redis',
    6,
    true,
    NOW(),
    NOW()
),
(
    'skill',
    'Linux & Docker',
    '경험이 있는 Linux 시스템 관리 및 Docker 컨테이너화 능력을 보유하고 있습니다. Debian 기반 시스템 운영, Docker Compose 사용 경험이 있습니다.',
    '{
        "proficiency": "familiar",
        "years": "2년+",
        "related_projects": ["그렉터 IoT 플랫폼", "불도저(BDZ)"]
    }'::jsonb,
    'skill-linux-docker',
    6,
    true,
    NOW(),
    NOW()
);

-- 학력 데이터
INSERT INTO portfolio_data (type, title, content, metadata, source, priority, is_public, created_at, updated_at) VALUES
(
    'education',
    '한경대학교 컴퓨터공학과',
    '한경대학교 컴퓨터공학과 졸업. 알고리즘, 자료구조, 데이터베이스, 운영체제 등의 기초 CS 지식을 학습했습니다.',
    '{
        "school": "한경대학교",
        "major": "컴퓨터공학과",
        "degree": "학사",
        "duration": "2021.03 - 2023.02"
    }'::jsonb,
    'education-hankyong',
    5,
    true,
    NOW(),
    NOW()
);


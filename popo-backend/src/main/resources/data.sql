 -- pgvector 활성화
  CREATE EXTENSION IF NOT EXISTS vector;

  -- 테이블 생성
  CREATE TABLE IF NOT EXISTS profile (
      id BIGSERIAL PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      occupation VARCHAR(255),
      experience VARCHAR(255),
      current_company VARCHAR(255),
      education TEXT,
      introduction TEXT,
      tech_stack JSONB,
      interests TEXT,
      email VARCHAR(255),
      github_url VARCHAR(500),
      blog_url VARCHAR(500),
      certifications JSONB,
      metadata JSONB,
      is_active BOOLEAN NOT NULL DEFAULT TRUE,
      created_at TIMESTAMP NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMP NOT NULL DEFAULT NOW()
  );

  CREATE TABLE IF NOT EXISTS portfolio_data (
      id BIGSERIAL PRIMARY KEY,
      type VARCHAR(50) NOT NULL,
      title VARCHAR(500) NOT NULL,
      content TEXT,
      metadata JSONB,
      embedding vector(1536),
      source VARCHAR(255),
      priority INTEGER,
      is_public BOOLEAN NOT NULL DEFAULT TRUE,
      created_at TIMESTAMP NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMP NOT NULL DEFAULT NOW()
  );

  CREATE INDEX IF NOT EXISTS idx_portfolio_data_type ON portfolio_data(type);
  CREATE INDEX IF NOT EXISTS idx_portfolio_data_source ON portfolio_data(source);
  CREATE INDEX IF NOT EXISTS idx_portfolio_data_is_public ON portfolio_data(is_public);

  CREATE TABLE IF NOT EXISTS chat_sessions (
      id BIGSERIAL PRIMARY KEY,
      session_id VARCHAR(255) UNIQUE NOT NULL,
      user_id VARCHAR(255),
      client_ip VARCHAR(50),
      messages JSONB,
      status VARCHAR(50),
      created_at TIMESTAMP NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMP NOT NULL DEFAULT NOW()
  );

  CREATE INDEX IF NOT EXISTS idx_chat_sessions_session_id ON chat_sessions(session_id);
  CREATE INDEX IF NOT EXISTS idx_chat_sessions_created_at ON chat_sessions(created_at);

  -- 확인
  SELECT 'Tables created successfully!' as status;

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
    '한경대학교 컴퓨터공학과 (학점 3.6), 원광대학교 컴퓨터소프트웨어공학과 (학점 4.03)',
    '자동화와 구조 개선으로 운영 효율과 안정성을 높인 서비스 전주기 경험. 플랫폼 개발팀(60명 중 20명)에서 IoT 수자원/시설물 운영 웹 서비스(Aliot WaterGrid, DMS Portal 등)를 개발 및 운영하며 서비스 전주기를 경험했습니다. 모니터링 시스템을 구축해 수동 타입별 없이 오류를 확인할 수 있는 환경을 마련했고, 데이터 수집 → 가공 → 저장 전 과정을 관리하며 안정적인 데이터 플로우 운영에 기여했습니다.',
    '{
        "expert": ["JavaScript", "Next.js","Node.js"],
        "proficient": ["Spring Boot",  "TypeScript", "React", "Vue", "PostgreSQL", "Opensearch/Elasticsearch", "Linux"],
        "familiar": ["Java", "Redis", "Docker"]
    }'::jsonb,
    '데이터베이스, 인프라, 시스템 아키텍처',
    'srlimvp@gmail.com',
    'https://github.com/Hediar',
    'https://velog.io/@hediar',
    '[
        {"name": "정보처리기사", "issuer": "한국산업인력공단", "date": "2025.09"},
        {"name": "SQLD", "issuer": "한국데이터산업진흥원", "date": "2026.03"}
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
    'POPO - AI 기반 포트폴리오 챗봇',
    '이력서, 포트폴리오, 경력기술서를 기반으로 사용자의 질문에 답변하는 RAG 기반 AI 챗봇입니다. 정적인 포트폴리오의 한계를 극복하고, 사용자가 원하는 정보를 대화형으로 빠르게 제공합니다. Spring Boot와 PostgreSQL + pgvector를 활용한 벡터 검색으로 관련 정보만 추출하여 답변 품질과 비용 효율을 동시에 확보했습니다.',
    '{
        "techStack": ["Spring Boot", "PostgreSQL", "pgvector", "OpenAI API", "Spring AI", "JPA", "Bucket4j", "Docker", "Next.js", "TypeScript", "Biome", "Render", "Cloudflare", "Vercel"],
        "role": "풀스택 개발",
        "duration": "2026.02 - 2026.03",
        "overview": "이력서를 기반으로 사용자의 질문에 답변하는 RAG 기반 AI 챗봇",
        "challenges": [
            {
                "problem": "정적인 포트폴리오의 한계",
                "description": "기존 포트폴리오는 정보를 한 번에 나열하는 방식이라, 사용자가 원하는 내용을 빠르게 찾기 어렵습니다. 또한, 사용자별로 관심사가 다르기 때문에 정적인 구조만으로는 어필하기 어렵다고 생각했습니다.",
                "consideration": "처음에는 단순히 포트폴리오 내용을 더 보기 좋게 배치하는 방향을 생각했지만, 정보량이 많아질수록 사용자가 직접 찾아야 한다는 문제는 그대로 남는다고 판단했습니다.",
                "solution": "포트폴리오, 이력서, 경력기술서를 데이터화하고, 사용자의 질문에 맞는 내용을 찾아 답변하는 AI 기반 포트폴리오 챗봇을 만들었습니다."
            },
            {
                "problem": "AI가 내 정보를 정확하게 답변하게 만들어야 함",
                "description": "LLM은 제 포트폴리오를 학습한 모델이 아니기 때문에, 별도 처리 없이 질문만 전달하면 부정확한 답변을 할 가능성이 있었습니다.",
                "consideration": "포트폴리오 전체 내용을 프롬프트에 매번 넣는 방식으로 해결하고자 하였으나, 정보가 많아질수록 토큰 비용이 커지고 관련 없는 정보까지 함께 들어가 답변 품질이 떨어질 수 있다고 생각했습니다.",
                "solution": "질문을 임베딩으로 변환한 뒤, PostgreSQL + pgvector 환경에서 유사도 검색을 수행하고, 상위 결과만 구성해 LLM에 전달하는 RAG 패턴을 적용했습니다. 질문과 관련있는 데이터만 검색해 답변 생성에 활용하도록 설계했습니다."
            },
            {
                "problem": "답변을 자연스럽고 일관성 있게 만들기",
                "description": "초기 응답으로는 3인칭이나 형식적인 톤으로 출력되는 문제가 있었습니다. 정보만 전달하면 충분할 것이라 생각했지만, 같은 정보라도 어떤 말투와 시점으로 표현하느냐에 따라 인상이 크게 달라졌습니다.",
                "consideration": "친절하고 일관성 있게 답변을 어떻게 만들어 내야할지 고민되었습니다.",
                "solution": "시스템 프롬프트에 역할, 말투, 응답 규칙을 구조화하여 정의했습니다. 검색 결과, 프로필 정보는 일정한 템플릿으로 넣고, 답변은 1인칭 시점으로 존댓말을 사용하도록 프롬프트를 구성했습니다."
            },
            {
                "problem": "성능과 비용 사이의 균형을 맞추는 문제",
                "description": "LLM 기반 서비스는 사용하는 모델과 프롬프트 길이에 따라 비용 부담이 크게 달라졌습니다. 더 높은 성능의 모델을 적용하고 싶었지만, 실제 운영 가능성을 고려하면 비용을 무시할 수 없었습니다.",
                "consideration": "성능이 좋은 모델을 사용하면 답변 품질은 높아지지만 비용이 급격히 증가하고, 많은 컨텍스트를 포함하면 정확도는 높아질 수 있어도 토큰 사용량과 응답 시간이 함께 늘어났습니다. 개인 프로젝트 특성상 클라우드 인프라 비용도 부담이 되었기 때문에, 가능한 한 무료 또는 저비용 서비스를 중심으로 운영 방식을 설계하고자 했습니다.",
                "solution": "대화 모델로는 GPT-4o-mini를 선택하고, 최근 대화 5개만 유지하도록 제한했으며, 검색 결과 역시 상위 5개만 컨텍스트에 포함하도록 구성했습니다. 또한 전체 데이터를 매번 프롬프트에 넣지 않고, RAG 방식으로 필요한 정보만 추출해 전달함으로써 토큰 사용량을 줄였습니다. 배포는 프론트엔드에 Vercel, 백엔드에 Render를 적용해 비용을 최소화했으며, Cloudflare 스케줄링을 통해 저비용 환경에서도 안정적으로 서비스를 유지할 수 있도록 구성했습니다."
            }
        ],
        "goals": [
            "RAG 패턴 구현: PostgreSQL + pgvector로 벡터 데이터베이스 구축",
            "AI 대화 시스템 구축: OpenAI Chat API를 통합하여 1인칭 시점의 자연스러운 응답 생성",
            "Spring Boot 기반 RESTful API 개발",
            "새로운 기술 스택 학습 및 적용: Spring AI framework, LLM 활용 및 프롬프트 엔지니어링, bucket4j(트래픽 제어)"
        ],
        "retrospective": {
            "learnings": [
                "사용자에게 중요한 것은 많은 정보를 보여주는 것이 아니라, 필요한 정보를 빠르게 전달하는 경험입니다.",
                "AI 서비스의 품질은 모델보다도 검색 구조와 프롬프트 설계에 크게 좌우됩니다.",
                "개인 프로젝트라도 비용과 배포 환경까지 고려해야 실제로 운영 가능한 서비스가 됩니다.",
                "AI 서비스의 핵심은 단순히 LLM을 연결하는 것보다, 데이터를 어떻게 가공하고 검색 가능한 형태로 설계하느냐가 더 중요합니다.",
                "실제로 포트폴리오, 이력서, 경력기술서를 답변에 활용할 수 있도록 정리하고 청크 단위로 구성하는 과정에서 예상보다 많은 시간과 노력이 필요했습니다.",
                "임베딩 생성과 벡터 유사도 검색은 데이터가 늘어날수록 비용과 성능에 영향을 주기 때문에, 정확도 뿐만 아니라 운영 효율까지 함께 고려해야 했습니다.",
                "데이터 전처리, 검색 구조, 응답 품질, 비용과 성능의 균형을 함께 설계하는 과정을 체감할 수 있었습니다."
            ]
        },
        "achievements": [
            "RAG 패턴 구현으로 질문과 관련있는 정보만 검색하여 답변 생성",
            "시스템 프롬프트 구조화를 통한 1인칭 시점의 자연스러운 답변 구현",
            "GPT-4o-mini와 컨텍스트 제한으로 비용 효율 확보",
            "Vercel/Render/Cloudflare 조합으로 저비용 운영 환경 구축",
            "Spring AI framework와 pgvector를 활용한 벡터 검색 시스템 구현",
            "Bucket4j를 통한 트래픽 제어로 안정적인 서비스 운영"
        ]
    }'::jsonb,
    'project-popo-ai',
    10,
    true,
    NOW(),
    NOW()
),
(
    'project',
    '불도저(BDZ) - 주차 플랫폼',
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

-- 경력 데이터
INSERT INTO portfolio_data (type, title, content, metadata, source, priority, is_public, created_at, updated_at) VALUES
(
    'career',
    '그렉터 - 풀스택 개발자',
    '기업부설연구소 연구원으로 재직하며 IoT 플랫폼 개발 및 운영 업무를 담당하고 있습니다. 수자원/시설물 관리 웹 서비스 개발, 데이터 파이프라인 구축, 로그 모니터링 시스템 개발 등 서비스 전주기를 경험하며 운영 효율과 안정성 개선에 기여했습니다.',
    '{
        "company": "그렉터",
        "position": "풀스택 개발자",
        "department": "기업부설연구소",
        "role": "연구원",
        "duration": "2023.11 ~ 재직중",
        "totalPeriod": "2년 4개월",
        "projects": [
            {
                "name": "위험시설물(FSMS) – 최소기능 개발 및 CORS 프록시 구축",
                "period": "2025.12.01 - 2025.12.24",
                "techStack": ["Nuxt.js (Vue.js)", "Node.js", "Express", "Bootstrap", "JavaScript"],
                "achievements": [
                    "단기 일정(약 2~3주) 내 핵심 화면 기능을 우선순위 중심으로 압축 개발하여 프로젝트 목표 달성",
                    "협업팀과의 진행 현황 공유 체계를 정비해 요구사항 반영 속도 및 의사결정 효율 제고",
                    "CORS 문제 해결을 위해 UI 서버에 프록시 API를 구축하고, 클라이언트–백엔드 간 통신을 Same-Origin 기반으로 안정화",
                    "장치 ID 변경 비용이 큰 환경을 고려해 정책을 협의하고, 임시 표준안을 도출",
                    "일정 압박 상황에서 기능 고도화보다 핵심 기능 완수가 우선될 수밖에 없는 한계를 확인하고, 우선순위 높은 것을 우선처리"
                ]
            },
            {
                "name": "FSMS dfm-evt 개발 / 유지보수",
                "period": "2026.02 ~ 진행중",
                "techStack": ["JavaScript", "OpenSearch/Elasticsearch", "NoSQL", "PostgreSQL", "MQTT", "Kubernetes", "systemd"],
                "achievements": [
                    "MQTT 기반 수신되는 데이터를 처리하여 이벤트 발생 로직 및 상태 등급 산정 로직 구현 및 운영",
                    "데이터 가공 후 Opensearch 및 내부 저장소에 적재하고, oneM2M 스펙 기반 데이터 저장 규칙을 반영, 정합성 관리",
                    "장치별 수신율 평가, 시간 단위 플랫폼 통계, 이상치/최빈값 등 배치 로직 운영 및 데이터 품질 관리",
                    "Kubernetes/systemd 환경에서 로그 확인, 터널링, 포트 확인, 스냅샷, 배포 후 모니터링 등 운영 안정화"
                ]
            },
            {
                "name": "IoT 시계열 데이터 집계 / 적재 파이프라인 구축",
                "period": "2026.02 ~ 2026.03",
                "techStack": ["OpenSearch", "PostgreSQL", "Next.js", "Node.js"],
                "achievements": [
                    "OpenSearch에 저장된 10만건 이상의 데이터를 집계 및 가공하여 PostgreSQL에 적재하는 배치 파이프라인을 구축",
                    "장치별 시초가/종가/최고가/최저가의 누적값을 생성하여 캔들차트 기반 조회 지원",
                    "OpenSearch Node 연동 및 데이터 대상 데이터 이전 스크립트 설계/구현",
                    "OpenSearch aggregation 쿼리(min, max, top_hits)를 활용해 장치별 시초가, 종가, 최고가, 최저가 산출 로직 구현",
                    "PostgreSQL 누적 테이블 설계 및 전일 누적값과 당일 최종값을 반영한 적재 로직 구현",
                    "Cron 기반 배치 작업 자동화 및 Queue Delay를 활용한 대용량 작업 분산 처리",
                    "Next.js 시각화 화면과 연계할 수 있도록 운영용 데이터 조회 화면 제공"
                ]
            },
            {
                "name": "Aliot DMS Portal",
                "period": "2025.07.08 - 2025.10.18",
                "techStack": ["Next.js(App Router)", "React", "TypeScript", "Tailwind CSS", "shadcn/ui", "Node.js", "Express", "PostgreSQL", "Drizzle ORM", "React Hook Form", "Zod", "Zustand", "Biome"],
                "achievements": [
                    "IoT 센서 단말기(DMS)의 조회/등록/수정/관리 업무를 하나의 웹 시스템으로 통합하여 운영팀의 관리 동선을 단축",
                    "운영팀 업무 프로세스를 기준으로 CRUD 및 관리 화면을 설계/구현하고, 반복적인 데이터 처리 업무를 효율화",
                    "Excel 입출력 기능을 도입해 기존 현업 운영 방식과의 연계성을 높이고, 데이터 조회 및 정리 작업의 편의성을 개선",
                    "React Hook Form과 Zod 기반 입력 검증을 적용해 오입력, 결측/중복 데이터 등 데이터 품질 이슈를 줄임"
                ]
            },
            {
                "name": "경량 로그 모니터링 시스템 구축",
                "period": "2025.12 ~ 2026.01",
                "techStack": ["React", "Express", "Cron", "OpenSearch", "SFTP"],
                "achievements": [
                    "SSH 접속 중심의 로그 확인 방식을 개선하기 위해 웹 기반 로그 검색 시스템 구축",
                    "Cron이 ingest job을 실행하고, ingest job이 SFTP로 로그를 수집한 뒤 OpenSearch에 색인하는 구조 설계",
                    "Express API를 통해 시간/키워드 기반 로그 조회 기능 구현",
                    "테스트 서버에서 먼저 검증한 뒤 점진적으로 운영 확대할 수 있도록 구조 설계"
                ]
            }
        ],
        "keyAchievements": [
            "서비스 전주기 경험: 요구사항 수집부터 설계, 개발, 배포, 운영까지 전 과정 참여",
            "운영 효율화: 로그 모니터링 시스템 구축으로 수동 SSH 접속 없이 웹에서 로그 확인 가능",
            "데이터 파이프라인 구축: IoT 데이터 수집 → 가공 → 저장 전 과정 설계 및 구현",
            "운영 안정성 개선: Kubernetes/systemd 환경에서 모니터링 및 장애 대응",
            "데이터 품질 관리: 수신율 평가, 이상치 탐지, 정합성 관리 등 데이터 품질 확보"
        ]
    }'::jsonb,
    'career-grecter',
    10,
    true,
    NOW(),
    NOW()
);

-- 학력 데이터
INSERT INTO portfolio_data (type, title, content, metadata, source, priority, is_public, created_at, updated_at) VALUES
(
    'education',
    '원광대학교 컴퓨터소프트웨어공학과',
    '원광대학교 컴퓨터소프트웨어공학과 재학. 프로그래밍 기초, 자료구조, 알고리즘 등의 컴퓨터 공학 기초를 학습했습니다.',
    '{
        "school": "원광대학교",
        "major": "컴퓨터소프트웨어공학과",
        "degree": "중퇴",
        "duration": "2019.03 - 2021.02",
        "gpa": "4.03/4.5"
    }'::jsonb,
    'education-wonkwang',
    5,
    true,
    NOW(),
    NOW()
),
(
    'education',
    '한경대학교 컴퓨터공학과',
    '한경대학교 컴퓨터공학과 졸업. 알고리즘, 자료구조, 데이터베이스, 운영체제 등의 기초 CS 지식을 학습했습니다.',
    '{
        "school": "한경대학교",
        "major": "컴퓨터공학과",
        "degree": "학사",
        "duration": "2021.03 - 2023.02",
        "gpa": "3.6/4.5"
    }'::jsonb,
    'education-hankyong',
    5,
    true,
    NOW(),
    NOW()
);


// 이력서 데이터 타입
export interface Profile {
  name: string;
  title: string;
  email: string;
  summary: string;
}

export interface Experience {
  company: string;
  position: string;
  period: string;
  description: string;
  tech_stack: string[];
}

export interface Project {
  title: string;
  description: string;
  period: string;
  tech_stack: string[];
  role: string;
}

export interface SkillCategory {
  category: string;
  items: string[];
}

export interface ResumeData {
  profile: Profile;
  experiences: Experience[];
  projects: Project[];
  skills: SkillCategory[];
}

// 채팅 메시지 타입
export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  // 백엔드 오류 여부 (오류 시 채팅 버블을 연한 빨간색으로 표시)
  isError?: boolean;
  // 응답 생성 중 상태 (스피너 표시)
  isLoading?: boolean;
}

// SSE 이벤트 타입
export interface SSEEvent {
  type: 'token' | 'done';
  content?: string;
}

// API 응답 타입
export interface InitializeResponse {
  success: boolean;
  message: string;
  sectionsCreated: number;
}

// 채팅 API 응답 타입 (POST /api/chat/message)
export interface ChatAPIResponse {
  sessionId: string;
  message: string;
}

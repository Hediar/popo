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

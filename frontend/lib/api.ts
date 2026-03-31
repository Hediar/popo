import { InitializeResponse, ResumeData, SSEEvent, ChatAPIResponse } from './types';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

// 이력서 데이터 초기화
export async function initializeResume(data: ResumeData): Promise<InitializeResponse> {
  const response = await fetch(`${API_URL}/api/admin/initialize`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    throw new Error('Failed to initialize resume');
  }

  return response.json();
}

// 이력서 데이터 조회
export async function getResume(): Promise<ResumeData> {
  const response = await fetch(`${API_URL}/api/resume`);

  if (!response.ok) {
    throw new Error('Failed to fetch resume');
  }

  return response.json();
}

// SSE를 통한 채팅 스트리밍
export function createChatStream(
  question: string,
  onMessage: (content: string) => void,
  onDone: () => void,
  onError: (error: Error) => void
): EventSource {
  const encodedQuestion = encodeURIComponent(question);
  const eventSource = new EventSource(`${API_URL}/api/chat/stream?question=${encodedQuestion}`);

  eventSource.onmessage = (event) => {
    try {
      const data: SSEEvent = JSON.parse(event.data);

      if (data.type === 'token' && data.content) {
        onMessage(data.content);
      } else if (data.type === 'done') {
        eventSource.close();
        onDone();
      }
    } catch (error) {
      console.error('Error parsing SSE message:', error);
    }
  };

  eventSource.onerror = (_error) => {
    eventSource.close();
    onError(new Error('SSE connection error'));
  };

  return eventSource;
}

// 채팅 메시지 전송 (POST 기반)
export async function sendChatMessage(message: string, sessionId?: string | null): Promise<ChatAPIResponse> {
  const response = await fetch(`${API_URL}/api/chat/message`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ message, sessionId: sessionId || null }),
  });

  // 정상 응답
  if (response.ok) {
    const data = (await response.json()) as ChatAPIResponse;
    return data;
  }

  // 오류 응답 본문을 최대한 읽어 사용자에게 표시 가능하도록 처리
  try {
    const data = await response.json();
    const errorMsg = typeof data === 'object' && data && 'message' in data ? data.message : JSON.stringify(data);
    throw new Error(errorMsg || 'Chat API error');
  } catch {
    const text = await response.text();
    throw new Error(text || `HTTP ${response.status}`);
  }
}

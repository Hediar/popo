import { InitializeResponse, ResumeData, ChatAPIResponse } from './types';

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

// SSE를 통한 채팅 스트리밍 (POST 기반)
export function createChatStream(
  message: string,
  sessionId: string | null,
  onMessage: (content: string) => void,
  onSessionId: (sessionId: string) => void,
  onDone: () => void,
  onError: (error: Error) => void
): AbortController {
  const controller = new AbortController();

  (async () => {
    try {
      const response = await fetch(`${API_URL}/api/chat/stream`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message, sessionId: sessionId || null }),
        signal: controller.signal,
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error('ReadableStream not supported');
      }

      const decoder = new TextDecoder();
      let buffer = '';
      let eventName = '';
      let dataLines: string[] = [];

      const flushEvent = () => {
        const data = dataLines.join('\n');
        if (!eventName && data) {
          // 서버가 event를 안 보내는 경우, data를 토큰으로 처리
          onMessage(data);
        } else if (eventName === 'sessionId') {
          onSessionId(data);
        } else if (eventName === 'token') {
          onMessage(data);
        } else if (eventName === 'done') {
          onDone();
        } else if (eventName === 'error') {
          onError(new Error(data || 'Stream error'));
        }
        eventName = '';
        dataLines = [];
      };

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });

        // 라인 단위로 파싱하면서, 빈 줄 만나면 이벤트 flush
        const lines = buffer.split('\n');
        // 마지막 줄이 완전하지 않으면 버퍼로 남김
        buffer = lines.pop() ?? '';

        for (const rawLine of lines) {
          const line = rawLine.replace(/\r$/, '');
          if (line === '') {
            // 이벤트 경계
            flushEvent();
            continue;
          }
          if (line.startsWith('event:')) {
            eventName = line.slice(6).trim();
            continue;
          }
          if (line.startsWith('data:')) {
            const payload = line.slice(5);
            dataLines.push(payload);

            // 일부 서버가 공백 라인 없이 data만 흘려보내는 경우, 즉시 토큰 처리
            if (!eventName) {
              onMessage(payload);
              dataLines = [];
            }
            continue;
          }
        }
      }

      // 잔여 버퍼 처리
      if (buffer.length > 0 || dataLines.length > 0 || eventName) {
        flushEvent();
      }

      // 스트림이 끝났는데 done 이벤트를 못 받은 경우 안전하게 종료 신호 전파
      onDone();
    } catch (err: any) {
      if (err.name !== 'AbortError') {
        onError(err);
      }
    }
  })();

  return controller;
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

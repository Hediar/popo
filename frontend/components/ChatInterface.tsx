'use client';

import { useEffect, useRef, useState } from 'react';
import { createChatStream } from '@/lib/api';
import { ChatMessage } from '@/lib/types';
import MessageList from './MessageList';

export default function ChatInterface() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const eventSourceRef = useRef<EventSource | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // 메시지 리스트 자동 스크롤
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!input.trim() || isLoading) return;

    const userMessage: ChatMessage = {
      id: Date.now().toString(),
      role: 'user',
      content: input.trim(),
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setIsLoading(true);

    // AI 응답을 위한 빈 메시지 생성
    const assistantMessageId = (Date.now() + 1).toString();
    const assistantMessage: ChatMessage = {
      id: assistantMessageId,
      role: 'assistant',
      content: '',
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, assistantMessage]);

    // SSE 스트리밍 시작
    eventSourceRef.current = createChatStream(
      userMessage.content,
      (content) => {
        // 스트리밍 중 메시지 업데이트
        setMessages((prev) =>
          prev.map((msg) =>
            msg.id === assistantMessageId ? { ...msg, content: msg.content + content } : msg
          )
        );
      },
      () => {
        // 완료
        setIsLoading(false);
      },
      (error) => {
        // 에러 처리
        console.error('Chat error:', error);
        setMessages((prev) =>
          prev.map((msg) =>
            msg.id === assistantMessageId
              ? { ...msg, content: '죄송합니다. 오류가 발생했습니다.' }
              : msg
          )
        );
        setIsLoading(false);
      }
    );
  };

  // 컴포넌트 언마운트 시 EventSource 정리
  useEffect(() => {
    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
      }
    };
  }, []);

  return (
    <div className="flex flex-col h-screen max-w-4xl mx-auto">
      <div className="bg-blue-600 text-white p-4">
        <h1 className="text-2xl font-bold">POPO-AI 챗봇</h1>
        <p className="text-sm text-blue-100">이력서 기반 AI 챗봇에게 질문해보세요</p>
      </div>

      <MessageList messages={messages} />
      <div ref={messagesEndRef} />

      <form onSubmit={handleSubmit} className="p-4 border-t bg-white">
        <div className="flex gap-2">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="질문을 입력하세요..."
            disabled={isLoading}
            className="flex-1 px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
          />
          <button
            type="submit"
            disabled={isLoading || !input.trim()}
            className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
          >
            {isLoading ? '응답 중...' : '전송'}
          </button>
        </div>
      </form>
    </div>
  );
}

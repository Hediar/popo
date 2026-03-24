"use client";

import { useEffect, useRef, useState } from "react";
import { createChatStream, sendChatMessage } from "@/lib/api";
import { ChatMessage } from "@/lib/types";
import MessageList from "./MessageList";
import IntroShowcase from "./IntroShowcase";

interface ChatInterfaceProps {
    introImages?: string[];
}

export default function ChatInterface({ introImages }: ChatInterfaceProps) {
	const [messages, setMessages] = useState<ChatMessage[]>([]);
	const [input, setInput] = useState("");
	const [isLoading, setIsLoading] = useState(false);
	const eventSourceRef = useRef<EventSource | null>(null);
	const messagesEndRef = useRef<HTMLDivElement>(null);

	// 기본값을 POST로 사용하고, 명시적으로 true일 때만 스트리밍 사용
	const useStream = process.env.NEXT_PUBLIC_CHAT_USE_STREAM === "true";

	// 메시지 리스트 자동 스크롤
	useEffect(() => {
		messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
	});

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();

		if (!input.trim() || isLoading) return;

		const userMessage: ChatMessage = {
			id: Date.now().toString(),
			role: "user",
			content: input.trim(),
			timestamp: new Date(),
		};

		setMessages((prev) => [...prev, userMessage]);
		setInput("");
		setIsLoading(true);

		// AI 응답을 위한 빈 메시지 생성
		const assistantMessageId = (Date.now() + 1).toString();
		const assistantMessage: ChatMessage = {
			id: assistantMessageId,
			role: "assistant",
			content: useStream ? "" : "응답 생성 중...",
			timestamp: new Date(),
			isError: false,
		};

		setMessages((prev) => [...prev, assistantMessage]);

		if (useStream) {
			// SSE 스트리밍 시도
			eventSourceRef.current = createChatStream(
				userMessage.content,
				(content) => {
					setMessages((prev) =>
						prev.map((msg) =>
							msg.id === assistantMessageId
								? { ...msg, content: msg.content + content }
								: msg,
						),
					);
				},
				() => {
					setIsLoading(false);
				},
				async (_error) => {
					// 스트리밍 에러 시 POST API로 폴백
					console.error("Stream error, falling back to POST:", _error);
					eventSourceRef.current?.close();
					eventSourceRef.current = null;
					try {
						const res = await sendChatMessage(userMessage.content);
						setMessages((prev) =>
							prev.map((msg) =>
								msg.id === assistantMessageId
									? { ...msg, content: res.message, isError: false }
									: msg,
							),
						);
					} catch (err: any) {
						const errorText =
							typeof err?.message === "string" && err.message.trim().length > 0
								? err.message
								: "죄송합니다. 오류가 발생했습니다.";
						setMessages((prev) =>
							prev.map((msg) =>
								msg.id === assistantMessageId
									? { ...msg, content: errorText, isError: true }
									: msg,
							),
						);
					} finally {
						setIsLoading(false);
					}
				},
			);
		} else {
			// POST 기반 채팅 요청
			try {
				const res = await sendChatMessage(userMessage.content);
				setMessages((prev) =>
					prev.map((msg) =>
						msg.id === assistantMessageId
							? { ...msg, content: res.message, isError: false }
							: msg,
					),
				);
			} catch (err: any) {
				console.error("Chat error:", err);
				const errorText =
					typeof err?.message === "string" && err.message.trim().length > 0
						? err.message
						: "죄송합니다. 오류가 발생했습니다.";
				setMessages((prev) =>
					prev.map((msg) =>
						msg.id === assistantMessageId
							? { ...msg, content: errorText, isError: true }
							: msg,
					),
				);
			} finally {
				setIsLoading(false);
			}
		}
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
		<div className="flex h-screen w-full overflow-hidden">
			{/* Main Chat Content */}
			<main className="flex-1 flex flex-col bg-white dark:bg-background-dark/50 overflow-hidden">
				{/* Header */}
				<header className="h-16 flex-shrink-0 border-b border-slate-200 dark:border-slate-800 px-6 flex items-center justify-between">
					<div className="flex items-center gap-4">
						<div className="flex items-center gap-2">
							<div className="size-8 rounded-lg bg-primary flex items-center justify-center text-white">
								<span className="material-symbols-outlined text-[18px]">
									auto_awesome
								</span>
							</div>
							<h2 className="font-bold text-lg tracking-tight">POPO</h2>
						</div>
						<div className="hidden sm:flex items-center px-2 py-0.5 rounded-full bg-slate-100 dark:bg-slate-800 text-[10px] font-bold text-slate-500 uppercase tracking-widest">
							개발자 - 이세령
						</div>
					</div>
				</header>

				{/* Intro + Chat Area (shared scroll container) */}
				<div className="flex-1 overflow-y-auto custom-scrollbar">
					<IntroShowcase images={introImages} />
					<MessageList messages={messages} />
					<div ref={messagesEndRef} />
				</div>

				{/* Input Area */}
				<div className="p-6 border-t border-slate-200 dark:border-slate-800 flex-shrink-0">
					<div className="max-w-4xl mx-auto relative">
						<form onSubmit={handleSubmit}>
							<div className="flex items-center gap-2 p-2 bg-slate-100 dark:bg-slate-800/80 rounded-xl border border-slate-200 dark:border-slate-700 focus-within:border-primary focus-within:ring-1 focus-within:ring-primary transition-all">
								<textarea
									value={input}
									onChange={(e) => setInput(e.target.value)}
									onKeyDown={(e) => {
										if (e.key === "Enter" && !e.shiftKey) {
											e.preventDefault();
											handleSubmit(e);
										}
									}}
									disabled={isLoading}
									className="flex-1 bg-transparent border-none focus:ring-0 text-sm py-2 resize-none dark:placeholder-slate-500 focus:outline-none"
									placeholder="저에 대해 궁금한게 무엇인가요?"
									rows={1}
								/>
								<button
									type="submit"
									disabled={isLoading || !input.trim()}
									className="p-2 rounded-lg bg-primary text-white shadow-md shadow-primary/20 hover:bg-primary/90 transition-all flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed min-w-9 min-h-9"
								>
									{isLoading ? (
										<span
											className="inline-block size-5 border-2 border-white/50 border-t-white rounded-full animate-spin"
											aria-label="로딩 중"
										/>
									) : (
										<span className="material-symbols-outlined text-[20px]">
											send
										</span>
									)}
								</button>
							</div>
						</form>
						<p className="text-[10px] text-center mt-3 text-slate-400 uppercase tracking-widest font-medium">
							Career Assistant can make mistakes. Verify important information.
						</p>
					</div>
				</div>
			</main>
		</div>
	);
}

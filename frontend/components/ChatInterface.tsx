"use client";

import { useEffect, useRef, useState } from "react";
import { createChatStream } from "@/lib/api";
import { ChatMessage } from "@/lib/types";
import MessageList from "./MessageList";

export default function ChatInterface() {
	const [messages, setMessages] = useState<ChatMessage[]>([]);
	const [input, setInput] = useState("");
	const [isLoading, setIsLoading] = useState(false);
	const eventSourceRef = useRef<EventSource | null>(null);
	const messagesEndRef = useRef<HTMLDivElement>(null);

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
			content: "",
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
						msg.id === assistantMessageId
							? { ...msg, content: msg.content + content }
							: msg,
					),
				);
			},
			() => {
				// 완료
				setIsLoading(false);
			},
			(_error) => {
				// 에러 처리
				console.error("Chat error:", _error);
				setMessages((prev) =>
					prev.map((msg) =>
						msg.id === assistantMessageId
							? { ...msg, content: "죄송합니다. 오류가 발생했습니다." }
							: msg,
					),
				);
				setIsLoading(false);
			},
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
							Model v4.2
						</div>
					</div>
					<div className="flex items-center gap-3">
						<button className="p-2 rounded-lg text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors">
							<span className="material-symbols-outlined">notifications</span>
						</button>
						<button className="p-2 rounded-lg text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors">
							<span className="material-symbols-outlined">history</span>
						</button>
						<button className="p-2 rounded-lg text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors">
							<span className="material-symbols-outlined">more_vert</span>
						</button>
					</div>
				</header>

				{/* Chat Area */}
				<MessageList messages={messages} />
				<div ref={messagesEndRef} />

				{/* Input Area */}
				<div className="p-6 border-t border-slate-200 dark:border-slate-800 flex-shrink-0">
					<div className="max-w-4xl mx-auto relative">
						<form onSubmit={handleSubmit}>
							<div className="flex items-center gap-2 p-2 bg-slate-100 dark:bg-slate-800/80 rounded-xl border border-slate-200 dark:border-slate-700 focus-within:border-primary focus-within:ring-1 focus-within:ring-primary transition-all">
								<button
									type="button"
									className="p-2 rounded-lg text-slate-500 hover:text-primary transition-colors"
								>
									<span className="material-symbols-outlined">attach_file</span>
								</button>
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
									placeholder="Ask about your resume, skills, or projects..."
									rows={1}
								/>
								<button
									type="submit"
									disabled={isLoading || !input.trim()}
									className="p-2 rounded-lg bg-primary text-white shadow-md shadow-primary/20 hover:bg-primary/90 transition-all flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
								>
									<span className="material-symbols-outlined text-[20px]">
										send
									</span>
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

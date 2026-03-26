"use client";

import React from "react";

interface IntroShowcaseProps {
	images?: string[]; // full url paths like /public/... (also used for files)
}

export default function IntroShowcase({
	images: imagePaths,
}: IntroShowcaseProps) {
	// Fallback to an empty list if not provided
	const files = (imagePaths || []).map((src) => {
		const encoded = encodeURI(src);
		const name = decodeURI(src.split("/").pop() || src);
		return { src: encoded, name };
	});
	return (
		<div className="p-6">
			<div className="max-w-4xl mx-auto space-y-8">
				<section className="bg-slate-50 dark:bg-slate-900/40 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
					<h1 className="text-xl font-bold mb-3">
						안녕하세요! 저는 이세령입니다.
					</h1>
					<div className="text-sm text-slate-700 dark:text-slate-300 whitespace-pre-line leading-relaxed">
						{`풀스택 개발자로서 현재 그렉터에서 일하고 있으며, 약 2년 4개월의 경력을 가지고 있습니다. 한경대학교에서 컴퓨터공학을 전공했어요.

IoT 수자원 및 시설물 운영 웹 서비스를 개발하고 운영하며 서비스 전주기를 경험했습니다. 
이 과정에서 경량 모니터링 시스템을 구축해 오류를 쉽게 확인할 수 있는 환경을 만들었고, 데이터 수집에서 가공, 저장까지 모든 과정을 관리하여 안정적인 데이터 플로우 운영에 기여했습니다.

주요 기술 스택
- JavaScript, TypeScript, Node.js
- Next.js, React, Vue, PostgreSQL, Opensearch/Elasticsearch, Linux
- Spring Boot, Java, Redis, Docker

자격증
- 정보처리기사
- SQLD

데이터베이스, 인프라에도 관심이 있습니다!`}
					</div>
				</section>

				<section>
					<div className="flex items-center justify-between mb-3">
						<h2 className="text-lg font-bold">자료 다운로드</h2>
						<span className="text-[10px] text-slate-500 uppercase tracking-widest">attachments</span>
					</div>
					{files.length > 0 ? (
						<ul className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
							{files.map((file) => (
								<li
									key={file.src}
									className="rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-3 flex items-center justify-between gap-3"
									title={file.name}
								>
									<div className="min-w-0">
										<p className="text-xs text-slate-600 dark:text-slate-400 truncate">{file.name}</p>
									</div>
									<a
										href={file.src}
										download={file.name}
										className="shrink-0 inline-flex items-center gap-1 px-2 py-1.5 rounded-md text-xs font-medium bg-primary text-white hover:bg-primary/90 transition-colors"
									>
										<span className="material-symbols-outlined text-[16px]">download</span>
										다운로드
									</a>
								</li>
							))}
						</ul>
					) : (
						<div className="text-sm text-slate-500 dark:text-slate-400" />
					)}
				</section>
			</div>
		</div>
	);
}

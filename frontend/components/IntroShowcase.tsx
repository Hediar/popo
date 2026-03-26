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
		const lastSlash = src.lastIndexOf("/");
		const prefix = lastSlash >= 0 ? src.slice(0, lastSlash + 1) : "";
		const rawName = lastSlash >= 0 ? src.slice(lastSlash + 1) : src;
		let displayName = rawName;
		try {
			displayName = decodeURIComponent(rawName);
		} catch {
			// keep rawName if not URI-encoded
		}
		// Encode only the filename to ensure special chars like '+' are safe
		const encodedName = encodeURIComponent(displayName);
		const href = `${prefix}${encodedName}`;
		return { src: href, name: displayName };
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
`}
					</div>
				</section>
			</div>
		</div>
	);
}

import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'POPO-AI - Career AI Assistant',
  description: '이력서 기반 AI 챗봇 시스템',
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko" className="dark">
      <head>
        <link
          href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght@100..700&display=swap"
          rel="stylesheet"
        />
      </head>
      <body className="antialiased overflow-hidden">{children}</body>
    </html>
  );
}

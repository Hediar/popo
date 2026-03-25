'use client';

import { ChatMessage } from '@/lib/types';

interface MessageListProps {
  messages: ChatMessage[];
}

export default function MessageList({ messages }: MessageListProps) {
  return (
    <div className="p-6 space-y-6">
      {messages.map((message) => {
        const isUser = message.role === 'user';
        const isError = !!message.isError;
        const timeString = message.timestamp.toLocaleTimeString('en-US', {
          hour: '2-digit',
          minute: '2-digit',
          hour12: true,
        });
        // Replace literal "\n" with actual newlines for display
        const formattedContent = (message.content || '').replaceAll('\\n', '\n');

        if (isUser) {
          return (
            <div key={message.id} className="flex gap-4 max-w-3xl ml-auto flex-row-reverse">
              <div className="flex-shrink-0 size-10 rounded-full overflow-hidden border border-primary/20 shadow-lg shadow-primary/10 bg-primary/20 flex items-center justify-center">
                <span className="text-primary font-bold text-lg">U</span>
              </div>
              <div className="space-y-1 flex flex-col items-end">
                <div className="flex items-center gap-2">
                  <span className="text-[10px] text-slate-400">{timeString}</span>
                  <span className="text-xs font-bold text-slate-500 dark:text-slate-400">User</span>
                </div>
                <div className="bg-primary text-white p-4 rounded-2xl rounded-tr-none shadow-lg shadow-primary/20 text-sm leading-relaxed whitespace-pre-line">
                  {formattedContent}
                </div>
              </div>
            </div>
          );
        }

        return (
          <div key={message.id} className="flex gap-4 max-w-3xl">
            <div className="flex-shrink-0 size-10 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center text-primary border border-slate-200 dark:border-slate-700">
              <span className="material-symbols-outlined text-[20px]">smart_toy</span>
            </div>
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold text-slate-500 dark:text-slate-400">
                  Career Bot
                </span>
                <span className="text-[10px] text-slate-400">{timeString}</span>
              </div>
              <div
                className={
                  `${
                    isError
                      ? 'bg-red-100 border border-red-200 text-red-900 dark:bg-red-950/40 dark:border-red-800'
                      : 'bg-slate-100 dark:bg-slate-800'
                  } p-4 rounded-2xl rounded-tl-none shadow-sm text-sm leading-relaxed whitespace-pre-line`
                }
              >
                {formattedContent}
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}

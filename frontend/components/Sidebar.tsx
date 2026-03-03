'use client';

export default function Sidebar() {
  const recentChats = [
    'Microservices Architecture Review',
    'Senior Frontend Dev Prep',
    'Salary Negotiation Strategy',
  ];

  return (
    <aside className="w-80 flex-shrink-0 border-r border-slate-200 dark:border-slate-800 bg-white dark:bg-background-dark flex flex-col">
      {/* Profile Header */}
      <div className="p-6 border-b border-slate-200 dark:border-slate-800">
        <div className="flex items-center gap-3">
          <div className="relative">
            <div className="size-11 rounded-full bg-primary/20 flex items-center justify-center border border-primary/30 overflow-hidden">
              <span className="text-primary text-xl font-bold">P</span>
            </div>
            <div className="absolute bottom-0 right-0 size-3 bg-green-500 rounded-full border-2 border-white dark:border-background-dark"></div>
          </div>
          <div className="flex flex-col">
            <h1 className="text-sm font-semibold leading-none">POPO User</h1>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">Premium Plan</p>
          </div>
        </div>
      </div>

      {/* Navigation Links */}
      <nav className="flex-1 overflow-y-auto p-4 flex flex-col gap-1 custom-scrollbar">
        <div className="text-[11px] font-bold text-slate-400 dark:text-slate-500 uppercase tracking-wider px-3 mb-2 mt-2">
          Tools
        </div>
        <a
          href="#"
          className="flex items-center gap-3 px-3 py-2.5 rounded-lg bg-primary text-white shadow-lg shadow-primary/20"
        >
          <span className="material-symbols-outlined text-[20px]">bar_chart</span>
          <span className="text-sm font-medium">Skills Gap Analysis</span>
        </a>
        <a
          href="#"
          className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
        >
          <span className="material-symbols-outlined text-[20px]">folder_open</span>
          <span className="text-sm font-medium">Projects Review</span>
        </a>
        <a
          href="#"
          className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
        >
          <span className="material-symbols-outlined text-[20px]">description</span>
          <span className="text-sm font-medium">Resume Tailoring</span>
        </a>

        <div className="text-[11px] font-bold text-slate-400 dark:text-slate-500 uppercase tracking-wider px-3 mb-2 mt-6">
          Recent Chats
        </div>
        <div className="space-y-1">
          {recentChats.map((chat, index) => (
            <button
              key={index}
              className="w-full text-left px-3 py-2 rounded-lg text-xs text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 truncate"
            >
              {chat}
            </button>
          ))}
        </div>
      </nav>

      {/* Bottom Actions */}
      <div className="p-4 border-t border-slate-200 dark:border-slate-800">
        <button className="w-full flex items-center gap-3 px-3 py-2 text-slate-600 dark:text-slate-400 hover:text-primary dark:hover:text-primary transition-colors">
          <span className="material-symbols-outlined text-[20px]">settings</span>
          <span className="text-sm font-medium">Settings</span>
        </button>
        <button className="w-full flex items-center gap-3 px-3 py-2 text-slate-600 dark:text-slate-400 hover:text-primary dark:hover:text-primary transition-colors">
          <span className="material-symbols-outlined text-[20px]">help_outline</span>
          <span className="text-sm font-medium">Help Center</span>
        </button>
      </div>
    </aside>
  );
}

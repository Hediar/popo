"use client";

import React from "react";

interface MarkdownTextProps {
  text: string;
}

// Minimal inline markdown renderer supporting **bold**, *italic*, `code`, and [link](url)
export default function MarkdownText({ text }: MarkdownTextProps) {
  const lines = text.split(/\n/);

  const renderInline = (input: string): React.ReactNode[] => {
    const nodes: React.ReactNode[] = [];
    let i = 0;

    const patterns = [
      { type: "link" as const, re: /\[(.+?)\]\((https?:\/\/[\w.-]+(?:\/[\w\-.~:%/?#[\]@!$&'()*+,;=]*)?)\)/ },
      { type: "code" as const, re: /`([^`]+)`/ },
      { type: "bold" as const, re: /\*\*([^*]+)\*\*/ },
      { type: "italic" as const, re: /\*([^*]+)\*/ },
    ];

    while (i < input.length) {
      let earliest: { type: typeof patterns[number]["type"]; start: number; end: number; groups: string[] } | null = null;

      for (const p of patterns) {
        p.re.lastIndex = 0; // ensure fresh search on slice
        const slice = input.slice(i);
        const m = p.re.exec(slice);
        if (m) {
          const start = i + m.index;
          const end = start + m[0].length;
          if (!earliest || start < earliest.start) {
            earliest = { type: p.type, start, end, groups: m.slice(1) };
          }
        }
      }

      if (!earliest) {
        // No more matches; push the rest as text
        nodes.push(input.slice(i));
        break;
      }

      if (earliest.start > i) {
        nodes.push(input.slice(i, earliest.start));
      }

      const key = nodes.length;
      const [g1, g2] = earliest.groups;
      switch (earliest.type) {
        case "link":
          nodes.push(
            <a key={`l-${key}`} href={g2} target="_blank" rel="noreferrer" className="underline text-blue-600 dark:text-blue-400">
              {g1}
            </a>,
          );
          break;
        case "code":
          nodes.push(
            <code key={`c-${key}`} className="px-1 py-0.5 rounded bg-slate-200/70 dark:bg-slate-700/60 text-[0.9em]">
              {g1}
            </code>,
          );
          break;
        case "bold":
          nodes.push(
            <strong key={`b-${key}`} className="font-bold">
              {g1}
            </strong>,
          );
          break;
        case "italic":
          nodes.push(
            <em key={`i-${key}`} className="italic">
              {g1}
            </em>,
          );
          break;
      }

      i = earliest.end;
    }

    return nodes;
  };

  return (
    <div className="leading-relaxed text-sm">
      {lines.map((line, idx) => (
        <span key={idx}>
          {renderInline(line)}
          {idx < lines.length - 1 ? <br /> : null}
        </span>
      ))}
    </div>
  );
}


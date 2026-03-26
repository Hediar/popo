import ChatInterface from '@/components/ChatInterface';
import fs from 'fs';
import path from 'path';

export default function Home() {
  // Collect PDF files from public/introduce_files
  const filesDir = path.join(process.cwd(), 'public', 'introduce_files');
  let introImages: string[] = [];
  try {
    const files = fs.readdirSync(filesDir, { withFileTypes: true });
    introImages = files
      .filter((f) => f.isFile())
      .map((f) => f.name)
      // Include only PDFs
      .filter((name) => /\.pdf$/i.test(name))
      .map((name) => `/introduce_files/${name}`);
  } catch {
    // directory may not exist; keep empty list
    introImages = [];
  }
  return <ChatInterface introImages={introImages} />;
}

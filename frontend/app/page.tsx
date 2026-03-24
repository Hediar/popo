import ChatInterface from '@/components/ChatInterface';
import fs from 'fs';
import path from 'path';

export default function Home() {
  // Collect image list from public/introduce_images on the server
  const imagesDir = path.join(process.cwd(), 'public', 'introduce_images');
  let introImages: string[] = [];
  try {
    const files = fs.readdirSync(imagesDir, { withFileTypes: true });
    introImages = files
      .filter((f) => f.isFile())
      .map((f) => f.name)
      .filter((name) => /\.(png|jpe?g|webp|gif|svg)$/i.test(name))
      .map((name) => `/introduce_images/${name}`);
  } catch {
    // directory may not exist; keep empty list
    introImages = [];
  }
  return <ChatInterface introImages={introImages} />;
}

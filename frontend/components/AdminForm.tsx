'use client';

import { useState } from 'react';
import { initializeResume } from '@/lib/api';
import { Experience, Profile, Project, ResumeData, SkillCategory } from '@/lib/types';

export default function AdminForm() {
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState('');

  const [profile, setProfile] = useState<Profile>({
    name: '',
    title: '',
    email: '',
    summary: '',
  });

  const [experiences, setExperiences] = useState<Experience[]>([
    {
      company: '',
      position: '',
      period: '',
      description: '',
      tech_stack: [],
    },
  ]);

  const [projects, _setProjects] = useState<Project[]>([
    {
      title: '',
      description: '',
      period: '',
      tech_stack: [],
      role: '',
    },
  ]);

  const [skills, _setSkills] = useState<SkillCategory[]>([
    {
      category: '',
      items: [],
    },
  ]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setMessage('');

    try {
      const data: ResumeData = {
        profile,
        experiences: experiences.filter((exp) => exp.company.trim() !== ''),
        projects: projects.filter((proj) => proj.title.trim() !== ''),
        skills: skills.filter((skill) => skill.category.trim() !== ''),
      };

      const response = await initializeResume(data);
      setMessage(`✓ ${response.message} (${response.sectionsCreated}개 섹션 생성됨)`);
    } catch (error) {
      setMessage('✗ 오류가 발생했습니다. 다시 시도해주세요.');
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-2xl font-bold mb-6">이력서 데이터 등록</h2>

        {message && (
          <div
            className={`mb-4 p-4 rounded-lg ${
              message.startsWith('✓') ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
            }`}
          >
            {message}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* 프로필 섹션 */}
          <div className="border rounded-lg p-4">
            <h3 className="text-lg font-semibold mb-4">프로필</h3>
            <div className="space-y-4">
              <input
                type="text"
                placeholder="이름"
                value={profile.name}
                onChange={(e) => setProfile({ ...profile, name: e.target.value })}
                className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
              <input
                type="text"
                placeholder="직무 (예: 백엔드 개발자)"
                value={profile.title}
                onChange={(e) => setProfile({ ...profile, title: e.target.value })}
                className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
              <input
                type="email"
                placeholder="이메일"
                value={profile.email}
                onChange={(e) => setProfile({ ...profile, email: e.target.value })}
                className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
              <textarea
                placeholder="요약 (간단한 자기소개)"
                value={profile.summary}
                onChange={(e) => setProfile({ ...profile, summary: e.target.value })}
                rows={4}
                className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
          </div>

          {/* 경력 섹션 */}
          <div className="border rounded-lg p-4">
            <h3 className="text-lg font-semibold mb-4">경력</h3>
            {experiences.map((exp, index) => (
              <div key={index} className="mb-4 p-4 bg-gray-50 rounded-lg space-y-3">
                <input
                  type="text"
                  placeholder="회사명"
                  value={exp.company}
                  onChange={(e) => {
                    const newExps = [...experiences];
                    newExps[index].company = e.target.value;
                    setExperiences(newExps);
                  }}
                  className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <input
                  type="text"
                  placeholder="직책"
                  value={exp.position}
                  onChange={(e) => {
                    const newExps = [...experiences];
                    newExps[index].position = e.target.value;
                    setExperiences(newExps);
                  }}
                  className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <input
                  type="text"
                  placeholder="기간 (예: 2022.01 ~ 2023.12)"
                  value={exp.period}
                  onChange={(e) => {
                    const newExps = [...experiences];
                    newExps[index].period = e.target.value;
                    setExperiences(newExps);
                  }}
                  className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <textarea
                  placeholder="업무 설명"
                  value={exp.description}
                  onChange={(e) => {
                    const newExps = [...experiences];
                    newExps[index].description = e.target.value;
                    setExperiences(newExps);
                  }}
                  rows={3}
                  className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <input
                  type="text"
                  placeholder="기술 스택 (쉼표로 구분, 예: Java, Spring Boot, Redis)"
                  value={exp.tech_stack.join(', ')}
                  onChange={(e) => {
                    const newExps = [...experiences];
                    newExps[index].tech_stack = e.target.value
                      .split(',')
                      .map((s) => s.trim())
                      .filter((s) => s);
                    setExperiences(newExps);
                  }}
                  className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            ))}
            <button
              type="button"
              onClick={() =>
                setExperiences([
                  ...experiences,
                  { company: '', position: '', period: '', description: '', tech_stack: [] },
                ])
              }
              className="text-blue-500 hover:text-blue-600"
            >
              + 경력 추가
            </button>
          </div>

          <div className="flex gap-4">
            <button
              type="submit"
              disabled={isLoading}
              className="flex-1 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors font-semibold"
            >
              {isLoading ? '등록 중...' : '이력서 등록'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

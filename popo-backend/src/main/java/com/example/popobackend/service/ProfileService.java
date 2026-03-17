package com.example.popobackend.service;

import com.example.popobackend.entity.Profile;
import com.example.popobackend.repository.ProfileRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    /**
     * 활성화된 프로필 조회
     */
    public Optional<Profile> getActiveProfile() {
        return profileRepository.findFirstByIsActiveTrue();
    }

    /**
     * 프로필 정보를 프롬프트용 텍스트로 변환
     * AI에게 전달할 기본 정보 생성
     */
    public String buildProfileContext() {
        Optional<Profile> profileOpt = getActiveProfile();

        if (profileOpt.isEmpty()) {
            return "기본 프로필 정보가 설정되지 않았습니다.";
        }

        Profile profile = profileOpt.get();
        StringBuilder context = new StringBuilder();

        context.append("=== 포트폴리오 주인 기본 정보 ===\n\n");

        // 이름
        if (profile.getName() != null && !profile.getName().isEmpty()) {
            context.append("이름: ").append(profile.getName()).append("\n");
        }

        // 직업
        if (profile.getOccupation() != null && !profile.getOccupation().isEmpty()) {
            context.append("직업: ").append(profile.getOccupation()).append("\n");
        }

        // 경력
        if (profile.getExperience() != null && !profile.getExperience().isEmpty()) {
            context.append("경력: ").append(profile.getExperience()).append("\n");
        }

        // 현재 회사
        if (profile.getCurrentCompany() != null && !profile.getCurrentCompany().isEmpty()) {
            context.append("현재 회사: ").append(profile.getCurrentCompany()).append("\n");
        }

        // 학력
        if (profile.getEducation() != null && !profile.getEducation().isEmpty()) {
            context.append("학력: ").append(profile.getEducation()).append("\n");
        }

        // 한 줄 소개
        if (profile.getIntroduction() != null && !profile.getIntroduction().isEmpty()) {
            context.append("\n소개: ").append(profile.getIntroduction()).append("\n");
        }

        // 기술 스택 (JSONB 파싱)
        if (profile.getTechStack() != null && !profile.getTechStack().isEmpty()) {
            context.append("\n주요 기술 스택:\n");
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, List<String>> techStack = mapper.readValue(
                    profile.getTechStack(),
                    new TypeReference<Map<String, List<String>>>() {}
                );

                // 전문가 수준
                if (techStack.containsKey("expert") && !techStack.get("expert").isEmpty()) {
                    context.append("- 전문가 수준: ")
                           .append(String.join(", ", techStack.get("expert")))
                           .append("\n");
                }

                // 능숙한 수준
                if (techStack.containsKey("proficient") && !techStack.get("proficient").isEmpty()) {
                    context.append("- 능숙한 수준: ")
                           .append(String.join(", ", techStack.get("proficient")))
                           .append("\n");
                }

                // 경험 있음
                if (techStack.containsKey("familiar") && !techStack.get("familiar").isEmpty()) {
                    context.append("- 경험 있음: ")
                           .append(String.join(", ", techStack.get("familiar")))
                           .append("\n");
                }
            } catch (Exception e) {
                // JSON 파싱 실패 시 원본 그대로 표시
                context.append(profile.getTechStack()).append("\n");
            }
        }

        // 관심 분야
        if (profile.getInterests() != null && !profile.getInterests().isEmpty()) {
            context.append("\n관심 분야: ").append(profile.getInterests()).append("\n");
        }

        // 자격증 (JSONB 파싱)
        if (profile.getCertifications() != null && !profile.getCertifications().isEmpty()) {
            context.append("\n자격증:\n");
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, String>> certifications = mapper.readValue(
                    profile.getCertifications(),
                    new TypeReference<List<Map<String, String>>>() {}
                );

                for (Map<String, String> cert : certifications) {
                    String name = cert.getOrDefault("name", "");
                    String issuer = cert.getOrDefault("issuer", "");
                    String date = cert.getOrDefault("date", "");

                    if (!name.isEmpty()) {
                        context.append("- ").append(name);
                        if (!issuer.isEmpty()) {
                            context.append(" (").append(issuer).append(")");
                        }
                        if (!date.isEmpty()) {
                            context.append(" - ").append(date);
                        }
                        context.append("\n");
                    }
                }
            } catch (Exception e) {
                // JSON 파싱 실패 시 원본 그대로 표시
                context.append(profile.getCertifications()).append("\n");
            }
        }

        // 연락처 및 링크
        context.append("\n연락처 및 링크:\n");
        if (profile.getEmail() != null && !profile.getEmail().isEmpty()) {
            context.append("- 이메일: ").append(profile.getEmail()).append("\n");
        }
        if (profile.getGithubUrl() != null && !profile.getGithubUrl().isEmpty()) {
            context.append("- GitHub: ").append(profile.getGithubUrl()).append("\n");
        }
        if (profile.getBlogUrl() != null && !profile.getBlogUrl().isEmpty()) {
            context.append("- 블로그: ").append(profile.getBlogUrl()).append("\n");
        }

        context.append("\n");

        return context.toString();
    }
}

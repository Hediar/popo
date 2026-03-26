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

        // 기본 인적사항
        if (profile.getName() != null && !profile.getName().isEmpty()) {
            context.append("이름: ").append(profile.getName()).append("\n");
        }
        if (profile.getOccupation() != null && !profile.getOccupation().isEmpty()) {
            context.append("직업: ").append(profile.getOccupation()).append("\n");
        }
        if (profile.getExperience() != null && !profile.getExperience().isEmpty()) {
            context.append("경력: ").append(profile.getExperience()).append("\n");
        }
        if (profile.getCurrentCompany() != null && !profile.getCurrentCompany().isEmpty()) {
            context.append("현재 회사: ").append(profile.getCurrentCompany()).append("\n");
        }
        if (profile.getEducation() != null && !profile.getEducation().isEmpty()) {
            context.append("학력: ").append(profile.getEducation()).append("\n");
        }
        if (profile.getIntroduction() != null && !profile.getIntroduction().isEmpty()) {
            context.append("소개: ").append(profile.getIntroduction()).append("\n");
        }

        // 기술 스택
        if (profile.getTechStack() != null && !profile.getTechStack().isEmpty()) {
            context.append("기술스택: ");
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, List<String>> techStack = mapper.readValue(
                    profile.getTechStack(),
                    new TypeReference<Map<String, List<String>>>() {}
                );

                if (techStack.containsKey("expert") && !techStack.get("expert").isEmpty()) {
                    context.append("전문(").append(String.join(", ", techStack.get("expert"))).append(") ");
                }
                if (techStack.containsKey("proficient") && !techStack.get("proficient").isEmpty()) {
                    context.append("능숙(").append(String.join(", ", techStack.get("proficient"))).append(") ");
                }
                if (techStack.containsKey("familiar") && !techStack.get("familiar").isEmpty()) {
                    context.append("경험(").append(String.join(", ", techStack.get("familiar"))).append(")");
                }
                context.append("\n");
            } catch (Exception e) {
                context.append(profile.getTechStack()).append("\n");
            }
        }

        // 관심 분야
        if (profile.getInterests() != null && !profile.getInterests().isEmpty()) {
            context.append("관심분야: ").append(profile.getInterests()).append("\n");
        }

        // 자격증
        if (profile.getCertifications() != null && !profile.getCertifications().isEmpty()) {
            context.append("자격증: ");
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, String>> certifications = mapper.readValue(
                    profile.getCertifications(),
                    new TypeReference<List<Map<String, String>>>() {}
                );

                List<String> certNames = certifications.stream()
                    .map(cert -> cert.getOrDefault("name", ""))
                    .filter(name -> !name.isEmpty())
                    .toList();
                context.append(String.join(", ", certNames)).append("\n");
            } catch (Exception e) {
                context.append(profile.getCertifications()).append("\n");
            }
        }

        // 연락처
        if (profile.getEmail() != null && !profile.getEmail().isEmpty()) {
            context.append("이메일: ").append(profile.getEmail()).append("\n");
        }
        if (profile.getGithubUrl() != null && !profile.getGithubUrl().isEmpty()) {
            context.append("GitHub: ").append(profile.getGithubUrl()).append("\n");
        }
        if (profile.getBlogUrl() != null && !profile.getBlogUrl().isEmpty()) {
            context.append("블로그: ").append(profile.getBlogUrl()).append("\n");
        }

        return context.toString();
    }
}

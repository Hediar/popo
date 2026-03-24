package com.example.popobackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:}")
    private String allowedOriginsConfig;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 기본 허용 패턴 (localhost)
        java.util.List<String> allowedPatterns = new java.util.ArrayList<>();
        allowedPatterns.add("http://localhost:*");
        allowedPatterns.add("http://127.0.0.1:*");
        allowedPatterns.add("https://localhost:*");
        allowedPatterns.add("https://127.0.0.1:*");

        // 환경변수로 설정된 배포 URL 추가
        if (allowedOriginsConfig != null && !allowedOriginsConfig.isEmpty()) {
            String[] configuredOrigins = allowedOriginsConfig.split(",");
            for (String origin : configuredOrigins) {
                String trimmed = origin.trim();
                if (!trimmed.isEmpty()) {
                    allowedPatterns.add(trimmed);
                }
            }
        }

        registry.addMapping("/api/**")
            .allowedOriginPatterns(allowedPatterns.toArray(new String[0]))
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}

package com.example.popobackend.util;

import com.example.popobackend.entity.PortfolioData;
import com.example.popobackend.repository.PortfolioDataRepository;
import com.example.popobackend.service.EmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 애플리케이션 시작 시 임베딩 재생성
 * 활성화하려면 @Component 주석 해제
 */
// @Component  // 필요할 때만 주석 해제 (title+content+metadata 임베딩 재생성 완료)
public class EmbeddingRegenerationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingRegenerationRunner.class);

    @Autowired
    private PortfolioDataRepository portfolioDataRepository;

    @Autowired
    private EmbeddingService embeddingService;

    @Override
    public void run(String... args) {
        log.info("======================================");
        log.info("임베딩 재생성 시작");
        log.info("======================================");

        try {
            List<PortfolioData> allData = portfolioDataRepository.findAll();
            log.info("총 {}개 항목 발견", allData.size());

            int successCount = 0;
            int failCount = 0;

            for (PortfolioData data : allData) {
                try {
                    // title + content + metadata 모두 임베딩 생성
                    String text = embeddingService.formatForEmbedding(
                        data.getTitle(),
                        data.getContent(),
                        data.getMetadata()
                    );

                    if (text == null || text.trim().isEmpty()) {
                        log.warn("ID={}: 빈 텍스트, 스킵", data.getId());
                        failCount++;
                        continue;
                    }

                    log.info("ID={}: 임베딩 생성 중... (type={}, title={})",
                        data.getId(), data.getType(), data.getTitle());

                    float[] embedding = embeddingService.createEmbedding(text);

                    if (embedding != null && embedding.length == 1536) {
                        data.setEmbedding(embedding);
                        portfolioDataRepository.save(data);
                        successCount++;
                        log.info("ID={}: ✓ 임베딩 저장 완료 (dimension={})", data.getId(), embedding.length);
                    } else {
                        log.error("ID={}: ✗ 임베딩 생성 실패 (null 또는 차원 불일치)", data.getId());
                        failCount++;
                    }

                    // API Rate Limit 방지 (OpenAI: 3000 RPM)
                    Thread.sleep(100);

                } catch (Exception e) {
                    log.error("ID={}: ✗ 에러 - {}", data.getId(), e.getMessage());
                    failCount++;
                }
            }

            log.info("======================================");
            log.info("임베딩 재생성 완료");
            log.info("성공: {}개, 실패: {}개", successCount, failCount);
            log.info("======================================");

        } catch (Exception e) {
            log.error("임베딩 재생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}

package com.example.popobackend.service;

import com.example.popobackend.entity.PortfolioData;
import com.example.popobackend.repository.PortfolioDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 포트폴리오 데이터의 임베딩을 재생성하는 서비스
 */
@Service
public class EmbeddingRegenerationService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingRegenerationService.class);

    @Autowired
    private PortfolioDataRepository portfolioDataRepository;

    @Autowired
    private EmbeddingService embeddingService;

    /**
     * 모든 포트폴리오 데이터의 임베딩 재생성
     *
     * @return 재생성된 항목 수
     */
    @Transactional
    public int regenerateAllEmbeddings() {
        log.info("[EmbeddingRegeneration] 전체 임베딩 재생성 시작");

        List<PortfolioData> allData = portfolioDataRepository.findAll();
        log.info("[EmbeddingRegeneration] 총 {}개 항목 발견", allData.size());

        int successCount = 0;
        int failCount = 0;

        for (PortfolioData data : allData) {
            try {
                // 제목 + 내용을 결합하여 임베딩 생성
                String text = embeddingService.formatForEmbedding(data.getTitle(), data.getContent());

                if (text == null || text.trim().isEmpty()) {
                    log.warn("[EmbeddingRegeneration] ID={}: 빈 텍스트, 스킵", data.getId());
                    failCount++;
                    continue;
                }

                log.info("[EmbeddingRegeneration] ID={}: 임베딩 생성 중... (type={}, title={})",
                    data.getId(), data.getType(), data.getTitle());

                float[] embedding = embeddingService.createEmbedding(text);

                if (embedding != null && embedding.length == 1536) {
                    data.setEmbedding(embedding);
                    portfolioDataRepository.save(data);
                    successCount++;
                    log.info("[EmbeddingRegeneration] ID={}: 임베딩 저장 완료 (dimension={})",
                        data.getId(), embedding.length);
                } else {
                    log.error("[EmbeddingRegeneration] ID={}: 임베딩 생성 실패 (null 또는 차원 불일치)", data.getId());
                    failCount++;
                }

            } catch (Exception e) {
                log.error("[EmbeddingRegeneration] ID={}: 임베딩 생성 중 에러 - {}",
                    data.getId(), e.getMessage());
                failCount++;
            }
        }

        log.info("[EmbeddingRegeneration] 완료: 성공 {}개, 실패 {}개", successCount, failCount);
        return successCount;
    }

    /**
     * 임베딩이 없는 항목만 재생성
     *
     * @return 재생성된 항목 수
     */
    @Transactional
    public int regenerateMissingEmbeddings() {
        log.info("[EmbeddingRegeneration] 임베딩 누락 항목 재생성 시작");

        List<PortfolioData> missingData = portfolioDataRepository.findByEmbeddingIsNull();
        log.info("[EmbeddingRegeneration] 임베딩 누락 항목: {}개", missingData.size());

        int successCount = 0;
        int failCount = 0;

        for (PortfolioData data : missingData) {
            try {
                String text = embeddingService.formatForEmbedding(data.getTitle(), data.getContent());

                if (text == null || text.trim().isEmpty()) {
                    log.warn("[EmbeddingRegeneration] ID={}: 빈 텍스트, 스킵", data.getId());
                    failCount++;
                    continue;
                }

                log.info("[EmbeddingRegeneration] ID={}: 임베딩 생성 중... (type={}, title={})",
                    data.getId(), data.getType(), data.getTitle());

                float[] embedding = embeddingService.createEmbedding(text);

                if (embedding != null && embedding.length == 1536) {
                    data.setEmbedding(embedding);
                    portfolioDataRepository.save(data);
                    successCount++;
                    log.info("[EmbeddingRegeneration] ID={}: 임베딩 저장 완료", data.getId());
                } else {
                    log.error("[EmbeddingRegeneration] ID={}: 임베딩 생성 실패", data.getId());
                    failCount++;
                }

            } catch (Exception e) {
                log.error("[EmbeddingRegeneration] ID={}: 에러 - {}", data.getId(), e.getMessage());
                failCount++;
            }
        }

        log.info("[EmbeddingRegeneration] 완료: 성공 {}개, 실패 {}개", successCount, failCount);
        return successCount;
    }

    /**
     * 특정 타입의 임베딩만 재생성
     *
     * @param type 타입 (project, career, education, etc.)
     * @return 재생성된 항목 수
     */
    @Transactional
    public int regenerateEmbeddingsByType(String type) {
        log.info("[EmbeddingRegeneration] type='{}' 임베딩 재생성 시작", type);

        List<PortfolioData> dataByType = portfolioDataRepository.findByTypeAndIsPublicTrue(type);
        log.info("[EmbeddingRegeneration] type='{}' 항목: {}개", type, dataByType.size());

        int successCount = 0;
        int failCount = 0;

        for (PortfolioData data : dataByType) {
            try {
                String text = embeddingService.formatForEmbedding(data.getTitle(), data.getContent());

                if (text == null || text.trim().isEmpty()) {
                    log.warn("[EmbeddingRegeneration] ID={}: 빈 텍스트, 스킵", data.getId());
                    failCount++;
                    continue;
                }

                log.info("[EmbeddingRegeneration] ID={}: 임베딩 생성 중... (title={})",
                    data.getId(), data.getTitle());

                float[] embedding = embeddingService.createEmbedding(text);

                if (embedding != null && embedding.length == 1536) {
                    data.setEmbedding(embedding);
                    portfolioDataRepository.save(data);
                    successCount++;
                    log.info("[EmbeddingRegeneration] ID={}: 임베딩 저장 완료", data.getId());
                } else {
                    log.error("[EmbeddingRegeneration] ID={}: 임베딩 생성 실패", data.getId());
                    failCount++;
                }

            } catch (Exception e) {
                log.error("[EmbeddingRegeneration] ID={}: 에러 - {}", data.getId(), e.getMessage());
                failCount++;
            }
        }

        log.info("[EmbeddingRegeneration] 완료: 성공 {}개, 실패 {}개", successCount, failCount);
        return successCount;
    }
}

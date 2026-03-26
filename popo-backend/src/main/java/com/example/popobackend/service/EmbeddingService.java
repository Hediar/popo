package com.example.popobackend.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OpenAI Embeddings API를 사용하여 텍스트를 벡터로 변환
 * text-embedding-3-small 모델 사용 (1536 dimensions)
 */
@Service
public class EmbeddingService {

    @Autowired
    private EmbeddingModel embeddingModel;

    /**
     * 텍스트를 벡터로 변환
     *
     * @param text 변환할 텍스트
     * @return 임베딩 벡터 (1536 dimensions)
     */
    public float[] createEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        try {
            // Spring AI EmbeddingModel 사용
            EmbeddingResponse response = embeddingModel.call(
                new EmbeddingRequest(List.of(text), null)
            );

            // 첫 번째 결과의 임베딩 가져오기
            if (!response.getResults().isEmpty()) {
                float[] embedding = response.getResults().get(0).getOutput();
                return embedding;
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create embedding for text: " + text, e);
        }
    }

    /**
     * 여러 텍스트를 한 번에 벡터로 변환 (배치 처리)
     *
     * @param texts 변환할 텍스트 목록
     * @return 임베딩 벡터 목록
     */
    public List<float[]> createEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        try {
            EmbeddingResponse response = embeddingModel.call(
                new EmbeddingRequest(texts, null)
            );

            return response.getResults().stream()
                .map(result -> result.getOutput())
                .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create embeddings for texts", e);
        }
    }

    /**
     * 포트폴리오 데이터를 임베딩용 텍스트로 포맷팅
     * title만 임베딩하여 더 정확한 키워드 매칭
     * content와 metadata는 답변 생성 시 사용
     *
     * @param title 제목
     * @param content 내용 (임베딩에는 사용하지 않음)
     * @return 임베딩용 텍스트 (title만)
     */
    public String formatForEmbedding(String title, String content) {
        // title만 임베딩 생성 (더 정확한 키워드 매칭)
        if (title != null && !title.isEmpty()) {
            return title;
        }

        // title이 없는 경우에만 content 사용
        if (content != null && !content.isEmpty()) {
            return content;
        }

        return "";
    }
}

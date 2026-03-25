package com.example.popobackend.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

/**
 * PostgreSQL vector 타입과 Java float[] 배열 간 변환
 * pgvector extension 사용
 *
 * String 기반 변환으로 Hibernate의 bytea 매핑 문제를 회피
 */
@Converter
public class VectorConverter implements AttributeConverter<float[], String> {

    @Override
    public String convertToDatabaseColumn(float[] attribute) {
        if (attribute == null) {
            return null;
        }

        // float[] → "[1.0,2.0,3.0]" 형식으로 변환
        return Arrays.toString(attribute).replace(" ", "");
    }

    @Override
    public float[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }

        try {
            // "[1.0,2.0,3.0]" → float[] 형식으로 변환
            String vectorString = dbData.replace("[", "").replace("]", "");

            if (vectorString.isEmpty()) {
                return new float[0];
            }

            String[] values = vectorString.split(",");
            float[] result = new float[values.length];

            for (int i = 0; i < values.length; i++) {
                result[i] = Float.parseFloat(values[i].trim());
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert DB value to float[]: " + dbData.substring(0, Math.min(50, dbData.length())), e);
        }
    }
}

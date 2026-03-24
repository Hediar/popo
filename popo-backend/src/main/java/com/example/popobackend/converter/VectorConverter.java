package com.example.popobackend.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * PostgreSQL vector 타입과 Java float[] 배열 간 변환
 * pgvector extension 사용
 */
@Converter
public class VectorConverter implements AttributeConverter<float[], PGobject> {

    @Override
    public PGobject convertToDatabaseColumn(float[] attribute) {
        if (attribute == null) {
            return null;
        }

        PGobject pgObject = new PGobject();
        pgObject.setType("vector");

        try {
            // float[] → "[1.0,2.0,3.0]" 형식으로 변환
            String vectorString = Arrays.toString(attribute)
                    .replace(" ", ""); // 공백 제거
            pgObject.setValue(vectorString);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to convert float[] to PGobject", e);
        }

        return pgObject;
    }

    @Override
    public float[] convertToEntityAttribute(PGobject dbData) {
        if (dbData == null || dbData.getValue() == null) {
            return null;
        }

        try {
            // "[1.0,2.0,3.0]" → float[] 형식으로 변환
            String vectorString = dbData.getValue();

            // 대괄호 제거
            vectorString = vectorString.replace("[", "").replace("]", "");

            if (vectorString.isEmpty()) {
                return new float[0];
            }

            // 쉼표로 분리하여 float 배열로 변환
            String[] values = vectorString.split(",");
            float[] result = new float[values.length];

            for (int i = 0; i < values.length; i++) {
                result[i] = Float.parseFloat(values[i].trim());
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert PGobject to float[]", e);
        }
    }
}

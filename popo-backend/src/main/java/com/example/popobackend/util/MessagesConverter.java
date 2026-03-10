package com.example.popobackend.util;

import com.example.popobackend.dto.MessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class MessagesConverter implements AttributeConverter<List<MessageDto>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public String convertToDatabaseColumn(List<MessageDto> messages) {
        if (messages == null || messages.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(messages);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert messages to JSON", e);
        }
    }

    @Override
    public List<MessageDto> convertToEntityAttribute(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<MessageDto>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}
package org.example.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.DocumentDto;

public class JsonDocumentSerializer implements DocumentSerializerUtil {
    private final ObjectMapper objectMapper;

    public JsonDocumentSerializer() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String serializeDocument(DocumentDto document) throws JsonProcessingException {
        return objectMapper.writeValueAsString(document);
    }
}

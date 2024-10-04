package org.example.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.dto.DocumentDto;

public interface DocumentSerializerUtil {
    String serializeDocument(DocumentDto document) throws JsonProcessingException;
}
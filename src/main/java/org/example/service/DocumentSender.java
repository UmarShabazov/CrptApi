package org.example.service;

import org.example.dto.DocumentDto;

public interface DocumentSender {
    void sendDocument(DocumentDto document, String signature);
}
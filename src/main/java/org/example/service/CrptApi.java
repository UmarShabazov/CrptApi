package org.example.service;

import org.example.dto.DocumentDto;

import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final Executor executor;
    private final DocumentSender documentSender;

    public CrptApi(TimeUnit timeUnit, int requestLimit, DocumentSender documentSender) {
        this.executor = new RateLimitedExecutor(requestLimit, timeUnit);
        this.documentSender = documentSender;
    }

    public void sendDocument(DocumentDto document, String signature) {
        executor.execute(() -> {
            documentSender.sendDocument(document, signature);
        });
    }
}
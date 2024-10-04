package org.example.service;

import org.example.dto.DocumentDto;
import org.example.util.DocumentSerializerUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpDocumentSender implements DocumentSender {
    private static final String API_URL = "https://ismp.crpt.ru/api/v3";
    private static final String PATH = "/lk/documents/create";

    private final DocumentSerializerUtil serializer;

    public HttpDocumentSender(DocumentSerializerUtil serializer) {
        this.serializer = serializer;
    }

    @Override
    public void sendDocument(DocumentDto document, String signature) {
        try {
            String json = serializer.serializeDocument(document);
            int responseCode = callCreateDocument(json);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed to send document. Response code: " + responseCode);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not send request", e);
        }
    }

    private int callCreateDocument(String json) throws IOException {
        URL url = new URL(API_URL + PATH);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return connection.getResponseCode();
    }
}

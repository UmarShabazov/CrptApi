package org.example;

import org.example.dto.DocumentDto;
import org.example.service.*;
import org.example.util.DocumentSerializerUtil;
import org.example.util.JsonDocumentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        DocumentSerializerUtil serializer = new JsonDocumentSerializer();
        DocumentSender sender = new HttpDocumentSender(serializer);
        CrptApi api = new CrptApi(TimeUnit.MINUTES, 3, sender);

        DocumentDto document = createSampleDocumentDto();
        for (int i = 0; i < 100; i++) {
            api.sendDocument(document, "CeoSignature");
        }
    }

    private static DocumentDto createSampleDocumentDto() {
        DocumentDto document = new DocumentDto();
        DocumentDto.Description description = new DocumentDto.Description();
        description.setParticipantInn("1234567890");

        document.setDescription(description);
        document.setDocId("doc123");
        document.setDocStatus("Active");
        document.setDocType("LP_INTRODUCE_GOODS");
        document.setImportRequest(true);
        document.setOwnerInn("9876543210");
        document.setParticipantInn("1234567890");
        document.setProducerInn("1122334455");
        document.setProductionDate("2020-01-23");
        document.setProductionType("TypeA");

        List<DocumentDto.Product> products = createSampleProductList();

        document.setProducts(products);
        document.setRegDate("2020-01-23");
        document.setRegNumber("reg123");
        return document;
    }

    private static List<DocumentDto.Product> createSampleProductList() {
        List<DocumentDto.Product> products = new ArrayList<>();
        DocumentDto.Product product = new DocumentDto.Product();
        product.setCertificateDocument("cert123");
        product.setCertificateDocumentDate("2020-01-23");
        product.setCertificateDocumentNumber("certnum123");
        product.setOwnerInn("9876543210");
        product.setProducerInn("1122334455");
        product.setProductionDate("2020-01-23");
        product.setTnvedCode("code123");
        product.setUitCode("uit123");
        product.setUituCode("uitu123");
        products.add(product);
        return products;
    }
}
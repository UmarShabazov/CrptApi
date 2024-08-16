package org.example.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;


public class CrptApi {

    public static void main(String[] args) {

        var api = new CrptApi(TimeUnit.MINUTES, 3);
        DocumentDto document = createSampleDocumentDto();

        for (int i = 0; i < 100; i++) {
            api.sendDocument(document, "CeoSignature");

        }
    }

    private static final String API_URL = "https://ismp.crpt.ru/api/v3";
    private static final String path = "/lk/documents/create";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Executor executor;


    public CrptApi(TimeUnit timeUnit, int requestLimit) {

        this.executor = new RateLimitedExecutor(requestLimit, timeUnit);

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

    public static String serializeDocument(DocumentDto document) throws JsonProcessingException {

        return objectMapper.writeValueAsString(document);
    }

    private void sendDocument(DocumentDto document, String signature) {

        executor.execute(() -> {

            try {

                String json = serializeDocument(document);

                int responseCode = callCreateDocument(json);

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    System.out.println(responseCode);

                    throw new RuntimeException("Failed to send document. Response code: " + responseCode);
                }

            } catch (JsonProcessingException e) {

                throw new RuntimeException("Could not serialize document", e);

            } catch (IOException e) {

                throw new RuntimeException("Could not send request", e);
            }

        });

    }

    /**
     * @return Http response code
     */
    private static int callCreateDocument(String json) throws IOException {
        URL url = new URL(API_URL + path);

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

    public interface Executor {

        void execute(Runnable action);
    }

    public static class RateLimitedExecutor implements Executor {
        private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        private final Semaphore semaphore;
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        public RateLimitedExecutor(int requestLimit, TimeUnit timeUnit) {
            this.semaphore = new Semaphore(requestLimit, true);

            // Scheduler resets the semaphore permits after the interval
            scheduler.scheduleAtFixedRate(() -> {
                semaphore.release(requestLimit - semaphore.availablePermits());
            }, 1, 1, timeUnit);

            // Thread for processing the queue
            Thread processingThread = new Thread(() -> {
                while (true) {
                    try {
                        Runnable action = queue.take();
                        semaphore.acquire();
                        action.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            processingThread.setDaemon(true);
            processingThread.start();
        }

        @Override
        public void execute(Runnable action) {

            queue.add(action);
        }

    }

    public static class DocumentDto {

        private Description description;

        @JsonProperty("doc_id")
        private String docId;

        @JsonProperty("doc_status")
        private String docStatus;

        @JsonProperty("doc_type")
        private String docType = "LP_INTRODUCE_GOODS";

        private boolean importRequest;

        @JsonProperty("owner_inn")
        private String ownerInn;

        @JsonProperty("participant_inn")
        private String participantInn;

        @JsonProperty("producer_inn")
        private String producerInn;

        @JsonProperty("production_date")
        private String productionDate;

        @JsonProperty("production_type")
        private String productionType;

        private List<Product> products;

        @JsonProperty("reg_date")
        private String regDate;

        @JsonProperty("reg_number")
        private String regNumber;


        public Description getDescription() {
            return description;
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        public String getDocId() {
            return docId;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }

        public String getDocStatus() {
            return docStatus;
        }

        public void setDocStatus(String docStatus) {
            this.docStatus = docStatus;
        }

        public String getDocType() {
            return docType;
        }

        public void setDocType(String docType) {
            this.docType = docType;
        }

        public boolean isImportRequest() {
            return importRequest;
        }

        public void setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public void setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
        }

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public void setProducerInn(String producerInn) {
            this.producerInn = producerInn;
        }

        public String getProductionDate() {
            return productionDate;
        }

        public void setProductionDate(String productionDate) {
            this.productionDate = productionDate;
        }

        public String getProductionType() {
            return productionType;
        }

        public void setProductionType(String productionType) {
            this.productionType = productionType;
        }

        public List<Product> getProducts() {
            return products;
        }

        public void setProducts(List<Product> products) {
            this.products = products;
        }

        public String getRegDate() {
            return regDate;
        }

        public void setRegDate(String regDate) {
            this.regDate = regDate;
        }

        public String getRegNumber() {
            return regNumber;
        }

        public void setRegNumber(String regNumber) {
            this.regNumber = regNumber;
        }

        public static class Description {

            private String participantInn;

            public String getParticipantInn() {
                return participantInn;
            }

            public void setParticipantInn(String participantInn) {
                this.participantInn = participantInn;
            }
        }

        public static class Product {

            @JsonProperty("certificate_document")
            private String certificateDocument;

            @JsonProperty("certificate_document_date")
            private String certificateDocumentDate;

            @JsonProperty("certificate_document_number")
            private String certificateDocumentNumber;

            @JsonProperty("owner_inn")
            private String ownerInn;

            @JsonProperty("producer_inn")
            private String producerInn;

            @JsonProperty("production_date")
            private String productionDate;

            @JsonProperty("tnved_code")
            private String tnvedCode;

            @JsonProperty("uit_code")
            private String uitCode;

            @JsonProperty("uitu_code")
            private String uituCode;


            public String getCertificateDocument() {
                return certificateDocument;
            }

            public void setCertificateDocument(String certificateDocument) {
                this.certificateDocument = certificateDocument;
            }

            public String getCertificateDocumentDate() {
                return certificateDocumentDate;
            }

            public void setCertificateDocumentDate(String certificateDocumentDate) {
                this.certificateDocumentDate = certificateDocumentDate;
            }

            public String getCertificateDocumentNumber() {
                return certificateDocumentNumber;
            }

            public void setCertificateDocumentNumber(String certificateDocumentNumber) {
                this.certificateDocumentNumber = certificateDocumentNumber;
            }

            public String getOwnerInn() {
                return ownerInn;
            }

            public void setOwnerInn(String ownerInn) {
                this.ownerInn = ownerInn;
            }

            public String getProducerInn() {
                return producerInn;
            }

            public void setProducerInn(String producerInn) {
                this.producerInn = producerInn;
            }

            public String getProductionDate() {
                return productionDate;
            }

            public void setProductionDate(String productionDate) {
                this.productionDate = productionDate;
            }

            public String getTnvedCode() {
                return tnvedCode;
            }

            public void setTnvedCode(String tnvedCode) {
                this.tnvedCode = tnvedCode;
            }

            public String getUitCode() {
                return uitCode;
            }

            public void setUitCode(String uitCode) {
                this.uitCode = uitCode;
            }

            public String getUituCode() {
                return uituCode;
            }

            public void setUituCode(String uituCode) {
                this.uituCode = uituCode;
            }
        }

    }
}

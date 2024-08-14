import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class CrptApi {

    private static final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    public TimeUnit timeUnit;
    public final int requestLimit;
    public Semaphore semaphore;
    private final long intervalMillis;

    public static void main(String[] args) {

//      RequestExecutor actionator = new InstantRequestExecutor();
//      RequestExecutor actionator = new LoggingRequestExecutor();

        RequestExecutor actionator = new RateLimitedRequestExecutor();

        DocumentDto document = getDocumentDto();

        for (int i = 0; i <= 3; i++) {

            actionator.execute(() -> System.out.println(new Date()));

        }

        try {
            String json = createDocument(document,"signature");
            System.out.println(json);
            sendPostRequest(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private static DocumentDto getDocumentDto() {
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

        List<DocumentDto.Product> products = getProductList();

        document.setProducts(products);
        document.setRegDate("2020-01-23");
        document.setRegNumber("reg123");
        return document;
    }

    private static List<DocumentDto.Product> getProductList() {
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

    public static String createDocument(DocumentDto document, String signature) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.writeValueAsString(document);
    }

    private static void sendPostRequest(String json) {

        try {

            URL url = new URL(API_URL);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.intervalMillis = timeUnit.toMillis(1);
        this.semaphore = new Semaphore(requestLimit, true);
    }



    public interface RequestExecutor {

        public abstract void execute(Runnable action);

    }

    public static class InstantRequestExecutor implements RequestExecutor {


        @Override
        public void execute(Runnable action) {

            action.run();

        }
    }

    public static class RateLimitedRequestExecutor implements RequestExecutor {
        Queue<Runnable> queue = new LinkedList<>();

        {
            new Thread(() -> {

                while (true) {
                    try {
                        Thread.sleep(1000);
                        if (!queue.isEmpty()) {
                            queue.poll().run();
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            }).start();
        }

        @Override
        public void execute(Runnable action) {


            queue.add(action);
            System.out.println(queue.size());

        }

    }
        public static class LoggingRequestExecutor implements RequestExecutor {

            @Override
            public void execute(Runnable action) {
                System.out.println("Начинаю выполнение действия.");

                action.run();

                System.out.println("Заканчиваю выполнение действия.");


            }
        }
/*
В теле запроса передается в формате JSON документ:
 {"description": { "participantInn": "string" },
 "doc_id": "string",
 "doc_status": "string",
 "doc_type": "LP_INTRODUCE_GOODS",
 109 "importRequest": true,
 "owner_inn": "string",
 "participant_inn": "string",
 "producer_inn": "string",
 "production_date": "2020-01-23",
 "production_type": "string",
 "products": [ { "certificate_document": "string",
 "certificate_document_date": "2020-01-23",
 "certificate_document_number": "string",
 "owner_inn": "string",
 "producer_inn": "string",
 "production_date": "2020-01-23",
 "tnved_code": "string",
 "uit_code": "string",
 "uitu_code": "string" } ],
  "reg_date": "2020-01-23",
  "reg_number": "string"}

 */

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

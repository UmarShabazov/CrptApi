# CrptApi
A Java-based application for sending and managing documents via the CRPT API to "Честный знак" with rate-limiting and serialization.

### Backend Tech stack
- **Java 17**
- **Jackson** for JSON processing
- **HttpURLConnection** for HTTP communication
- **Custom Executor** for rate-limiting

## Project tasks
### EPIC: Document Serialization and HTTP Request Handling
  - **DESCRIPTION:** As a developer, I need to serialize documents into JSON format and send them via HTTP requests to the CRPT API.
  - **TECH:** Uses Jackson for serialization and `HttpURLConnection` for HTTP POST requests.
  - **DETAILS:**
    - Implements `DocumentDto` as the core data model for document management.
    - Sends serialized documents to the CRPT API using the `/lk/documents/create` endpoint.
  
### EPIC: Rate-Limiting API Requests
  - **DESCRIPTION:** As a developer, I need to ensure that API requests are rate-limited to avoid overwhelming the CRPT API with too many requests in a short period.
  - **TECH:** Custom `RateLimitedExecutor` with configurable time limits (e.g., 3 requests per minute).
  - **DETAILS:**
    - Uses Java `Semaphore` and `ScheduledExecutorService` to handle concurrency and limit the number of requests sent per minute.
  
### EPIC: Extensible Document Sending Mechanism
  - **DESCRIPTION:** As a developer, I need to ensure the document sending logic is modular and extensible, enabling different types of document senders or serialization methods.
  - **TECH:** Follows SOLID principles:
    - **Dependency Injection** for the `DocumentSender` and `DocumentSerializer`.
    - **HttpDocumentSender** handles the HTTP-specific logic.
    - **JacksonDocumentSerializer** handles JSON serialization.
  
### EPIC: Flexible Document DTO Structure
  - **DESCRIPTION:** As a developer, I need to be able to easily extend and modify the structure of the document data (such as adding new fields) without affecting other parts of the system.
  - **TECH:** Follows Java DTO design using Jackson annotations.
  - **DETAILS:** 
    - Supports nested DTOs for document description and product details.
    - Document structure adheres to CRPT API requirements.

## Project setup for developers

### Prerequisites

1. Install **Java 17** (Recommended)
2. (Optional) Install **Docker** if you'd like to containerize parts of the system.
3. Install **Maven** for managing dependencies and building the project.

### Running the Application

1. Clone the repository:

   git clone https://github.com/your-username/crpt-api.git
   cd crpt-api

2. Build the project with Maven:

   mvn clean install

3. Run the application:

   java -jar target/crpt-api-1.0-SNAPSHOT.jar

   The application will start and send 100 test documents to the CRPT API with rate-limiting applied.

### Running with Docker (Optional)

1. Build the Docker image:

   docker build -t crpt-api .

2. Run the container:

   docker run -d --name crpt-api-container crpt-api

## API Endpoints

- `POST /lk/documents/create` – Sends a document to the CRPT API.
- Request body must follow the `DocumentDto` format (as serialized to JSON).

## Future Enhancements

- **Implement Logging:** Add structured logging for API requests and responses.
- **Error Handling Improvements:** More robust handling of HTTP errors and retries.
- **Tests:** Unit and integration tests for document sending and serialization logic.
- **Additional Document Types:** Add support for new document types and data structures.

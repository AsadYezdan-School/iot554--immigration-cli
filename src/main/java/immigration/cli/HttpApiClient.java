package immigration.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import immigration.api.dto.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpApiClient {

    private final String serverUrl;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public HttpApiClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public VerificationResponse verifyByShareCode(ShareCodeVerifyRequest req) {
        return post("/api/v1/verify/share-code", req, VerificationResponse.class);
    }

    public VerificationResponse verifyByDocument(DocumentVerifyRequest req) {
        return post("/api/v1/verify/document", req, VerificationResponse.class);
    }

    public GenerateCodeResponse generateCode(GenerateCodeRequest req) {
        return post("/api/v1/codes/generate", req, GenerateCodeResponse.class);
    }

    public PersonLookupResponse lookupByPassport(String passportNumber) {
        return get("/api/v1/persons/lookup?passport=" + passportNumber, PersonLookupResponse.class);
    }

    public AnalyticsResponse getAnalyticsByOrganisation() {
        return get("/api/v1/analytics/by-organisation", AnalyticsResponse.class);
    }

    public AnalyticsResponse getAnalyticsByDate() {
        return get("/api/v1/analytics/by-date", AnalyticsResponse.class);
    }

    public AnalyticsResponse getAnalyticsByPurpose() {
        return get("/api/v1/analytics/by-purpose", AnalyticsResponse.class);
    }

    public AnalyticsResponse getAnalyticsByOutcome() {
        return get("/api/v1/analytics/outcomes", AnalyticsResponse.class);
    }

    private <T> T post(String path, Object body, Class<T> responseType) {
        try {
            var json = mapper.writeValueAsString(body);
            var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new ApiException(response.statusCode(), parseErrorMessage(response.body()));
            }
            return mapper.readValue(response.body(), responseType);
        } catch (IOException e) {
            System.err.println("Cannot connect to server at " + serverUrl + " — ensure the API server is running");
            throw new ApiException(0, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(0, "Request interrupted");
        }
    }

    private <T> T get(String path, Class<T> responseType) {
        try {
            var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .GET()
                .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new ApiException(response.statusCode(), parseErrorMessage(response.body()));
            }
            return mapper.readValue(response.body(), responseType);
        } catch (IOException e) {
            System.err.println("Cannot connect to server at " + serverUrl + " — ensure the API server is running");
            throw new ApiException(0, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(0, "Request interrupted");
        }
    }

    private String parseErrorMessage(String body) {
        try {
            return mapper.readTree(body).get("message").asText();
        } catch (Exception e) {
            return "Server error";
        }
    }
}

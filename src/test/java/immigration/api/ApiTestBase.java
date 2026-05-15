package immigration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import immigration.AppContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class ApiTestBase {

    protected AppContext appCtx;
    protected ApiServer server;
    protected String baseUrl;
    protected HttpClient httpClient;
    protected ObjectMapper mapper;

    @BeforeAll
    void startServer(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("persons.json"), """
            [
              {"id":"P001","fullName":"Emma Harrison","dateOfBirth":"1985-03-22",
               "nationality":"Australian","passportNumber":"AB1234567","permitNumber":null},
              {"id":"P004","fullName":"Luca Ferretti","dateOfBirth":"1968-05-01",
               "nationality":"Italian","passportNumber":"EF3456789","permitNumber":null},
              {"id":"P005","fullName":"Mei Zhang","dateOfBirth":"2000-09-25",
               "nationality":"Chinese","passportNumber":"GH5678901","permitNumber":null},
              {"id":"P006","fullName":"Omar Khalid","dateOfBirth":"1990-07-15",
               "nationality":"Jordanian","passportNumber":"JJ9876543","permitNumber":null},
              {"id":"P007","fullName":"Ana Sousa","dateOfBirth":"1995-02-28",
               "nationality":"Portuguese","passportNumber":"PT3456789","permitNumber":"CD7654321"}
            ]
            """);

        Files.writeString(tempDir.resolve("visas.json"), """
            [
              {"id":"V001","personId":"P001","visaType":"WORK","expiryDate":"2027-06-30",
               "rightToWork":true,"rightToRent":false,"entryPermitted":true,"conditions":[]},
              {"id":"V004","personId":"P004","visaType":"SETTLEMENT","expiryDate":"2035-12-31",
               "rightToWork":true,"rightToRent":true,"entryPermitted":true,"conditions":[]},
              {"id":"V005","personId":"P005","visaType":"VISITOR","expiryDate":"2026-10-25",
               "rightToWork":false,"rightToRent":false,"entryPermitted":true,
               "conditions":["No employment"]},
              {"id":"V007","personId":"P007","visaType":"SKILLED_WORKER","expiryDate":"2028-06-30",
               "rightToWork":true,"rightToRent":true,"entryPermitted":true,"conditions":[]}
            ]
            """);

        Files.writeString(tempDir.resolve("share_codes.json"), """
            [
              {"code":"ABC123XY1","personId":"P001","purpose":"EMPLOYMENT",
               "issuedAt":"2026-01-01T00:00:00Z","expiresAt":"2027-01-01T00:00:00Z"},
              {"code":"KLM012BC4","personId":"P001","purpose":"EMPLOYMENT",
               "issuedAt":"2024-01-01T00:00:00Z","expiresAt":"2024-06-01T00:00:00Z"},
              {"code":"DEF456YZ2","personId":"P004","purpose":"ACCOMMODATION",
               "issuedAt":"2026-01-01T00:00:00Z","expiresAt":"2027-01-01T00:00:00Z"},
              {"code":"QRS678FG6","personId":"P005","purpose":"EMPLOYMENT",
               "issuedAt":"2026-01-01T00:00:00Z","expiresAt":"2027-01-01T00:00:00Z"},
              {"code":"EDU789AB3","personId":"P001","purpose":"EDUCATION",
               "issuedAt":"2026-01-01T00:00:00Z","expiresAt":"2027-01-01T00:00:00Z"},
              {"code":"OKL456ZZ9","personId":"P006","purpose":"EMPLOYMENT",
               "issuedAt":"2026-01-01T00:00:00Z","expiresAt":"2027-01-01T00:00:00Z"}
            ]
            """);

        Files.writeString(tempDir.resolve("organisations.json"), """
            [
              {"id":"ORG001","name":"Acme Ltd","email":"hr@acme.com","role":"EMPLOYER"},
              {"id":"ORG003","name":"City Rentals","email":"lets@cityrentals.com","role":"LANDLORD"},
              {"id":"ORG004","name":"City College","email":"admin@citycollege.ac.uk","role":"EDUCATION"},
              {"id":"ORG005","name":"Port Authority","email":"ops@portauth.gov","role":"BORDER_CONTROL"},
              {"id":"ORG006","name":"Metro Police","email":"verify@metropolice.gov","role":"LAW_ENFORCEMENT"}
            ]
            """);

        Files.writeString(tempDir.resolve("audit_log.jsonl"), "");

        appCtx = new AppContext(tempDir);
        server = new ApiServer(appCtx);
        server.start(0);
        baseUrl = "http://localhost:" + server.port();

        httpClient = HttpClient.newHttpClient();
        mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @AfterAll
    void stopServer() {
        if (server != null) server.stop();
    }

    protected HttpResponse<String> post(String path, String body) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> get(String path) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .GET()
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

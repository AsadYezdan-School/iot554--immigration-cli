package immigration.cli;

import io.javalin.Javalin;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class HttpApiClientTest {

    // --- server not running or misconfigured URL ---

    @Test
    void serverNotRunning_throwsApiException() {
        var client = new HttpApiClient("http://localhost:1");
        assertThrows(ApiException.class, () -> client.getAnalyticsByOrganisation());
    }

    @Test
    void serverNotRunning_printsConnectionErrorToStderr() {
        var client = new HttpApiClient("http://localhost:1");
        var original = System.err;
        var buffer = new ByteArrayOutputStream();
        System.setErr(new PrintStream(buffer));
        try {
            assertThrows(ApiException.class, () -> client.getAnalyticsByOrganisation());
        } finally {
            System.setErr(original);
        }
        assertTrue(buffer.toString().contains("Cannot connect to server"),
            "Expected connection error message in stderr: " + buffer);
    }

    @Test
    void misconfiguredUrl_throwsApiException() {
        var client = new HttpApiClient("http://host.invalid:9999");
        assertThrows(ApiException.class, () -> client.getAnalyticsByOrganisation());
    }

    // --- server returns 500 ---

    @Test
    void serverReturns500_throwsApiExceptionWithStatus500() {
        var app = Javalin.create();
        app.get("/api/v1/analytics/by-organisation", ctx -> {
            throw new RuntimeException("deliberate test error");
        });
        app.exception(Exception.class, (e, ctx) ->
            ctx.status(500).result(
                "{\"status\":500,\"error\":\"Internal Server Error\","
                + "\"message\":\"An unexpected error occurred\"}"));
        app.start(0);

        var client = new HttpApiClient("http://localhost:" + app.port());
        try {
            var ex = assertThrows(ApiException.class, () -> client.getAnalyticsByOrganisation());
            assertEquals(500, ex.status());
            assertNotNull(ex.getMessage());
        } finally {
            app.stop();
        }
    }

    // --- server becomes unavailable mid-session ---

    @Test
    void serverBecomesUnavailable_midSession_throwsApiException() {
        var app = Javalin.create();
        app.get("/api/v1/analytics/by-organisation", ctx ->
            ctx.result("{\"data\":{}}"));
        app.start(0);

        var client = new HttpApiClient("http://localhost:" + app.port());

        // First request succeeds
        assertDoesNotThrow(() -> client.getAnalyticsByOrganisation());

        // Server is stopped mid-session
        app.stop();

        // Subsequent request fails with ApiException
        assertThrows(ApiException.class, () -> client.getAnalyticsByOrganisation());
    }
}

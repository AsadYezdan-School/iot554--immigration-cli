package immigration;

import immigration.api.ApiServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DevLauncher {

    private static final Logger logger = LoggerFactory.getLogger(DevLauncher.class);
    private static final String HEALTH_URL = "http://localhost:7070/api/v1/persons/lookup";

    public static void main(String[] args) throws Exception {
        Thread serverThread = new Thread(() -> ApiServer.main(new String[]{}));
        serverThread.setDaemon(true);
        serverThread.setName("api-server");
        serverThread.start();

        waitForServer();

        Main.main(args);
    }

    private static void waitForServer() throws InterruptedException {
        var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(500))
                .build();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(HEALTH_URL))
                .GET()
                .timeout(Duration.ofMillis(500))
                .build();

        logger.info("Waiting for API server...");
        for (int i = 0; i < 20; i++) {
            try {
                client.send(request, HttpResponse.BodyHandlers.discarding());
                logger.info("API server ready");
                return;
            } catch (Exception ignored) {
                Thread.sleep(250);
            }
        }
        logger.warn("API server did not respond after 5 s — starting CLI anyway");
    }
}

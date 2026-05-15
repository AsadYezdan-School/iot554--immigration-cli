package immigration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import immigration.AppContext;
import immigration.api.dto.ErrorResponse;
import immigration.api.handlers.AnalyticsHandler;
import immigration.api.handlers.CodesHandler;
import immigration.api.handlers.PersonsHandler;
import immigration.api.handlers.VerifyHandler;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;

public class ApiServer {

    private static final Logger logger = LoggerFactory.getLogger(ApiServer.class);
    private static final int DEFAULT_PORT = 7070;

    public static void main(String[] args) {
        int port = parsePort(args);
        var server = new ApiServer(new AppContext());
        server.start(port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown signal received — stopping API server");
            server.stop();
        }));
    }

    private static int parsePort(String[] args) {
        var list = Arrays.asList(args);
        int idx = list.indexOf("--port");
        if (idx >= 0 && idx + 1 < list.size()) {
            try { return Integer.parseInt(list.get(idx + 1)); }
            catch (NumberFormatException ignored) {}
        }
        return DEFAULT_PORT;
    }

    private final AppContext ctx;
    private Javalin app;

    public ApiServer(AppContext ctx) {
        this.ctx = ctx;
    }

    public void start(int port) {
        var mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        var verifyHandler    = new VerifyHandler(ctx.verification);
        var codesHandler     = new CodesHandler(ctx.shareCodeSvc, ctx.persons);
        var analyticsHandler = new AnalyticsHandler(ctx.analytics);
        var personsHandler   = new PersonsHandler(ctx.persons);

        app = Javalin.create(config ->
            config.jsonMapper(new JavalinJackson(mapper, false))
        );

        app.post("/api/v1/verify/share-code",        verifyHandler::shareCode);
        app.post("/api/v1/verify/document",          verifyHandler::document);
        app.post("/api/v1/codes/generate",           codesHandler::generate);
        app.get("/api/v1/analytics/by-organisation", analyticsHandler::byOrganisation);
        app.get("/api/v1/analytics/by-date",         analyticsHandler::byDate);
        app.get("/api/v1/analytics/by-purpose",      analyticsHandler::byPurpose);
        app.get("/api/v1/analytics/outcomes",        analyticsHandler::outcomes);
        app.get("/api/v1/persons/lookup",            personsHandler::lookup);

        app.exception(Exception.class, (e, reqCtx) -> {
            logger.error("Unhandled exception on {} {}: {}", reqCtx.method(), reqCtx.path(), e.getMessage(), e);
            reqCtx.status(500).json(new ErrorResponse(500, "Internal Server Error",
                "An unexpected error occurred"));
        });

        app.start(port);
        logger.info("API server started on port {}", app.port());
    }

    public int port() {
        return app.port();
    }

    public void stop() {
        if (app != null) app.stop();
    }
}

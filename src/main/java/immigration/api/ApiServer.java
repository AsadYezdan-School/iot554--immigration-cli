package immigration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import immigration.AppContext;
import immigration.api.auth.ApiKeyAuthMiddleware;
import immigration.api.dto.ErrorResponse;
import immigration.api.handlers.AnalyticsHandler;
import immigration.api.handlers.CodesHandler;
import immigration.api.handlers.VerifyHandler;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiServer {

    private static final Logger logger = LoggerFactory.getLogger(ApiServer.class);

    private final AppContext ctx;
    private Javalin app;

    public ApiServer(AppContext ctx) {
        this.ctx = ctx;
    }

    public void start(int port) {
        var mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        var middleware      = new ApiKeyAuthMiddleware(ctx.organisations);
        var verifyHandler   = new VerifyHandler(ctx.verification);
        var codesHandler    = new CodesHandler(ctx.shareCodeSvc, ctx.persons);
        var analyticsHandler = new AnalyticsHandler(ctx.analytics);

        app = Javalin.create(config ->
            config.jsonMapper(new JavalinJackson(mapper, false))
        );

        app.before("/api/v1/*", middleware::handle);

        app.post("/api/v1/verify/share-code",       verifyHandler::shareCode);
        app.post("/api/v1/verify/document",          verifyHandler::document);
        app.post("/api/v1/codes/generate",           codesHandler::generate);
        app.get("/api/v1/analytics/by-organisation", analyticsHandler::byOrganisation);
        app.get("/api/v1/analytics/by-date",         analyticsHandler::byDate);
        app.get("/api/v1/analytics/by-purpose",      analyticsHandler::byPurpose);
        app.get("/api/v1/analytics/outcomes",        analyticsHandler::outcomes);

        app.exception(Exception.class, (e, reqCtx) -> {
            logger.error("Unhandled exception on {} {}: {}", reqCtx.method(), reqCtx.path(), e.getMessage(), e);
            reqCtx.status(500).json(new ErrorResponse(500, "Internal Server Error",
                "An unexpected error occurred"));
        });

        app.start(port);
        logger.info("API server started on port {}", port);
    }

    public void stop() {
        if (app != null) app.stop();
    }
}

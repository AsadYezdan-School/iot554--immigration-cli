package immigration.api.auth;

import immigration.api.dto.ErrorResponse;
import immigration.repositories.OrganisationRepository;
import io.javalin.http.Context;

public class ApiKeyAuthMiddleware {

    private final OrganisationRepository orgRepo;

    public ApiKeyAuthMiddleware(OrganisationRepository orgRepo) {
        this.orgRepo = orgRepo;
    }

    public void handle(Context ctx) {
        String key = ctx.header("X-API-Key");
        var orgOpt = orgRepo.findByApiKey(key);
        if (orgOpt.isEmpty()) {
            ctx.status(401).json(new ErrorResponse(401, "Unauthorized", "Missing or invalid API key"));
            ctx.skipRemainingHandlers();
            return;
        }
        ctx.attribute("org", orgOpt.get());
    }
}

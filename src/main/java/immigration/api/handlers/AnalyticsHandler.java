package immigration.api.handlers;

import immigration.api.dto.AnalyticsResponse;
import immigration.services.AnalyticsService;
import io.javalin.http.Context;

public class AnalyticsHandler {

    private final AnalyticsService analyticsService;

    public AnalyticsHandler(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    public void byOrganisation(Context ctx) {
        ctx.json(new AnalyticsResponse(analyticsService.requestsByOrganisation()));
    }

    public void byDate(Context ctx) {
        ctx.json(new AnalyticsResponse(analyticsService.requestsByDate()));
    }

    public void byPurpose(Context ctx) {
        ctx.json(new AnalyticsResponse(analyticsService.shareCodesByPurpose()));
    }

    public void outcomes(Context ctx) {
        ctx.json(new AnalyticsResponse(analyticsService.outcomesByType()));
    }
}

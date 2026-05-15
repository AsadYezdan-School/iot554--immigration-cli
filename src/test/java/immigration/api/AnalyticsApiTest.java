package immigration.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnalyticsApiTest extends ApiTestBase {

    @Test
    void byOrganisation_emptyLog_returnsEmptyMap() throws Exception {
        var resp = get("/api/v1/analytics/by-organisation");
        assertEquals(200, resp.statusCode());
        var data = mapper.readTree(resp.body()).get("data");
        assertNotNull(data);
    }

    @Test
    void byOrganisation_afterEvents_groupsCorrectly() throws Exception {
        post("/api/v1/verify/share-code", """
            {"orgId":"ORG001","shareCode":"ABC123XY1","dateOfBirth":"1985-03-22",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        var resp = get("/api/v1/analytics/by-organisation");
        assertEquals(200, resp.statusCode());
        var data = mapper.readTree(resp.body()).get("data");
        assertTrue(data.has("ORG001"), "Expected ORG001 in analytics: " + data);
    }

    @Test
    void byDate_returnsValidResponse() throws Exception {
        var resp = get("/api/v1/analytics/by-date");
        assertEquals(200, resp.statusCode());
        assertNotNull(mapper.readTree(resp.body()).get("data"));
    }

    @Test
    void byPurpose_afterCodeGeneration_groupsCorrectly() throws Exception {
        post("/api/v1/codes/generate", """
            {"personId":"P001","purpose":"EMPLOYMENT"}
            """);
        var resp = get("/api/v1/analytics/by-purpose");
        assertEquals(200, resp.statusCode());
        var data = mapper.readTree(resp.body()).get("data");
        assertTrue(data.has("EMPLOYMENT"), "Expected EMPLOYMENT in analytics: " + data);
    }

    @Test
    void outcomes_afterEvents_groupsCorrectly() throws Exception {
        post("/api/v1/verify/share-code", """
            {"orgId":"ORG001","shareCode":"ABC123XY1","dateOfBirth":"1985-03-22",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        var resp = get("/api/v1/analytics/outcomes");
        assertEquals(200, resp.statusCode());
        var data = mapper.readTree(resp.body()).get("data");
        assertTrue(data.has("APPROVED"), "Expected APPROVED in outcomes: " + data);
    }
}

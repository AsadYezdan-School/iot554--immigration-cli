package immigration.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CodesApiTest extends ApiTestBase {

    @Test
    void generate_byPersonId_returns201() throws Exception {
        var resp = post("/api/v1/codes/generate", """
            {"personId":"P001","purpose":"EMPLOYMENT"}
            """);
        assertEquals(201, resp.statusCode());
        var body = mapper.readTree(resp.body());
        assertEquals("P001", body.get("personId").asText());
        assertEquals("EMPLOYMENT", body.get("purpose").asText());
        assertTrue(body.get("code").asText().matches("[A-Z0-9]{9}"));
    }

    @Test
    void generate_codeIsPersisted() throws Exception {
        var resp = post("/api/v1/codes/generate", """
            {"personId":"P004","purpose":"ACCOMMODATION"}
            """);
        assertEquals(201, resp.statusCode());
        var code = mapper.readTree(resp.body()).get("code").asText();
        assertTrue(appCtx.shareCodes.findByCode(code).isPresent());
    }

    @Test
    void generate_writesAuditEvent() throws Exception {
        post("/api/v1/codes/generate", """
            {"personId":"P001","purpose":"EDUCATION"}
            """);
        var events = appCtx.audit.queryAll();
        assertTrue(events.stream().anyMatch(e -> "SHARE_CODE_GENERATED".equals(e.eventType())));
    }

    @Test
    void generate_invalidPurpose_returns400() throws Exception {
        var resp = post("/api/v1/codes/generate", """
            {"personId":"P001","purpose":"HOUSING"}
            """);
        assertEquals(400, resp.statusCode());
    }

    @Test
    void generate_unknownPerson_returns404() throws Exception {
        var resp = post("/api/v1/codes/generate", """
            {"personId":"P999","purpose":"EMPLOYMENT"}
            """);
        assertEquals(404, resp.statusCode());
    }

    @Test
    void generate_missingPersonId_returns400() throws Exception {
        var resp = post("/api/v1/codes/generate", """
            {"purpose":"EMPLOYMENT"}
            """);
        assertEquals(400, resp.statusCode());
    }

    @Test
    void generate_missingPurpose_returns400() throws Exception {
        var resp = post("/api/v1/codes/generate", """
            {"personId":"P001"}
            """);
        assertEquals(400, resp.statusCode());
    }
}

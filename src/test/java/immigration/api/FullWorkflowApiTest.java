package immigration.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FullWorkflowApiTest extends ApiTestBase {

    @Test
    void generate_thenVerify_success() throws Exception {
        var genResp = post("/api/v1/codes/generate", """
            {"personId":"P001","purpose":"EMPLOYMENT"}
            """);
        assertEquals(201, genResp.statusCode());
        var code = mapper.readTree(genResp.body()).get("code").asText();

        var verifyPayload = """
            {"orgId":"ORG001","shareCode":"%s","dateOfBirth":"1985-03-22",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """.formatted(code);
        var verifyResp = post("/api/v1/verify/share-code", verifyPayload);
        assertEquals(200, verifyResp.statusCode());
        assertEquals("RIGHT_TO_WORK", mapper.readTree(verifyResp.body()).get("outcomeType").asText());
    }

    @Test
    void generate_thenVerifyTwice_bothSucceed() throws Exception {
        var genResp = post("/api/v1/codes/generate", """
            {"personId":"P001","purpose":"EMPLOYMENT"}
            """);
        var code = mapper.readTree(genResp.body()).get("code").asText();
        var payload = """
            {"orgId":"ORG001","shareCode":"%s","dateOfBirth":"1985-03-22",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """.formatted(code);

        var first = post("/api/v1/verify/share-code", payload);
        var second = post("/api/v1/verify/share-code", payload);

        assertEquals("RIGHT_TO_WORK", mapper.readTree(first.body()).get("outcomeType").asText());
        assertEquals("RIGHT_TO_WORK", mapper.readTree(second.body()).get("outcomeType").asText());
    }

    @Test
    void generate_thenVerifyTwice_auditHasThreeEvents() throws Exception {
        var auditSizeBefore = appCtx.audit.queryAll().size();

        var genResp = post("/api/v1/codes/generate", """
            {"personId":"P004","purpose":"ACCOMMODATION"}
            """);
        var code = mapper.readTree(genResp.body()).get("code").asText();
        var payload = """
            {"orgId":"ORG003","shareCode":"%s","dateOfBirth":"1968-05-01",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """.formatted(code);

        post("/api/v1/verify/share-code", payload);
        post("/api/v1/verify/share-code", payload);

        var events = appCtx.audit.queryAll();
        assertEquals(auditSizeBefore + 3, events.size());
    }

    @Test
    void auditTrail_masksPersonId() throws Exception {
        post("/api/v1/verify/share-code", """
            {"orgId":"ORG001","shareCode":"ABC123XY1","dateOfBirth":"1985-03-22",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        var event = appCtx.audit.queryAll().stream()
            .filter(e -> "SHARE_CODE_VERIFICATION".equals(e.eventType()))
            .findFirst()
            .orElseThrow();

        assertNotNull(event.maskedPersonId());
        assertTrue(event.maskedPersonId().startsWith("P001"),
            "Masked ID should start with P001: " + event.maskedPersonId());
        assertTrue(event.maskedPersonId().contains("****"),
            "Masked ID should contain ****: " + event.maskedPersonId());
    }

    @Test
    void lookupByPassport_thenGenerateCode_success() throws Exception {
        var lookupResp = get("/api/v1/persons/lookup?passport=EF3456789");
        assertEquals(200, lookupResp.statusCode());
        var personId = mapper.readTree(lookupResp.body()).get("personId").asText();

        var genResp = post("/api/v1/codes/generate",
            """
            {"personId":"%s","purpose":"ACCOMMODATION"}
            """.formatted(personId));
        assertEquals(201, genResp.statusCode());
        assertEquals(personId, mapper.readTree(genResp.body()).get("personId").asText());
    }
}

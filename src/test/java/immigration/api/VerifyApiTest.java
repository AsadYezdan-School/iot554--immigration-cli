package immigration.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VerifyApiTest extends ApiTestBase {

    // --- Share-code verification: success paths ---

    @Test
    void shareCode_validEmployer_returnsRightToWork() throws Exception {
        var resp = post("/api/v1/verify/share-code", """
            {"orgId":"ORG001","shareCode":"ABC123XY1","dateOfBirth":"1985-03-22",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        var body = mapper.readTree(resp.body());
        assertEquals("RIGHT_TO_WORK", body.get("outcomeType").asText());
        assertTrue(body.get("eligible").asBoolean());
    }

    @Test
    void shareCode_reusableWithinWindow() throws Exception {
        var payload = """
            {"orgId":"ORG001","shareCode":"ABC123XY1","dateOfBirth":"1985-03-22",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """;
        var first = post("/api/v1/verify/share-code", payload);
        var second = post("/api/v1/verify/share-code", payload);

        assertEquals(200, first.statusCode());
        assertEquals(200, second.statusCode());
        assertEquals("RIGHT_TO_WORK", mapper.readTree(second.body()).get("outcomeType").asText());
    }

    @Test
    void shareCode_landlord_returnsRightToRent() throws Exception {
        var resp = post("/api/v1/verify/share-code", """
            {"orgId":"ORG003","shareCode":"DEF456YZ2","dateOfBirth":"1968-05-01",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        assertEquals("RIGHT_TO_RENT", mapper.readTree(resp.body()).get("outcomeType").asText());
    }

    @Test
    void shareCode_visitorVisa_rightToWorkFalse() throws Exception {
        var resp = post("/api/v1/verify/share-code", """
            {"orgId":"ORG001","shareCode":"QRS678FG6","dateOfBirth":"2000-09-25",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        var body = mapper.readTree(resp.body());
        assertEquals("RIGHT_TO_WORK", body.get("outcomeType").asText());
        assertFalse(body.get("eligible").asBoolean());
    }

    // --- Share-code verification: rejection paths ---

    @Test
    void shareCode_expired_rejected() throws Exception {
        var resp = post("/api/v1/verify/share-code", """
            {"orgId":"ORG001","shareCode":"KLM012BC4","dateOfBirth":"1985-03-22",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        var body = mapper.readTree(resp.body());
        assertEquals("REJECTED", body.get("outcomeType").asText());
        assertTrue(body.get("reason").asText().contains("expired"));
    }

    @Test
    void shareCode_wrongDob_rejected() throws Exception {
        var resp = post("/api/v1/verify/share-code", """
            {"orgId":"ORG001","shareCode":"ABC123XY1","dateOfBirth":"1999-01-01",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        assertEquals("REJECTED", mapper.readTree(resp.body()).get("outcomeType").asText());
    }

    @Test
    void shareCode_purposeMismatch_rejected() throws Exception {
        var resp = post("/api/v1/verify/share-code", """
            {"orgId":"ORG001","shareCode":"DEF456YZ2","dateOfBirth":"1968-05-01",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        assertEquals("REJECTED", mapper.readTree(resp.body()).get("outcomeType").asText());
    }

    @Test
    void shareCode_unknownCode_rejected() throws Exception {
        var resp = post("/api/v1/verify/share-code", """
            {"orgId":"ORG001","shareCode":"ZZZ999XY1","dateOfBirth":"1985-03-22",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        assertEquals("REJECTED", mapper.readTree(resp.body()).get("outcomeType").asText());
    }

    @Test
    void shareCode_borderControlOnShareCodeRoute_rejected() throws Exception {
        var resp = post("/api/v1/verify/share-code", """
            {"orgId":"ORG005","shareCode":"ABC123XY1","dateOfBirth":"1985-03-22",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        assertEquals("REJECTED", mapper.readTree(resp.body()).get("outcomeType").asText());
    }

    @Test
    void shareCode_firstConfirmationRefused_returns422() throws Exception {
        var resp = post("/api/v1/verify/share-code", """
            {"orgId":"ORG001","shareCode":null,"dateOfBirth":null,
             "lawfulPurposeConfirmed":false,"dataProtectionConfirmed":false,"nonDiscriminatoryConfirmed":false}
            """);
        assertEquals(422, resp.statusCode());
        assertTrue(resp.body().contains("All confirmations must be accepted"));
    }

    @Test
    void shareCode_confirmationRefused_auditsEvent() throws Exception {
        post("/api/v1/verify/share-code", """
            {"orgId":"ORG001","shareCode":null,"dateOfBirth":null,
             "lawfulPurposeConfirmed":false,"dataProtectionConfirmed":false,"nonDiscriminatoryConfirmed":false}
            """);
        var events = appCtx.audit.queryAll();
        assertTrue(events.stream().anyMatch(e -> "CONFIRMATION_REFUSED".equals(e.eventType())));
    }

    @Test
    void shareCode_missingShareCode_returns400() throws Exception {
        var resp = post("/api/v1/verify/share-code", """
            {"orgId":"ORG001","dateOfBirth":"1985-03-22",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(400, resp.statusCode());
    }

    @Test
    void shareCode_missingOrgId_returns400() throws Exception {
        var resp = post("/api/v1/verify/share-code", """
            {"shareCode":"ABC123XY1","dateOfBirth":"1985-03-22",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(400, resp.statusCode());
    }

    @Test
    void shareCode_invalidFormat_rejected() throws Exception {
        var resp = post("/api/v1/verify/share-code", """
            {"orgId":"ORG001","shareCode":"BADCODE","dateOfBirth":"1985-03-22",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        assertEquals("REJECTED", mapper.readTree(resp.body()).get("outcomeType").asText());
    }

    @Test
    void shareCode_nullDateOfBirth_returns400() throws Exception {
        var resp = post("/api/v1/verify/share-code", """
            {"orgId":"ORG001","shareCode":"ABC123XY1",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(400, resp.statusCode());
    }

    @Test
    void shareCode_education_returnsRightToWork() throws Exception {
        var resp = post("/api/v1/verify/share-code", """
            {"orgId":"ORG004","shareCode":"EDU789AB3","dateOfBirth":"1985-03-22",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        var body = mapper.readTree(resp.body());
        assertEquals("RIGHT_TO_WORK", body.get("outcomeType").asText());
        assertTrue(body.get("eligible").asBoolean());
    }

    @Test
    void shareCode_personExistsNoVisa_rejected() throws Exception {
        var resp = post("/api/v1/verify/share-code", """
            {"orgId":"ORG001","shareCode":"OKL456ZZ9","dateOfBirth":"1990-07-15",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        var body = mapper.readTree(resp.body());
        assertEquals("REJECTED", body.get("outcomeType").asText());
        assertTrue(body.get("reason").asText().toLowerCase().contains("visa"));
    }

    @Test
    void shareCode_success_writesAuditEvent() throws Exception {
        post("/api/v1/verify/share-code", """
            {"orgId":"ORG001","shareCode":"ABC123XY1","dateOfBirth":"1985-03-22",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        var events = appCtx.audit.queryAll();
        assertTrue(events.stream().anyMatch(e ->
            "SHARE_CODE_VERIFICATION".equals(e.eventType()) && "APPROVED".equals(e.outcome())));
    }

    // --- Document verification: success paths ---

    @Test
    void document_borderControl_passport_returnsEntryPermission() throws Exception {
        var resp = post("/api/v1/verify/document", """
            {"orgId":"ORG005","documentNumber":"AB1234567","documentType":"PASSPORT",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        var body = mapper.readTree(resp.body());
        assertEquals("ENTRY_PERMISSION", body.get("outcomeType").asText());
        assertTrue(body.get("permitted").asBoolean());
    }

    @Test
    void document_lawEnforcement_passport_returnsStatusValidity() throws Exception {
        var resp = post("/api/v1/verify/document", """
            {"orgId":"ORG006","documentNumber":"AB1234567","documentType":"PASSPORT",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        var body = mapper.readTree(resp.body());
        assertEquals("STATUS_VALIDITY", body.get("outcomeType").asText());
        assertEquals("WORK", body.get("visaType").asText());
        assertTrue(body.get("valid").asBoolean());
    }

    // --- Document verification: rejection paths ---

    @Test
    void document_employer_rejected() throws Exception {
        var resp = post("/api/v1/verify/document", """
            {"orgId":"ORG001","documentNumber":"AB1234567","documentType":"PASSPORT",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        assertEquals("REJECTED", mapper.readTree(resp.body()).get("outcomeType").asText());
    }

    @Test
    void document_invalidPassportFormat_rejected() throws Exception {
        var resp = post("/api/v1/verify/document", """
            {"orgId":"ORG005","documentNumber":"AB123456","documentType":"PASSPORT",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        assertEquals("REJECTED", mapper.readTree(resp.body()).get("outcomeType").asText());
    }

    @Test
    void document_passportNotFound_rejected() throws Exception {
        var resp = post("/api/v1/verify/document", """
            {"orgId":"ORG005","documentNumber":"ZZ9999999","documentType":"PASSPORT",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        assertEquals("REJECTED", mapper.readTree(resp.body()).get("outcomeType").asText());
    }

    @Test
    void document_confirmationRefused_returns422() throws Exception {
        var resp = post("/api/v1/verify/document", """
            {"orgId":"ORG005","documentNumber":null,"documentType":null,
             "lawfulPurposeConfirmed":false,"dataProtectionConfirmed":false,"nonDiscriminatoryConfirmed":false}
            """);
        assertEquals(422, resp.statusCode());
    }

    @Test
    void document_invalidDocType_returns400() throws Exception {
        var resp = post("/api/v1/verify/document", """
            {"orgId":"ORG005","documentNumber":"AB1234567","documentType":"CARD",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(400, resp.statusCode());
    }

    @Test
    void document_borderControl_permit_returnsEntryPermission() throws Exception {
        var resp = post("/api/v1/verify/document", """
            {"orgId":"ORG005","documentNumber":"CD7654321","documentType":"PERMIT",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        var body = mapper.readTree(resp.body());
        assertEquals("ENTRY_PERMISSION", body.get("outcomeType").asText());
        assertTrue(body.get("permitted").asBoolean());
    }

    @Test
    void document_lawEnforcement_permit_returnsStatusValidity() throws Exception {
        var resp = post("/api/v1/verify/document", """
            {"orgId":"ORG006","documentNumber":"CD7654321","documentType":"PERMIT",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        var body = mapper.readTree(resp.body());
        assertEquals("STATUS_VALIDITY", body.get("outcomeType").asText());
        assertEquals("SKILLED_WORKER", body.get("visaType").asText());
    }

    @Test
    void document_unknownOrg_rejected() throws Exception {
        var resp = post("/api/v1/verify/document", """
            {"orgId":"ORG999","documentNumber":"AB1234567","documentType":"PASSPORT",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(200, resp.statusCode());
        var body = mapper.readTree(resp.body());
        assertEquals("REJECTED", body.get("outcomeType").asText());
        assertTrue(body.get("reason").asText().toLowerCase().contains("not recognised")
            || body.get("reason").asText().toLowerCase().contains("not recognized"));
    }

    @Test
    void document_missingDocumentNumber_returns400() throws Exception {
        var resp = post("/api/v1/verify/document", """
            {"orgId":"ORG005","documentType":"PASSPORT",
             "lawfulPurposeConfirmed":true,"dataProtectionConfirmed":true,"nonDiscriminatoryConfirmed":true}
            """);
        assertEquals(400, resp.statusCode());
    }
}

package immigration.cli;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DocumentMenuTest extends BaseCliTest {

    // --- Confirmation flows ---

    @Test
    void firstConfirmationRefused_showsRejected() {
        var out = runDocumentMenu(driver().type("ORG005").type("no"));
        assertTrue(out.contains("All confirmations must be accepted"), out);
    }

    @Test
    void confirmationRefusal_writesAuditEvent() {
        runDocumentMenu(driver().type("ORG005").type("no"));
        var events = auditRepo.queryAll();
        assertTrue(events.stream().anyMatch(e -> "CONFIRMATION_REFUSED".equals(e.eventType())),
            "CONFIRMATION_REFUSED event not in audit log");
    }

    // --- Happy paths ---

    @Test
    void borderControl_passport_showsEntryPermitted() {
        var out = runDocumentMenu(driver()
            .type("ORG005").type("yes").type("yes").type("yes")
            .type("PASSPORT").type("AB1234567"));
        assertTrue(out.contains("Entry permitted"), out);
    }

    @Test
    void borderControl_passport_permittedTrue() {
        var out = runDocumentMenu(driver()
            .type("ORG005").type("yes").type("yes").type("yes")
            .type("PASSPORT").type("AB1234567"));
        assertTrue(out.contains("Entry permitted: true"), out);
    }

    @Test
    void lawEnforcement_passport_showsStatusValidity() {
        var out = runDocumentMenu(driver()
            .type("ORG006").type("yes").type("yes").type("yes")
            .type("PASSPORT").type("AB1234567"));
        assertTrue(out.contains("Visa type"), out);
        assertTrue(out.contains("WORK"), out);
    }

    @Test
    void lawEnforcement_passport_visaValid() {
        var out = runDocumentMenu(driver()
            .type("ORG006").type("yes").type("yes").type("yes")
            .type("PASSPORT").type("AB1234567"));
        assertTrue(out.contains("valid: true"), out);
    }

    // --- Rejection paths ---

    @Test
    void invalidDocType_showsError() {
        var out = runDocumentMenu(driver()
            .type("ORG005").type("yes").type("yes").type("yes")
            .type("CARD"));
        assertTrue(out.contains("Invalid document type"), out);
    }

    @Test
    void unknownPassport_showsNotFound() {
        var out = runDocumentMenu(driver()
            .type("ORG005").type("yes").type("yes").type("yes")
            .type("PASSPORT").type("ZZ9999999"));
        assertTrue(out.toLowerCase().contains("not found"), out);
    }

    @Test
    void employerOnDocumentRoute_showsRejection() {
        var out = runDocumentMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type("PASSPORT").type("AB1234567"));
        assertTrue(out.contains("REJECTED"), out);
    }

    // --- Audit side effect ---

    @Test
    void success_writesAuditEvent() {
        runDocumentMenu(driver()
            .type("ORG005").type("yes").type("yes").type("yes")
            .type("PASSPORT").type("AB1234567"));

        var events = auditRepo.queryAll();
        assertTrue(events.stream().anyMatch(e ->
            "DOCUMENT_VERIFICATION".equals(e.eventType()) && "APPROVED".equals(e.outcome())),
            "No approved DOCUMENT_VERIFICATION event found");
    }
}

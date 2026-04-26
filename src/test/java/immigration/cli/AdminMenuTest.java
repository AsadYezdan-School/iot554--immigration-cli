package immigration.cli;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdminMenuTest extends BaseCliTest {

    // --- Confirmation flows ---

    @Test
    void firstConfirmationRefused_showsRejected() {
        var out = runAdminMenu(driver().type("ORG001").type("no"));
        assertTrue(out.contains("All confirmations must be accepted"), out);
    }

    @Test
    void secondConfirmationRefused_showsRejected() {
        var out = runAdminMenu(driver().type("ORG001").type("yes").type("no"));
        assertTrue(out.contains("All confirmations must be accepted"), out);
    }

    @Test
    void thirdConfirmationRefused_showsRejected() {
        var out = runAdminMenu(driver().type("ORG001").type("yes").type("yes").type("no"));
        assertTrue(out.contains("All confirmations must be accepted"), out);
    }

    @Test
    void confirmationRefusal_writesAuditEvent() {
        runAdminMenu(driver().type("ORG001").type("no"));
        var events = auditRepo.queryAll();
        assertTrue(events.stream().anyMatch(e -> "CONFIRMATION_REFUSED".equals(e.eventType())),
            "CONFIRMATION_REFUSED event not in audit log");
    }

    // --- Happy paths ---

    @Test
    void validCode_employer_showsRightToWork() {
        var out = runAdminMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type("ABC123XY1").type("1985-03-22"));
        assertTrue(out.contains("Right to work"), out);
    }

    @Test
    void validCode_employer_eligibleTrue() {
        var out = runAdminMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type("ABC123XY1").type("1985-03-22"));
        assertTrue(out.contains("Right to work: true"), out);
    }

    @Test
    void validCode_landlord_showsRightToRent() {
        var out = runAdminMenu(driver()
            .type("ORG003").type("yes").type("yes").type("yes")
            .type("DEF456YZ2").type("1968-05-01"));
        assertTrue(out.contains("Right to rent"), out);
    }

    @Test
    void visitorVisa_employer_rightToWorkFalse() {
        var out = runAdminMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type("QRS678FG6").type("2000-09-25"));
        assertTrue(out.contains("Right to work: false"), out);
    }

    // --- Rejection paths ---

    @Test
    void expiredCode_showsExpired() {
        var out = runAdminMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type("KLM012BC4").type("1985-03-22"));
        assertTrue(out.toLowerCase().contains("expired"), out);
    }

    @Test
    void alreadyUsedCode_showsUsed() {
        var out = runAdminMenu(driver()
            .type("ORG003").type("yes").type("yes").type("yes")
            .type("NOP345DE5").type("1968-05-01"));
        assertTrue(out.toLowerCase().contains("used"), out);
    }

    @Test
    void wrongDob_showsDateOfBirth() {
        var out = runAdminMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type("ABC123XY1").type("1999-01-01"));
        assertTrue(out.toLowerCase().contains("date of birth"), out);
    }

    @Test
    void purposeMismatch_showsRejection() {
        var out = runAdminMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type("DEF456YZ2").type("1968-05-01"));
        assertTrue(out.contains("REJECTED"), out);
        assertTrue(out.contains("Purpose"), out);
    }

    // --- File side effects ---

    @Test
    void success_marksCodeUsedInRepository() {
        runAdminMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type("ABC123XY1").type("1985-03-22"));

        var code = shareCodeRepo.findByCode("ABC123XY1");
        assertTrue(code.isPresent());
        assertTrue(code.get().used(), "Share code ABC123XY1 should be marked used after verification");
    }

    @Test
    void success_writesAuditEvent() {
        runAdminMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type("ABC123XY1").type("1985-03-22"));

        var events = auditRepo.queryAll();
        assertFalse(events.isEmpty(), "Audit log should have at least one entry");
        assertTrue(events.stream().anyMatch(e ->
            "SHARE_CODE_VERIFICATION".equals(e.eventType()) && "APPROVED".equals(e.outcome())),
            "No approved SHARE_CODE_VERIFICATION event found");
    }
}

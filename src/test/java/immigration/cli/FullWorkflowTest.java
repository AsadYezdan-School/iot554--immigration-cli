package immigration.cli;

import org.junit.jupiter.api.Test;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

class FullWorkflowTest extends BaseCliTest {

    private String extractGeneratedCode(String output) {
        var m = Pattern.compile("Share code generated:\\s+([A-Z0-9]{9})").matcher(output);
        assertTrue(m.find(), "Could not extract generated code from output: " + output);
        return m.group(1);
    }

    @Test
    void generateThenUse_success() {
        // ShareCodeMenu generates a new EMPLOYMENT code for P001
        var generateOut = runShareCodeMenu(driver().type("P001").type("EMPLOYMENT"));
        var code = extractGeneratedCode(generateOut);

        // AdminMenu verifies the generated code
        var verifyOut = runAdminMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type(code).type("1985-03-22"));

        assertTrue(verifyOut.contains("Right to work: true"), verifyOut);
    }

    @Test
    void generateThenUseTwice_secondAttemptFails() {
        // Generate code
        var generateOut = runShareCodeMenu(driver().type("P001").type("EMPLOYMENT"));
        var code = extractGeneratedCode(generateOut);

        // First verification — should succeed
        runAdminMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type(code).type("1985-03-22"));

        // Second verification with same code — should fail because code is now marked used
        var secondOut = runAdminMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type(code).type("1985-03-22"));

        assertTrue(secondOut.toLowerCase().contains("used"), secondOut);
    }

    @Test
    void auditTrailIntegrity_afterShareCodeVerification() {
        runAdminMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type("ABC123XY1").type("1985-03-22"));

        var events = auditRepo.queryAll();
        assertFalse(events.isEmpty(), "Audit log must not be empty after verification");

        var event = events.stream()
            .filter(e -> "SHARE_CODE_VERIFICATION".equals(e.eventType()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("SHARE_CODE_VERIFICATION event not found"));

        assertEquals("APPROVED", event.outcome());
        assertEquals("ORG001", event.organisationId());
        assertNotNull(event.maskedPersonId(), "Masked person ID should not be null");
        assertTrue(event.maskedPersonId().startsWith("P001"),
            "Masked ID should start with P001: " + event.maskedPersonId());
        assertTrue(event.maskedPersonId().contains("****"),
            "Masked ID should contain ****: " + event.maskedPersonId());
        assertEquals("ABC123XY1", event.shareCode());
    }

    @Test
    void generateViaPassport_thenVerify_success() {
        // ShareCodeMenu looks up P001 by passport number and generates a code
        var generateOut = runShareCodeMenu(driver().type("").type("AB1234567").type("EMPLOYMENT"));
        assertTrue(generateOut.contains("Found person: Emma Harrison"), generateOut);
        var code = extractGeneratedCode(generateOut);

        // Verify with the new code
        var verifyOut = runAdminMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type(code).type("1985-03-22"));

        assertTrue(verifyOut.contains("Right to work: true"), verifyOut);
    }
}

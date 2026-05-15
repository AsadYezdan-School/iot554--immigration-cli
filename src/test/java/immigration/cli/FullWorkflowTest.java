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
        var generateOut = runShareCodeMenu(driver().type("P001").type("EMPLOYMENT"));
        var code = extractGeneratedCode(generateOut);

        var verifyOut = runAdminMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type(code).type("1985-03-22"));

        assertTrue(verifyOut.contains("Right to work: true"), verifyOut);
    }

    @Test
    void generateThenUseTwice_bothSucceed() {
        var generateOut = runShareCodeMenu(driver().type("P001").type("EMPLOYMENT"));
        var code = extractGeneratedCode(generateOut);

        var input = driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type(code).type("1985-03-22");

        var first = runAdminMenu(input);
        var second = runAdminMenu(input);

        assertTrue(first.contains("Right to work: true"), first);
        assertTrue(second.contains("Right to work: true"), second);
    }

    @Test
    void auditTrailIntegrity_afterShareCodeVerification() {
        runAdminMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type("ABC123XY1").type("1985-03-22"));

        var events = auditRepo.queryAll();
        assertFalse(events.isEmpty(), "Audit log must not be empty after verification");

        var event = events.stream()
            .filter(e -> "SHARE_CODE_VERIFICATION".equals(e.eventType())
                      && "ABC123XY1".equals(e.shareCode()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("SHARE_CODE_VERIFICATION event for ABC123XY1 not found"));

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
        var generateOut = runShareCodeMenu(driver().type("").type("AB1234567").type("EMPLOYMENT"));
        assertTrue(generateOut.contains("Found person: Emma Harrison"), generateOut);
        var code = extractGeneratedCode(generateOut);

        var verifyOut = runAdminMenu(driver()
            .type("ORG001").type("yes").type("yes").type("yes")
            .type(code).type("1985-03-22"));

        assertTrue(verifyOut.contains("Right to work: true"), verifyOut);
    }
}

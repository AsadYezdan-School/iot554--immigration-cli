package immigration.cli;

import org.junit.jupiter.api.Test;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

class ShareCodeMenuTest extends BaseCliTest {

    // --- Happy paths ---

    @Test
    void byPersonId_generatesCode() {
        var out = runShareCodeMenu(driver().type("P001").type("EMPLOYMENT"));
        assertTrue(out.contains("Share code generated:"), out);
    }

    @Test
    void byPersonId_generatedCodeHasCorrectFormat() {
        var out = runShareCodeMenu(driver().type("P001").type("EMPLOYMENT"));
        var matcher = Pattern.compile("[A-Z0-9]{9}").matcher(out);
        assertTrue(matcher.find(), "No 9-char alphanumeric code found in output: " + out);
    }

    @Test
    void byPassport_showsFoundPerson() {
        var out = runShareCodeMenu(driver().type("").type("AB1234567").type("EMPLOYMENT"));
        assertTrue(out.contains("Found person: Emma Harrison"), out);
    }

    @Test
    void byPassport_generatesCode() {
        var out = runShareCodeMenu(driver().type("").type("AB1234567").type("EMPLOYMENT"));
        assertTrue(out.contains("Share code generated:"), out);
    }

    @Test
    void byPersonId_showsPurposeInOutput() {
        var out = runShareCodeMenu(driver().type("P001").type("EMPLOYMENT"));
        assertTrue(out.contains("EMPLOYMENT"), out);
    }

    // --- Rejection paths ---

    @Test
    void unknownPersonId_showsError() {
        var out = runShareCodeMenu(driver().type("P999"));
        assertTrue(out.contains("No person found with ID: P999"), out);
    }

    @Test
    void unknownPassport_showsError() {
        var out = runShareCodeMenu(driver().type("").type("ZZ0000000"));
        assertTrue(out.contains("No person found with passport number:"), out);
    }

    @Test
    void invalidPurpose_showsError() {
        var out = runShareCodeMenu(driver().type("P001").type("HOUSING"));
        assertTrue(out.contains("Invalid purpose."), out);
    }

    // --- File side effects ---

    @Test
    void generatedCode_savedToRepository() {
        var out = runShareCodeMenu(driver().type("P001").type("EMPLOYMENT"));

        var matcher = Pattern.compile("Share code generated:\\s+([A-Z0-9]{9})").matcher(out);
        assertTrue(matcher.find(), "Could not extract code from: " + out);
        var code = matcher.group(1);

        var found = shareCodeRepo.findByCode(code);
        assertTrue(found.isPresent(), "Generated code not found in repository");
        assertEquals("P001", found.get().personId());
        assertEquals("EMPLOYMENT", found.get().purpose());
        assertFalse(found.get().used());
    }

    @Test
    void generatedCode_writesAuditEvent() {
        runShareCodeMenu(driver().type("P001").type("EMPLOYMENT"));

        var events = auditRepo.queryAll();
        assertTrue(events.stream().anyMatch(e -> "SHARE_CODE_GENERATED".equals(e.eventType())),
            "SHARE_CODE_GENERATED event not in audit log");
    }
}

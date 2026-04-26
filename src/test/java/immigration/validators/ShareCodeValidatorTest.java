package immigration.validators;

import immigration.models.ShareCode;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import static org.junit.jupiter.api.Assertions.*;

class ShareCodeValidatorTest {

    // --- validateFormat ---

    @Test
    void validFormat_passes() {
        // EP: valid 9-char uppercase alphanumeric
        var result = ShareCodeValidator.validateFormat("ABC123XY1");
        assertTrue(result.ok());
    }

    @Test
    void tooShort_fails() {
        // BVA: 8 chars (boundary below 9)
        var result = ShareCodeValidator.validateFormat("ABCDEFGH");
        assertFalse(result.ok());
        assertNotNull(result.reason());
    }

    @Test
    void tooLong_fails() {
        // BVA: 10 chars (boundary above 9)
        var result = ShareCodeValidator.validateFormat("ABCDEFGHIJ");
        assertFalse(result.ok());
    }

    @Test
    void lowercase_fails() {
        // EP: lowercase is invalid class
        var result = ShareCodeValidator.validateFormat("abc123xy1");
        assertFalse(result.ok());
    }

    @Test
    void withSymbols_fails() {
        // EP: special characters are invalid class
        var result = ShareCodeValidator.validateFormat("ABC-23XY1");
        assertFalse(result.ok());
    }

    @Test
    void nullCode_fails() {
        // EP: null input
        var result = ShareCodeValidator.validateFormat(null);
        assertFalse(result.ok());
    }

    @Test
    void emptyCode_fails() {
        // BVA: empty string
        var result = ShareCodeValidator.validateFormat("");
        assertFalse(result.ok());
    }

    // --- validateNotExpired ---

    @Test
    void futureExpiry_passes() {
        // EP: expiry in the future
        var sc = shareCodeWith(Instant.now().plus(30, ChronoUnit.DAYS).toString(), false);
        assertTrue(ShareCodeValidator.validateNotExpired(sc).ok());
    }

    @Test
    void pastExpiry_fails() {
        // EP: expiry in the past
        var sc = shareCodeWith(Instant.now().minus(1, ChronoUnit.DAYS).toString(), false);
        assertFalse(ShareCodeValidator.validateNotExpired(sc).ok());
    }

    // --- validateNotUsed ---

    @Test
    void unusedCode_passes() {
        // ST: initial state — not used
        var sc = shareCodeWith(Instant.now().plus(1, ChronoUnit.DAYS).toString(), false);
        assertTrue(ShareCodeValidator.validateNotUsed(sc).ok());
    }

    @Test
    void usedCode_fails() {
        // ST: after-use state — used=true
        var sc = shareCodeWith(Instant.now().plus(1, ChronoUnit.DAYS).toString(), true);
        var result = ShareCodeValidator.validateNotUsed(sc);
        assertFalse(result.ok());
        assertNotNull(result.reason());
    }

    private static ShareCode shareCodeWith(String expiresAt, boolean used) {
        return new ShareCode("ABCDEFGH1", "P001", "EMPLOYMENT",
            Instant.now().toString(), expiresAt, used);
    }
}

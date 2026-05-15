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
        var result = ShareCodeValidator.validateFormat("ABC123XY1");
        assertTrue(result.ok());
    }

    @Test
    void tooShort_fails() {
        var result = ShareCodeValidator.validateFormat("ABCDEFGH");
        assertFalse(result.ok());
        assertNotNull(result.reason());
    }

    @Test
    void tooLong_fails() {
        var result = ShareCodeValidator.validateFormat("ABCDEFGHIJ");
        assertFalse(result.ok());
    }

    @Test
    void lowercase_fails() {
        var result = ShareCodeValidator.validateFormat("abc123xy1");
        assertFalse(result.ok());
    }

    @Test
    void withSymbols_fails() {
        var result = ShareCodeValidator.validateFormat("ABC-23XY1");
        assertFalse(result.ok());
    }

    @Test
    void nullCode_fails() {
        var result = ShareCodeValidator.validateFormat(null);
        assertFalse(result.ok());
    }

    @Test
    void emptyCode_fails() {
        var result = ShareCodeValidator.validateFormat("");
        assertFalse(result.ok());
    }

    // --- validateNotExpired ---

    @Test
    void futureExpiry_passes() {
        var sc = shareCodeWith(Instant.now().plus(30, ChronoUnit.DAYS).toString());
        assertTrue(ShareCodeValidator.validateNotExpired(sc).ok());
    }

    @Test
    void atBoundary_notYetExpired_passes() {
        // isAfter is strict — a code expiring just in the future is still valid
        var sc = shareCodeWith(Instant.now().plus(100, ChronoUnit.MILLIS).toString());
        assertTrue(ShareCodeValidator.validateNotExpired(sc).ok());
    }

    @Test
    void pastExpiry_fails() {
        var sc = shareCodeWith(Instant.now().minus(1, ChronoUnit.DAYS).toString());
        assertFalse(ShareCodeValidator.validateNotExpired(sc).ok());
    }

    private static ShareCode shareCodeWith(String expiresAt) {
        return new ShareCode("ABCDEFGH1", "P001", "EMPLOYMENT",
            Instant.now().toString(), expiresAt);
    }
}

package immigration.validators;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DobValidatorTest {

    @Test
    void matchingDates_passes() {
        assertTrue(DobValidator.validate("1985-03-22", "1985-03-22").ok());
    }

    @Test
    void mismatchedDates_fails() {
        // EP: correct format but wrong date
        var result = DobValidator.validate("1999-01-01", "1985-03-22");
        assertFalse(result.ok());
        assertNotNull(result.reason());
    }

    @Test
    void invalidFormat_fails() {
        // EP: non-ISO date format
        assertFalse(DobValidator.validate("22/03/1985", "1985-03-22").ok());
    }

    @Test
    void nullInput_fails() {
        assertFalse(DobValidator.validate(null, "1985-03-22").ok());
    }

    @Test
    void blankInput_fails() {
        // BVA: empty string
        assertFalse(DobValidator.validate("   ", "1985-03-22").ok());
    }

    @Test
    void partialDate_fails() {
        // EP: incomplete ISO date
        assertFalse(DobValidator.validate("1985-03", "1985-03-22").ok());
    }

    @Test
    void leadingTrailingSpaces_areStripped() {
        // Validator trims input before parsing
        assertTrue(DobValidator.validate("  1985-03-22  ", "1985-03-22").ok());
    }

    @Test
    void dayBoundary_matchesExactly() {
        // BVA: 1 day off from the correct date
        assertFalse(DobValidator.validate("1985-03-23", "1985-03-22").ok());
    }
}
